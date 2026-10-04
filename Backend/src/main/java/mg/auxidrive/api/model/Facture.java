package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idFacture")
    private Long idFacture;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrat")
    private Contrat contrat;

    @Enumerated(EnumType.STRING)
    @Column(name = "typeFacture")
    private TypeFacture typeFacture;

    @Column(name = "primeNette")
    private Double primeNette;

    @Column(name = "montantTaxes")
    private Double montantTaxes;

    @Column(name = "montantTtc")
    private Double montantTtc;

    @Column(name = "dateEmission")
    private LocalDate dateEmission;

    @Column(name = "cheminFichierPdf", length = 100)
    private String cheminFichierPdf;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutFacture")
    private StatutFacture statutFacture;

    public Facture(Contrat contrat, TypeFacture typeFacture, Double primeNette, Double montantTaxes, Double montantTtc, LocalDate dateEmission, String cheminFichierPdf, StatutFacture statutFacture) {
        this.contrat = contrat;
        this.typeFacture = typeFacture;
        this.primeNette = primeNette;
        this.montantTaxes = montantTaxes;
        this.montantTtc = montantTtc;
        this.dateEmission = dateEmission;
        this.cheminFichierPdf = cheminFichierPdf;
        this.statutFacture = statutFacture;
    }

    public Facture(Long idFacture, Contrat contrat, TypeFacture typeFacture, Double primeNette, Double montantTaxes, Double montantTtc, LocalDate dateEmission, String cheminFichierPdf, StatutFacture statutFacture) {
        this.idFacture = idFacture;
        this.contrat = contrat;
        this.typeFacture = typeFacture;
        this.primeNette = primeNette;
        this.montantTaxes = montantTaxes;
        this.montantTtc = montantTtc;
        this.dateEmission = dateEmission;
        this.cheminFichierPdf = cheminFichierPdf;
        this.statutFacture = statutFacture;
    }

    public Long getIdFacture() {
        return idFacture;
    }

    public void setIdFacture(Long idFacture) {
        this.idFacture = idFacture;
    }

    public Contrat getContrat() {
        return contrat;
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
    }

    public TypeFacture getTypeFacture() {
        return typeFacture;
    }

    public void setTypeFacture(TypeFacture typeFacture) {
        this.typeFacture = typeFacture;
    }

    public Double getPrimeNette() {
        return primeNette;
    }

    public void setPrimeNette(Double primeNette) {
        this.primeNette = primeNette;
    }

    public Double getMontantTaxes() {
        return montantTaxes;
    }

    public void setMontantTaxes(Double montantTaxes) {
        this.montantTaxes = montantTaxes;
    }

    public Double getMontantTtc() {
        return montantTtc;
    }

    public void setMontantTtc(Double montantTtc) {
        this.montantTtc = montantTtc;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public String getCheminFichierPdf() {
        return cheminFichierPdf;
    }

    public void setCheminFichierPdf(String cheminFichierPdf) {
        this.cheminFichierPdf = cheminFichierPdf;
    }

    public StatutFacture getStatutFacture() {
        return statutFacture;
    }

    public void setStatutFacture(StatutFacture statutFacture) {
        this.statutFacture = statutFacture;
    }
}
