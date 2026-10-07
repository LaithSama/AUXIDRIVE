package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface ContratRepository extends JpaRepository<Contrat, Long> {

    /* open-in-view est désactivé : l'assuré est chargé d'avance,
       sinon la sérialisation (nouveaux champs enrichis) échouerait. */

    @Override
    @EntityGraph(attributePaths = "assure")
    List<Contrat> findAll();

    @Override
    @EntityGraph(attributePaths = "assure")
    Optional<Contrat> findById(Long id);

    @EntityGraph(attributePaths = "assure")
    List<Contrat> findByAssure_IdAssureOrderByIdContrat(Long idAssure);

    List<Contrat> findByStatutContratOrderByIdContrat(StatutContrat statutContrat);

    Optional<Contrat> findByNumPolice(String numPolice);

    boolean existsByNumPolice(String numPolice);
}
