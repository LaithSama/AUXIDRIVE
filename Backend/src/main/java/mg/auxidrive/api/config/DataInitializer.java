package mg.auxidrive.api.config;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutUtilisateur;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.RoleRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/** Au démarrage : crée les rôles CLIENT, AGENT, ADMIN et le premier administrateur s'ils n'existent pas. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(RoleRepository roleRepository,
                           UtilisateurRepository utilisateurRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email:}") String adminEmail,
                           @Value("${app.admin.password:}") String adminPassword) {
        this.roleRepository = roleRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        creerRole("CLIENT", "Assuré connecté au portail client");
        creerRole("AGENT", "Agent de production automobile");
        Role admin = creerRole("ADMIN", "Administrateur");

        if (adminEmail.isEmpty() || adminPassword.isEmpty()) {
            log.warn("app.admin.email / app.admin.password non définis : aucun administrateur créé");
            return;
        }
        if (!utilisateurRepository.existsByEmail(adminEmail)) {
            Utilisateur u = new Utilisateur();
            u.setEmail(adminEmail);
            u.setNomUtil("Administrateur");
            u.setMotDePasse(passwordEncoder.encode(adminPassword));
            u.setRole(admin);
            u.setStatutUtilisateur(StatutUtilisateur.ACTIF);
            u.setDateCreation(LocalDateTime.now());
            utilisateurRepository.save(u);
            log.info("Administrateur initial créé : {}", adminEmail);
        }
    }

    private Role creerRole(String libelle, String description) {
        return roleRepository.findByLibRole(libelle).orElseGet(() -> {
            Role r = new Role();
            r.setLibRole(libelle);
            r.setDescription(description);
            return roleRepository.save(r);
        });
    }
}
