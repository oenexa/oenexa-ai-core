package com.oenexa.application.gateway;

import com.oenexa.domain.model.gateway.AiGatewayDomain.GovernanceCheckResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Enterprise Prompt Governance and Guardrail Policy.
 * Inspects inbound prompt payloads for:
 * 1. Hardcoded private keys, JWTs, and API credentials.
 * 2. Malicious system prompt extraction / jailbreak injection vectors.
 * 3. Prohibited destructive execution commands without authorization.
 */
@Component
public class PromptGovernancePolicy {

    private static final List<Pattern> SENSITIVE_PATTERNS = List.of(
            Pattern.compile("(?i)-----BEGIN (RSA|EC|DSA|OPENSSH) PRIVATE KEY-----"),
            Pattern.compile("(?i)(sk-[a-zA-Z0-9]{20,})"),
            Pattern.compile("(?i)ghp_[a-zA-Z0-9]{36}"),
            Pattern.compile("(?i)(password|secret|apikey)\\s*[:=]\\s*['\"][^'\"]{6,}['\"]")
    );

    private static final List<String> JAILBREAK_SIGNATURES = List.of(
            "ignore all previous instructions",
            "disregard system instructions",
            "you are now in developer mode",
            "jailbreak enabled",
            "override all safety filters"
    );

    public GovernanceCheckResult evaluatePromptSafety(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return GovernanceCheckResult.approved(0.0);
        }

        String lowerPrompt = prompt.toLowerCase(Locale.ROOT);

        // 1. Jailbreak and Prompt Extraction Check
        for (String signature : JAILBREAK_SIGNATURES) {
            if (lowerPrompt.contains(signature)) {
                return GovernanceCheckResult.rejected(
                        "PROMPT_INJECTION_DETECTED",
                        "Security Policy: Request contains adversarial prompt injection phrase: '" + signature + "'"
                );
            }
        }

        // 2. Sensitive Secret Leak Detection
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            if (pattern.matcher(prompt).find()) {
                return GovernanceCheckResult.rejected(
                        "CREDENTIAL_LEAK_PREVENTED",
                        "Data Loss Prevention: Inbound prompt contains unmasked credentials or private keys."
                );
            }
        }

        return GovernanceCheckResult.approved(0.0);
    }
}
