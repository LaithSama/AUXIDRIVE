package mg.auxidrive.api.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.repository.OffreRepository;

/** Catalogue d'offres (Monovéhicule, Flotte...) : CRUD complet. */
@Service
public class OffreService {

    private final OffreRepository offreRepository;

    public OffreService(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    @Transactional(readOnly = true)
    public List<Offre> lister() {
        return offreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Offre trouver(Long id) {
        return offreRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));
    }

    @Transactional
    public Offre creer(Offre offre) {
        verifier(offre);
        offre.setIdOffre(null); // l'identifiant est généré par la base
        return offreRepository.save(offre);
    }

    @Transactional
    public Offre modifier(Long id, Offre donnees) {
        verifier(donnees);
        Offre offre = trouver(id);
        offre.setLibelleOffre(donnees.getLibelleOffre());
        offre.setTypeOffre(donnees.getTypeOffre());
        offre.setDureeMinMois(donnees.getDureeMinMois());
        offre.setNbVehiculesMin(donnees.getNbVehiculesMin());
        offre.setTauxRemise(donnees.getTauxRemise());
        offre.setDescription(donnees.getDescription());
        return offre;
    }

    @Transactional
    public void supprimer(Long id) {
        Offre offre = trouver(id);
        try {
            offreRepository.delete(offre);
            offreRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Offre utilisée par des contrats, suppression impossible");
        }
    }

    private void verifier(Offre offre) {
        if (offre.getLibelleOffre() == null || offre.getLibelleOffre().isBlank() || offre.getTypeOffre() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le libellé et le type d'offre sont obligatoires");
        }
    }
}
