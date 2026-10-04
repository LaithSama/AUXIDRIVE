package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface AssureIdentifiantsLegauxRepository extends JpaRepository<AssureIdentifiantsLegaux, Long> {

    Optional<AssureIdentifiantsLegaux> findByAssure_IdAssure(Long idAssure);
}
