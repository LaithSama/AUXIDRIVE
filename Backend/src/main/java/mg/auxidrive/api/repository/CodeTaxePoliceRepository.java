package mg.auxidrive.api.repository;

import mg.auxidrive.api.model.CodeTaxePolice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeTaxePoliceRepository extends JpaRepository<CodeTaxePolice, Long> {
}
