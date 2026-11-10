package mg.auxidrive.api.repository;

import java.util.List;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface ContratAutoRepository extends JpaRepository<ContratAuto, Long> {

    List<ContratAuto> findByContrat_IdContrat(Long idContrat);

    List<ContratAuto> findByVehicule_IdVehicule(Long idVehicule);

    boolean existsByVehicule_IdVehicule(Long idVehicule);

    boolean existsByContrat_IdContratAndVehicule_IdVehicule(Long idContrat, Long idVehicule);

    /** Intervalles de couverture demi-ouverts : [dateEffet, dateExpiration). */
    @Query("select c from ContratAuto ca join ca.contrat c "
            + "where ca.vehicule.idVehicule = :idVehicule and c.statutContrat = :statut "
            + "and c.dateEffet < :fin and c.dateExpiration > :debut "
            + "and (:exclureId is null or c.idContrat <> :exclureId)")
    List<Contrat> trouverContratsChevauchants(@Param("idVehicule") Long idVehicule,
                                              @Param("statut") StatutContrat statut,
                                              @Param("debut") LocalDate debut,
                                              @Param("fin") LocalDate fin,
                                              @Param("exclureId") Long exclureId);
}
