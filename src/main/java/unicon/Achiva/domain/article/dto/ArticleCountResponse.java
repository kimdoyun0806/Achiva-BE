package unicon.Achiva.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 기간 동안 작성한 게시글 수 응답")
public record ArticleCountResponse(
        @Schema(description = "게시글 수", example = "12")
        Long articleCount
) {
}
