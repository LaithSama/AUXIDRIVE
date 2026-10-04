package mg.auxidrive.api.controller;

import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.service.CompteClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comptes-clients")
public class CompteClientController {

    private final CompteClientService compteClientService;

    public CompteClientController(CompteClientService compteClientService) {
        this.compteClientService = compteClientService;
    }

    @GetMapping
    public List<CompteClient> lister() {
        return compteClientService.listerCompteClient();
    }

    @GetMapping("/{id}")
    public CompteClient trouver(@PathVariable Long idAssre) {
        return compteClientService.trouverCompte(idAssre);
    }

    /** PUT /api/comptes-clients/{id}/statut?statut=SUSPENDU */
    @PutMapping("/{id}/statut")
    public CompteClient changerStatut(@PathVariable Long idAssure, @RequestParam StatutCompte statut) {
        return compteClientService.changerStatut(idAssure, statut);
    }
}
