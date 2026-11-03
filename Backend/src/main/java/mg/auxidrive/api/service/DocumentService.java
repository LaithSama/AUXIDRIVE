package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Documents justificatifs. Ils sont créés lors de l'inscription (import par le client) ou
 * ajoutés depuis le profil, puis vérifiés un par un par l'agent : EN_ATTENTE -> VALIDE ou REJETE.
 */
@Service
public class DocumentService {

    public record FichierDocument(Resource ressource, String typeMime) {
    }

    /**
     * Vue agent de la file « Documents à vérifier ». L'assuré et le dossier sont chargés
     * dans la requête : aucune sérialisation paresseuse hors transaction.
     */
    public record DocumentAVerifier(Long idDocument, String typeDocument, String formatFichier,
                                    Integer tailleKo, LocalDateTime dateTeleversement,
                                    String statutDocuments, String motifRejet,
                                    Long idAssure, String nomAssure, String prenomAssure,
                                    String numerosAssure, String mailAssure,
                                    Long idInscription, String statutInscription, String origine) {
    }

    private final DocumentJustificatifRepository documentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FichierService fichierService;
    private final InscriptionService inscriptionService;

    public DocumentService(DocumentJustificatifRepository documentRepository,
                           UtilisateurRepository utilisateurRepository,
                           FichierService fichierService, InscriptionService inscriptionService) {
        this.documentRepository = documentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.fichierService = fichierService;
        this.inscriptionService = inscriptionService;
    }

    /** Filtre par assuré ou par inscription ; sans filtre, tous les documents. */
    @Transactional(readOnly = true)
    public List<DocumentJustificatif> lister(Long idAssure, Long idInscription) {
        if (idInscription != null) {
            return documentRepository.findByInscription_IdInscription(idInscription);
        }
        if (idAssure != null) {
            return documentRepository.findByAssure_IdAssure(idAssure);
        }
        return documentRepository.findAll();
    }

    /** File de vérification des pièces ajoutées ou remplacées depuis le profil (hors dossiers en cours). */
    @Transactional(readOnly = true)
    public List<DocumentAVerifier> aVerifier() {
        return documentRepository
                .findAVerifier(StatutDocument.EN_ATTENTE,
                        List.of(StatutInscription.EN_ATTENTE, StatutInscription.CORRECTION_DEMANDEE))
                .stream()
                .map(DocumentService::versAVerifier)
                .toList();
    }

    private static DocumentAVerifier versAVerifier(DocumentJustificatif d) {
        Assure a = d.getAssure();
        Inscription i = d.getInscription();
        return new DocumentAVerifier(
                d.getIdDocument(),
                d.getTypeDocument(),
                d.getFormatFichier(),
                d.getTailleKo(),
                d.getDateTeleversement(),
                d.getStatutDocuments() == null ? null : d.getStatutDocuments().name(),
                d.getMotifRejet(),
                a == null ? null : a.getIdAssure(),
                a == null ? null : a.getNomAssure(),
                a == null ? null : a.getPrenomAssure(),
                a == null ? null : a.getNumerosAssure(),
                a == null ? null : a.getMail(),
                i == null ? null : i.getIdInscription(),
                i == null || i.getStatutInscription() == null ? null : i.getStatutInscription().name(),
                "PROFIL");
    }

    @Transactional(readOnly = true)
    public DocumentJustificatif trouver(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    @Transactional(readOnly = true)
    public FichierDocument fichier(Long id) {
        DocumentJustificatif d = trouver(id);
        return new FichierDocument(fichierService.charger(d.getCheminFichier()),
                fichierService.typeMime(d.getFormatFichier()));
    }

    /**
     * Valide une pièce. Les pièces de profil (sans dossier, ou rattachées à un dossier déjà
     * traité) sont directement examinables. Seule une correction d'inscription en attente
     * impose de passer par le parcours e-mail.
     */
    @Transactional
    public DocumentJustificatif valider(Long id, String emailAgent) {
        DocumentJustificatif d = trouver(id);
        if (d.getStatutDocuments() != StatutDocument.EN_ATTENTE || (d.getInscription() != null
                && d.getInscription().getStatutInscription() == StatutInscription.CORRECTION_DEMANDEE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pièce non examinable");
        }
        d.setStatutDocuments(StatutDocument.VALIDE);
        d.setMotifRejet(null);
        d.setDateValidation(LocalDateTime.now());
        d.setUtilisateur(trouverAgent(emailAgent));
        return d;
    }

    /**
     * Rejette une pièce. Une pièce de profil est simplement marquée REJETE : aucun jeton de
     * correction d'inscription n'est créé. Une pièce d'un dossier encore ouvert déclenche la
     * demande de correction (parcours e-mail existant).
     */
    @Transactional
    public DocumentJustificatif rejeter(Long id, String motif, String emailAgent) {
        if (motif == null || motif.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif du rejet est obligatoire");
        }
        DocumentJustificatif d = trouver(id);
        if (d.getStatutDocuments() != StatutDocument.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pièce déjà examinée");
        }
        Inscription inscription = d.getInscription();
        if (inscription != null && (inscription.getStatutInscription() == StatutInscription.EN_ATTENTE
                || inscription.getStatutInscription() == StatutInscription.CORRECTION_DEMANDEE)) {
            inscriptionService.demanderCorrection(d, motif.trim());
        }
        d.setStatutDocuments(StatutDocument.REJETE);
        d.setMotifRejet(motif.trim());
        d.setDateValidation(LocalDateTime.now());
        d.setUtilisateur(trouverAgent(emailAgent));
        return d;
    }

    @Transactional
    public void supprimer(Long id) {
        DocumentJustificatif d = trouver(id);
        if (d.getInscription() != null
                && (d.getInscription().getStatutInscription() == StatutInscription.EN_ATTENTE
                || d.getInscription().getStatutInscription() == StatutInscription.CORRECTION_DEMANDEE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une pièce d'un dossier en cours ne peut pas être supprimée");
        }
        documentRepository.delete(d);
        fichierService.supprimer(d.getCheminFichier());
    }

    private Utilisateur trouverAgent(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agent introuvable"));
    }
}
