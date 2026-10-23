package com.oenexa.application.service.auth;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.auth.AuthDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenService Zero-Mock Unit Tests")
class TokenServiceTest {

    private final TokenService tokenService = new TokenService();

    @Test
    @DisplayName("Should issue valid token pair and parse access token claims successfully")
    void shouldIssueAndParseAccessToken() {
        TokenPair pair = tokenService.issue("usr-123", "alice", "ADMIN");

        assertNotNull(pair);
        assertNotNull(pair.accessToken());
        assertNotNull(pair.refreshToken());
        assertTrue(pair.expiresInSeconds() > 0);

        AuthClaims claims = tokenService.parse(pair.accessToken());
        assertNotNull(claims);
        assertEquals("usr-123", claims.userId());
        assertEquals("alice", claims.username());
        assertEquals("ADMIN", claims.role());
        assertNotNull(claims.issuedAt());
        assertNotNull(claims.expiresAt());
        assertTrue(claims.expiresAt().isAfter(claims.issuedAt()));
    }

    @Test
    @DisplayName("Should parse refresh token claims successfully")
    void shouldParseRefreshToken() {
        TokenPair pair = tokenService.issue("usr-456", "bob", "DEVELOPER");
        AuthClaims claims = tokenService.parse(pair.refreshToken());

        assertNotNull(claims);
        assertEquals("usr-456", claims.userId());
        assertEquals("bob", claims.username());
        assertEquals("DEVELOPER", claims.role());
    }

    @Test
    @DisplayName("Should throw SecurityException when parsing an invalid or tampered token")
    void shouldThrowSecurityExceptionOnTamperedToken() {
        TokenPair pair = tokenService.issue("usr-789", "charlie", "VIEWER");
        String tampered = pair.accessToken() + "tampered";

        SecurityException ex = assertThrows(SecurityException.class, () -> tokenService.parse(tampered));
        assertTrue(ex.getMessage().contains("Invalid or expired JWT token"));
    }

    @Test
    @DisplayName("Should throw SecurityException when parsing null or malformed token")
    void shouldThrowSecurityExceptionOnNullOrMalformedToken() {
        assertThrows(SecurityException.class, () -> tokenService.parse(null));
        assertThrows(SecurityException.class, () -> tokenService.parse("not-a-jwt"));
    }
}


