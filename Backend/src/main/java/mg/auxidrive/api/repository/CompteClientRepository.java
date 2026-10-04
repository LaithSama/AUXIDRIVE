package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.CompteClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompteClientRepository extends JpaRepository<CompteClient, Long> {
    public Optional<CompteClient> findByEmail(String email);
    public boolean existsByEmail(String email);
    public Optional<CompteClient> findByAssureId(Long idAssure);
}
