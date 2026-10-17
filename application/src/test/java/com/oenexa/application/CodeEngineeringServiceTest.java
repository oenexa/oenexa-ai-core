package com.oenexa.application;

import com.oenexa.application.engine.AstAnalysisEngine;
import com.oenexa.application.engine.SecurityAuditEngine;
import com.oenexa.application.engine.TestSynthesizerEngine;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisRequest;
import com.oenexa.domain.CodeEngineeringDomain.CodeAnalysisResponse;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanRequest;
import com.oenexa.domain.CodeEngineeringDomain.SecurityScanResponse;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CodeEngineeringService Zero-Mock Unit Tests")
class CodeEngineeringServiceTest {

    private CodeEngineeringService service;

    @BeforeEach
    void setUp() {
        service = new CodeEngineeringService();
    }

    @Test
    @DisplayName("Should validate non-null constructor arguments")
    void shouldValidateConstructorArguments() {
        AstAnalysisEngine ast = new AstAnalysisEngine();
        SecurityAuditEngine sec = new SecurityAuditEngine();
        TestSynthesizerEngine test = new TestSynthesizerEngine();

        assertThrows(NullPointerException.class, () -> new CodeEngineeringService(null, sec, test));
        assertThrows(NullPointerException.class, () -> new CodeEngineeringService(ast, null, test));
        assertThrows(NullPointerException.class, () -> new CodeEngineeringService(ast, sec, null));

        CodeEngineeringService customService = new CodeEngineeringService(ast, sec, test);
        assertNotNull(customService);
    }

    @Test
    @DisplayName("Should delegate analyzeAst to AstAnalysisEngine")
    void shouldDelegateAnalyzeAst() {
        CodeAnalysisRequest request = new CodeAnalysisRequest("public class Demo { public void test() {} }", "java");
        CodeAnalysisResponse response = service.analyzeAst(request);

        assertNotNull(response);
        assertEquals("java", response.language());
        assertTrue(response.classes().contains("Demo"));
        assertTrue(response.methods().contains("test"));
    }

    @Test
    @DisplayName("Should delegate auditSecurity to SecurityAuditEngine")
    void shouldDelegateAuditSecurity() {
        SecurityScanRequest request = new SecurityScanRequest("public class Clean {}", "Clean.java");
        SecurityScanResponse response = service.auditSecurity(request);

        assertNotNull(response);
        assertTrue(response.passed());
        assertEquals(0, response.totalIssues());
    }

    @Test
    @DisplayName("Should delegate synthesizeTests to TestSynthesizerEngine")
    void shouldDelegateSynthesizeTests() {
        TestGenerationRequest request = new TestGenerationRequest("Demo", List.of("test"), "JUNIT_5");
        TestGenerationResponse response = service.synthesizeTests(request);

        assertNotNull(response);
        assertEquals("DemoTest", response.suiteName());
        assertTrue(response.zeroMockEnforced());
        assertTrue(response.generatedTests().contains("shouldTest"));
    }
}
