package com.oenexa.api.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenIdConfigurationController Unit Tests")
class OpenIdConfigurationControllerTest {

    private final OpenIdConfigurationController controller = new OpenIdConfigurationController();

    @Test
    @DisplayName("Returns compliant OIDC discovery configuration for id.oenexa.com")
    void openidConfigurationReturnsCorrectMetadata() {
        ResponseEntity<Map<String, Object>> response = controller.openidConfiguration();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("https://id.oenexa.com", body.get("issuer"));
        assertEquals("https://id.oenexa.com/login", body.get("authorization_endpoint"));
        assertEquals("https://id.oenexa.com/api/v1/auth/login", body.get("token_endpoint"));
        assertEquals("https://id.oenexa.com/.well-known/jwks.json", body.get("jwks_uri"));

        @SuppressWarnings("unchecked")
        List<String> scopes = (List<String>) body.get("scopes_supported");
        assertTrue(scopes.contains("openid"));
        assertTrue(scopes.contains("wallet:read"));
        assertTrue(scopes.contains("trade:write"));

        @SuppressWarnings("unchecked")
        List<String> codeChallenges = (List<String>) body.get("code_challenge_methods_supported");
        assertTrue(codeChallenges.contains("S256"));
    }

    @Test
    @DisplayName("Returns valid JWKS keystore structure")
    void jwksReturnsKeyList() {
        ResponseEntity<Map<String, Object>> response = controller.jwks();
        assertEquals(HttpStatus.OK, response.getStatusCode());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("keys"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keys = (List<Map<String, Object>>) body.get("keys");
        assertFalse(keys.isEmpty());
        assertEquals("RSA", keys.get(0).get("kty"));
    }
}
