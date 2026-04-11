package unicon.Achiva.domain.moim.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.moim.entity.Moim;

import java.util.List;
import java.util.Optional;

public interface MoimRepository extends JpaRepository<Moim, Long> {
    
    @Query("SELECT DISTINCT m FROM Moim m WHERE " +
           "m.organization.id = :organizationId AND " +
           "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.description LIKE %:keyword%) " +
           "AND (:isOfficial IS NULL OR m.isOfficial = :isOfficial)")
    Page<Moim> findMoimsBySearchAndCategory(
            @Param("organizationId") Long organizationId,
            @Param("keyword") String keyword, 
            @Param("isOfficial") Boolean isOfficial,
            Pageable pageable);

    @Query("""
            SELECT DISTINCT m
              FROM Moim m
              LEFT JOIN FETCH m.members mm
              LEFT JOIN FETCH mm.member
             WHERE m.organization.id = :organizationId
            """)
    List<Moim> findAllWithMembers(@Param("organizationId") Long organizationId);

    List<Moim> findByIsOfficialTrue();

    Optional<Moim> findByIdAndOrganization_Id(Long id, Long organizationId);
}
