package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Modes_renouvellement")
public class ModeRenouvellement {

    @Id
    @Column(name = "mode_de_rnvlt", length = 5)
    private String modeDeRnvlt;

    @Column(name = "mode_de_rnvlt_lib", length = 50)
    private String modeDeRnvltLib;

    public String getModeDeRnvlt() {
        return modeDeRnvlt;
    }

    public void setModeDeRnvlt(String modeDeRnvlt) {
        this.modeDeRnvlt = modeDeRnvlt;
    }

    public String getModeDeRnvltLib() {
        return modeDeRnvltLib;
    }

    public void setModeDeRnvltLib(String modeDeRnvltLib) {
        this.modeDeRnvltLib = modeDeRnvltLib;
    }
}
