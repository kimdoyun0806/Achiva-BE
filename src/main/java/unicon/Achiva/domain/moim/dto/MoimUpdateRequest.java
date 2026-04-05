package unicon.Achiva.domain.moim.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "모임 수정 요청. 전달한 필드만 반영됩니다.")
public class MoimUpdateRequest {
    @Schema(description = "모임 이름", example = "주말 러닝 크루")
    private String name;

    @Schema(description = "모임 설명", example = "주말 아침 러닝 인증 모임")
    private String description;

    @Schema(description = "최대 인원", example = "30")
    private Integer maxMember;

    @JsonProperty("isPrivate")
    @JsonAlias("private")
    @Schema(description = "비공개 모임 여부", example = "true")
    private Boolean privateMoim;

    @Schema(description = "비공개 모임 비밀번호. 비공개 모임일 때만 반영됩니다.", example = "1234")
    private String password;

    @JsonProperty("isOfficial")
    @JsonAlias("official")
    @Schema(description = "공식 모임 여부", example = "false")
    private Boolean officialMoim;

    @Schema(description = "모임 목표치", example = "100")
    private Integer targetAmount;

    @Schema(description = "독려 기준 일수", example = "5")
    private Integer pokeDays;
}
