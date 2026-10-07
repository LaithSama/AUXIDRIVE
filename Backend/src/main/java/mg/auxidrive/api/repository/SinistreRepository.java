package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    /* open-in-view est désactivé : le contrat, l'assuré et le véhicule sont chargés
       d'avance, sinon la sérialisation (nouveaux champs enrichis) échouerait. */

    @Override
    @EntityGraph(attributePaths = {"contrat", "contrat.assure", "vehicule", "vehicule.codeTarif"})
    List<Sinistre> findAll();

    @Override
    @EntityGraph(attributePaths = {"contrat", "contrat.assure", "vehicule", "vehicule.codeTarif"})
    Optional<Sinistre> findById(Long id);

    @EntityGraph(attributePaths = {"contrat", "contrat.assure", "vehicule", "vehicule.codeTarif"})
    List<Sinistre> findByContrat_IdContratOrderByIdSinistre(Long idContrat);

    @EntityGraph(attributePaths = {"contrat", "contrat.assure", "vehicule", "vehicule.codeTarif"})
    List<Sinistre> findByContrat_Assure_IdAssureOrderByIdSinistre(Long idAssure);
}
