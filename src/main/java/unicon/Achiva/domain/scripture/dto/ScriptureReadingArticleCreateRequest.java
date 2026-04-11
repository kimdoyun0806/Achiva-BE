package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ScriptureReadingArticleCreateRequest(
        @Schema(description = "성경 권 ID", example = "요한복음")
        @NotBlank String scriptureId,
        @Schema(description = "이번 기록의 시작 장", example = "3")
        @NotNull Integer startChapter,
        @Schema(description = "이번 기록의 종료 장", example = "4")
        @NotNull Integer endChapter,
        @Schema(description = "기록 시점 기준 누적 완료 장 수", example = "5")
        @NotNull Integer completedChapters,
        @Schema(description = "성경 일독 게시글 본문", example = "오늘은 이 구절이 오래 남았어요.")
        @NotBlank String content,
        @Schema(description = "실제 읽은 날짜. 없으면 서버에서 작성일 기준으로 처리", example = "2026-04-11")
        LocalDate readAt
) {
}
