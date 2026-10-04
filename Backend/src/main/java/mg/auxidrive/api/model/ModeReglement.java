package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "modesReglement")
public class ModeReglement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idReglement")
    private Long idReglement;

    @Column(name = "codeRegl", length = 2)
    private String codeRegl;

    @Column(name = "modeRegl", length = 30)
    private String modeRegl;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idModePaie")
    private ModePaiement modePaiement;

    @Column(name = "oldCodeRegl", length = 2)
    private String oldCodeRegl;

    public ModeReglement(String codeRegl, String modeRegl, ModePaiement modePaiement, String oldCodeRegl) {
        this.codeRegl = codeRegl;
        this.modeRegl = modeRegl;
        this.modePaiement = modePaiement;
        this.oldCodeRegl = oldCodeRegl;
    }

    public ModeReglement(Long idReglement, String codeRegl, String modeRegl, ModePaiement modePaiement, String oldCodeRegl) {
        this.idReglement = idReglement;
        this.codeRegl = codeRegl;
        this.modeRegl = modeRegl;
        this.modePaiement = modePaiement;
        this.oldCodeRegl = oldCodeRegl;
    }

    public Long getIdReglement() {
        return idReglement;
    }

    public void setIdReglement(Long idReglement) {
        this.idReglement = idReglement;
    }

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
