package ai.oenexa.core.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByOrganizationId(UUID organizationId);
    List<ProjectEntity> findByOrganizationIdAndTeamId(UUID organizationId, UUID teamId);
}
