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
import unicon.Achiva.domain.member.dto.MemberResponse;
import unicon.Achiva.domain.member.dto.SearchMemberCondition;
import unicon.Achiva.domain.s3.S3Service;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.HashMap;
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

    @Operation(summary = "특정 유저 정보 조회")
    @GetMapping("/api/members/{memberId}")
    public ResponseEntity<ApiResponseForm<MemberResponse>> getMemberInfo(
            @PathVariable UUID memberId
    ) {
        MemberResponse memberResponse = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "유저 정보 조회 성공"));
    }

    @Operation(summary = "닉네임으로 유저 정보 조회")
    @GetMapping("/api2/members/{nickname}")
    public ResponseEntity<ApiResponseForm<MemberResponse>> getMemberInfoByNickname(
            @PathVariable String nickname
    ) {
        MemberResponse memberResponse = memberService.getMemberInfoByNickname(nickname);
        return ResponseEntity.ok(ApiResponseForm.success(memberResponse, "닉네임으로 유저 정보 조회 성공"));
    }

    @Operation(summary = "닉네임으로 유저 목록 검색")
    @GetMapping("/api/members")
    public ResponseEntity<ApiResponseForm<Page<MemberResponse>>> getMembers(
            SearchMemberCondition condition,
            @ParameterObject Pageable pageable
    ) {
        Page<MemberResponse> members = memberService.getMembers(condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(members, "닉네임으로 유저 검색 성공"));
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
            @RequestParam UUID memberId
    ) {
        CategoryCountResponse result = articleService.getArticleCountByCategory(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(result, "카테고리별 작성 수 조회 성공"));
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
}
