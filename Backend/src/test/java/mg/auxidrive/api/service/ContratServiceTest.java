package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.Branche;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.Offre;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.BrancheParametresCalculRepository;
import mg.auxidrive.api.repository.CodeTaxePoliceRepository;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.repository.OffreRepository;
import mg.auxidrive.api.repository.VehiculeRepository;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Reconduction tacite d'un contrat : prolongation des dates sur le même numéro de police,
 * prime recalculée à partir des tarifs du jour, facture de renouvellement émise.
 *
 * Le cas de référence reprend celui de PrimeCalculatorTest : prime nette 100 000,
 * accessoires 2 000, TE 4,5 %, TACAVA 18 %, TVA 20 %, commission 10 % → TTC 149 940.
 */
@ExtendWith(MockitoExtension.class)
class ContratServiceTest {

    private static final double DELTA = 0.001d;

    @Mock
    private ContratRepository contratRepository;
    @Mock
    private ContratAutoRepository contratAutoRepository;
    @Mock
    private OffreRepository offreRepository;
    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private AssureRepository assureRepository;
    @Mock
    private BrancheParametresCalculRepository parametresRepository;
    @Mock
    private CodeTaxePoliceRepository codeTaxeRepository;
    @Mock
    private AutoCodeTarifRepository codeTarifRepository;
    @Mock
    private FactureService factureService;

    private ContratService service;

    @BeforeEach
    void setUp() {
        service = new ContratService(contratRepository, contratAutoRepository, offreRepository,
                vehiculeRepository, assureRepository, parametresRepository, codeTaxeRepository,
                codeTarifRepository, factureService);
    }

    // ---- fixtures ----

    private BrancheParametresCalcul parametres() {
        BrancheParametresCalcul p = new BrancheParametresCalcul();
        p.setAccessoires(2000d);
        p.setTauxTe(4.5d);
        p.setTauxTacava(18d);
        p.setTauxTva(20d);
        return p;
    }

    private CodeTaxePolice codeTaxe() {
        CodeTaxePolice c = new CodeTaxePolice();
        c.setCodeTaxe("T");
        c.setAppliquerTe(true);
        c.setAppliquerTva(true);
        c.setAppliquerTacava(true);
        c.setTauxTaxe(20d);
        return c;
    }

    private AutoCodeTarif codeTarif() {
        AutoCodeTarif c = new AutoCodeTarif();
        c.setTarifCode("11 (A)");
        c.setAvecTacava(true);
        c.setCodeTaxePolice(codeTaxe());
        return c;
    }

    private Contrat contratValide() {
        LocalDate today = LocalDate.now();
        Branche branche = new Branche();
        branche.setCdBranche("11");

        Offre offre = new Offre();
        offre.setDureeMinMois(12);
        offre.setBranche(branche);

        Assure assure = new Assure();
        assure.setNomAssure("Ranaivo");

        Vehicule vehicule = new Vehicule();
        vehicule.setMarque("Toyota");
        vehicule.setModele("Corolla");
        vehicule.setCodeTarif(codeTarif());

        ContratAuto lien = new ContratAuto();
        lien.setVehicule(vehicule);

        Contrat contrat = new Contrat();
        contrat.setIdContrat(1L);
        contrat.setNumPolice("2600000001");
        contrat.setStatutContrat(StatutContrat.VALIDE);
        contrat.setAssure(assure);
        contrat.setOffre(offre);
        contrat.setBranche(branche);
        contrat.setDateEffet(today.minusMonths(12));
        contrat.setDateExpiration(today);
        contrat.setDateNotifEcheance(today.minusDays(2));
        contrat.setPrimeNetteCourante(100000d);
        contrat.setPrimeTtcCourante(149940d);
        contrat.setTauxCommission(10d);
        return contrat;
    }

    private void stubsRecalcul(Contrat contrat) {
        ContratAuto lien = new ContratAuto();
        lien.setVehicule(vehiculeDuFixture());
        when(contratAutoRepository.findByContrat_IdContrat(1L)).thenReturn(List.of(lien));
        when(parametresRepository.findByBranche_CdBranche("11")).thenReturn(Optional.of(parametres()));
    }

    private Vehicule vehiculeDuFixture() {
        Vehicule v = new Vehicule();
        v.setCodeTarif(codeTarif());
        return v;
    }

    // ---- renouveler ----

    @Test
    @DisplayName("Le contrat est prolongé d'un an, la prime recalculée et la facture de renouvellement émise")
    void renouvelerProlongeEtEmetLaFacture() {
        LocalDate today = LocalDate.now();
        Contrat contrat = contratValide();
        stubsRecalcul(contrat);
        when(contratRepository.save(any(Contrat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contrat renouvele = service.renouveler(contrat);

        assertThat(renouvele.getDateEffet()).isEqualTo(today);
        assertThat(renouvele.getDateExpiration()).isEqualTo(today.plusMonths(12));
        assertThat(renouvele.getPrimeTtcCourante()).isEqualTo(149940d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(renouvele.getPrimeNetteCourante()).isEqualTo(100000d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(renouvele.getMontantCommission()).isEqualTo(10200d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(renouvele.getDateNotifEcheance()).isNull();
        verify(factureService).emettreRenouvellement(eq(contrat), any(Detail.class));
        verify(contratRepository).save(contrat);
    }

    @Test
    @DisplayName("Un contrat non validé ne peut pas être renouvelé")
    void renouvelerRefuseContratNonValide() {
        Contrat contrat = contratValide();
        contrat.setStatutContrat(StatutContrat.EN_ATTENTE);

        assertThatThrownBy(() -> service.renouveler(contrat))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("renouvelé");

        verifyNoInteractions(contratAutoRepository, factureService, contratRepository);
    }
}
