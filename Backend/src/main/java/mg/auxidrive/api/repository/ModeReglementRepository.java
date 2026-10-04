package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.ModeReglement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeReglementRepository extends JpaRepository<ModeReglement, Long> {
}
