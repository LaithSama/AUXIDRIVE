package mg.auxidrive.api.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.AssureIdentifiantsLegaux;
import mg.auxidrive.api.model.AssureProfession;
import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.service.InscriptionService;

/**
 * POST /api/inscriptions est public (le client n'a pas encore de compte) ;
 * toutes les autres routes sont réservées aux agents et administrateurs.
 */
@RestController
@RequestMapping("/api/inscriptions")
public class InscriptionController {

    private final InscriptionService inscriptionService;

    public InscriptionController(InscriptionService inscriptionService) {
        this.inscriptionService = inscriptionService;
    }

    /**
     * Requête multipart/form-data. Parties JSON (Content-Type application/json) : "assure" (obligatoire),
     * "profession" et "identifiants" (facultatives). Fichiers facultatifs : cin, carteGrise, carteRose, modeleN1.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> soumettre(
            @RequestPart("assure") Assure assure,
            @RequestPart(value = "profession", required = false) AssureProfession profession,
            @RequestPart(value = "identifiants", required = false) AssureIdentifiantsLegaux identifiants,
            @RequestPart(value = "cin", required = false) MultipartFile cin,
            @RequestPart(value = "carteGrise", required = false) MultipartFile carteGrise,
            @RequestPart(value = "carteRose", required = false) MultipartFile carteRose,
            @RequestPart(value = "modeleN1", required = false) MultipartFile modeleN1) {

        Map<String, MultipartFile> fichiers = new LinkedHashMap<>();
        ajouter(fichiers, "CIN", cin);
        ajouter(fichiers, "CARTE_GRISE", carteGrise);
        ajouter(fichiers, "CARTE_ROSE", carteRose);
        ajouter(fichiers, "MODELE_N1", modeleN1);

        Inscription inscription = inscriptionService.soumettre(assure, profession, identifiants, fichiers);
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("idInscription", inscription.getIdInscription());
        reponse.put("statut", inscription.getStatutInscription());
        reponse.put("message", "Demande enregistrée, en attente de validation par un agent");
        return ResponseEntity.status(HttpStatus.CREATED).body(reponse);
    }

    /** GET /api/inscriptions?statut=EN_ATTENTE (sans paramètre : toutes les demandes). */
    @GetMapping
    public List<Inscription> lister(@RequestParam(required = false) StatutInscription statut) {
        return inscriptionService.lister(statut);
    }

    /** Dossier complet : demande, professions, identifiants légaux, documents. */
    @GetMapping("/{id}")
    public Map<String, Object> dossier(@PathVariable Long id) {
        return inscriptionService.dossier(id);
    }

    @PostMapping("/{id}/valider")
    public Inscription valider(@PathVariable Long id, @RequestParam String numerosAssure,
                               Authentication authentication) {
        return inscriptionService.valider(id, numerosAssure, authentication.getName());
    }

    @PostMapping("/{id}/refuser")
    public Inscription refuser(@PathVariable Long id, @RequestParam String motif,
                               Authentication authentication) {
        return inscriptionService.refuser(id, motif, authentication.getName());
    }

    private void ajouter(Map<String, MultipartFile> fichiers, String type, MultipartFile fichier) {
        if (fichier != null && !fichier.isEmpty()) {
            fichiers.put(type, fichier);
        }
    }
}
