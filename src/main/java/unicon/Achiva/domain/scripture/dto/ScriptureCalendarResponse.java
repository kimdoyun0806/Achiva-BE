package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "특정 월의 성경 일독 기록 목록")
public class ScriptureCalendarResponse {
    @Schema(description = "조회한 년월", example = "2026-04")
    private String yearMonth;
    @ArraySchema(schema = @Schema(implementation = ScriptureCalendarItemResponse.class))
    private List<ScriptureCalendarItemResponse> items;
}
