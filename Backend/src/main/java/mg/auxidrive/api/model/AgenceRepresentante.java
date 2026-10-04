package mg.auxidrive.api.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Agences_representantes")
public class AgenceRepresentante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_agence")
    private Long idAgence;

    @Column(name = "code_agence", length = 2, unique = true)
    private String codeAgence;

    @Column(name = "nom_agence", length = 50)
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
