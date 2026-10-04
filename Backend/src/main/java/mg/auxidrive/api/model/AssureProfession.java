package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;

@Entity
@Table(name = "Assures_profession")
public class AssureProfession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prof")
    private Long idProf;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure")
    private Assure assure;

    @Column(name = "profession_assure", length = 30)
    private String professionAssure;

    @Column(name = "num_mle", length = 10)
    private String numMle;

    @Column(name = "num_mle_ancien", length = 50)
    private String numMleAncien;

    @Column(name = "ste_facturation", length = 50)
    private String steFacturation;

    @Column(name = "ste_adresse", length = 50)
    private String steAdresse;

    @Column(name = "ste_tel", length = 13)
    private String steTel;

    @Column(name = "ste_mail", length = 50)
    private String steMail;

    @Column(name = "nom_responsable", length = 40)
    private String nomResponsable;

    public Long getIdProf() {
        return idProf;
    }

    public void setIdProf(Long idProf) {
        this.idProf = idProf;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public String getProfessionAssure() {
        return professionAssure;
    }

    public void setProfessionAssure(String professionAssure) {
        this.professionAssure = professionAssure;
    }

    public String getNumMle() {
        return numMle;
    }

    public void setNumMle(String numMle) {
        this.numMle = numMle;
    }

    public String getNumMleAncien() {
        return numMleAncien;
    }

    public void setNumMleAncien(String numMleAncien) {
        this.numMleAncien = numMleAncien;
    }

    public String getSteFacturation() {
        return steFacturation;
    }

    public void setSteFacturation(String steFacturation) {
        this.steFacturation = steFacturation;
    }

    public String getSteAdresse() {
        return steAdresse;
    }

    public void setSteAdresse(String steAdresse) {
        this.steAdresse = steAdresse;
    }

    public String getSteTel() {
        return steTel;
    }

    public void setSteTel(String steTel) {
        this.steTel = steTel;
    }

    public String getSteMail() {
        return steMail;
    }

    public void setSteMail(String steMail) {
        this.steMail = steMail;
    }

    public String getNomResponsable() {
        return nomResponsable;
    }

    public void setNomResponsable(String nomResponsable) {
        this.nomResponsable = nomResponsable;
    }
}
