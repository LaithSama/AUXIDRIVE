package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "agencesRepresentantes")
public class AgenceRepresentante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAgence")
    private Long idAgence;

    @Column(name = "codeAgence", length = 2, unique = true)
    private String codeAgence;

    @Column(name = "nomAgence", length = 50)
    private String nomAgence;

    @Column(name = "ville", length = 50)
    private String ville;

    @Column(name = "adresse", length = 100)
    private String adresse;

    @Column(name = "bp", length = 10)
    private String bp;

    @Column(name = "tel", length = 20)
    private String tel;

    @Column(name = "mail", length = 50)
    private String mail;

    public AgenceRepresentante(String codeAgence, String nomAgence, String ville, String adresse, String bp, String tel, String mail) {
        this.codeAgence = codeAgence;
        this.nomAgence = nomAgence;
        this.ville = ville;
        this.adresse = adresse;
        this.bp = bp;
        this.tel = tel;
        this.mail = mail;
    }

    public AgenceRepresentante(Long idAgence, String codeAgence, String nomAgence, String ville, String adresse, String bp, String tel, String mail) {
        this.idAgence = idAgence;
        this.codeAgence = codeAgence;
        this.nomAgence = nomAgence;
        this.ville = ville;
        this.adresse = adresse;
        this.bp = bp;
        this.tel = tel;
        this.mail = mail;
    }

    public Long getIdAgence() {
        return idAgence;
    }

    public void setIdAgence(Long idAgence) {
        this.idAgence = idAgence;
    }

    public String getCodeAgence() {
        return codeAgence;
    }

    public void setCodeAgence(String codeAgence) {
        this.codeAgence = codeAgence;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getBp() {
        return bp;
    }

    public void setBp(String bp) {
        this.bp = bp;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}
