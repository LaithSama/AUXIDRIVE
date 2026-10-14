package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import mg.auxidrive.api.model.Branche;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.Facture;
import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.FactureRepository;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Facturation : la facture recopie la police du contrat et reçoit un numéro daté
 * (année + mois + séquence mensuelle de trois chiffres).
 */
@ExtendWith(MockitoExtension.class)
class FactureServiceTest {

    private static final DateTimeFormatter PREFIXE = DateTimeFormatter.ofPattern("yyyyMM");

    private static final Detail DETAIL = new Detail(102000d, 2000d, 4590d, 18360d, 20400d,
            149940d, 18360d, 18d, true, true, true);

    @Mock
    private FactureRepository factureRepository;

    @Mock
    private ContratAutoRepository contratAutoRepository;

    @Mock
    private FacturePdfGenerator pdfGenerator;

    @InjectMocks
    private FactureService service;

    @Test
    @DisplayName("La facture reprend la police du contrat et un numéro daté séquentiel")
    void emettreReprendLaPoliceEtNumeroteLaFacture() {
        Contrat contrat = contrat("ABC123");
        stub(Optional.empty());

        Facture facture = service.emettreSouscription(contrat, DETAIL);

        assertThat(facture.getNumPolice()).isEqualTo("ABC123");
        assertThat(facture.getNumFacture()).isEqualTo(LocalDate.now().format(PREFIXE) + "001");
    }

    @Test
    @DisplayName("La séquence mensuelle repart du plus grand numéro du mois courant")
    void laSequenceMensuelleProgresse() {
        Contrat contrat = contrat("ABC123");
        Facture derniere = new Facture();
        derniere.setNumFacture(LocalDate.now().format(PREFIXE) + "007");
        stub(Optional.of(derniere));

        Facture facture = service.emettreSouscription(contrat, DETAIL);

        assertThat(facture.getNumFacture()).isEqualTo(LocalDate.now().format(PREFIXE) + "008");
    }

    private void stub(Optional<Facture> derniere) {
        when(factureRepository.save(any(Facture.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(factureRepository.findFirstByNumFactureStartingWithOrderByNumFactureDesc(any()))
                .thenReturn(derniere);
        when(pdfGenerator.generer(any(), any(), any())).thenReturn("factures/test.pdf");
    }

    private static Contrat contrat(String numPolice) {
        Contrat contrat = new Contrat();
        contrat.setNumPolice(numPolice);
        contrat.setBranche(new Branche());
        contrat.setOffre(new Offre());
        return contrat;
    }
}
