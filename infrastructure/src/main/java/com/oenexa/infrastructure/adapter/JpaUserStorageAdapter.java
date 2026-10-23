package com.oenexa.infrastructure.adapter;

import com.oenexa.application.UserStoragePort;
import com.oenexa.domain.User;
import com.oenexa.infrastructure.persistence.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

/**
 * Secondary adapter implementing UserStoragePort backed by Spring Data JPA UserRepository.
 */
@Component
public class JpaUserStorageAdapter implements UserStoragePort, com.oenexa.application.port.UserStoragePort {

    private final UserRepository repository;

    public JpaUserStorageAdapter(UserRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
