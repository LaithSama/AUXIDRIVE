package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Factures")
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facture")
    private Long idFacture;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private Contrat contrat;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_facture")
    private TypeFacture typeFacture;

    @Column(name = "prime_nette")
    private Double primeNette;

    @Column(name = "montant_taxes")
    private Double montantTaxes;

    @Column(name = "montant_ttc")
    private Double montantTtc;

    @Column(name = "date_emission")
    private LocalDate dateEmission;

    @Column(name = "chemin_fichier_pdf", length = 100)
    private String cheminFichierPdf;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_facture")
    private StatutFacture statutFacture;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_facture_liee")
    private Facture factureLiee;

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

    public Facture getFactureLiee() {
        return factureLiee;
    }

    public void setFactureLiee(Facture factureLiee) {
        this.factureLiee = factureLiee;
    }
}
