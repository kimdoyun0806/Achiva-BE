package unicon.Achiva.domain.goal.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unicon.Achiva.domain.goal.entity.Goal;
import unicon.Achiva.domain.goal.entity.GoalCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    // Find all active goals for a member
    List<Goal> findByMemberIdAndIsDeletedFalseAndIsArchivedFalse(UUID memberId);

    // Find all archived goals for a member
    List<Goal> findByMemberIdAndIsDeletedFalseAndIsArchivedTrue(UUID memberId);

    // Find active goals by category for a member
    List<Goal> findByMemberIdAndCategoryAndIsDeletedFalseAndIsArchivedFalse(UUID memberId, GoalCategory category);

    // Find archived goals by category for a member
    List<Goal> findByMemberIdAndCategoryAndIsDeletedFalseAndIsArchivedTrue(UUID memberId, GoalCategory category);

    // Find a specific goal by id and member (for authorization check)
    Optional<Goal> findByIdAndMemberIdAndIsDeletedFalse(UUID id, UUID memberId);

    // Find all goals (active and archived) for a member
    List<Goal> findByMemberIdAndIsDeletedFalse(UUID memberId);

    /**
     * 특정 기간 동안 특정 사용자의 목표들의 클릭 수 합계를 조회합니다.
     * @param memberId 사용자 ID
     * @param startDate 시작 일시 (null이면 제한 없음)
     * @param endDate 종료 일시 (null이면 제한 없음)
     * @return 클릭 수 합계
     */
    @Query("""
            SELECT COALESCE(SUM(g.clickCount), 0)
            FROM Goal g
            WHERE g.member.id = :memberId
            AND g.isDeleted = false
            AND (:startDate is null or g.createdAt >= :startDate)
            AND (:endDate is null or g.createdAt <= :endDate)
            """)
    long sumClickCountByDateRange(@Param("memberId") UUID memberId,
                                   @Param("startDate") java.time.LocalDateTime startDate,
                                   @Param("endDate") java.time.LocalDateTime endDate);
}
