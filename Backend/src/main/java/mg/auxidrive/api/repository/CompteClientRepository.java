package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface CompteClientRepository extends JpaRepository<CompteClient, Long> {

    Optional<CompteClient> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<CompteClient> findByAssure_IdAssure(Long idAssure);
}
