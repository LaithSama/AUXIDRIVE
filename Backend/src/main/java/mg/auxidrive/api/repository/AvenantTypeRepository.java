package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.AvenantType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvenantTypeRepository extends JpaRepository<AvenantType, String> {
}
