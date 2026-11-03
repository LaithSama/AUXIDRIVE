package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;

@ExtendWith(MockitoExtension.class)
class DocumentClientServiceTest {

    @Mock CompteClientRepository comptes;
    @Mock DocumentJustificatifRepository documents;
    @Mock FichierService fichiers;
    @InjectMocks DocumentClientService service;

    private Assure assure() {
        Assure assure = new Assure();
        assure.setIdAssure(9L);
        assure.setMail("client@example.org");
        return assure;
    }

    private void connecte(Assure assure) {
        CompteClient compte = new CompteClient();
        compte.setAssure(assure);
        compte.setEmail("client@example.org");
        when(comptes.findByEmail("client@example.org")).thenReturn(Optional.of(compte));
    }

    private DocumentJustificatif piece(String type, String chemin, LocalDateTime date) {
        DocumentJustificatif piece = new DocumentJustificatif();
        piece.setIdDocument(7L);
        piece.setAssure(assure());
        piece.setTypeDocument(type);
        piece.setCheminFichier(chemin);
        piece.setFormatFichier("jpg");
        piece.setTailleKo(10);
        piece.setDateTeleversement(date);
        piece.setStatutDocuments(StatutDocument.VALIDE);
        return piece;
    }

    @Test void lAjoutDeduitLAssureDuCompteEtRepasseLaPieceEnAttente() {
        Assure assure = assure();
        connecte(assure);
        MockMultipartFile fichier = new MockMultipartFile("fichier", "cin.jpg", "image/jpeg", new byte[] { 1 });
        when(documents.existsByAssure_IdAssureAndTypeDocument(9L, "CIN")).thenReturn(false);
        when(fichiers.stocker(fichier)).thenReturn(new FichierService.Fichier("cin-nouveau.jpg", "jpg", 12));

        DocumentClientService.DocumentProfil resultat = service.ajouter("client@example.org", "cin", fichier);

        assertThat(resultat.typeDocument()).isEqualTo("CIN");
        assertThat(resultat.statutDocuments()).isEqualTo("EN_ATTENTE");
        assertThat(resultat.verrouille()).isFalse();
        verify(documents).saveAndFlush(argThat(d ->
                d.getAssure() == assure && "cin-nouveau.jpg".equals(d.getCheminFichier())
                        && d.getStatutDocuments() == StatutDocument.EN_ATTENTE));
    }

    @Test void unePieceNePeutPasEtreAjouteeDeuxFoisPourLeMemeType() {
        connecte(assure());
        when(documents.existsByAssure_IdAssureAndTypeDocument(9L, "CIN")).thenReturn(true);
        MockMultipartFile fichier = new MockMultipartFile("fichier", "cin.jpg", "image/jpeg", new byte[] { 1 });

        assertThatThrownBy(() -> service.ajouter("client@example.org", "CIN", fichier))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("existe déjà");
        verifyNoInteractions(fichiers);
    }

    @Test void leRemplacementEstRefusePendantUneInscriptionEnCours() {
        Assure assure = assure();
        connecte(assure);
        DocumentJustificatif piece = piece("CIN", "ancien.jpg", LocalDateTime.now().minusDays(1));
        Inscription dossier = new Inscription();
        dossier.setStatutInscription(StatutInscription.CORRECTION_DEMANDEE);
        piece.setInscription(dossier);
        when(documents.findByIdDocumentAndAssure_IdAssure(7L, 9L)).thenReturn(Optional.of(piece));
        MockMultipartFile fichier = new MockMultipartFile("fichier", "cin.jpg", "image/jpeg", new byte[] { 1 });

        assertThatThrownBy(() -> service.remplacer("client@example.org", 7L, fichier))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("cours d'instruction");
        verifyNoInteractions(fichiers);
    }

    @Test void unePieceDUneAutreAssureEstIntrouvable() {
        connecte(assure());
        when(documents.findByIdDocumentAndAssure_IdAssure(7L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.fichier("client@example.org", 7L))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("introuvable");
        verify(fichiers, never()).charger(any());
    }

    @Test void laListeNeRetientQueLaPieceLaPlusRecenteParType() {
        Assure assure = assure();
        connecte(assure);
        DocumentJustificatif ancienneCin = piece("CIN", "ancienne-cin.jpg", LocalDateTime.now().minusDays(10));
        DocumentJustificatif nouvelleCin = piece("CIN", "nouvelle-cin.jpg", LocalDateTime.now());
        DocumentJustificatif carteGrise = piece("CARTE_GRISE", "grise.jpg", LocalDateTime.now().minusDays(2));
        when(documents.findByAssure_IdAssureOrderByDateTeleversementDescIdDocumentDesc(9L))
                .thenReturn(List.of(nouvelleCin, carteGrise, ancienneCin));

        List<DocumentClientService.DocumentProfil> resultat = service.lister("client@example.org");

        assertThat(resultat).extracting(DocumentClientService.DocumentProfil::typeDocument)
                .containsExactly("CIN", "CARTE_GRISE");
        assertThat(resultat.get(0).statutDocuments()).isEqualTo("VALIDE");
    }
}
