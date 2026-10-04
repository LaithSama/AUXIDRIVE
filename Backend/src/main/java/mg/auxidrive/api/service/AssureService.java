package mg.auxidrive.api.service;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.repository.AssureIdentifiantsLegauxRepository;
import mg.auxidrive.api.repository.AssureProfessionRepository;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssureService {
    private final AssureRepository assureRepository;
    private final AssureProfessionRepository professionRepository;
    private final AssureIdentifiantsLegauxRepository legauxRepository;
    private final CompteClientRepository compteClientRepository;

    public AssureService(AssureRepository assureRepository, AssureProfessionRepository professionRepository, AssureIdentifiantsLegauxRepository legauxRepository, CompteClientRepository compteClientRepository) {
        this.assureRepository = assureRepository;
        this.professionRepository = professionRepository;
        this.legauxRepository = legauxRepository;
        this.compteClientRepository = compteClientRepository;
    }

    @Transactional(readOnly = true)
    public List<Assure> listerAssure() {
        return assureRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Assure trouverAssure(Long idAssure) {
        return assureRepository.findById(idAssure).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable"));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> fiche(Long idAssure){
        Assure assure = trouverAssure(idAssure);
        Map<String, Object> fiche = new LinkedHashMap<>();
        fiche.put("assure", assure);
        fiche.put("professions", professionRepository.findByAssureId(idAssure));
        fiche.put("identifiants", legauxRepository.findByAssureId(idAssure));
        fiche.put("compte", compteClientRepository.findByAssureId(idAssure));
        return fiche;
    }

    @Transactional
    public Assure modifier(Long idAssure, Assure donnees) {
        Assure assure = trouverAssure(idAssure);
        if (donnees.getNomAssure() == null || donnees.getNomAssure().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire");
        }
        assure.setNomAssure(donnees.getNomAssure().trim());
        assure.setPrenomAssure(donnees.getPrenomAssure());
        assure.setPersonneMorale(donnees.getPersonneMorale());
        assure.setDateNaissance(donnees.getDateNaissance());
        assure.setSexe(donnees.getSexe());
        assure.setCivilite(donnees.getCivilite());
        assure.setNationalite(donnees.getNationalite());
        assure.setPaysResidence(donnees.getPaysResidence());
        assure.setAdresse(donnees.getAdresse());
        assure.setContact(donnees.getContact());
        return assure;
    }
}
