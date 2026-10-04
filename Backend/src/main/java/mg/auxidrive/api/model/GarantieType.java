package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "garantiesType")
public class GarantieType {

    @Id
    @Column(name = "cdGarantie", length = 2)
    private String cdGarantie;

    @Column(name = "libelleGarantie", length = 100)
    private String libelleGarantie;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdBranche")
    private Branche branche;

    public GarantieType(String libelleGarantie, Branche branche) {
        this.libelleGarantie = libelleGarantie;
        this.branche = branche;
    }

    public GarantieType(String cdGarantie, String libelleGarantie, Branche branche) {
        this.cdGarantie = cdGarantie;
        this.libelleGarantie = libelleGarantie;
        this.branche = branche;
    }

    public String getCdGarantie() {
        return cdGarantie;
    }

    public void setCdGarantie(String cdGarantie) {
        this.cdGarantie = cdGarantie;
    }

    public String getLibelleGarantie() {
        return libelleGarantie;
    }

    public void setLibelleGarantie(String libelleGarantie) {
        this.libelleGarantie = libelleGarantie;
    }

    public Branche getBranche() {
        return branche;
    }

    public void setBranche(Branche branche) {
        this.branche = branche;
    }
}
