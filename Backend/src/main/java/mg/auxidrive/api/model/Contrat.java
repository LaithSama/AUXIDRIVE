package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "contrats")
public class Contrat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idContrat")
    private Long idContrat;

    @Column(name = "numPolice", length = 10)
    private String numPolice;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure")
    private Assure assure;

    @Column(name = "codeAgence", length = 2)
    private String codeAgence;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idOffre")
    private Offre offre;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cdBranche")
    private Branche branche;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_de_rnvlt")
    private ModeRenouvellement modeRenouvellement;

    @Column(name = "dateSouscription")
    private LocalDate dateSouscription;

    @Column(name = "dateEffet")
    private LocalDate dateEffet;

    @Column(name = "dateExpiration")
    private LocalDate dateExpiration;

    @Column(name = "statutContrat", length = 20)
    private String statutContrat;

    @Column(name = "coefficientBonusMalus")
    private Double coefficientBonusMalus;

    @Column(name = "primeNetteCourante")
    private Double primeNetteCourante;

    @Column(name = "primeTtcCourante")
    private Double primeTtcCourante;

    @Column(name = "canalSouscription", length = 15)
    private String canalSouscription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtil")
    private Utilisateur utilisateur;

    public Contrat(String numPolice, Assure assure, String codeAgence, Offre offre, Branche branche, ModeRenouvellement modeRenouvellement, LocalDate dateSouscription, LocalDate dateEffet, LocalDate dateExpiration, String statutContrat, Double coefficientBonusMalus, Double primeNetteCourante, Double primeTtcCourante, String canalSouscription, Utilisateur utilisateur) {
        this.numPolice = numPolice;
        this.assure = assure;
        this.codeAgence = codeAgence;
        this.offre = offre;
        this.branche = branche;
        this.modeRenouvellement = modeRenouvellement;
        this.dateSouscription = dateSouscription;
        this.dateEffet = dateEffet;
        this.dateExpiration = dateExpiration;
        this.statutContrat = statutContrat;
        this.coefficientBonusMalus = coefficientBonusMalus;
        this.primeNetteCourante = primeNetteCourante;
        this.primeTtcCourante = primeTtcCourante;
        this.canalSouscription = canalSouscription;
        this.utilisateur = utilisateur;
    }

    public Contrat(Long idContrat, String numPolice, Assure assure, String codeAgence, Offre offre, Branche branche, ModeRenouvellement modeRenouvellement, LocalDate dateSouscription, LocalDate dateEffet, LocalDate dateExpiration, String statutContrat, Double coefficientBonusMalus, Double primeNetteCourante, Double primeTtcCourante, String canalSouscription, Utilisateur utilisateur) {
        this.idContrat = idContrat;
        this.numPolice = numPolice;
        this.assure = assure;
        this.codeAgence = codeAgence;
        this.offre = offre;
        this.branche = branche;
        this.modeRenouvellement = modeRenouvellement;
        this.dateSouscription = dateSouscription;
        this.dateEffet = dateEffet;
        this.dateExpiration = dateExpiration;
        this.statutContrat = statutContrat;
        this.coefficientBonusMalus = coefficientBonusMalus;
        this.primeNetteCourante = primeNetteCourante;
        this.primeTtcCourante = primeTtcCourante;
        this.canalSouscription = canalSouscription;
        this.utilisateur = utilisateur;
    }

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
