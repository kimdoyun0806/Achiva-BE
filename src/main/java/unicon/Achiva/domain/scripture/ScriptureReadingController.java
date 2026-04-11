package unicon.Achiva.domain.scripture;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.scripture.dto.ScriptureCalendarResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressItemResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressListResponse;
import unicon.Achiva.domain.scripture.dto.ScriptureProgressUpdateRequest;
import unicon.Achiva.domain.scripture.dto.ScriptureReadingArticleCreateRequest;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Scripture Reading API", description = "성경 일독 진도 및 게시글 관리 API")
public class ScriptureReadingController {

    private final ScriptureReadingService scriptureReadingService;
    private final AuthService authService;

    @Operation(summary = "내 성경 권별 진도 조회")
    @GetMapping("/api/scripture-reading/progress/me")
    public ResponseEntity<ApiResponseForm<ScriptureProgressListResponse>> getMyProgress() {
        UUID memberId = authService.getMemberIdFromToken();
        ScriptureProgressListResponse response = scriptureReadingService.getMyProgress(memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "내 성경 권별 진도 조회 성공"));
    }

    @Operation(summary = "내 특정 성경 권 진도 저장")
    @PutMapping("/api/scripture-reading/progress/me/scriptures/{scriptureId}")
    public ResponseEntity<ApiResponseForm<ScriptureProgressItemResponse>> updateProgress(
            @Parameter(description = "성경 권 ID. 예: 요한복음") 
            @PathVariable String scriptureId,
            @Valid @RequestBody ScriptureProgressUpdateRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        ScriptureProgressItemResponse response = scriptureReadingService.upsertProgress(memberId, scriptureId, request);
        return ResponseEntity.ok(ApiResponseForm.success(response, "성경 권 진도 저장 성공"));
    }

    @Operation(summary = "성경 일독 게시글 생성")
    @PostMapping("/api/scripture-reading/articles")
    public ResponseEntity<ApiResponseForm<ArticleResponse>> createScriptureArticle(
            @Valid @RequestBody ScriptureReadingArticleCreateRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        ArticleResponse response = scriptureReadingService.createScriptureArticle(memberId, request);
        return ResponseEntity.ok(ApiResponseForm.created(response, "성경 일독 게시글 생성 성공"));
    }

    @Operation(summary = "특정 회원의 월별 성경 일독 기록 조회")
    @GetMapping("/api/members/{memberId}/scripture-reading/articles/calendar")
    public ResponseEntity<ApiResponseForm<ScriptureCalendarResponse>> getCalendar(
            @Parameter(description = "조회 대상 회원 ID")
            @PathVariable UUID memberId,
            @Parameter(description = "조회할 년월. 형식: YYYY-MM, 예: 2026-04")
            @RequestParam String yearMonth
    ) {
        UUID requesterId = authService.getMemberIdFromToken();
        ScriptureCalendarResponse response = scriptureReadingService.getCalendar(requesterId, memberId, yearMonth);
        return ResponseEntity.ok(ApiResponseForm.success(response, "월별 성경 일독 기록 조회 성공"));
    }
}
