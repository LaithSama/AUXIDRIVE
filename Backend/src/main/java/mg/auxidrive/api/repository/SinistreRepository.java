package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.Sinistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {
}
