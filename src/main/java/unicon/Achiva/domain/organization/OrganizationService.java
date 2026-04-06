package unicon.Achiva.domain.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unicon.Achiva.domain.organization.dto.OrganizationSummaryResponse;
import unicon.Achiva.domain.organization.entity.Organization;
import unicon.Achiva.domain.organization.infrastructure.OrganizationRepository;
import unicon.Achiva.global.response.GeneralException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public List<OrganizationSummaryResponse> getOrganizations() {
        return organizationRepository.findAllByActiveTrueAndIsDeletedFalseOrderByNameAsc().stream()
                .map(OrganizationSummaryResponse::from)
                .toList();
    }

    public Organization getSignupOrganization(Long organizationId, String rawPassword) {
        if (organizationId == null) {
            throw new GeneralException(OrganizationErrorCode.ORGANIZATION_REQUIRED);
        }

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new GeneralException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND));

        if (!organization.isActive() || Boolean.TRUE.equals(organization.getIsDeleted())) {
            throw new GeneralException(OrganizationErrorCode.ORGANIZATION_INACTIVE);
        }

        if (!organization.isPasswordValid(rawPassword)) {
            throw new GeneralException(OrganizationErrorCode.INVALID_ORGANIZATION_PASSWORD);
        }

        return organization;
    }
}
