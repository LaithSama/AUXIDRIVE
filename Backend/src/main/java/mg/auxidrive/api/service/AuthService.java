package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UtilisateurRepository utilisateurRepository,
                       CompteClientRepository compteClientRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
    }

    /**
     * Retourne le token et le rôle. Lève une AuthenticationException si les identifiants sont faux
     * et une DisabledException si le compte n'est pas actif.
     */
    @Transactional
    public Map<String, String> login(String email, String motDePasse) {
        if (email == null || email.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail et mot de passe obligatoires");
        }
        String mail = email.trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(mail, motDePasse));
        UserDetails user = (UserDetails) auth.getPrincipal();
        String role = user.getAuthorities().iterator().next().getAuthority();

        LocalDateTime maintenant = LocalDateTime.now();
        utilisateurRepository.findByEmail(mail).ifPresentOrElse(
                u -> u.setDerniereConnexion(maintenant),
                () -> compteClientRepository.findByEmail(mail)
                        .ifPresent(c -> c.setDateDerniereConnexion(maintenant)));
        return Map.of("token", jwtService.generateToken(user), "role", role);
    }
}
