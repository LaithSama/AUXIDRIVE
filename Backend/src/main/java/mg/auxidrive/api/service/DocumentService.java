package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Documents justificatifs. Ils sont créés lors de l'inscription (import par le client),
 * puis vérifiés un par un par l'agent : EN_ATTENTE -> VALIDE ou REJETE.
 */
@Service
public class DocumentService {

    public record FichierDocument(Resource ressource, String typeMime) {
    }

    private final DocumentJustificatifRepository documentRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FichierService fichierService;

    public DocumentService(DocumentJustificatifRepository documentRepository,
                           UtilisateurRepository utilisateurRepository,
                           FichierService fichierService) {
        this.documentRepository = documentRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.fichierService = fichierService;
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

    @Transactional
    public DocumentJustificatif valider(Long id, String emailAgent) {
        DocumentJustificatif d = trouver(id);
        d.setStatutDocuments(StatutDocument.VALIDE);
        d.setMotifRejet(null);
        d.setDateValidation(LocalDateTime.now());
        d.setUtilisateur(trouverAgent(emailAgent));
        return d;
    }

    @Transactional
    public DocumentJustificatif rejeter(Long id, String motif, String emailAgent) {
        if (motif == null || motif.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif du rejet est obligatoire");
        }
        DocumentJustificatif d = trouver(id);
        d.setStatutDocuments(StatutDocument.REJETE);
        d.setMotifRejet(motif.trim());
        d.setDateValidation(LocalDateTime.now());
        d.setUtilisateur(trouverAgent(emailAgent));
        return d;
    }

    @Transactional
    public void supprimer(Long id) {
        DocumentJustificatif d = trouver(id);
        documentRepository.delete(d);
        fichierService.supprimer(d.getCheminFichier());
    }

    private Utilisateur trouverAgent(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agent introuvable"));
    }
}
