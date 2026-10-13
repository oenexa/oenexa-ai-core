package com.oenexa.application;

import com.oenexa.domain.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public User registerUser(String username, String email, String passwordHash) {
        // TODO: add validation, password hashing, etc.
        return new User(username, passwordHash, email);
    }
    // Additional business use‑cases go here
}
