package ai.oenexa.core.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamJpaRepository extends JpaRepository<TeamEntity, UUID> {
    List<TeamEntity> findByOrganizationId(UUID organizationId);
}
