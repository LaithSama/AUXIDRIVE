package mg.auxidrive.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.service.CompteClientService;

/** Consultation des comptes clients et suspension/réactivation (agents et administrateurs). */
@RestController
@RequestMapping("/api/comptes-clients")
public class CompteClientController {

    private final CompteClientService compteClientService;

    public CompteClientController(CompteClientService compteClientService) {
        this.compteClientService = compteClientService;
    }

    @GetMapping
    public List<CompteClient> lister() {
        return compteClientService.lister();
    }

    @GetMapping("/{id}")
    public CompteClient trouver(@PathVariable Long id) {
        return compteClientService.trouver(id);
    }

    /** PUT /api/comptes-clients/{id}/statut?statut=SUSPENDU */
    @PutMapping("/{id}/statut")
    public CompteClient changerStatut(@PathVariable Long id, @RequestParam StatutCompte statut) {
        return compteClientService.changerStatut(id, statut);
    }
}
