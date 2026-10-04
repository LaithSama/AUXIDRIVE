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
}
