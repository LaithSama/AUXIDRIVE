package mg.auxidrive.api.service;

import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.repository.OffreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OffreService {

    private final OffreRepository offreRepository;

    public OffreService(OffreRepository offreRepository) {
        this.offreRepository = offreRepository;
    }

    @Transactional(readOnly = true)
    public List<Offre> listerOffre() {
        return offreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Offre trouverOffre(Long idOffre) {
        return offreRepository.findById(idOffre)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offre introuvable"));
    }

    @Transactional
    public Offre creerOffre(Offre offre) {
        verifierOffre(offre);
        offre.setIdOffre(null); // l'identifiant est généré par la base
        return offreRepository.save(offre);
    }

    @Transactional
    public Offre modifierOffre(Long idOffre, Offre donnees) {
        verifierOffre(donnees);
        Offre offre = trouverOffre(idOffre);
        offre.setLibelleOffre(donnees.getLibelleOffre());
        offre.setTypeOffre(donnees.getTypeOffre());
        offre.setDureeMinMois(donnees.getDureeMinMois());
        offre.setNbVehiculesMin(donnees.getNbVehiculesMin());
        offre.setTauxRemise(donnees.getTauxRemise());
        offre.setDescription(donnees.getDescription());
        return offre;
    }

    @Transactional
    public void supprimerOffre(Long idOffre) {
        Offre offre = trouverOffre(idOffre);
        try {
            offreRepository.delete(offre);
            offreRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Offre utilisée par des contrats, suppression impossible");
        }
    }

    private void verifierOffre(Offre offre) {
        if (offre.getLibelleOffre() == null || offre.getLibelleOffre().isBlank() || offre.getTypeOffre() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le libellé et le type d'offre sont obligatoires");
        }
    }
}
