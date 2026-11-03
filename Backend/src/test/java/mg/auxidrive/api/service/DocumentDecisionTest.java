package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import mg.auxidrive.api.model.*;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class DocumentDecisionTest {
    @Mock DocumentJustificatifRepository documents;
    @Mock UtilisateurRepository utilisateurs;
    @Mock FichierService fichiers;
    @Mock InscriptionService inscriptions;
    @InjectMocks DocumentService service;

    private DocumentJustificatif piece() {
        Inscription dossier = new Inscription();
        dossier.setStatutInscription(StatutInscription.EN_ATTENTE);
        DocumentJustificatif piece = new DocumentJustificatif();
        piece.setInscription(dossier);
        piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
        return piece;
    }

    @Test void rejetDemandeUneCorrectionPlutotQueRefuserLeDossier() {
        DocumentJustificatif piece = piece();
        when(documents.findById(7L)).thenReturn(Optional.of(piece));
        when(utilisateurs.findByEmail("agent@example.org")).thenReturn(Optional.of(new Utilisateur()));
        service.rejeter(7L, "Photo illisible", "agent@example.org");
        verify(inscriptions).demanderCorrection(piece, "Photo illisible");
        assertThat(piece.getStatutDocuments()).isEqualTo(StatutDocument.REJETE);
        assertThat(piece.getMotifRejet()).isEqualTo("Photo illisible");
    }

    @Test void suppressionInterditePendantUneDemandeDeCorrection() {
        DocumentJustificatif piece = piece();
        piece.getInscription().setStatutInscription(StatutInscription.CORRECTION_DEMANDEE);
        when(documents.findById(7L)).thenReturn(Optional.of(piece));
        assertThatThrownBy(() -> service.supprimer(7L)).isInstanceOf(ResponseStatusException.class);
        verify(documents, never()).delete(piece);
    }

    @Test void unePieceDeProfilEstValidableSansDossier() {
        DocumentJustificatif piece = new DocumentJustificatif();
        piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
        when(documents.findById(8L)).thenReturn(Optional.of(piece));
        when(utilisateurs.findByEmail("agent@example.org")).thenReturn(Optional.of(new Utilisateur()));

        service.valider(8L, "agent@example.org");

        assertThat(piece.getStatutDocuments()).isEqualTo(StatutDocument.VALIDE);
        assertThat(piece.getDateValidation()).isNotNull();
        verifyNoInteractions(inscriptions);
    }

    @Test void unRejetDeProfilNeCreePasDeJetonDeCorrection() {
        DocumentJustificatif piece = new DocumentJustificatif();
        piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
        piece.setTypeDocument("CARTE_GRISE");
        when(documents.findById(8L)).thenReturn(Optional.of(piece));
        when(utilisateurs.findByEmail("agent@example.org")).thenReturn(Optional.of(new Utilisateur()));

        service.rejeter(8L, "Photo floue", "agent@example.org");

        assertThat(piece.getStatutDocuments()).isEqualTo(StatutDocument.REJETE);
        assertThat(piece.getMotifRejet()).isEqualTo("Photo floue");
        verifyNoInteractions(inscriptions);
    }

    @Test void unePieceRemplaceeApresValidationDuDossierResteExaminable() {
        DocumentJustificatif piece = piece();
        piece.getInscription().setStatutInscription(StatutInscription.VALIDEE);
        when(documents.findById(9L)).thenReturn(Optional.of(piece));
        when(utilisateurs.findByEmail("agent@example.org")).thenReturn(Optional.of(new Utilisateur()));

        service.valider(9L, "agent@example.org");

        assertThat(piece.getStatutDocuments()).isEqualTo(StatutDocument.VALIDE);
        verifyNoInteractions(inscriptions);
    }

    @Test void uneCorrectionEnCoursResteTraiteeParLeParcoursInscription() {
        DocumentJustificatif piece = piece();
        piece.getInscription().setStatutInscription(StatutInscription.CORRECTION_DEMANDEE);
        when(documents.findById(9L)).thenReturn(Optional.of(piece));

        assertThatThrownBy(() -> service.valider(9L, "agent@example.org"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("non examinable");
    }

    @Test void laFileNeRetientQueLesPiecesHorsDossierEnCours() {
        Assure assure = new Assure();
        assure.setNomAssure("Ranaivo");
        assure.setPrenomAssure("Hery");
        DocumentJustificatif piece = new DocumentJustificatif();
        piece.setAssure(assure);
        piece.setTypeDocument("CIN");
        piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
        when(documents.findAVerifier(eq(StatutDocument.EN_ATTENTE),
                eq(java.util.List.of(StatutInscription.EN_ATTENTE, StatutInscription.CORRECTION_DEMANDEE))))
                .thenReturn(java.util.List.of(piece));

        var file = service.aVerifier();

        assertThat(file).hasSize(1);
        assertThat(file.get(0).typeDocument()).isEqualTo("CIN");
        assertThat(file.get(0).origine()).isEqualTo("PROFIL");
        assertThat(file.get(0).nomAssure()).isEqualTo("Ranaivo");
    }
}
