package com.oenexa.application.security;

import com.oenexa.domain.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise Security Policy Layer for Autonomous Agents & Swarm Orchestration.
 * Enforces execution sandbox boundaries, maximum delegation recursion depth,
 * and dangerous command blacklists.
 */
@Component
public class AgentSecurityPolicy {

    public static final int MAX_DELEGATION_DEPTH = 10;

    private static final List<String> FORBIDDEN_COMMANDS = List.of(
            "rm -rf /",
            "mkfs",
            "dd if=",
            ":(){ :|:& };:",
            "chmod -r 777 /",
            "shutdown -h now"
    );

    public void verifyDelegationDepth(int currentDepth) {
        if (currentDepth > MAX_DELEGATION_DEPTH) {
            throw new OenexaForbiddenException(
                    "Autonomous agent delegation depth exceeded maximum allowable threshold (" + MAX_DELEGATION_DEPTH + ")",
                    "ROLE_AGENT_ADMIN"
            );
        }
    }

    public void verifyCommandSafety(String command) {
        if (command == null || command.isBlank()) {
            return;
        }
        String normalized = command.toLowerCase().trim();
        for (String forbidden : FORBIDDEN_COMMANDS) {
            if (normalized.contains(forbidden)) {
                throw new OenexaValidationException(
                        "Command execution blocked by Autonomous Agent Sandbox Guard: Disallowed destructive command detected."
                );
            }
        }
    }
}
