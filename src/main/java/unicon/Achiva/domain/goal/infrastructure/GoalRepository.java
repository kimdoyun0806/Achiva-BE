package unicon.Achiva.domain.goal.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
