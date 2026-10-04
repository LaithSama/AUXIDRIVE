package mg.auxidrive.api.service;

import mg.auxidrive.api.model.CompteClient;
import mg.auxidrive.api.model.StatutCompte;
import mg.auxidrive.api.repository.CompteClientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CompteClientService {
    private final CompteClientRepository compteClientRepository;

    public CompteClientService(CompteClientRepository compteClientRepository) {
        this.compteClientRepository = compteClientRepository;
    }

    @Transactional(readOnly = true)
    public List<CompteClient> listerCompteClient() {
        return compteClientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CompteClient trouverCompte(Long idCompte) {
        return compteClientRepository.findById(idCompte).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,  "Compte Client introuvable"));
    }

    @Transactional
    public CompteClient changerStatut(Long idCompte, StatutCompte statut) {
        CompteClient compteClient = trouverCompte(idCompte);
        compteClient.setStatutCompte(statut);
        return compteClient;
    }
}
