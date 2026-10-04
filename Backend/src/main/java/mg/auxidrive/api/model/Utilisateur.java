package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_util")
    private Long idUtil;

    @Column(name = "email", length = 100, unique = true)
    private String email;

    @Column(name = "nom_util", length = 40)
    private String nomUtil;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "mot_de_passe", length = 255)
    private String motDePasse;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_role")
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_utilisateur")
    private StatutUtilisateur statutUtilisateur;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    public Long getIdUtil() {
        return idUtil;
    }

    public void setIdUtil(Long idUtil) {
        this.idUtil = idUtil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomUtil() {
        return nomUtil;
    }

    public void setNomUtil(String nomUtil) {
        this.nomUtil = nomUtil;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public StatutUtilisateur getStatutUtilisateur() {
        return statutUtilisateur;
    }

    public void setStatutUtilisateur(StatutUtilisateur statutUtilisateur) {
        this.statutUtilisateur = statutUtilisateur;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDerniereConnexion() {
        return derniereConnexion;
    }

    public void setDerniereConnexion(LocalDateTime derniereConnexion) {
        this.derniereConnexion = derniereConnexion;
    }
}
