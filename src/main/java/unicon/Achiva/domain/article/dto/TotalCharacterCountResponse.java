package unicon.Achiva.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "작성한 글의 총 글자 수 응답")
public record TotalCharacterCountResponse(
        @Schema(description = "총 글자 수", example = "15420")
        Long totalCharacterCount
) {
}
