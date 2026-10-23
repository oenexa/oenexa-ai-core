package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Security Policy Layer for Code Intelligence & SAST Domain.
 * Inspects source code and AST trees against dangerous runtime calls, unsafe memory operations,
 * and malicious reflection primitives.
 */
@Component
public class CodeSecurityPolicy {

    private static final List<String> DANGEROUS_PATTERNS = List.of(
            "sun.misc.Unsafe",
            "Runtime.getRuntime().exec",
            "ProcessBuilder",
            "System.exit(",
            "java.lang.reflect.AccessibleObject.setAccessible(true)"
    );

    public void auditCodeSecurity(String sourceCode) {
        if (sourceCode == null || sourceCode.isBlank()) {
            return;
        }
        List<String> detectedVulnerabilities = new ArrayList<>();
        for (String pattern : DANGEROUS_PATTERNS) {
            if (sourceCode.contains(pattern)) {
                detectedVulnerabilities.add("Disallowed dangerous runtime construct detected: " + pattern);
            }
        }

        if (!detectedVulnerabilities.isEmpty()) {
            throw new OenexaValidationException(
                    "SAST Code Security Gate Rejected code submission",
                    detectedVulnerabilities
            );
        }
    }
}

