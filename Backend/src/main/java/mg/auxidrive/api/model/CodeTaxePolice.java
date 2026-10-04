package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "codesTaxePolice")
public class CodeTaxePolice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCodeTaxe")
    private Long idCodeTaxe;

    @Column(name = "codeTaxe", length = 1)
    private String codeTaxe;

    @Column(name = "libelleCodeTaxe", length = 50)
    private String libelleCodeTaxe;

    @Column(name = "tauxTaxe")
    private Double tauxTaxe;

    @Column(name = "appliquerTva")
    private Boolean appliquerTva;

    @Column(name = "appliquerTe")
    private Boolean appliquerTe;

    @Column(name = "appliquerTacava")
    private Boolean appliquerTacava;

    @Column(name = "tauxTe")
    private Double tauxTe;

    @Column(name = "tauxTacava")
    private Double tauxTacava;

    public CodeTaxePolice(String codeTaxe, String libelleCodeTaxe, Double tauxTaxe, Boolean appliquerTva, Boolean appliquerTe, Boolean appliquerTacava, Double tauxTe, Double tauxTacava) {
        this.codeTaxe = codeTaxe;
        this.libelleCodeTaxe = libelleCodeTaxe;
        this.tauxTaxe = tauxTaxe;
        this.appliquerTva = appliquerTva;
        this.appliquerTe = appliquerTe;
        this.appliquerTacava = appliquerTacava;
        this.tauxTe = tauxTe;
        this.tauxTacava = tauxTacava;
    }

    public CodeTaxePolice(Long idCodeTaxe, String codeTaxe, String libelleCodeTaxe, Double tauxTaxe, Boolean appliquerTva, Boolean appliquerTe, Boolean appliquerTacava, Double tauxTe, Double tauxTacava) {
        this.idCodeTaxe = idCodeTaxe;
        this.codeTaxe = codeTaxe;
        this.libelleCodeTaxe = libelleCodeTaxe;
        this.tauxTaxe = tauxTaxe;
        this.appliquerTva = appliquerTva;
        this.appliquerTe = appliquerTe;
        this.appliquerTacava = appliquerTacava;
        this.tauxTe = tauxTe;
        this.tauxTacava = tauxTacava;
    }

    public Long getIdCodeTaxe() {
        return idCodeTaxe;
    }

    public void setIdCodeTaxe(Long idCodeTaxe) {
        this.idCodeTaxe = idCodeTaxe;
    }

    public String getCodeTaxe() {
        return codeTaxe;
    }

    public void setCodeTaxe(String codeTaxe) {
        this.codeTaxe = codeTaxe;
    }

    public String getLibelleCodeTaxe() {
        return libelleCodeTaxe;
    }

    public void setLibelleCodeTaxe(String libelleCodeTaxe) {
        this.libelleCodeTaxe = libelleCodeTaxe;
    }

    public Double getTauxTaxe() {
        return tauxTaxe;
    }

    public void setTauxTaxe(Double tauxTaxe) {
        this.tauxTaxe = tauxTaxe;
    }

    public Boolean getAppliquerTva() {
        return appliquerTva;
    }

    public void setAppliquerTva(Boolean appliquerTva) {
        this.appliquerTva = appliquerTva;
    }

    public Boolean getAppliquerTe() {
        return appliquerTe;
    }

    public void setAppliquerTe(Boolean appliquerTe) {
        this.appliquerTe = appliquerTe;
    }

    public Boolean getAppliquerTacava() {
        return appliquerTacava;
    }

    public void setAppliquerTacava(Boolean appliquerTacava) {
        this.appliquerTacava = appliquerTacava;
    }

    public Double getTauxTe() {
        return tauxTe;
    }

    public void setTauxTe(Double tauxTe) {
        this.tauxTe = tauxTe;
    }

    public Double getTauxTacava() {
        return tauxTacava;
    }

    public void setTauxTacava(Double tauxTacava) {
        this.tauxTacava = tauxTacava;
    }
}
