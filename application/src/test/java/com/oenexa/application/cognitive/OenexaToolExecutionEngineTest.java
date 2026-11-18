package com.oenexa.application.cognitive;

import com.oenexa.domain.model.cognitive.OenexaCognitiveDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaToolExecutionEngine Zero-Mock Unit Tests")
class OenexaToolExecutionEngineTest {

    private OenexaToolExecutionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new OenexaToolExecutionEngine();
    }

    @Test
    @DisplayName("Should list all registered enterprise cognitive tools")
    void shouldListRegisteredTools() {
        List<OenexaToolDefinition> tools = engine.getRegisteredTools();
        assertNotNull(tools);
        assertEquals(7, tools.size());
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("ast_analyzer")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("sast_security_scanner")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("zero_mock_test_synthesizer")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("docker_pipeline_builder")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("workspace_patch_engine")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("post_quantum_blockchain_oracle")));
        assertTrue(tools.stream().anyMatch(t -> t.function().name().equals("orderbook_anomaly_detector")));
    }

    @Test
    @DisplayName("Should execute ast_analyzer tool successfully")
    void shouldExecuteAstAnalyzer() {
        OenexaToolCall call = new OenexaToolCall("call-1", "function", new OenexaFunctionCall("ast_analyzer", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals("ast_analyzer", result.functionName());
        assertTrue(result.executionOutput().contains("AST Parsed"));
    }

    @Test
    @DisplayName("Should execute sast_security_scanner tool successfully")
    void shouldExecuteSastSecurityScanner() {
        OenexaToolCall call = new OenexaToolCall("call-2", "function", new OenexaFunctionCall("sast_security_scanner", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertEquals("sast_security_scanner", result.functionName());
        assertTrue(result.executionOutput().contains("SAST Audit completed"));
    }

    @Test
    @DisplayName("Should execute zero_mock_test_synthesizer tool and produce code artifact")
    void shouldExecuteZeroMockTestSynthesizer() {
        OenexaToolCall call = new OenexaToolCall("call-3", "function", new OenexaFunctionCall("zero_mock_test_synthesizer", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.generatedArtifact().isPresent());
        assertTrue(result.generatedArtifact().get().codeContent().contains("@Test"));
    }

    @Test
    @DisplayName("Should execute docker_pipeline_builder tool and produce Dockerfile")
    void shouldExecuteDockerPipelineBuilder() {
        OenexaToolCall call = new OenexaToolCall("call-4", "function", new OenexaFunctionCall("docker_pipeline_builder", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.generatedArtifact().isPresent());
        assertEquals("Dockerfile", result.generatedArtifact().get().title());
    }

    @Test
    @DisplayName("Should execute workspace_patch_engine tool successfully")
    void shouldExecuteWorkspacePatchEngine() {
        OenexaToolCall call = new OenexaToolCall("call-5", "function", new OenexaFunctionCall("workspace_patch_engine", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.generatedArtifact().isPresent());
        assertTrue(result.generatedArtifact().get().title().endsWith(".patch"));
    }

    @Test
    @DisplayName("Should execute post_quantum_blockchain_oracle tool and produce L1 telemetry artifact")
    void shouldExecutePostQuantumBlockchainOracle() {
        OenexaToolCall call = new OenexaToolCall("call-6", "function", new OenexaFunctionCall("post_quantum_blockchain_oracle", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.generatedArtifact().isPresent());
        assertTrue(result.generatedArtifact().get().codeContent().contains("Dilithium-5"));
        assertTrue(result.executionOutput().contains("Post-Quantum"));
    }

    @Test
    @DisplayName("Should execute orderbook_anomaly_detector tool and produce audit artifact")
    void shouldExecuteOrderbookAnomalyDetector() {
        OenexaToolCall call = new OenexaToolCall("call-7", "function", new OenexaFunctionCall("orderbook_anomaly_detector", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertTrue(result.success());
        assertTrue(result.generatedArtifact().isPresent());
        assertTrue(result.generatedArtifact().get().codeContent().contains("spoofingProbability"));
        assertTrue(result.executionOutput().contains("Orderbook audit complete"));
    }

    @Test
    @DisplayName("Should handle unknown tool gracefully")
    void shouldHandleUnknownTool() {
        OenexaToolCall call = new OenexaToolCall("call-unk", "function", new OenexaFunctionCall("quantum_crypto", "{}"));
        var result = engine.executeTool(call);

        assertNotNull(result);
        assertFalse(result.success());
        assertTrue(result.executionOutput().contains("Unknown tool"));
    }

    @Test
    @DisplayName("Should reject null tool call")
    void shouldRejectNullToolCall() {
        assertThrows(NullPointerException.class, () -> engine.executeTool(null));
    }
}

