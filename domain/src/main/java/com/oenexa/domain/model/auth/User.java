package com.oenexa.domain.model.auth;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

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

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String country;

    @Column
    private String profileImageUrl;

    @Column(length = 1000)
    private String bio;

    @Column
    private String company;

    @Column
    private String profession;

    @Column
    private String language = "en";

    @Column
    private String timezone = "UTC";

    @Column(nullable = false)
    private String subscriptionPlan = "FREE";

    @Column(nullable = false)
    private String role = "ROLE_ENGINEER";

    @Column
    private String tenantId = "default-tenant";

    @Column
    private Boolean twoFactorEnabled = Boolean.FALSE;

    @Column
    private String twoFactorSecret;

    @Column
    private Integer failedLoginAttempts = 0;

    @Column
    private Instant lockedUntil;

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
        this.subscriptionPlan = "FREE";
        this.role = "ROLE_ENGINEER";
        this.tenantId = "default-tenant";
        this.twoFactorEnabled = Boolean.FALSE;
        this.failedLoginAttempts = 0;
    }

    public Long id() { return id; }
    public String username() { return username; }
    public String passwordHash() { return passwordHash; }
    public String email() { return email; }
    public Boolean isEnabled() { return enabled; }
    public Instant createdAt() { return createdAt; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String country() { return country; }
    public String profileImageUrl() { return profileImageUrl; }
    public String bio() { return bio; }
    public String company() { return company; }
    public String profession() { return profession; }
    public String language() { return language != null ? language : "en"; }
    public String timezone() { return timezone != null ? timezone : "UTC"; }
    public String subscriptionPlan() { return subscriptionPlan != null ? subscriptionPlan : "FREE"; }
    public String role() { return role != null ? role : "ROLE_ENGINEER"; }
    public String tenantId() { return tenantId != null ? tenantId : "default-tenant"; }
    public Boolean isTwoFactorEnabled() { return Boolean.TRUE.equals(twoFactorEnabled); }
    public String twoFactorSecret() { return twoFactorSecret; }
    public Integer failedLoginAttempts() { return failedLoginAttempts != null ? failedLoginAttempts : 0; }
    public Instant lockedUntil() { return lockedUntil; }

    public boolean isAccountLocked() {
        return lockedUntil != null && lockedUntil.isAfter(Instant.now());
    }

    public User withId(Long id) {
        this.id = id;
        return this;
    }

    public User withEnabled(Boolean enabled) {
        this.enabled = Objects.requireNonNull(enabled, "enabled must not be null");
        return this;
    }

    public User withPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        return this;
    }

    public User withProfile(String firstName, String lastName, String country, String bio, String company, String profession, String profileImageUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.country = country;
        this.bio = bio;
        this.company = company;
        this.profession = profession;
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public User withPreferences(String language, String timezone) {
        if (language != null && !language.isBlank()) this.language = language;
        if (timezone != null && !timezone.isBlank()) this.timezone = timezone;
        return this;
    }

    public User withSubscriptionPlan(String plan) {
        this.subscriptionPlan = (plan != null && !plan.isBlank()) ? plan : "FREE";
        return this;
    }

    public User withRole(String role) {
        this.role = (role != null && !role.isBlank()) ? role : "ROLE_ENGINEER";
        return this;
    }

    public User withTenantId(String tenantId) {
        this.tenantId = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default-tenant";
        return this;
    }

    public User withTwoFactor(boolean enabled, String secret) {
        this.twoFactorEnabled = enabled;
        this.twoFactorSecret = secret;
        return this;
    }

    public User recordFailedLogin() {
        this.failedLoginAttempts = (this.failedLoginAttempts == null ? 0 : this.failedLoginAttempts) + 1;
        if (this.failedLoginAttempts >= 5) {
            this.lockedUntil = Instant.now().plusSeconds(900); // 15-minute lock
        }
        return this;
    }

    public User resetFailedLogins() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
        return this;
    }
}

