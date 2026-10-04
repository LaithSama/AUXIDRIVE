package mg.auxidrive.api.service;

import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.model.StatutUtilisateur;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository,
                                    CompteClientRepository compteClientRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur.isPresent()) {
            Utilisateur u = utilisateur.get();
            if (u.getRole() == null) {
                throw new UsernameNotFoundException("Rôle manquant pour " + email);
            }
            return User.withUsername(u.getEmail())
                    .password(u.getMotDePasse() == null ? "" : u.getMotDePasse())
                    .authorities("ROLE_" + u.getRole().getLibRole().toUpperCase())
                    .disabled(u.getStatutUtilisateur() != StatutUtilisateur.ACTIF)
                    .build();
        }

        CompteClient c = compteClientRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + email));
        if (c.getRole() == null) {
            throw new UsernameNotFoundException("Rôle manquant pour " + email);
        }
        return User.withUsername(c.getEmail())
                .password(c.getMotDePasse() == null ? "" : c.getMotDePasse())
                .authorities("ROLE_" + c.getRole().getLibRole().toUpperCase())
                .disabled(c.getStatutCompte() != StatutCompte.ACTIF)
                .build();
    }
}
