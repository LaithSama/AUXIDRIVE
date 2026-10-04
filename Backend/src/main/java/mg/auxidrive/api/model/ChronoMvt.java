package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "Chronos_mvt")
public class ChronoMvt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idChrono")
    private Long idChrono;

    @Column(name = "anneeMvt")
    private Integer anneeMvt;

    @Column(name = "moisMvt")
    private Integer moisMvt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idMvtType")
    private MvtType mvtType;

    @Column(name = "dernierChrono")
    private Integer dernierChrono;

    @Column(name = "estDernier")
    private Boolean estDernier;

    @Column(name = "estClotureExercice")
    private Boolean estClotureExercice;

    public ChronoMvt(Integer anneeMvt, Integer moisMvt, MvtType mvtType, Integer dernierChrono, Boolean estDernier, Boolean estClotureExercice) {
        this.anneeMvt = anneeMvt;
        this.moisMvt = moisMvt;
        this.mvtType = mvtType;
        this.dernierChrono = dernierChrono;
        this.estDernier = estDernier;
        this.estClotureExercice = estClotureExercice;
    }

    public ChronoMvt(Long idChrono, Integer anneeMvt, Integer moisMvt, MvtType mvtType, Integer dernierChrono, Boolean estDernier, Boolean estClotureExercice) {
        this.idChrono = idChrono;
        this.anneeMvt = anneeMvt;
        this.moisMvt = moisMvt;
        this.mvtType = mvtType;
        this.dernierChrono = dernierChrono;
        this.estDernier = estDernier;
        this.estClotureExercice = estClotureExercice;
    }

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
