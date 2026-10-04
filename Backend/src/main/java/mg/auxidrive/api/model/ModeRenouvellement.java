package mg.auxidrive.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "modesRenouvellement")
public class ModeRenouvellement {

    @Id
    @Column(name = "modeDeRnvlt", length = 5)
    private String modeDeRnvlt;

    @Column(name = "modeDeRnvltLib", length = 50)
    private String modeDeRnvltLib;

    public ModeRenouvellement(String modeDeRnvltLib) {
        this.modeDeRnvltLib = modeDeRnvltLib;
    }

    public ModeRenouvellement(String modeDeRnvlt, String modeDeRnvltLib) {
        this.modeDeRnvlt = modeDeRnvlt;
        this.modeDeRnvltLib = modeDeRnvltLib;
    }

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
