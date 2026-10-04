package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;
import mg.auxidrive.api.service.PrimeCalculator.CalculPrimeException;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Vérifie le calcul de la prime étape par étape.
 *
 * Le cas de référence est celui vérifié à la main : prime nette 100 000, accessoires 2 000,
 * TE 4,5 %, TACAVA 18 %, TVA 20 %, commission 10 %.
 */
class PrimeCalculatorTest {

    private static final double DELTA = 0.001d;

    private BrancheParametresCalcul parametres(Double accessoires, double tauxTe, double tauxTacava) {
        BrancheParametresCalcul p = new BrancheParametresCalcul();
        p.setAccessoires(accessoires);
        p.setTauxTe(tauxTe);
        p.setTauxTacava(tauxTacava);
        p.setTauxTva(20d);
        return p;
    }

    private CodeTaxePolice codeTaxe(boolean te, boolean tva, boolean tacava, double tauxTva) {
        CodeTaxePolice c = new CodeTaxePolice();
        c.setCodeTaxe("T");
        c.setAppliquerTe(te);
        c.setAppliquerTva(tva);
        c.setAppliquerTacava(tacava);
        c.setTauxTaxe(tauxTva);
        return c;
    }

    private AutoCodeTarif codeTarif(boolean avecTacava) {
        AutoCodeTarif c = new AutoCodeTarif();
        c.setTarifCode("11 (A)");
        c.setAvecTacava(avecTacava);
        return c;
    }

    /** Police complète : TE, TVA et TACAVA, tarif qui demande le TACAVA. */
    private Detail calculerComplet(double primeNette) {
        return PrimeCalculator.calculer(primeNette, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d);
    }

    // ---- cas de référence ----

    @Test
    @DisplayName("Cas de référence : base 102 000, TE 4 590, TACAVA 18 360, TVA 24 990, TTC 149 940")
    void casDeReference() {
        Detail d = calculerComplet(100000d);

        assertThat(d.base()).isEqualTo(102000d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.te()).isEqualTo(4590d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tacava()).isEqualTo(18360d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tva()).isEqualTo(24990d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.ttc()).isEqualTo(149940d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.commission()).isEqualTo(10200d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("La TVA porte en cascade sur TE et TACAVA")
    void tvaEstEnCascade() {
        Detail d = calculerComplet(100000d);

        // 102 000 + 4 590 + 18 360 = 124 950, puis 20 % = 24 990
        assertThat(d.tva()).isEqualTo(24990d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tva()).isGreaterThan(d.base() * 0.20);
    }

    @Test
    @DisplayName("TTC = base + TE + TACAVA + TVA")
    void ttcEstLaSommeDesComposantes() {
        Detail d = calculerComplet(100000d);

        assertThat(d.ttc()).isEqualTo(d.base() + d.te() + d.tacava() + d.tva(), org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.totalTaxes()).isEqualTo(d.te() + d.tacava() + d.tva(), org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("La commission n'entre pas dans le TTC")
    void commissionHorsTtc() {
        Detail d = calculerComplet(100000d);
        Detail sansCommission = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), codeTarif(true), 0d);

        assertThat(sansCommission.commission()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(sansCommission.ttc()).isEqualTo(d.ttc(), org.assertj.core.data.Offset.offset(DELTA));
    }

    // ---- application des taxes ----

    @Test
    @DisplayName("Code E : sans TE, avec TVA, sans TACAVA")
    void codeESansTeAvecTva() {
        Detail d = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(false, true, false, 20d), codeTarif(true), 10d);

        assertThat(d.te()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tacava()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tva()).isEqualTo(20400d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.ttc()).isEqualTo(122400d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Code B : ni TE, ni TVA, ni TACAVA, prime TTC = base")
    void codeBSansTaxe() {
        Detail d = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(false, false, false, 0d), codeTarif(true), 10d);

        assertThat(d.totalTaxes()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.ttc()).isEqualTo(102000d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Code U : avec TE, sans TVA")
    void codeUAvecTeSansTva() {
        Detail d = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, false, false, 0d), codeTarif(true), 10d);

        assertThat(d.te()).isEqualTo(4590d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tva()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.ttc()).isEqualTo(106590d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Le TACAVA exige les deux autorisations : code taxe ET tarif du véhicule")
    void tacavaExigeLesDeuxAutorisations() {
        // le code T autorise le TACAVA mais le tarif ne le demande pas
        Detail sansTacava = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), codeTarif(false), 10d);
        assertThat(sansTacava.tacava()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));

        // le tarif le demande mais le code ne l'autorise pas
        Detail refus = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, false, 20d), codeTarif(true), 10d);
        assertThat(refus.tacava()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Sans code tarifaire, le TACAVA ne s'applique pas")
    void sansCodeTarifPasDeTacava() {
        Detail d = PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), null, 10d);

        assertThat(d.tacava()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
    }

    // ---- contrôles de cohérence ----

    @Test
    @DisplayName("TE qui s'applique avec un taux nul -> erreur « Veuillez introduire Taux TE »")
    void teSansTauxEstRefuse() {
        BrancheParametresCalcul p = parametres(2000d, 0d, 18d);

        assertThatThrownBy(() -> PrimeCalculator.calculer(100000d, p,
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessage("Veuillez introduire Taux TE");
    }

    @Test
    @DisplayName("TVA qui s'applique avec un taux nul -> erreur « Veuillez introduire Taux TVA »")
    void tvaSansTauxEstRefusee() {
        assertThatThrownBy(() -> PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 0d), codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessage("Veuillez introduire Taux TVA");
    }

    @Test
    @DisplayName("TACAVA qui s'applique avec un taux nul -> erreur « Veuillez introduire Taux TACAVA »")
    void tacavaSansTauxEstRefuse() {
        BrancheParametresCalcul p = parametres(2000d, 4.5d, 0d);

        assertThatThrownBy(() -> PrimeCalculator.calculer(100000d, p,
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessage("Veuillez introduire Taux TACAVA");
    }

    @Test
    @DisplayName("Un taux non appliqué n'est pas contrôlé, même s'il est nul")
    void tauxNonAppliqueNonControle() {
        // TE non appliquée : le taux de TE nul de la branche ne doit pas bloquer
        Detail d = PrimeCalculator.calculer(100000d, parametres(2000d, 0d, 18d),
                codeTaxe(false, true, false, 20d), codeTarif(false), 10d);

        assertThat(d.te()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Code taxe absent -> erreur")
    void codeTaxeAbsentEstRefuse() {
        assertThatThrownBy(() -> PrimeCalculator.calculer(100000d, parametres(2000d, 4.5d, 18d),
                null, codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessage("Veuillez sélectionner un code taxe");
    }

    @Test
    @DisplayName("Paramètres de branche absents -> erreur")
    void parametresAbsentsSontRefuses() {
        assertThatThrownBy(() -> PrimeCalculator.calculer(100000d, null,
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessageContaining("paramètre de calcul");
    }

    @Test
    @DisplayName("Prime nette négative -> erreur")
    void primeNegativeEstRefusee() {
        assertThatThrownBy(() -> PrimeCalculator.calculer(-100d, parametres(2000d, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d))
                .isInstanceOf(CalculPrimeException.class)
                .hasMessageContaining("négative");
    }

    @Test
    @DisplayName("Branche sans accessoires : la base est la prime nette seule")
    void brancheSansAccessoires() {
        Detail d = PrimeCalculator.calculer(100000d, parametres((Double) null, 4.5d, 18d),
                codeTaxe(true, true, true, 20d), codeTarif(true), 10d);

        assertThat(d.base()).isEqualTo(100000d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.accessoires()).isEqualTo(0d, org.assertj.core.data.Offset.offset(DELTA));
    }

    @Test
    @DisplayName("Les montants sont arrondis à deux décimales")
    void arrondiADeuxDecimales() {
        // 1000 + 2000 = 3000, TE 4,5 % = 135 exactement ; on prend une base qui produit une décimale
        Detail d = PrimeCalculator.calculer(333.33d, parametres(0d, 4.5d, 0d),
                codeTaxe(true, false, false, 0d), codeTarif(false), null);

        assertThat(d.te()).isEqualTo(15.0d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(String.valueOf(d.te())).doesNotContain("0000001");
    }

    @Test
    @DisplayName("Prime nulle : le calcul reste cohérent")
    void primeNulle() {
        Detail d = calculerComplet(0d);

        assertThat(d.base()).isEqualTo(2000d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.te()).isEqualTo(90d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tacava()).isEqualTo(360d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.tva()).isEqualTo(490d, org.assertj.core.data.Offset.offset(DELTA));
        assertThat(d.ttc()).isEqualTo(2940d, org.assertj.core.data.Offset.offset(DELTA));
    }
}