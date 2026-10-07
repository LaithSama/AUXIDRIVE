package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {

    /* open-in-view est désactivé : le code tarifaire est chargé d'avance,
       sinon sa sérialisation (libellé + TACAVA) échouerait. */

    @Override
    @EntityGraph(attributePaths = "codeTarif")
    Optional<Vehicule> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "codeTarif")
    List<Vehicule> findAll();

    @EntityGraph(attributePaths = "codeTarif")
    List<Vehicule> findByAssure_IdAssureOrderByIdVehicule(Long idAssure);

    Optional<Vehicule> findByNumImmatriculationIgnoreCase(String numImmatriculation);

    Optional<Vehicule> findByNumChassisIgnoreCase(String numChassis);

    boolean existsByNumImmatriculationIgnoreCase(String numImmatriculation);

    boolean existsByNumChassisIgnoreCase(String numChassis);
}
