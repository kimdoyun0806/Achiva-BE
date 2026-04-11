package unicon.Achiva.domain.moim.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "모임 생성 요청")
public class MoimCreateRequest {
    @Schema(description = "모임 이름", example = "새벽 러닝 크루")
    private String name;

    @Schema(description = "모임 설명", example = "주 3회 이상 러닝 인증하는 모임")
    private String description;

    @Schema(description = "최대 인원", example = "20")
    private int maxMember;

    @JsonProperty("isPrivate")
    @JsonAlias("private")
    @Schema(description = "비공개 모임 여부", example = "true")
    private Boolean privateMoim;

    @Schema(description = "비공개 모임 비밀번호", example = "1234")
    private String password;
}
