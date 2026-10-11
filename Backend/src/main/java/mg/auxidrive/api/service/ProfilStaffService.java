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

import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Profil de l'agent ou de l'administrateur connecté : consultation et modification de ses
 * informations (nom, e-mail de connexion) et changement de mot de passe.
 *
 * Le rôle et le statut ne sont jamais modifiables depuis le profil : ils restent du ressort de
 * l'administrateur. L'e-mail du token identifie le compte, aucune id n'est acceptée depuis le corps.
 */
@Service
public class ProfilStaffService {

    private static final Pattern EMAIL_VALIDE = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfilStaffService(UtilisateurRepository utilisateurRepository,
                              CompteClientRepository compteClientRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Fiche profil : nom, e-mail de connexion, rôle, statut et dates. */
    @Transactional(readOnly = true)
    public Map<String, Object> profil(String email) {
        return fiche(utilisateur(email));
    }

    /**
     * Mise à jour du nom et de l'e-mail de connexion.
     * Corps : {"nomUtil","emailConnexion"}.
     */
    @Transactional
    public Map<String, Object> enregistrer(String email, Map<String, Object> corps) {
        Utilisateur utilisateur = utilisateur(email);

        String nom = texte(corps, "nomUtil");
        String emailConnexion = texte(corps, "emailConnexion").toLowerCase();

        if (nom.isBlank()) {
            throw badRequest("Le nom est obligatoire.");
        }
        if (nom.length() > 40) {
            throw badRequest("Le nom dépasse 40 caractères.");
        }
        verifierEmail(emailConnexion);

        if (!utilisateur.getEmail().equalsIgnoreCase(emailConnexion)
                && (utilisateurRepository.existsByEmail(emailConnexion)
                    || compteClientRepository.existsByEmail(emailConnexion))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée.");
        }

        utilisateur.setNomUtil(nom);
        utilisateur.setEmail(emailConnexion);
        return fiche(utilisateur);
    }

    /** Changement de mot de passe : l'ancien mot de passe doit être fourni et vérifié. */
    @Transactional
    public void changerMotDePasse(String email, String ancien, String nouveau) {
        Utilisateur utilisateur = utilisateur(email);
        if (ancien == null || ancien.isBlank()) {
            throw badRequest("L'ancien mot de passe est obligatoire.");
        }
        if (nouveau == null || nouveau.length() < 8 || nouveau.length() > 72) {
            throw badRequest("Le nouveau mot de passe doit contenir entre 8 et 72 caractères.");
        }
        if (utilisateur.getMotDePasse() == null
                || !passwordEncoder.matches(ancien, utilisateur.getMotDePasse())) {
            throw badRequest("L'ancien mot de passe est incorrect.");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(nouveau));
    }

    // ------------------------------------------------------------------ utilitaires

    private Map<String, Object> fiche(Utilisateur utilisateur) {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("email", utilisateur.getEmail());
        reponse.put("nomUtil", utilisateur.getNomUtil());
        reponse.put("role", utilisateur.getRole() == null ? null : utilisateur.getRole().getLibRole());
        reponse.put("statutUtilisateur",
                utilisateur.getStatutUtilisateur() == null ? null : utilisateur.getStatutUtilisateur().name());
        reponse.put("dateCreation", iso(utilisateur.getDateCreation()));
        reponse.put("derniereConnexion", iso(utilisateur.getDerniereConnexion()));
        return reponse;
    }

    private Utilisateur utilisateur(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private String texte(Map<String, Object> corps, String cle) {
        Object valeur = corps.get(cle);
        return valeur == null ? "" : valeur.toString().trim();
    }

    private void verifierEmail(String valeur) {
        if (valeur.isBlank() || !EMAIL_VALIDE.matcher(valeur).matches()) {
            throw badRequest("L'adresse e-mail de connexion n'est pas valide.");
        }
        if (valeur.length() > 100) {
            throw badRequest("L'adresse e-mail dépasse 100 caractères.");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private String iso(LocalDateTime valeur) {
        return valeur == null ? null : valeur.format(ISO);
    }
}
