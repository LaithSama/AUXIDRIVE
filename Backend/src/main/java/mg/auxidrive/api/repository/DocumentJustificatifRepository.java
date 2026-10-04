package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.DocumentJustificatif;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentJustificatifRepository extends JpaRepository<DocumentJustificatif, Long> {
    public List<DocumentJustificatif> findByAssureId(Long idAssure);
    public List<DocumentJustificatif> findByInscriptionId(Long idInscription);
}
