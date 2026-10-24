package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    /* open-in-view est désactivé : l'assuré est chargé d'avance pour la sérialisation. */

    @Override
    @EntityGraph(attributePaths = {"assure", "agent"})
    Optional<Inscription> findById(Long id);

    @EntityGraph(attributePaths = {"assure", "agent"})
    List<Inscription> findAllByOrderByDateDemandeDesc();

    @EntityGraph(attributePaths = {"assure", "agent"})
    List<Inscription> findByStatutInscriptionOrderByDateDemandeDesc(StatutInscription statutInscription);

    boolean existsByAssure_MailAndStatutInscription(String mail, StatutInscription statutInscription);

    /** Comptage par statut, pour le tableau de bord administrateur. */
    long countByStatutInscription(StatutInscription statutInscription);
}
