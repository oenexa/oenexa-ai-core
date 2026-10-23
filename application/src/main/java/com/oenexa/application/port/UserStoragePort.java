package com.oenexa.application.port;

import com.oenexa.domain.User;
import java.util.Optional;

/**
 * Enterprise Outbound Port for User Persistence.
 * Follows Hexagonal Architecture: core application logic is completely decoupled from JPA.
 */
public interface UserStoragePort {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
