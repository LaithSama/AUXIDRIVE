package mg.auxidrive.api.controller;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import mg.auxidrive.api.service.DocumentClientService;

/**
 * Pièces justificatives du client connecté.
 *
 * Routes dédiées sous /api/profil : /api/documents reste réservé aux agents et administrateurs.
 * L'e-mail du token identifie toujours l'assuré, jamais un identifiant envoyé par le client.
 */
@RestController
@RequestMapping("/api/profil/documents")
@PreAuthorize("hasRole('CLIENT')")
public class DocumentProfilController {

    private final DocumentClientService documentClientService;

    public DocumentProfilController(DocumentClientService documentClientService) {
        this.documentClientService = documentClientService;
    }

    @GetMapping
    public List<DocumentClientService.DocumentProfil> lister(Authentication authentication) {
        return documentClientService.lister(authentication.getName());
    }

    @GetMapping("/{id}")
    public DocumentClientService.DocumentProfil trouver(@PathVariable Long id, Authentication authentication) {
        return documentClientService.trouver(authentication.getName(), id);
    }

    @GetMapping("/{id}/fichier")
    public ResponseEntity<Resource> fichier(@PathVariable Long id, Authentication authentication) {
        DocumentClientService.FichierDocument fichier = documentClientService.fichier(authentication.getName(), id);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.parseMediaType(fichier.typeMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(fichier.ressource());
    }

    /** Ajout d'une pièce manquante : type = CIN, CARTE_GRISE, CARTE_ROSE ou MODELE_N1. */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentClientService.DocumentProfil ajouter(Authentication authentication,
                                                        @RequestParam String type,
                                                        @RequestPart("fichier") MultipartFile fichier) {
        return documentClientService.ajouter(authentication.getName(), type, fichier);
    }

    /** Remplacement : la pièce repasse en attente et rejoint la file de vérification des agents. */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DocumentClientService.DocumentProfil remplacer(Authentication authentication,
                                                          @PathVariable Long id,
                                                          @RequestPart("fichier") MultipartFile fichier) {
        return documentClientService.remplacer(authentication.getName(), id, fichier);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication authentication) {
        documentClientService.supprimer(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
