package mg.auxidrive.api.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import mg.auxidrive.api.model.AutoCodeTarif;
import mg.auxidrive.api.model.Branche;
import mg.auxidrive.api.model.BrancheParametresCalcul;
import mg.auxidrive.api.model.CodeTaxePolice;
import mg.auxidrive.api.model.GarantieType;
import mg.auxidrive.api.repository.AutoCodeTarifRepository;
import mg.auxidrive.api.repository.BrancheParametresCalculRepository;
import mg.auxidrive.api.repository.BrancheRepository;
import mg.auxidrive.api.repository.CodeTaxePoliceRepository;
import mg.auxidrive.api.repository.GarantieTypeRepository;

/**
 * Lecture des référentiels utilisés par le calcul de la prime : branches, garanties, codes taxe,
 * codes tarifaires. Ces données sont chargées au démarrage et ne sont pas modifiables par l'API.
 */
@RestController
@RequestMapping("/api/referentiels")
public class ReferentielController {

    private final BrancheRepository brancheRepository;
    private final BrancheParametresCalculRepository parametresRepository;
    private final CodeTaxePoliceRepository codeTaxeRepository;
    private final AutoCodeTarifRepository codeTarifRepository;
    private final GarantieTypeRepository garantieRepository;

    public ReferentielController(BrancheRepository brancheRepository,
                                 BrancheParametresCalculRepository parametresRepository,
                                 CodeTaxePoliceRepository codeTaxeRepository,
                                 AutoCodeTarifRepository codeTarifRepository,
                                 GarantieTypeRepository garantieRepository) {
        this.brancheRepository = brancheRepository;
        this.parametresRepository = parametresRepository;
        this.codeTaxeRepository = codeTaxeRepository;
        this.codeTarifRepository = codeTarifRepository;
        this.garantieRepository = garantieRepository;
    }

    @GetMapping("/branches")
    public List<Branche> branches() {
        return brancheRepository.findAll();
    }

    /** Branche et ses paramètres de calcul : taux de TE, de TVA, de TACAVA et montant des accessoires. */
    @GetMapping("/branches/{cdBranche}/parametres")
    public BrancheParametresCalcul parametres(@PathVariable String cdBranche) {
        return parametresRepository.findByBranche_CdBranche(cdBranche).orElse(null);
    }

    @GetMapping("/branches/{cdBranche}/garanties")
    public List<GarantieType> garanties(@PathVariable String cdBranche) {
        return garantieRepository.findByBranche_CdBrancheOrderByCdGarantie(cdBranche);
    }

    @GetMapping("/codes-taxe")
    public List<CodeTaxePolice> codesTaxe() {
        return codeTaxeRepository.findAll();
    }

    @GetMapping("/codes-tarifaires")
    public List<AutoCodeTarif> codesTarifaires() {
        return codeTarifRepository.findAll();
    }
}