package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.GarantieType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GarantieTypeRepository extends JpaRepository<GarantieType, String> {
}
