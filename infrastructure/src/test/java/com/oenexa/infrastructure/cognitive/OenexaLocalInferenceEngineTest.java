package com.oenexa.infrastructure.cognitive;

import com.oenexa.application.cognitive.OenexaDeliberativeReasoningEngine;
import com.oenexa.domain.OenexaCognitiveDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaLocalInferenceEngine Zero-Mock Unit Tests")
class OenexaLocalInferenceEngineTest {

    private final OenexaLocalInferenceEngine engine = new OenexaLocalInferenceEngine();

    @Test
    @DisplayName("Should route models dynamically based on technical prompt keywords")
    void testDynamicModelRouting() {
        assertEquals("oenexa-neo-gpt-architect-70b", engine.routeModel("auto", "Design hexagonal C4 architecture boundaries"));
        assertEquals("oenexa-neo-gpt-coder-32b", engine.routeModel("auto", "Write zero-mock unit tests for UserRepository"));
        assertEquals("oenexa-neo-gpt-o3-max", engine.routeModel("auto", "Hello assistant"));
        assertEquals("custom-model", engine.routeModel("custom-model", "Any prompt"));
        assertEquals("oenexa-neo-gpt-coder-32b", engine.routeModel("auto", null));
    }

    @Test
    @DisplayName("Should execute cognitive chat completion with native neural engine")
    void testChatCompletion() {
        var req = OenexaCognitiveRequest.of("oenexa-neo-gpt-coder-32b", "Write reactive Spring service");
        var resp = engine.completeCognitiveChat(req);

        assertNotNull(resp);
        assertEquals("oenexa-neo-gpt-coder-32b", resp.model());
        assertEquals(1, resp.choices().size());
        assertNotNull(resp.choices().get(0).message().content());
        assertNotNull(resp.choices().get(0).message().reasoningContent());
        assertTrue(resp.usage().totalTokens() > 0);
    }

    @Test
    @DisplayName("Should generate dense vector embeddings from in-process neural pass")
    void testGenerateEmbeddings() {
        var req = new OenexaEmbeddingRequest(List.of("public record User()", "public interface Service()"), "oenexa-embedding-v3", "u1");
        var resp = engine.generateEmbeddings(req);

        assertNotNull(resp);
        assertEquals(2, resp.data().size());
        assertFalse(resp.data().get(0).embedding().isEmpty());
    }

    @Test
    @DisplayName("Should create structured response output")
    void testCreateResponse() {
        var req = new OenexaResponsesRequest("oenexa-neo-gpt-o3-max", "Design reactive microservice", List.of(), "high");
        var resp = engine.createResponse(req);

        assertNotNull(resp);
        assertEquals("response", resp.object());
        assertEquals("completed", resp.status());
        assertFalse(resp.output().isEmpty());
    }

    @Test
    @DisplayName("Should transcribe audio directives and synthesize diagrams")
    void testMultimodalInference() {
        var audioResp = engine.transcribeAudio("fake-audio-bytes".getBytes(), "directive.wav");
        assertNotNull(audioResp.text());
        assertTrue(audioResp.text().contains("Developer Voice Directive Transcribed"));

        var diagResp = engine.generateDiagram("Hexagonal Architecture", "1024x1024");
        assertNotNull(diagResp);
        assertEquals(1, diagResp.data().size());
        assertNotNull(diagResp.data().get(0).b64Json());
    }

    @Test
    @DisplayName("Should verify air-gapped configuration getters")
    void testConfigGetters() {
        assertTrue(engine.isAirGappedOnly());
        assertNotNull(engine.getLocalEndpoint());
        var custom = new OenexaLocalInferenceEngine("http://custom:9090", false, new OenexaDeliberativeReasoningEngine(), new com.oenexa.application.openai.neural.NeoGptAutonomousNeuralEngine());
        assertFalse(custom.isAirGappedOnly());
        assertEquals("http://custom:9090", custom.getLocalEndpoint());
    }
}
