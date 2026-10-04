package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comptesClients")
public class CompteClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idCompteClient")
    private Long idCompteClient;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure", unique = true)
    private Assure assure;

    @Column(name = "email", length = 50, unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "motDePasse", length = 255)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutCompte")
    private StatutCompte statutCompte;

    @Column(name = "dateCreation")
    private LocalDateTime dateCreation;

    @Column(name = "dateDerniereConnexion")
    private LocalDateTime dateDerniereConnexion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "idRole")
    private Role role;

    public CompteClient(Assure assure, String email, String motDePasse, StatutCompte statutCompte, LocalDateTime dateCreation, LocalDateTime dateDerniereConnexion, Role role) {
        this.assure = assure;
        this.email = email;
        this.motDePasse = motDePasse;
        this.statutCompte = statutCompte;
        this.dateCreation = dateCreation;
        this.dateDerniereConnexion = dateDerniereConnexion;
        this.role = role;
    }

    public CompteClient(Long idCompteClient, Assure assure, String email, String motDePasse, StatutCompte statutCompte, LocalDateTime dateCreation, LocalDateTime dateDerniereConnexion, Role role) {
        this.idCompteClient = idCompteClient;
        this.assure = assure;
        this.email = email;
        this.motDePasse = motDePasse;
        this.statutCompte = statutCompte;
        this.dateCreation = dateCreation;
        this.dateDerniereConnexion = dateDerniereConnexion;
        this.role = role;
    }

    public CompteClient() {

    }

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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
