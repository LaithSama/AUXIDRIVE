package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
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
 * Consultation et mise à jour des assurés. La création et la suppression sont réservées à
 * l'administrateur : un assuré se crée normalement par l'inscription, et sa suppression est
 * refusée tant qu'il est référencé par des contrats, véhicules ou documents.
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

    /**
     * Mise à jour partielle : seuls les champs renseignés sont modifiés.
     * Le numéro d'assuré, l'e-mail (identifiant de connexion) et le compte ne sont pas modifiables ici.
     */
    @Transactional
    public Assure modifier(Long id, Assure donnees) {
        Assure assure = trouver(id);
        if (vide(donnees.getNomAssure())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire");
        }
        assure.setNomAssure(donnees.getNomAssure().trim());
        if (donnees.getPrenomAssure() != null) assure.setPrenomAssure(trimOuNull(donnees.getPrenomAssure()));
        if (donnees.getPersonneMorale() != null) assure.setPersonneMorale(donnees.getPersonneMorale());
        if (donnees.getDateNaissance() != null) assure.setDateNaissance(donnees.getDateNaissance());
        if (donnees.getSexe() != null) assure.setSexe(trimOuNull(donnees.getSexe()));
        if (donnees.getCivilite() != null) assure.setCivilite(trimOuNull(donnees.getCivilite()));
        if (donnees.getNationalite() != null) assure.setNationalite(trimOuNull(donnees.getNationalite()));
        if (donnees.getPaysResidence() != null) assure.setPaysResidence(trimOuNull(donnees.getPaysResidence()));
        if (donnees.getAdresse() != null) assure.setAdresse(trimOuNull(donnees.getAdresse()));
        if (donnees.getContact() != null) assure.setContact(trimOuNull(donnees.getContact()));
        return assure;
    }

    /**
     * Création d'un assuré par l'administrateur. Le numéro d'assuré est obligatoire et saisi à la
     * main (jamais généré) : il doit être unique. Aucun compte de connexion n'est créé ici.
     */
    @Transactional
    public Assure creer(Assure donnees) {
        String numero = donnees.getNumerosAssure() == null ? "" : donnees.getNumerosAssure().trim();
        if (numero.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le numéro d'assuré est obligatoire");
        }
        if (numero.length() > 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le numéro d'assuré est limité à 8 caractères");
        }
        if (assureRepository.existsByNumerosAssure(numero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro d'assuré existe déjà");
        }
        if (vide(donnees.getNomAssure())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom est obligatoire");
        }
        if (!Boolean.TRUE.equals(donnees.getPersonneMorale()) && vide(donnees.getPrenomAssure())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le prénom est obligatoire pour une personne physique");
        }

        Assure assure = new Assure();
        assure.setNumerosAssure(numero);
        assure.setNomAssure(donnees.getNomAssure().trim());
        assure.setPrenomAssure(trimOuNull(donnees.getPrenomAssure()));
        assure.setPersonneMorale(Boolean.TRUE.equals(donnees.getPersonneMorale()));
        assure.setDateNaissance(donnees.getDateNaissance());
        assure.setSexe(trimOuNull(donnees.getSexe()));
        assure.setCivilite(trimOuNull(donnees.getCivilite()));
        assure.setNationalite(trimOuNull(donnees.getNationalite()));
        assure.setPaysResidence(trimOuNull(donnees.getPaysResidence()));
        assure.setAdresse(trimOuNull(donnees.getAdresse()));
        assure.setContact(trimOuNull(donnees.getContact()));
        assure.setMail(normaliserMail(donnees.getMail()));
        assure.setDateCreation(LocalDate.now());
        return assureRepository.save(assure);
    }

    /** Suppression réelle : refusée si l'assuré est référencé (contrats, véhicules, documents...). */
    @Transactional
    public void supprimer(Long id) {
        Assure assure = trouver(id);
        try {
            assureRepository.delete(assure);
            assureRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Assuré référencé par d'autres données (contrats, véhicules, documents...) : "
                            + "supprimez-les d'abord ou conservez la fiche");
        }
    }

    private String trimOuNull(String s) {
        return vide(s) ? null : s.trim();
    }

    private String normaliserMail(String mail) {
        return vide(mail) ? null : mail.trim().toLowerCase();
    }

    private boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
