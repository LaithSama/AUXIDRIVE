package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mvtsType")
public class MvtType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idMvtType")
    private Long idMvtType;

    @Column(name = "libelleType", length = 20)
    private String libelleType;

    public MvtType(String libelleType) {
        this.libelleType = libelleType;
    }

    public MvtType(Long idMvtType, String libelleType) {
        this.idMvtType = idMvtType;
        this.libelleType = libelleType;
    }

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
