package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.repository.AssureIdentifiantsLegauxRepository;
import mg.auxidrive.api.repository.AssureProfessionRepository;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;

@ExtendWith(MockitoExtension.class)
class AssureServiceTest {

    @Mock
    private AssureRepository assureRepository;
    @Mock
    private AssureProfessionRepository professionRepository;
    @Mock
    private AssureIdentifiantsLegauxRepository legauxRepository;
    @Mock
    private CompteClientRepository compteClientRepository;

    private AssureService service;

    @BeforeEach
    void setUp() {
        service = new AssureService(assureRepository, professionRepository, legauxRepository, compteClientRepository);
    }

    /** Assuré existant en base, entièrement renseigné. */
    private Assure assureEnBase() {
        Assure a = new Assure();
        a.setIdAssure(1L);
        a.setNumerosAssure("2026A001");
        a.setNomAssure("RANDRIANA");
        a.setPrenomAssure("Toky");
        a.setPersonneMorale(false);
        a.setDateNaissance(LocalDate.of(1990, 5, 14));
        a.setSexe("M");
        a.setCivilite("Monsieur");
        a.setNationalite("Malagasy");
        a.setPaysResidence("Madagascar");
        a.setAdresse("Lot II B 45 Toliara");
        a.setContact("0341234567");
        a.setMail("toky@example.mg");
        return a;
    }

    private Assure charge() {
        Assure a = assureEnBase();
        when(assureRepository.findById(1L)).thenReturn(Optional.of(a));
        return a;
    }

    @Test
    @DisplayName("PUT partiel : seul le champ fourni est modifié, les autres sont intacts")
    void putPartielNeModifieQueLeChampFourni() {
        Assure enBase = charge();

        Assure resultat = service.modifier(1L, donnees("RANDRIANA", null, null, null, null, null, null, null, null, null));

        assertThat(resultat.getContact()).isEqualTo(enBase.getContact());
        assertThat(resultat.getPrenomAssure()).isEqualTo("Toky");
        assertThat(resultat.getAdresse()).isEqualTo("Lot II B 45 Toliara");
        assertThat(resultat.getDateNaissance()).isEqualTo(LocalDate.of(1990, 5, 14));
        assertThat(resultat.getSexe()).isEqualTo("M");
        assertThat(resultat.getNationalite()).isEqualTo("Malagasy");
    }

    @Test
    @DisplayName("PUT partiel : le champ fourni est bien mis à jour")
    void putPartielMetAJourLeChampFourni() {
        charge();

        Assure entree = donnees("RANDRIANA", null, null, null, null, null, null, null, "Lot IV, Toliara", null);
        Assure resultat = service.modifier(1L, entree);

        assertThat(resultat.getAdresse()).isEqualTo("Lot IV, Toliara");
        assertThat(resultat.getContact()).isEqualTo("0341234567");
    }

    @Test
    @DisplayName("PUT : le numéro d'assuré et l'e-mail ne sont jamais modifiables")
    void putNeModifiePasNumeroNiEmail() {
        Assure enBase = charge();

        Assure entree = donnees("RANDRIANA", null, null, null, null, null, null, null, null, null);
        entree.setNumerosAssure("PIRATE99");
        entree.setMail("pirate@example.mg");
        Assure resultat = service.modifier(1L, entree);

        assertThat(resultat.getNumerosAssure()).isEqualTo("2026A001").isEqualTo(enBase.getNumerosAssure());
        assertThat(resultat.getMail()).isEqualTo("toky@example.mg");
    }

    @Test
    @DisplayName("PUT : corps complet, toutes les valeurs sont appliquées")
    void putCompletAppliqueToutesLesValeurs() {
        charge();

        Assure resultat = service.modifier(1L, donnees("RAJAONARISON", "Fara", true,
                LocalDate.of(1995, 11, 20), "F", "Madame", "Malagasy", "Madagascar", "Fianarantsoa", "0332222222"));

        assertThat(resultat.getNomAssure()).isEqualTo("RAJAONARISON");
        assertThat(resultat.getPrenomAssure()).isEqualTo("Fara");
        assertThat(resultat.getPersonneMorale()).isTrue();
        assertThat(resultat.getDateNaissance()).isEqualTo(LocalDate.of(1995, 11, 20));
        assertThat(resultat.getSexe()).isEqualTo("F");
        assertThat(resultat.getCivilite()).isEqualTo("Madame");
        assertThat(resultat.getPaysResidence()).isEqualTo("Madagascar");
        assertThat(resultat.getAdresse()).isEqualTo("Fianarantsoa");
        assertThat(resultat.getContact()).isEqualTo("0332222222");
    }

    @Test
    @DisplayName("PUT : les valeurs sont nettoyées (espaces en trop)")
    void putNettoieLesEspaces() {
        charge();

        Assure resultat = service.modifier(1L, donnees("  RANDRIANA  ", " Toky ", null,
                null, null, null, null, null, "  Lot IV  ", null));

        assertThat(resultat.getNomAssure()).isEqualTo("RANDRIANA");
        assertThat(resultat.getPrenomAssure()).isEqualTo("Toky");
        assertThat(resultat.getAdresse()).isEqualTo("Lot IV");
    }

    @Test
    @DisplayName("PUT : vider un champ facultatif le retire réellement de la fiche")
    void putEffaceChampFacultatif() {
        charge();

        Assure resultat = service.modifier(1L, donnees("RANDRIANA", null, null,
                null, null, null, null, null, "   ", ""));

        assertThat(resultat.getAdresse()).isNull();
        assertThat(resultat.getContact()).isNull();
        assertThat(resultat.getPrenomAssure()).isEqualTo("Toky");
        assertThat(resultat.getDateNaissance()).isEqualTo(LocalDate.of(1990, 5, 14));
    }

    @Test
    @DisplayName("PUT : nom manquant -> 400")
    void putSansNomRenvoie400() {
        charge();
        Assure sansNom = donnees(null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.modifier(1L, sansNom))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    @DisplayName("PUT : nom vide ou composé uniquement d'espaces -> 400")
    void putAvecNomBlancRenvoie400() {
        charge();

        assertThatThrownBy(() -> service.modifier(1L, donnees("   ", null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(400));
    }

    @Test
    @DisplayName("PUT : assuré inexistant -> 404")
    void putSurAssureInexistantRenvoie404() {
        when(assureRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.modifier(99L, donnees("RANDRIANA", null, null, null, null, null, null, null, null, null)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode().value()).isEqualTo(404));
    }

    @Test
    @DisplayName("GET /liste : l'assuré est bien renvoyé")
    void listerRenvoieLesAssures() {
        Assure a = assureEnBase();
        when(assureRepository.findAll()).thenReturn(java.util.List.of(a));

        assertThat(service.lister()).containsExactly(a);
    }

    @Test
    @DisplayName("GET /fiche : professions, identifiants et compte sont assemblés")
    void ficheAssembleLesDonneesLiees() {
        Assure a = charge();
        when(professionRepository.findByAssure_IdAssure(1L)).thenReturn(java.util.List.of());
        when(legauxRepository.findByAssure_IdAssure(1L)).thenReturn(Optional.empty());
        when(compteClientRepository.findByAssure_IdAssure(1L)).thenReturn(Optional.empty());

        var fiche = service.fiche(1L);

        assertThat(fiche).containsKeys("assure", "professions", "identifiants", "compte");
        assertThat(fiche.get("assure")).isSameAs(a);
    }

    @Test
    @DisplayName("GET /fiche : la lecture ne modifie aucune donnée")
    void ficheNeModifieRien() {
        Assure a = assureEnBase();
        when(assureRepository.findById(1L)).thenReturn(Optional.of(a));
        when(professionRepository.findByAssure_IdAssure(1L)).thenReturn(java.util.List.of());
        when(legauxRepository.findByAssure_IdAssure(1L)).thenReturn(Optional.empty());
        when(compteClientRepository.findByAssure_IdAssure(1L)).thenReturn(Optional.empty());

        service.fiche(1L);

        assertThat(a.getNomAssure()).isEqualTo("RANDRIANA");
        assertThat(a.getNumerosAssure()).isEqualTo("2026A001");
    }

    private Assure donnees(String nom, String prenom, Boolean personneMorale, LocalDate naissance,
                           String sexe, String civilite, String nationalite, String paysResidence,
                           String adresse, String contact) {
        Assure d = new Assure();
        d.setNomAssure(nom);
        d.setPrenomAssure(prenom);
        d.setPersonneMorale(personneMorale);
        d.setDateNaissance(naissance);
        d.setSexe(sexe);
        d.setCivilite(civilite);
        d.setNationalite(nationalite);
        d.setPaysResidence(paysResidence);
        d.setAdresse(adresse);
        d.setContact(contact);
        return d;
    }
}
