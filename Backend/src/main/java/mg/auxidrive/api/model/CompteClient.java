package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Comptes_clients")
public class CompteClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_compte_client")
    private Long idCompteClient;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure", unique = true)
    private Assure assure;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mot_de_passe", length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_compte")
    private StatutCompte statutCompte;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_derniere_connexion")
    private LocalDateTime dateDerniereConnexion;

    /** Date à laquelle le compte sera définitivement supprimé (délai de rétractation de 72 h). */
    @Column(name = "date_suppression_planifiee")
    private LocalDateTime dateSuppressionPlanifiee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private Role role;

    public Long getIdCompteClient() {
        return idCompteClient;
    }

    public void setIdCompteClient(Long idCompteClient) {
        this.idCompteClient = idCompteClient;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public StatutCompte getStatutCompte() {
        return statutCompte;
    }

    public void setStatutCompte(StatutCompte statutCompte) {
        this.statutCompte = statutCompte;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDerniereConnexion() {
        return dateDerniereConnexion;
    }

    public void setDateDerniereConnexion(LocalDateTime dateDerniereConnexion) {
        this.dateDerniereConnexion = dateDerniereConnexion;
    }

    public LocalDateTime getDateSuppressionPlanifiee() {
        return dateSuppressionPlanifiee;
    }

    public void setDateSuppressionPlanifiee(LocalDateTime dateSuppressionPlanifiee) {
        this.dateSuppressionPlanifiee = dateSuppressionPlanifiee;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
