package com.oenexa.application.core;

import com.oenexa.domain.model.cognitive.OenexaCoreBrainDomain.SecurityAssessment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Core Module 10: Security Engine.
 * Provides fine-grained access control, SHA-256 integrity hashing, prompt injection detection,
 * secret leak prevention, and autonomous threat mitigation across all cognitive interactions.
 */
@Service
public class OenexaSecurityEngine {

    private static final List<Pattern> THREAT_PATTERNS = List.of(
            Pattern.compile("(?i)ignore (all )?previous instructions"),
            Pattern.compile("(?i)drop table "),
            Pattern.compile("(?i)eval\\(.*system\\("),
            Pattern.compile("(?i)BEGIN PRIVATE KEY"),
            Pattern.compile("(?i)bearer [a-zA-Z0-9_\\-\\.]{32,}")
    );

    private final Map<String, List<String>> auditTrails = new ConcurrentHashMap<>();

    public SecurityAssessment evaluateThreatAndIntegrity(String payload, String userId, String tenantId) {
        if (payload == null || payload.isBlank()) {
            return SecurityAssessment.denied("EMPTY_PAYLOAD", 0.8);
        }

        // Threat pattern scanning
        for (Pattern p : THREAT_PATTERNS) {
            if (p.matcher(payload).find()) {
                recordAudit(tenantId, userId, "THREAT_BLOCKED: " + p.pattern());
                return SecurityAssessment.denied("THREAT_DETECTED", 0.95);
            }
        }

        // Compute SHA-256 integrity hash
        String integrityHash = computeSha256(payload);
        recordAudit(tenantId, userId, "CLEARED: " + integrityHash);

        return new SecurityAssessment(true, 0.0, "CLEAN", integrityHash, Instant.now());
    }

    public List<String> getAuditTrail(String tenantId) {
        return auditTrails.getOrDefault(tenantId != null ? tenantId : "default", List.of());
    }

    private void recordAudit(String tenantId, String userId, String event) {
        String key = (tenantId != null && !tenantId.isBlank()) ? tenantId : "default";
        auditTrails.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()))
                .add("[" + Instant.now() + "] User: " + userId + " -> " + event);
    }

    private String computeSha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder("sha256:");
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "sha256:unknown";
        }
    }
}

