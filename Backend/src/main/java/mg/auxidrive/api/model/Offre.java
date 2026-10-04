package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "offres")
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idOffre")
    private Long idOffre;

    @Column(name = "libelleOffre", length = 50)
    private String libelleOffre;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeOffre")
    private TypeOffre typeOffre;

    @Column(name = "dureeMinMois")
    private Integer dureeMinMois;

    @Column(name = "nbVehiculesMin")
    private Integer nbVehiculesMin;

    @Column(name = "tauxRemise")
    private Double tauxRemise;

    @Column(name = "description", length = 255)
    private String description;

    public Offre(String libelleOffre, TypeOffre typeOffre, Integer dureeMinMois, Integer nbVehiculesMin, Double tauxRemise, String description) {
        this.libelleOffre = libelleOffre;
        this.typeOffre = typeOffre;
        this.dureeMinMois = dureeMinMois;
        this.nbVehiculesMin = nbVehiculesMin;
        this.tauxRemise = tauxRemise;
        this.description = description;
    }

    public Offre(Long idOffre, String libelleOffre, TypeOffre typeOffre, Integer dureeMinMois, Integer nbVehiculesMin, Double tauxRemise, String description) {
        this.idOffre = idOffre;
        this.libelleOffre = libelleOffre;
        this.typeOffre = typeOffre;
        this.dureeMinMois = dureeMinMois;
        this.nbVehiculesMin = nbVehiculesMin;
        this.tauxRemise = tauxRemise;
        this.description = description;
    }

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
