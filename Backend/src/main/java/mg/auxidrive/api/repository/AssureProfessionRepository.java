package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.AssureProfession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssureProfessionRepository extends JpaRepository<AssureProfession, Long> {
    public List<AssureProfession> findByAssureId(Long idAssure);
}
