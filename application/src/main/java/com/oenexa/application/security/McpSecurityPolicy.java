package com.oenexa.application.security;

import com.oenexa.domain.exception.OenexaExceptions.OenexaForbiddenException;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Enterprise Security Policy for Model Context Protocol (MCP v1.0).
 */
@Component
public class McpSecurityPolicy {

    private static final List<String> DANGEROUS_COMMANDS = List.of("rm -rf /", ":(){ :|:& };:", "drop database", "format c:");

    public void verifyToolSafety(String toolName, String commandInput) {
        if ("system_exec".equalsIgnoreCase(toolName) || "terminal_exec".equalsIgnoreCase(toolName)) {
            if (commandInput != null) {
                for (String dangerous : DANGEROUS_COMMANDS) {
                    if (commandInput.toLowerCase().contains(dangerous)) {
                        throw new OenexaValidationException("MCP execution aborted: Dangerous command blocked by sandbox.");
                    }
                }
            }
        }
    }

    public void verifyToolAccess(String toolName, String role) {
        if ("admin_debug_eval".equalsIgnoreCase(toolName) && !"ROLE_ADMIN".equalsIgnoreCase(role)) {
            throw new OenexaForbiddenException("MCP Tool " + toolName + " requires administrator role", "ROLE_ADMIN");
        }
    }
}
