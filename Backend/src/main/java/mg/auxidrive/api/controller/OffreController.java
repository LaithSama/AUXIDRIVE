package mg.auxidrive.api.controller;

import java.util.List;

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

import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.service.OffreService;

/** Catalogue d'offres : lecture publique, création/modification/suppression réservées à l'ADMIN. */
@RestController
@RequestMapping("/api/offres")
public class OffreController {

    private final OffreService offreService;

    public OffreController(OffreService offreService) {
        this.offreService = offreService;
    }

    @GetMapping
    public List<Offre> lister() {
        return offreService.lister();
    }

    @GetMapping("/{id}")
    public Offre trouver(@PathVariable Long id) {
        return offreService.trouver(id);
    }

    @PostMapping
    public ResponseEntity<Offre> creer(@RequestBody Offre offre) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offreService.creer(offre));
    }

    @PutMapping("/{id}")
    public Offre modifier(@PathVariable Long id, @RequestBody Offre offre) {
        return offreService.modifier(id, offre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        offreService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
