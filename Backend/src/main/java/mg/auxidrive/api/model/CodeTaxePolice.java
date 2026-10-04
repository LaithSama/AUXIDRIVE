package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Codes_taxe_police")
public class CodeTaxePolice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_code_taxe")
    private Long idCodeTaxe;

    @Column(name = "code_taxe", length = 1)
    private String codeTaxe;

    @Column(name = "libelle_code_taxe", length = 50)
    private String libelleCodeTaxe;

    @Column(name = "taux_taxe")
    private Double tauxTaxe;

    @Column(name = "appliquer_tva")
    private Boolean appliquerTva;

    @Column(name = "appliquer_te")
    private Boolean appliquerTe;

    @Column(name = "appliquer_tacava")
    private Boolean appliquerTacava;

    @Column(name = "taux_te")
    private Double tauxTe;

    @Column(name = "taux_tacava")
    private Double tauxTacava;

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
