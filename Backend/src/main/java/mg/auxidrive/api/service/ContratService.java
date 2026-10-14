package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.Branche;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.DemandeSouscription;
import mg.auxidrive.api.model.GarantieType;
import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.model.ParametresPrime;
import mg.auxidrive.api.model.PrimeRequest;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.TypeOffre;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.BrancheParametresCalculRepository;
import mg.auxidrive.api.repository.CodeTaxePoliceRepository;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.repository.GarantieTypeRepository;
import mg.auxidrive.api.repository.OffreRepository;
import mg.auxidrive.api.repository.VehiculeRepository;
import mg.auxidrive.api.service.PrimeCalculator.CalculPrimeException;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Souscription et cycle de vie des contrats.
 *
 * Un contrat passe par trois états : EN_ATTENTE à la demande, puis VALIDE ou REFUSE sur décision
 * de l'agent. Le numéro de police et la facture ne sont produits qu'à la validation : un contrat
 * refusé ne consomme donc pas de numéro.
 *
 * Le montant TTC n'est jamais reçu du client : il est recalculé par PrimeCalculator à partir de la
 * prime nette saisie, de la branche et du code taxe.
 */
@Service
public class ContratService {

    /**
     * Prime nette par défaut lorsque le client n'en transmet pas. Ce n'est qu'une valeur d'attente :
     * la prime réelle est saisie par l'agent au moment du calcul, à la validation. Le nombre de mois
     * ne la multiplie pas : la durée fixe l'échéance, pas le montant.
     */
    public static final double PRIME_NETTE_DEFAUT = 20000d;

    /** Durée retenue si ni le contrat, ni l'offre ne précisent de durée. */
    private static final int DUREE_DEFAUT_MOIS = 12;

    /** Alphabet des trois lettres du numéro de police. */
    private static final String LETTRES_POLICE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final ContratRepository contratRepository;
    private final ContratAutoRepository contratAutoRepository;
    private final OffreRepository offreRepository;
    private final VehiculeRepository vehiculeRepository;
    private final AssureRepository assureRepository;
    private final BrancheParametresCalculRepository parametresRepository;
    private final CodeTaxePoliceRepository codeTaxeRepository;
    private final AutoCodeTarifRepository codeTarifRepository;
    private final GarantieTypeRepository garantieRepository;
    private final FactureService factureService;

    public ContratService(ContratRepository contratRepository,
                          ContratAutoRepository contratAutoRepository,
                          OffreRepository offreRepository,
                          VehiculeRepository vehiculeRepository,
                          AssureRepository assureRepository,
                          BrancheParametresCalculRepository parametresRepository,
                          CodeTaxePoliceRepository codeTaxeRepository,
                          AutoCodeTarifRepository codeTarifRepository,
                          GarantieTypeRepository garantieRepository,
                          FactureService factureService) {
        this.contratRepository = contratRepository;
        this.contratAutoRepository = contratAutoRepository;
        this.offreRepository = offreRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.assureRepository = assureRepository;
        this.parametresRepository = parametresRepository;
        this.codeTaxeRepository = codeTaxeRepository;
        this.codeTarifRepository = codeTarifRepository;
        this.garantieRepository = garantieRepository;
        this.factureService = factureService;
    }

    // ---- lecture ----

    @Transactional(readOnly = true)
    public List<Contrat> lister() {
        return contratRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Contrat> listerParAssure(Long idAssure) {
        return contratRepository.findByAssure_IdAssureOrderByIdContrat(idAssure);
    }

    @Transactional(readOnly = true)
    public Contrat trouver(Long id) {
        return contratRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));
    }

    /** Contrat appartenant bien à l'assuré : contrôle d'appartenance du portail client. */
    @Transactional(readOnly = true)
    public Contrat trouverPourAssure(Long id, Long idAssure) {
        Contrat contrat = trouver(id);
        if (contrat.getAssure() == null || !idAssure.equals(contrat.getAssure().getIdAssure())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable");
        }
        return contrat;
    }

    @Transactional(readOnly = true)
    public List<ContratAuto> vehiculesDuContrat(Long idContrat) {
        trouver(idContrat);
        return contratAutoRepository.findByContrat_IdContrat(idContrat);
    }

    // ---- tarification ----

    /**
     * Calcule une prime sans rien enregistrer : sert à afficher le détail du montant TTC
     * avant que l'assuré ne valide sa demande, et à l'agent avant de la valider.
     */
    @Transactional(readOnly = true)
    public Detail simuler(DemandeSouscription demande) {
        Preparation p = preparer(demande);
        resoudreGaranties(demande.getCdGaranties(), p.branche());
        return calculer(p, demande.getTauxCommission());
    }

    /**
     * Crée la demande de souscription à l'état EN_ATTENTE.
     * Le montant est calculé puis conservé : le client ne peut pas imposer de montant.
     */
    @Transactional
    public Contrat souscrire(DemandeSouscription demande, Long idAssure, String canal) {
        Preparation p = preparer(demande);
        Detail detail = calculer(p, demande.getTauxCommission());

        Assure assure = assureRepository.findById(idAssure)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable"));

        Contrat contrat = new Contrat();
        contrat.setAssure(assure);
        contrat.setOffre(p.offre());
        contrat.setBranche(p.branche());
        contrat.setCodeAgence(demande.getCodeAgence());
        contrat.setCanalSouscription(canal);
        contrat.setDateSouscription(LocalDate.now());
        // la date d'effet n'est pas fixée ici : elle le sera à la validation par l'agent
        contrat.setDureeMois(p.dureeMois());
        contrat.setStatutContrat(StatutContrat.EN_ATTENTE);
        contrat.setCoefficientBonusMalus(p.coefficient());
        contrat.setPrimeNetteCourante(p.primeNetteAjustee());
        contrat.setAccessoires(detail.accessoires());
        contrat.setPrimeTtcCourante(detail.ttc());
        contrat.setTauxCommission(detail.tauxCommission());
        contrat.setMontantCommission(detail.commission());
        contrat.setGaranties(resoudreGaranties(demande.getCdGaranties(), p.branche()));
        contrat = contratRepository.save(contrat);

        for (Vehicule vehicule : p.vehicules()) {
            ContratAuto lien = new ContratAuto();
            lien.setContrat(contrat);
            lien.setVehicule(vehicule);
            lien.setDateAjout(LocalDate.now());
            contratAutoRepository.save(lien);
        }
        return contrat;
    }

    /**
     * Valide une demande avec les valeurs courantes du contrat (variante sans calcul explicite).
     */
    @Transactional
    public Contrat valider(Long idContrat) {
        return valider(idContrat, null);
    }

    /**
     * Valide une demande : le contrat reçoit son numéro de police, sa date d'effet (le jour de la
     * validation) et sa date d'expiration (date d'effet + durée choisie), et sa facture de
     * souscription est émise.
     *
     * <p>La prime est recalculée par le serveur à partir de la prime nette et des accessoires
     * saisis par l'agent ({@code requete}) : la facture correspond ainsi exactement au calcul
     * affiché. Un champ absent de la requête reprend la valeur courante du contrat.
     */
    @Transactional
    public Contrat valider(Long idContrat, PrimeRequest requete) {
        Contrat contrat = trouver(idContrat);
        if (contrat.getStatutContrat() != StatutContrat.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une demande en attente peut être validée");
        }

        Detail detail = calculerPourContrat(contrat, requete);

        // la date d'effet est celle de la validation : le contrat prend effet immédiatement
        LocalDate dateValidation = LocalDate.now();
        Integer dureeMois = dureeMoisDuContrat(contrat);

        contrat.setNumPolice(nextNumPolice());
        contrat.setStatutContrat(StatutContrat.VALIDE);
        contrat.setPrimeNetteCourante(detail.base() - detail.accessoires());
        contrat.setAccessoires(detail.accessoires());
        contrat.setPrimeTtcCourante(detail.ttc());
        contrat.setTauxCommission(detail.tauxCommission());
        contrat.setMontantCommission(detail.commission());
        contrat.setDateEffet(dateValidation);
        contrat.setDureeMois(dureeMois);
        contrat.setDateExpiration(dateValidation.plusMonths(dureeMois));
        contrat = contratRepository.save(contrat);

        factureService.emettreSouscription(contrat, detail);
        return contrat;
    }

    @Transactional
    public Contrat refuser(Long idContrat) {
        Contrat contrat = trouver(idContrat);
        if (contrat.getStatutContrat() != StatutContrat.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une demande en attente peut être refusée");
        }
        contrat.setStatutContrat(StatutContrat.REFUSE);
        return contratRepository.save(contrat);
    }

    /**
     * Reconduction tacite d'un contrat validé à son échéance : le même contrat (donc le même
     * numéro de police) est prolongé d'une durée, la prime est recalculée à partir des tarifs
     * du jour et une facture de renouvellement est émise.
     *
     * La notification d'échéance est remise à zéro : le rappel J-3 repartira pour la nouvelle
     * échéance.
     */
    @Transactional
    public Contrat renouveler(Contrat contrat) {
        if (contrat.getStatutContrat() != StatutContrat.VALIDE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seul un contrat validé peut être renouvelé");
        }
        Detail detail = calculerPourContrat(contrat, null);

        Integer dureeMois = dureeMoisDuContrat(contrat);
        LocalDate ancienneExpiration = contrat.getDateExpiration();
        contrat.setDateEffet(ancienneExpiration == null ? LocalDate.now() : ancienneExpiration);
        contrat.setDateExpiration(contrat.getDateEffet().plusMonths(dureeMois));
        contrat.setPrimeNetteCourante(detail.base() - detail.accessoires());
        contrat.setAccessoires(detail.accessoires());
        contrat.setPrimeTtcCourante(detail.ttc());
        contrat.setTauxCommission(detail.tauxCommission());
        contrat.setMontantCommission(detail.commission());
        contrat.setDateNotifEcheance(null);
        contrat = contratRepository.save(contrat);

        factureService.emettreRenouvellement(contrat, detail);
        return contrat;
    }

    /**
     * Vérifie qu'un véhicule appartient bien à l'assuré.
     * Appelé avant toute souscription faite par un client, pour qu'il ne puisse pas couvrir
     * un véhicule d'un autre assuré. Un véhicule qui n'est pas le sien est signalé introuvable.
     */
    @Transactional(readOnly = true)
    public void vehiculeAppartientA(Long idVehicule, Long idAssure) {
        Vehicule v = vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable : " + idVehicule));
        if (v.getAssure() == null || !idAssure.equals(v.getAssure().getIdAssure())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable : " + idVehicule);
        }
    }

    /**
     * Paramètres de préparation du calcul de prime d'un contrat existant, à destination de l'agent :
     * code taxe, taux appliqués et valeurs par défaut des champs qu'il peut saisir.
     */
    @Transactional(readOnly = true)
    public ParametresPrime parametresPrime(Long idContrat) {
        Contrat contrat = trouver(idContrat);
        BrancheParametresCalcul parametres = parametresDuContrat(contrat);
        AutoCodeTarif codeTarif = codeTarifDuContrat(contrat);
        CodeTaxePolice codeTaxe = codeTaxeDe(codeTarif);

        double tauxCommission = contrat.getTauxCommission() != null && contrat.getTauxCommission() > 0
                ? contrat.getTauxCommission()
                : (parametres.getCommissionTotale() == null ? 0d : parametres.getCommissionTotale());
        boolean appliqueTacava = vrai(codeTaxe.getAppliquerTacava()) && vrai(codeTarif.getAvecTacava());

        return new ParametresPrime(
                codeTaxe.getCodeTaxe(),
                codeTaxe.getLibelleCodeTaxe(),
                contrat.getPrimeNetteCourante(),
                contrat.getAccessoires() != null ? contrat.getAccessoires() : parametres.getAccessoires(),
                tauxCommission,
                parametres.getTauxTe(),
                codeTaxe.getTauxTaxe(),
                parametres.getTauxTacava(),
                vrai(codeTaxe.getAppliquerTe()),
                vrai(codeTaxe.getAppliquerTva()),
                appliqueTacava);
    }

    /**
     * Calcule la prime d'un contrat sans rien enregistrer : sert à l'agent pour prévisualiser le
     * montant TTC à partir de la prime nette et des accessoires qu'il saisit.
     */
    @Transactional(readOnly = true)
    public Detail calculerPrime(Long idContrat, PrimeRequest requete) {
        return calculerPourContrat(trouver(idContrat), requete);
    }

    // ---- interne ----

    /**
     * Calcule la prime d'un contrat déjà enregistré, en résolvant la branche, le code taxe et le
     * code tarifaire à partir des véhicules rattachés.
     *
     * <p>Les valeurs saisies par l'agent ({@code requete}) priment ; à défaut, la prime nette et le
     * taux de commission du contrat, ainsi que les accessoires déjà enregistrés puis ceux de la
     * branche, sont utilisés.
     */
    private Detail calculerPourContrat(Contrat contrat, PrimeRequest requete) {
        BrancheParametresCalcul parametres = parametresDuContrat(contrat);
        AutoCodeTarif codeTarif = codeTarifDuContrat(contrat);
        CodeTaxePolice codeTaxe = codeTaxeDe(codeTarif);

        double primeNette = requete != null && requete.primeNette() != null
                ? requete.primeNette()
                : (contrat.getPrimeNetteCourante() == null ? 0d : contrat.getPrimeNetteCourante());
        Double accessoires = requete != null && requete.accessoires() != null
                ? requete.accessoires()
                : contrat.getAccessoires();
        Double tauxCommission = requete != null && requete.tauxCommission() != null
                ? requete.tauxCommission()
                : contrat.getTauxCommission();

        try {
            return PrimeCalculator.calculer(primeNette, accessoires, parametres, codeTaxe, codeTarif, tauxCommission);
        } catch (CalculPrimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** Paramètres de calcul de la branche du contrat, ou une erreur si elle n'en a pas. */
    private BrancheParametresCalcul parametresDuContrat(Contrat contrat) {
        if (contrat.getBranche() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce contrat n'est rattaché à aucune branche");
        }
        return parametresRepository.findByBranche_CdBranche(contrat.getBranche().getCdBranche())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucun paramètre de calcul pour la branche " + contrat.getBranche().getCdBranche()));
    }

    /** Code tarifaire du premier véhicule couvert par le contrat. */
    private AutoCodeTarif codeTarifDuContrat(Contrat contrat) {
        List<ContratAuto> liens = contratAutoRepository.findByContrat_IdContrat(contrat.getIdContrat());
        if (liens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce contrat n'a aucun véhicule rattaché");
        }
        Vehicule premier = liens.get(0).getVehicule();
        return premier == null ? null : premier.getCodeTarif();
    }

    /** Code taxe porté par un code tarifaire ; une erreur si le véhicule n'en porte aucun. */
    private CodeTaxePolice codeTaxeDe(AutoCodeTarif codeTarif) {
        CodeTaxePolice codeTaxe = codeTarif == null ? null : codeTarif.getCodeTaxePolice();
        if (codeTaxe == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le véhicule couvert ne porte aucun code taxe");
        }
        return codeTaxe;
    }

    private static boolean vrai(Boolean b) {
        return b != null && b;
    }

    /** Convertit une erreur de calcul en erreur HTTP 400 pour que le frontend affiche le message. */
    private Detail calculer(Preparation p, Double tauxCommission) {
        try {
            return PrimeCalculator.calculer(p.primeNetteAjustee(), p.accessoires(), p.parametres(),
                    p.codeTaxe(), p.codeTarif(), tauxCommission);
        } catch (CalculPrimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Vérifie la demande et rassemble tout ce dont le calcul a besoin.
     * Regroupe les contrôles d'accès aux véhicules, le nombre de véhicules autorisé par l'offre
     * et la résolution du code taxe, du code tarifaire et des paramètres de branche.
     */
    private Preparation preparer(DemandeSouscription demande) {
        if (demande == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Demande vide");
        }
        if (demande.getIdOffre() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'offre est obligatoire");
        }

        Offre offre = offreRepository.findById(demande.getIdOffre())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));

        Integer dureeMois = resoudreDuree(demande, offre);

        // prime nette : valeur d'attente si le client n'en fournit pas ; la durée ne la multiplie pas
        double primeNetteSaisie = demande.getPrimeNette() == null
                ? PRIME_NETTE_DEFAUT
                : demande.getPrimeNette();

        Branche branche = offre.getBranche();
        if (branche == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cette offre n'est rattachée à aucune branche, la prime ne peut pas être calculée");
        }
        BrancheParametresCalcul parametres = parametresRepository.findByBranche_CdBranche(branche.getCdBranche())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Aucun paramètre de calcul pour la branche " + branche.getCdBranche()));

        List<Vehicule> vehicules = chargerVehicules(demande, offre);
        controllerVehicules(offre, vehicules);

        CodeTaxePolice codeTaxe = resoudreCodeTaxe(demande, vehicules);
        AutoCodeTarif codeTarif = vehicules.get(0).getCodeTarif();

        // le bonus/malus s'applique à la prime nette avant le calcul des taxes
        double coefficient = demande.getCoefficientBonusMalus() == null || demande.getCoefficientBonusMalus() <= 0
                ? 1d : demande.getCoefficientBonusMalus();
        double primeNetteAjustee = arrondir(primeNetteSaisie * coefficient);

        return new Preparation(offre, branche, parametres, codeTaxe, codeTarif, vehicules, dureeMois,
                primeNetteAjustee, coefficient, demande.getAccessoires());
    }

    /**
     * Durée du contrat choisie par le client, contrôlée par rapport au minimum de l'offre.
     * À défaut, la durée minimale de l'offre est retenue.
     */
    private Integer resoudreDuree(DemandeSouscription demande, Offre offre) {
        Integer minimum = offre.getDureeMinMois() == null ? DUREE_DEFAUT_MOIS : offre.getDureeMinMois();
        Integer duree = demande.getDureeMois() == null ? minimum : demande.getDureeMois();
        if (duree < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La durée du contrat doit être d'au moins 1 mois");
        }
        if (duree < minimum) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cette offre exige une durée d'au moins " + minimum + " mois");
        }
        return duree;
    }

    /** Durée contractuelle retenue : celle du contrat, sinon le minimum de l'offre, sinon un an. */
    private Integer dureeMoisDuContrat(Contrat contrat) {
        if (contrat.getDureeMois() != null && contrat.getDureeMois() > 0) {
            return contrat.getDureeMois();
        }
        return contrat.getOffre() == null || contrat.getOffre().getDureeMinMois() == null
                ? DUREE_DEFAUT_MOIS : contrat.getOffre().getDureeMinMois();
    }

    /**
     * Résout les garanties choisies par le client. Une liste vide ou absente donne un contrat
     * sans garantie (0..n autorisé). Chaque garantie doit exister et relever de la branche de
     * l'offre, sinon la demande est rejetée.
     */
    private Set<GarantieType> resoudreGaranties(List<String> codes, Branche branche) {
        Set<GarantieType> garanties = new LinkedHashSet<>();
        if (codes == null || codes.isEmpty()) {
            return garanties;
        }
        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            GarantieType garantie = garantieRepository.findById(code.trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Garantie inconnue : " + code));
            if (branche == null || garantie.getBranche() == null
                    || !branche.getCdBranche().equals(garantie.getBranche().getCdBranche())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "La garantie " + garantie.getCdGarantie() + " n'appartient pas à la branche de l'offre");
            }
            garanties.add(garantie);
        }
        return garanties;
    }

    private List<Vehicule> chargerVehicules(DemandeSouscription demande, Offre offre) {
        List<Long> ids = demande.getIdVehicules();
        if (ids == null || ids.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sélectionnez au moins un véhicule");
        }
        List<Vehicule> vehicules = new ArrayList<>();
        for (Long id : ids) {
            if (id == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifiant de véhicule invalide");
            }
            Vehicule vehicule = vehiculeRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable : " + id));
            vehicules.add(vehicule);
        }
        return vehicules;
    }

    /** Une offre MONOVEHICULE ne couvre qu'un véhicule, une offre FLOTTE en couvre plusieurs. */
    private void controllerVehicules(Offre offre, List<Vehicule> vehicules) {
        if (offre.getTypeOffre() == TypeOffre.MONOVEHICULE && vehicules.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'offre " + offre.getLibelleOffre() + " ne couvre qu'un seul véhicule");
        }
        if (offre.getTypeOffre() == TypeOffre.FLOTTE && vehicules.size() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "L'offre " + offre.getLibelleOffre() + " couvre une flotte : sélectionnez au moins deux véhicules");
        }
        if (offre.getNbVehiculesMin() != null && vehicules.size() < offre.getNbVehiculesMin()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cette offre exige au moins " + offre.getNbVehiculesMin() + " véhicules");
        }
    }

    /**
     * Le code taxe est soit choisi explicitement, soit déduit du code tarifaire du véhicule.
     * Tous les véhicules couverts doivent relever du même code taxe : deux véhicules de codes
     * différents ne peuvent pas être garantis par la même police.
     */
    private CodeTaxePolice resoudreCodeTaxe(DemandeSouscription demande, List<Vehicule> vehicules) {
        Vehicule premier = vehicules.get(0);
        for (Vehicule v : vehicules) {
            String code = codeTaxeDu(v);
            String attendu = codeTaxeDu(premier);
            if (code != null && attendu != null && !code.equalsIgnoreCase(attendu)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Les véhicules couverts doivent relever du même code taxe");
            }
        }

        if (demande.getCodeTaxe() != null && !demande.getCodeTaxe().isBlank()) {
            return codeTaxeRepository.findByCodeTaxeIgnoreCase(demande.getCodeTaxe().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code taxe inconnu : " + demande.getCodeTaxe()));
        }
        if (premier.getCodeTarif() != null && premier.getCodeTarif().getCodeTaxePolice() != null) {
            return premier.getCodeTarif().getCodeTaxePolice();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Sélectionnez un code taxe : aucun véhicule n'en porte");
    }

    private String codeTaxeDu(Vehicule v) {
        return v.getCodeTarif() == null || v.getCodeTarif().getCodeTaxePolice() == null
                ? null : v.getCodeTarif().getCodeTaxePolice().getCodeTaxe();
    }

    /**
     * Numéro de police sur 6 caractères : trois lettres puis trois chiffres (par exemple KJD482),
     * tirés au hasard. L'unicité est garantie par la base : on retire un numéro tant qu'il est
     * déjà attribué. Un contrat refusé ne consomme donc pas de numéro, et deux tirages successifs
     * ne peuvent pas se télescoper.
     */
    private String nextNumPolice() {
        String numPolice;
        do {
            numPolice = policeAleatoire();
        } while (contratRepository.existsByNumPolice(numPolice));
        return numPolice;
    }

    /** Trois lettres majuscules puis trois chiffres, soit un code de police de 6 caractères. */
    private static String policeAleatoire() {
        ThreadLocalRandom aleatoire = ThreadLocalRandom.current();
        StringBuilder code = new StringBuilder(6);
        for (int i = 0; i < 3; i++) {
            code.append(LETTRES_POLICE.charAt(aleatoire.nextInt(LETTRES_POLICE.length())));
        }
        for (int i = 0; i < 3; i++) {
            code.append(aleatoire.nextInt(10));
        }
        return code.toString();
    }

    private double arrondir(double valeur) {
        return java.math.BigDecimal.valueOf(valeur)
                .setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    /** Regroupement des éléments dont le calcul a besoin. */
    private record Preparation(Offre offre,
                               Branche branche,
                               BrancheParametresCalcul parametres,
                               CodeTaxePolice codeTaxe,
                               AutoCodeTarif codeTarif,
                               List<Vehicule> vehicules,
                               Integer dureeMois,
                               double primeNetteAjustee,
                               double coefficient,
                               Double accessoires) {
    }
}