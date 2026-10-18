package com.oenexa.application;

import com.oenexa.application.openai.DeliberativeReasoningEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatResponseSynthesisEngine Zero-Mock Unit Tests")
class ChatResponseSynthesisEngineTest {

    @Test
    @DisplayName("Should test all constructor variants")
    void testConstructors() {
        ChatResponseSynthesisEngine defaultEngine = new ChatResponseSynthesisEngine();
        assertNotNull(defaultEngine);

        ChatResponseSynthesisEngine nullEngine = new ChatResponseSynthesisEngine(null);
        assertNotNull(nullEngine);

        ChatResponseSynthesisEngine customEngine = new ChatResponseSynthesisEngine(new DeliberativeReasoningEngine());
        assertNotNull(customEngine);
    }

    @Test
    @DisplayName("Should extract domain entities accurately with edge cases")
    void testExtractDomainEntity() {
        ChatResponseSynthesisEngine engine = new ChatResponseSynthesisEngine();

        // null and blank
        assertEquals("AutonomousSolution", engine.extractDomainEntity(null));
        assertEquals("AutonomousSolution", engine.extractDomainEntity(""));
        assertEquals("AutonomousSolution", engine.extractDomainEntity("   "));

        // Only stopwords or short characters
        assertEquals("AutonomousWorkingProcess", engine.extractDomainEntity("a in on at to"));
        assertEquals("AutonomousWorkingProcess", engine.extractDomainEntity("x y z"));

        // Domain extraction
        String entity = engine.extractDomainEntity("test for the doevek working process");
        assertEquals("DoevekWorkingProcess", entity);

        String entityOrder = engine.extractDomainEntity("create payment checkout flow");
        assertEquals("PaymentCheckoutFlow", entityOrder);
    }

    @Test
    @DisplayName("Should escape java strings safely")
    void testEscapeJavaString() {
        ChatResponseSynthesisEngine engine = new ChatResponseSynthesisEngine();
        assertEquals("", engine.escapeJavaString(null));
        assertEquals("Line with \\\"quotes\\\" and \\\\backslashes\\\\ and  newlines",
                engine.escapeJavaString("Line with \"quotes\" and \\backslashes\\ and \nnewlines\r"));
    }

    @Test
    @DisplayName("Should generate thought processes for all intents")
    void testGenerateThoughtProcess() {
        ChatResponseSynthesisEngine engine = new ChatResponseSynthesisEngine();
        String model = "oenexa-reasoning-v1";

        assertTrue(engine.generateThoughtProcess("ARCHITECTURE_DESIGN", "req", model).contains("Software Architecture"));
        assertTrue(engine.generateThoughtProcess("SECURITY_AUDIT", "req", model).contains("Security & SAST Audit"));
        assertTrue(engine.generateThoughtProcess("BUG_FIX_ANALYSIS", "req", model).contains("Defect Diagnosis"));
        assertTrue(engine.generateThoughtProcess("CODE_REFACTORING", "req", model).contains("Code Smell Refactoring"));
        assertTrue(engine.generateThoughtProcess("DEVOPS_PIPELINE", "req", model).contains("DevOps & Containerization"));
        assertTrue(engine.generateThoughtProcess("TESTING_SYNTHESIS", "req", model).contains("Test Suite Generation"));
        assertTrue(engine.generateThoughtProcess("CODE_GENERATION", "req", model).contains("Modern Code Generation"));
        assertTrue(engine.generateThoughtProcess("GENERAL_QUERY", "req", model).contains("General AI Software Engineering"));
    }

    @Test
    @DisplayName("Should generate engineering responses for all intents")
    void testGenerateEngineeringResponse() {
        ChatResponseSynthesisEngine engine = new ChatResponseSynthesisEngine();

        String arch = engine.generateEngineeringResponse("design auth system", "ARCHITECTURE_DESIGN");
        assertTrue(arch.contains("Hexagonal modular monolith"));
        assertTrue(arch.contains("AuthSystemAggregate"));

        String sec = engine.generateEngineeringResponse("audit token validator", "SECURITY_AUDIT");
        assertTrue(sec.contains("Security Agent activated"));
        assertTrue(sec.contains("TokenValidatorSecurityContext"));

        String bug = engine.generateEngineeringResponse("fix null pointer in user loader", "BUG_FIX_ANALYSIS");
        assertTrue(bug.contains("Bug Fix Agent activated"));
        assertTrue(bug.contains("UserLoaderPatch"));

        String refactor = engine.generateEngineeringResponse("refactor large order processor", "CODE_REFACTORING");
        assertTrue(refactor.contains("Refactoring Engine Activated"));
        assertTrue(refactor.contains("LargeOrderProcessorRefactored"));

        String devops = engine.generateEngineeringResponse("deploy billing service", "DEVOPS_PIPELINE");
        assertTrue(devops.contains("DevOps & CI/CD Engine Activated"));
        assertTrue(devops.contains("DeployBillingService"));
        assertTrue(devops.contains("apiVersion: apps/v1"));

        String testing = engine.generateEngineeringResponse("test for the doevek working process", "TESTING_SYNTHESIS");
        assertTrue(testing.contains("Test Synthesizer Activated"));
        assertTrue(testing.contains("DoevekWorkingProcess"));
        assertTrue(testing.contains("DoevekWorkingProcessTest"));

        String codegen = engine.generateEngineeringResponse("generate invoice calculator", "CODE_GENERATION");
        assertTrue(codegen.contains("Code Synthesizer Activated"));
        assertTrue(codegen.contains("InvoiceCalculatorService"));

        String def = engine.generateEngineeringResponse("general prompt for Doevek", "UNKNOWN_INTENT");
        assertTrue(def.contains("OENEXA AI Engineer: Request received"));
        assertTrue(def.contains("Doevek"));
    }

    @Test
    @DisplayName("Should suggest follow-ups for all intents")
    void testSuggestFollowUps() {
        ChatResponseSynthesisEngine engine = new ChatResponseSynthesisEngine();

        // 1-arg overload
        List<String> archFollowUps = engine.suggestFollowUps("ARCHITECTURE_DESIGN");
        assertFalse(archFollowUps.isEmpty());

        // 2-arg overloads
        assertTrue(engine.suggestFollowUps("ARCHITECTURE_DESIGN", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("SECURITY_AUDIT", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("BUG_FIX_ANALYSIS", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("CODE_REFACTORING", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("DEVOPS_PIPELINE", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("TESTING_SYNTHESIS", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("CODE_GENERATION", "auth flow").getFirst().contains("AuthFlow"));
        assertTrue(engine.suggestFollowUps("OTHER", "auth flow").getFirst().contains("AuthFlow"));
    }
}
