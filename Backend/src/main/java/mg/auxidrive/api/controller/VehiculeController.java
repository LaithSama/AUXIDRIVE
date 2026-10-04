package mg.auxidrive.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.service.CompteClientService;
import mg.auxidrive.api.service.VehiculeService;

/**
 * Véhicules des assurés.
 *
 * Un client ne voit que ses propres véhicules : la cible est déduite de son compte connecté
 * et l'identifiant d'assuré éventuellement fourni dans la requête est ignoré.
 * L'agent et l'administrateur travaillent sur n'importe quel assuré.
 *
 * L'accès est ouvert au client parce que la souscription en a besoin ; les contrats et les
 * factures, qui portent des données financières, ne le sont pas.
 *
 * Les règles sont posées deux fois : par motif d'URL dans SecurityConfig et par @PreAuthorize ici.
 * Les deux couches sont nécessaires, l'une sans l'autre laisserait un accès ouvert.
 */
@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private static final String ROLES_STAFF = "hasAnyRole('AGENT', 'ADMIN')";

    private final VehiculeService vehiculeService;
    private final CompteClientService compteClientService;

    public VehiculeController(VehiculeService vehiculeService, CompteClientService compteClientService) {
        this.vehiculeService = vehiculeService;
        this.compteClientService = compteClientService;
    }

    /** Tous les véhicules : agent et administrateur. */
    @GetMapping
    @PreAuthorize(ROLES_STAFF)
    public List<Vehicule> listerTous() {
        return vehiculeService.lister();
    }

    /** Véhicules d'un assuré : agent et administrateur. Un client utilise /api/vehicules/compte. */
    @GetMapping("/assure/{idAssure}")
    @PreAuthorize(ROLES_STAFF)
    public List<Vehicule> listerParAssure(@PathVariable Long idAssure) {
        return vehiculeService.listerParAssure(idAssure);
    }

    /** Véhicules du client connecté. */
    @GetMapping("/compte")
    @PreAuthorize("hasRole('CLIENT')")
    public List<Vehicule> listerMesVehicules(Authentication authentication) {
        return vehiculeService.listerParAssure(idAssure(authentication));
    }

    /**
     * Un véhicule, soit pour l'agent et l'administrateur, soit pour le client
     * mais uniquement s'il lui appartient.
     */
    @GetMapping("/{id}")
    public Vehicule trouver(@PathVariable Long id, Authentication authentication) {
        if (estClient(authentication)) {
            return vehiculeService.trouverPourAssure(id, idAssure(authentication));
        }
        return vehiculeService.trouver(id);
    }

    /** Ajout par un agent ou un administrateur pour un assuré donné. */
    @PostMapping("/assure/{idAssure}")
    @PreAuthorize(ROLES_STAFF)
    public Vehicule creerPourAssure(@PathVariable Long idAssure, @RequestBody Vehicule donnees) {
        return vehiculeService.creer(donnees, idAssure);
    }

    /** Ajout par le client pour lui-même. */
    @PostMapping("/compte")
    @PreAuthorize("hasRole('CLIENT')")
    public Vehicule creer(Authentication authentication, @RequestBody Vehicule donnees) {
        return vehiculeService.creer(donnees, idAssure(authentication));
    }

    /** Mise à jour : un client ne modifie que ses propres véhicules, l'assuré d'un véhicule ne change pas. */
    @PutMapping("/{id}")
    public Vehicule modifier(@PathVariable Long id, Authentication authentication, @RequestBody Vehicule donnees) {
        if (estClient(authentication)) {
            vehiculeService.trouverPourAssure(id, idAssure(authentication));
        }
        return vehiculeService.modifier(id, donnees);
    }

    /** Suppression réservée à l'agent et à l'administrateur. */
    @DeleteMapping("/{id}")
    @PreAuthorize(ROLES_STAFF)
    public Map<String, Object> supprimer(@PathVariable Long id) {
        Vehicule vehicule = vehiculeService.supprimer(id);
        return Map.of("supprime", true, "idVehicule", vehicule.getIdVehicule());
    }

    /** Identifiant de l'assuré déduit du compte connecté. */
    private Long idAssure(Authentication authentication) {
        Assure assure = compteClientService.assureCourant(authentication.getName());
        return assure.getIdAssure();
    }

    /** Un client est celui qui n'a ni le rôle AGENT ni le rôle ADMIN. */
    private boolean estClient(Authentication authentication) {
        return !authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_AGENT".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }
}