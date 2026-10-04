package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.AgenceRepresentante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgenceRepresentanteRepository extends JpaRepository<AgenceRepresentante, Long> {
}
