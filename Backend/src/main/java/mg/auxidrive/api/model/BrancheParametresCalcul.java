package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Branches_parametres_calcul")
public class BrancheParametresCalcul {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_param")
    private Long idParam;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cd_branche")
    private Branche branche;

    @Column(name = "accessoires")
    private Double accessoires;

    @Column(name = "taux_te")
    private Double tauxTe;

    @Column(name = "taux_tva")
    private Double tauxTva;

    @Column(name = "taux_tacava")
    private Double tauxTacava;

    @Column(name = "commission_totale")
    private Double commissionTotale;

    @Column(name = "comm_app")
    private Double commApp;

    @Column(name = "comm_ges")
    private Double commGes;

    @Column(name = "ventilation_be", length = 50)
    private String ventilationBe;

    @Column(name = "ventilation_ca", length = 50)
    private String ventilationCa;

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

    public Double getTauxTacava() {
        return tauxTacava;
    }

    public void setTauxTacava(Double tauxTacava) {
        this.tauxTacava = tauxTacava;
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
