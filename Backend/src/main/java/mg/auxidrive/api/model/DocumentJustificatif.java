package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentsJustificatif")
public class DocumentJustificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idDocument")
    private Long idDocument;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure")
    private Assure assure;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idInscription")
    private Inscription inscription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idVehicule")
    private Vehicule vehicule;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idContrat")
    private Contrat contrat;

    @Column(name = "typeDocument", length = 30)
    private String typeDocument;

    @JsonIgnore
    @Column(name = "cheminFichier", length = 100)
    private String cheminFichier;

    @Column(name = "formatFichier", length = 5)
    private String formatFichier;

    @Column(name = "tailleKo")
    private Integer tailleKo;

    @Column(name = "dateTeleversement")
    private LocalDateTime dateTeleversement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statutDocuments")
    private StatutDocument statutDocuments;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUtil")
    private Utilisateur utilisateur;

    @Column(name = "dateValidation")
    private LocalDateTime dateValidation;

    @Column(name = "motifRejet", length = 255)
    private String motifRejet;

    public DocumentJustificatif(Assure assure, Inscription inscription, Vehicule vehicule, Contrat contrat, String typeDocument, String cheminFichier, String formatFichier, Integer tailleKo, LocalDateTime dateTeleversement, StatutDocument statutDocuments, Utilisateur utilisateur, LocalDateTime dateValidation, String motifRejet) {
        this.assure = assure;
        this.inscription = inscription;
        this.vehicule = vehicule;
        this.contrat = contrat;
        this.typeDocument = typeDocument;
        this.cheminFichier = cheminFichier;
        this.formatFichier = formatFichier;
        this.tailleKo = tailleKo;
        this.dateTeleversement = dateTeleversement;
        this.statutDocuments = statutDocuments;
        this.utilisateur = utilisateur;
        this.dateValidation = dateValidation;
        this.motifRejet = motifRejet;
    }

    public DocumentJustificatif(Long idDocument, Assure assure, Inscription inscription, Vehicule vehicule, Contrat contrat, String typeDocument, String cheminFichier, String formatFichier, Integer tailleKo, LocalDateTime dateTeleversement, StatutDocument statutDocuments, Utilisateur utilisateur, LocalDateTime dateValidation, String motifRejet) {
        this.idDocument = idDocument;
        this.assure = assure;
        this.inscription = inscription;
        this.vehicule = vehicule;
        this.contrat = contrat;
        this.typeDocument = typeDocument;
        this.cheminFichier = cheminFichier;
        this.formatFichier = formatFichier;
        this.tailleKo = tailleKo;
        this.dateTeleversement = dateTeleversement;
        this.statutDocuments = statutDocuments;
        this.utilisateur = utilisateur;
        this.dateValidation = dateValidation;
        this.motifRejet = motifRejet;
    }

    public Long getIdDocument() {
        return idDocument;
    }

    public void setIdDocument(Long idDocument) {
        this.idDocument = idDocument;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }

    public Inscription getInscription() {
        return inscription;
    }

    public void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public Contrat getContrat() {
        return contrat;
    }

    public void setContrat(Contrat contrat) {
        this.contrat = contrat;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public String getFormatFichier() {
        return formatFichier;
    }

    public void setFormatFichier(String formatFichier) {
        this.formatFichier = formatFichier;
    }

    public Integer getTailleKo() {
        return tailleKo;
    }

    public void setTailleKo(Integer tailleKo) {
        this.tailleKo = tailleKo;
    }

    public LocalDateTime getDateTeleversement() {
        return dateTeleversement;
    }

    public void setDateTeleversement(LocalDateTime dateTeleversement) {
        this.dateTeleversement = dateTeleversement;
    }

    public StatutDocument getStatutDocuments() {
        return statutDocuments;
    }

    public void setStatutDocuments(StatutDocument statutDocuments) {
        this.statutDocuments = statutDocuments;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public LocalDateTime getDateValidation() {
        return dateValidation;
    }

    public void setDateValidation(LocalDateTime dateValidation) {
        this.dateValidation = dateValidation;
    }

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }
}
