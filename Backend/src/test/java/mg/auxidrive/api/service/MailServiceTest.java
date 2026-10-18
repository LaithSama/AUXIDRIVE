package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import mg.auxidrive.api.service.MailService.DonneesIdentifiants;
import mg.auxidrive.api.service.MailService.OrigineCompte;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    private static final String URL = "https://portail.auxidrive.mg/connexion";
    private static final String RELAIS = "admin@auximad.mg";
    private static final String CLIENT = "dupont@mail.mg";
    private static final String MDP = "kR7mP2xWq9";

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;
    @Mock
    private JavaMailSender mailSender;

    private MailService service;

    @BeforeEach
    void setUp() {
        // lenient : le scénario « mail désactivé » n'appelle jamais getIfAvailable()
        lenient().when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        service = new MailService(mailSenderProvider, true, "no-reply@auxidrive.mg", URL, RELAIS);
    }

    private static DonneesIdentifiants donnees(OrigineCompte origine) {
        return new DonneesIdentifiants(CLIENT, MDP, "Madame", "Dupont", "Marie", "12345678", origine);
    }

    /** Dernier message capturé parmi les n envois attendus. */
    private SimpleMailMessage envoye(int nombreAttendu) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(nombreAttendu)).send(captor.capture());
        return captor.getAllValues().get(nombreAttendu - 1);
    }

    private static String corps(SimpleMailMessage m) {
        return m.getText();
    }

    private static String destinataire(SimpleMailMessage m) {
        return Arrays.toString(m.getTo());
    }

    // ------------------------------------------------------------------ contenu

    @Test
    @DisplayName("Inscription validée : objet dédié, identité, lien et n° d'assuré sont présents")
    void inscriptionValidee() {
        assertThat(service.envoyerIdentifiants(donnees(OrigineCompte.INSCRIPTION_VALIDEE))).isTrue();

        SimpleMailMessage m = envoye(1);
        assertThat(m.getSubject()).isEqualTo("AUXIDRIVE - Votre inscription est validée : vos identifiants");
        assertThat(destinataire(m)).contains(CLIENT);
        assertThat(corps(m))
                // l'ordre « civilité nom prénom » est celui de l'helper identite() partagé
                // avec les mails d'échéance et de renouvellement
                .contains("Bonjour Madame Dupont Marie,")
                .contains("Votre inscription sur AUXIDRIVE a été validée")
                .contains(CLIENT)
                .contains(MDP)
                .contains("12345678")
                .contains(URL)
                .contains("8 caractères minimum")
                .contains("ne le communiquez à personne")
                .endsWith("Cordialement,\nL'équipe AUXIDRIVE");
    }

    @Test
    @DisplayName("Création par l'administrateur : objet et accroche propres à l'origine")
    void creationAdmin() {
        assertThat(service.envoyerIdentifiants(donnees(OrigineCompte.COMPTE_CREE))).isTrue();

        SimpleMailMessage m = envoye(1);
        assertThat(m.getSubject()).isEqualTo("AUXIDRIVE - Votre compte client a été créé : vos identifiants");
        assertThat(corps(m))
                .contains("Votre compte client AUXIDRIVE a été créé à votre demande.")
                .doesNotContain("inscription");
    }

    @Test
    @DisplayName("Sans n° d'assuré, la ligne est omise plutôt que laissée vide")
    void numeroAssureAbsent() {
        DonneesIdentifiants d = new DonneesIdentifiants(CLIENT, MDP, "Monsieur", "Dupont", null, "  ", OrigineCompte.COMPTE_CREE);

        service.envoyerIdentifiants(d);

        assertThat(corps(envoye(1))).doesNotContain("N° d'assuré");
    }

    @Test
    @DisplayName("Sans identité, la salutation dégénère en « Bonjour, »")
    void identiteAbsente() {
        DonneesIdentifiants d = new DonneesIdentifiants(CLIENT, MDP, null, null, null, null, OrigineCompte.COMPTE_CREE);

        service.envoyerIdentifiants(d);

        assertThat(corps(envoye(1))).startsWith("Bonjour,\n\n");
    }

    // ------------------------------------------------------------------ échecs et relais

    @Test
    @DisplayName("Échec SMTP : retourne false et relaye les identifiants à l'administrateur")
    void echecRelaisVersAdministrateur() {
        doThrow(new MailSendException("SMTP en panne"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(service.envoyerIdentifiants(donnees(OrigineCompte.INSCRIPTION_VALIDEE))).isFalse();

        SimpleMailMessage relais = envoye(2);
        assertThat(destinataire(relais)).contains(RELAIS);
        assertThat(relais.getSubject()).isEqualTo("AUXIDRIVE - Identifiants non transmis à " + CLIENT);
        assertThat(corps(relais))
                .contains("a échoué")
                .contains(CLIENT)
                .contains(MDP)
                .contains("12345678")
                .contains("inscription validée");
    }

    @Test
    @DisplayName("Relais en échec aussi : aucun troisième envoi, retour false")
    void relaisEgalementImpossible() {
        doThrow(new MailSendException("SMTP en panne"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertThat(service.envoyerIdentifiants(donnees(OrigineCompte.COMPTE_CREE))).isFalse();

        assertThat(corps(envoye(2))).contains("création par l'administrateur");
    }

    @Test
    @DisplayName("Destinataire vide : rien pour le client, retour false et les identifiants partent en relais")
    void destinataireAbsent() {
        DonneesIdentifiants d = new DonneesIdentifiants(" ", MDP, null, null, null, null, OrigineCompte.COMPTE_CREE);

        assertThat(service.envoyerIdentifiants(d)).isFalse();

        assertThat(destinataire(envoye(1))).isEqualTo("[" + RELAIS + "]");
    }

    // ------------------------------------------------------------------ développement

    @Test
    @DisplayName("Mail désactivé : pas d'envoi réel mais retour true, le contenu étant journalisé")
    void mailDesactive() {
        MailService dev = new MailService(mailSenderProvider, false, "", URL, RELAIS);

        assertThat(dev.envoyerIdentifiants(donnees(OrigineCompte.COMPTE_CREE))).isTrue();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Expéditeur absent : le message part sans en-tête From plutôt qu'en échec")
    void expediteurAbsent() {
        MailService sansExpediteur = new MailService(mailSenderProvider, true, "", URL, RELAIS);

        assertThat(sansExpediteur.envoyerIdentifiants(donnees(OrigineCompte.COMPTE_CREE))).isTrue();

        assertThat(envoye(1).getFrom()).isNull();
    }
}
