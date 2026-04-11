package unicon.Achiva.domain.organization.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import unicon.Achiva.domain.organization.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findAllByActiveTrueAndIsDeletedFalseOrderByNameAsc();

    Optional<Organization> findByIdAndActiveTrueAndIsDeletedFalse(Long id);
}
