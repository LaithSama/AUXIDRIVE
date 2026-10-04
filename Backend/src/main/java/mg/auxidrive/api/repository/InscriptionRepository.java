package mg.auxidrive.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    List<Inscription> findAllByOrderByDateDemandeDesc();

    List<Inscription> findByStatutInscriptionOrderByDateDemandeDesc(StatutInscription statutInscription);

    boolean existsByAssure_MailAndStatutInscription(String mail, StatutInscription statutInscription);
}
