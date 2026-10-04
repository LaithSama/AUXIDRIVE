package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inscriptions")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idInscription")
    private Long idInscription;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idAssure", unique = true)
    private Assure assure;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutInscription")
    private StatutInscription statutInscription;

    @Column(name = "dateDemande")
    private LocalDateTime dateDemande;

    @Column(name = "dateDecision")
    private LocalDateTime dateDecision;

    @Column(name = "motifRefus", length = 255)
    private String motifRefus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtil")
    private Utilisateur agent;

    public Inscription(Assure assure, StatutInscription statutInscription, LocalDateTime dateDemande, LocalDateTime dateDecision, String motifRefus, Utilisateur agent) {
        this.assure = assure;
        this.statutInscription = statutInscription;
        this.dateDemande = dateDemande;
        this.dateDecision = dateDecision;
        this.motifRefus = motifRefus;
        this.agent = agent;
    }

    public Inscription(Long idInscription, Assure assure, StatutInscription statutInscription, LocalDateTime dateDemande, LocalDateTime dateDecision, String motifRefus, Utilisateur agent) {
        this.idInscription = idInscription;
        this.assure = assure;
        this.statutInscription = statutInscription;
        this.dateDemande = dateDemande;
        this.dateDecision = dateDecision;
        this.motifRefus = motifRefus;
        this.agent = agent;
    }

    public Inscription() {

    }

    public Long getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(Long idInscription) {
        this.idInscription = idInscription;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public StatutInscription getStatutInscription() {
        return statutInscription;
    }

    public void setStatutInscription(StatutInscription statutInscription) {
        this.statutInscription = statutInscription;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDateTime getDateDecision() {
        return dateDecision;
    }

    public void setDateDecision(LocalDateTime dateDecision) {
        this.dateDecision = dateDecision;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    public void setMotifRefus(String motifRefus) {
        this.motifRefus = motifRefus;
    }

    public Utilisateur getAgent() {
        return agent;
    }

    public void setAgent(Utilisateur agent) {
        this.agent = agent;
    }
}
