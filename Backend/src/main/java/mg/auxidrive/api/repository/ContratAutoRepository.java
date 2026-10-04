package mg.auxidrive.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface ContratAutoRepository extends JpaRepository<ContratAuto, Long> {

    List<ContratAuto> findByContrat_IdContrat(Long idContrat);

    List<ContratAuto> findByVehicule_IdVehicule(Long idVehicule);

    boolean existsByContrat_IdContratAndVehicule_IdVehicule(Long idContrat, Long idVehicule);
}