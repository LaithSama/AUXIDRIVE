package mg.auxidrive.api.controller;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.service.ProfilStaffService;

/**
 * Profil de l'agent ou de l'administrateur connecté : consultation et modification de ses
 * informations (nom, e-mail de connexion) et changement de mot de passe.
 *
 * L'e-mail du token identifie le compte : aucune idée de profil n'est acceptée depuis le corps.
 * Les règles sont posées deux fois : par motif d'URL dans SecurityConfig et par @PreAuthorize ici.
 */
@RestController
@RequestMapping("/api/profil-staff")
@PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
public class ProfilStaffController {

    private final ProfilStaffService profilStaffService;

    public ProfilStaffController(ProfilStaffService profilStaffService) {
        this.profilStaffService = profilStaffService;
    }

    /** GET /api/profil-staff : fiche du compte connecté. */
    @GetMapping
    public Map<String, Object> profil(Authentication authentication) {
        return profilStaffService.profil(authentication.getName());
    }

    /** PUT /api/profil-staff : mise à jour du nom et de l'e-mail de connexion. */
    @PutMapping
    public Map<String, Object> enregistrer(Authentication authentication,
                                           @RequestBody Map<String, Object> corps) {
        return profilStaffService.enregistrer(authentication.getName(), corps);
    }

    /** PUT /api/profil-staff/mot-de-passe : {"motDePasseActuel","nouveauMotDePasse"}. */
    @PutMapping("/mot-de-passe")
    public Map<String, Object> changerMotDePasse(Authentication authentication,
                                                 @RequestBody Map<String, String> corps) {
        profilStaffService.changerMotDePasse(authentication.getName(),
                corps.get("motDePasseActuel"), corps.get("nouveauMotDePasse"));
        return Map.of("modifie", true);
    }
}
