package mg.auxidrive.api.controller;

import java.io.File;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Facture;
import mg.auxidrive.api.service.CompteClientService;
import mg.auxidrive.api.service.FactureService;

/**
 * Consultation des factures et téléchargement du PDF.
 *
 * Les factures portent des montants : un client ne peut consulter que les siennes.
 * L'agent et l'administrateur voient toutes les factures.
 */
@RestController
@RequestMapping("/api/factures")
public class FactureController {

    private static final String ROLES_STAFF = "hasAnyRole('AGENT', 'ADMIN')";

    private final FactureService factureService;
    private final CompteClientService compteClientService;

    public FactureController(FactureService factureService, CompteClientService compteClientService) {
        this.factureService = factureService;
        this.compteClientService = compteClientService;
    }

    @GetMapping
    @PreAuthorize(ROLES_STAFF)
    public List<Facture> lister() {
        return factureService.lister();
    }

    @GetMapping("/contrat/{idContrat}")
    @PreAuthorize(ROLES_STAFF)
    public List<Facture> listerParContrat(@PathVariable Long idContrat) {
        return factureService.listerParContrat(idContrat);
    }

    @GetMapping("/compte")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Facture> mesFactures(Authentication authentication) {
        return factureService.listerParAssure(idAssure(authentication));
    }

    @GetMapping("/{id}")
    public Facture trouver(@PathVariable Long id, Authentication authentication) {
        if (estClient(authentication)) {
            return factureService.trouverPourAssure(id, idAssure(authentication));
        }
        return factureService.trouver(id);
    }

    /** Téléchargement du PDF : le contrôle d'appartenance précède la lecture du fichier. */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> pdf(@PathVariable Long id, Authentication authentication) {
        Facture facture = estClient(authentication)
                ? factureService.trouverPourAssure(id, idAssure(authentication))
                : factureService.trouver(id);

        File fichier = factureService.fichierPdf(facture.getIdFacture());
        String nom = fichier.getName();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nom + "\"")
                .contentLength(fichier.length())
                .body(new FileSystemResource(fichier));
    }

    private Long idAssure(Authentication authentication) {
        Assure assure = compteClientService.assureCourant(authentication.getName());
        return assure.getIdAssure();
    }

    private boolean estClient(Authentication authentication) {
        return !authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_AGENT".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }
}