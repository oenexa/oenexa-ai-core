package com.oenexa.application;

import com.oenexa.domain.User;
import com.oenexa.domain.UserRegistration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void shouldRegisterUserSuccessfully() {
        UserService service = new UserService();
        UserRegistration registration = new UserRegistration("charlie", "charlie@oenexa.com", "secureHash_99");
        User user = service.registerUser(registration);

        assertNotNull(user);
        assertEquals("charlie", user.username());
        assertEquals("charlie@oenexa.com", user.email());
        assertEquals("secureHash_99", user.passwordHash());
        assertTrue(user.isEnabled());
    }

    @Test
    void shouldRejectNullRegistration() {
        UserService service = new UserService();
        assertThrows(NullPointerException.class, () -> service.registerUser(null));
    }
}
