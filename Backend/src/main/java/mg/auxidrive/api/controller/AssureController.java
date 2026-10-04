package mg.auxidrive.api.controller;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.service.AssureService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assures")
public class AssureController {

    private final AssureService assureService;

    public AssureController(AssureService assureService) {
        this.assureService = assureService;
    }

    @GetMapping
    public List<Assure> lister() {
        return assureService.listerAssure();
    }

    // Fiche complète : assuré, professions, identifiants légaux
    @GetMapping("/{id}")
    public Map<String, Object> fiche(@PathVariable Long idAssure) {
        return assureService.fiche(idAssure);
    }

    @PutMapping("/{id}")
    public Assure modifier(@PathVariable Long idAssure, @RequestBody Assure assure) {
        return assureService.modifierAssure(idAssure, assure);
    }
}
