package com.oenexa.api.mapper;

import com.oenexa.api.dto.auth.AuthDtos.LoginRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.RegisterRequestDto;
import com.oenexa.api.dto.auth.AuthDtos.TokenResponseDto;
import com.oenexa.api.dto.auth.AuthDtos.UserProfileDto;
import com.oenexa.domain.AuthDomain.LoginRequest;
import com.oenexa.domain.AuthDomain.RegisterRequest;
import com.oenexa.domain.AuthDomain.TokenPair;
import com.oenexa.domain.AuthDomain.UserProfile;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Authentication DTOs.
 */
@Component
public class AuthApiMapper {

    public LoginRequest toDomainLogin(LoginRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new LoginRequest(dto.usernameOrEmail(), dto.password());
    }

    public RegisterRequest toDomainRegister(RegisterRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new RegisterRequest(dto.username(), dto.email(), dto.password(), dto.fullName());
    }

    public TokenResponseDto toTokenDto(TokenPair pair) {
        if (pair == null) {
            return null;
        }
        return new TokenResponseDto(pair.accessToken(), pair.refreshToken(), pair.expiresInSeconds());
    }

    public UserProfileDto toProfileDto(UserProfile profile) {
        if (profile == null) {
            return null;
        }
        String fullName = ((profile.firstName() != null ? profile.firstName() : "") + " " +
                (profile.lastName() != null ? profile.lastName() : "")).trim();
        if (fullName.isBlank()) {
            fullName = profile.username();
        }
        return new UserProfileDto(
                profile.id(),
                profile.username(),
                profile.email(),
                fullName,
                profile.role(),
                profile.tenantId(),
                profile.subscriptionPlan(),
                profile.twoFactorEnabled(),
                profile.createdAt()
        );
    }
}
