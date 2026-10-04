package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    List<Contrat> findByAssure_IdAssureOrderByIdContrat(Long idAssure);

    List<Contrat> findByStatutContratOrderByIdContrat(StatutContrat statutContrat);

    Optional<Contrat> findByNumPolice(String numPolice);

    boolean existsByNumPolice(String numPolice);
}