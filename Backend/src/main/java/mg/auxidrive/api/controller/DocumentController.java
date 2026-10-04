package mg.auxidrive.api.controller;

import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.service.DocumentService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentJustificatif> lister(@RequestParam(required = false) Long idAssure,
                                             @RequestParam(required = false) Long idInscription) {
        return documentService.listerDocument(idAssure, idInscription);
    }

    @GetMapping("/{id}")
    public DocumentJustificatif trouver(@PathVariable Long idDocument) {
        return documentService.trouverDocument(idDocument);
    }

    @GetMapping("/{id}/fichier")
    public ResponseEntity<Resource> fichier(@PathVariable Long idDocument) {
        DocumentService.FichierDocument f = documentService.fichier(idDocument);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.typeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(f.ressource());
    }

    @PutMapping("/{id}/valider")
    public DocumentJustificatif valider(@PathVariable Long idDocument, Authentication authentication) {
        return documentService.validerDocument(idDocument, authentication.getName());
    }

    @PutMapping("/{id}/rejeter")
    public DocumentJustificatif rejeter(@PathVariable Long idDocument, @RequestParam String motif,
                                        Authentication authentication) {
        return documentService.rejeterDocument(idDocument, motif, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long idDocument) {
        documentService.supprimerDocument(idDocument);
        return ResponseEntity.noContent().build();
    }
}
