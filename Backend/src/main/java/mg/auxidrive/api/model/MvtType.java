package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Mvts_type")
public class MvtType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mvt_type")
    private Long idMvtType;

    @Column(name = "libelle_type", length = 20)
    private String libelleType;

    public Long getIdMvtType() {
        return idMvtType;
    }

    public void setIdMvtType(Long idMvtType) {
        this.idMvtType = idMvtType;
    }

    public String getLibelleType() {
        return libelleType;
    }

    public void setLibelleType(String libelleType) {
        this.libelleType = libelleType;
    }
}
