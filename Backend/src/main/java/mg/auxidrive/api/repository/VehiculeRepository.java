package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    List<Vehicule> findByAssure_IdAssureOrderByIdVehicule(Long idAssure);

    Optional<Vehicule> findByNumImmatriculationIgnoreCase(String numImmatriculation);

    Optional<Vehicule> findByNumChassisIgnoreCase(String numChassis);

    boolean existsByNumImmatriculationIgnoreCase(String numImmatriculation);

    boolean existsByNumChassisIgnoreCase(String numChassis);
}