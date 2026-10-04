package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "assuresIdentifiantsLegaux")
public class AssureIdentifiantsLegaux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idLegaux")
    private Long idLegaux;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure", unique = true)
    private Assure assure;

    @Column(name = "cin", length = 20)
    private String cin;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "cif", length = 30)
    private String cif;

    @Column(name = "numPasseport", length = 30)
    private String numPasseport;

    @Column(name = "nCarteProf", length = 30)
    private String nCarteProf;

    @Column(name = "dateNif")
    private LocalDate dateNif;

    @Column(name = "dateCif")
    private LocalDate dateCif;

    public AssureIdentifiantsLegaux(LocalDate dateCif, LocalDate dateNif, String nCarteProf, String numPasseport, String cif, String nif, String cin, Assure assure) {
        this.dateCif = dateCif;
        this.dateNif = dateNif;
        this.nCarteProf = nCarteProf;
        this.numPasseport = numPasseport;
        this.cif = cif;
        this.nif = nif;
        this.cin = cin;
        this.assure = assure;
    }

    public AssureIdentifiantsLegaux(Long idLegaux, Assure assure, String cin, String nif, String cif, String numPasseport, String nCarteProf, LocalDate dateNif, LocalDate dateCif) {
        this.idLegaux = idLegaux;
        this.assure = assure;
        this.cin = cin;
        this.nif = nif;
        this.cif = cif;
        this.numPasseport = numPasseport;
        this.nCarteProf = nCarteProf;
        this.dateNif = dateNif;
        this.dateCif = dateCif;
    }

    public Long getIdLegaux() {
        return idLegaux;
    }

    public void setIdLegaux(Long idLegaux) {
        this.idLegaux = idLegaux;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getNumPasseport() {
        return numPasseport;
    }

    public void setNumPasseport(String numPasseport) {
        this.numPasseport = numPasseport;
    }

    public String getnCarteProf() {
        return nCarteProf;
    }

    public void setnCarteProf(String nCarteProf) {
        this.nCarteProf = nCarteProf;
    }

    public LocalDate getDateNif() {
        return dateNif;
    }

    public void setDateNif(LocalDate dateNif) {
        this.dateNif = dateNif;
    }

    public LocalDate getDateCif() {
        return dateCif;
    }

    public void setDateCif(LocalDate dateCif) {
        this.dateCif = dateCif;
    }
}
