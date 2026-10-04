package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "assures")
public class Assure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAssure")
    private Long idAssure;

    @Column(name = "numerosAssure", length = 8, unique = true)
    private String numerosAssure;

    @Column(name = "nomAssure", length = 50)
    private String nomAssure;

    @Column(name = "prenomAssure", length = 50)
    private String prenomAssure;

    @Column(name = "personneMorale")
    private Boolean personneMorale;

    @Column(name = "dateNaissance")
    private LocalDate dateNaissance;

    @Column(name = "sexe", length = 1)
    private String sexe;

    @Column(name = "civilite", length = 15)
    private String civilite;

    @Column(name = "nationalite", length = 50)
    private String nationalite;

    @Column(name = "paysResidence", length = 50)
    private String paysResidence;

    @Column(name = "adresse", length = 50)
    private String adresse;

    @Column(name = "contact", length = 13)
    private String contact;

    @Column(name = "mail", length = 50)
    private String mail;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_util", unique = true)
    private Utilisateur utilisateur;

    @Column(name = "date_creation")
    private LocalDate dateCreation;

    public Assure(LocalDate dateCreation, Utilisateur utilisateur, String mail, String contact, String adresse, String paysResidence, String nationalite, String civilite, String sexe, LocalDate dateNaissance, Boolean personneMorale, String prenomAssure, String nomAssure, String numerosAssure) {
        this.dateCreation = dateCreation;
        this.utilisateur = utilisateur;
        this.mail = mail;
        this.contact = contact;
        this.adresse = adresse;
        this.paysResidence = paysResidence;
        this.nationalite = nationalite;
        this.civilite = civilite;
        this.sexe = sexe;
        this.dateNaissance = dateNaissance;
        this.personneMorale = personneMorale;
        this.prenomAssure = prenomAssure;
        this.nomAssure = nomAssure;
        this.numerosAssure = numerosAssure;
    }

    public Assure(Long idAssure, String numerosAssure, String nomAssure, String prenomAssure, Boolean personneMorale, LocalDate dateNaissance, String sexe, String civilite, String nationalite, String paysResidence, String adresse, String contact, String mail, Utilisateur utilisateur, LocalDate dateCreation) {
        this.idAssure = idAssure;
        this.numerosAssure = numerosAssure;
        this.nomAssure = nomAssure;
        this.prenomAssure = prenomAssure;
        this.personneMorale = personneMorale;
        this.dateNaissance = dateNaissance;
        this.sexe = sexe;
        this.civilite = civilite;
        this.nationalite = nationalite;
        this.paysResidence = paysResidence;
        this.adresse = adresse;
        this.contact = contact;
        this.mail = mail;
        this.utilisateur = utilisateur;
        this.dateCreation = dateCreation;
    }

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
