package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Assures_identifiants_legaux")
public class AssureIdentifiantsLegaux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_legaux")
    private Long idLegaux;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure", unique = true)
    private Assure assure;

    @Column(name = "cin", length = 20)
    private String cin;

    @Column(name = "nif", length = 20)
    private String nif;

    @Column(name = "cif", length = 30)
    private String cif;

    @Column(name = "num_passeport", length = 30)
    private String numPasseport;

    @Column(name = "n_carte_prof", length = 30)
    private String nCarteProf;

    @Column(name = "date_nif")
    private LocalDate dateNif;

    @Column(name = "date_cif")
    private LocalDate dateCif;

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

    public String getNCarteProf() {
        return nCarteProf;
    }

    public void setNCarteProf(String nCarteProf) {
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
