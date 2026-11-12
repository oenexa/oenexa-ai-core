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
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enterprise Token Service issuing local HS256 session tokens and verifying
 * both local HS256 and federated id.oenexa.com RS256/JWKS OIDC tokens.
 */
@Service
public class TokenService {

    private static final String DEV_FALLBACK_SECRET = "oenexa-enterprise-ai-platform-secret-key-must-be-at-least-32-bytes!";
    private static final Duration ACCESS_TTL = Duration.ofHours(2);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);
    private static final Duration JWKS_TTL = Duration.ofMinutes(5);
    private static final Pattern JSON_STR_FIELD = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]*)\"");

    private final SecretKey key;
    private final String jwksUrl;
    private final RSAPublicKey staticRsaPublicKey;
    private final HttpClient httpClient;
    private final Map<String, RSAPublicKey> jwksCache = new ConcurrentHashMap<>();
    private volatile Instant jwksLastFetch = Instant.EPOCH;

    public TokenService() {
        this(
                resolveEnvOrDefault("OENEXA_JWT_SECRET", DEV_FALLBACK_SECRET),
                System.getenv("OENEXA_JWKS_URL"),
                parsePublicKeyPem(System.getenv("OENEXA_JWT_PUBLIC_KEY_PEM"))
        );
    }

    public TokenService(String hmacSecret, String jwksUrl, RSAPublicKey staticRsaPublicKey) {
        String effectiveSecret = (hmacSecret != null && !hmacSecret.isBlank()) ? hmacSecret : DEV_FALLBACK_SECRET;
        if (effectiveSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("OENEXA JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(effectiveSecret.getBytes(StandardCharsets.UTF_8));
        this.jwksUrl = (jwksUrl != null && !jwksUrl.isBlank()) ? jwksUrl.trim() : null;
        this.staticRsaPublicKey = staticRsaPublicKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
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
        if (token == null || token.isBlank()) {
            throw new SecurityException("Invalid or expired JWT token: empty token");
        }
        try {
            JwtHeader header = parseJwtHeader(token);
            Claims claims;
            if ("RS256".equalsIgnoreCase(header.alg())) {
                RSAPublicKey verifyKey = resolveRsaPublicKey(header.kid());
                claims = Jwts.parser()
                        .verifyWith(verifyKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } else if ("HS256".equalsIgnoreCase(header.alg())
                    || "HS384".equalsIgnoreCase(header.alg())
                    || "HS512".equalsIgnoreCase(header.alg())) {
                claims = Jwts.parser()
                        .verifyWith(key)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } else {
                throw new SecurityException("Invalid or expired JWT token: unsupported alg " + header.alg());
            }

            String userId = firstNonBlank(claims.get("uuid", String.class), claims.getSubject());
            String username = firstNonBlank(
                    claims.get("username", String.class),
                    claims.get("email", String.class),
                    claims.getSubject()
            );
            String role = resolveRoleClaim(claims);
            Instant issuedAt = claims.getIssuedAt() != null ? claims.getIssuedAt().toInstant() : Instant.now();
            Instant expiresAt = claims.getExpiration() != null ? claims.getExpiration().toInstant() : issuedAt.plus(ACCESS_TTL);

            return new AuthClaims(userId, username, role, issuedAt, expiresAt);
        } catch (SecurityException se) {
            throw se;
        } catch (JwtException | IllegalArgumentException e) {
            throw new SecurityException("Invalid or expired JWT token: " + e.getMessage(), e);
        }
    }

    private RSAPublicKey resolveRsaPublicKey(String kid) {
        if (staticRsaPublicKey != null && (jwksUrl == null || jwksUrl.isBlank())) {
            return staticRsaPublicKey;
        }
        if (jwksUrl != null && !jwksUrl.isBlank()) {
            RSAPublicKey cached = findCachedKey(kid);
            if (cached == null || Duration.between(jwksLastFetch, Instant.now()).compareTo(JWKS_TTL) > 0) {
                refreshJwks();
                cached = findCachedKey(kid);
            }
            if (cached != null) {
                return cached;
            }
        }
        if (staticRsaPublicKey != null) {
            return staticRsaPublicKey;
        }
        throw new SecurityException("Invalid or expired JWT token: no RSA verification key available for kid=" + kid);
    }

    private RSAPublicKey findCachedKey(String kid) {
        if (kid != null && jwksCache.containsKey(kid)) {
            return jwksCache.get(kid);
        }
        if ((kid == null || kid.isBlank()) && !jwksCache.isEmpty()) {
            return jwksCache.values().iterator().next();
        }
        return null;
    }

    private synchronized void refreshJwks() {
        if (jwksUrl == null || jwksUrl.isBlank()) {
            return;
        }
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jwksUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300 || response.body() == null) {
                throw new SecurityException("Invalid or expired JWT token: JWKS HTTP " + response.statusCode());
            }
            parseJwksIntoCache(response.body());
            jwksLastFetch = Instant.now();
        } catch (SecurityException se) {
            throw se;
        } catch (Exception e) {
            throw new SecurityException("Invalid or expired JWT token: failed to fetch JWKS: " + e.getMessage(), e);
        }
    }

    private void parseJwksIntoCache(String json) {
        int keysIdx = json.indexOf("\"keys\"");
        if (keysIdx < 0) {
            return;
        }
        int arrStart = json.indexOf('[', keysIdx);
        int arrEnd = json.lastIndexOf(']');
        if (arrStart < 0 || arrEnd <= arrStart) {
            return;
        }
        String arrayContent = json.substring(arrStart + 1, arrEnd);
        int depth = 0;
        int objStart = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char ch = arrayContent.charAt(i);
            if (ch == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (ch == '}') {
                depth--;
                if (depth == 0 && objStart >= 0) {
                    parseSingleJwk(arrayContent.substring(objStart, i + 1));
                    objStart = -1;
                }
            }
        }
    }

    private void parseSingleJwk(String objJson) {
        String kty = null, kid = null, n = null, e = null;
        Matcher m = JSON_STR_FIELD.matcher(objJson);
        while (m.find()) {
            switch (m.group(1)) {
                case "kty" -> kty = m.group(2);
                case "kid" -> kid = m.group(2);
                case "n" -> n = m.group(2);
                case "e" -> e = m.group(2);
                default -> {}
            }
        }
        if ("RSA".equalsIgnoreCase(kty) && n != null && e != null) {
            try {
                BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n));
                BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(e));
                RSAPublicKey pub = (RSAPublicKey) KeyFactory.getInstance("RSA")
                        .generatePublic(new RSAPublicKeySpec(modulus, exponent));
                jwksCache.put(kid != null ? kid : "default", pub);
            } catch (Exception ignored) {
                // Skip malformed JWK entry while allowing valid keys in the set
            }
        }
    }

    private static JwtHeader parseJwtHeader(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new SecurityException("Invalid or expired JWT token: malformed compact JWS");
        }
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
        String alg = null;
        String kid = null;
        Matcher m = JSON_STR_FIELD.matcher(headerJson);
        while (m.find()) {
            if ("alg".equals(m.group(1))) {
                alg = m.group(2);
            } else if ("kid".equals(m.group(1))) {
                kid = m.group(2);
            }
        }
        return new JwtHeader(alg != null ? alg : "", kid);
    }

    private static String resolveRoleClaim(Claims claims) {
        String directRole = claims.get("role", String.class);
        if (directRole != null && !directRole.isBlank()) {
            return directRole;
        }
        Object rawRoles = claims.get("roles");
        if (rawRoles instanceof List<?> list && !list.isEmpty()) {
            return String.valueOf(list.get(0));
        }
        return "ROLE_USER";
    }

    private static String firstNonBlank(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) {
                return c;
            }
        }
        return null;
    }

    private static String resolveEnvOrDefault(String name, String fallback) {
        String val = System.getenv(name);
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    private static RSAPublicKey parsePublicKeyPem(String pem) {
        if (pem == null || pem.isBlank()) {
            return null;
        }
        try {
            String normalized = pem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] der = Base64.getDecoder().decode(normalized);
            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid OENEXA_JWT_PUBLIC_KEY_PEM", e);
        }
    }

    private record JwtHeader(String alg, String kid) {}
}


