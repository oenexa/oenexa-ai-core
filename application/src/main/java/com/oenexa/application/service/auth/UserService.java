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


