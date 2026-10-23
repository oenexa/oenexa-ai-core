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
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Enterprise Token Service issuing and validating signed HS256 JWTs.
 */
@Service
public class TokenService {

    private static final String DEFAULT_SECRET = "oenexa-enterprise-ai-platform-secret-key-must-be-at-least-32-bytes!";
    private static final Duration ACCESS_TTL = Duration.ofHours(2);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    private final SecretKey key;

    public TokenService() {
        this.key = Keys.hmacShaKeyFor(DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair issue(String userId, String username, String role) {
        Instant now = Instant.now();
        Instant accessExpiry = now.plus(ACCESS_TTL);
        Instant refreshExpiry = now.plus(REFRESH_TTL);

        String accessToken = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .claim("typ", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(accessExpiry))
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .claim("typ", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(refreshExpiry))
                .signWith(key)
                .compact();

        return new TokenPair(accessToken, refreshToken, ACCESS_TTL.toSeconds());
    }

    public AuthClaims parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return new AuthClaims(
                    claims.getSubject(),
                    claims.get("username", String.class),
                    claims.get("role", String.class),
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant()
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid or expired JWT token: " + e.getMessage(), e);
        }
    }
}


