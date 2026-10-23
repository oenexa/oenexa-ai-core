package com.oenexa.application.validation;

import com.oenexa.domain.AuthDomain.LoginRequest;
import com.oenexa.domain.AuthDomain.RegisterRequest;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthValidator Zero-Mock Unit Tests")
class AuthValidatorTest {

    private final AuthValidator validator = new AuthValidator();

    @Test
    @DisplayName("Should accept valid login request")
    void shouldAcceptValidLogin() {
        LoginRequest req = new LoginRequest("developer", "Password123!");
        assertDoesNotThrow(() -> validator.validateLogin(req));
    }

    @Test
    @DisplayName("Should reject null login request or blank credentials")
    void shouldRejectInvalidLogin() {
        assertThrows(OenexaValidationException.class, () -> validator.validateLogin(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateLogin(new LoginRequest("", "secret")));
        assertThrows(OenexaValidationException.class, () -> validator.validateLogin(new LoginRequest("admin", "")));
    }

    @Test
    @DisplayName("Should accept valid registration request")
    void shouldAcceptValidRegister() {
        RegisterRequest req = new RegisterRequest("engineer_01", "dev@oenexa.ai", "SecretPass123!", "Oenexa Dev");
        assertDoesNotThrow(() -> validator.validateRegister(req));
    }

    @Test
    @DisplayName("Should reject registration with invalid email or weak password")
    void shouldRejectInvalidRegister() {
        assertThrows(OenexaValidationException.class, () -> validator.validateRegister(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateRegister(
                new RegisterRequest("dev", "not-an-email", "short", "Full Name")
        ));
    }
}
