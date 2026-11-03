package mg.auxidrive.api.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import mg.auxidrive.api.model.*;

@Repository
public interface DocumentJustificatifRepository extends JpaRepository<DocumentJustificatif, Long> {

    List<DocumentJustificatif> findByAssure_IdAssure(Long idAssure);

    List<DocumentJustificatif> findByInscription_IdInscription(Long idInscription);

    /* open-in-view est désactivé : l'assuré et le dossier sont chargés d'avance pour les vues exposées. */

    /** Pièces d'un assuré, la plus récente d'abord (profil client). */
    @EntityGraph(attributePaths = { "assure", "inscription" })
    List<DocumentJustificatif> findByAssure_IdAssureOrderByDateTeleversementDescIdDocumentDesc(Long idAssure);

    /** Recherche par identifiant ET par assuré : aucune pièce d'un autre client ne peut être atteinte. */
    @EntityGraph(attributePaths = { "assure", "inscription" })
    Optional<DocumentJustificatif> findByIdDocumentAndAssure_IdAssure(Long idDocument, Long idAssure);

    boolean existsByAssure_IdAssureAndTypeDocument(Long idAssure, String typeDocument);

    /**
     * File de vérification « Documents à vérifier » : les pièces ajoutées ou remplacées depuis le
     * profil. Les pièces d'un dossier encore ouvert (EN_ATTENTE ou CORRECTION_DEMANDEE) restent
     * traitées dans la modale d'inscription et sont donc exclues.
     */
    @EntityGraph(attributePaths = { "assure", "inscription" })
    @Query("""
            select d from DocumentJustificatif d
            left join d.inscription i
            where d.statutDocuments = :statut
              and (i is null or i.statutInscription not in :statutsActifs)
            order by d.dateTeleversement asc, d.idDocument asc
            """)
    List<DocumentJustificatif> findAVerifier(@Param("statut") StatutDocument statut,
                                             @Param("statutsActifs") Collection<StatutInscription> statutsActifs);
}
