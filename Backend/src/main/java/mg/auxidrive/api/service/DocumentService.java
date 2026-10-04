package mg.auxidrive.api.service;

import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

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
    public List<DocumentJustificatif> listerDocument(Long idAssure, Long idInscription) {
        if (idInscription != null) {
            return documentRepository.findByInscriptionId(idInscription);
        }
        if (idAssure != null) {
            return documentRepository.findByAssureId(idAssure);
        }
        return documentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DocumentJustificatif trouverDocument(Long idDocument) {
        return documentRepository.findById(idDocument)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    @Transactional(readOnly = true)
    public FichierDocument fichier(Long idDocument) {
        DocumentJustificatif d = trouverDocument(idDocument);
        return new FichierDocument(fichierService.chargerFichier(d.getCheminFichier()),
                fichierService.typeMime(d.getFormatFichier()));
    }

    @Transactional
    public DocumentJustificatif validerDocument(Long idDocument, String emailAgent) {
        DocumentJustificatif d = trouverDocument(idDocument);
        d.setStatutDocuments(StatutDocument.VALIDE);
        d.setMotifRejet(null);
        d.setDateValidation(LocalDateTime.now());
        d.setUtilisateur(trouverAgent(emailAgent));
        return d;
    }

    @Transactional
    public DocumentJustificatif rejeterDocument(Long idDocument, String motif, String emailAgent) {
        if (motif == null || motif.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif du rejet est obligatoire");
        }
        DocumentJustificatif document = trouverDocument(idDocument);
        document.setStatutDocuments(StatutDocument.REJETE);
        document.setMotifRejet(motif.trim());
        document.setDateValidation(LocalDateTime.now());
        document.setUtilisateur(trouverAgent(emailAgent));
        return document;
    }

    @Transactional
    public void supprimerDocument(Long idDocument) {
        DocumentJustificatif document = trouverDocument(idDocument);
        documentRepository.delete(document);
        fichierService.supprimerFichier(document.getCheminFichier());
    }

    private Utilisateur trouverAgent(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agent introuvable"));
    }
}
