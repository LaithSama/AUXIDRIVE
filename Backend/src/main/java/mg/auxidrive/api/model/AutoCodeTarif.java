package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "autosCodeTarif")
public class AutoCodeTarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodeAuto")
    private Long idCodeAuto;

    @Column(name = "tarifCode", length = 15)
    private String tarifCode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCodeTaxe")
    private CodeTaxePolice codeTaxePolice;

    @Column(name = "tarifLibelle", length = 100)
    private String tarifLibelle;

    @Column(name = "avecTacava")
    private Boolean avecTacava;

    @Column(name = "tauxTe")
    private Double tauxTe;

    public AutoCodeTarif(String tarifCode, CodeTaxePolice codeTaxePolice, String tarifLibelle, Boolean avecTacava, Double tauxTe) {
        this.tarifCode = tarifCode;
        this.codeTaxePolice = codeTaxePolice;
        this.tarifLibelle = tarifLibelle;
        this.avecTacava = avecTacava;
        this.tauxTe = tauxTe;
    }

    public AutoCodeTarif(Long idCodeAuto, String tarifCode, CodeTaxePolice codeTaxePolice, String tarifLibelle, Boolean avecTacava, Double tauxTe) {
        this.idCodeAuto = idCodeAuto;
        this.tarifCode = tarifCode;
        this.codeTaxePolice = codeTaxePolice;
        this.tarifLibelle = tarifLibelle;
        this.avecTacava = avecTacava;
        this.tauxTe = tauxTe;
    }

    public Long getIdCodeAuto() {
        return idCodeAuto;
    }

    public void setIdCodeAuto(Long idCodeAuto) {
        this.idCodeAuto = idCodeAuto;
    }

    public String getTarifCode() {
        return tarifCode;
    }

    public void setTarifCode(String tarifCode) {
        this.tarifCode = tarifCode;
    }

    public CodeTaxePolice getCodeTaxePolice() {
        return codeTaxePolice;
    }

    public void setCodeTaxePolice(CodeTaxePolice codeTaxePolice) {
        this.codeTaxePolice = codeTaxePolice;
    }

    public String getTarifLibelle() {
        return tarifLibelle;
    }

    public void setTarifLibelle(String tarifLibelle) {
        this.tarifLibelle = tarifLibelle;
    }

    public Boolean getAvecTacava() {
        return avecTacava;
    }

    public void setAvecTacava(Boolean avecTacava) {
        this.avecTacava = avecTacava;
    }

    public Double getTauxTe() {
        return tauxTe;
    }

    public void setTauxTe(Double tauxTe) {
        this.tauxTe = tauxTe;
    }
}
