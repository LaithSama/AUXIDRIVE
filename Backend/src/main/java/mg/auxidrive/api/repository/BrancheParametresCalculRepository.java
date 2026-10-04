package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.BrancheParametresCalcul;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrancheParametresCalculRepository extends JpaRepository<BrancheParametresCalcul, Long> {
}
