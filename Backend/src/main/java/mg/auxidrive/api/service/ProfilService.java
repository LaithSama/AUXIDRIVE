package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.repository.CompteClientRepository;

/**
 * Profil du client connecté : consultation et modification de ses informations,
 * changement de mot de passe, et suppression programmée avec délai de rétractation de 72 h.
 *
 * Le compte n'est jamais supprimé immédiatement : la demande pose une date (J+72 h) que le
 * client peut annuler depuis son profil ; la purge définitive est assurée par une tâche planifiée.
 */
@Service
public class ProfilService {

    /** Délai de rétractation avant suppression définitive, en heures. */
    public static final int DELAI_SUPPRESSION_HEURES = 72;

    private static final Pattern EMAIL_VALIDE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final CompteClientRepository compteClientRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfilService(CompteClientRepository compteClientRepository, PasswordEncoder passwordEncoder) {
        this.compteClientRepository = compteClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Fiche profil : compte (e-mail de connexion, dates) et données de l'assuré. */
    @Transactional(readOnly = true)
    public Map<String, Object> profil(String email) {
        CompteClient compte = compte(email);
        Assure assure = compte.getAssure();

        Map<String, Object> boutDeCompte = new LinkedHashMap<>();
        boutDeCompte.put("email", compte.getEmail());
        boutDeCompte.put("dateCreation", iso(compte.getDateCreation()));
        boutDeCompte.put("dateSuppressionPlanifiee", iso(compte.getDateSuppressionPlanifiee()));

        Map<String, Object> boutDAssure = new LinkedHashMap<>();
        if (assure != null) {
            boutDAssure.put("numerosAssure", assure.getNumerosAssure());
            boutDAssure.put("civilite", assure.getCivilite());
            boutDAssure.put("dateNaissance", assure.getDateNaissance() == null ? null : assure.getDateNaissance().toString());
            boutDAssure.put("nomAssure", assure.getNomAssure());
            boutDAssure.put("prenomAssure", assure.getPrenomAssure());
            boutDAssure.put("adresse", assure.getAdresse());
            boutDAssure.put("contact", assure.getContact());
            boutDAssure.put("mail", assure.getMail());
        }

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("compte", boutDeCompte);
        reponse.put("assure", boutDAssure);
        return reponse;
    }

    /**
     * Mise à jour des informations personnelles de l'assuré et de l'e-mail de connexion.
     * Corps : {"nomAssure","prenomAssure","adresse","contact","mail","emailConnexion"}.
     */
    @Transactional
    public Map<String, Object> enregistrer(String email, Map<String, Object> corps) {
        CompteClient compte = compte(email);
        Assure assure = compte.getAssure();
        if (assure == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable");
        }

        String nom = texte(corps, "nomAssure");
        String prenom = texte(corps, "prenomAssure");
        String adresse = texte(corps, "adresse");
        String contact = texte(corps, "contact");
        String mail = texte(corps, "mail");
        String emailConnexion = texte(corps, "emailConnexion");

        if (nom.isBlank() || prenom.isBlank()) {
            throw badRequest("Le nom et le prénom sont obligatoires.");
        }
        verifierLongueur(nom, 50, "nom");
        verifierLongueur(prenom, 50, "prénom");
        verifierLongueur(adresse, 50, "adresse");
        verifierLongueur(contact, 13, "téléphone");
        verifierLongueur(mail, 50, "e-mail personnel");
        verifierEmail(mail, "e-mail personnel");
        verifierEmail(emailConnexion, "e-mail de connexion");

        if (!compte.getEmail().equalsIgnoreCase(emailConnexion)
                && compteClientRepository.existsByEmail(emailConnexion)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cet e-mail de connexion est déjà utilisé.");
        }

        assure.setNomAssure(nom);
        assure.setPrenomAssure(prenom);
        assure.setAdresse(adresse);
        assure.setContact(contact);
        assure.setMail(mail);
        compte.setEmail(emailConnexion);
        return profil(compte.getEmail());
    }

    /** Changement de mot de passe : l'ancien mot de passe doit être fourni et vérifié. */
    @Transactional
    public void changerMotDePasse(String email, String ancien, String nouveau) {
        CompteClient compte = compte(email);
        if (ancien == null || ancien.isBlank()) {
            throw badRequest("L'ancien mot de passe est obligatoire.");
        }
        if (nouveau == null || nouveau.length() < 8) {
            throw badRequest("Le nouveau mot de passe doit contenir au moins 8 caractères.");
        }
        if (compte.getMotDePasse() == null || !passwordEncoder.matches(ancien, compte.getMotDePasse())) {
            throw badRequest("L'ancien mot de passe est incorrect.");
        }
        compte.setMotDePasse(passwordEncoder.encode(nouveau));
    }

    /** Pose la date de suppression définitive à maintenant + 72 h et la retourne (ISO). */
    @Transactional
    public Map<String, Object> planifierSuppression(String email) {
        CompteClient compte = compte(email);
        compte.setDateSuppressionPlanifiee(LocalDateTime.now().plusHours(DELAI_SUPPRESSION_HEURES));
        return dateSuppression(compte);
    }

    /** Annule la suppression programmée. */
    @Transactional
    public Map<String, Object> annulerSuppression(String email) {
        CompteClient compte = compte(email);
        compte.setDateSuppressionPlanifiee(null);
        return dateSuppression(compte);
    }

    /** Suppression définitive des comptes dont le délai est écoulé (appelée par la tâche planifiée). */
    @Transactional
    public int purgerEchues() {
        return compteClientRepository.purgeEchues(LocalDateTime.now());
    }

    private Map<String, Object> dateSuppression(CompteClient compte) {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("dateSuppressionPlanifiee", iso(compte.getDateSuppressionPlanifiee()));
        return reponse;
    }

    private CompteClient compte(String email) {
        return compteClientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte client introuvable"));
    }

    private String texte(Map<String, Object> corps, String cle) {
        Object valeur = corps.get(cle);
        return valeur == null ? "" : valeur.toString().trim();
    }

    private void verifierLongueur(String valeur, int max, String libelle) {
        if (valeur.length() > max) {
            throw badRequest("Le champ « " + libelle + " » dépasse " + max + " caractères.");
        }
    }

    private void verifierEmail(String valeur, String libelle) {
        if (valeur.isBlank() || !EMAIL_VALIDE.matcher(valeur).matches()) {
            throw badRequest("L'adresse « " + libelle + " » n'est pas valide.");
        }
        if (valeur.length() > 50) {
            throw badRequest("Le champ « " + libelle + " » dépasse 50 caractères.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private String iso(LocalDateTime valeur) {
        return valeur == null ? null : valeur.format(ISO);
    }
}
