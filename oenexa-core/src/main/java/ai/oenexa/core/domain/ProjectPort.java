package ai.oenexa.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectPort {
    Project saveProject(Project project);
    Optional<Project> findProjectById(UUID id);
    List<Project> findProjectsByOrganizationId(UUID organizationId);
}
