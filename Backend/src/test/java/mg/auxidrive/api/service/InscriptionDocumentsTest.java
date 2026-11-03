package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.*;
import mg.auxidrive.api.repository.*;

@ExtendWith(MockitoExtension.class)
class InscriptionDocumentsTest {
    @Mock InscriptionRepository inscriptions;
    @Mock UtilisateurRepository utilisateurs;
    @Mock CompteClientRepository comptes;
    @Mock AssureRepository assures;
    @Mock AssureProfessionRepository professions;
    @Mock AssureIdentifiantsLegauxRepository legaux;
    @Mock DocumentJustificatifRepository documents;
    @Mock RoleRepository roles;
    @Mock FichierService fichiers;
    @Mock MailService mails;
    @Mock org.springframework.security.crypto.password.PasswordEncoder encoder;
    @InjectMocks InscriptionService service;

    private Inscription dossier() {
        Assure assure = new Assure();
        assure.setIdAssure(9L);
        assure.setMail("client@example.org");
        Inscription inscription = new Inscription();
        inscription.setIdInscription(42L);
        inscription.setAssure(assure);
        inscription.setStatutInscription(StatutInscription.EN_ATTENTE);
        return inscription;
    }

    @Test void nifSeulNeRemplacePasLeCinPourOuvrirUnCompte() {
        Assure assure = new Assure();
        assure.setNomAssure("Entreprise");
        assure.setPersonneMorale(true);
        assure.setMail("client@example.org");
        assure.setContact("0123456789");
        assure.setAdresse("Antananarivo");
        AssureIdentifiantsLegaux identifiants = new AssureIdentifiantsLegaux();
        identifiants.setNif("123456");

        assertThatThrownBy(() -> service.soumettre(assure, null, identifiants, Map.of()))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("numéro de CIN ou importez sa photo");
        verifyNoInteractions(fichiers);
    }

    @Test void unePieceEnAttenteBloqueLaCreationDuCompte() {
        Inscription inscription = dossier();
        DocumentJustificatif document = new DocumentJustificatif();
        document.setStatutDocuments(StatutDocument.EN_ATTENTE);
        when(inscriptions.findById(42L)).thenReturn(Optional.of(inscription));
        when(documents.findByInscription_IdInscription(42L)).thenReturn(List.of(document));

        assertThatThrownBy(() -> service.valider(42L, "123", "123456", "agent@example.org"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("Examinez toutes les pièces");
        verifyNoInteractions(comptes);
    }

    @Test void rejetDeclencheUneCorrectionAvecJetonNonExpose() {
        Inscription inscription = dossier();
        DocumentJustificatif document = new DocumentJustificatif();
        document.setInscription(inscription);
        document.setTypeDocument("CIN");
        when(mails.envoyerDemandeCorrection(any(), any(), any(), any(), any())).thenReturn(true);

        service.demanderCorrection(document, "Illisible");

        assertThat(inscription.getStatutInscription()).isEqualTo(StatutInscription.CORRECTION_DEMANDEE);
        assertThat(inscription.getJetonCorrectionHash()).hasSize(64);
        assertThat(inscription.getExpirationCorrection()).isAfter(LocalDateTime.now());
        verify(mails).envoyerDemandeCorrection(eq("client@example.org"), eq(42L), eq("CIN"), eq("Illisible"), any());
    }

    @Test void remplacementRetablitLExamenEtInvalideLeLien() {
        Inscription inscription = dossier();
        inscription.setStatutInscription(StatutInscription.CORRECTION_DEMANDEE);
        inscription.setExpirationCorrection(LocalDateTime.now().plusDays(1));
        DocumentJustificatif document = new DocumentJustificatif();
        document.setTypeDocument("CIN");
        document.setCheminFichier("ancien.jpg");
        document.setStatutDocuments(StatutDocument.REJETE);
        document.setMotifRejet("Illisible");
        when(inscriptions.findByJetonCorrectionHash(any())).thenReturn(Optional.of(inscription));
        when(documents.findByInscription_IdInscription(42L)).thenReturn(List.of(document));
        MockMultipartFile nouveau = new MockMultipartFile("cin", "cin.jpg", "image/jpeg", new byte[] {1});
        when(fichiers.stocker(nouveau)).thenReturn(new FichierService.Fichier("nouveau.jpg", "jpg", 1));
        String jeton = "a".repeat(64);

        service.corriger(jeton, Map.of("CIN", nouveau));

        assertThat(document.getCheminFichier()).isEqualTo("nouveau.jpg");
        assertThat(document.getStatutDocuments()).isEqualTo(StatutDocument.EN_ATTENTE);
        assertThat(document.getMotifRejet()).isNull();
        assertThat(inscription.getJetonCorrectionHash()).isNull();
        assertThat(inscription.getStatutInscription()).isEqualTo(StatutInscription.EN_ATTENTE);
        verify(fichiers).supprimer("ancien.jpg");
    }
}
