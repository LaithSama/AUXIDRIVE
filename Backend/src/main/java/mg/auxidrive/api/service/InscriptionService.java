package mg.auxidrive.api.service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.AssureIdentifiantsLegaux;
import mg.auxidrive.api.model.AssureProfession;
import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.DocumentJustificatif;
import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.model.StatutDocument;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.model.Utilisateur;
import mg.auxidrive.api.repository.AssureIdentifiantsLegauxRepository;
import mg.auxidrive.api.repository.AssureProfessionRepository;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.DocumentJustificatifRepository;
import mg.auxidrive.api.repository.InscriptionRepository;
import mg.auxidrive.api.repository.RoleRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/**
 * Workflow d'inscription :
 * 1. le client envoie sa demande (assuré, profession, identifiants légaux, documents facultatifs) -> EN_ATTENTE.
 *    Aucun compte utilisateur n'est créé à ce stade ;
 * 2. un agent valide (compte client CompteClient créé avec le rôle CLIENT, lié à l'assuré,
 *    identifiants envoyés par e-mail) -> VALIDEE ;
 * 3. ou refuse (avec un motif) -> REFUSEE, aucun compte créé.
 */
@Service
public class InscriptionService {

    /** Types de documents que le client peut importer (tous facultatifs). */
    public static final List<String> TYPES_DOCUMENTS = List.of("CIN", "CARTE_GRISE", "CARTE_ROSE", "MODELE_N1");

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InscriptionRepository inscriptionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;
    private final AssureRepository assureRepository;
    private final AssureProfessionRepository professionRepository;
    private final AssureIdentifiantsLegauxRepository legauxRepository;
    private final DocumentJustificatifRepository documentRepository;
    private final RoleRepository roleRepository;
    private final FichierService fichierService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    public InscriptionService(InscriptionRepository inscriptionRepository,
                              UtilisateurRepository utilisateurRepository,
                              CompteClientRepository compteClientRepository,
                              AssureRepository assureRepository,
                              AssureProfessionRepository professionRepository,
                              AssureIdentifiantsLegauxRepository legauxRepository,
                              DocumentJustificatifRepository documentRepository,
                              RoleRepository roleRepository,
                              FichierService fichierService,
                              MailService mailService,
                              PasswordEncoder passwordEncoder) {
        this.inscriptionRepository = inscriptionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
        this.assureRepository = assureRepository;
        this.professionRepository = professionRepository;
        this.legauxRepository = legauxRepository;
        this.documentRepository = documentRepository;
        this.roleRepository = roleRepository;
        this.fichierService = fichierService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------------ côté client

    /**
     * Enregistre la demande. Les informations légales peuvent être saisies à la main (identifiants),
     * importées sous forme de documents, ou les deux. Il faut au moins l'un des deux.
     *
     * @param fichiers documents importés, indexés par type (CIN, CARTE_GRISE, CARTE_ROSE, MODELE_N1)
     */
    @Transactional
    public Inscription soumettre(Assure assure, AssureProfession profession,
                                 AssureIdentifiantsLegaux identifiants, Map<String, MultipartFile> fichiers) {
        verifierAssure(assure);
        String email = assure.getMail().trim().toLowerCase();
        if (emailDejaUtilise(email)
                || inscriptionRepository.existsByAssure_MailAndStatutInscription(email, StatutInscription.EN_ATTENTE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cette adresse e-mail est déjà utilisée ou une demande est déjà en attente");
        }
        if (fichiers.isEmpty() && !contientUnIdentifiant(identifiants)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Saisissez au moins un identifiant légal (CIN, NIF, CIF...) ou importez un document");
        }

        // 1) fichiers sur le disque (leur validation peut échouer : rien n'est encore écrit en base)
        Map<String, FichierService.Fichier> stockes = new LinkedHashMap<>();
        try {
            for (Map.Entry<String, MultipartFile> e : fichiers.entrySet()) {
                stockes.put(e.getKey(), fichierService.stocker(e.getValue()));
            }
            return enregistrer(assure, email, profession, identifiants, stockes);
        } catch (RuntimeException e) {
            stockes.values().forEach(f -> fichierService.supprimer(f.chemin()));
            throw e;
        }
    }

    private Inscription enregistrer(Assure assure, String email, AssureProfession profession,
                                    AssureIdentifiantsLegaux identifiants,
                                    Map<String, FichierService.Fichier> stockes) {
        // Les champs gérés par le serveur sont forcés (ce que le client envoie est ignoré)
        assure.setIdAssure(null);
        assure.setNumerosAssure(null); // attribué par l'agent à la validation
        assure.setMail(email);
        assure.setDateCreation(LocalDate.now());
        assureRepository.save(assure);

        if (profession != null) {
            profession.setIdProf(null);
            profession.setAssure(assure);
            professionRepository.save(profession);
        }
        if (identifiants != null) {
            identifiants.setIdLegaux(null);
            identifiants.setAssure(assure);
            legauxRepository.save(identifiants);
        }

        Inscription inscription = new Inscription();
        inscription.setAssure(assure);
        inscription.setStatutInscription(StatutInscription.EN_ATTENTE);
        inscription.setDateDemande(LocalDateTime.now());
        inscriptionRepository.save(inscription);

        for (Map.Entry<String, FichierService.Fichier> e : stockes.entrySet()) {
            FichierService.Fichier f = e.getValue();
            DocumentJustificatif document = new DocumentJustificatif();
            document.setAssure(assure);
            document.setInscription(inscription);
            document.setTypeDocument(e.getKey());
            document.setCheminFichier(f.chemin());
            document.setFormatFichier(f.format());
            document.setTailleKo(f.tailleKo());
            document.setDateTeleversement(LocalDateTime.now());
            document.setStatutDocuments(StatutDocument.EN_ATTENTE);
            documentRepository.save(document);
        }
        return inscription;
    }

    // ------------------------------------------------------------------ côté agent

    @Transactional(readOnly = true)
    public List<Inscription> lister(StatutInscription statut) {
        return statut == null
                ? inscriptionRepository.findAllByOrderByDateDemandeDesc()
                : inscriptionRepository.findByStatutInscriptionOrderByDateDemandeDesc(statut);
    }

    @Transactional(readOnly = true)
    public Inscription trouver(Long id) {
        return inscriptionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));
    }

    /** Dossier complet pour l'agent : demande, professions, identifiants légaux et documents. */
    @Transactional(readOnly = true)
    public Map<String, Object> dossier(Long id) {
        Inscription inscription = trouver(id);
        Long idAssure = inscription.getAssure().getIdAssure();
        Map<String, Object> dossier = new LinkedHashMap<>();
        dossier.put("inscription", inscription);
        dossier.put("professions", professionRepository.findByAssure_IdAssure(idAssure));
        dossier.put("identifiants", legauxRepository.findByAssure_IdAssure(idAssure).orElse(null));
        dossier.put("documents", documentRepository.findByInscription_IdInscription(id));
        return dossier;
    }

    /**
     * Valide la demande : attribue le numéro d'assuré, crée le compte client (rôle CLIENT), le lie à l'assuré,
     * génère un mot de passe temporaire (stocké haché) et l'envoie par e-mail.
     * Si l'envoi échoue, toute la validation est annulée et l'agent peut réessayer.
     */
    @Transactional
    public Inscription valider(Long id, String numerosAssure, String emailAgent) {
        Inscription inscription = trouverEnAttente(id);
        Assure assure = inscription.getAssure();

        String numero = numerosAssure == null ? "" : numerosAssure.trim();
        if (numero.isEmpty() || numero.length() > 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le numéro d'assuré est obligatoire (8 caractères maximum)");
        }
        if (assureRepository.existsByNumerosAssure(numero)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce numéro d'assuré existe déjà");
        }
        if (emailDejaUtilise(assure.getMail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un compte existe déjà pour cette adresse e-mail");
        }
        Role roleClient = roleRepository.findByLibRole("CLIENT")
                .orElseThrow(() -> new IllegalStateException("Le rôle CLIENT est absent de la base"));
        Utilisateur agent = trouverAgent(emailAgent);

        // compte client (login + rôle CLIENT), lié à l'assuré
        String motDePasse = genererMotDePasse();
        CompteClient compte = new CompteClient();
        compte.setAssure(assure);
        compte.setEmail(assure.getMail());
        compte.setMotDePasse(passwordEncoder.encode(motDePasse));
        compte.setRole(roleClient);
        compte.setStatutCompte(StatutCompte.ACTIF);
        compte.setDateCreation(LocalDateTime.now());
        compteClientRepository.save(compte);

        assure.setNumerosAssure(numero);

        LocalDateTime maintenant = LocalDateTime.now();
        inscription.setStatutInscription(StatutInscription.VALIDEE);
        inscription.setDateDecision(maintenant);
        inscription.setAgent(agent);
        inscription.setMotifRefus(null);

        // les documents que l'agent n'a pas encore examinés sont validés avec la demande
        for (DocumentJustificatif d : documentRepository.findByInscription_IdInscription(id)) {
            if (d.getStatutDocuments() == StatutDocument.EN_ATTENTE) {
                d.setStatutDocuments(StatutDocument.VALIDE);
                d.setDateValidation(maintenant);
                d.setUtilisateur(agent);
            }
        }

        try {
            mailService.envoyerIdentifiants(compte.getEmail(), motDePasse);
        } catch (MailException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Envoi du mail impossible, validation annulée");
        }
        return inscription;
    }

    /** Refuse la demande : aucun compte n'est créé, le motif est conservé. */
    @Transactional
    public Inscription refuser(Long id, String motif, String emailAgent) {
        if (motif == null || motif.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le motif du refus est obligatoire");
        }
        Inscription inscription = trouverEnAttente(id);
        Utilisateur agent = trouverAgent(emailAgent);
        LocalDateTime maintenant = LocalDateTime.now();

        inscription.setStatutInscription(StatutInscription.REFUSEE);
        inscription.setDateDecision(maintenant);
        inscription.setAgent(agent);
        inscription.setMotifRefus(motif.trim());

        for (DocumentJustificatif d : documentRepository.findByInscription_IdInscription(id)) {
            if (d.getStatutDocuments() == StatutDocument.EN_ATTENTE) {
                d.setStatutDocuments(StatutDocument.REJETE);
                d.setMotifRejet("Inscription refusée");
                d.setDateValidation(maintenant);
                d.setUtilisateur(agent);
            }
        }
        return inscription;
    }

    // ------------------------------------------------------------------ utilitaires

    private void verifierAssure(Assure a) {
        if (a == null || vide(a.getNomAssure()) || vide(a.getMail()) || vide(a.getContact()) || vide(a.getAdresse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nom, e-mail, contact et adresse sont obligatoires");
        }
        if (!EMAIL.matcher(a.getMail().trim()).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse e-mail invalide");
        }
        if (!Boolean.TRUE.equals(a.getPersonneMorale()) && vide(a.getPrenomAssure())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le prénom est obligatoire pour une personne physique");
        }
    }

    private boolean contientUnIdentifiant(AssureIdentifiantsLegaux i) {
        return i != null && (!vide(i.getCin()) || !vide(i.getNif()) || !vide(i.getCif())
                || !vide(i.getNumPasseport()) || !vide(i.getNCarteProf()));
    }

    private boolean vide(String s) {
        return s == null || s.isBlank();
    }

    private Inscription trouverEnAttente(Long id) {
        Inscription inscription = trouver(id);
        if (inscription.getStatutInscription() != StatutInscription.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cette inscription a déjà été traitée");
        }
        return inscription;
    }

    /** Un e-mail identifie un seul compte, parmi les agents/administrateurs et les clients. */
    private boolean emailDejaUtilise(String email) {
        return utilisateurRepository.existsByEmail(email) || compteClientRepository.existsByEmail(email);
    }

    private Utilisateur trouverAgent(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Agent introuvable"));
    }

    private String genererMotDePasse() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
