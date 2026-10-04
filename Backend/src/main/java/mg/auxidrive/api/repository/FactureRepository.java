package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByContrat_IdContratOrderByIdFacture(Long idContrat);

    List<Facture> findByContrat_Assure_IdAssureOrderByIdFacture(Long idAssure);

    Optional<Facture> findFirstByContrat_IdContratAndTypeFacture(Long idContrat, TypeFacture typeFacture);
}