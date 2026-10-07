package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Sinistres")
public class Sinistre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sinistre")
    private Long idSinistre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private Contrat contrat;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehicule")
    private Vehicule vehicule;

    @Column(name = "date_sinistre")
    private LocalDate dateSinistre;

    @Column(name = "date_declaration")
    private LocalDate dateDeclaration;

    @Column(name = "lieu_sinistre", length = 100)
    private String lieuSinistre;

    @Column(name = "circonstances", length = 255)
    private String circonstances;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_sinistre", length = 30)
    private StatutSinistre statutSinistre;

    @Column(name = "num_rapport_police", length = 30)
    private String numRapportPolice;

    @Column(name = "date_rapport_police")
    private LocalDate dateRapportPolice;

    @Column(name = "rapport_policier", columnDefinition = "text")
    private String rapportPolicier;

    @Column(name = "responsabilite_assure")
    private Boolean responsabiliteAssure;

    @Column(name = "montant_dommages_estime")
    private Double montantDommagesEstime;

    @Column(name = "montant_indemnisation")
    private Double montantIndemnisation;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_util")
    private Utilisateur utilisateur;

    @Column(name = "date_cloture")
    private LocalDate dateCloture;

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

    public StatutSinistre getStatutSinistre() {
        return statutSinistre;
    }

    public void setStatutSinistre(StatutSinistre statutSinistre) {
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

    /* Vues enrichies pour l'agent : les FK sont ignorées, on expose l'essentiel. */

    @JsonProperty("idContrat")
    public Long getIdContrat() {
        return contrat == null ? null : contrat.getIdContrat();
    }

    @JsonProperty("idVehicule")
    public Long getIdVehicule() {
        return vehicule == null ? null : vehicule.getIdVehicule();
    }

    @JsonProperty("numPolice")
    public String getNumPolice() {
        return contrat == null ? null : contrat.getNumPolice();
    }

    @JsonProperty("nomAssure")
    public String getNomAssure() {
        return contrat == null || contrat.getAssure() == null ? null : contrat.getAssure().getNomAssure();
    }

    @JsonProperty("prenomAssure")
    public String getPrenomAssure() {
        return contrat == null || contrat.getAssure() == null ? null : contrat.getAssure().getPrenomAssure();
    }
}
