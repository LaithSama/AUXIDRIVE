package mg.auxidrive.api.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;

/**
 * Calcul du montant TTC d'une prime automobile, étape par étape.
 *
 * Le calcul est volontairement isolé dans une classe sans dépendance à Spring : il ne touche
 * ni la base ni le web, ce qui le rend vérifiable par des tests unitaires sur des valeurs connues.
 *
 * Dérivé des règles de gestion :
 * <ol>
 * <li>le code taxe (B, E, P, T, U, Z) décide quelles taxes s'appliquent ;</li>
 * <li>le TACAVA s'applique seulement si le code taxe l'autorise ET si le code tarifaire du
 *     véhicule le demande (avec_tacava) ;</li>
 * <li>la branche fournit les taux de TE et de TACAVA, ainsi que le montant des accessoires ;</li>
 * <li>si une taxe s'applique mais que son taux est nul, le calcul est refusé.</li>
 * </ol>
 *
 * Formule :
 * <pre>
 * base          = prime nette + accessoires
 * TACAVA        = arrondi(base x taux TACAVA / 100, 2)
 * TE            = arrondi(base x taux TE / 100, 2)
 * TVA           = arrondi((base + TE + TACAVA) x taux TVA / 100, 2)
 * commission    = arrondi(base x taux commission / 100, 2)
 * TTC           = base + TE + TACAVA + TVA
 * </pre>
 * Le taux de commission vient de la requête s'il est fourni et positif, sinon de la
 * {@code commission_totale} de la branche : c'est le taux de référence quand l'agent n'en saisit
 * pas. La TVA est calculée en cascade sur TE et TACAVA, et la commission n'entre pas dans le TTC :
 * c'est une rémunération d'intermédiaire, pas une taxe payée par l'assuré.
 */
public final class PrimeCalculator {

    /** Nombre de décimales des montants intermédiaires et finaux. */
    private static final int DECIMALES = 2;

    private PrimeCalculator() {
    }

    /**
     * Calcule le détail complet d'une prime, les accessoires venant de la branche.
     *
     * @param primeNette  prime nette saisie, hors accessoires et avant bonus/malus
     * @param parametres  paramètres de calcul de la branche, porteuse des taux
     * @param codeTaxe    code taxe de la police
     * @param codeTarif   code tarifaire du véhicule, peut être absent si le véhicule n'en a pas encore
     * @param tauxCommission taux de commission demandé, peut être nul (repli sur la branche)
     * @return le détail du calcul
     * @throws CalculPrimeException si une taxe s'applique sans taux, ou si un montant est négatif
     */
    public static Detail calculer(double primeNette,
                                  BrancheParametresCalcul parametres,
                                  CodeTaxePolice codeTaxe,
                                  AutoCodeTarif codeTarif,
                                  Double tauxCommission) {
        return calculer(primeNette, null, parametres, codeTaxe, codeTarif, tauxCommission);
    }

    /**
     * Variante où l'agent impose lui-même le montant des accessoires. Si {@code accessoiresSaisis}
     * est nul, les accessoires de la branche sont utilisés : c'est le cas du portail client.
     * Le reste du calcul est identique.
     *
     * @param accessoiresSaisis accessoires saisis par l'agent, ou null pour prendre ceux de la branche
     */
    public static Detail calculer(double primeNette,
                                  Double accessoiresSaisis,
                                  BrancheParametresCalcul parametres,
                                  CodeTaxePolice codeTaxe,
                                  AutoCodeTarif codeTarif,
                                  Double tauxCommission) {

        if (codeTaxe == null) {
            throw new CalculPrimeException("Veuillez sélectionner un code taxe");
        }
        if (parametres == null) {
            throw new CalculPrimeException("Aucun paramètre de calcul n'est défini pour cette branche");
        }
        if (primeNette < 0) {
            throw new CalculPrimeException("La prime nette ne peut pas être négative");
        }

        boolean appliqueTe = vrai(codeTaxe.getAppliquerTe());
        boolean appliqueTva = vrai(codeTaxe.getAppliquerTva());
        // le TACAVA doit être autorisé deux fois : par la police et par le tarif du véhicule
        boolean appliqueTacava = vrai(codeTaxe.getAppliquerTacava()) && codeTarif != null && vrai(codeTarif.getAvecTacava());

        double accessoires = accessoiresSaisis != null ? accessoiresSaisis
                : (parametres.getAccessoires() == null ? 0d : parametres.getAccessoires());

        // Étape 2 : une taxe qui s'applique doit avoir un taux, sinon la police est inexploitable.
        double tauxTe = appliqueTe ? exigerTaux(parametres.getTauxTe(), "Veuillez introduire Taux TE") : 0d;
        double tauxTva = appliqueTva ? exigerTaux(codeTaxe.getTauxTaxe(), "Veuillez introduire Taux TVA") : 0d;
        double tauxTacava = appliqueTacava ? exigerTaux(parametres.getTauxTacava(), "Veuillez introduire Taux TACAVA") : 0d;

        // Étape 3 : les accessoires viennent de la branche, ils entrent dans la base taxable.
        double base = arrondir(primeNette + accessoires);

        // Étape 4 : TACAVA sur la base.
        double tacava = appliqueTacava ? arrondir(base * tauxTacava / 100) : 0d;
        // Étape 5 : TE sur la même base.
        double te = appliqueTe ? arrondir(base * tauxTe / 100) : 0d;
        // Étape 6 : TVA en cascade, elle porte sur la base augmentée de TE et TACAVA.
        double tva = appliqueTva ? arrondir((base + te + tacava) * tauxTva / 100) : 0d;
        // Étape 7 : commission sur la base, hors TTC. Le taux demandé prime ; à défaut (absent ou
        // nul), c'est celui de la branche : la commission_totale, seule valeur de référence connue.
        double taux = tauxCommission != null && tauxCommission > 0
                ? tauxCommission
                : (parametres.getCommissionTotale() == null ? 0d : parametres.getCommissionTotale());
        double commission = taux <= 0 ? 0d : arrondir(base * taux / 100);
        // Étape 8 : montant dû par l'assuré.
        double ttc = arrondir(base + te + tacava + tva);

        return new Detail(base, accessoires, te, tacava, tva, ttc, commission,
                taux, appliqueTe, appliqueTva, appliqueTacava);
    }

    private static double exigerTaux(Double taux, String message) {
        if (taux == null || taux <= 0) {
            throw new CalculPrimeException(message);
        }
        return taux;
    }

    private static boolean vrai(Boolean b) {
        return b != null && b;
    }

    private static double arrondir(double valeur) {
        return BigDecimal.valueOf(valeur).setScale(DECIMALES, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Résultat du calcul. Les montants sont exposés séparément pour que la facture et le PDF
     * puissent afficher la ligne correspondante à chaque taxe.
     */
    public record Detail(double base,
                         double accessoires,
                         double te,
                         double tacava,
                         double tva,
                         double ttc,
                         double commission,
                         double tauxCommission,
                         boolean appliqueTe,
                         boolean appliqueTva,
                         boolean appliqueTacava) {

        public double totalTaxes() {
            return arrondir(te + tacava + tva);
        }
    }

    /** Erreur métier du calcul : message destiné à être renvoyé tel quel à l'utilisateur. */
    public static class CalculPrimeException extends RuntimeException {
        public CalculPrimeException(String message) {
            super(message);
        }
    }
}