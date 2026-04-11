package unicon.Achiva.domain.scripture.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import unicon.Achiva.domain.scripture.entity.ArticleScriptureReading;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ArticleScriptureReadingRepository extends JpaRepository<ArticleScriptureReading, UUID> {

    @Query("""
            select r
              from ArticleScriptureReading r
              join fetch r.article a
             where a.member.id = :memberId
               and a.isDeleted = false
               and r.readAt between :startDate and :endDate
             order by r.readAt desc, a.createdAt desc
            """)
    List<ArticleScriptureReading> findCalendarItemsByMemberId(
            @Param("memberId") UUID memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
