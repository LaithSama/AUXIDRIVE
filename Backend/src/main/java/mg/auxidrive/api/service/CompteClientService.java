package mg.auxidrive.api.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.RoleRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Comptes clients. Un compte est normalement créé par la validation d'une inscription ; l'agent
 * peut seulement le consulter et le suspendre ou le réactiver. L'administrateur, lui, peut créer le
 * compte d'un assuré existant (mot de passe temporaire envoyé par e-mail), le modifier ou le
 * supprimer (suppression refusée tant que le compte est référencé).
 */
@Service
public class CompteClientService {

    private static final Logger log = LoggerFactory.getLogger(CompteClientService.class);

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final CompteClientRepository compteClientRepository;
    private final AssureRepository assureRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public CompteClientService(CompteClientRepository compteClientRepository,
                               AssureRepository assureRepository,
                               UtilisateurRepository utilisateurRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder,
                               MailService mailService) {
        this.compteClientRepository = compteClientRepository;
        this.assureRepository = assureRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public List<CompteClient> lister() {
        return compteClientRepository.findAll();
    }

    /** Vue enrichie pour l'administrateur : l'assuré (nom, numéro) est joint à chaque compte. */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listerVue() {
        return compteClientRepository.findAll().stream().map(this::vue).toList();
    }

    /** Vue enrichie d'un compte précis (avec l'identité de l'assuré). */
    @Transactional(readOnly = true)
    public Map<String, Object> detail(Long id) {
        return vue(trouver(id));
    }

    @Transactional(readOnly = true)
    public CompteClient trouver(Long id) {
        return compteClientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte client introuvable"));
    }

    @Transactional
    public CompteClient changerStatut(Long id, StatutCompte statut) {
        CompteClient compte = trouver(id);
        compte.setStatutCompte(statut);
        return compte;
    }

    /**
     * Crée le compte de connexion d'un assuré existant. Le mot de passe temporaire est envoyé par
     * e-mail, et le résultat de l'envoi est restitué à l'administrateur, qui dispose du mot de
     * passe dans la foulée et peut le transmettre lui-même si l'envoi a échoué (MailService
     * tente également un relais vers l'administrateur). L'envoi a lieu après le flush de
     * l'entité pour que l'adresse soit déjà validée par la base avant tout envoi.
     */
    @Transactional
    public Map<String, Object> creer(Long idAssure, String emailBrut) {
        Assure assure = assureRepository.findById(idAssure)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable"));
        if (compteClientRepository.findByAssure_IdAssure(idAssure).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet assuré possède déjà un compte client");
        }
        String email = emailBrut == null ? "" : emailBrut.trim().toLowerCase();
        if (email.isEmpty() || !EMAIL.matcher(email).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse e-mail invalide");
        }
        if (utilisateurRepository.existsByEmail(email) || compteClientRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
        }
        Role roleClient = roleRepository.findByLibRole("CLIENT")
                .orElseThrow(() -> new IllegalStateException("Le rôle CLIENT est absent de la base"));

        String motDePasse = genererMotDePasse();
        CompteClient compte = new CompteClient();
        compte.setAssure(assure);
        compte.setEmail(email);
        compte.setMotDePasse(passwordEncoder.encode(motDePasse));
        compte.setRole(roleClient);
        compte.setStatutCompte(StatutCompte.ACTIF);
        compte.setDateCreation(LocalDateTime.now());
        compteClientRepository.save(compte);
        try {
            compteClientRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
        }

        boolean emailEnvoye;
        try {
            emailEnvoye = mailService.envoyerIdentifiants(new MailService.DonneesIdentifiants(
                    email, motDePasse,
                    assure.getCivilite(), assure.getNomAssure(), assure.getPrenomAssure(),
                    assure.getNumerosAssure(), MailService.OrigineCompte.COMPTE_CREE));
        } catch (RuntimeException e) {
            // le compte est créé : un souci de configuration ou de transport ne doit pas
            // transformer la requête de l'administrateur en erreur, il perdrait le mot de passe
            log.error("Envoi des identifiants impossible à {} (compte néanmoins créé)", email, e);
            emailEnvoye = false;
        }

        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("compte", vue(compte));
        resultat.put("motDePasseTemporaire", motDePasse);
        resultat.put("emailEnvoye", emailEnvoye);
        return resultat;
    }

    /** Mise à jour partielle : seul l'e-mail et le statut sont modifiables, le rôle reste CLIENT. */
    @Transactional
    public CompteClient modifier(Long id, CompteClient donnees) {
        CompteClient compte = trouver(id);
        if (donnees.getEmail() != null && !donnees.getEmail().isBlank()) {
            String email = donnees.getEmail().trim().toLowerCase();
            if (!email.equals(compte.getEmail())) {
                if (!EMAIL.matcher(email).matches()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse e-mail invalide");
                }
                if (utilisateurRepository.existsByEmail(email) || compteClientRepository.existsByEmail(email)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
                }
                compte.setEmail(email);
            }
        }
        if (donnees.getStatutCompte() != null) {
            compte.setStatutCompte(donnees.getStatutCompte());
        }
        return compte;
    }

    /** Suppression réelle : refusée si le compte est encore référencé. */
    @Transactional
    public void supprimer(Long id) {
        CompteClient compte = trouver(id);
        try {
            compteClientRepository.delete(compte);
            compteClientRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Compte client référencé par d'autres données : suspendez-le ou passez-le en INACTIF à la place");
        }
    }

    /**
     * Assuré correspondant au compte connecté : c'est le point d'entrée du portail client.
     * L'e-mail de connexion identifie le compte, et donc l'assuré dont les données sont accessibles.
     */
    @Transactional(readOnly = true)
    public Assure assureCourant(String email) {
        CompteClient compte = compteClientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte client introuvable"));
        Assure assure = compte.getAssure();
        if (assure == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable");
        }
        return assure;
    }

    // ------------------------------------------------------------------ utilitaires

    /** Représentation neutre (sans mot de passe) exposant l'identité de l'assuré au tableau admin. */
    private Map<String, Object> vue(CompteClient c) {
        Map<String, Object> vue = new LinkedHashMap<>();
        vue.put("idCompteClient", c.getIdCompteClient());
        vue.put("email", c.getEmail());
        vue.put("statutCompte", c.getStatutCompte());
        vue.put("dateCreation", c.getDateCreation());
        vue.put("dateDerniereConnexion", c.getDateDerniereConnexion());
        Assure assure = c.getAssure();
        vue.put("idAssure", assure == null ? null : assure.getIdAssure());
        vue.put("numerosAssure", assure == null ? null : assure.getNumerosAssure());
        vue.put("nomAssure", assure == null ? null : assure.getNomAssure());
        vue.put("prenomAssure", assure == null ? null : assure.getPrenomAssure());
        return vue;
    }

    private String genererMotDePasse() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
