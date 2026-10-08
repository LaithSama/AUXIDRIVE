package mg.auxidrive.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Suppression définitive des comptes clients dont le délai de rétractation de 72 h est écoulé.
 * Tourne toutes les 5 minutes : la purge intervient donc au plus 5 minutes après l'échéance.
 */
@Component
public class PurgeComptesTask {

    private static final Logger LOG = LoggerFactory.getLogger(PurgeComptesTask.class);

    private final ProfilService profilService;

    public PurgeComptesTask(ProfilService profilService) {
        this.profilService = profilService;
    }

    @Scheduled(fixedDelay = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void purgerComptesEchus() {
        int purge = profilService.purgerEchues();
        if (purge > 0) {
            LOG.info("{} compte(s) client(s) définitivement supprimé(s) après le délai de rétractation", purge);
        }
    }
}
