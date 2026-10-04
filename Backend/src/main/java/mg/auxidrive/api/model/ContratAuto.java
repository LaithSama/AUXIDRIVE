package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Contrats_auto")
public class ContratAuto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat_auto")
    private Long idContratAuto;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private Contrat contrat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehicule")
    private Vehicule vehicule;

    @Column(name = "date_ajout")
    private LocalDate dateAjout;

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
