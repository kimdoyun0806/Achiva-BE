package unicon.Achiva.domain.goal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.goal.dto.GoalListResponse;
import unicon.Achiva.domain.goal.dto.GoalRequest;
import unicon.Achiva.domain.goal.dto.GoalResponse;
import unicon.Achiva.domain.goal.dto.TotalClickCountResponse;
import unicon.Achiva.domain.goal.entity.GoalCategory;
import unicon.Achiva.global.response.ApiResponseForm;

import java.time.LocalDateTime;
import java.util.UUID;

@Tag(name = "Goal", description = "목표 관리 API")
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;
    private final AuthService authService;

    @Operation(summary = "새 목표 생성", description = "새로운 목표를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponseForm<GoalResponse>> createGoal(
            @Valid @RequestBody GoalRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalResponse response = goalService.createGoal(request, memberId);
        return ResponseEntity.ok(ApiResponseForm.created(response, "목표 생성 성공"));
    }

    @Operation(summary = "활성 목표 목록 조회", description = "현재 사용자의 활성 목표 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponseForm<GoalListResponse>> getActiveGoals(
            @RequestParam(required = false) GoalCategory category
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalListResponse response;

        if (category != null) {
            response = goalService.getGoalsByCategory(memberId, category, false);
        } else {
            response = goalService.getActiveGoals(memberId);
        }

        return ResponseEntity.ok(ApiResponseForm.success(response, "활성 목표 목록 조회 성공"));
    }

    @Operation(summary = "보관된 목표 목록 조회", description = "현재 사용자의 보관된 목표 목록을 조회합니다.")
    @GetMapping("/archived")
    public ResponseEntity<ApiResponseForm<GoalListResponse>> getArchivedGoals(
            @RequestParam(required = false) GoalCategory category
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalListResponse response;

        if (category != null) {
            response = goalService.getGoalsByCategory(memberId, category, true);
        } else {
            response = goalService.getArchivedGoals(memberId);
        }

        return ResponseEntity.ok(ApiResponseForm.success(response, "보관된 목표 목록 조회 성공"));
    }

    @Operation(summary = "특정 목표 조회", description = "ID로 특정 목표를 조회합니다.")
    @GetMapping("/{goalId}")
    public ResponseEntity<ApiResponseForm<GoalResponse>> getGoal(
            @PathVariable UUID goalId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalResponse response = goalService.getGoal(goalId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "목표 조회 성공"));
    }

    @Operation(summary = "목표 수정", description = "기존 목표의 내용을 수정합니다.")
    @PutMapping("/{goalId}")
    public ResponseEntity<ApiResponseForm<GoalResponse>> updateGoal(
            @PathVariable UUID goalId,
            @Valid @RequestBody GoalRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalResponse response = goalService.updateGoal(goalId, request, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "목표 수정 성공"));
    }

    @Operation(summary = "목표 삭제", description = "목표를 삭제합니다 (소프트 삭제).")
    @DeleteMapping("/{goalId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteGoal(
            @PathVariable UUID goalId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        goalService.deleteGoal(goalId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "목표 삭제 성공"));
    }

    @Operation(summary = "목표 보관 상태 토글", description = "목표의 보관 상태를 토글합니다.")
    @PatchMapping("/{goalId}/archive")
    public ResponseEntity<ApiResponseForm<GoalResponse>> toggleArchive(
            @PathVariable UUID goalId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalResponse response = goalService.toggleArchive(goalId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "목표 보관 상태 변경 성공"));
    }

    @Operation(summary = "목표 클릭 카운트 증가", description = "목표의 클릭 카운트를 1 증가시킵니다.")
    @PatchMapping("/{goalId}/click")
    public ResponseEntity<ApiResponseForm<GoalResponse>> incrementClickCount(
            @PathVariable UUID goalId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        GoalResponse response = goalService.incrementClickCount(goalId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "클릭 카운트 증가 성공"));
    }

    @Operation(summary = "기본 목표 생성", description = "사용자를 위한 기본 목표를 생성합니다.")
    @PostMapping("/seed-defaults")
    public ResponseEntity<ApiResponseForm<Void>> seedDefaultGoals() {
        UUID memberId = authService.getMemberIdFromToken();
        goalService.seedDefaultGoals(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "기본 목표 생성 성공"));
    }

    @Operation(
            summary = "특정 기간 동안 본인의 목표 클릭 수 합계 조회",
            description = "기간을 지정하여 본인의 목표들의 클릭 수 합계를 조회합니다. " +
                    "기간을 지정하지 않으면 전체 기간의 클릭 수를 조회합니다. " +
                    "올해 기록만 조회하려면 startDate에 올해 1월 1일 00:00:00을 설정하세요."
    )
    @GetMapping("/my-total-click-count")
    public ResponseEntity<ApiResponseForm<TotalClickCountResponse>> getMyTotalClickCount(
            @Parameter(description = "시작 일시 ex) 2024-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @Parameter(description = "종료 일시 ex) 2024-12-31T23:59:59)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        TotalClickCountResponse response = goalService.getTotalClickCountByDateRange(memberId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 기간 동안의 목표 클릭 수 합계 조회 성공"));
    }
}
