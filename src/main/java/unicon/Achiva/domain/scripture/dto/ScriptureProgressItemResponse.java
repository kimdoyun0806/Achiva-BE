package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.scripture.entity.MemberScriptureProgress;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "성경 권별 누적 진도 응답")
public class ScriptureProgressItemResponse {
    @Schema(description = "성경 권 ID", example = "요한복음")
    private String scriptureId;
    @Schema(description = "누적 완료 장 수", example = "5")
    private Integer completedChapters;
    @Schema(description = "마지막 업데이트 시각", example = "2026-04-11T10:30:00")
    private LocalDateTime updatedAt;

    public static ScriptureProgressItemResponse fromEntity(MemberScriptureProgress entity) {
        return ScriptureProgressItemResponse.builder()
                .scriptureId(entity.getScriptureId())
                .completedChapters(entity.getCompletedChapters())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
