package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Autos_code_tarif")
public class AutoCodeTarif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_code_auto")
    private Long idCodeAuto;

    @Column(name = "tarif_code", length = 15)
    private String tarifCode;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_code_taxe")
    private CodeTaxePolice codeTaxePolice;

    @Column(name = "tarif_libelle", length = 100)
    private String tarifLibelle;

    @Column(name = "avec_tacava")
    private Boolean avecTacava;

    @Column(name = "taux_te")
    private Double tauxTe;

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
