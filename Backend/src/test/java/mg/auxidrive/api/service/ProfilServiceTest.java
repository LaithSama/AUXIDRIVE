package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.repository.CompteClientRepository;

@ExtendWith(MockitoExtension.class)
class ProfilServiceTest {

    private static final String EMAIL = "client@test.mg";

    @Mock
    private CompteClientRepository compteClientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private ProfilService service;
    private CompteClient compte;
    private Assure assure;

    @BeforeEach
    void setUp() {
        service = new ProfilService(compteClientRepository, passwordEncoder);

        assure = new Assure();
        assure.setIdAssure(7L);
        assure.setNomAssure("Rakoto");
        assure.setPrenomAssure("Jean");
        assure.setMail("jean@mail.test");
        assure.setContact("0320524221");

        compte = new CompteClient();
        compte.setIdCompteClient(3L);
        compte.setEmail(EMAIL);
        compte.setMotDePasse("$2a$10$ancien");
        compte.setAssure(assure);

        // lenient : le test « compte introuvable » n'utilise pas ce stub
        lenient().when(compteClientRepository.findByEmail(EMAIL)).thenReturn(Optional.of(compte));
    }

    private Map<String, Object> corpsValide() {
        Map<String, Object> corps = new HashMap<>();
        corps.put("nomAssure", "Rakoto");
        corps.put("prenomAssure", "Jean");
        corps.put("adresse", "Ankorondrano");
        corps.put("contact", "0320524221");
        corps.put("mail", "jean@mail.test");
        corps.put("emailConnexion", EMAIL);
        return corps;
    }

    @Test
    @DisplayName("profil : retourne le compte et les données de l'assuré")
    void profilRetourneCompteEtAssure() {
        Map<String, Object> reponse = service.profil(EMAIL);

        @SuppressWarnings("unchecked")
        Map<String, Object> compteReponse = (Map<String, Object>) reponse.get("compte");
        @SuppressWarnings("unchecked")
        Map<String, Object> assureReponse = (Map<String, Object>) reponse.get("assure");

        assertThat(compteReponse.get("email")).isEqualTo(EMAIL);
        assertThat(compteReponse.get("dateSuppressionPlanifiee")).isNull();
        assertThat(assureReponse.get("nomAssure")).isEqualTo("Rakoto");
        assertThat(assureReponse.get("numerosAssure")).isNull();
    }

    @Test
    @DisplayName("enregistrer : met à jour l'assuré et retourne la fiche à jour")
    void enregistrerMetAJourLesChamps() {
        Map<String, Object> reponse = service.enregistrer(EMAIL, corpsValide());

        assertThat(assure.getAdresse()).isEqualTo("Ankorondrano");
        assertThat(assure.getMail()).isEqualTo("jean@mail.test");
        assertThat(compte.getEmail()).isEqualTo(EMAIL);

        @SuppressWarnings("unchecked")
        Map<String, Object> assureReponse = (Map<String, Object>) reponse.get("assure");
        assertThat(assureReponse.get("adresse")).isEqualTo("Ankorondrano");
    }

    @Test
    @DisplayName("enregistrer : e-mail de connexion déjà utilisé → 409")
    void enregistrerRefuseEmailDupe() {
        when(compteClientRepository.existsByEmail("autre@test.mg")).thenReturn(true);
        Map<String, Object> corps = corpsValide();
        corps.put("emailConnexion", "autre@test.mg");

        assertThatThrownBy(() -> service.enregistrer(EMAIL, corps))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @DisplayName("enregistrer : nom vide → 400")
    void enregistrerRefuseNomVide() {
        Map<String, Object> corps = corpsValide();
        corps.put("nomAssure", "  ");

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

        assertThat(compte.getMotDePasse()).isEqualTo("$2a$10$nouveau");
    }

    @Test
    @DisplayName("planifierSuppression : pose la date à maintenant + 72 h")
    void planifieSuppression72Heures() {
        LocalDateTime avant = LocalDateTime.now().plusHours(ProfilService.DELAI_SUPPRESSION_HEURES);

        Map<String, Object> reponse = service.planifierSuppression(EMAIL);

        assertThat(compte.getDateSuppressionPlanifiee()).isNotNull();
        assertThat(compte.getDateSuppressionPlanifiee()).isBetween(avant, avant.plusMinutes(1));
        assertThat(reponse.get("dateSuppressionPlanifiee")).isNotNull();
    }

    @Test
    @DisplayName("annulerSuppression : efface la date programmée")
    void annuleSuppression() {
        compte.setDateSuppressionPlanifiee(LocalDateTime.now().plusHours(24));

        Map<String, Object> reponse = service.annulerSuppression(EMAIL);

        assertThat(compte.getDateSuppressionPlanifiee()).isNull();
        assertThat(reponse.get("dateSuppressionPlanifiee")).isNull();
    }

    @Test
    @DisplayName("purgerEchues : délègue la purge au repository")
    void purgeDelegueeAuRepository() {
        when(compteClientRepository.purgeEchues(any(LocalDateTime.class))).thenReturn(2);

        assertThat(service.purgerEchues()).isEqualTo(2);

        verify(compteClientRepository).purgeEchues(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("profil : compte introuvable → 404")
    void profilCompteIntrouvable() {
        when(compteClientRepository.findByEmail("inconnu@test.mg")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.profil("inconnu@test.mg"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }
}
