package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.service.MailService.DonneesEcheance;
import mg.auxidrive.api.service.MailService.DonneesRenouvellement;

@ExtendWith(MockitoExtension.class)
class EcheanceRenouvellementServiceTest {

    @Mock
    private ContratRepository contratRepository;
    @Mock
    private ContratAutoRepository contratAutoRepository;
    @Mock
    private ContratService contratService;
    @Mock
    private MailService mailService;

    @InjectMocks
    private EcheanceRenouvellementService service;

    private Assure assureAvecMail(String mail) {
        Assure a = new Assure();
        a.setCivilite("M.");
        a.setNomAssure("Ranaivo");
        a.setPrenomAssure("Hery");
        a.setMail(mail);
        return a;
    }

    private Contrat contratValide(LocalDate effet, LocalDate expiration, LocalDate notif) {
        Contrat c = new Contrat();
        c.setIdContrat(1L);
        c.setNumPolice("2600000001");
        c.setAssure(assureAvecMail("hery@example.mg"));
        c.setStatutContrat(StatutContrat.VALIDE);
        c.setDateEffet(effet);
        c.setDateExpiration(expiration);
        c.setDateNotifEcheance(notif);
        c.setPrimeTtcCourante(149940d);
        return c;
    }

    // ---- rappel J-3 ----

    @Test
    @DisplayName("Un contrat expirant dans deux jours reçoit le rappel et la notification est marquée")
    void notificationEnvoiEtMarque() {
        LocalDate today = LocalDate.now();
        Contrat c = contratValide(today.minusMonths(12), today.plusDays(2), null);
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today, today.plusDays(3))).thenReturn(List.of(c));

        int envoyes = service.notifierEcheances();

        assertThat(envoyes).isEqualTo(1);
        ArgumentCaptor<DonneesEcheance> captor = ArgumentCaptor.forClass(DonneesEcheance.class);
        verify(mailService).envoyerEcheanceProche(captor.capture());
        assertThat(captor.getValue().destinataire()).isEqualTo("hery@example.mg");
        assertThat(captor.getValue().numPolice()).isEqualTo("2600000001");
        assertThat(captor.getValue().civilite()).isEqualTo("M.");
        assertThat(captor.getValue().dateExpiration()).isEqualTo(today.plusDays(2));
        assertThat(c.getDateNotifEcheance()).isEqualTo(today);
        verify(contratRepository).save(c);
    }

    @Test
    @DisplayName("Un contrat déjà rappelé pour cette échéance ne reçoit pas de second mail")
    void notificationIgnoreDejaNotifie() {
        LocalDate today = LocalDate.now();
        Contrat c = contratValide(today.minusMonths(12), today.plusDays(1), today.minusDays(2));
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today, today.plusDays(3))).thenReturn(List.of(c));

        int envoyes = service.notifierEcheances();

        assertThat(envoyes).isZero();
        verify(mailService, never()).envoyerEcheanceProche(any());
        verify(contratRepository, never()).save(any());
    }

    @Test
    @DisplayName("Assuré sans e-mail : le rappel n'est pas envoyé mais la notification est marquée")
    void notificationSansMailMarqueQuandMeme() {
        LocalDate today = LocalDate.now();
        Contrat c = contratValide(today.minusMonths(12), today.plusDays(2), null);
        c.getAssure().setMail(null);
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today, today.plusDays(3))).thenReturn(List.of(c));

        int envoyes = service.notifierEcheances();

        assertThat(envoyes).isZero();
        verify(mailService, never()).envoyerEcheanceProche(any());
        assertThat(c.getDateNotifEcheance()).isEqualTo(today);
        verify(contratRepository).save(c);
    }

    @Test
    @DisplayName("Aucun contrat échéant : rien n'est fait")
    void notificationAucunContrat() {
        LocalDate today = LocalDate.now();
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today, today.plusDays(3))).thenReturn(List.of());

        assertThat(service.notifierEcheances()).isZero();
        verify(mailService, never()).envoyerEcheanceProche(any());
    }

    // ---- reconduction tacite ----

    @Test
    @DisplayName("Un contrat échu est renouvelé et reçoit la confirmation")
    void renouvellementEtConfirmation() {
        LocalDate today = LocalDate.now();
        Contrat c = contratValide(today.minusMonths(12), today, today.minusDays(2));
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today.minusDays(7), today)).thenReturn(List.of(c));
        // le renouvellement réel prolonge les dates sur le même objet
        when(contratService.renouveler(c)).thenAnswer(invocation -> {
            c.setDateEffet(today);
            c.setDateExpiration(today.plusMonths(12));
            return c;
        });

        int renouvelles = service.renouvelerContratsEchus();

        assertThat(renouvelles).isEqualTo(1);
        verify(contratService).renouveler(c);
        ArgumentCaptor<DonneesRenouvellement> captor = ArgumentCaptor.forClass(DonneesRenouvellement.class);
        verify(mailService).envoyerRenouvellement(captor.capture());
        assertThat(captor.getValue().numPolice()).isEqualTo("2600000001");
        assertThat(captor.getValue().dateEffet()).isEqualTo(today);
        assertThat(captor.getValue().dateExpiration()).isEqualTo(today.plusMonths(12));
    }

    @Test
    @DisplayName("Un renouvellement qui échoue n'empêche pas les autres contrats")
    void unEchecNEmpechePasLesAutres() {
        LocalDate today = LocalDate.now();
        Contrat KO = contratValide(today.minusMonths(12), today, null);
        KO.setIdContrat(1L);
        KO.setNumPolice("2600000001");
        Contrat ok = contratValide(today.minusMonths(12), today.minusDays(3), null);
        ok.setIdContrat(2L);
        ok.setNumPolice("2600000002");
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today.minusDays(7), today)).thenReturn(List.of(KO, ok));
        when(contratService.renouveler(KO))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "paramètre de calcul manquant"));
        when(contratService.renouveler(ok)).thenReturn(ok);

        int renouvelles = service.renouvelerContratsEchus();

        assertThat(renouvelles).isEqualTo(1);
        verify(mailService).envoyerRenouvellement(any(DonneesRenouvellement.class));
    }

    @Test
    @DisplayName("Assuré renouvelé sans e-mail : pas de mail, mais le renouvellement compte")
    void renouvellementSansMail() {
        LocalDate today = LocalDate.now();
        Contrat c = contratValide(today.minusMonths(12), today, null);
        c.getAssure().setMail("");
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today.minusDays(7), today)).thenReturn(List.of(c));
        when(contratService.renouveler(c)).thenReturn(c);

        int renouvelles = service.renouvelerContratsEchus();

        assertThat(renouvelles).isEqualTo(1);
        verify(mailService, never()).envoyerRenouvellement(any());
    }

    @Test
    @DisplayName("Aucun contrat échu : rien n'est renouvelé")
    void renouvellementAucunContrat() {
        LocalDate today = LocalDate.now();
        when(contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today.minusDays(7), today)).thenReturn(List.of());

        assertThat(service.renouvelerContratsEchus()).isZero();
        verify(contratService, never()).renouveler(any());
    }
}
