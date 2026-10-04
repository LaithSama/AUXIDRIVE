package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Documents_justificatifs")
public class DocumentJustificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Long idDocument;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure")
    private Assure assure;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inscription")
    private Inscription inscription;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vehicule")
    private Vehicule vehicule;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private Contrat contrat;

    @Column(name = "type_document", length = 30)
    private String typeDocument;

    @JsonIgnore
    @Column(name = "chemin_fichier", length = 100)
    private String cheminFichier;

    @Column(name = "format_fichier", length = 5)
    private String formatFichier;

    @Column(name = "taille_ko")
    private Integer tailleKo;

    @Column(name = "date_televersement")
    private LocalDateTime dateTeleversement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_documents")
    private StatutDocument statutDocuments;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_util")
    private Utilisateur utilisateur;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @Column(name = "motif_rejet", length = 255)
    private String motifRejet;

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
