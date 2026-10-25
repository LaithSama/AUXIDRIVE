package mg.auxidrive.api.service;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Transcription du montant à deux décimales effectivement imprimé sur la facture. */
final class MontantEnLettres {

    private static final String[] UNITES = {
            "zéro", "un", "deux", "trois", "quatre", "cinq", "six", "sept", "huit", "neuf",
            "dix", "onze", "douze", "treize", "quatorze", "quinze", "seize"
    };
    private static final String[] DIZAINES = {
            "", "", "vingt", "trente", "quarante", "cinquante", "soixante", "", "quatre-vingt", ""
    };
    private static final String[] ECHELLES = {"", "mille", "million", "milliard", "billion", "billiard"};

    private MontantEnLettres() {
    }

    static String ariary(BigDecimal montant) {
        // Aucune opération d'arrondi ici : le chiffre et les lettres utilisent le même montant.
        BigInteger centiemes = montant.movePointRight(2).toBigIntegerExact();
        BigInteger[] parties = centiemes.abs().divideAndRemainder(BigInteger.valueOf(100));
        String resultat = nombre(parties[0]) + " ariary";
        if (parties[1].signum() != 0) {
            int fraction = parties[1].intValueExact();
            resultat += " et " + moinsDeMille(fraction)
                    + (fraction == 1 ? " centième d'ariary" : " centièmes d'ariary");
        }
        return centiemes.signum() < 0 ? "moins " + resultat : resultat;
    }

    private static String nombre(BigInteger valeur) {
        if (valeur.signum() == 0) {
            return UNITES[0];
        }
        String resultat = "";
        int echelle = 0;
        BigInteger mille = BigInteger.valueOf(1000);
        while (valeur.signum() > 0) {
            BigInteger[] division = valeur.divideAndRemainder(mille);
            int groupe = division[1].intValueExact();
            if (groupe != 0) {
                if (echelle >= ECHELLES.length) {
                    throw new IllegalArgumentException("Montant trop élevé pour une transcription en lettres");
                }
                String texte = echelle == 1 && groupe == 1 ? "mille" : moinsDeMille(groupe);
                if (echelle == 1 && groupe > 1) {
                    if (texte.endsWith("cents") || texte.endsWith("vingts")) {
                        texte = texte.substring(0, texte.length() - 1);
                    }
                    texte += " mille";
                } else if (echelle > 1) {
                    texte += " " + ECHELLES[echelle] + (groupe > 1 ? "s" : "");
                }
                // « cent » et « vingt » ne prennent pas de s lorsqu'un autre nombre suit.
                if (echelle == 0 && !resultat.isEmpty()) {
                    if (texte.endsWith("cents")) {
                        texte = texte.substring(0, texte.length() - 1);
                    } else if (texte.endsWith("vingts")) {
                        texte = texte.substring(0, texte.length() - 1);
                    }
                }
                resultat = texte + (resultat.isEmpty() ? "" : " " + resultat);
            }
            valeur = division[0];
            echelle++;
        }
        return resultat;
    }

    private static String moinsDeMille(int nombre) {
        if (nombre < 100) {
            return moinsDeCent(nombre);
        }
        int centaines = nombre / 100;
        int reste = nombre % 100;
        String texte = centaines == 1 ? "cent" : UNITES[centaines] + " cent";
        if (reste == 0) {
            return centaines > 1 ? texte + "s" : texte;
        }
        return texte + " " + moinsDeCent(reste);
    }

    private static String moinsDeCent(int nombre) {
        if (nombre <= 16) {
            return UNITES[nombre];
        }
        if (nombre < 20) {
            return "dix-" + UNITES[nombre - 10];
        }
        int dizaine = nombre / 10;
        int unite = nombre % 10;
        if (dizaine == 7 || dizaine == 9) {
            int complement = unite + 10;
            String prefixe = dizaine == 7 ? "soixante" : "quatre-vingt";
            return prefixe + (dizaine == 7 && unite == 1 ? " et " : "-") + moinsDeCent(complement);
        }
        String texte = DIZAINES[dizaine];
        if (unite == 0) {
            return dizaine == 8 ? texte + "s" : texte;
        }
        return texte + (unite == 1 && dizaine != 8 ? " et " : "-") + UNITES[unite];
    }
}
