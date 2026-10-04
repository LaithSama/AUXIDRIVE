package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.Inscription;
import mg.auxidrive.api.model.StatutInscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {
    public List<Inscription> findByAllOrderByDateDemandeDesc();
    public List<Inscription> findByStatutInscriptionOrderByDateDemandeDesc(StatutInscription statutInscription);
    public boolean existsByAssureMailAndStatutInscription(String email, StatutInscription statutInscription);
}
