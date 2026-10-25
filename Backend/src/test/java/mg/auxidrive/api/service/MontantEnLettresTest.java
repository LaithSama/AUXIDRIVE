package mg.auxidrive.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class MontantEnLettresTest {

    @Test
    void montantsEtCentiemes() {
        assertThat(MontantEnLettres.ariary(new BigDecimal("0.00"))).isEqualTo("zéro ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("0.01")))
                .isEqualTo("zéro ariary et un centième d'ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("1.50")))
                .isEqualTo("un ariary et cinquante centièmes d'ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("71.71")))
                .isEqualTo("soixante et onze ariary et soixante et onze centièmes d'ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("80.90")))
                .isEqualTo("quatre-vingts ariary et quatre-vingt-dix centièmes d'ariary");
    }

    @Test
    void grandesValeurs() {
        assertThat(MontantEnLettres.ariary(new BigDecimal("200000.00")))
                .isEqualTo("deux cent mille ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("80000.00")))
                .isEqualTo("quatre-vingt mille ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("2000000.00")))
                .isEqualTo("deux millions ariary");
        assertThat(MontantEnLettres.ariary(new BigDecimal("1230250.00")))
                .isEqualTo("un million deux cent trente mille deux cent cinquante ariary");
    }
}
