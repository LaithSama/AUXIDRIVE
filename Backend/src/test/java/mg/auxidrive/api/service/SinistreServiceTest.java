package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.Sinistre;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.StatutSinistre;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.repository.SinistreRepository;
import mg.auxidrive.api.repository.VehiculeRepository;

/**
 * Déclaration d'un sinistre par l'assuré.
 *
 * Le bug corrigé : des circonstances ou un lieu trop longs dépassaient les colonnes et
 * faisaient échouer l'enregistrement par une erreur 500, que le portail traduisait à tort
 * par « contrat non validé ». La saisie est désormais bornée et contrôlée (400 explicite).
 */
@ExtendWith(MockitoExtension.class)
class SinistreServiceTest {

    @Mock
    private SinistreRepository sinistreRepository;
    @Mock
    private ContratRepository contratRepository;
    @Mock
    private ContratAutoRepository contratAutoRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;

    private SinistreService service;

    @BeforeEach
    void setUp() {
        service = new SinistreService(sinistreRepository, contratRepository, contratAutoRepository,
                vehiculeRepository);
    }

    private Contrat contratValide(Long idContrat, Long idAssure) {
        Assure assure = new Assure();
        assure.setIdAssure(idAssure);

        Contrat contrat = new Contrat();
        contrat.setIdContrat(idContrat);
        contrat.setStatutContrat(StatutContrat.VALIDE);
        contrat.setAssure(assure);
        return contrat;
    }

    private void stubSave() {
        when(sinistreRepository.save(any(Sinistre.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Un sinistre est déclaré sur un contrat validé de l'assuré")
    void declarerEnregistreUnSinistreSurContratValide() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));
        stubSave();

        Sinistre sinistre = service.declarer(1L, null, LocalDate.now().minusDays(1),
                "Antananarivo", "Collision légère", 5L);

        assertThat(sinistre.getStatutSinistre()).isEqualTo(StatutSinistre.DECLARE);
        assertThat(sinistre.getDateDeclaration()).isEqualTo(LocalDate.now());
        verify(sinistreRepository).save(any(Sinistre.class));
    }

    @Test
    @DisplayName("Des circonstances longues mais dans la limite sont acceptées")
    void declarerAccepteDesCirconstancesLongues() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));
        stubSave();

        String circonstances = "a".repeat(SinistreService.LONGUEUR_MAX_CIRCONSTANCES);

        Sinistre sinistre = service.declarer(1L, null, LocalDate.now(), null, circonstances, 5L);

        assertThat(sinistre.getCirconstances()).hasSize(SinistreService.LONGUEUR_MAX_CIRCONSTANCES);
        verify(sinistreRepository).save(any(Sinistre.class));
    }

    @Test
    @DisplayName("Des circonstances hors limite sont refusées par un 400, pas une erreur 500")
    void declarerRefuseDesCirconstancesTropLongues() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));

        String circonstances = "a".repeat(SinistreService.LONGUEUR_MAX_CIRCONSTANCES + 1);

        assertThatThrownBy(() -> service.declarer(1L, null, LocalDate.now(), null, circonstances, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("circonstances");
        verifyNoInteractions(sinistreRepository);
    }

    @Test
    @DisplayName("Un lieu hors limite est refusé par un 400, pas une erreur 500")
    void declarerRefuseUnLieuTropLong() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));

        String lieu = "a".repeat(SinistreService.LONGUEUR_MAX_LIEU + 1);

        assertThatThrownBy(() -> service.declarer(1L, null, LocalDate.now(), lieu, null, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("lieu");
        verifyNoInteractions(sinistreRepository);
    }

    @Test
    @DisplayName("Un sinistre ne peut pas être déclaré sur un contrat non validé")
    void declarerRefuseUnContratNonValide() {
        Contrat contrat = contratValide(1L, 5L);
        contrat.setStatutContrat(StatutContrat.EN_ATTENTE);
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contrat));

        assertThatThrownBy(() -> service.declarer(1L, null, LocalDate.now(), null, null, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("contrat validé");
        verifyNoInteractions(sinistreRepository);
    }

    @Test
    @DisplayName("La date du sinistre ne peut pas être dans le futur")
    void declarerRefuseUneDateFuture() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));

        assertThatThrownBy(() -> service.declarer(1L, null, LocalDate.now().plusDays(1), null, null, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("futur");
        verifyNoInteractions(sinistreRepository);
    }

    @Test
    @DisplayName("Un véhicule non couvert par le contrat est refusé")
    void declarerRefuseUnVehiculeNonCouvert() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));

        Vehicule autre = new Vehicule();
        autre.setIdVehicule(99L);
        ContratAuto lien = new ContratAuto();
        lien.setVehicule(autre);
        when(contratAutoRepository.findByContrat_IdContrat(1L)).thenReturn(List.of(lien));

        assertThatThrownBy(() -> service.declarer(1L, 42L, LocalDate.now(), null, null, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("couvert");
        verifyNoInteractions(sinistreRepository);
    }

    @Test
    @DisplayName("Un véhicule couvert par le contrat est rattaché au sinistre")
    void declarerRattacheLeVehiculeCouvert() {
        when(contratRepository.findById(1L)).thenReturn(Optional.of(contratValide(1L, 5L)));
        stubSave();

        Vehicule vehicule = new Vehicule();
        vehicule.setIdVehicule(42L);
        ContratAuto lien = new ContratAuto();
        lien.setVehicule(vehicule);
        when(contratAutoRepository.findByContrat_IdContrat(1L)).thenReturn(List.of(lien));
        when(vehiculeRepository.findById(42L)).thenReturn(Optional.of(vehicule));

        Sinistre sinistre = service.declarer(1L, 42L, LocalDate.now(), null, null, 5L);

        assertThat(sinistre.getVehicule()).isEqualTo(vehicule);
        verify(sinistreRepository).save(any(Sinistre.class));
    }
}
