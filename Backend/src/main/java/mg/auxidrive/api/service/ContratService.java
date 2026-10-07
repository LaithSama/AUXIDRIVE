package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.TypeOffre;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.BrancheParametresCalculRepository;
import mg.auxidrive.api.repository.CodeTaxePoliceRepository;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
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

    private final ContratRepository contratRepository;
    private final ContratAutoRepository contratAutoRepository;
    private final OffreRepository offreRepository;
    private final VehiculeRepository vehiculeRepository;
    private final AssureRepository assureRepository;
    private final BrancheParametresCalculRepository parametresRepository;
    private final CodeTaxePoliceRepository codeTaxeRepository;
    private final AutoCodeTarifRepository codeTarifRepository;
    private final FactureService factureService;

    public ContratService(ContratRepository contratRepository,
                          ContratAutoRepository contratAutoRepository,
                          OffreRepository offreRepository,
                          VehiculeRepository vehiculeRepository,
                          AssureRepository assureRepository,
                          BrancheParametresCalculRepository parametresRepository,
                          CodeTaxePoliceRepository codeTaxeRepository,
                          AutoCodeTarifRepository codeTarifRepository,
                          FactureService factureService) {
        this.contratRepository = contratRepository;
        this.contratAutoRepository = contratAutoRepository;
        this.offreRepository = offreRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.assureRepository = assureRepository;
        this.parametresRepository = parametresRepository;
        this.codeTaxeRepository = codeTaxeRepository;
        this.codeTarifRepository = codeTarifRepository;
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
        contrat.setDateEffet(demande.getDateEffet() == null ? LocalDate.now() : demande.getDateEffet());
        contrat.setStatutContrat(StatutContrat.EN_ATTENTE);
        contrat.setCoefficientBonusMalus(p.coefficient());
        contrat.setPrimeNetteCourante(p.primeNetteAjustee());
        contrat.setPrimeTtcCourante(detail.ttc());
        contrat.setTauxCommission(detail.tauxCommission());
        contrat.setMontantCommission(detail.commission());
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
     * Valide une demande : le contrat reçoit son numéro de police et sa date d'expiration,
     * et sa facture de souscription est émise.
     * Le montant est recalculé à partir du contrat pour que la facture corresponde exactement
     * à ce qui a été souscrit, bonus/malus compris.
     */
    @Transactional
    public Contrat valider(Long idContrat) {
        Contrat contrat = trouver(idContrat);
        if (contrat.getStatutContrat() != StatutContrat.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seule une demande en attente peut être validée");
        }

        Detail detail = recalculer(contrat);

        contrat.setNumPolice(nextNumPolice());
        contrat.setStatutContrat(StatutContrat.VALIDE);
        contrat.setPrimeNetteCourante(detail.base() - detail.accessoires());
        contrat.setPrimeTtcCourante(detail.ttc());
        contrat.setTauxCommission(detail.tauxCommission());
        contrat.setMontantCommission(detail.commission());
        // la durée de l'offre fixe la durée du contrat si l'offre ne précise rien
        Integer dureeMois = contrat.getOffre() == null || contrat.getOffre().getDureeMinMois() == null
                ? 12 : contrat.getOffre().getDureeMinMois();
        contrat.setDateExpiration(contrat.getDateEffet() == null
                ? LocalDate.now().plusMonths(dureeMois)
                : contrat.getDateEffet().plusMonths(dureeMois));
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

    // ---- interne ----

    /**
     * Recalcule la prime d'un contrat déjà enregistré, en résolvant à nouveau la branche,
     * le code taxe et le code tarifaire à partir des véhicules rattachés.
     * Utilisé à la validation pour que la facture émise corresponde au contrat souscrit.
     */
    private Detail recalculer(Contrat contrat) {
        if (contrat.getBranche() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce contrat n'est rattaché à aucune branche");
        }
        BrancheParametresCalcul parametres = parametresRepository.findByBranche_CdBranche(contrat.getBranche().getCdBranche())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Aucun paramètre de calcul pour la branche " + contrat.getBranche().getCdBranche()));

        List<ContratAuto> liens = contratAutoRepository.findByContrat_IdContrat(contrat.getIdContrat());
        if (liens.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce contrat n'a aucun véhicule rattaché");
        }
        Vehicule premier = liens.get(0).getVehicule();
        CodeTaxePolice codeTaxe = premier.getCodeTarif() == null || premier.getCodeTarif().getCodeTaxePolice() == null
                ? null : premier.getCodeTarif().getCodeTaxePolice();
        if (codeTaxe == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le véhicule couvert ne porte aucun code taxe");
        }

        try {
            return PrimeCalculator.calculer(contrat.getPrimeNetteCourante() == null ? 0d : contrat.getPrimeNetteCourante(),
                    parametres, codeTaxe, premier.getCodeTarif(), contrat.getTauxCommission());
        } catch (CalculPrimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /** Convertit une erreur de calcul en erreur HTTP 400 pour que le frontend affiche le message. */
    private Detail calculer(Preparation p, Double tauxCommission) {
        try {
            return PrimeCalculator.calculer(p.primeNetteAjustee(), p.parametres(), p.codeTaxe(), p.codeTarif(), tauxCommission);
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
        if (demande.getPrimeNette() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Veuillez introduire le montant de la prime nette");
        }

        Offre offre = offreRepository.findById(demande.getIdOffre())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));

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
        double primeNetteAjustee = arrondir(demande.getPrimeNette() * coefficient);

        return new Preparation(offre, branche, parametres, codeTaxe, codeTarif, vehicules, primeNetteAjustee, coefficient);
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
     * Numéro de police sur 10 caractères : deux chiffres d'année puis huit chiffres de séquence.
     * La séquence repart du plus grand numéro existant, donc un numéro n'est pas réutilisé.
     * On ne lit que les huit chiffres de fin : les deux premiers sont l'année, les re-préfixer
     * produirait un numéro de 12 caractères, trop long pour la colonne.
     */
    private String nextNumPolice() {
        long maxSequence = contratRepository.findAll().stream()
                .map(Contrat::getNumPolice)
                .filter(n -> n != null && n.matches("\\d+"))
                .mapToLong(ContratService::sequence)
                .max()
                .orElse(0L);
        String annee = String.valueOf(LocalDate.now().getYear() % 100);
        return annee + String.format("%08d", maxSequence + 1);
    }

    /** Les huit derniers chiffres d'un numéro de police, c'est-à-dire sa séquence. */
    private static long sequence(String numPolice) {
        return Long.parseLong(numPolice.length() > 8 ? numPolice.substring(numPolice.length() - 8) : numPolice);
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
                               double primeNetteAjustee,
                               double coefficient) {
    }
}