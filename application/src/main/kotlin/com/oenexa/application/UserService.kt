package com.oenexa.application

import com.oenexa.domain.User
import org.springframework.stereotype.Service

@Service
class UserService {
    fun registerUser(username: String, email: String, passwordHash: String): User {
        // TODO: add validation, password encoding, etc.
        return User(
            username = username,
            passwordHash = passwordHash,
            email = email
        )
    }
    // Additional business use‑cases go here
}
