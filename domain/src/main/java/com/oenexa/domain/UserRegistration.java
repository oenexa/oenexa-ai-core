package com.oenexa.domain;

/**
 * Immutable User Registration Record.
 * Why: Standard DTO replacing mutable boilerplate classes.
 * How: Java Record with strict invariant validation.
 */
public record UserRegistration(String username, String email, String passwordHash) {
    public UserRegistration {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("passwordHash must not be blank");
        }
    }
}
