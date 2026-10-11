package mg.auxidrive.api.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mg.auxidrive.api.model.Role;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.model.StatutContrat;
import mg.auxidrive.api.model.StatutInscription;
import mg.auxidrive.api.repository.AssureRepository;
import mg.auxidrive.api.repository.CompteClientRepository;
import mg.auxidrive.api.repository.ContratRepository;
import mg.auxidrive.api.repository.InscriptionRepository;
import mg.auxidrive.api.repository.RoleRepository;
import mg.auxidrive.api.repository.UtilisateurRepository;

/** Indicateurs et référentiels de l'espace d'administration (rôle ADMIN). */
@Service
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final CompteClientRepository compteClientRepository;
    private final AssureRepository assureRepository;
    private final ContratRepository contratRepository;
    private final InscriptionRepository inscriptionRepository;
    private final RoleRepository roleRepository;

    public AdminService(UtilisateurRepository utilisateurRepository,
                        CompteClientRepository compteClientRepository,
                        AssureRepository assureRepository,
                        ContratRepository contratRepository,
                        InscriptionRepository inscriptionRepository,
                        RoleRepository roleRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.compteClientRepository = compteClientRepository;
        this.assureRepository = assureRepository;
        this.contratRepository = contratRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.roleRepository = roleRepository;
    }

    /** Chiffres du tableau de bord administrateur. */
    @Transactional(readOnly = true)
    public Map<String, Object> statistiques() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("utilisateurs", utilisateurRepository.count());
        stats.put("agents", utilisateurRepository.countByRole_LibRole("AGENT"));
        stats.put("administrateurs", utilisateurRepository.countByRole_LibRole("ADMIN"));
        stats.put("comptesClients", compteClientRepository.count());
        stats.put("comptesClientsActifs", compteClientRepository.countByStatutCompte(StatutCompte.ACTIF));
        stats.put("assures", assureRepository.count());
        stats.put("contrats", contratRepository.count());
        stats.put("contratsEnAttente", contratRepository.countByStatutContrat(StatutContrat.EN_ATTENTE));
        stats.put("contratsValides", contratRepository.countByStatutContrat(StatutContrat.VALIDE));
        stats.put("contratsRefuses", contratRepository.countByStatutContrat(StatutContrat.REFUSE));
        stats.put("inscriptionsEnAttente",
                inscriptionRepository.countByStatutInscription(StatutInscription.EN_ATTENTE));
        return stats;
    }

    /** Rôles disponibles (CLIENT, AGENT, ADMIN) pour les sélecteurs du front. */
    @Transactional(readOnly = true)
    public List<Role> roles() {
        return roleRepository.findAll();
    }
}
