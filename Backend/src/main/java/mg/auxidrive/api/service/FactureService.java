package mg.auxidrive.api.service;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.Facture;
import mg.auxidrive.api.model.GarantieType;
import mg.auxidrive.api.model.StatutFacture;
import mg.auxidrive.api.model.TypeFacture;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.FactureRepository;
import mg.auxidrive.api.service.PrimeCalculator.Detail;

/**
 * Émission des factures et localisation de leur PDF.
 *
 * La facture recopie le détail du calcul ligne par ligne : sans les colonnes accessoires, TE, TACAVA
 * et TVA, le PDF ne pourrait pas expliquer le montant TTC payé par l'assuré.
 * Le montant n'est pas recalculé ici, il est repris du calcul établi à la souscription : le contrat
 * et sa facture ne peuvent ainsi pas diverger.
 */
@Service
public class FactureService {

    private static final Logger log = LoggerFactory.getLogger(FactureService.class);

    private final FactureRepository factureRepository;
    private final ContratAutoRepository contratAutoRepository;
    private final FacturePdfGenerator pdfGenerator;

    public FactureService(FactureRepository factureRepository,
                          ContratAutoRepository contratAutoRepository,
                          FacturePdfGenerator pdfGenerator) {
        this.factureRepository = factureRepository;
        this.contratAutoRepository = contratAutoRepository;
        this.pdfGenerator = pdfGenerator;
    }

    @Transactional(readOnly = true)
    public List<Facture> lister() {
        return factureRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Facture trouver(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable"));
    }

    @Transactional(readOnly = true)
    public List<Facture> listerParContrat(Long idContrat) {
        return factureRepository.findByContrat_IdContratOrderByIdFacture(idContrat);
    }

    /** Factures de tous les contrats d'un assuré : c'est la vue du portail client. */
    @Transactional(readOnly = true)
    public List<Facture> listerParAssure(Long idAssure) {
        return factureRepository.findByContrat_Assure_IdAssureOrderByIdFacture(idAssure);
    }

    /** Facture appartenant bien à l'assuré : contrôle d'appartenance du portail client. */
    @Transactional(readOnly = true)
    public Facture trouverPourAssure(Long idFacture, Long idAssure) {
        Facture facture = trouver(idFacture);
        Long idAssureFacture = facture.getContrat() == null || facture.getContrat().getAssure() == null
                ? null : facture.getContrat().getAssure().getIdAssure();
        if (idAssureFacture == null || !idAssureFacture.equals(idAssure)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable");
        }
        return facture;
    }

    /**
     * Émet la facture de souscription et joint son PDF.
     * Le détail du calcul est celui établi à la souscription : il porte la base taxable et
     * la part de chaque taxe, ce qui permet au PDF d'être vérifiable par l'assuré.
     */
    @Transactional
    public Facture emettreSouscription(Contrat contrat, Detail detail) {
        return emettre(contrat, detail, TypeFacture.SOUSCRIPTION);
    }

    /**
     * Émet la facture de renouvellement émise à chaque reconduction tacite du contrat,
     * sur le même modèle que la facture de souscription.
     */
    @Transactional
    public Facture emettreRenouvellement(Contrat contrat, Detail detail) {
        return emettre(contrat, detail, TypeFacture.RENOUVELLEMENT);
    }

    private Facture emettre(Contrat contrat, Detail detail, TypeFacture type) {
        if (contrat.getBranche() == null || contrat.getOffre() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Contrat sans branche ni offre : impossible d'établir la facture");
        }

        Facture facture = new Facture();
        facture.setContrat(contrat);
        facture.setTypeFacture(type);
        facture.setDateEmission(LocalDate.now());
        facture.setStatutFacture(StatutFacture.EMISE);
        // prime_nette porte la base taxable, c'est-à-dire la prime nette plus les accessoires
        facture.setPrimeNette(detail.base());
        facture.setAccessoires(detail.accessoires());
        facture.setMontantTe(detail.te());
        facture.setMontantTacava(detail.tacava());
        facture.setMontantTva(detail.tva());
        facture.setMontantTaxes(detail.totalTaxes());
        facture.setMontantTtc(detail.ttc());

        facture = factureRepository.save(facture);
        try {
            String chemin = pdfGenerator.generer(facture, vehicules(contrat), garanties(contrat));
            facture.setCheminFichierPdf(chemin);
            facture = factureRepository.save(facture);
        } catch (RuntimeException e) {
            // la facture reste valable même si le PDF n'a pas pu être produit
            log.warn("Facture {} enregistrée sans PDF : {}", facture.getIdFacture(), e.getMessage());
        }
        return facture;
    }

    /** Fichier PDF d'une facture, ou une erreur si elle n'a pas de PDF ou si le fichier a disparu. */
    @Transactional(readOnly = true)
    public File fichierPdf(Long idFacture) {
        Facture facture = trouver(idFacture);
        String chemin = facture.getCheminFichierPdf();
        if (chemin == null || chemin.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cette facture n'a pas de PDF");
        }
        File fichier = new File(chemin);
        if (!fichier.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Le PDF de cette facture est introuvable");
        }
        return fichier;
    }

    /** Véhicules couverts par le contrat, pour le PDF. */
    @Transactional(readOnly = true)
    public List<ContratAuto> vehicules(Contrat contrat) {
        return contratAutoRepository.findByContrat_IdContrat(contrat.getIdContrat());
    }

    /** Garanties choisies pour le contrat (0..n), triées par code, pour le PDF. */
    @Transactional(readOnly = true)
    public List<GarantieType> garanties(Contrat contrat) {
        return contrat.getGaranties().stream()
                .sorted(java.util.Comparator.comparing(GarantieType::getCdGarantie))
                .toList();
    }
}