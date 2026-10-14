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
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.DemandeSouscription;
import mg.auxidrive.api.model.ParametresPrime;
import mg.auxidrive.api.model.PrimeRequest;
import mg.auxidrive.api.service.CompteClientService;
import mg.auxidrive.api.service.ContratService;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Souscription et suivi des contrats.
 *
 * Un client ne voit et ne souscrit que pour lui-même : l'assuré est déduit de son compte connecté.
 * Seul l'agent valide ou refuse une demande, car ces décisions engagent la compagnie.
 */
@RestController
@RequestMapping("/api/contrats")
public class ContratController {

    private static final String ROLES_STAFF = "hasAnyRole('AGENT', 'ADMIN')";

    private final ContratService contratService;
    private final CompteClientService compteClientService;

    public ContratController(ContratService contratService, CompteClientService compteClientService) {
        this.contratService = contratService;
        this.compteClientService = compteClientService;
    }

    @GetMapping
    @PreAuthorize(ROLES_STAFF)
    public List<Contrat> lister() {
        return contratService.lister();
    }

    @GetMapping("/assure/{idAssure}")
    @PreAuthorize(ROLES_STAFF)
    public List<Contrat> listerParAssure(@PathVariable Long idAssure) {
        return contratService.listerParAssure(idAssure);
    }

    @GetMapping("/compte")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Contrat> mesContrats(Authentication authentication) {
        return contratService.listerParAssure(idAssure(authentication));
    }

    @GetMapping("/{id}")
    public Contrat trouver(@PathVariable Long id, Authentication authentication) {
        if (estClient(authentication)) {
            return contratService.trouverPourAssure(id, idAssure(authentication));
        }
        return contratService.trouver(id);
    }

    /**
     * Calcule une prime sans rien enregistrer.
     * Le client l'appelle pour afficher le montant TTC avant de confirmer sa demande.
     */
    @PostMapping("/simulation")
    public Detail simuler(@RequestBody DemandeSouscription demande, Authentication authentication) {
        if (estClient(authentication)) {
            verifierVehiculesAppartenance(demande, idAssure(authentication));
        }
        return contratService.simuler(demande);
    }

    /** Dépose une demande de souscription : le contrat part en attente de validation. */
    @PostMapping
    public Map<String, Object> souscrire(@RequestBody DemandeSouscription demande, Authentication authentication) {
        boolean client = estClient(authentication);
        Long idAssure = client ? idAssure(authentication) : null;
        if (client) {
            verifierVehiculesAppartenance(demande, idAssure);
        }
        if (idAssure == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Précisez l'assuré concerné dans la demande");
        }
        Contrat contrat = contratService.souscrire(demande, idAssure, client ? "PORTAL_CLIENT" : "AGENCE");
        return Map.of("idContrat", contrat.getIdContrat(),
                "statut", contrat.getStatutContrat(),
                "primeTtc", contrat.getPrimeTtcCourante());
    }

    @PostMapping("/{id}/valider")
    @PreAuthorize(ROLES_STAFF)
    public Contrat valider(@PathVariable Long id,
                           @RequestBody(required = false) PrimeRequest prime) {
        return contratService.valider(id, prime);
    }

    /**
     * Paramètres du calcul de prime d'un contrat : code taxe, taux appliqués et valeurs par défaut,
     * pour que l'agent prépare la modale de calcul de prime.
     */
    @GetMapping("/{id}/parametres-prime")
    @PreAuthorize(ROLES_STAFF)
    public ParametresPrime parametresPrime(@PathVariable Long id) {
        return contratService.parametresPrime(id);
    }

    /**
     * Calcule la prime d'un contrat sans rien enregistrer, à partir de la prime nette et des
     * accessoires saisis par l'agent. Le montant TTC n'est jamais reçu du client.
     */
    @PostMapping("/{id}/calculer-prime")
    @PreAuthorize(ROLES_STAFF)
    public Detail calculerPrime(@PathVariable Long id,
                                @RequestBody(required = false) PrimeRequest prime) {
        return contratService.calculerPrime(id, prime);
    }

    @PostMapping("/{id}/refuser")
    @PreAuthorize(ROLES_STAFF)
    public Contrat refuser(@PathVariable Long id) {
        return contratService.refuser(id);
    }

    /** Un client ne peut pas mobiliser un véhicule qui ne lui appartient pas. */
    private void verifierVehiculesAppartenance(DemandeSouscription demande, Long idAssure) {
        if (demande == null || demande.getIdVehicules() == null) {
            return;
        }
        demande.getIdVehicules().forEach(id -> contratService.vehiculeAppartientA(id, idAssure));
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