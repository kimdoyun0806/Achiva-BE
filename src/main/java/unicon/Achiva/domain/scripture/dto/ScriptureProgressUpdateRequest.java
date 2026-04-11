package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ScriptureProgressUpdateRequest(
        @Schema(description = "해당 성경 권의 누적 완료 장 수", example = "5")
        @NotNull Integer completedChapters
) {
}
