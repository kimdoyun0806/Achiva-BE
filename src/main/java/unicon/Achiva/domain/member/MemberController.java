package unicon.Achiva.domain.member;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.category.CategoryCountResponse;
import unicon.Achiva.domain.member.dto.ConfirmProfileImageUploadRequest;
import unicon.Achiva.domain.member.dto.MemberDetailResponse;
import unicon.Achiva.domain.member.dto.MemberRankingResponse;
import unicon.Achiva.domain.member.dto.MemberResponse;
import unicon.Achiva.domain.member.dto.SearchMemberCondition;
import unicon.Achiva.domain.s3.S3Service;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ArticleService articleService;
    private final AuthService authService;
    private final S3Service s3Service;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/api/members/me")
    public ResponseEntity<ApiResponseForm<MemberResponse>> getMyInfo() {
        UUID memberId = authService.getMemberIdFromToken();
        MemberResponse memberResponse = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "내 정보 조회 성공"));
    }

    @Operation(summary = "특정 유저 정보 조회", description = "같은 organization의 유저만 조회할 수 있습니다.")
    @GetMapping("/api/members/{memberId}")
    public ResponseEntity<ApiResponseForm<MemberResponse>> getMemberInfo(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        MemberResponse memberResponse = memberService.getMemberInfo(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "유저 정보 조회 성공"));
    }

    @Operation(summary = "특정 유저 상세 정보 조회", description = "같은 organization의 유저만 조회할 수 있습니다.")
    @GetMapping("/api/members/{memberId}/detail")
    public ResponseEntity<ApiResponseForm<MemberDetailResponse>> getMemberDetailInfo(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        MemberDetailResponse memberResponse = memberService.getMemberDetailInfo(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "유저 상세 정보 조회 성공"));
    }

    @Operation(summary = "닉네임으로 유저 정보 조회", description = "같은 organization의 유저만 조회할 수 있습니다.")
    @GetMapping("/api2/members/{nickname}")
    public ResponseEntity<ApiResponseForm<MemberResponse>> getMemberInfoByNickname(
            @PathVariable String nickname
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        MemberResponse memberResponse = memberService.getMemberInfoByNickname(requesterId, nickname);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "닉네임으로 유저 정보 조회 성공"));
    }

    @Operation(summary = "닉네임으로 유저 상세 정보 조회", description = "같은 organization의 유저만 조회할 수 있습니다.")
    @GetMapping("/api2/members/{nickname}/detail")
    public ResponseEntity<ApiResponseForm<MemberDetailResponse>> getMemberDetailInfoByNickname(
            @PathVariable String nickname
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        MemberDetailResponse memberResponse = memberService.getMemberDetailInfoByNickname(requesterId, nickname);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "닉네임으로 유저 상세 정보 조회 성공"));
    }

    @Operation(summary = "닉네임으로 유저 목록 검색", description = "로그인한 사용자의 organization 범위 안에서만 검색합니다.")
    @GetMapping("/api/members")
    public ResponseEntity<ApiResponseForm<Page<MemberResponse>>> getMembers(
            SearchMemberCondition condition,
            @ParameterObject Pageable pageable
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        Page<MemberResponse> members = memberService.getMembers(requesterId, condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(members, "닉네임으로 유저 검색 성공"));
    }

    @Operation(summary = "전체 유저 랭킹 데이터 조회", description = "서비스 전체가 아니라 로그인한 사용자의 organization 기준 랭킹 데이터입니다.")
    @GetMapping("/api/members/ranking")
    public ResponseEntity<ApiResponseForm<List<MemberRankingResponse>>> getMembersForRanking() {
        UUID requesterId = authService.getMemberIdFromToken();
        List<MemberRankingResponse> members = memberService.getMembersForRanking(requesterId);
        return ResponseEntity.ok(ApiResponseForm.success(members, "전체 유저 랭킹 데이터 조회 성공"));
    }

    @Operation(summary = "유저 프로필 사진 저장용 presigned URL 발급(이후 회원가입이나 프로필이미지 수정시 쿼리파라미터를 제외한 url을 BE에 보내야함.)")
    @GetMapping("/api/members/presigned-url")
    public ResponseEntity<ApiResponseForm<Map<String, String>>> getPresignedUrl(
            @RequestParam(defaultValue = "application/octet-stream") String contentType
    ) {
        String url = s3Service.generatePresignedUrl(contentType);
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(ApiResponseForm.success(response, "Presigned URL 발급 성공"));
    }

    @GetMapping("/api/members/{memberId}/count-by-category")
    public ResponseEntity<ApiResponseForm<CategoryCountResponse>> getArticleCountByCategory(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        CategoryCountResponse result = articleService.getArticleCountByCategory(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(result, "카테고리별 작성 수 조회 성공"));
    }

    @GetMapping("/api/members/{memberId}/weekly-count-by-category")
    public ResponseEntity<ApiResponseForm<CategoryCountResponse>> getWeeklyArticleCountByCategory(
            @PathVariable UUID memberId
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        CategoryCountResponse result = articleService.getWeeklyArticleCountByCategory(requesterId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(result, "이번 주 카테고리별 작성 수 조회 성공"));
    }

    @Operation(summary = "내 프로필 이미지 조회 API")
    @GetMapping("/api/members/me/image")
    public ResponseEntity<ApiResponseForm<String>> getMyProfileImageUrl() {
        UUID memberId = authService.getMemberIdFromToken();
        String profileImageUrl = memberService.getMemberInfo(memberId).getProfileImageUrl();
        return ResponseEntity.ok(ApiResponseForm.success(
                profileImageUrl,
                "내 프로필 이미지 조회 성공"));
    }

    @Operation(summary = "내 기록 통계 조회 API (주간 운동 횟수, 연속 달성 주차)")
    @GetMapping("/api/members/me/stats")
    public ResponseEntity<ApiResponseForm<unicon.Achiva.domain.member.dto.MemberStatsResponse>> getMyStats() {
        UUID memberId = authService.getMemberIdFromToken();
        unicon.Achiva.domain.member.dto.MemberStatsResponse stats = articleService.getMemberStats(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(stats, "내 기록 통계 조회 성공"));
    }

    @Operation(summary = "내 프로필 이미지 수정 API. presigned URL 발급 및 업로드가 선행되어야 함.")
    @PutMapping("/api/members/me/image")
    public ResponseEntity<ApiResponseForm<Boolean>> confirmProfileImageUpload(
            @RequestBody ConfirmProfileImageUploadRequest confirmProfileImageUploadRequest
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        memberService.updateProfileImageUrl(memberId, confirmProfileImageUploadRequest);
        return ResponseEntity.ok(ApiResponseForm.success(
                true,
                "프로필 사진 업데이트 성공"));
    }

    @Operation(summary = "친구 운동 게시글 푸시 알림 사용 여부 토글 (true: 활성화, false: 비활성화)")
    @PutMapping("/api/members/me/friend-workout-push-enabled/{enabled}")
    public ResponseEntity<ApiResponseForm<Boolean>> updateMyFriendWorkoutPushEnabled(
            @PathVariable boolean enabled
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        memberService.updateFriendWorkoutPushEnabled(memberId, enabled);
        return ResponseEntity.ok(ApiResponseForm.success(
                enabled,
                "친구 운동 푸시 알림 사용 여부 변경 성공"
        ));
    }
}
