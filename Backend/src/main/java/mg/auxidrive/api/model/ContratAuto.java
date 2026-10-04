package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "contratsAuto")
public class ContratAuto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idContratAuto")
    private Long idContratAuto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrat")
    private Contrat contrat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVehicule")
    private Vehicule vehicule;

    @Column(name = "dateAjout")
    private LocalDate dateAjout;

    public ContratAuto(Contrat contrat, Vehicule vehicule, LocalDate dateAjout) {
        this.contrat = contrat;
        this.vehicule = vehicule;
        this.dateAjout = dateAjout;
    }

    public ContratAuto(Long idContratAuto, Contrat contrat, Vehicule vehicule, LocalDate dateAjout) {
        this.idContratAuto = idContratAuto;
        this.contrat = contrat;
        this.vehicule = vehicule;
        this.dateAjout = dateAjout;
    }

    public Long getIdContratAuto() {
        return idContratAuto;
    }

    public void setIdContratAuto(Long idContratAuto) {
        this.idContratAuto = idContratAuto;
    }

    public Contrat getContrat() {
        return contrat;
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public LocalDate getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }
}
