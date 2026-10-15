package ai.oenexa.testing;

import ai.oenexa.testing.domain.CoverageReport;
import ai.oenexa.testing.domain.GeneratedTestSuite;
import ai.oenexa.testing.domain.TestCase;
import ai.oenexa.testing.domain.TestFramework;
import ai.oenexa.testing.domain.TestGenerationRequest;
import ai.oenexa.testing.domain.TestType;
import ai.oenexa.testing.service.DefaultTestingService;
import ai.oenexa.testing.web.TestingController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Testing Service & Controller Tests (Zero-Mock Policy)")
class TestingServiceTest {

    private DefaultTestingService testingService;
    private TestingController testingController;

    @BeforeEach
    void setUp() {
        testingService = new DefaultTestingService();
        testingController = new TestingController(testingService);
    }

    @Test
    @DisplayName("TestingModule metadata and initialization")
    void testTestingModuleMetadata() {
        TestingModule module = new TestingModule();
        assertEquals("testing", module.id());
        assertEquals("Automated Test Engineer", module.displayName());
        assertTrue(module.capabilities().contains("junit5"));
    }

    @Test
    @DisplayName("TestFramework and TestType enum invariants")
    void testEnumInvariants() {
        assertEquals(4, TestFramework.values().length);
        assertEquals(TestFramework.JUNIT5, TestFramework.valueOf("JUNIT5"));

        assertEquals(5, TestType.values().length);
        assertEquals(TestType.BDD_FEATURE, TestType.valueOf("BDD_FEATURE"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        TestCase tc = new TestCase("tc-1", "Test", null, null, true);
        assertEquals("", tc.givenWhenThen());
        assertEquals("", tc.testCode());
        assertTrue(tc.assertionsIncluded());

        assertThrows(NullPointerException.class, () -> new TestCase(null, "Test", "", "", true));
        assertThrows(NullPointerException.class, () -> new TestCase("tc-1", null, "", "", true));

        TestGenerationRequest req = new TestGenerationRequest("Service", null, null, null, null);
        assertEquals("", req.sourceCode());
        assertEquals("java", req.language());
        assertEquals(TestFramework.JUNIT5, req.framework());
        assertEquals(TestType.UNIT, req.type());

        assertThrows(NullPointerException.class, () -> new TestGenerationRequest(null, "", "java", null, null));

        GeneratedTestSuite suite = new GeneratedTestSuite("Suite", TestFramework.JUNIT5, TestType.UNIT, null, null, 85);
        assertEquals("", suite.testCode());
        assertTrue(suite.testCases().isEmpty());
        assertFalse(suite.hasTests());

        GeneratedTestSuite populatedSuite = new GeneratedTestSuite("Suite", TestFramework.JUNIT5, TestType.UNIT, "code", List.of(tc), 90);
        assertTrue(populatedSuite.hasTests());

        assertThrows(NullPointerException.class, () -> new GeneratedTestSuite(null, TestFramework.JUNIT5, TestType.UNIT, "", List.of(), 90));
        assertThrows(NullPointerException.class, () -> new GeneratedTestSuite("S", null, TestType.UNIT, "", List.of(), 90));
        assertThrows(NullPointerException.class, () -> new GeneratedTestSuite("S", TestFramework.JUNIT5, null, "", List.of(), 90));
    }

    @Test
    @DisplayName("CoverageReport calculation and threshold verification")
    void testCoverageReportCalculations() {
        CoverageReport passedReport = CoverageReport.of("oenexa-code", 100, 95, 20, 18, 80.0);
        assertEquals(95.0, passedReport.lineCoveragePercent());
        assertEquals(90.0, passedReport.branchCoveragePercent());
        assertTrue(passedReport.passed());

        CoverageReport failedReport = CoverageReport.of("oenexa-fail", 100, 50, 20, 10, 80.0);
        assertFalse(failedReport.passed());

        CoverageReport emptyReport = CoverageReport.of("empty", 0, 0, 0, 0, 80.0);
        assertEquals(100.0, emptyReport.lineCoveragePercent());
        assertTrue(emptyReport.passed());

        assertThrows(NullPointerException.class, () -> new CoverageReport(null, 10, 10, 5, 5, 100.0, 100.0, true));
    }

    @Test
    @DisplayName("Generate JUnit 5 test suite for Java class with methods")
    void testGenerateJUnit5SuiteWithMethods() {
        String javaSource = """
                package com.example;
                public class UserService {
                    public User findById(String id) { return null; }
                    public void saveUser(User u) {}
                }
                """;
        TestGenerationRequest req = new TestGenerationRequest("UserService", javaSource, "java", TestFramework.JUNIT5, TestType.UNIT);
        GeneratedTestSuite suite = testingService.generateTests(req);

        assertEquals("UserServiceTest", suite.suiteName());
        assertEquals(TestFramework.JUNIT5, suite.framework());
        assertTrue(suite.hasTests());
        assertTrue(suite.testCases().size() >= 2);
        assertTrue(suite.testCode().contains("@DisplayName"));
        assertTrue(suite.testCode().contains("testFindByIdExecution"));
        assertTrue(suite.testCode().contains("testSaveUserExecution"));
        assertTrue(suite.estimatedCoveragePercent() >= 80);
    }

    @Test
    @DisplayName("Generate JUnit 5 test suite with empty source uses fallback methods")
    void testGenerateJUnit5SuiteFallback() {
        TestGenerationRequest req = new TestGenerationRequest("OrderServiceTest", "", "java", TestFramework.JUNIT5, TestType.UNIT);
        GeneratedTestSuite suite = testingService.generateTests(req);

        assertEquals("OrderServiceTest", suite.suiteName());
        assertTrue(suite.testCode().contains("testExecuteExecution"));
        assertTrue(suite.testCode().contains("testValidateInvariantsExecution"));
    }

    @Test
    @DisplayName("Generate BDD Cucumber feature suite")
    void testGenerateBddSuite() {
        TestGenerationRequest req = new TestGenerationRequest("PaymentProcessor", "", "gherkin", TestFramework.CUCUMBER_BDD, TestType.BDD_FEATURE);
        GeneratedTestSuite suite = testingService.generateTests(req);

        assertEquals("PaymentProcessorFeature", suite.suiteName());
        assertEquals(TestFramework.CUCUMBER_BDD, suite.framework());
        assertTrue(suite.testCode().contains("Feature: PaymentProcessor"));
        assertTrue(suite.testCode().contains("Scenario: Successful state transition"));
        assertTrue(suite.hasTests());
    }

    @Test
    @DisplayName("Generate Vitest TypeScript test suite")
    void testGenerateVitestSuite() {
        TestGenerationRequest req = new TestGenerationRequest("UserBadge", "", "typescript", TestFramework.VITEST, TestType.UNIT);
        GeneratedTestSuite suite = testingService.generateTests(req);

        assertEquals("UserBadge.spec.ts", suite.suiteName());
        assertEquals(TestFramework.VITEST, suite.framework());
        assertTrue(suite.testCode().contains("import { describe, it, expect } from 'vitest'"));
        assertTrue(suite.testCode().contains("describe('UserBadge Component"));
    }

    @Test
    @DisplayName("Generate Pytest test suite")
    void testGeneratePytestSuite() {
        TestGenerationRequest req = new TestGenerationRequest("Analyzer", "", "python", TestFramework.PYTEST, TestType.UNIT);
        GeneratedTestSuite suite = testingService.generateTests(req);

        assertEquals("test_analyzer.py", suite.suiteName());
        assertEquals(TestFramework.PYTEST, suite.framework());
        assertTrue(suite.testCode().contains("import pytest"));
        assertTrue(suite.testCode().contains("def test_analyzer_execution():"));
    }

    @Test
    @DisplayName("TestingController endpoints return correct HTTP responses")
    void testTestingControllerEndpoints() {
        TestGenerationRequest req = new TestGenerationRequest("AccountService", "", "java", null, null);
        ResponseEntity<GeneratedTestSuite> genResp = testingController.generate(req);
        assertEquals(200, genResp.getStatusCode().value());
        assertNotNull(genResp.getBody());
        assertTrue(genResp.getBody().hasTests());

        ResponseEntity<CoverageReport> evalResp = testingController.evaluate(Map.of(
                "module", "oenexa-testing",
                "linesTotal", 120,
                "linesCovered", 120,
                "branchesTotal", 30,
                "branchesCovered", 30,
                "threshold", 85.0
        ));
        assertEquals(200, evalResp.getStatusCode().value());
        assertNotNull(evalResp.getBody());
        assertTrue(evalResp.getBody().passed());
        assertEquals(100.0, evalResp.getBody().lineCoveragePercent());
    }
}
