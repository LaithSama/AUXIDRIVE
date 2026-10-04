package mg.auxidrive.api.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.repository.AssureIdentifiantsLegauxRepository;
import mg.auxidrive.api.repository.AssureProfessionRepository;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;

/**
 * Consultation et mise à jour des assurés. Un assuré n'est jamais créé ici (c'est l'inscription qui le crée)
 * ni supprimé (il est référencé par ses contrats, véhicules et documents).
 */
@Service
public class AssureService {

    private final AssureRepository assureRepository;
    private final AssureProfessionRepository professionRepository;
    private final AssureIdentifiantsLegauxRepository legauxRepository;
    private final CompteClientRepository compteClientRepository;

    public AssureService(AssureRepository assureRepository,
                         AssureProfessionRepository professionRepository,
                         AssureIdentifiantsLegauxRepository legauxRepository,
                         CompteClientRepository compteClientRepository) {
        this.assureRepository = assureRepository;
        this.professionRepository = professionRepository;
        this.legauxRepository = legauxRepository;
        this.compteClientRepository = compteClientRepository;
    }

    @Transactional(readOnly = true)
    public List<Assure> lister() {
        return assureRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Assure trouver(Long id) {
        return assureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable"));
    }

    /** Fiche complète : l'assuré, ses professions, ses identifiants légaux et son compte client. */
    @Transactional(readOnly = true)
    public Map<String, Object> fiche(Long id) {
        Assure assure = trouver(id);
        Map<String, Object> fiche = new LinkedHashMap<>();
        fiche.put("assure", assure);
        fiche.put("professions", professionRepository.findByAssure_IdAssure(id));
        fiche.put("identifiants", legauxRepository.findByAssure_IdAssure(id).orElse(null));
        fiche.put("compte", compteClientRepository.findByAssure_IdAssure(id).orElse(null));
        return fiche;
    }

    /** Le numéro d'assuré, l'e-mail (identifiant de connexion) et le compte ne sont pas modifiables ici. */
    @Transactional
    public Assure modifier(Long id, Assure donnees) {
        Assure assure = trouver(id);
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
