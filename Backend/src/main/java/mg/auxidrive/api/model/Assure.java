package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Assures")
public class Assure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_assure")
    private Long idAssure;

    @Column(name = "numeros_assure", length = 8, unique = true)
    private String numerosAssure;

    @Column(name = "nom_assure", length = 50)
    private String nomAssure;

    @Column(name = "prenom_assure", length = 50)
    private String prenomAssure;

    @Column(name = "personne_morale")
    private Boolean personneMorale;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "sexe", length = 1)
    private String sexe;

    @Column(name = "civilite", length = 15)
    private String civilite;

    @Column(name = "nationalite", length = 50)
    private String nationalite;

    @Column(name = "pays_residence", length = 50)
    private String paysResidence;

    @Column(name = "adresse", length = 50)
    private String adresse;

    @Column(name = "contact", length = 13)
    private String contact;

    @Column(name = "mail", length = 50)
    private String mail;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_util")
    private Utilisateur utilisateur;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    public Long getIdAssure() {
        return idAssure;
    }

    public void setIdAssure(Long idAssure) {
        this.idAssure = idAssure;
    }

    public String getNumerosAssure() {
        return numerosAssure;
    }

    public void setNumerosAssure(String numerosAssure) {
        this.numerosAssure = numerosAssure;
    }

    public String getNomAssure() {
        return nomAssure;
    }

    public void setNomAssure(String nomAssure) {
        this.nomAssure = nomAssure;
    }

    public String getPrenomAssure() {
        return prenomAssure;
    }

    public void setPrenomAssure(String prenomAssure) {
        this.prenomAssure = prenomAssure;
    }

    public Boolean getPersonneMorale() {
        return personneMorale;
    }

    public void setPersonneMorale(Boolean personneMorale) {
        this.personneMorale = personneMorale;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getSexe() {
        return sexe;
    }

    public void setSexe(String sexe) {
        this.sexe = sexe;
    }

    public String getCivilite() {
        return civilite;
    }

    public void setCivilite(String civilite) {
        this.civilite = civilite;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getPaysResidence() {
        return paysResidence;
    }

    public void setPaysResidence(String paysResidence) {
        this.paysResidence = paysResidence;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDate dateCreation) {
        this.dateCreation = dateCreation;
    }
}
