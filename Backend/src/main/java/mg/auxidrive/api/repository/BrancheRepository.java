package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.Branche;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrancheRepository extends JpaRepository<Branche, Long> {
}
