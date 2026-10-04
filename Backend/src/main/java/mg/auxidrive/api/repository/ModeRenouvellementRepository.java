package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.ModeRenouvellement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeRenouvellementRepository extends JpaRepository<ModeRenouvellement, Long> {
}
