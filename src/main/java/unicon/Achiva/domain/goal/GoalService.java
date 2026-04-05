package unicon.Achiva.domain.goal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.goal.dto.GoalListResponse;
import unicon.Achiva.domain.goal.dto.GoalRequest;
import unicon.Achiva.domain.goal.dto.GoalResponse;
import unicon.Achiva.domain.goal.dto.TotalClickCountResponse;
import unicon.Achiva.domain.goal.entity.Goal;
import unicon.Achiva.domain.goal.entity.GoalCategory;
import unicon.Achiva.domain.goal.infrastructure.GoalRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoalService {

    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public GoalResponse createGoal(GoalRequest request, UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        Goal goal = Goal.builder()
                .category(request.category())
                .text(request.text())
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        Goal savedGoal = goalRepository.save(goal);
        log.info("Created goal with id: {} for member: {}", savedGoal.getId(), memberId);

        return GoalResponse.fromEntity(savedGoal);
    }

    public GoalListResponse getActiveGoals(UUID memberId) {
        List<Goal> goals = goalRepository.findByMemberIdAndIsDeletedFalseAndIsArchivedFalse(memberId);
        List<GoalResponse> responses = goals.stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());

        return GoalListResponse.fromGoals(responses);
    }

    public GoalListResponse getArchivedGoals(UUID memberId) {
        List<Goal> goals = goalRepository.findByMemberIdAndIsDeletedFalseAndIsArchivedTrue(memberId);
        List<GoalResponse> responses = goals.stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());

        return GoalListResponse.fromGoals(responses);
    }

    public GoalListResponse getGoalsByCategory(UUID memberId, GoalCategory category, Boolean archived) {
        List<Goal> goals;
        if (archived != null && archived) {
            goals = goalRepository.findByMemberIdAndCategoryAndIsDeletedFalseAndIsArchivedTrue(memberId, category);
        } else {
            goals = goalRepository.findByMemberIdAndCategoryAndIsDeletedFalseAndIsArchivedFalse(memberId, category);
        }

        List<GoalResponse> responses = goals.stream()
                .map(GoalResponse::fromEntity)
                .collect(Collectors.toList());

        return GoalListResponse.fromGoals(responses);
    }

    public GoalResponse getGoal(UUID goalId, UUID memberId) {
        Goal goal = goalRepository.findByIdAndMemberIdAndIsDeletedFalse(goalId, memberId)
                .orElseThrow(() -> new GeneralException(GoalErrorCode.GOAL_NOT_FOUND));

        return GoalResponse.fromEntity(goal);
    }

    @Transactional
    public GoalResponse updateGoal(UUID goalId, GoalRequest request, UUID memberId) {
        Goal goal = goalRepository.findByIdAndMemberIdAndIsDeletedFalse(goalId, memberId)
                .orElseThrow(() -> new GeneralException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.updateText(request.text());
        log.info("Updated goal with id: {} for member: {}", goalId, memberId);

        return GoalResponse.fromEntity(goal);
    }

    @Transactional
    public void deleteGoal(UUID goalId, UUID memberId) {
        Goal goal = goalRepository.findByIdAndMemberIdAndIsDeletedFalse(goalId, memberId)
                .orElseThrow(() -> new GeneralException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.markAsDeleted();
        log.info("Soft deleted goal with id: {} for member: {}", goalId, memberId);
    }

    @Transactional
    public GoalResponse toggleArchive(UUID goalId, UUID memberId) {
        Goal goal = goalRepository.findByIdAndMemberIdAndIsDeletedFalse(goalId, memberId)
                .orElseThrow(() -> new GeneralException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.toggleArchive();
        log.info("Toggled archive status for goal with id: {} to: {}", goalId, goal.getIsArchived());

        return GoalResponse.fromEntity(goal);
    }

    @Transactional
    public GoalResponse incrementClickCount(UUID goalId, UUID memberId) {
        Goal goal = goalRepository.findByIdAndMemberIdAndIsDeletedFalse(goalId, memberId)
                .orElseThrow(() -> new GeneralException(GoalErrorCode.GOAL_NOT_FOUND));

        goal.incrementClickCount();
        log.info("Incremented click count for goal with id: {} to: {}", goalId, goal.getClickCount());

        return GoalResponse.fromEntity(goal);
    }

    public TotalClickCountResponse getTotalClickCountByDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        long totalClickCount = goalRepository.sumClickCountByDateRange(memberId, startDate, endDate);
        return new TotalClickCountResponse(totalClickCount);
    }
}
