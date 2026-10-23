package com.oenexa.application.mapper;

import com.oenexa.domain.model.auth.AuthDomain.AuthClaims;
import com.oenexa.domain.model.auth.AuthDomain.UserProfile;
import com.oenexa.domain.model.auth.User;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Enterprise Mapper Layer for Authentication & Identity Domain.
 * Maps between JPA User domain records and UserProfile / AuthClaims DTOs.
 */
@Component
public class AuthDtoMapper {

    public UserProfile toProfile(User user) {
        if (user == null) {
            return null;
        }
        return new UserProfile(
                user.id(),
                user.username(),
                user.email(),
                user.firstName(),
                user.lastName(),
                user.country(),
                user.profileImageUrl(),
                user.bio(),
                user.company(),
                user.profession(),
                user.language(),
                user.timezone(),
                user.subscriptionPlan(),
                user.role(),
                user.tenantId(),
                user.isTwoFactorEnabled(),
                user.createdAt()
        );
    }

    public AuthClaims toClaims(User user, long expirySeconds) {
        if (user == null) {
            return null;
        }
        Instant now = Instant.now();
        return new AuthClaims(
                String.valueOf(user.id()),
                user.username(),
                user.role(),
                now,
                now.plusSeconds(expirySeconds)
        );
    }
}

