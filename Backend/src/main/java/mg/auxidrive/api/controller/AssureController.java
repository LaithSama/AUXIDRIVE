package mg.auxidrive.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.service.AssureService;

/** Consultation et mise à jour des assurés (création par inscription, pas de suppression). */
@RestController
@RequestMapping("/api/assures")
public class AssureController {

    private final AssureService assureService;

    public AssureController(AssureService assureService) {
        this.assureService = assureService;
    }

    @GetMapping
    public List<Assure> lister() {
        return assureService.lister();
    }

    /** Fiche complète : assuré, professions, identifiants légaux. */
    @GetMapping("/{id}")
    public Map<String, Object> fiche(@PathVariable Long id) {
        return assureService.fiche(id);
    }

    @PutMapping("/{id}")
    public Assure modifier(@PathVariable Long id, @RequestBody Assure assure) {
        return assureService.modifier(id, assure);
    }
}
