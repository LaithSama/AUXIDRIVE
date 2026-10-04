package mg.auxidrive.api.model;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Vehicules")
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehicule")
    private Long idVehicule;

    @Column(name = "num_immatriculation", length = 15, nullable = false, unique = true)
    private String numImmatriculation;

    @Column(name = "num_chassis", length = 17, nullable = false, unique = true)
    private String numChassis;

    @Column(name = "num_moteur", length = 30)
    private String numMoteur;

    @Column(name = "marque", length = 50, nullable = false)
    private String marque;

    @Column(name = "modele", length = 50, nullable = false)
    private String modele;

    @Column(name = "type_vehicule", length = 50)
    private String typeVehicule;

    @Column(name = "usage_vehicule", length = 50)
    private String usageVehicule;

    @Column(name = "puissance_fiscale", nullable = false)
    private Integer puissanceFiscale;

    @Column(name = "energie", length = 20, nullable = false)
    private String energie;

    @Column(name = "nombre_places", nullable = false)
    private Integer nombrePlaces;

    @Column(name = "poids_a_vide", precision = 8, scale = 2)
    private BigDecimal poidsAVide;

    @Column(name = "ptac", precision = 8, scale = 2)
    private BigDecimal ptac;

    @Column(name = "charge_utile", precision = 8, scale = 2)
    private BigDecimal chargeUtile;

    @Column(name = "periodicite_visite_mois")
    private Integer periodiciteVisiteMois = 12;

    @Column(name = "date_derniere_visite")
    private LocalDate dateDerniereVisite;

    @Column(name = "date_premier_mise_circulation")
    private LocalDate datePremierMiseCirculation;

    @Column(name = "date_ajout")
    private LocalDate dateAjout = LocalDate.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_assure", nullable = false)
    private Assure assure;

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
