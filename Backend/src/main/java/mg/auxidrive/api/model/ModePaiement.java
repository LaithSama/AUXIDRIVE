package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Modes_paiement")
public class ModePaiement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mode_paie")
    private Long idModePaie;

    @Column(name = "code_paiement", length = 10)
    private String codePaiement;

    @Column(name = "libelle_mode", length = 30)
    private String libelleMode;

    @Column(name = "description", length = 100)
    private String description;

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
