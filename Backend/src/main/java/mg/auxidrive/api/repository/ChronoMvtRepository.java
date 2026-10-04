package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.ChronoMvt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChronoMvtRepository extends JpaRepository<ChronoMvt, Long> {
}
