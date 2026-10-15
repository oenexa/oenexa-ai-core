package ai.oenexa.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationPort {
    Organization saveOrganization(Organization org);
    Optional<Organization> findOrganizationById(UUID id);
    Optional<Organization> findOrganizationBySlug(String slug);
    List<Organization> findAllOrganizations();
}
