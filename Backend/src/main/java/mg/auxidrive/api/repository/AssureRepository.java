package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface AssureRepository extends JpaRepository<Assure, Long> {

    Optional<Assure> findByNumerosAssure(String numerosAssure);

    boolean existsByNumerosAssure(String numerosAssure);
}
