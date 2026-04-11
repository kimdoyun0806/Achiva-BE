package unicon.Achiva.domain.cheering;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.cheering.dto.*;
import unicon.Achiva.global.response.ApiResponseForm;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CheeringController {

    private final CheeringService cheeringService;
    private final AuthService authService;

    @Operation(summary = "응원 작성")
    @PostMapping("/api/articles/{articleId}/cheerings")
    public ResponseEntity<ApiResponseForm<CheeringResponse>> createCheering(
            @RequestBody CheeringRequest request,
            @RequestParam UUID articleId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        CheeringResponse response = cheeringService.createCheering(request, memberId, articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 작성 성공"));
    }

    @Operation(summary = "응원 수정")
    @PutMapping("api/articles/{articleId}/cheerings/{cheeringId}")
    public ResponseEntity<ApiResponseForm<CheeringResponse>> updateCheering(
            @RequestBody CheeringRequest request,
            @RequestParam Long cheeringId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        CheeringResponse response = cheeringService.updateCheering(request, cheeringId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 수정 성공"));
    }

    @Operation(summary = "응원 삭제")
    @DeleteMapping("api/articles/{articleId}/cheerings/{cheeringId}")
    public ResponseEntity<ApiResponseForm<Void>> deleteCheering(
            @RequestParam Long cheeringId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        cheeringService.deleteCheering(cheeringId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "응원 삭제 성공"));
    }

    @Operation(summary = "특정 응원 조회", description = "다른 organization의 응원 데이터는 조회할 수 없습니다.")
    @GetMapping("api/articles/{articleId}/cheerings/{cheeringId}")
    public ResponseEntity<ApiResponseForm<CheeringResponse>> getCheering(
            @RequestParam Long cheeringId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        CheeringResponse response = cheeringService.getCheering(requesterId, cheeringId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 조회 성공"));
    }

    @Operation(summary = "특정 게시글의 응원 목록 조회", description = "같은 organization 안의 게시글에 대해서만 응원 목록을 조회할 수 있습니다.")
    @Parameter(
            name = "sort",
            description = "정렬 기준 (예: createdAt,desc 또는 sender.nickName,asc)",
            array = @ArraySchema(schema = @Schema(type = "string")),
            in = ParameterIn.QUERY
    )
    @GetMapping("api/articles/{articleId}/cheerings")
    public ResponseEntity<ApiResponseForm<Page<CheeringResponse>>> getCheeringsByArticleId(
            @PathVariable UUID articleId,
            @ParameterObject Pageable pageable
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        Page<CheeringResponse> responses = cheeringService.getCheeringsByArticleId(requesterId, articleId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(responses, "응원 목록 조회 성공"));
    }

    @Operation(summary = "내 읽지 않은 응원 개수 조회")
    @GetMapping("/api/cheerings/unread-count")
    public ResponseEntity<ApiResponseForm<UnreadCheeringResponse>> getUnreadCheeringCount() {
        UUID memberId = authService.getMemberIdFromToken();
        UnreadCheeringResponse response = cheeringService.getUnreadCheeringCount(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "내 읽지 않은 응원 개수 조회 성공"));
    }

    @Operation(summary = "내가 받은 응원 목록 조회 - 응원함 조회용으로 호출했다면 PATCH/api/cheering/read API로 읽음 처리 필요")
    @GetMapping("/api/members/me/cheerings")
    public ResponseEntity<ApiResponseForm<Page<CheeringResponse>>> getMyCheerings(
            @ParameterObject Pageable pageable
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        Page<CheeringResponse> responses = cheeringService.getCheeringsByMemberId(memberId, memberId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(responses, "내가 받은 응원 목록 조회 성공"));
    }

    @Operation(summary = "내 응원 읽음 처리")
    @PatchMapping("/api/cheerings/read")
    public ResponseEntity<ApiResponseForm<List<CheeringResponse>>> readCheering(
            @RequestBody CheeringReadRequest request
    ) {
        UUID receiverId = authService.getMemberIdFromToken();
        List<CheeringResponse> response = cheeringService.readCheering(request, receiverId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 읽음 처리 성공"));
    }

    @Operation(
            summary = "특정 기간 동안 특정 유저가 보낸 총 응원 점수 조회",
            description = "기간을 지정하여 특정 유저가 보낸 응원의 총 점수를 조회합니다. " +
                    "기간을 지정하지 않으면 전체 기간의 점수를 조회합니다. 같은 organization 유저에 대해서만 조회 가능합니다. " +
                    "올해 기록만 조회하려면 startDate에 올해 1월 1일 00:00:00을 설정하세요."
    )
    @GetMapping("/api/members/{memberId}/cheerings/total-sending-score")
    public ResponseEntity<ApiResponseForm<TotalSendingCheeringScoreResponse>> getTotalSendingCheeringScore(
            @PathVariable UUID memberId,
            @Parameter(description = "시작 일시 ex) 2024-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @Parameter(description = "종료 일시 ex) 2024-12-31T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        TotalSendingCheeringScoreResponse response = cheeringService.getTotalGivenPointsByDateRange(requesterId, memberId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 기간 동안 특정 유저가 보낸 총 응원 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저 받은 총 응원 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/total-receiving-score")
    public ResponseEntity<ApiResponseForm<TotalReceivedCheeringScoreResponse>> getTotalReceivingCheeringScore(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        TotalReceivedCheeringScoreResponse response = cheeringService.getTotalReceivedPoints(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저 받은 총 응원 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저의 보낸 응원의 모든 카테고리별 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/sending-category-stats")
    public ResponseEntity<ApiResponseForm<List<CategoryStatDto>>> getGivenStats(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        List<CategoryStatDto> response = cheeringService.getGivenStats(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저의 보낸 응원의 모든 카테고리별 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저의 받은 응원의 모든 카테고리별 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/receiving-category-stats")
    public ResponseEntity<ApiResponseForm<List<CategoryStatDto>>> getReceivedStats(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        List<CategoryStatDto> response = cheeringService.getReceivedStats(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저의 받은 응원의 모든 카테고리별 점수 조회 성공"));
    }

    @Operation(
            summary = "특정 기간 동안 본인이 보낸 총 응원 점수 조회",
            description = "기간을 지정하여 본인이 보낸 응원의 총 점수를 조회합니다. " +
                    "기간을 지정하지 않으면 전체 기간의 점수를 조회합니다. " +
                    "올해 기록만 조회하려면 startDate에 올해 1월 1일 00:00:00을 설정하세요."
    )
    @GetMapping("/api/members/me/cheerings/total-sending-score")
    public ResponseEntity<ApiResponseForm<TotalSendingCheeringScoreResponse>> getMyTotalSendingCheeringScoreByDateRange(
            @Parameter(description = "시작 일시 ex) 2024-01-01T00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,
            @Parameter(description = "종료 일시 ex) 2024-12-31T23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        TotalSendingCheeringScoreResponse response = cheeringService.getTotalGivenPointsByDateRange(memberId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 기간 동안 보낸 총 응원 점수 조회 성공"));
    }
}
