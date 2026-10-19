package ai.oenexa.core.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    CoreUser saveUser(CoreUser user, String passwordHash);
    Optional<CoreUser> findUserById(UUID id);
    Optional<CoreUser> findUserByEmail(String email);
    List<CoreUser> findUsersByOrganizationId(UUID organizationId);
    Optional<String> getPasswordHash(UUID userId);
}
