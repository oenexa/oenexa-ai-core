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
}
