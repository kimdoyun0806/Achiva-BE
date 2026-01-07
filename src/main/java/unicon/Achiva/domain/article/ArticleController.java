package unicon.Achiva.domain.article;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unicon.Achiva.domain.article.dto.ArticleRequest;
import unicon.Achiva.domain.article.dto.ArticleResponse;
import unicon.Achiva.domain.article.dto.ArticleWithBookResponse;
import unicon.Achiva.domain.article.dto.SearchArticleCondition;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.domain.s3.S3Service;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;
    private final AuthService authService;
    private final S3Service s3Service;

    @Operation(summary = "새 게시글 작성")
    @PostMapping("/api/articles")
    public ResponseEntity<ApiResponseForm<ArticleResponse>> createArticle(
            @RequestBody ArticleRequest request
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        ArticleResponse response = articleService.createArticle(request, memberId);
        return ResponseEntity.ok(ApiResponseForm.created(response, "게시글 작성 성공"));
    }

    @Operation(summary = "게시글 수정")
    @PutMapping("/api/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleResponse>> updateArticle(
            @RequestBody ArticleRequest request,
            @RequestParam UUID articleId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        ArticleResponse response = articleService.updateArticle(request, articleId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 수정 성공"));
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/api/articles/{articleId}/delete")
    public ResponseEntity<ApiResponseForm<Void>> deleteArticle(
            @RequestParam UUID articleId
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        articleService.deleteArticle(articleId, memberId);
        return ResponseEntity.ok(ApiResponseForm.success(null, "게시글 삭제 성공"));
    }

    @Operation(summary = "게시글 검색")
    @GetMapping("/api/articles")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> searchArticles(
            SearchArticleCondition condition,
            @ParameterObject Pageable pageable
    ) {
        Page<ArticleWithBookResponse> response = articleService.getArticles(condition, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 검색 성공"));
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/api/articles/{articleId}")
    public ResponseEntity<ApiResponseForm<ArticleResponse>> getArticle(
            @PathVariable UUID articleId
    ) {
        ArticleResponse response = articleService.getArticle(articleId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "게시글 상세 조회 성공"));
    }

    @Operation(summary = "내 게시글 목록 조회")
    @GetMapping("/api/articles/my-articles")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getMyArticles(
            @ParameterObject Pageable pageable
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        Page<ArticleWithBookResponse> response = articleService.getArticlesByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "내 게시글 목록 조회 성공"));
    }

    @Operation(summary = "특정 유저 게시글 목록 조회")
    @GetMapping("/api/member/{memberId}/articles")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getArticlesByMember(
            @PathVariable UUID memberId,
            @ParameterObject Pageable pageable
    ) {
        Page<ArticleWithBookResponse> response = articleService.getArticlesByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저 게시글 목록 조회 성공"));
    }

    @Operation(summary = "특정 유저 특정 카테고리 게시글 목록 최신순 조회")
    @GetMapping("/api/members/{memberId}/categories/{category}/articles")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getArticlesByMemberAndCategory(
            @PathVariable UUID memberId,
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        Page<ArticleWithBookResponse> response = articleService.getArticlesByMemberAndCateogry(memberId, category, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "특정 유저 특정 카테고리 게시글 목록 최신순 조회 성공"));
    }

    @Operation(summary = "게시글 사진 저장용 presigned URL 발급. 이후 게시글 생성, 수정 등 요청 보낼 때 본 api에서 발급받은 url에서 쿼리스트링 뺀 부분을 photoUrl로 사용해야 함.")
    @GetMapping("/api/articles/presigned-url")
    public ResponseEntity<ApiResponseForm<Map<String, String>>> getPresignedUrl(
            @RequestParam(defaultValue = "application/octet-stream") String contentType
    ) {
        String url = s3Service.generatePresignedUrl(contentType);
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        return ResponseEntity.ok(ApiResponseForm.success(response, "Presigned URL 발급 성공"));
    }

    @Operation(summary = "홈화면 게시글 목록 조회")
    @GetMapping("/api/articles/home")
    public ResponseEntity<Page<ArticleWithBookResponse>> getCombinedFeed(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        Page<ArticleWithBookResponse> page = articleService.getHomeArticles(memberId, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(summary = "멤버 관심 카테고리 기반 최신글 게시글 목록")
    @GetMapping("/api/members/{memberId}/feed")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getFeed(
            @PathVariable UUID memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        Page<ArticleWithBookResponse> response = articleService.getMemberInterestFeed(memberId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "멤버 관심 카테고리 기반 최신글 게시글 목록 조회 성공"));
    }

    @Operation(summary = "전체 게시글 최신순 목록 조회")
    @GetMapping("/api/articles/feed")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getAllArticlesFeed(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        Page<ArticleWithBookResponse> response = articleService.getAllArticlesFeed(pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "전체 게시글 최신순 목록 조회 성공"));
    }

    @Operation(
            summary = "응원 관계 사용자 게시글 목록 조회",
            description = "내가 응원을 보낸 사람 + 나에게 응원을 보낸 사람이 작성한 게시글을 최신순으로 조회합니다. "
    )
    @GetMapping("/api/articles/cheering-feed")
    public ResponseEntity<ApiResponseForm<Page<ArticleWithBookResponse>>> getCheeringRelatedArticlesFeed(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        UUID memberId = authService.getMemberIdFromToken();
        Page<ArticleWithBookResponse> response = articleService.getCheeringRelatedArticlesFeed(memberId, pageable);
        return ResponseEntity.ok(ApiResponseForm.success(response, "응원 관계 사용자 게시글 목록 조회 성공"));
    }
}
