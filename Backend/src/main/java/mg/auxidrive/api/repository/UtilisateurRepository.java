package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    public Optional<Utilisateur> findByEmail(String email);
    public boolean existsByEmail(String email);
}
