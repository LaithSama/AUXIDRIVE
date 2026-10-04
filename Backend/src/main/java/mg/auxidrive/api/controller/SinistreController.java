package mg.auxidrive.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.DemandeSinistre;
import mg.auxidrive.api.model.Sinistre;
import mg.auxidrive.api.service.CompteClientService;
import mg.auxidrive.api.service.SinistreService;
import mg.auxidrive.api.service.UtilisateurService;

/**
 * Sinistres.
 *
 * L'assuré déclare un sinistre sur un de ses contrats validés et suit son traitement.
 * L'agent instruit puis clôt le dossier : rapport police, estimation, indemnisation ou refus.
 * Ces dernières étapes touchent aux versements, elles sont donc réservées au staff.
 */
@RestController
@RequestMapping("/api/sinistres")
public class SinistreController {

    private static final String ROLES_STAFF = "hasAnyRole('AGENT', 'ADMIN')";

    private final SinistreService sinistreService;
    private final CompteClientService compteClientService;
    private final UtilisateurService utilisateurService;

    public SinistreController(SinistreService sinistreService,
                              CompteClientService compteClientService,
                              UtilisateurService utilisateurService) {
        this.sinistreService = sinistreService;
        this.compteClientService = compteClientService;
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    @PreAuthorize(ROLES_STAFF)
    public List<Sinistre> lister() {
        return sinistreService.lister();
    }

    @GetMapping("/contrat/{idContrat}")
    @PreAuthorize(ROLES_STAFF)
    public List<Sinistre> listerParContrat(@PathVariable Long idContrat) {
        return sinistreService.listerParContrat(idContrat);
    }

    @GetMapping("/compte")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Sinistre> mesSinistres(Authentication authentication) {
        return sinistreService.listerParAssure(idAssure(authentication));
    }

    @GetMapping("/{id}")
    public Sinistre trouver(@PathVariable Long id, Authentication authentication) {
        if (estClient(authentication)) {
            return sinistreService.trouverPourAssure(id, idAssure(authentication));
        }
        return sinistreService.trouver(id);
    }

    /** Déclaration par l'assuré. Le service vérifie le contrat et la couverture du véhicule. */
    @PostMapping("/declarer")
    public Sinistre declarer(@RequestBody DemandeSinistre demande, Authentication authentication) {
        if (demande.getIdContrat() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le contrat est obligatoire");
        }
        Long idAssure = idAssure(authentication);
        return sinistreService.declarer(demande.getIdContrat(), demande.getIdVehicule(),
                demande.getDateSinistre(), demande.getLieuSinistre(),
                demande.getCirconstances(), idAssure);
    }

    /** Rattachement du rapport policier par l'agent. */
    @PostMapping("/{id}/rapport")
    @PreAuthorize(ROLES_STAFF)
    public Sinistre attacherRapport(@PathVariable Long id, @RequestBody Map<String, Object> corps,
                                    Authentication authentication) {
        String numRapport = (String) corps.get("numRapportPolice");
        java.time.LocalDate dateRapport = corps.get("dateRapportPolice") == null ? null
                : java.time.LocalDate.parse((String) corps.get("dateRapportPolice"));
        String rapport = (String) corps.get("rapportPolicier");
        Boolean responsabilite = corps.get("responsabiliteAssure") == null ? null
                : Boolean.valueOf(String.valueOf(corps.get("responsabiliteAssure")));

        return sinistreService.attacherRapport(id, numRapport, dateRapport, rapport, responsabilite,
                agent(authentication));
    }

    @PostMapping("/{id}/estimation")
    @PreAuthorize(ROLES_STAFF)
    public Sinistre estimer(@PathVariable Long id, @RequestBody Map<String, Object> corps) {
        Double montant = corps.get("montant") == null ? null : Double.valueOf(String.valueOf(corps.get("montant")));
        return sinistreService.estimerDommages(id, montant);
    }

    @PostMapping("/{id}/indemniser")
    @PreAuthorize(ROLES_STAFF)
    public Sinistre indemniser(@PathVariable Long id, @RequestBody Map<String, Object> corps) {
        Double montant = corps.get("montant") == null ? null : Double.valueOf(String.valueOf(corps.get("montant")));
        return sinistreService.indemniser(id, montant);
    }

    @PostMapping("/{id}/refuser")
    @PreAuthorize(ROLES_STAFF)
    public Sinistre refuser(@PathVariable Long id) {
        return sinistreService.refuser(id);
    }

    /** L'agent connecté, pour tracer qui a instruit le dossier. */
    private mg.auxidrive.api.model.Utilisateur agent(Authentication authentication) {
        try {
            return utilisateurService.trouverParEmail(authentication.getName());
        } catch (RuntimeException e) {
            return null;
        }
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