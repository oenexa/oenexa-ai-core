package com.oenexa.application.validation;

import com.oenexa.domain.model.auth.AuthDomain.LoginRequest;
import com.oenexa.domain.model.auth.AuthDomain.RegisterRequest;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Enterprise Validation Layer for Authentication & Identity Domain.
 * Enforces username syntax, email RFC 5322 regex validation, and password strength policies.
 */
@Component
public class AuthValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,32}$");

    public void validateLogin(LoginRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Login request payload must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (request.usernameOrEmail() == null || request.usernameOrEmail().isBlank()) {
            violations.add("usernameOrEmail must not be blank");
        }
        if (request.password() == null || request.password().isBlank()) {
            violations.add("password must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Invalid login credentials request", violations);
        }
    }

    public void validateRegister(RegisterRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Registration request payload must not be null");
        }
        List<String> violations = new ArrayList<>();

        if (request.username() == null || !USERNAME_PATTERN.matcher(request.username().trim()).matches()) {
            violations.add("username must be 3-32 characters and contain only alphanumeric, '.', '_' or '-' characters");
        }
        if (request.email() == null || !EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            violations.add("email must be a valid RFC 5322 email address");
        }
        if (request.password() == null || request.password().length() < 8) {
            violations.add("password must be at least 8 characters long");
        }

        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Registration validation failed", violations);
        }
    }
}

