package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.AssureIdentifiantsLegaux;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssureIdentifiantsLegauxRepository extends JpaRepository<AssureIdentifiantsLegaux, Long> {
    public Optional<AssureIdentifiantsLegaux> findByAssureId(Long idAssure);
}
