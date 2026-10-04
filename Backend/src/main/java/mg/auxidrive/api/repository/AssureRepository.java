package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.Assure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssureRepository extends JpaRepository<Assure, Long> {
    public Optional<Assure> findByNumerosAssure(String numerosAssure);
    public boolean existsByNumerosAssure(String numerosAssure);
}
