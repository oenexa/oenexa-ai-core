package com.oenexa.api.controller;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.service.code.CodeEngineeringService;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.model.code.CodeEngineeringDomain.TestGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CodeEngineeringController Zero-Mock Unit Tests")
class CodeEngineeringControllerTest {

    private CodeEngineeringService service;
    private CodeEngineeringController controller;

    @BeforeEach
    void setUp() {
        service = new CodeEngineeringService();
        controller = new CodeEngineeringController(service);
    }

    @Test
    @DisplayName("Should reject null service in constructor")
    void shouldRejectNullService() {
        assertThrows(NullPointerException.class, () -> new CodeEngineeringController(null));
    }

    @Test
    @DisplayName("Should analyze code and return 200 OK")
    void shouldAnalyzeCode() {
        CodeAnalysisRequest request = new CodeAnalysisRequest("public class Solution { public void run() {} }", "java");
        ResponseEntity<CodeAnalysisResponse> response = controller.analyzeCode(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("java", response.getBody().language());
        assertTrue(response.getBody().classes().contains("Solution"));
        assertTrue(response.getBody().methods().contains("run"));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when analyzeCode request is null")
    void shouldReturnBadRequestForNullAnalyzeRequest() {
        ResponseEntity<CodeAnalysisResponse> response = controller.analyzeCode(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should scan security and return 200 OK")
    void shouldScanSecurity() {
        SecurityScanRequest request = new SecurityScanRequest("public class Secure {}", "Secure.java");
        ResponseEntity<SecurityScanResponse> response = controller.scanSecurity(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().passed());
        assertEquals(0, response.getBody().totalIssues());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when scanSecurity request is null")
    void shouldReturnBadRequestForNullSecurityRequest() {
        ResponseEntity<SecurityScanResponse> response = controller.scanSecurity(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("Should generate zero-mock test suite and return 200 OK")
    void shouldGenerateTests() {
        TestGenerationRequest request = new TestGenerationRequest("Solution", List.of("listActiveUsers"), "JUNIT_5");
        ResponseEntity<TestGenerationResponse> response = controller.generateTests(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SolutionTest", response.getBody().suiteName());
        assertTrue(response.getBody().zeroMockEnforced());
        assertNotNull(response.getBody().code());
        assertNotNull(response.getBody().testCode());
    }

    @Test
    @DisplayName("Should return 400 Bad Request when generateTests request is null")
    void shouldReturnBadRequestForNullGenerateTestsRequest() {
        ResponseEntity<TestGenerationResponse> response = controller.generateTests(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }
}



