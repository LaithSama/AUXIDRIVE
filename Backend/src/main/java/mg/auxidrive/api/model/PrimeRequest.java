package mg.auxidrive.api.model;

/**
 * Valeurs saisies par l'agent pour calculer ou valider la prime d'un contrat existant.
 *
 * Les trois champs sont facultatifs : un champ absent est remplacé par la valeur courante du
 * contrat (prime nette, taux de commission) ou par les paramètres de la branche (accessoires).
 * Le montant TTC n'est jamais transmis : il est recalculé par le serveur.
 */
public record PrimeRequest(Double primeNette,
                           Double accessoires,
                           Double tauxCommission) {
}
