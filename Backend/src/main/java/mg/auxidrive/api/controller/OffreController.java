package mg.auxidrive.api.controller;

import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.service.OffreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
    }

    @GetMapping
    public List<Offre> lister() {
        return offreService.listerOffre();
    }

    @GetMapping("/{id}")
    public Offre trouver(@PathVariable Long idOffre) {
        return offreService.trouverOffre(idOffre);
    }

    @PostMapping
    public ResponseEntity<Offre> creer(@RequestBody Offre offre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offreService.creerOffre(offre));
    }

    @PutMapping("/{id}")
    public Offre modifier(@PathVariable Long idOffre, @RequestBody Offre offre) {
        return offreService.modifierOffre(idOffre, offre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long idOffre) {
        offreService.supprimerOffre(idOffre);
        return ResponseEntity.noContent().build();
    }
}
