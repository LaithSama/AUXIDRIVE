package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Modes_reglement")
public class ModeReglement {

    @Id
    @Column(name = "code_regl", length = 2)
    private String codeRegl;

    @Column(name = "mode_regl", length = 30)
    private String modeRegl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mode_paie")
    private ModePaiement modePaiement;

    @Column(name = "old_code_regl", length = 2)
    private String oldCodeRegl;

    public String getCodeRegl() {
        return codeRegl;
    }

    public void setCodeRegl(String codeRegl) {
        this.codeRegl = codeRegl;
    }

    public String getModeRegl() {
        return modeRegl;
    }

    public void setModeRegl(String modeRegl) {
        this.modeRegl = modeRegl;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getOldCodeRegl() {
        return oldCodeRegl;
    }

    public void setOldCodeRegl(String oldCodeRegl) {
        this.oldCodeRegl = oldCodeRegl;
    }
}
