package com.oenexa.application.validation;

import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.exception.OenexaExceptions.OenexaValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CodeEngineeringValidator Zero-Mock Unit Tests")
class CodeEngineeringValidatorTest {

    private final CodeEngineeringValidator validator = new CodeEngineeringValidator();

    @Test
    @DisplayName("Should accept valid analysis, security scan, and test generation requests")
    void shouldAcceptValidRequests() {
        CodeAnalysisRequest analysis = new CodeAnalysisRequest("public class A {}", "java");
        assertDoesNotThrow(() -> validator.validateAnalysisRequest(analysis));

        SecurityScanRequest scan = new SecurityScanRequest("public class A {}", "src/main/A.java");
        assertDoesNotThrow(() -> validator.validateSecurityScan(scan));

        TestGenerationRequest testReq = new TestGenerationRequest("Service", List.of("execute"), "JUNIT_5", "code");
        assertDoesNotThrow(() -> validator.validateTestGeneration(testReq));
    }

    @Test
    @DisplayName("Should reject blank code or traversal characters")
    void shouldRejectViolations() {
        assertThrows(OenexaValidationException.class, () -> validator.validateAnalysisRequest(null));
        assertThrows(OenexaValidationException.class, () -> validator.validateAnalysisRequest(new CodeAnalysisRequest("", "java")));

        SecurityScanRequest traversal = new SecurityScanRequest("class A {}", "../../etc/passwd");
        assertThrows(OenexaValidationException.class, () -> validator.validateSecurityScan(traversal));

        TestGenerationRequest blankClass = new TestGenerationRequest("", List.of(), "JUNIT_5", "code");
        // TestGenerationRequest has a constructor that replaces blank className with "Solution" unless manually passed
        assertDoesNotThrow(() -> validator.validateTestGeneration(blankClass));
    }
}

