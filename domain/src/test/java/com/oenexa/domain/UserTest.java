package com.oenexa.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        User user = new User("alice", "hashed_pwd", "alice@example.com");

        assertEquals("alice", user.username());
        assertEquals("hashed_pwd", user.passwordHash());
        assertEquals("alice@example.com", user.email());
        assertTrue(user.isEnabled());
        assertNotNull(user.createdAt());
        assertNull(user.id());
    }

    @Test
    void shouldExerciseNoArgConstructorForJPACompliance() {
        User emptyUser = new User() {};
        assertNull(emptyUser.id());
        assertNull(emptyUser.username());
        assertNull(emptyUser.passwordHash());
        assertNull(emptyUser.email());
        assertTrue(emptyUser.isEnabled());
        assertNotNull(emptyUser.createdAt());
    }

    @Test
    void shouldRejectNullArgumentsInConstructor() {
        assertThrows(NullPointerException.class, () -> new User(null, "hash", "email@test.com"));
        assertThrows(NullPointerException.class, () -> new User("user", null, "email@test.com"));
        assertThrows(NullPointerException.class, () -> new User("user", "hash", null));
    }

    @Test
    void shouldMutateWithIdAndEnabledCorrectly() {
        User user = new User("bob", "hash", "bob@test.com");
        User updated = user.withId(101L).withEnabled(false);

        assertEquals(101L, updated.id());
        assertFalse(updated.isEnabled());
        assertThrows(NullPointerException.class, () -> user.withEnabled(null));
    }

    @Test
    void shouldValidateUserRegistrationRecord() {
        UserRegistration reg = new UserRegistration("carol", "carol@test.com", "hash123");
        assertEquals("carol", reg.username());
        assertEquals("carol@test.com", reg.email());
        assertEquals("hash123", reg.passwordHash());

        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("", "email", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("   ", "email", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration(null, "email", "hash"));

        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", "", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", "   ", "hash"));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", null, "hash"));

        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", "email", ""));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", "email", "   "));
        assertThrows(IllegalArgumentException.class, () -> new UserRegistration("user", "email", null));
    }

    @Test
    void shouldSupportProfileFieldsAndMutations() {
        User user = new User("developer", "secret", "dev@oenexa.ai");
        assertEquals("en", user.language());
        assertEquals("UTC", user.timezone());
        assertEquals("FREE", user.subscriptionPlan());
        assertEquals("ROLE_ENGINEER", user.role());
        assertEquals("default-tenant", user.tenantId());
        assertFalse(user.isTwoFactorEnabled());
        assertFalse(user.isAccountLocked());

        user.withProfile("John", "Doe", "Switzerland", "Full-Stack AI Engineer", "OENEXA Corp", "AI Architect", "https://avatar.url/1.png")
            .withPreferences("de", "Europe/Zurich")
            .withSubscriptionPlan("ENTERPRISE")
            .withRole("ROLE_ADMIN")
            .withTenantId("org-swiss")
            .withTwoFactor(true, "MFRGGZDFMY======");

        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("Switzerland", user.country());
        assertEquals("Full-Stack AI Engineer", user.bio());
        assertEquals("OENEXA Corp", user.company());
        assertEquals("AI Architect", user.profession());
        assertEquals("https://avatar.url/1.png", user.profileImageUrl());
        assertEquals("de", user.language());
        assertEquals("Europe/Zurich", user.timezone());
        assertEquals("ENTERPRISE", user.subscriptionPlan());
        assertEquals("ROLE_ADMIN", user.role());
        assertEquals("org-swiss", user.tenantId());
        assertTrue(user.isTwoFactorEnabled());
        assertEquals("MFRGGZDFMY======", user.twoFactorSecret());
    }

    @Test
    void shouldHandleFailedLoginAttemptsAndLockout() {
        User user = new User("locked_user", "hash", "locked@test.com");
        assertEquals(0, user.failedLoginAttempts());
        assertFalse(user.isAccountLocked());

        for (int i = 1; i <= 4; i++) {
            user.recordFailedLogin();
            assertEquals(i, user.failedLoginAttempts());
            assertFalse(user.isAccountLocked());
        }

        user.recordFailedLogin(); // 5th attempt locks account
        assertEquals(5, user.failedLoginAttempts());
        assertTrue(user.isAccountLocked());
        assertNotNull(user.lockedUntil());

        user.resetFailedLogins();
        assertEquals(0, user.failedLoginAttempts());
        assertFalse(user.isAccountLocked());
        assertNull(user.lockedUntil());
    }
}
