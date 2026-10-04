package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Contrats")
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Long idContrat;

    @Column(name = "num_police", length = 10)
    private String numPolice;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure")
    private Assure assure;

    @Column(name = "code_agence", length = 2)
    private String codeAgence;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre")
    private Offre offre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cd_branche")
    private Branche branche;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_de_rnvlt")
    private ModeRenouvellement modeRenouvellement;

    @Column(name = "date_souscription")
    private LocalDate dateSouscription;

    @Column(name = "date_effet")
    private LocalDate dateEffet;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "statut_contrat", length = 20)
    private String statutContrat;

    @Column(name = "coefficient_bonus_malus")
    private Double coefficientBonusMalus;

    @Column(name = "prime_nette_courante")
    private Double primeNetteCourante;

    @Column(name = "prime_ttc_courante")
    private Double primeTtcCourante;

    @Column(name = "canal_souscription", length = 15)
    private String canalSouscription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_util")
    private Utilisateur utilisateur;

    public Long getIdContrat() {
        return idContrat;
    }

    public void setIdContrat(Long idContrat) {
        this.idContrat = idContrat;
    }

    public String getNumPolice() {
        return numPolice;
    }

    public void setNumPolice(String numPolice) {
        this.numPolice = numPolice;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public Offre getOffre() {
        return offre;
    }

    public void setOffre(Offre offre) {
        this.offre = offre;
    }

    public Branche getBranche() {
        return branche;
    }

    public void setBranche(Branche branche) {
        this.branche = branche;
    }

    public ModeRenouvellement getModeRenouvellement() {
        return modeRenouvellement;
    }

    public void setModeRenouvellement(ModeRenouvellement modeRenouvellement) {
        this.modeRenouvellement = modeRenouvellement;
    }

    public LocalDate getDateSouscription() {
        return dateSouscription;
    }

    public void setDateSouscription(LocalDate dateSouscription) {
        this.dateSouscription = dateSouscription;
    }

    public LocalDate getDateEffet() {
        return dateEffet;
    }

    public void setDateEffet(LocalDate dateEffet) {
        this.dateEffet = dateEffet;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(LocalDate dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public String getStatutContrat() {
        return statutContrat;
    }

    public void setStatutContrat(String statutContrat) {
        this.statutContrat = statutContrat;
    }

    public Double getCoefficientBonusMalus() {
        return coefficientBonusMalus;
    }

    public void setCoefficientBonusMalus(Double coefficientBonusMalus) {
        this.coefficientBonusMalus = coefficientBonusMalus;
    }

    public Double getPrimeNetteCourante() {
        return primeNetteCourante;
    }

    public void setPrimeNetteCourante(Double primeNetteCourante) {
        this.primeNetteCourante = primeNetteCourante;
    }

    public Double getPrimeTtcCourante() {
        return primeTtcCourante;
    }

    public void setPrimeTtcCourante(Double primeTtcCourante) {
        this.primeTtcCourante = primeTtcCourante;
    }

    public String getCanalSouscription() {
        return canalSouscription;
    }

    public void setCanalSouscription(String canalSouscription) {
        this.canalSouscription = canalSouscription;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
