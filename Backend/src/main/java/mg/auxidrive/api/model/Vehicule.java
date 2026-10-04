package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicules")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idVehicule")
    private Long idVehicule;

    @Column(name = "numImmatriculation", length = 15, nullable = false, unique = true)
    private String numImmatriculation;

    @Column(name = "numChassis", length = 17, nullable = false, unique = true)
    private String numChassis;

    @Column(name = "numMoteur", length = 30)
    private String numMoteur;

    @Column(name = "marque", length = 50, nullable = false)
    private String marque;

    @Column(name = "modele", length = 50, nullable = false)
    private String modele;

    @Column(name = "typeVehicule", length = 50)
    private String typeVehicule;

    @Column(name = "usageVehicule", length = 50)
    private String usageVehicule;

    @Column(name = "puissanceFiscale", nullable = false)
    private Integer puissanceFiscale;

    @Column(name = "energie", length = 20, nullable = false)
    private String energie;

    @Column(name = "nombrePlaces", nullable = false)
    private Integer nombrePlaces;

    @Column(name = "poidsAVide", precision = 8, scale = 2)
    private BigDecimal poidsAVide;

    @Column(name = "ptac", precision = 8, scale = 2)
    private BigDecimal ptac;

    @Column(name = "chargeUtile", precision = 8, scale = 2)
    private BigDecimal chargeUtile;

    @Column(name = "periodiciteVisiteMois")
    private Integer periodiciteVisiteMois = 12;

    @Column(name = "dateDerniereVisite")
    private LocalDate dateDerniereVisite;

    @Column(name = "datePremierMiseCirculation")
    private LocalDate datePremierMiseCirculation;

    @Column(name = "dateAjout")
    private LocalDate dateAjout = LocalDate.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idAssure", nullable = false)
    private Assure assure;

    public Vehicule(String numImmatriculation, String numChassis, String numMoteur, String marque, String modele, String typeVehicule, String usageVehicule, Integer puissanceFiscale, String energie, Integer nombrePlaces, BigDecimal poidsAVide, BigDecimal ptac, BigDecimal chargeUtile, Integer periodiciteVisiteMois, LocalDate dateDerniereVisite, LocalDate datePremierMiseCirculation, LocalDate dateAjout, Assure assure) {
        this.numImmatriculation = numImmatriculation;
        this.numChassis = numChassis;
        this.numMoteur = numMoteur;
        this.marque = marque;
        this.modele = modele;
        this.typeVehicule = typeVehicule;
        this.usageVehicule = usageVehicule;
        this.puissanceFiscale = puissanceFiscale;
        this.energie = energie;
        this.nombrePlaces = nombrePlaces;
        this.poidsAVide = poidsAVide;
        this.ptac = ptac;
        this.chargeUtile = chargeUtile;
        this.periodiciteVisiteMois = periodiciteVisiteMois;
        this.dateDerniereVisite = dateDerniereVisite;
        this.datePremierMiseCirculation = datePremierMiseCirculation;
        this.dateAjout = dateAjout;
        this.assure = assure;
    }

    public Vehicule(Long idVehicule, String numImmatriculation, String numChassis, String numMoteur, String marque, String modele, String typeVehicule, String usageVehicule, Integer puissanceFiscale, String energie, Integer nombrePlaces, BigDecimal poidsAVide, BigDecimal ptac, BigDecimal chargeUtile, Integer periodiciteVisiteMois, LocalDate dateDerniereVisite, LocalDate datePremierMiseCirculation, LocalDate dateAjout, Assure assure) {
        this.idVehicule = idVehicule;
        this.numImmatriculation = numImmatriculation;
        this.numChassis = numChassis;
        this.numMoteur = numMoteur;
        this.marque = marque;
        this.modele = modele;
        this.typeVehicule = typeVehicule;
        this.usageVehicule = usageVehicule;
        this.puissanceFiscale = puissanceFiscale;
        this.energie = energie;
        this.nombrePlaces = nombrePlaces;
        this.poidsAVide = poidsAVide;
        this.ptac = ptac;
        this.chargeUtile = chargeUtile;
        this.periodiciteVisiteMois = periodiciteVisiteMois;
        this.dateDerniereVisite = dateDerniereVisite;
        this.datePremierMiseCirculation = datePremierMiseCirculation;
        this.dateAjout = dateAjout;
        this.assure = assure;
    }

    public Long getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Long idVehicule) {
        this.idVehicule = idVehicule;
    }

    public String getNumImmatriculation() {
        return numImmatriculation;
    }

    public void setNumImmatriculation(String numImmatriculation) {
        this.numImmatriculation = numImmatriculation;
    }

    public String getNumChassis() {
        return numChassis;
    }

    public void setNumChassis(String numChassis) {
        this.numChassis = numChassis;
    }

    public String getNumMoteur() {
        return numMoteur;
    }

    public void setNumMoteur(String numMoteur) {
        this.numMoteur = numMoteur;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getTypeVehicule() {
        return typeVehicule;
    }

    public void setTypeVehicule(String typeVehicule) {
        this.typeVehicule = typeVehicule;
    }

    public String getUsageVehicule() {
        return usageVehicule;
    }

    public void setUsageVehicule(String usageVehicule) {
        this.usageVehicule = usageVehicule;
    }

    public Integer getPuissanceFiscale() {
        return puissanceFiscale;
    }

    public void setPuissanceFiscale(Integer puissanceFiscale) {
        this.puissanceFiscale = puissanceFiscale;
    }

    public String getEnergie() {
        return energie;
    }

    public void setEnergie(String energie) {
        this.energie = energie;
    }

    public Integer getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(Integer nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public BigDecimal getPoidsAVide() {
        return poidsAVide;
    }

    public void setPoidsAVide(BigDecimal poidsAVide) {
        this.poidsAVide = poidsAVide;
    }

    public BigDecimal getPtac() {
        return ptac;
    }

    public void setPtac(BigDecimal ptac) {
        this.ptac = ptac;
    }

    public BigDecimal getChargeUtile() {
        return chargeUtile;
    }

    public void setChargeUtile(BigDecimal chargeUtile) {
        this.chargeUtile = chargeUtile;
    }

    public Integer getPeriodiciteVisiteMois() {
        return periodiciteVisiteMois;
    }

    public void setPeriodiciteVisiteMois(Integer periodiciteVisiteMois) {
        this.periodiciteVisiteMois = periodiciteVisiteMois;
    }

    public LocalDate getDateDerniereVisite() {
        return dateDerniereVisite;
    }

    public void setDateDerniereVisite(LocalDate dateDerniereVisite) {
        this.dateDerniereVisite = dateDerniereVisite;
    }

    public LocalDate getDatePremierMiseCirculation() {
        return datePremierMiseCirculation;
    }

    public void setDatePremierMiseCirculation(LocalDate datePremierMiseCirculation) {
        this.datePremierMiseCirculation = datePremierMiseCirculation;
    }

    public LocalDate getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDate dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Assure getAssure() {
        return assure;
    }

    public void setAssure(Assure assure) {
        this.assure = assure;
    }
}
