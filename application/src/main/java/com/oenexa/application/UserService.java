package com.oenexa.application;

import com.oenexa.domain.User;
import com.oenexa.domain.UserRegistration;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Enterprise Application Service for User Management.
 * Why: Encapsulates user registration logic cleanly.
 * How: Uses immutable UserRegistration record inputs and enforces validation.
 */
@Service
public class UserService {

    public User registerUser(UserRegistration registration) {
        Objects.requireNonNull(registration, "registration must not be null");
        return new User(
                registration.username(),
                registration.passwordHash(),
                registration.email()
        );
    }
}
