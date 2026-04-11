package unicon.Achiva.domain.organization;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unicon.Achiva.domain.organization.dto.OrganizationSummaryResponse;
import unicon.Achiva.global.response.ApiResponseForm;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/organizations")
@Tag(name = "Organization", description = "Organization(상위 모임) API. 모든 전역 조회는 이 목록 조회만 허용됩니다.")
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Organization 목록 조회", description = "회원가입 전에 호출하는 공개 API입니다. organizationId와 organizationPassword를 회원가입 요청에 사용합니다.")
    @SecurityRequirements
    @GetMapping
    public ResponseEntity<ApiResponseForm<List<OrganizationSummaryResponse>>> getOrganizations() {
        List<OrganizationSummaryResponse> response = organizationService.getOrganizations();
        return ResponseEntity.ok(ApiResponseForm.success(response, "상위 모임 목록 조회 성공"));
    }
}
