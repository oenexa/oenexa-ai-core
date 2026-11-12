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
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

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

    @Test
    @DisplayName("Should verify RS256 OIDC tokens issued by id.oenexa.com via live JWKS endpoint")
    void shouldVerifyRs256TokenViaJwksEndpoint() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

        String nB64 = encodeUnsignedBigInt(pub.getModulus());
        String eB64 = encodeUnsignedBigInt(pub.getPublicExponent());
        String jwksJson = "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"oenexa-id-k1\",\"n\":\""
                + nB64 + "\",\"e\":\"" + eB64 + "\"}]}";

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/.well-known/jwks.json", exchange -> {
            byte[] body = jwksJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        try {
            String jwksUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/.well-known/jwks.json";
            TokenService federatedService = new TokenService(
                    "custom-local-hmac-secret-at-least-32-bytes-long!!",
                    jwksUrl,
                    null
            );

            Instant now = Instant.now();
            String ssoToken = Jwts.builder()
                    .header().keyId("oenexa-id-k1").and()
                    .subject("founder@oenexa.com")
                    .issuer("https://id.oenexa.com")
                    .claim("uuid", "uuid-founder-001")
                    .claim("email", "founder@oenexa.com")
                    .claim("roles", List.of("ROLE_TRADER", "ROLE_ADMIN"))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(900)))
                    .signWith(kp.getPrivate(), Jwts.SIG.RS256)
                    .compact();

            AuthClaims claims = federatedService.parse(ssoToken);
            assertEquals("uuid-founder-001", claims.userId());
            assertEquals("founder@oenexa.com", claims.username());
            assertEquals("ROLE_TRADER", claims.role());

            // Must reject unsigned/alg-none token
            String algNoneToken = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8))
                    + "." + Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"attacker\"}".getBytes(StandardCharsets.UTF_8))
                    + ".fakesig";
            assertThrows(SecurityException.class, () -> federatedService.parse(algNoneToken));

            // Must reject token signed with different secret
            TokenService otherSecretService = new TokenService(
                    "another-totally-different-secret-at-least-32-bytes!",
                    null,
                    null
            );
            TokenPair foreignPair = otherSecretService.issue("u1", "eve", "ROLE_USER");
            assertThrows(SecurityException.class, () -> federatedService.parse(foreignPair.accessToken()));
        } finally {
            server.stop(0);
        }
    }

    private static String encodeUnsignedBigInt(BigInteger value) {
        byte[] bytes = value.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] trimmed = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, trimmed, 0, trimmed.length);
            bytes = trimmed;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}


