package mg.auxidrive.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface CompteClientRepository extends JpaRepository<CompteClient, Long> {

    /* open-in-view est désactivé : l'assuré est chargé d'avance pour la vue administrateur. */

    @Override
    @EntityGraph(attributePaths = "assure")
    List<CompteClient> findAll();

    @Override
    @EntityGraph(attributePaths = "assure")
    Optional<CompteClient> findById(Long id);

    Optional<CompteClient> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<CompteClient> findByAssure_IdAssure(Long idAssure);

    /** Comptage par statut, pour le tableau de bord administrateur. */
    long countByStatutCompte(StatutCompte statutCompte);

    /** Suppression définitive des comptes dont le délai de rétractation est écoulé. */
    @Modifying
    @Query("delete from CompteClient c where c.dateSuppressionPlanifiee is not null "
            + "and c.dateSuppressionPlanifiee <= :limite")
    int purgeEchues(@Param("limite") LocalDateTime limite);
}
