package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Offres")
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    @Column(name = "libelle_offre", length = 50)
    private String libelleOffre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_offre")
    private TypeOffre typeOffre;

    /**
     * Branche de rattachement de l'offre. Elle détermine les garanties incluses et, par l'intermédiaire
     * des paramètres de calcul de la branche, les taux de TE, de TVA et de TACAVA ainsi que le
     * montant des accessoires appliqués au contrat.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cd_branche")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Branche branche;

    @Column(name = "duree_min_mois")
    private Integer dureeMinMois;

    @Column(name = "nb_vehicules_min")
    private Integer nbVehiculesMin;

    @Column(name = "taux_remise")
    private Double tauxRemise;

    @Column(name = "description", length = 255)
    private String description;

    public Long getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(Long idOffre) {
        this.idOffre = idOffre;
    }

    public String getLibelleOffre() {
        return libelleOffre;
    }

    public void setLibelleOffre(String libelleOffre) {
        this.libelleOffre = libelleOffre;
    }

    public TypeOffre getTypeOffre() {
        return typeOffre;
    }

    public void setTypeOffre(TypeOffre typeOffre) {
        this.typeOffre = typeOffre;
    }

    public Branche getBranche() {
        return branche;
    }

    public void setBranche(Branche branche) {
        this.branche = branche;
    }

    public Integer getDureeMinMois() {
        return dureeMinMois;
    }

    public void setDureeMinMois(Integer dureeMinMois) {
        this.dureeMinMois = dureeMinMois;
    }

    public Integer getNbVehiculesMin() {
        return nbVehiculesMin;
    }

    public void setNbVehiculesMin(Integer nbVehiculesMin) {
        this.nbVehiculesMin = nbVehiculesMin;
    }

    public Double getTauxRemise() {
        return tauxRemise;
    }

    public void setTauxRemise(Double tauxRemise) {
        this.tauxRemise = tauxRemise;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
