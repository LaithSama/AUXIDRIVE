package mg.auxidrive.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tâche quotidienne des échéances : rappel J-3 puis reconduction tacite des contrats échus.
 * Tourne chaque matin à 8 h (heure du serveur).
 */
@Component
public class RenouvellementTask {

    private static final Logger LOG = LoggerFactory.getLogger(RenouvellementTask.class);

    private final EcheanceRenouvellementService echeanceService;

    public RenouvellementTask(EcheanceRenouvellementService echeanceService) {
        this.echeanceService = echeanceService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void traiterEcheances() {
        int notifies = echeanceService.notifierEcheances();
        if (notifies > 0) {
            LOG.info("{} rappel(s) d'échéance envoyé(s) (J-3)", notifies);
        }
        int renouvelles = echeanceService.renouvelerContratsEchus();
        if (renouvelles > 0) {
            LOG.info("{} contrat(s) renouvelé(s) automatiquement", renouvelles);
        }
    }
}
