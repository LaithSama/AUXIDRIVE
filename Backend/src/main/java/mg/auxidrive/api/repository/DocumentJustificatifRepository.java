package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface DocumentJustificatifRepository extends JpaRepository<DocumentJustificatif, Long> {

    List<DocumentJustificatif> findByAssure_IdAssure(Long idAssure);

    List<DocumentJustificatif> findByInscription_IdInscription(Long idInscription);
}
