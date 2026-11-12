package com.oenexa.api.filter;

import com.oenexa.application.service.auth.TokenService;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

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

@DisplayName("OenexaSsoAuthenticationFilter Resource-Server Tests")
class OenexaSsoAuthenticationFilterTest {

    @Test
    @DisplayName("When enforce=true, rejects missing token and accepts federated RS256 JWKS token from id.oenexa.com")
    void shouldEnforceAndVerifyFederatedRs256Token() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        String jwksJson = "{\"keys\":[{\"kty\":\"RSA\",\"use\":\"sig\",\"alg\":\"RS256\",\"kid\":\"oenexa-id-k1\",\"n\":\""
                + encodeUnsignedBigInt(pub.getModulus()) + "\",\"e\":\"" + encodeUnsignedBigInt(pub.getPublicExponent()) + "\"}]}";

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/.well-known/jwks.json", ex -> {
            byte[] body = jwksJson.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        try {
            TokenService tokenService = new TokenService(
                    "unit-test-hmac-secret-must-be-at-least-32-bytes-long",
                    "http://127.0.0.1:" + server.getAddress().getPort() + "/.well-known/jwks.json",
                    null
            );
            OenexaSsoAuthenticationFilter strictFilter = new OenexaSsoAuthenticationFilter(tokenService, true, false);

            // 1. Missing Authorization header -> 401
            MockHttpServletRequest missingReq = new MockHttpServletRequest("GET", "/api/v1/agents");
            MockHttpServletResponse missingRes = new MockHttpServletResponse();
            strictFilter.doFilter(missingReq, missingRes, new MockFilterChain());
            assertEquals(401, missingRes.getStatus());

            // 2. Untrusted X-Oenexa-Sub ignored when trustGatewayHeaders=false -> 401
            MockHttpServletRequest spoofReq = new MockHttpServletRequest("GET", "/api/v1/agents");
            spoofReq.addHeader("X-Oenexa-Sub", "spoofed-admin");
            MockHttpServletResponse spoofRes = new MockHttpServletResponse();
            strictFilter.doFilter(spoofReq, spoofRes, new MockFilterChain());
            assertEquals(401, spoofRes.getStatus());

            // 3. Public health & auth routes pass without token
            MockHttpServletRequest healthReq = new MockHttpServletRequest("GET", "/api/health");
            MockHttpServletResponse healthRes = new MockHttpServletResponse();
            strictFilter.doFilter(healthReq, healthRes, new MockFilterChain());
            assertEquals(200, healthRes.getStatus());

            // 4. Valid RS256 SSO token -> 200 and binds attributes
            Instant now = Instant.now();
            String ssoJwt = Jwts.builder()
                    .header().keyId("oenexa-id-k1").and()
                    .subject("architect@oenexa.com")
                    .issuer("https://id.oenexa.com")
                    .claim("uuid", "sub-ai-42")
                    .claim("email", "architect@oenexa.com")
                    .claim("roles", List.of("ROLE_ENGINEER"))
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(300)))
                    .signWith(kp.getPrivate(), Jwts.SIG.RS256)
                    .compact();

            MockHttpServletRequest validReq = new MockHttpServletRequest("GET", "/api/v1/agents");
            validReq.addHeader("Authorization", "Bearer " + ssoJwt);
            MockHttpServletResponse validRes = new MockHttpServletResponse();
            strictFilter.doFilter(validReq, validRes, new MockFilterChain());
            assertEquals(200, validRes.getStatus());
            assertEquals("sub-ai-42", validReq.getAttribute(OenexaSsoAuthenticationFilter.ATTR_USER_ID));
            assertEquals("architect@oenexa.com", validReq.getAttribute(OenexaSsoAuthenticationFilter.ATTR_USERNAME));
            assertEquals("ROLE_ENGINEER", validReq.getAttribute(OenexaSsoAuthenticationFilter.ATTR_ROLE));
        } finally {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("When enforce=false, still rejects invalid Bearer tokens while honoring trusted gateway headers if enabled")
    void shouldRejectTamperedBearerEvenInPermissiveMode() throws Exception {
        TokenService tokenService = new TokenService();
        OenexaSsoAuthenticationFilter gatewayAwareFilter = new OenexaSsoAuthenticationFilter(tokenService, false, true);

        MockHttpServletRequest badBearerReq = new MockHttpServletRequest("POST", "/api/v1/chat");
        badBearerReq.addHeader("Authorization", "Bearer tampered.jwt.token");
        MockHttpServletResponse badBearerRes = new MockHttpServletResponse();
        gatewayAwareFilter.doFilter(badBearerReq, badBearerRes, new MockFilterChain());
        assertEquals(401, badBearerRes.getStatus());

        MockHttpServletRequest gwReq = new MockHttpServletRequest("POST", "/api/v1/chat");
        gwReq.addHeader("X-Oenexa-Sub", "usr-gw-1");
        gwReq.addHeader("X-User-Id", "user@oenexa.com");
        gwReq.addHeader("X-User-Roles", "ROLE_TRADER,ROLE_USER");
        MockHttpServletResponse gwRes = new MockHttpServletResponse();
        gatewayAwareFilter.doFilter(gwReq, gwRes, new MockFilterChain());
        assertEquals(200, gwRes.getStatus());
        assertEquals("usr-gw-1", gwReq.getAttribute(OenexaSsoAuthenticationFilter.ATTR_USER_ID));
        assertEquals("ROLE_TRADER", gwReq.getAttribute(OenexaSsoAuthenticationFilter.ATTR_ROLE));
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
