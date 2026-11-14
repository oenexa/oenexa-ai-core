package com.oenexa.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenID Connect (OIDC) Discovery and Metadata Controller for id.oenexa.com.
 * Conforms to RFC 8414 and OpenID Connect Discovery 1.0 specifications.
 */
@RestController
public class OpenIdConfigurationController {

    private static final String ISSUER = "https://id.oenexa.com";

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<Map<String, Object>> openidConfiguration() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("issuer", ISSUER);
        config.put("authorization_endpoint", ISSUER + "/login");
        config.put("token_endpoint", ISSUER + "/api/v1/auth/login");
        config.put("userinfo_endpoint", ISSUER + "/api/v1/auth/v2/me");
        config.put("jwks_uri", ISSUER + "/.well-known/jwks.json");
        config.put("registration_endpoint", ISSUER + "/register");
        config.put("end_session_endpoint", ISSUER + "/api/v1/auth/logout");
        
        config.put("response_types_supported", List.of("code", "token", "id_token"));
        config.put("subject_types_supported", List.of("public"));
        config.put("id_token_signing_alg_values_supported", List.of("HS256", "RS256"));
        config.put("scopes_supported", List.of(
                "openid",
                "profile",
                "email",
                "wallet:read",
                "trade:write",
                "pay:execute",
                "ai:use",
                "oengo:order"
        ));
        config.put("token_endpoint_auth_methods_supported", List.of("client_secret_post", "client_secret_basic", "none"));
        config.put("claims_supported", List.of("sub", "iss", "aud", "exp", "iat", "email", "name", "role", "tier"));
        config.put("code_challenge_methods_supported", List.of("S256", "plain"));
        config.put("service_documentation", "https://docs.oenexa.com/identity");

        return ResponseEntity.ok(config);
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        // Standard JSON Web Key Set (JWKS) metadata
        Map<String, Object> key = new LinkedHashMap<>();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("alg", "RS256");
        key.put("kid", "oenexa-id-2026-v1");
        key.put("n", "uQ2...oenexa-auth-token-verification-key");
        key.put("e", "AQAB");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("keys", List.of(key));
        return ResponseEntity.ok(response);
    }
}
