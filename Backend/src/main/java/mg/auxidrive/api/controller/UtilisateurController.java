package mg.auxidrive.api.controller;

import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.service.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // Corps : {"email","nomUtil","motDePasse","role":{"libRole":"AGENT"}}.
    @PostMapping
    public ResponseEntity<Utilisateur> creer(@RequestBody Utilisateur utilisateur) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(utilisateur));
    }

    @GetMapping
    public List<Utilisateur> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    public Utilisateur trouver(@PathVariable Long idUtil) {
        return utilisateurService.trouverUtilisateur(idUtil);
    }

    @PutMapping("/{id}")
    public Utilisateur modifier(@PathVariable Long idUtil, @RequestBody Utilisateur utilisateur) {
        return utilisateurService.modifierUtilisateur(idUtil, utilisateur);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long idUtil, Authentication authentication) {
        utilisateurService.supprimer(idUtil, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
