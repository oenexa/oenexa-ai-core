package com.oenexa.application.engine;

import com.oenexa.domain.CodeEngineeringDomain.TestGenerationRequest;
import com.oenexa.domain.CodeEngineeringDomain.TestGenerationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TestSynthesizerEngine Zero-Mock Unit Tests")
class TestSynthesizerEngineTest {

    private TestSynthesizerEngine engine;

    @BeforeEach
    void setUp() {
        engine = new TestSynthesizerEngine();
    }

    @Test
    @DisplayName("Should reject null request with NullPointerException")
    void shouldRejectNullRequest() {
        assertThrows(NullPointerException.class, () -> engine.synthesize(null));
    }

    @Test
    @DisplayName("Should synthesize zero-mock JUnit 5 test suite for specified methods")
    void shouldSynthesizeForSpecifiedMethods() {
        TestGenerationRequest request = new TestGenerationRequest("Solution", List.of("listActiveUsers", "computeMetrics"), "JUNIT_5");
        TestGenerationResponse response = engine.synthesize(request);

        assertNotNull(response);
        assertEquals("SolutionTest", response.suiteName());
        assertEquals("JUNIT_5", response.framework());
        assertTrue(response.zeroMockEnforced());
        assertTrue(response.estimatedCoveragePercent() >= 80);
        assertEquals(2, response.generatedTests().size());
        assertTrue(response.generatedTests().contains("shouldListActiveUsers"));
        assertTrue(response.generatedTests().contains("shouldComputeMetrics"));

        String code = response.testCode();
        assertNotNull(code);
        assertEquals(code, response.code());
        assertTrue(code.contains("class SolutionTest"));
        assertTrue(code.contains("@DisplayName(\"Solution Zero-Mock Real Verification Suite\")"));
        assertTrue(code.contains("engineer@oenexa.ai"));
    }

    @Test
    @DisplayName("Should parse and discover methods from source code when method list is empty")
    void shouldDiscoverMethodsFromSourceCode() {
        String source = """
                public class OrderService {
                    public void createOrder(String orderId) {}
                    protected boolean validateOrder(String orderId) { return true; }
                }
                """;
        TestGenerationRequest request = new TestGenerationRequest("OrderService", List.of(), "JUNIT_5", source);
        TestGenerationResponse response = engine.synthesize(request);

        assertNotNull(response);
        assertEquals("OrderServiceTest", response.suiteName());
        assertTrue(response.generatedTests().contains("shouldCreateOrder"));
        assertTrue(response.generatedTests().contains("shouldValidateOrder"));
    }

    @Test
    @DisplayName("Should fallback to default methods when none provided or discovered")
    void shouldFallbackToDefaults() {
        TestGenerationRequest request = new TestGenerationRequest("EmptyService", List.of(), "JUNIT_5", "");
        TestGenerationResponse response = engine.synthesize(request);

        assertNotNull(response);
        assertTrue(response.generatedTests().contains("shouldListActiveUsers"));
        assertTrue(response.generatedTests().contains("shouldExecute"));
    }

    @Test
    @DisplayName("Should test TestGenerationRequest and Response constructors with nulls and defaults")
    void shouldHandleConstructors() {
        TestGenerationRequest req = new TestGenerationRequest(null, null, null, null);
        assertEquals("Solution", req.className());
        assertTrue(req.methods().isEmpty());
        assertEquals("JUNIT_5", req.framework());
        assertEquals("", req.sourceCode());

        TestGenerationResponse res = new TestGenerationResponse("SuiteTest", "JUNIT_5", null, null, null, 90, true);
        assertEquals("", res.code());
        assertEquals("", res.testCode());
        assertTrue(res.generatedTests().isEmpty());
    }
}
