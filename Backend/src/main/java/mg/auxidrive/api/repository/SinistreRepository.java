package mg.auxidrive.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface SinistreRepository extends JpaRepository<Sinistre, Long> {

    List<Sinistre> findByContrat_IdContratOrderByIdSinistre(Long idContrat);

    List<Sinistre> findByContrat_Assure_IdAssureOrderByIdSinistre(Long idAssure);
}