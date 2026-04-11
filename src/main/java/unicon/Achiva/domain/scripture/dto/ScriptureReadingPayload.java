package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.scripture.entity.ArticleScriptureReading;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "성경 일독 게시글의 구조화 메타데이터")
public class ScriptureReadingPayload {
    @Schema(description = "성경 권 ID", example = "요한복음")
    private String scriptureId;
    @Schema(description = "이번 기록의 시작 장", example = "3")
    private Integer startChapter;
    @Schema(description = "이번 기록의 종료 장", example = "4")
    private Integer endChapter;
    @Schema(description = "기록 시점 기준 누적 완료 장 수", example = "5")
    private Integer completedChapters;
    @Schema(description = "실제 읽은 날짜", example = "2026-04-11")
    private LocalDate readAt;

    public static ScriptureReadingPayload fromEntity(ArticleScriptureReading entity) {
        if (entity == null) {
            return null;
        }

        return ScriptureReadingPayload.builder()
                .scriptureId(entity.getScriptureId())
                .startChapter(entity.getStartChapter())
                .endChapter(entity.getEndChapter())
                .completedChapters(entity.getCompletedChapters())
                .readAt(entity.getReadAt())
                .build();
    }
}
