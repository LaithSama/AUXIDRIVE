package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "modesPaiement")
public class ModePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idModePaie")
    private Long idModePaie;

    @Column(name = "codePaiement", length = 10)
    private String codePaiement;

    @Column(name = "libelleMode", length = 30)
    private String libelleMode;

    @Column(name = "description", length = 100)
    private String description;

    public ModePaiement(String codePaiement, String libelleMode, String description) {
        this.codePaiement = codePaiement;
        this.libelleMode = libelleMode;
        this.description = description;
    }

    public ModePaiement(Long idModePaie, String codePaiement, String libelleMode, String description) {
        this.idModePaie = idModePaie;
        this.codePaiement = codePaiement;
        this.libelleMode = libelleMode;
        this.description = description;
    }

    public Long getIdModePaie() {
        return idModePaie;
    }

    public void setIdModePaie(Long idModePaie) {
        this.idModePaie = idModePaie;
    }

    public String getCodePaiement() {
        return codePaiement;
    }

    public void setCodePaiement(String codePaiement) {
        this.codePaiement = codePaiement;
    }

    public String getLibelleMode() {
        return libelleMode;
    }

    public void setLibelleMode(String libelleMode) {
        this.libelleMode = libelleMode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
