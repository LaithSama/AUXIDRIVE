package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutUtilisateur;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.RoleRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Gestion des agents et administrateurs (réservée à l'administrateur).
 * Les comptes clients (table Comptes_clients) ne se créent pas ici : ils sont créés par la validation d'une inscription.
 */
@Service
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepository,
                              CompteClientRepository compteClientRepository,
                              RoleRepository roleRepository,
                              PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Utilisateur trouver(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    /** Retrouve l'agent ou l'administrateur connecté par son e-mail, pour tracer ses acts. */
    @Transactional(readOnly = true)
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    @Transactional
    public Utilisateur creer(Utilisateur donnees) {
        String email = donnees.getEmail() == null ? "" : donnees.getEmail().trim().toLowerCase();
        if (email.isEmpty() || donnees.getNomUtil() == null || donnees.getNomUtil().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail et nom obligatoires");
        }
        verifierMotDePasse(donnees.getMotDePasse());
        if (utilisateurRepository.existsByEmail(email) || compteClientRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
        }
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setNomUtil(donnees.getNomUtil().trim());
        utilisateur.setMotDePasse(passwordEncoder.encode(donnees.getMotDePasse()));
        utilisateur.setRole(trouverRoleInterne(donnees));
        utilisateur.setStatutUtilisateur(StatutUtilisateur.ACTIF);
        utilisateur.setDateCreation(LocalDateTime.now());
        return utilisateurRepository.save(utilisateur);
    }

    /** Seuls les champs renseignés sont modifiés. Le mot de passe n'est ré-encodé que s'il est fourni. */
    @Transactional
    public Utilisateur modifier(Long id, Utilisateur donnees) {
        Utilisateur utilisateur = trouver(id);
        if (donnees.getNomUtil() != null && !donnees.getNomUtil().isBlank()) {
            utilisateur.setNomUtil(donnees.getNomUtil().trim());
        }
        if (donnees.getEmail() != null && !donnees.getEmail().isBlank()) {
            String email = donnees.getEmail().trim().toLowerCase();
            if (!email.equals(utilisateur.getEmail())) {
                if (utilisateurRepository.existsByEmail(email) || compteClientRepository.existsByEmail(email)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette adresse e-mail est déjà utilisée");
                }
                utilisateur.setEmail(email);
            }
        }
        if (donnees.getRole() != null) {
            utilisateur.setRole(trouverRoleInterne(donnees));
        }
        if (donnees.getStatutUtilisateur() != null) {
            utilisateur.setStatutUtilisateur(donnees.getStatutUtilisateur());
        }
        if (donnees.getMotDePasse() != null && !donnees.getMotDePasse().isBlank()) {
            verifierMotDePasse(donnees.getMotDePasse());
            utilisateur.setMotDePasse(passwordEncoder.encode(donnees.getMotDePasse()));
        }
        return utilisateur;
    }

    @Transactional
    public void supprimer(Long id, String emailCourant) {
        Utilisateur utilisateur = trouver(id);
        if (utilisateur.getEmail().equalsIgnoreCase(emailCourant)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous ne pouvez pas supprimer votre propre compte");
        }
        try {
            utilisateurRepository.delete(utilisateur);
            utilisateurRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Utilisateur référencé par d'autres données : passez son statut à INACTIF à la place");
        }
    }

    // ------------------------------------------------------------------ utilitaires

    /** Retrouve le rôle à partir de son libellé (AGENT ou ADMIN). */
    private Role trouverRoleInterne(Utilisateur donnees) {
        if (donnees.getRole() == null || donnees.getRole().getLibRole() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle obligatoire (AGENT ou ADMIN)");
        }
        String lib = donnees.getRole().getLibRole().trim().toUpperCase();
        if (!lib.equals("AGENT") && !lib.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le rôle doit être AGENT ou ADMIN");
        }
        return roleRepository.findByLibRole(lib)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle inconnu : " + lib));
    }

    private void verifierMotDePasse(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < 8 || motDePasse.length() > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le mot de passe doit contenir entre 8 et 72 caractères");
        }
    }
}
