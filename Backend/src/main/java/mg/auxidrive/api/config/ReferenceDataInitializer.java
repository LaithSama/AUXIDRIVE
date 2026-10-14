package mg.auxidrive.api.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.AvenantType;
import mg.auxidrive.api.model.Branche;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;
import mg.auxidrive.api.model.GarantieType;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.AvenantTypeRepository;
import mg.auxidrive.api.repository.BrancheParametresCalculRepository;
import mg.auxidrive.api.repository.BrancheRepository;
import mg.auxidrive.api.repository.CodeTaxePoliceRepository;
import mg.auxidrive.api.repository.GarantieTypeRepository;

/**
 * Au démarrage : charge les référentiels de calcul de la prime s'ils sont absents.
 *
 * Le chargement est idempotent, chaque ligne est cherchée par sa clé avant d'être créée :
 * redémarrer l'application ne duplique rien et ne réécrit pas les données modifiées à la main.
 *
 * Répartition des rôles dans le calcul de la prime :
 * - le code taxe (B, E, P, T, U, Z) décide si TE, TVA et TACAVA s'appliquent,
 * - le code tarifaire du véhicule indique si le TACAVA est attendu pour ce véhicule (avec_tacava),
 * - la branche fournit les taux : taux_te, taux_tacava, et le montant des accessoires.
 * Les taux de la table des codes taxe restent vides, la branche fait foi.
 */
@Component
public class ReferenceDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataInitializer.class);

    /** Branche automobile : celle qui porte les codes tarifaires et les garanties. */
    private static final String BRANCHE_AUTO = "01";

    private final BrancheRepository brancheRepository;
    private final BrancheParametresCalculRepository parametresRepository;
    private final CodeTaxePoliceRepository codeTaxeRepository;
    private final AutoCodeTarifRepository codeTarifRepository;
    private final GarantieTypeRepository garantieRepository;
    private final AvenantTypeRepository avenantRepository;

    public ReferenceDataInitializer(BrancheRepository brancheRepository,
                                    BrancheParametresCalculRepository parametresRepository,
                                    CodeTaxePoliceRepository codeTaxeRepository,
                                    AutoCodeTarifRepository codeTarifRepository,
                                    GarantieTypeRepository garantieRepository,
                                    AvenantTypeRepository avenantRepository) {
        this.brancheRepository = brancheRepository;
        this.parametresRepository = parametresRepository;
        this.codeTaxeRepository = codeTaxeRepository;
        this.codeTarifRepository = codeTarifRepository;
        this.garantieRepository = garantieRepository;
        this.avenantRepository = avenantRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        chargerBranches();
        chargerCodesTaxe();
        chargerGaranties();
        chargerCodesTarifaires();
        chargerAvenants();
        log.info("Référentiels de calcul de la prime vérifiés");
    }

    /**
     * Branches et leurs paramètres de calcul.
     * La dernière colonne du fichier source est le taux de TACAVA : il varie fortement d'une
     * branche à l'autre (5 % en aviation, 18 % en automobile), il n'a donc de sens que par branche.
     * La commission totale n'est connue que pour l'automobile (18 %) : les autres branches restent
     * sans taux tant que les valeurs réelles ne sont pas connues.
     */
    private void chargerBranches() {
        String[][] lignes = {
                // code, libellé, accessoires, taux TE, taux TVA, taux TACAVA, commission totale
                {"01", "AUTOMOBILE", "2000", "4.50", "20.00", "18.00", "18.00"},
                {"07", "AUTRE INDIVIDUELLE", "2000", "4.50", "20.00", "20.00", ""},
                {"08", "AVIATION CORPS RG", "2000", "4.50", "20.00", "5.00", ""},
                {"09", "AVIATION CORPS RO", "2000", "4.50", "20.00", "7.50", ""},
                {"10", "AVIATION RESPONS CIVILE RG", "2000", "4.50", "20.00", "7.50", ""},
                {"11", "AVIATION RESPONS CIVILE RO", "2000", "4.50", "20.00", "12.50", ""},
                {"12", "BRIS DE GLACE", "2000", "4.50", "20.00", "20.00", ""},
                {"13", "BRIS DE MACHINE", "2000", "4.50", "20.00", "10.00", ""},
                {"14", "CAPITALISATION", "2000", "4.50", "20.00", "10.00", ""},
        };

        for (String[] l : lignes) {
            String code = l[0];
            Branche branche = brancheRepository.findById(code).orElseGet(Branche::new);
            branche.setCdBranche(code);
            branche.setBranches(l[1]);
            branche = brancheRepository.save(branche);

            BrancheParametresCalcul parametres = parametresRepository.findByBranche_CdBranche(code)
                    .orElseGet(BrancheParametresCalcul::new);
            parametres.setBranche(branche);
            parametres.setAccessoires(Double.valueOf(l[2]));
            parametres.setTauxTe(Double.valueOf(l[3]));
            parametres.setTauxTva(Double.valueOf(l[4]));
            parametres.setTauxTacava(Double.valueOf(l[5]));
            parametres.setCommissionTotale(versDouble(l[6]));
            parametresRepository.save(parametres);
        }
    }

    /** Convertit une cellule texte en Double, ou renvoie null si la valeur n'est pas renseignée. */
    private static Double versDouble(String valeur) {
        return valeur == null || valeur.isBlank() ? null : Double.valueOf(valeur);
    }

    /**
     * Codes taxe : ils décident quelles taxes s'appliquent.
     * taux_te et taux_tacava restent nuls car la branche fournit ces taux.
     * Seul T autorise le TACAVA, ce qui laisse le code tarifaire du véhicule trancher
     * (le TACAVA s'applique si le code T l'autorise ET si le tarif du véhicule le demande).
     */
    private void chargerCodesTaxe() {
        String[][] lignes = {
                // code, libellé, taux TVA, applique TE, applique TVA, applique TACAVA
                {"B", "Police sans TE - sans TVA", "0.00", "false", "false", "false"},
                {"E", "Police sans TE - avec TVA", "20.00", "false", "true", "false"},
                {"P", "Police sans TE - sans TVA", "0.00", "false", "false", "false"},
                {"T", "Police avec TE et TVA", "20.00", "true", "true", "true"},
                {"U", "Police avec TE - sans TVA", "0.00", "true", "false", "false"},
                {"Z", "Police avec TE et TVA", "20.00", "true", "true", "false"},
        };

        for (String[] l : lignes) {
            if (codeTaxeRepository.existsByCodeTaxeIgnoreCase(l[0])) {
                continue;
            }
            CodeTaxePolice c = new CodeTaxePolice();
            c.setCodeTaxe(l[0]);
            c.setLibelleCodeTaxe(l[1]);
            c.setTauxTaxe(Double.valueOf(l[2]));
            c.setAppliquerTe(Boolean.valueOf(l[3]));
            c.setAppliquerTva(Boolean.valueOf(l[4]));
            c.setAppliquerTacava(Boolean.valueOf(l[5]));
            c.setTauxTe(null);
            c.setTauxTacava(null);
            codeTaxeRepository.save(c);
        }
    }

    /** Garanties de la branche automobile. */
    private void chargerGaranties() {
        String[][] lignes = {
                {"05", "R.C Matériel"},
                {"08", "Dommages"},
                {"12", "Vol"},
                {"14", "Bris de glace"},
                {"20", "Tierce Collision"},
        };

        Branche automobile = brancheRepository.findById(BRANCHE_AUTO).orElseThrow();
        for (String[] l : lignes) {
            if (garantieRepository.existsById(l[0])) {
                continue;
            }
            GarantieType g = new GarantieType();
            g.setCdGarantie(l[0]);
            g.setLibelleGarantie(l[1]);
            g.setBranche(automobile);
            garantieRepository.save(g);
        }
    }

    /**
     * Codes tarifaires des véhicules.
     * avec_tacava est vrai pour A (11), E (12) et J (6) : ce sont eux qui déclenchent le TACAVA
     * lorsque le code taxe T l'autorise. Le préfixe "TARIF" du fichier source est retiré,
     * le code retenu est celui entre parenthèses, seul exploitable comme identifiant lisible.
     * Les deux lignes distinctes se distinguent par leur numéro (41 et 42 pour F, 31 et 32 sans lettre).
     */
    private void chargerCodesTarifaires() {
        String[][] lignes = {
                // code, libellé, applique TACAVA, code taxe
                {"11 (A)", "VP. CI. CI4P. BREAK. de 2 à 9 places", "true", "T"},
                {"12 (E)", "Moto 2 Roues ou 3 Roues", "true", "T"},
                {"21 (B)", "Transport de Personne a Titre Gratuit", "false", "T"},
                {"22 (D)", "Transport de Personne a Titre Payant (Minibus ou bus)", "false", "T"},
                {"22 (H)", "Location de vehicule a 2 ou 3 roues et 4 roues", "false", "T"},
                {"22 (ZHG)", "Transport de Personne a Titre Payant (Taxi, Location)", "false", "T"},
                {"31", "Transport de Personne a Titre Gratuit (sans code)", "false", "T"},
                {"31 (R)", "Transport de Marchandise a Titre Gratuit (Camion)", "false", "T"},
                {"32", "Transport de Marchandise (sans code)", "false", "T"},
                {"32 (C)", "CITERNE", "false", "T"},
                {"41 (F)", "Vehicule a 2 ou 3 roues (destine au garagiste)", "false", "T"},
                {"42 (F)", "Vehicule a 4 roues (destine au garagiste)", "false", "T"},
                {"5 (I)", "Engin de chantier", "false", "T"},
                {"6 (J)", "AMBULANCE", "true", "T"},
        };

        for (String[] l : lignes) {
            if (codeTarifRepository.existsByTarifCodeIgnoreCase(l[0])) {
                continue;
            }
            AutoCodeTarif c = new AutoCodeTarif();
            c.setTarifCode(l[0]);
            c.setTarifLibelle(l[1]);
            c.setAvecTacava(Boolean.valueOf(l[2]));
            c.setCodeTaxePolice(codeTaxeRepository.findByCodeTaxeIgnoreCase(l[3]).orElse(null));
            // le taux de TE du fichier source est ignoré : la branche fournit le taux appliqué
            c.setTauxTe(null);
            codeTarifRepository.save(c);
        }
    }

    /** Types d'avenant. Les numéros absents du fichier source ne sont pas inventés. */
    private void chargerAvenants() {
        List<String[]> lignes = List.of(
                new String[]{"0", "CERTIFICAT MARITIME"},
                new String[]{"1", "CONDITIONS PARTICULIERES"},
                new String[]{"2", "AVENANT DE RENOUVELLEMENT"},
                new String[]{"3", "AVENANT D'ORDRE"},
                new String[]{"4", "AVENANT D'INCLUSION"},
                new String[]{"5", "AVENANT D'EXCLUSION"},
                new String[]{"6", "AVENANT DE PRECISION"},
                new String[]{"7", "AVENANT DE NOUVELLE REPARTITION"},
                new String[]{"9", "AUGMENTATION DE CAPITAL"},
                new String[]{"10", "AVENANT DE DIMINUTION DE CAPITAL"},
                new String[]{"11", "AVENANT DE TRANSFERT DE RISQUE"},
                new String[]{"13", "AJUSTEMENT RISTOURNE"},
                new String[]{"14", "AJUSTEMENT PERCEPTION"},
                new String[]{"15", "RISTOURNE MAPFRE"},
                new String[]{"16", "COTISATION"},
                new String[]{"17", "RENOUVELLEMENT"},
                new String[]{"18", "AVENANT DE REGULARISATION"},
                new String[]{"19", "AVENANT DE RESILIATION"},
                new String[]{"20", "A PASSER SANS EFFET"}
        );

        for (String[] l : lignes) {
            if (avenantRepository.existsById(l[0])) {
                continue;
            }
            AvenantType a = new AvenantType();
            a.setNatureAvenantCode(l[0]);
            a.setNatureAvenant(l[1]);
            // le numéro d'ordre du fichier source est décimal, il est reconstruit à partir du code
            a.setNatureAvenantNum(Integer.valueOf(l[0]));
            avenantRepository.save(a);
        }
    }
}