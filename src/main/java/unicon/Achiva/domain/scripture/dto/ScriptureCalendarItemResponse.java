package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.article.entity.Question;
import unicon.Achiva.domain.scripture.entity.ArticleScriptureReading;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "월별 성경 일독 기록 아이템")
public class ScriptureCalendarItemResponse {
    @Schema(description = "게시글 ID")
    private UUID articleId;
    @Schema(description = "게시글 생성 시각", example = "2026-04-11T10:00:00")
    private LocalDateTime createdAt;
    @Schema(description = "게시글 본문", example = "오늘은 이 구절이 오래 남았어요.")
    private String content;
    @Schema(description = "성경 일독 메타데이터")
    private ScriptureReadingPayload scriptureReading;

    public static ScriptureCalendarItemResponse fromEntity(ArticleScriptureReading entity, String content) {
        return ScriptureCalendarItemResponse.builder()
                .articleId(entity.getArticle().getId())
                .createdAt(entity.getArticle().getCreatedAt())
                .content(content)
                .scriptureReading(ScriptureReadingPayload.fromEntity(entity))
                .build();
    }

    public static String extractContent(ArticleScriptureReading entity) {
        return entity.getArticle().getQuestions().stream()
                .findFirst()
                .map(Question::getContent)
                .orElse("");
    }
}
