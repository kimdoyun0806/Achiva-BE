package unicon.Achiva.domain.moim.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.category.Category;
import unicon.Achiva.domain.moim.entity.Moim;

import java.util.List;

public interface MoimRepository extends JpaRepository<Moim, Long> {
    
    @Query("SELECT DISTINCT m FROM Moim m LEFT JOIN m.categories c WHERE " +
           "(:keyword IS NULL OR m.name LIKE %:keyword% OR m.description LIKE %:keyword%) " +
           "AND (:hasCategories = false OR c IN :categories) " +
           "AND (:isOfficial IS NULL OR m.isOfficial = :isOfficial)")
    Page<Moim> findMoimsBySearchAndCategory(
            @Param("keyword") String keyword, 
            @Param("categories") List<Category> categories, 
            @Param("hasCategories") boolean hasCategories,
            @Param("isOfficial") Boolean isOfficial,
            Pageable pageable);

    List<Moim> findByIsOfficialTrue();
}
