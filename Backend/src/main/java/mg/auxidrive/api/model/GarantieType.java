package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Garanties_type")
public class GarantieType {

    @Id
    @Column(name = "cd_garantie", length = 2)
    private String cdGarantie;

    @Column(name = "libelle_garantie", length = 100)
    private String libelleGarantie;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cd_branche")
    private Branche branche;

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
