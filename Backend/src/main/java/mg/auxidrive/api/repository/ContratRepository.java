package mg.auxidrive.api.repository;

import java.time.LocalDate;
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
    @EntityGraph(attributePaths = {"assure", "garanties"})
    List<Contrat> findAll();

    @Override
    @EntityGraph(attributePaths = {"assure", "garanties"})
    Optional<Contrat> findById(Long id);

    @EntityGraph(attributePaths = {"assure", "garanties"})
    List<Contrat> findByAssure_IdAssureOrderByIdContrat(Long idAssure);

    List<Contrat> findByStatutContratOrderByIdContrat(StatutContrat statutContrat);

    /* Échéances : assure, offre et branche sont chargés d'avance (open-in-view désactivé),
       le service de planification lit ces relations pour calculer et rédiger les mails. */
    @EntityGraph(attributePaths = {"assure", "offre", "branche"})
    List<Contrat> findByStatutContratAndDateExpirationBetween(StatutContrat statutContrat,
                                                              LocalDate debut, LocalDate fin);

    Optional<Contrat> findByNumPolice(String numPolice);

    boolean existsByNumPolice(String numPolice);

    /** Comptage par statut, pour le tableau de bord administrateur. */
    long countByStatutContrat(StatutContrat statutContrat);
}
