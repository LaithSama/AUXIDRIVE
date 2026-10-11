package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutUtilisateur;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class ProfilStaffServiceTest {

    private static final String EMAIL = "agent@test.mg";

    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private CompteClientRepository compteClientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ProfilStaffService service;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        service = new ProfilStaffService(utilisateurRepository, compteClientRepository, passwordEncoder);

        Role role = new Role();
        role.setIdRole(2L);
        role.setLibRole("AGENT");

        utilisateur = new Utilisateur();
        utilisateur.setIdUtil(5L);
        utilisateur.setEmail(EMAIL);
        utilisateur.setNomUtil("Rasoa");
        utilisateur.setMotDePasse("$2a$10$ancien");
        utilisateur.setRole(role);
        utilisateur.setStatutUtilisateur(StatutUtilisateur.ACTIF);

        // lenient : le test « utilisateur introuvable » n'utilise pas ce stub
        lenient().when(utilisateurRepository.findByEmail(EMAIL)).thenReturn(Optional.of(utilisateur));
    }

    private Map<String, Object> corpsValide() {
        Map<String, Object> corps = new HashMap<>();
        corps.put("nomUtil", "Rasoa");
        corps.put("emailConnexion", EMAIL);
        return corps;
    }

    @Test
    @DisplayName("profil : retourne le nom, l'e-mail, le rôle et le statut")
    void profilRetourneLaFiche() {
        Map<String, Object> reponse = service.profil(EMAIL);

        assertThat(reponse.get("email")).isEqualTo(EMAIL);
        assertThat(reponse.get("nomUtil")).isEqualTo("Rasoa");
        assertThat(reponse.get("role")).isEqualTo("AGENT");
        assertThat(reponse.get("statutUtilisateur")).isEqualTo("ACTIF");
    }

    @Test
    @DisplayName("enregistrer : met à jour le nom et retourne la fiche à jour")
    void enregistrerMetAJourLeNom() {
        Map<String, Object> corps = corpsValide();
        corps.put("nomUtil", "Rakoto");

        Map<String, Object> reponse = service.enregistrer(EMAIL, corps);

        assertThat(utilisateur.getNomUtil()).isEqualTo("Rakoto");
        assertThat(reponse.get("nomUtil")).isEqualTo("Rakoto");
    }

    @Test
    @DisplayName("enregistrer : nouvel e-mail de connexion conservé en minuscules")
    void enregistrerNormaliseEmail() {
        Map<String, Object> corps = corpsValide();
        corps.put("emailConnexion", "Nouvel@Mail.MG");

        service.enregistrer(EMAIL, corps);

        assertThat(utilisateur.getEmail()).isEqualTo("nouvel@mail.mg");
    }

    @Test
    @DisplayName("enregistrer : e-mail déjà utilisé par un utilisateur → 409")
    void enregistrerRefuseEmailDupeUtilisateur() {
        when(utilisateurRepository.existsByEmail("autre@test.mg")).thenReturn(true);
        Map<String, Object> corps = corpsValide();
        corps.put("emailConnexion", "autre@test.mg");

        assertThatThrownBy(() -> service.enregistrer(EMAIL, corps))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("enregistrer : e-mail déjà utilisé par un client → 409")
    void enregistrerRefuseEmailDupeClient() {
        when(compteClientRepository.existsByEmail("client@test.mg")).thenReturn(true);
        Map<String, Object> corps = corpsValide();
        corps.put("emailConnexion", "client@test.mg");

        assertThatThrownBy(() -> service.enregistrer(EMAIL, corps))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("enregistrer : nom vide → 400")
    void enregistrerRefuseNomVide() {
        Map<String, Object> corps = corpsValide();
        corps.put("nomUtil", "  ");

        assertThatThrownBy(() -> service.enregistrer(EMAIL, corps))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("enregistrer : e-mail invalide → 400")
    void enregistrerRefuseEmailInvalide() {
        Map<String, Object> corps = corpsValide();
        corps.put("emailConnexion", "pas-un-email");

        assertThatThrownBy(() -> service.enregistrer(EMAIL, corps))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("changerMotDePasse : ancien mot de passe incorrect → 400")
    void motDePasseAncienIncorrect() {
        when(passwordEncoder.matches("faux", "$2a$10$ancien")).thenReturn(false);

        assertThatThrownBy(() -> service.changerMotDePasse(EMAIL, "faux", "nouveaumdp"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("changerMotDePasse : nouveau mot de passe trop court → 400")
    void motDePasseTropCourt() {
        assertThatThrownBy(() -> service.changerMotDePasse(EMAIL, "ancien", "court"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    @DisplayName("changerMotDePasse : ancien vérifié puis nouveau encodé")
    void motDePasseChange() {
        when(passwordEncoder.matches("bon", "$2a$10$ancien")).thenReturn(true);
        when(passwordEncoder.encode("nouveaumdp")).thenReturn("$2a$10$nouveau");

        service.changerMotDePasse(EMAIL, "bon", "nouveaumdp");

        assertThat(utilisateur.getMotDePasse()).isEqualTo("$2a$10$nouveau");
    }

    @Test
    @DisplayName("profil : utilisateur introuvable → 404")
    void profilUtilisateurIntrouvable() {
        when(utilisateurRepository.findByEmail("inconnu@test.mg")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profil("inconnu@test.mg"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
