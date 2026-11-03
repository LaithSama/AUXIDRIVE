package mg.auxidrive.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;

/**
 * Pièces justificatives du client connecté, gérées depuis son profil.
 *
 * L'assuré est toujours déduit de l'e-mail du token : aucun identifiant d'assuré n'est accepté
 * depuis la requête. Toute pièce ajoutée ou remplacée repasse en EN_ATTENTE et rejoint la file
 * de vérification des agents ; un rejet n'ouvre jamais de parcours de correction d'inscription.
 */
@Service
public class DocumentClientService {

    public record DocumentProfil(Long idDocument, String typeDocument, String formatFichier,
                                 Integer tailleKo, LocalDateTime dateTeleversement,
                                 String statutDocuments, String motifRejet,
                                 LocalDateTime dateValidation, boolean verrouille) {
    }

    public record FichierDocument(Resource ressource, String typeMime) {
    }

    private final CompteClientRepository compteClientRepository;
    private final DocumentJustificatifRepository documentRepository;
    private final FichierService fichierService;

    public DocumentClientService(CompteClientRepository compteClientRepository,
                                 DocumentJustificatifRepository documentRepository,
                                 FichierService fichierService) {
        this.compteClientRepository = compteClientRepository;
        this.documentRepository = documentRepository;
        this.fichierService = fichierService;
    }

    /** Une pièce par type : la plus récente est retenue, dans l'ordre des types autorisés. */
    @Transactional(readOnly = true)
    public List<DocumentProfil> lister(String email) {
        Assure assure = assure(email);
        List<DocumentJustificatif> pieces = documentRepository
                .findByAssure_IdAssureOrderByDateTeleversementDescIdDocumentDesc(assure.getIdAssure());

        Map<String, DocumentJustificatif> recentes = new LinkedHashMap<>();
        for (DocumentJustificatif piece : pieces) {
            if (InscriptionService.TYPES_DOCUMENTS.contains(piece.getTypeDocument())) {
                recentes.putIfAbsent(piece.getTypeDocument(), piece);
            }
        }

        List<DocumentProfil> resultat = new ArrayList<>();
        for (String type : InscriptionService.TYPES_DOCUMENTS) {
            DocumentJustificatif piece = recentes.get(type);
            if (piece != null) {
                resultat.add(versProfil(piece));
            }
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public DocumentProfil trouver(String email, Long idDocument) {
        return versProfil(piece(email, idDocument));
    }

    /** Ajoute une pièce absente. Une pièce du même type doit passer par le remplacement. */
    @Transactional
    public DocumentProfil ajouter(String email, String type, MultipartFile fichier) {
        String typeNormalise = normaliserType(type);
        Assure assure = assure(email);
        if (documentRepository.existsByAssure_IdAssureAndTypeDocument(assure.getIdAssure(), typeNormalise)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Une pièce de ce type existe déjà. Utilisez le remplacement.");
        }

        FichierService.Fichier stocke = fichierService.stocker(fichier);
        try {
            DocumentJustificatif piece = new DocumentJustificatif();
            piece.setAssure(assure);
            piece.setTypeDocument(typeNormalise);
            piece.setCheminFichier(stocke.chemin());
            piece.setFormatFichier(stocke.format());
            piece.setTailleKo(stocke.tailleKo());
            piece.setDateTeleversement(LocalDateTime.now());
            piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
            documentRepository.saveAndFlush(piece);
            return versProfil(piece);
        } catch (RuntimeException e) {
            fichierService.supprimer(stocke.chemin());
            throw e;
        }
    }

    /**
     * Remplace le fichier d'une pièce du client et la remet en attente (motif et date de
     * validation effacés). Refusé tant qu'un dossier d'inscription est en cours d'instruction :
     * le parcours de correction par e-mail reste alors obligatoire.
     */
    @Transactional
    public DocumentProfil remplacer(String email, Long idDocument, MultipartFile fichier) {
        Assure assure = assure(email);
        DocumentJustificatif piece = documentRepository
                .findByIdDocumentAndAssure_IdAssure(idDocument, assure.getIdAssure())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        verifierModifiable(piece);

        FichierService.Fichier stocke = fichierService.stocker(fichier);
        try {
            String ancienChemin = piece.getCheminFichier();
            piece.setCheminFichier(stocke.chemin());
            piece.setFormatFichier(stocke.format());
            piece.setTailleKo(stocke.tailleKo());
            piece.setDateTeleversement(LocalDateTime.now());
            piece.setStatutDocuments(StatutDocument.EN_ATTENTE);
            piece.setMotifRejet(null);
            piece.setDateValidation(null);
            piece.setUtilisateur(null);
            documentRepository.saveAndFlush(piece);
            apresCommit(() -> fichierService.supprimer(ancienChemin));
            return versProfil(piece);
        } catch (RuntimeException e) {
            fichierService.supprimer(stocke.chemin());
            throw e;
        }
    }

    @Transactional
    public void supprimer(String email, Long idDocument) {
        Assure assure = assure(email);
        DocumentJustificatif piece = documentRepository
                .findByIdDocumentAndAssure_IdAssure(idDocument, assure.getIdAssure())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        verifierModifiable(piece);
        String chemin = piece.getCheminFichier();
        documentRepository.delete(piece);
        apresCommit(() -> fichierService.supprimer(chemin));
    }

    /** Aperçu : le fichier n'est servi que si la pièce appartient bien au client connecté. */
    @Transactional(readOnly = true)
    public FichierDocument fichier(String email, Long idDocument) {
        DocumentJustificatif piece = piece(email, idDocument);
        return new FichierDocument(fichierService.charger(piece.getCheminFichier()),
                fichierService.typeMime(piece.getFormatFichier()));
    }

    private DocumentJustificatif piece(String email, Long idDocument) {
        Assure assure = assure(email);
        return documentRepository.findByIdDocumentAndAssure_IdAssure(idDocument, assure.getIdAssure())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    private Assure assure(String email) {
        CompteClient compte = compteClientRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte client introuvable"));
        Assure assure = compte.getAssure();
        if (assure == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Assuré introuvable");
        }
        return assure;
    }

    private static String normaliserType(String type) {
        String valeur = type == null ? "" : type.trim().toUpperCase();
        if (!InscriptionService.TYPES_DOCUMENTS.contains(valeur)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Type de document non autorisé");
        }
        return valeur;
    }

    private static boolean verrouillee(DocumentJustificatif piece) {
        Inscription inscription = piece.getInscription();
        return inscription != null
                && (inscription.getStatutInscription() == StatutInscription.EN_ATTENTE
                || inscription.getStatutInscription() == StatutInscription.CORRECTION_DEMANDEE);
    }

    private static void verifierModifiable(DocumentJustificatif piece) {
        if (verrouillee(piece)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ce dossier est en cours d'instruction : suivez le lien de correction reçu par e-mail.");
        }
    }

    private static DocumentProfil versProfil(DocumentJustificatif piece) {
        return new DocumentProfil(
                piece.getIdDocument(),
                piece.getTypeDocument(),
                piece.getFormatFichier(),
                piece.getTailleKo(),
                piece.getDateTeleversement(),
                piece.getStatutDocuments() == null ? null : piece.getStatutDocuments().name(),
                piece.getMotifRejet(),
                piece.getDateValidation(),
                verrouillee(piece));
    }

    private static void apresCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
