package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "assuresProfession")
public class AssureProfession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProf")
    private Long idProf;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure")
    private Assure assure;

    @Column(name = "professionAssure", length = 30)
    private String professionAssure;

    @Column(name = "numMle", length = 10)
    private String numMle;

    @Column(name = "numMleAncien", length = 50)
    private String numMleAncien;

    @Column(name = "steFacturation", length = 50)
    private String steFacturation;

    @Column(name = "steAdresse", length = 50)
    private String steAdresse;

    @Column(name = "steTel", length = 13)
    private String steTel;

    @Column(name = "steMail", length = 50)
    private String steMail;

    @Column(name = "nomResponsable", length = 40)
    private String nomResponsable;

    public AssureProfession(Assure assure, String professionAssure, String numMle, String numMleAncien, String steFacturation, String steAdresse, String steTel, String steMail, String nomResponsable) {
        this.assure = assure;
        this.professionAssure = professionAssure;
        this.numMle = numMle;
        this.numMleAncien = numMleAncien;
        this.steFacturation = steFacturation;
        this.steAdresse = steAdresse;
        this.steTel = steTel;
        this.steMail = steMail;
        this.nomResponsable = nomResponsable;
    }

    public AssureProfession(Long idProf, Assure assure, String professionAssure, String numMle, String numMleAncien, String steFacturation, String steAdresse, String steTel, String steMail, String nomResponsable) {
        this.idProf = idProf;
        this.assure = assure;
        this.professionAssure = professionAssure;
        this.numMle = numMle;
        this.numMleAncien = numMleAncien;
        this.steFacturation = steFacturation;
        this.steAdresse = steAdresse;
        this.steTel = steTel;
        this.steMail = steMail;
        this.nomResponsable = nomResponsable;
    }

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
