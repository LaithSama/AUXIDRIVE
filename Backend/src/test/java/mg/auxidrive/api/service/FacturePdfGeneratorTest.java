package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.Facture;
import mg.auxidrive.api.model.ModeRenouvellement;

/**
 * Génère un vrai PDF et en relit le contenu, c'est le seul moyen de vérifier la mise en page
 * depuis un test : le rendu visuel lui-même reste à contrôler dehors.
 */
class FacturePdfGeneratorTest {

    private final FacturePdfGenerator generator = new FacturePdfGenerator();
    private String genere;

    /**
     * Le PDF est déplacé vers target/ pour inspection visuelle, puis retiré de factures/ :
     * le rendu n'étant pas vérifiable depuis un test, il doit rester ouvrable après le run.
     * Windows peut encore tenir le fichier un instant : l'échec d'un déplacement ne doit pas
     * faire échouer le test, le PDF reste consultable dans factures/.
     */
    @AfterEach
    void nettoyer() throws Exception {
        if (genere == null) {
            return;
        }
        try {
            Path aConsulter = Paths.get("target", "facture-pdf-test.pdf");
            Files.createDirectories(aConsulter.getParent());
            Files.move(Paths.get(genere), aConsulter, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Déplacement du PDF de test impossible : " + e.getMessage());
        }
        genere = null;
    }

    // ------------------------------------------------------------------ données

    private Facture facture(LocalDate emission) {
        Assure assure = new Assure();
        assure.setNomAssure("Rakoto");
        assure.setPrenomAssure("Jean");
        assure.setNumerosAssure("12345678");
        assure.setAdresse("Lot II M 12 Antananarivo");
        assure.setContact("0320524221");

        ModeRenouvellement mode = new ModeRenouvellement();
        mode.setModeDeRnvlt("AUTO");
        mode.setModeDeRnvltLib("Renouvellement automatique");

        Contrat contrat = new Contrat();
        contrat.setNumPolice("2600000001");
        contrat.setAssure(assure);
        contrat.setDateSouscription(LocalDate.of(2026, 10, 1));
        contrat.setDateEffet(LocalDate.of(2026, 10, 15));
        contrat.setDateExpiration(LocalDate.of(2027, 10, 14));
        contrat.setModeRenouvellement(mode);

        Facture facture = new Facture();
        facture.setNumFacture("FAC-2026-" + String.format("%04d", SEQUENCE.incrementAndGet()));
        facture.setContrat(contrat);
        facture.setDateEmission(emission);
        facture.setPrimeNette(1_000_000d);
        facture.setAccessoires(0d);
        facture.setMontantTe(10_000d);
        facture.setMontantTacava(5_000d);
        facture.setMontantTva(215_250d);
        facture.setMontantTaxes(230_250d);
        facture.setMontantTtc(1_230_250d);
        return facture;
    }

    /** Chaque test produit sa propre facture : un fichier verrouillé ne peut pas cascader. */
    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    private String extraireTexte(String chemin) throws Exception {
        // lecture par octets et non par chemin : PdfReader memory-mappe le fichier, et la poignée
        // ainsi ouverte reste prise sur Windows quelques instants, ce qui verrouille le PDF
        try (PdfReader lecteur = new PdfReader(Files.readAllBytes(Paths.get(chemin)))) {
            StringBuilder texte = new StringBuilder();
            for (int page = 1; page <= lecteur.getNumberOfPages(); page++) {
                texte.append(new PdfTextExtractor(lecteur).getTextFromPage(page));
            }
            return texte.toString();
        }
    }

    // ------------------------------------------------------------------ en-tête

    @Test
    @DisplayName("En-tête : mention de lieu et de date d'émission sous le logo")
    void mentionDeLieuEtDate() throws Exception {
        genere = generator.generer(facture(LocalDate.of(2026, 10, 18)), List.of(), List.of());

        assertThat(extraireTexte(genere))
                .contains("Fait le 18/10/2026 à Antananarivo")
                .contains("FACTURE DE SOUSCRIPTION")
                .contains("Facture n° FAC-2026-")
                .contains("Police n° 2600000001");
    }

    @Test
    @DisplayName("Sans date d'émission, la mention est omise plutôt qu'affichée à tort")
    void sansDateDEmission() throws Exception {
        genere = generator.generer(facture(null), List.of(), List.of());

        assertThat(extraireTexte(genere))
                .doesNotContain("Fait le")
                .contains("FACTURE DE SOUSCRIPTION");
    }

    @Test
    @DisplayName("Le logo est bien incorporé au PDF, qui est alors nettement plus lourd qu'un simple texte")
    void logoIncorpore() throws Exception {
        genere = generator.generer(facture(LocalDate.of(2026, 10, 18)), List.of(), List.of());

        // un PDF de texte seul pèse quelques kilo-octets ; le logo fait 107 Ko à l'origine
        assertThat(Files.size(Paths.get(genere))).isGreaterThan(50_000L);
    }

    // ------------------------------------------------------------------ contrat et signatures

    @Test
    @DisplayName("Section Contrat : période de couverture et renouvellement")
    void datesDuContrat() throws Exception {
        genere = generator.generer(facture(LocalDate.of(2026, 10, 18)), List.of(), List.of());

        assertThat(extraireTexte(genere))
                .contains("Date de souscription")
                .contains("Date d'effet")
                .contains("15/10/2026")
                .contains("Date d'expiration")
                .contains("14/10/2027")
                .contains("Mode de renouvellement")
                .contains("Renouvellement automatique");
    }

    @Test
    @DisplayName("Zone de signatures : client à gauche, agent de production à droite")
    void zoneDeSignatures() throws Exception {
        genere = generator.generer(facture(LocalDate.of(2026, 10, 18)), List.of(), List.of());

        assertThat(extraireTexte(genere))
                .contains("Le client")
                .contains("L'agent de production")
                .contains("Montant TTC à payer");
    }

    @Test
    @DisplayName("Le contenu d'origine est conservé : assuré, calcul et mention de commission")
    void contenuHistorique() throws Exception {
        genere = generator.generer(facture(LocalDate.of(2026, 10, 18)), List.of(), List.of());

        String texte = extraireTexte(genere);
        assertThat(texte)
                .contains("Assuré")
                .contains("Rakoto")
                .contains("12345678")
                .contains("Prime nette")
                .contains("Base taxable")
                .contains("TACAVA")
                .contains("1230250.00");
    }
}
