package com.oenexa.application.validation;

import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Model Context Protocol (MCP v1.0).
 */
@Component
public class McpValidator {

    public void validateToolExecution(String toolName) {
        if (toolName == null || toolName.trim().isBlank()) {
            throw new OenexaValidationException("toolName must not be blank");
        }
    }

    public void validateRpcRequest(String jsonrpc, String method) {
        List<String> violations = new ArrayList<>();
        if (jsonrpc == null || !jsonrpc.equals("2.0")) {
            violations.add("jsonrpc version must be exactly '2.0'");
        }
        if (method == null || method.trim().isBlank()) {
            violations.add("method must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("MCP RPC validation failed", violations);
        }
    }
}
