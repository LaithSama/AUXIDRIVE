package mg.auxidrive.api.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.service.ProfilService;

/**
 * Profil du client connecté : consultation et modification des informations,
 * changement de mot de passe, et suppression programmée avec délai de 72 h.
 *
 * L'e-mail du token identifie le compte : aucune idée de profil n'est acceptée depuis le corps.
 * Les règles sont posées deux fois : par motif d'URL dans SecurityConfig et par @PreAuthorize ici.
 */
@RestController
@RequestMapping("/api/profil")
@PreAuthorize("hasRole('CLIENT')")
public class ProfilController {

    private final ProfilService profilService;

    public ProfilController(ProfilService profilService) {
        this.profilService = profilService;
    }

    /** GET /api/profil : fiche du compte connecté. */
    @GetMapping
    public Map<String, Object> profil(Authentication authentication) {
        return profilService.profil(authentication.getName());
    }

    /** PUT /api/profil : mises à jour des informations et de l'e-mail de connexion. */
    @PutMapping
    public Map<String, Object> enregistrer(Authentication authentication, @RequestBody Map<String, Object> corps) {
        return profilService.enregistrer(authentication.getName(), corps);
    }

    /** PUT /api/profil/mot-de-passe : {"motDePasseActuel","nouveauMotDePasse"}. */
    @PutMapping("/mot-de-passe")
    public Map<String, Object> changerMotDePasse(Authentication authentication,
                                                 @RequestBody Map<String, String> corps) {
        profilService.changerMotDePasse(authentication.getName(),
                corps.get("motDePasseActuel"), corps.get("nouveauMotDePasse"));
        return Map.of("modifie", true);
    }

    /** POST /api/profil/suppression : planifie la suppression définitive à +72 h. */
    @PostMapping("/suppression")
    public Map<String, Object> planifierSuppression(Authentication authentication) {
        return profilService.planifierSuppression(authentication.getName());
    }

    /** POST /api/profil/suppression/annuler : annule la suppression programmée. */
    @PostMapping("/suppression/annuler")
    public Map<String, Object> annulerSuppression(Authentication authentication) {
        return profilService.annulerSuppression(authentication.getName());
    }
}
