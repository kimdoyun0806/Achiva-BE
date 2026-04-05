package unicon.Achiva.domain.moim;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.moim.dto.MoimCreateRequest;
import unicon.Achiva.domain.moim.dto.MoimDetailResponse;
import unicon.Achiva.domain.moim.dto.MoimRankingResponse;
import unicon.Achiva.domain.moim.dto.MoimResponse;
import unicon.Achiva.domain.moim.dto.MoimUpdateRequest;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/moim")
public class MoimController {

    private final MoimService moimService;
    private final AuthService authService;

    @Operation(summary = "모임 생성")
    @PostMapping
    public ResponseEntity<ApiResponseForm<MoimResponse>> createMoim(@RequestBody MoimCreateRequest request) {
        UUID memberId = authService.getMemberIdFromToken();
        MoimResponse response = moimService.createMoim(request, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "모임 생성 성공"));
    }

    @Operation(summary = "모임 목록 조회 (필터링)")
    @GetMapping
    public ResponseEntity<ApiResponseForm<Page<MoimResponse>>> getMoims(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isOfficial,
            @ParameterObject Pageable pageable
    ) {
        Page<MoimResponse> response = moimService.getMoims(keyword, isOfficial, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "모임 목록 조회 성공"));
    }

    @Operation(summary = "전체 모임 랭킹 데이터 조회", description = "랭킹 기능을 위한 임시 API")
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponseForm<List<MoimRankingResponse>>> getMoimsForRanking() {
        List<MoimRankingResponse> response = moimService.getMoimsForRanking();
        return ResponseEntity.ok(ApiResponseForm.success(response, "전체 모임 랭킹 데이터 조회 성공"));
    }

    @Operation(summary = "내 모임(크루) 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponseForm<List<MoimResponse>>> getMyMoims() {
        UUID memberId = authService.getMemberIdFromToken();
        List<MoimResponse> response = moimService.getMyMoims(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "내 모임 조회 성공"));
    }

    @Operation(summary = "모임 상세 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseForm<MoimDetailResponse>> getMoimDetail(@PathVariable Long id) {
        UUID memberId = authService.getMemberIdFromToken();
        MoimDetailResponse response = moimService.getMoimDetail(id, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "모임 상세 조회 성공"));
    }

    @Operation(summary = "모임 가입 (비공개 시 패스워드 필요)")
    @PostMapping("/{id}/join")
    public ResponseEntity<ApiResponseForm<Boolean>> joinMoim(
            @PathVariable Long id,
            @RequestBody(required = false) String password
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        moimService.joinMoim(id, memberId, password);
        return ResponseEntity.ok(ApiResponseForm.success(true, "모임 가입 성공"));
    }

    @Operation(summary = "모임 수정 (방장 전용, 전달한 필드만 반영)")
    @PutMapping("/{id}/settings")
    public ResponseEntity<ApiResponseForm<MoimDetailResponse>> updateMoimSettings(
            @PathVariable Long id,
            @RequestBody MoimUpdateRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        MoimDetailResponse response = moimService.updateMoimSettings(id, memberId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "모임 설정 변경 성공"));
    }

    @Operation(summary = "이번 달 모임 피드 (모임 멤버들의 게시물)")
    @GetMapping("/{id}/feed")
    public ResponseEntity<ApiResponseForm<Page<ArticleResponse>>> getMoimFeed(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        Page<ArticleResponse> response = moimService.getMoimFeed(id, memberId, PageRequest.of(page, size));
        return ResponseEntity.ok(ApiResponseForm.success(response, "모임 피드 조회 성공"));
    }

    @Operation(summary = "모임 탈퇴")
    @DeleteMapping("/{id}/members/me")
    public ResponseEntity<ApiResponseForm<Boolean>> leaveMoim(@PathVariable Long id) {
        UUID memberId = authService.getMemberIdFromToken();
        moimService.leaveMoim(id, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(true, "모임 탈퇴 성공"));
    }

    @Operation(summary = "모임 멤버 강퇴 (방장 전용)", description = "로그인한 사용자가 해당 모임의 방장일 때 memberId에 해당하는 멤버를 모임에서 제외합니다.")
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<ApiResponseForm<Boolean>> removeMoimMember(
            @Parameter(description = "모임 ID") @PathVariable Long id,
            @Parameter(description = "강퇴할 멤버의 UUID") @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        moimService.removeMoimMember(id, requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(true, "모임 멤버 제외 성공"));
    }

    @Operation(summary = "모임 삭제 (방장 전용)")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseForm<Boolean>> deleteMoim(@PathVariable Long id) {
        UUID memberId = authService.getMemberIdFromToken();
        moimService.deleteMoim(id, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(true, "모임 삭제 성공"));
    }
}
