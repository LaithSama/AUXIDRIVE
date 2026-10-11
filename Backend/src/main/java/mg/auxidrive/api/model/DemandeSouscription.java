package mg.auxidrive.api.model;

import java.util.List;

/**
 * Demande de souscription envoyée par le client ou l'agent.
 *
 * Ce n'est pas un jeu de données de sortie mais une commande : la créer évite de faire transiter
 * une entité {@link Contrat} largement vide, et empêche le client de choisir son statut, son numéro
 * de police ou ses montants, qui sont calculés par le serveur.
 */
public class DemandeSouscription {

    /** Offre choisie : elle porte la branche, donc les garanties et les taux. */
    private Long idOffre;

    /**
     * Code taxe de la police (B, E, P, T, U ou Z). Facultatif : s'il est absent, le code est
     * déduit du code tarifaire du premier véhicule.
     */
    private String codeTaxe;

    /** Véhicules à couvrir : un seul pour une offre MONOVEHICULE, plusieurs pour une FLOTTE. */
    private List<Long> idVehicules;

    /**
     * Garanties souscrites, par leur code. Facultatif : une liste vide ou absente signifie
     * que le contrat est souscrit sans garantie (0..n).
     */
    private List<String> cdGaranties;

    /** Prime nette saisie, hors accessoires et avant application du bonus/malus. */
    private Double primeNette;

    /** Taux de commission saisi pour ce contrat. */
    private Double tauxCommission;

    /** Coefficient de bonus/malus : appliqué à la prime nette avant le calcul, 1.0 si nul. */
    private Double coefficientBonusMalus;

    /**
     * Durée du contrat en mois choisie par le client. La date d'effet n'est pas transmise :
     * elle est fixée à la date de validation par l'agent, et l'expiration en découle.
     */
    private Integer dureeMois;

    private String codeAgence;

    public Long getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(Long idOffre) {
        this.idOffre = idOffre;
    }

    public String getCodeTaxe() {
        return codeTaxe;
    }

    public void setCodeTaxe(String codeTaxe) {
        this.codeTaxe = codeTaxe;
    }

    public List<Long> getIdVehicules() {
        return idVehicules;
    }

    public void setIdVehicules(List<Long> idVehicules) {
        this.idVehicules = idVehicules;
    }

    public List<String> getCdGaranties() {
        return cdGaranties;
    }

    public void setCdGaranties(List<String> cdGaranties) {
        this.cdGaranties = cdGaranties;
    }

    public Double getPrimeNette() {
        return primeNette;
    }

    public void setPrimeNette(Double primeNette) {
        this.primeNette = primeNette;
    }

    public Double getTauxCommission() {
        return tauxCommission;
    }

    public void setTauxCommission(Double tauxCommission) {
        this.tauxCommission = tauxCommission;
    }

    public Double getCoefficientBonusMalus() {
        return coefficientBonusMalus;
    }

    public void setCoefficientBonusMalus(Double coefficientBonusMalus) {
        this.coefficientBonusMalus = coefficientBonusMalus;
    }

    public Integer getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(Integer dureeMois) {
        this.dureeMois = dureeMois;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }
}