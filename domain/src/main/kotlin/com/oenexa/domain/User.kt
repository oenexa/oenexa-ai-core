package com.oenexa.domain

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false, unique = true)
    var username: String,
    @Column(nullable = false)
    var passwordHash: String,
    @Column(nullable = false)
    var email: String,
    var enabled: Boolean = true,
    var createdAt: Instant = Instant.now()
) {
    // Domain logic (e.g., password verification) can be added here
}
