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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.cheering.dto.*;
import unicon.Achiva.global.response.ApiResponseForm;

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

    @Operation(summary = "특정 응원 조회")
    @GetMapping("api/articles/{articleId}/cheerings/{cheeringId}")
    public ResponseEntity<ApiResponseForm<CheeringResponse>> getCheering(
            @RequestParam Long cheeringId
    ) {
        CheeringResponse response = cheeringService.getCheering(cheeringId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 조회 성공"));
    }

    @Operation(summary = "특정 게시글의 응원 목록 조회")
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
        Page<CheeringResponse> responses = cheeringService.getCheeringsByArticleId(articleId, pageable);
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
        Page<CheeringResponse> responses = cheeringService.getCheeringsByMemberId(memberId, pageable);
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

    @Operation(summary = "특정 유저 보낸 총 응원 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/total-sending-score")
    public ResponseEntity<ApiResponseForm<TotalSendingCheeringScoreResponse>> getTotalSendingCheeringScore(
            @PathVariable UUID memberId
    ) {
        TotalSendingCheeringScoreResponse response = cheeringService.getTotalGivenPoints(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저 보낸 총 응원 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저 받은 총 응원 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/total-receiving-score")
    public ResponseEntity<ApiResponseForm<TotalReceivedCheeringScoreResponse>> getTotalReceivingCheeringScore(
            @PathVariable UUID memberId
    ) {
        TotalReceivedCheeringScoreResponse response = cheeringService.getTotalReceivedPoints(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저 받은 총 응원 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저의 보낸 응원의 모든 카테고리별 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/sending-category-stats")
    public ResponseEntity<ApiResponseForm<List<CategoryStatDto>>> getGivenStats(
            @PathVariable UUID memberId
    ) {
        List<CategoryStatDto> response = cheeringService.getGivenStats(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저의 보낸 응원의 모든 카테고리별 점수 조회 성공"));
    }

    @Operation(summary = "특정 유저의 받은 응원의 모든 카테고리별 점수 조회")
    @GetMapping("/api/members/{memberId}/cheerings/receiving-category-stats")
    public ResponseEntity<ApiResponseForm<List<CategoryStatDto>>> getReceivedStats(
            @PathVariable UUID memberId
    ) {
        List<CategoryStatDto> response = cheeringService.getReceivedStats(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저의 받은 응원의 모든 카테고리별 점수 조회 성공"));
    }
}
