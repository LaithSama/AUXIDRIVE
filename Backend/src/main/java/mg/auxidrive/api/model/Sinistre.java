package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "sinistres")
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSinistre")
    private Long idSinistre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrat")
    private Contrat contrat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVehicule")
    private Vehicule vehicule;

    @Column(name = "dateSinistre")
    private LocalDate dateSinistre;

    @Column(name = "dateDeclaration")
    private LocalDate dateDeclaration;

    @Column(name = "lieuSinistre", length = 100)
    private String lieuSinistre;

    @Column(name = "circonstances", length = 255)
    private String circonstances;

    @Column(name = "statutSinistre", length = 30)
    private String statutSinistre;

    @Column(name = "numRapportPolice", length = 30)
    private String numRapportPolice;

    @Column(name = "dateRapportPolice")
    private LocalDate dateRapportPolice;

    @Column(name = "rapportPolicier", columnDefinition = "text")
    private String rapportPolicier;

    @Column(name = "responsabiliteAssure")
    private Boolean responsabiliteAssure;

    @Column(name = "montantDommagesEstime")
    private Double montantDommagesEstime;

    @Column(name = "montantIndemnisation")
    private Double montantIndemnisation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtil")
    private Utilisateur utilisateur;

    @Column(name = "dateCloture")
    private LocalDate dateCloture;

    public Sinistre(Contrat contrat, Vehicule vehicule, LocalDate dateSinistre, LocalDate dateDeclaration, String lieuSinistre, String circonstances, String statutSinistre, String numRapportPolice, LocalDate dateRapportPolice, String rapportPolicier, Boolean responsabiliteAssure, Double montantDommagesEstime, Double montantIndemnisation, Utilisateur utilisateur, LocalDate dateCloture) {
        this.contrat = contrat;
        this.vehicule = vehicule;
        this.dateSinistre = dateSinistre;
        this.dateDeclaration = dateDeclaration;
        this.lieuSinistre = lieuSinistre;
        this.circonstances = circonstances;
        this.statutSinistre = statutSinistre;
        this.numRapportPolice = numRapportPolice;
        this.dateRapportPolice = dateRapportPolice;
        this.rapportPolicier = rapportPolicier;
        this.responsabiliteAssure = responsabiliteAssure;
        this.montantDommagesEstime = montantDommagesEstime;
        this.montantIndemnisation = montantIndemnisation;
        this.utilisateur = utilisateur;
        this.dateCloture = dateCloture;
    }

    public Sinistre(Long idSinistre, Contrat contrat, Vehicule vehicule, LocalDate dateSinistre, LocalDate dateDeclaration, String lieuSinistre, String circonstances, String statutSinistre, String numRapportPolice, LocalDate dateRapportPolice, String rapportPolicier, Boolean responsabiliteAssure, Double montantDommagesEstime, Double montantIndemnisation, Utilisateur utilisateur, LocalDate dateCloture) {
        this.idSinistre = idSinistre;
        this.contrat = contrat;
        this.vehicule = vehicule;
        this.dateSinistre = dateSinistre;
        this.dateDeclaration = dateDeclaration;
        this.lieuSinistre = lieuSinistre;
        this.circonstances = circonstances;
        this.statutSinistre = statutSinistre;
        this.numRapportPolice = numRapportPolice;
        this.dateRapportPolice = dateRapportPolice;
        this.rapportPolicier = rapportPolicier;
        this.responsabiliteAssure = responsabiliteAssure;
        this.montantDommagesEstime = montantDommagesEstime;
        this.montantIndemnisation = montantIndemnisation;
        this.utilisateur = utilisateur;
        this.dateCloture = dateCloture;
    }

    public Long getIdSinistre() {
        return idSinistre;
    }

    public void setIdSinistre(Long idSinistre) {
        this.idSinistre = idSinistre;
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

    public LocalDate getDateSinistre() {
        return dateSinistre;
    }

    public void setDateSinistre(LocalDate dateSinistre) {
        this.dateSinistre = dateSinistre;
    }

    public LocalDate getDateDeclaration() {
        return dateDeclaration;
    }

    public void setDateDeclaration(LocalDate dateDeclaration) {
        this.dateDeclaration = dateDeclaration;
    }

    public String getLieuSinistre() {
        return lieuSinistre;
    }

    public void setLieuSinistre(String lieuSinistre) {
        this.lieuSinistre = lieuSinistre;
    }

    public String getCirconstances() {
        return circonstances;
    }

    public void setCirconstances(String circonstances) {
        this.circonstances = circonstances;
    }

    public String getStatutSinistre() {
        return statutSinistre;
    }

    public void setStatutSinistre(String statutSinistre) {
        this.statutSinistre = statutSinistre;
    }

    public String getNumRapportPolice() {
        return numRapportPolice;
    }

    public void setNumRapportPolice(String numRapportPolice) {
        this.numRapportPolice = numRapportPolice;
    }

    public LocalDate getDateRapportPolice() {
        return dateRapportPolice;
    }

    public void setDateRapportPolice(LocalDate dateRapportPolice) {
        this.dateRapportPolice = dateRapportPolice;
    }

    public String getRapportPolicier() {
        return rapportPolicier;
    }

    public void setRapportPolicier(String rapportPolicier) {
        this.rapportPolicier = rapportPolicier;
    }

    public Boolean getResponsabiliteAssure() {
        return responsabiliteAssure;
    }

    public void setResponsabiliteAssure(Boolean responsabiliteAssure) {
        this.responsabiliteAssure = responsabiliteAssure;
    }

    public Double getMontantDommagesEstime() {
        return montantDommagesEstime;
    }

    public void setMontantDommagesEstime(Double montantDommagesEstime) {
        this.montantDommagesEstime = montantDommagesEstime;
    }

    public Double getMontantIndemnisation() {
        return montantIndemnisation;
    }

    public void setMontantIndemnisation(Double montantIndemnisation) {
        this.montantIndemnisation = montantIndemnisation;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDate getDateCloture() {
        return dateCloture;
    }

    public void setDateCloture(LocalDate dateCloture) {
        this.dateCloture = dateCloture;
    }
}
