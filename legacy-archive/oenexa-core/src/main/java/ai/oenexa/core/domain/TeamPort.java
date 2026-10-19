package ai.oenexa.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamPort {
    Team saveTeam(Team team);
    Optional<Team> findTeamById(UUID id);
    List<Team> findTeamsByOrganizationId(UUID organizationId);
}
