package mg.auxidrive.api.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.service.DocumentService;

/** Vérification des documents importés par les clients (agents et administrateurs). */
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
        return documentService.lister(idAssure, idInscription);
    }

    @GetMapping("/{id}")
    public DocumentJustificatif trouver(@PathVariable Long id) {
        return documentService.trouver(id);
    }

    @GetMapping("/{id}/fichier")
    public ResponseEntity<Resource> fichier(@PathVariable Long id) {
        DocumentService.FichierDocument f = documentService.fichier(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(f.typeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(f.ressource());
    }

    @PutMapping("/{id}/valider")
    public DocumentJustificatif valider(@PathVariable Long id, Authentication authentication) {
        return documentService.valider(id, authentication.getName());
    }

    @PutMapping("/{id}/rejeter")
    public DocumentJustificatif rejeter(@PathVariable Long id, @RequestParam String motif,
                                        Authentication authentication) {
        return documentService.rejeter(id, motif, authentication.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        documentService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
