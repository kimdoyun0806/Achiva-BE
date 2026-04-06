package unicon.Achiva.domain.category;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import unicon.Achiva.domain.article.ArticleService;
import unicon.Achiva.domain.auth.AuthService;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final ArticleService articleService;
    private final AuthService authService;

    /**
     * 모든 Category의 displayName(설명값)을 반환합니다.
     *
     * @return 카테고리 설명 문자열 리스트
     */
    @GetMapping("/api/categories")
    public ResponseEntity<ApiResponseForm<List<String>>> getAllCategoryValues() {
        var data = Arrays.stream(Category.values())
                .map(Category::getDescription)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseForm.success(data, "현재 카테고리 목록 조회 성공"));
    }

    @Operation(summary = "카테고리별 유저 랭킹 데이터 조회", description = "서비스 전체가 아니라 로그인한 사용자의 organization 기준 카테고리 랭킹 데이터입니다.")
    @GetMapping("/api/category/ranking")
    public ResponseEntity<ApiResponseForm<CategoryRankingResponse>> getCategoryRanking() {
        var requesterId = authService.getMemberIdFromToken();
        CategoryRankingResponse response = articleService.getCategoryRanking(requesterId);
        return ResponseEntity.ok(ApiResponseForm.success(response, "카테고리별 유저 랭킹 데이터 조회 성공"));
    }
}
