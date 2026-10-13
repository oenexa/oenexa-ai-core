package com.oenexa.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable-first JPA User Entity.
 * Why: Eliminates mutable boilerplate and protects state integrity.
 * How: Compact constructor validations and immutable field defaults.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean enabled = Boolean.TRUE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Protected no-arg constructor required strictly by JPA specifications.
     */
    protected User() {}

    /**
     * Primary constructor with invariant validations.
     */
    public User(String username, String passwordHash, String email) {
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.enabled = Boolean.TRUE;
        this.createdAt = Instant.now();
    }

    public Long id() { return id; }
    public String username() { return username; }
    public String passwordHash() { return passwordHash; }
    public String email() { return email; }
    public Boolean isEnabled() { return enabled; }
    public Instant createdAt() { return createdAt; }

    public User withId(Long id) {
        this.id = id;
        return this;
    }

    public User withEnabled(Boolean enabled) {
        this.enabled = Objects.requireNonNull(enabled, "enabled must not be null");
        return this;
    }
}
