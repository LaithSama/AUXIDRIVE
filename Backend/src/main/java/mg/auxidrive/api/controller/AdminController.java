package mg.auxidrive.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.service.AdminService;
import mg.auxidrive.api.service.AssureService;
import mg.auxidrive.api.service.CompteClientService;

/**
 * Espace d'administration (rôle ADMIN uniquement, voir SecurityConfig) : tableau de bord et gestion
 * des assurés et des comptes clients. La gestion des agents/administrateurs reste sur
 * {@code /api/utilisateurs}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    /** Corps de création d'un compte client : l'assuré existant et son e-mail de connexion. */
    public record DemandeCompte(Long idAssure, String email) {
    }

    private final AdminService adminService;
    private final AssureService assureService;
    private final CompteClientService compteClientService;

    public AdminController(AdminService adminService,
                           AssureService assureService,
                           CompteClientService compteClientService) {
        this.adminService = adminService;
        this.assureService = assureService;
        this.compteClientService = compteClientService;
    }

    // ---------- tableau de bord ----------

    @GetMapping("/statistiques")
    public Map<String, Object> statistiques() {
        return adminService.statistiques();
    }

    @GetMapping("/roles")
    public List<Role> roles() {
        return adminService.roles();
    }

    // ---------- assurés ----------

    @GetMapping("/assures")
    public List<Assure> assures() {
        return assureService.lister();
    }

    /** Fiche complète : assuré, professions, identifiants légaux et compte client. */
    @GetMapping("/assures/{id}")
    public Map<String, Object> assure(@PathVariable Long id) {
        return assureService.fiche(id);
    }

    @PostMapping("/assures")
    public ResponseEntity<Assure> creerAssure(@RequestBody Assure assure) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assureService.creer(assure));
    }

    @PutMapping("/assures/{id}")
    public Assure modifierAssure(@PathVariable Long id, @RequestBody Assure assure) {
        return assureService.modifier(id, assure);
    }

    @DeleteMapping("/assures/{id}")
    public ResponseEntity<Void> supprimerAssure(@PathVariable Long id) {
        assureService.supprimer(id);
        return ResponseEntity.noContent().build();
    }

    // ---------- comptes clients ----------

    /** Liste enrichie : chaque compte expose le nom et le numéro de son assuré. */
    @GetMapping("/comptes-clients")
    public List<Map<String, Object>> comptes() {
        return compteClientService.listerVue();
    }

    /** Crée le compte d'un assuré existant ; renvoie le mot de passe temporaire pour transmission. */
    @PostMapping("/comptes-clients")
    public ResponseEntity<Map<String, Object>> creerCompte(@RequestBody DemandeCompte demande) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(compteClientService.creer(demande.idAssure(), demande.email()));
    }

    @PutMapping("/comptes-clients/{id}")
    public Map<String, Object> modifierCompte(@PathVariable Long id, @RequestBody CompteClient compte) {
        compteClientService.modifier(id, compte);
        return compteClientService.detail(id);
    }

    @DeleteMapping("/comptes-clients/{id}")
    public ResponseEntity<Void> supprimerCompte(@PathVariable Long id) {
        compteClientService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
