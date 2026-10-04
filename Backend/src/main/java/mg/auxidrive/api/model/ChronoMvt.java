package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Chronos_mvt")
public class ChronoMvt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_chrono")
    private Long idChrono;

    @Column(name = "annee_mvt")
    private Integer anneeMvt;

    @Column(name = "mois_mvt")
    private Integer moisMvt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mvt_type")
    private MvtType mvtType;

    @Column(name = "dernier_chrono")
    private Integer dernierChrono;

    @Column(name = "est_dernier")
    private Boolean estDernier;

    @Column(name = "est_cloture_exercice")
    private Boolean estClotureExercice;

    public Long getIdChrono() {
        return idChrono;
    }

    public void setIdChrono(Long idChrono) {
        this.idChrono = idChrono;
    }

    public Integer getAnneeMvt() {
        return anneeMvt;
    }

    public void setAnneeMvt(Integer anneeMvt) {
        this.anneeMvt = anneeMvt;
    }

    public Integer getMoisMvt() {
        return moisMvt;
    }

    public void setMoisMvt(Integer moisMvt) {
        this.moisMvt = moisMvt;
    }

    public MvtType getMvtType() {
        return mvtType;
    }

    public void setMvtType(MvtType mvtType) {
        this.mvtType = mvtType;
    }

    public Integer getDernierChrono() {
        return dernierChrono;
    }

    public void setDernierChrono(Integer dernierChrono) {
        this.dernierChrono = dernierChrono;
    }

    public Boolean getEstDernier() {
        return estDernier;
    }

    public void setEstDernier(Boolean estDernier) {
        this.estDernier = estDernier;
    }

    public Boolean getEstClotureExercice() {
        return estClotureExercice;
    }

    public void setEstClotureExercice(Boolean estClotureExercice) {
        this.estClotureExercice = estClotureExercice;
    }
}
