package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import mg.auxidrive.api.model.Vehicule;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.VehiculeRepository;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceTest {

    @Mock
    private VehiculeRepository vehiculeRepository;
    @Mock
    private AssureRepository assureRepository;
    @Mock
    private AutoCodeTarifRepository codeTarifRepository;

    private VehiculeService service;

    @BeforeEach
    void setUp() {
        service = new VehiculeService(vehiculeRepository, assureRepository, codeTarifRepository);
    }

    private Assure assure(Long id) {
        Assure a = new Assure();
        a.setIdAssure(id);
        return a;
    }

    /** Véhicule déjà rattaché à l'assuré 1. */
    private Vehicule vehiculeEnBase() {
        Vehicule v = new Vehicule();
        v.setIdVehicule(10L);
        v.setNumImmatriculation("1234ABC");
        v.setNumChassis("VF1BB000000123456");
        v.setMarque("Renault");
        v.setModele("Clio");
        v.setEnergie("Essence");
        v.setNombrePlaces(5);
        v.setPuissanceFiscale(6);
        v.setAssure(assure(1L));
        return v;
    }

    private Vehicule saisieValide() {
        Vehicule v = new Vehicule();
        v.setNumImmatriculation("1234ABC");
        v.setNumChassis("VF1BB000000123456");
        v.setMarque("Renault");
        v.setModele("Clio");
        v.setEnergie("Essence");
        v.setNombrePlaces(5);
        v.setPuissanceFiscale(6);
        return v;
    }

    private void assertStatut(ResponseStatusException e, int attendu) {
        assertThat(e.getStatusCode()).isEqualTo(HttpStatus.valueOf(attendu));
    }

    // ---- unicité ----

    @Test
    @DisplayName("Création : immatriculation déjà enregistrée -> 409")
    void creationRefuseImmatriculationEnDouble() {
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(true);
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));

        assertThatThrownBy(() -> service.creer(saisieValide(), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 409));
        verify(vehiculeRepository, never()).save(any());
    }

    @Test
    @DisplayName("Création : châssis déjà enregistré -> 409")
    void creationRefuseChassisEnDouble() {
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(false);
        when(vehiculeRepository.existsByNumChassisIgnoreCase("VF1BB000000123456")).thenReturn(true);
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));

        assertThatThrownBy(() -> service.creer(saisieValide(), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 409));
    }

    @Test
    @DisplayName("Modification : garder sa propre immatriculation n'est pas un doublon")
    void modificationAccepteSaPropreImmatriculation() {
        Vehicule enBase = vehiculeEnBase();
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(enBase));
        when(vehiculeRepository.findByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(Optional.of(enBase));

        Vehicule donnees = saisieValide();
        donnees.setMarque("Peugeot");

        Vehicule resultat = service.modifier(10L, donnees);

        assertThat(resultat.getMarque()).isEqualTo("Peugeot");
        assertThat(resultat.getNumImmatriculation()).isEqualTo("1234ABC");
    }

    @Test
    @DisplayName("Modification : prendre l'immatriculation d'un autre véhicule -> 409")
    void modificationRefuseImmatriculationDAutreVehicule() {
        Vehicule enBase = vehiculeEnBase();
        Vehicule autre = vehiculeEnBase();
        autre.setIdVehicule(11L);
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(enBase));
        when(vehiculeRepository.findByNumImmatriculationIgnoreCase("9999XYZ")).thenReturn(Optional.of(autre));

        Vehicule donnees = saisieValide();
        donnees.setNumImmatriculation("9999XYZ");

        assertThatThrownBy(() -> service.modifier(10L, donnees))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 409));
    }

    // ---- champs obligatoires ----

    @Test
    @DisplayName("Création : immatriculation absente -> 400")
    void creationSansImmatriculationRenvoie400() {
        Vehicule donnees = saisieValide();
        donnees.setNumImmatriculation("  ");
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));

        assertThatThrownBy(() -> service.creer(donnees, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 400));
    }

    @Test
    @DisplayName("Création : châssis absent -> 400")
    void creationSansChassisRenvoie400() {
        Vehicule donnees = saisieValide();
        donnees.setNumChassis(null);
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));

        assertThatThrownBy(() -> service.creer(donnees, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 400));
    }

    @Test
    @DisplayName("Création : marque absente -> 400")
    void creationSansMarqueRenvoie400() {
        Vehicule donnees = saisieValide();
        donnees.setMarque(null);
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(false);
        when(vehiculeRepository.existsByNumChassisIgnoreCase("VF1BB000000123456")).thenReturn(false);

        assertThatThrownBy(() -> service.creer(donnees, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 400));
    }

    @Test
    @DisplayName("Création : nombre de places absent -> 400")
    void creationSansNombrePlacesRenvoie400() {
        Vehicule donnees = saisieValide();
        donnees.setNombrePlaces(null);
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(false);
        when(vehiculeRepository.existsByNumChassisIgnoreCase("VF1BB000000123456")).thenReturn(false);

        assertThatThrownBy(() -> service.creer(donnees, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 400));
    }

    @Test
    @DisplayName("Création : assuré inexistant -> 404")
    void creationPourAssureInexistantRenvoie404() {
        when(assureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.creer(saisieValide(), 99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 404));
    }

    // ---- code tarifaire ----

    @Test
    @DisplayName("Création : le code tarifaire est résolu et rattaché")
    void creationRattacheLeCodeTarif() {
        AutoCodeTarif code = new AutoCodeTarif();
        code.setIdCodeAuto(3L);
        code.setTarifCode("11 (A)");
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(false);
        when(vehiculeRepository.existsByNumChassisIgnoreCase("VF1BB000000123456")).thenReturn(false);
        when(codeTarifRepository.findById(3L)).thenReturn(Optional.of(code));
        when(vehiculeRepository.save(any(Vehicule.class))).thenAnswer(i -> i.getArgument(0));

        Vehicule donnees = saisieValide();
        donnees.setCodeTarif(code);

        Vehicule resultat = service.creer(donnees, 1L);

        assertThat(resultat.getCodeTarif()).isNotNull();
        assertThat(resultat.getCodeTarif().getTarifCode()).isEqualTo("11 (A)");
    }

    @Test
    @DisplayName("Création : code tarifaire inconnu -> 400")
    void creationRefuseCodeTarifInconnu() {
        when(assureRepository.findById(1L)).thenReturn(Optional.of(assure(1L)));
        when(vehiculeRepository.existsByNumImmatriculationIgnoreCase("1234ABC")).thenReturn(false);
        when(vehiculeRepository.existsByNumChassisIgnoreCase("VF1BB000000123456")).thenReturn(false);
        when(codeTarifRepository.findById(99L)).thenReturn(Optional.empty());

        Vehicule donnees = saisieValide();
        AutoCodeTarif code = new AutoCodeTarif();
        code.setIdCodeAuto(99L);
        donnees.setCodeTarif(code);

        assertThatThrownBy(() -> service.creer(donnees, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 400));
    }

    // ---- isolation du portail client ----

    @Test
    @DisplayName("Lecture client : le véhicule d'un autre assuré est renvoyé comme introuvable")
    void clientNeVoitPasLeVehiculeDUnAutre() {
        Vehicule vehiculeDAutre = vehiculeEnBase();
        vehiculeDAutre.setAssure(assure(2L));
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehiculeDAutre));

        assertThatThrownBy(() -> service.trouverPourAssure(10L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 404));
    }

    @Test
    @DisplayName("Lecture client : le véhicule de l'assuré connecté est renvoyé")
    void clientVoitSonPropreVehicule() {
        Vehicule vehicule = vehiculeEnBase();
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(vehicule));

        assertThat(service.trouverPourAssure(10L, 1L)).isSameAs(vehicule);
    }

    @Test
    @DisplayName("Véhicule inexistant -> 404")
    void vehiculeInexistantRenvoie404() {
        when(vehiculeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouver(99L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertStatut((ResponseStatusException) e, 404));
    }

    @Test
    @DisplayName("Listing par assuré : seuls les véhicules de cet assuré sont renvoyés")
    void listingParAssureEstFiltre() {
        Vehicule v = vehiculeEnBase();
        when(vehiculeRepository.findByAssure_IdAssureOrderByIdVehicule(1L)).thenReturn(List.of(v));

        assertThat(service.listerParAssure(1L)).containsExactly(v);
    }

    // ---- conservation de l'assuré ----

    @Test
    @DisplayName("Modification : l'assuré du véhicule n'est jamais remplacé")
    void modificationNeChangePasLAssure() {
        Vehicule enBase = vehiculeEnBase();
        when(vehiculeRepository.findById(10L)).thenReturn(Optional.of(enBase));

        Vehicule donnees = saisieValide();
        donnees.setMarque("Dacia");

        Vehicule resultat = service.modifier(10L, donnees);

        assertThat(resultat.getAssure().getIdAssure()).isEqualTo(1L);
        assertThat(resultat.getMarque()).isEqualTo("Dacia");
    }
}