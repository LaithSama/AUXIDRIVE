package mg.auxidrive.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.VehiculeRepository;

/**
 * Véhicules des assurés. Un véhicule appartient à un seul assuré et porte un code tarifaire :
 * c'est ce code qui indique si le TACAVA s'applique (avec_tacava) lors du calcul de la prime.
 * La suppression est refusée si le véhicule est rattaché à un contrat.
 */
@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final AssureRepository assureRepository;
    private final AutoCodeTarifRepository codeTarifRepository;

    public VehiculeService(VehiculeRepository vehiculeRepository,
                           AssureRepository assureRepository,
                           AutoCodeTarifRepository codeTarifRepository) {
        this.vehiculeRepository = vehiculeRepository;
        this.assureRepository = assureRepository;
        this.codeTarifRepository = codeTarifRepository;
    }

    /** Tous les véhicules, pour l'agent et l'administrateur. */
    @Transactional(readOnly = true)
    public List<Vehicule> lister() {
        return vehiculeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Vehicule> listerParAssure(Long idAssure) {
        return vehiculeRepository.findByAssure_IdAssureOrderByIdVehicule(idAssure);
    }

    /** Véhicule avec son code tarifaire résolu, pour les formulaires. */
    @Transactional(readOnly = true)
    public Vehicule trouver(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));
    }

    /**
     * Véhicule appartenant bien à l'assuré : c'est le contrôle d'appartenance du portail client.
     * Un véhicule qui n'appartient pas à l'assuré est renvoyé comme introuvable, pas comme interdit,
     * pour ne pas révéler l'existence d'un véhicule d'un autre client.
     */
    @Transactional(readOnly = true)
    public Vehicule trouverPourAssure(Long id, Long idAssure) {
        Vehicule vehicule = trouver(id);
        if (vehicule.getAssure() == null || !idAssure.equals(vehicule.getAssure().getIdAssure())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable");
        }
        return vehicule;
    }

    @Transactional
    public Vehicule creer(Vehicule donnees, Long idAssure) {
        Assure assure = assureRepository.findById(idAssure)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable"));

        String immatriculation = exiger(donnees.getNumImmatriculation(), "Le numéro d'immatriculation est obligatoire");
        String chassis = exiger(donnees.getNumChassis(), "Le numéro de châssis est obligatoire");

        if (vehiculeRepository.existsByNumImmatriculationIgnoreCase(immatriculation)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro d'immatriculation est déjà enregistré");
        }
        if (vehiculeRepository.existsByNumChassisIgnoreCase(chassis)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro de châssis est déjà enregistré");
        }

        Vehicule vehicule = new Vehicule();
        vehicule.setAssure(assure);
        appliquer(vehicule, donnees, idAssure, false);
        return vehiculeRepository.save(vehicule);
    }

    @Transactional
    public Vehicule modifier(Long id, Vehicule donnees) {
        Vehicule vehicule = trouver(id);
        appliquer(vehicule, donnees, vehicule.getAssure().getIdAssure(), true);
        return vehicule;
    }

    @Transactional
    public Vehicule supprimer(Long id) {
        Vehicule vehicule = trouver(id);
        vehiculeRepository.delete(vehicule);
        return vehicule;
    }

    /**
     * Copie les champs modifiables et contrôle les unicités.
     * L'immatriculation et le châssis sont uniques dans toute la base : à la création on refuse
     * un doublon, en modification on ignore le véhicule lui-même.
     */
    private void appliquer(Vehicule cible, Vehicule donnees, Long idAssure, boolean modification) {
        if (donnees.getNumImmatriculation() != null && !donnees.getNumImmatriculation().isBlank()) {
            String immatriculation = donnees.getNumImmatriculation().trim();
            Vehicule autre = vehiculeRepository.findByNumImmatriculationIgnoreCase(immatriculation).orElse(null);
            if (autre != null && (cible.getIdVehicule() == null || !cible.getIdVehicule().equals(autre.getIdVehicule()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro d'immatriculation est déjà enregistré");
            }
            cible.setNumImmatriculation(immatriculation);
        }
        if (donnees.getNumChassis() != null && !donnees.getNumChassis().isBlank()) {
            String chassis = donnees.getNumChassis().trim();
            Vehicule autre = vehiculeRepository.findByNumChassisIgnoreCase(chassis).orElse(null);
            if (autre != null && (cible.getIdVehicule() == null || !cible.getIdVehicule().equals(autre.getIdVehicule()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro de châssis est déjà enregistré");
            }
            cible.setNumChassis(chassis);
        }

        if (donnees.getMarque() != null) cible.setMarque(donnees.getMarque().trim());
        if (donnees.getModele() != null) cible.setModele(donnees.getModele().trim());
        if (donnees.getNumMoteur() != null) cible.setNumMoteur(vide(donnees.getNumMoteur()) ? null : donnees.getNumMoteur().trim());
        if (donnees.getTypeVehicule() != null) cible.setTypeVehicule(donnees.getTypeVehicule().trim());
        if (donnees.getUsageVehicule() != null) cible.setUsageVehicule(donnees.getUsageVehicule().trim());
        if (donnees.getEnergie() != null) cible.setEnergie(donnees.getEnergie().trim());
        if (donnees.getPuissanceFiscale() != null) cible.setPuissanceFiscale(donnees.getPuissanceFiscale());
        if (donnees.getNombrePlaces() != null) cible.setNombrePlaces(donnees.getNombrePlaces());
        if (donnees.getPoidsAVide() != null) cible.setPoidsAVide(donnees.getPoidsAVide());
        if (donnees.getPtac() != null) cible.setPtac(donnees.getPtac());
        if (donnees.getChargeUtile() != null) cible.setChargeUtile(donnees.getChargeUtile());
        if (donnees.getPeriodiciteVisiteMois() != null) cible.setPeriodiciteVisiteMois(donnees.getPeriodiciteVisiteMois());
        if (donnees.getDateDerniereVisite() != null) cible.setDateDerniereVisite(donnees.getDateDerniereVisite());
        if (donnees.getDatePremierMiseCirculation() != null) cible.setDatePremierMiseCirculation(donnees.getDatePremierMiseCirculation());

        // le code tarifaire pilote l'application du TACAVA : il doit exister s'il est fourni
        if (donnees.getCodeTarif() != null && donnees.getCodeTarif().getIdCodeAuto() != null) {
            AutoCodeTarif codeTarif = codeTarifRepository.findById(donnees.getCodeTarif().getIdCodeAuto())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code tarifaire inconnu"));
            cible.setCodeTarif(codeTarif);
        } else if (!modification) {
            cible.setCodeTarif(null);
        }

        if (!modification) {
            if (vide(cible.getMarque())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La marque est obligatoire");
            if (vide(cible.getModele())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le modèle est obligatoire");
            if (vide(cible.getEnergie())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'énergie est obligatoire");
            if (cible.getNombrePlaces() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nombre de places est obligatoire");
            if (cible.getPuissanceFiscale() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La puissance fiscale est obligatoire");
        }
    }

    private String exiger(String valeur, String message) {
        if (vide(valeur)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        return valeur.trim();
    }

    private boolean vide(String s) {
        return s == null || s.isBlank();
    }
}