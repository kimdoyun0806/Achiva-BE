package unicon.Achiva.domain.goal.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "목표들의 총 클릭 수 응답")
public record TotalClickCountResponse(
        @Schema(description = "총 클릭 수", example = "245")
        Long totalClickCount
) {
}
