package com.oenexa.application.service.auth;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.auth.User;
import com.oenexa.domain.model.auth.UserRegistration;
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


