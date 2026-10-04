package mg.auxidrive.api.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.repository.CompteClientRepository;

/**
 * Comptes clients. Un compte n'est jamais créé ici (c'est la validation d'une inscription qui le crée)
 * ni supprimé (il est lié à un assuré) : l'agent peut seulement le consulter et le suspendre ou le réactiver.
 */
@Service
public class CompteClientService {

    private final CompteClientRepository compteClientRepository;

    public CompteClientService(CompteClientRepository compteClientRepository) {
        this.compteClientRepository = compteClientRepository;
    }

    @Transactional(readOnly = true)
    public List<CompteClient> lister() {
        return compteClientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CompteClient trouver(Long id) {
        return compteClientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte client introuvable"));
    }

    @Transactional
    public CompteClient changerStatut(Long id, StatutCompte statut) {
        CompteClient compte = trouver(id);
        compte.setStatutCompte(statut);
        return compte;
    }
}
