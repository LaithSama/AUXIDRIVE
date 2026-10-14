package mg.auxidrive.api.model;

/**
 * Paramètres nécessaires à l'agent pour préparer le calcul de prime d'un contrat existant :
 * le code taxe de la police (les formules en dépendent), les taux appliqués et les valeurs par
 * défaut des champs que l'agent peut saisir (prime nette, accessoires, taux de commission).
 *
 * <p>Les taux de TE et de TACAVA proviennent de la branche, le taux de TVA du code taxe, et le
 * taux de commission de la branche tant que le contrat n'en porte pas encore.
 */
public record ParametresPrime(String codeTaxe,
                              String libelleCodeTaxe,
                              Double primeNetteCourante,
                              Double accessoiresDefaut,
                              Double tauxCommission,
                              Double tauxTe,
                              Double tauxTva,
                              Double tauxTacava,
                              boolean appliqueTe,
                              boolean appliqueTva,
                              boolean appliqueTacava) {
}
