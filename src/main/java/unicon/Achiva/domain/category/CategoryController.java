package unicon.Achiva.domain.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CategoryController {

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
}
