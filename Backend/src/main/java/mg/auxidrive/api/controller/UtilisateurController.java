package mg.auxidrive.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.service.UtilisateurService;

/** Gestion des agents et administrateurs (rôle ADMIN uniquement, voir SecurityConfig). */
@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /** Corps : {"email","nomUtil","motDePasse","role":{"libRole":"AGENT"}}. */
    @PostMapping
    public ResponseEntity<Utilisateur> creer(@RequestBody Utilisateur utilisateur) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.creer(utilisateur));
    }

    @GetMapping
    public List<Utilisateur> lister() {
        return utilisateurService.lister();
    }

    @GetMapping("/{id}")
    public Utilisateur trouver(@PathVariable Long id) {
        return utilisateurService.trouver(id);
    }

    @PutMapping("/{id}")
    public Utilisateur modifier(@PathVariable Long id, @RequestBody Utilisateur utilisateur) {
        return utilisateurService.modifier(id, utilisateur);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id, Authentication authentication) {
        utilisateurService.supprimer(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
