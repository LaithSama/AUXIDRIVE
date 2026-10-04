package mg.auxidrive.api.model;

import java.time.LocalDate;

/**
 * Déclaration de sinistre envoyée par l'assuré.
 *
 * Comme pour la souscription, c'est une commande et non une entité : le client choisit son
 * véhicule, la date et les circonstances, jamais le statut ni les montants, qui relèvent de l'agent.
 */
public class DemandeSinistre {

    private Long idContrat;
    private Long idVehicule;
    private LocalDate dateSinistre;
    private String lieuSinistre;
    private String circonstances;

    public Long getIdContrat() {
        return idContrat;
    }

    public void setIdContrat(Long idContrat) {
        this.idContrat = idContrat;
    }

    public Long getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Long idVehicule) {
        this.idVehicule = idVehicule;
    }

    public LocalDate getDateSinistre() {
        return dateSinistre;
    }

    public void setDateSinistre(LocalDate dateSinistre) {
        this.dateSinistre = dateSinistre;
    }

    public String getLieuSinistre() {
        return lieuSinistre;
    }

    public void setLieuSinistre(String lieuSinistre) {
        this.lieuSinistre = lieuSinistre;
    }

    public String getCirconstances() {
        return circonstances;
    }

    public void setCirconstances(String circonstances) {
        this.circonstances = circonstances;
    }
}