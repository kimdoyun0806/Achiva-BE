package unicon.Achiva.domain.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import unicon.Achiva.domain.organization.entity.Organization;

@Getter
@Builder
@Schema(description = "회원가입 가능한 Organization 요약 정보")
public class OrganizationSummaryResponse {
    @Schema(description = "Organization ID", example = "1")
    private Long id;
    @Schema(description = "Organization 이름", example = "Achiva University")
    private String name;
    @Schema(description = "Organization 설명", example = "운동 기록을 함께하는 상위 모임")
    private String description;
    @Schema(description = "회원가입 시 비밀번호 입력이 필요한지 여부", example = "true")
    private boolean requiresPassword;

    public static OrganizationSummaryResponse from(Organization organization) {
        return OrganizationSummaryResponse.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .requiresPassword(organization.requiresPassword())
                .build();
    }
}
