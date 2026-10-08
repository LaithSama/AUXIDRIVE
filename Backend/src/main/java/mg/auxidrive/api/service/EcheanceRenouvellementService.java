package mg.auxidrive.api.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import mg.auxidrive.api.model.Assure;
import mg.auxidrive.api.model.Contrat;
import mg.auxidrive.api.model.ContratAuto;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.repository.ContratAutoRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.service.MailService.DonneesEcheance;
import mg.auxidrive.api.service.MailService.DonneesRenouvellement;

/**
 * Rappels d'échéance et renouvellement automatique des contrats.
 *
 * Chaque jour : un mail part trois jours avant l'échéance (une seule fois par échéance, grâce à
 * dateNotifEcheance), puis tout contrat validé échu depuis moins de sept jours est reconduit
 * tacitement — même numéro de police, prime recalculée, facture de renouvellement émise — et un
 * mail de confirmation part à l'assuré.
 *
 * Aucune transaction n'enveloppe les boucles : chaque renouvellement a la sienne, ainsi une
 * prime inexploitable n'empêche pas les autres contrats d'être traités. Les entités sont chargées
 * avec assure, offre et branche (EntityGraph) pour que leur lecture reste possible hors transaction.
 */
@Service
public class EcheanceRenouvellementService {

    private static final Logger log = LoggerFactory.getLogger(EcheanceRenouvellementService.class);

    /** Délai du rappel avant échéance, en jours. */
    public static final int DELAI_NOTIFICATION_JOURS = 3;

    /** Contrats échus depuis plus de ce délai ne sont plus renouvelés automatiquement. */
    public static final int FENETRE_RATTRAPAGE_JOURS = 7;

    private final ContratRepository contratRepository;
    private final ContratAutoRepository contratAutoRepository;
    private final ContratService contratService;
    private final MailService mailService;

    public EcheanceRenouvellementService(ContratRepository contratRepository,
                                         ContratAutoRepository contratAutoRepository,
                                         ContratService contratService,
                                         MailService mailService) {
        this.contratRepository = contratRepository;
        this.contratAutoRepository = contratAutoRepository;
        this.contratService = contratService;
        this.mailService = mailService;
    }

    /** Contrats valides expirant d'ici trois jours et pas encore rappelés : envoi du mail J-3. */
    public int notifierEcheances() {
        LocalDate today = LocalDate.now();
        List<Contrat> contrats = contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today, today.plusDays(DELAI_NOTIFICATION_JOURS));
        int envoyes = 0;
        for (Contrat contrat : contrats) {
            if (contrat.getDateNotifEcheance() != null) {
                continue;
            }
            Assure assure = contrat.getAssure();
            if (assure == null || assure.getMail() == null || assure.getMail().isBlank()) {
                log.warn("Contrat {} : aucune adresse e-mail chez l'assuré, rappel d'échéance non envoyé",
                        contrat.getNumPolice());
            } else {
                mailService.envoyerEcheanceProche(new DonneesEcheance(
                        assure.getMail(), assure.getCivilite(), assure.getNomAssure(), assure.getPrenomAssure(),
                        contrat.getNumPolice(), contrat.getDateEffet(), contrat.getDateExpiration(),
                        contrat.getPrimeTtcCourante(), vehicules(contrat)));
                envoyes++;
            }
            contrat.setDateNotifEcheance(today);
            contratRepository.save(contrat);
        }
        return envoyes;
    }

    /** Reconduction tacite des contrats valides échus depuis sept jours ou moins. */
    public int renouvelerContratsEchus() {
        LocalDate today = LocalDate.now();
        List<Contrat> contrats = contratRepository.findByStatutContratAndDateExpirationBetween(
                StatutContrat.VALIDE, today.minusDays(FENETRE_RATTRAPAGE_JOURS), today);
        int renouvelles = 0;
        for (Contrat contrat : contrats) {
            try {
                contratService.renouveler(contrat);
            } catch (RuntimeException e) {
                log.error("Renouvellement du contrat {} impossible : {}",
                        contrat.getNumPolice(), e.getMessage(), e);
                continue;
            }
            Assure assure = contrat.getAssure();
            if (assure == null || assure.getMail() == null || assure.getMail().isBlank()) {
                log.warn("Contrat {} renouvelé mais aucune adresse e-mail chez l'assuré",
                        contrat.getNumPolice());
            } else {
                mailService.envoyerRenouvellement(new DonneesRenouvellement(
                        assure.getMail(), assure.getCivilite(), assure.getNomAssure(), assure.getPrenomAssure(),
                        contrat.getNumPolice(), contrat.getDateEffet(), contrat.getDateExpiration(),
                        contrat.getPrimeTtcCourante()));
            }
            renouvelles++;
        }
        return renouvelles;
    }

    /** Liste des véhicules couverts, formatée pour le corps du mail. */
    private String vehicules(Contrat contrat) {
        List<ContratAuto> liens = contratAutoRepository.findByContrat_IdContrat(contrat.getIdContrat());
        if (liens.isEmpty()) {
            return "—";
        }
        StringBuilder b = new StringBuilder();
        for (ContratAuto lien : liens) {
            if (lien.getVehicule() == null) {
                continue;
            }
            if (b.length() > 0) {
                b.append("; ");
            }
            b.append(lien.getVehicule().getMarque()).append(' ').append(lien.getVehicule().getModele());
            if (lien.getVehicule().getNumImmatriculation() != null
                    && !lien.getVehicule().getNumImmatriculation().isBlank()) {
                b.append(" (").append(lien.getVehicule().getNumImmatriculation()).append(')');
            }
        }
        return b.length() == 0 ? "—" : b.toString();
    }
}
