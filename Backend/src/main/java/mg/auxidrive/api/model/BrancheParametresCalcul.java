package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "branchesParametresCalcul")
public class BrancheParametresCalcul {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idParam")
    private Long idParam;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdBranche")
    private Branche branche;

    @Column(name = "accessoires")
    private Double accessoires;

    @Column(name = "tauxTe")
    private Double tauxTe;

    @Column(name = "tauxTva")
    private Double tauxTva;

    @Column(name = "commissionTotale")
    private Double commissionTotale;

    @Column(name = "commApp")
    private Double commApp;

    @Column(name = "commGes")
    private Double commGes;

    @Column(name = "ventilationBe", length = 50)
    private String ventilationBe;

    @Column(name = "ventilationCa", length = 50)
    private String ventilationCa;

    public BrancheParametresCalcul(Branche branche, Double accessoires, Double tauxTe, Double tauxTva, Double commissionTotale, Double commApp, Double commGes, String ventilationBe, String ventilationCa) {
        this.branche = branche;
        this.accessoires = accessoires;
        this.tauxTe = tauxTe;
        this.tauxTva = tauxTva;
        this.commissionTotale = commissionTotale;
        this.commApp = commApp;
        this.commGes = commGes;
        this.ventilationBe = ventilationBe;
        this.ventilationCa = ventilationCa;
    }

    public BrancheParametresCalcul(Long idParam, Branche branche, Double accessoires, Double tauxTe, Double tauxTva, Double commissionTotale, Double commApp, Double commGes, String ventilationBe, String ventilationCa) {
        this.idParam = idParam;
        this.branche = branche;
        this.accessoires = accessoires;
        this.tauxTe = tauxTe;
        this.tauxTva = tauxTva;
        this.commissionTotale = commissionTotale;
        this.commApp = commApp;
        this.commGes = commGes;
        this.ventilationBe = ventilationBe;
        this.ventilationCa = ventilationCa;
    }

    public Long getIdParam() {
        return idParam;
    }

    public void setIdParam(Long idParam) {
        this.idParam = idParam;
    }

    public Branche getBranche() {
        return branche;
    }

    public void setBranche(Branche branche) {
        this.branche = branche;
    }

    public Double getAccessoires() {
        return accessoires;
    }

    public void setAccessoires(Double accessoires) {
        this.accessoires = accessoires;
    }

    public Double getTauxTe() {
        return tauxTe;
    }

    public void setTauxTe(Double tauxTe) {
        this.tauxTe = tauxTe;
    }

    public Double getTauxTva() {
        return tauxTva;
    }

    public void setTauxTva(Double tauxTva) {
        this.tauxTva = tauxTva;
    }

    public Double getCommissionTotale() {
        return commissionTotale;
    }

    public void setCommissionTotale(Double commissionTotale) {
        this.commissionTotale = commissionTotale;
    }

    public Double getCommApp() {
        return commApp;
    }

    public void setCommApp(Double commApp) {
        this.commApp = commApp;
    }

    public Double getCommGes() {
        return commGes;
    }

    public void setCommGes(Double commGes) {
        this.commGes = commGes;
    }

    public String getVentilationBe() {
        return ventilationBe;
    }

    public void setVentilationBe(String ventilationBe) {
        this.ventilationBe = ventilationBe;
    }

    public String getVentilationCa() {
        return ventilationCa;
    }

    public void setVentilationCa(String ventilationCa) {
        this.ventilationCa = ventilationCa;
    }
}
