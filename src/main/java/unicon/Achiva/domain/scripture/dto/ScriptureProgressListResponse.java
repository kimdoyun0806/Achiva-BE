package unicon.Achiva.domain.scripture.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "내 성경 권별 진도 목록")
public class ScriptureProgressListResponse {
    @ArraySchema(schema = @Schema(implementation = ScriptureProgressItemResponse.class))
    private List<ScriptureProgressItemResponse> items;
}
