package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.Sinistre;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.StatutSinistre;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.repository.SinistreRepository;
import mg.auxidrive.api.repository.VehiculeRepository;

/**
 * Sinistres : déclaration par l'assuré, traitement par l'agent.
 *
 * Un sinistre ne peut porter que sur un contrat validé, et sur un véhicule réellement couvert
 * par ce contrat. Un véhicule couvert par un autre contrat n'est pas sinistrable sur celui-ci,
 * même s'il appartient au même assuré.
 */
@Service
public class SinistreService {

    private final SinistreRepository sinistreRepository;
    private final ContratRepository contratRepository;
    private final ContratAutoRepository contratAutoRepository;
    private final VehiculeRepository vehiculeRepository;

    public SinistreService(SinistreRepository sinistreRepository,
                           ContratRepository contratRepository,
                           ContratAutoRepository contratAutoRepository,
                           VehiculeRepository vehiculeRepository) {
        this.sinistreRepository = sinistreRepository;
        this.contratRepository = contratRepository;
        this.contratAutoRepository = contratAutoRepository;
        this.vehiculeRepository = vehiculeRepository;
    }

    @Transactional(readOnly = true)
    public List<Sinistre> lister() {
        return sinistreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Sinistre> listerParContrat(Long idContrat) {
        return sinistreRepository.findByContrat_IdContratOrderByIdSinistre(idContrat);
    }

    /** Sinistres de tous les contrats d'un assuré : c'est la vue du portail client. */
    @Transactional(readOnly = true)
    public List<Sinistre> listerParAssure(Long idAssure) {
        return sinistreRepository.findByContrat_Assure_IdAssureOrderByIdSinistre(idAssure);
    }

    @Transactional(readOnly = true)
    public Sinistre trouver(Long id) {
        return sinistreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sinistre introuvable"));
    }

    /** Sinistre dont le contrat appartient bien à l'assuré : contrôle du portail client. */
    @Transactional(readOnly = true)
    public Sinistre trouverPourAssure(Long id, Long idAssure) {
        Sinistre sinistre = trouver(id);
        Long idAssureContrat = sinistre.getContrat() == null || sinistre.getContrat().getAssure() == null
                ? null : sinistre.getContrat().getAssure().getIdAssure();
        if (idAssureContrat == null || !idAssureContrat.equals(idAssure)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sinistre introuvable");
        }
        return sinistre;
    }

    /** Déclaration d'un sinistre par l'assuré sur un contrat validé. */
    @Transactional
    public Sinistre declarer(Long idContrat, Long idVehicule, LocalDate dateSinistre,
                             String lieu, String circonstances, Long idAssure) {
        Contrat contrat = contratRepository.findById(idContrat)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable"));

        if (contrat.getStatutContrat() != StatutContrat.VALIDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un sinistre ne peut être déclaré que sur un contrat validé");
        }
        if (contrat.getAssure() == null || !contrat.getAssure().getIdAssure().equals(idAssure)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contrat introuvable");
        }
        if (dateSinistre == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date du sinistre est obligatoire");
        }
        if (dateSinistre.isAfter(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La date du sinistre ne peut pas être dans le futur");
        }

        Sinistre sinistre = new Sinistre();
        sinistre.setContrat(contrat);
        sinistre.setDateSinistre(dateSinistre);
        sinistre.setDateDeclaration(LocalDate.now());
        sinistre.setLieuSinistre(lieu);
        sinistre.setCirconstances(circonstances);
        sinistre.setStatutSinistre(StatutSinistre.DECLARE);
        if (idVehicule != null) {
            sinistre.setVehicule(vehiculeCouvert(contrat, idVehicule));
        }
        return sinistreRepository.save(sinistre);
    }

    /** Rattachement du rapport policier par l'agent : le sinistre passe en instruction. */
    @Transactional
    public Sinistre attacherRapport(Long idSinistre, String numRapport, LocalDate dateRapport,
                                    String rapport, Boolean responsabilite, Utilisateur agent) {
        Sinistre sinistre = trouver(idSinistre);
        if (sinistre.getStatutSinistre() != StatutSinistre.DECLARE
                && sinistre.getStatutSinistre() != StatutSinistre.RAPPORT_EN_COURS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce sinistre ne peut plus recevoir de rapport");
        }
        if (numRapport == null || numRapport.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro de rapport police est obligatoire");
        }
        sinistre.setNumRapportPolice(numRapport.trim());
        sinistre.setDateRapportPolice(dateRapport);
        sinistre.setRapportPolicier(rapport);
        sinistre.setResponsabiliteAssure(responsabilite);
        sinistre.setStatutSinistre(StatutSinistre.RAPPORT_EN_COURS);
        if (agent != null) {
            sinistre.setUtilisateur(agent);
        }
        return sinistreRepository.save(sinistre);
    }

    /** Indemnisation : le sinistre est clôturé et le montant versé est enregistré. */
    @Transactional
    public Sinistre indemniser(Long idSinistre, Double montant) {
        Sinistre sinistre = trouver(idSinistre);
        if (sinistre.getStatutSinistre() == StatutSinistre.INDEMNISE
                || sinistre.getStatutSinistre() == StatutSinistre.REFUSE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce sinistre est déjà clôturé");
        }
        if (montant == null || montant < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant d'indemnisation est invalide");
        }
        sinistre.setMontantIndemnisation(montant);
        if (sinistre.getMontantDommagesEstime() == null) {
            sinistre.setMontantDommagesEstime(montant);
        }
        sinistre.setStatutSinistre(StatutSinistre.INDEMNISE);
        sinistre.setDateCloture(LocalDate.now());
        return sinistreRepository.save(sinistre);
    }

    /** Refus d'indemniser : le sinistre est clôturé sans versement. */
    @Transactional
    public Sinistre refuser(Long idSinistre) {
        Sinistre sinistre = trouver(idSinistre);
        if (sinistre.getStatutSinistre() == StatutSinistre.INDEMNISE
                || sinistre.getStatutSinistre() == StatutSinistre.REFUSE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce sinistre est déjà clôturé");
        }
        sinistre.setStatutSinistre(StatutSinistre.REFUSE);
        sinistre.setDateCloture(LocalDate.now());
        return sinistreRepository.save(sinistre);
    }

    /** Estimation des dommages par l'agent pendant l'instruction. */
    @Transactional
    public Sinistre estimerDommages(Long idSinistre, Double montant) {
        Sinistre sinistre = trouver(idSinistre);
        if (montant == null || montant < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant estimé est invalide");
        }
        sinistre.setMontantDommagesEstime(montant);
        return sinistreRepository.save(sinistre);
    }

    /** Vérifie que le véhicule est bien couvert par le contrat, pas seulement du même assuré. */
    private Vehicule vehiculeCouvert(Contrat contrat, Long idVehicule) {
        boolean couvert = contratAutoRepository.findByContrat_IdContrat(contrat.getIdContrat()).stream()
                .anyMatch(lien -> lien.getVehicule() != null && idVehicule.equals(lien.getVehicule().getIdVehicule()));
        if (!couvert) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Ce véhicule n'est pas couvert par ce contrat");
        }
        return vehiculeRepository.findById(idVehicule)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Véhicule introuvable"));
    }
}