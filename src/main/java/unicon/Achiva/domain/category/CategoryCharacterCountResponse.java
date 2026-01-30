package unicon.Achiva.domain.category;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "카테고리별 글자 수 통계 응답")
public record CategoryCharacterCountResponse(
        @Schema(description = "카테고리별 글자 수 목록")
        List<CategoryCharacterCount> categoryCharacterCounts
) {

    public static CategoryCharacterCountResponse fromObjectList(List<Object[]> categoryCharacterCountObjects) {
        List<CategoryCharacterCount> categoryCharacterCounts = categoryCharacterCountObjects.stream()
                .map(obj -> new CategoryCharacterCount((String) obj[0], (Long) obj[1]))
                .toList();
        return new CategoryCharacterCountResponse(categoryCharacterCounts);
    }

    @Schema(description = "카테고리별 글자 수")
    public record CategoryCharacterCount(
            @Schema(description = "카테고리명", example = "축구")
            String category,
            @Schema(description = "총 글자 수", example = "5420")
            Long characterCount
    ) {
    }
}
