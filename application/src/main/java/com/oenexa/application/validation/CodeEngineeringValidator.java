package com.oenexa.application.validation;

import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Code Intelligence Domain.
 * Validates file paths, source code content, security scan requests, and test synthesis requests.
 */
@Component
public class CodeEngineeringValidator {

    public void validateAnalysisRequest(CodeAnalysisRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Code analysis request must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (request.code() == null || request.code().trim().isBlank()) {
            violations.add("code must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Code analysis request validation failed", violations);
        }
    }

    public void validateSecurityScan(SecurityScanRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Security scan request must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (request.code() == null || request.code().trim().isBlank()) {
            violations.add("code must not be blank");
        }
        if (request.filename() != null && request.filename().contains("..")) {
            violations.add("filename must not contain path traversal characters ('..')");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Security scan request validation failed", violations);
        }
    }

    public void validateTestGeneration(TestGenerationRequest request) {
        if (request == null) {
            throw new OenexaValidationException("Test generation request must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (request.className() == null || request.className().trim().isBlank()) {
            violations.add("className must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Test generation request validation failed", violations);
        }
    }
}

