package unicon.Achiva.domain.goal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.goal.dto.GoalListResponse;
import unicon.Achiva.domain.goal.dto.GoalRequest;
import unicon.Achiva.domain.goal.dto.GoalResponse;
import unicon.Achiva.domain.goal.entity.Goal;
import unicon.Achiva.domain.goal.entity.GoalCategory;
import unicon.Achiva.domain.goal.infrastructure.GoalRepository;
import unicon.Achiva.domain.member.MemberErrorCode;
import unicon.Achiva.domain.member.entity.Member;
import unicon.Achiva.domain.member.infrastructure.MemberRepository;
import unicon.Achiva.global.response.GeneralException;

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

    @Transactional
    public void seedDefaultGoals(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new GeneralException(MemberErrorCode.MEMBER_NOT_FOUND));

        // Check if user already has goals
        List<Goal> existingGoals = goalRepository.findByMemberIdAndIsDeletedFalse(memberId);
        if (!existingGoals.isEmpty()) {
            log.info("Member {} already has goals, skipping default goal creation", memberId);
            return;
        }

        // Create default goals for each category
        // Vision (1개)
        Goal visionGoal = Goal.builder()
                .category(GoalCategory.VISION)
                .text("어제보다 더 멋진 나")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        // Mission (3개)
        Goal missionGoal1 = Goal.builder()
                .category(GoalCategory.MISSION)
                .text("일주일에 두번 이상 운동하기")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        Goal missionGoal2 = Goal.builder()
                .category(GoalCategory.MISSION)
                .text("운동 끝나고 기록하기")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        Goal missionGoal3 = Goal.builder()
                .category(GoalCategory.MISSION)
                .text("스트레칭 자주하기")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        // Mindset (3개)
        Goal mindsetGoal1 = Goal.builder()
                .category(GoalCategory.MINDSET)
                .text("운동할 땐 다치지 않게")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        Goal mindsetGoal2 = Goal.builder()
                .category(GoalCategory.MINDSET)
                .text("기록하며 발전하기")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        Goal mindsetGoal3 = Goal.builder()
                .category(GoalCategory.MINDSET)
                .text("운동할 때는 운동에만 집중")
                .clickCount(0)
                .isArchived(false)
                .member(member)
                .build();

        goalRepository.save(visionGoal);
        goalRepository.save(missionGoal1);
        goalRepository.save(missionGoal2);
        goalRepository.save(missionGoal3);
        goalRepository.save(mindsetGoal1);
        goalRepository.save(mindsetGoal2);
        goalRepository.save(mindsetGoal3);

        log.info("Created 7 default goals for member: {}", memberId);
    }
}
