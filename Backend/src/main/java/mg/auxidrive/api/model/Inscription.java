package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inscriptions")
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscription")
    private Long idInscription;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_assure", unique = true)
    private Assure assure;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_inscription")
    private StatutInscription statutInscription;

    @Column(name = "date_demande")
    private LocalDateTime dateDemande;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "motif_refus", length = 255)
    private String motifRefus;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"motDePasse", "role", "statutUtilisateur", "dateCreation", "derniereConnexion", "hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_agent")
    private Utilisateur agent;

    /** Identifiants légaux (CIN, NIF, CIF...) assemblés pour la liste/dossier agent, non persisté ici. */
    @Transient
    private AssureIdentifiantsLegaux identifiants;

    public AssureIdentifiantsLegaux getIdentifiants() {
        return identifiants;
    }

    public void setIdentifiants(AssureIdentifiantsLegaux identifiants) {
        this.identifiants = identifiants;
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

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("agentNom")
    public String getAgentNom() {
        if (agent == null) {
            return null;
        }
        if (agent.getNomUtil() != null && !agent.getNomUtil().isBlank()) {
            return agent.getNomUtil().trim();
        }
        if (agent.getEmail() != null && !agent.getEmail().isBlank()) {
            return agent.getEmail().trim();
        }
        return null;
    }
}
