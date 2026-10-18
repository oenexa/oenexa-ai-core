package com.oenexa.infrastructure.openai;

import com.oenexa.application.openai.DeliberativeReasoningEngine;
import com.oenexa.domain.OpenAiDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalInferenceWorker Zero-Mock Unit Tests")
class LocalInferenceWorkerTest {

    private final LocalInferenceWorker worker = new LocalInferenceWorker();

    @Test
    @DisplayName("Should route models intelligently based on prompt contents")
    void testModelRouting() {
        assertEquals("gpt-4o", worker.routeModel("gpt-4o", "hi"));
        assertEquals("oenexa-neo-gpt-deepseek-arch", worker.routeModel("auto", "Explain hexagonal architecture and C4 diagram"));
        assertEquals("oenexa-neo-gpt-coder-32b", worker.routeModel("auto", "Write zero-mock unit test for controller"));
        assertEquals("oenexa-neo-gpt-o3-max", worker.routeModel("auto", "Summarize user stories"));
        assertEquals("oenexa-neo-gpt-coder-32b", worker.routeModel(null, null));
    }

    @Test
    @DisplayName("Should complete chat request with o3 deliberative reasoning trace")
    void testCompleteChat() {
        var req = OpenAiChatCompletionRequest.of("oenexa-neo-gpt-coder-32b", "Write reactive Spring service");
        var res = worker.completeChat(req);

        assertNotNull(res);
        assertTrue(res.id().startsWith("chatcmpl-oenexa-"));
        assertEquals("oenexa-neo-gpt-coder-32b", res.model());
        assertEquals(1, res.choices().size());

        var msg = res.choices().get(0).message();
        assertEquals("assistant", msg.role());
        assertNotNull(msg.content());
        assertNotNull(msg.reasoningContent());
        assertTrue(msg.reasoningContent().contains("Deliberative Reasoning"));
        assertTrue(res.usage().totalTokens() > 0);
        assertNotNull(worker.getNeuralEngine());
        assertNotNull(worker.getDeliberativeReasoningEngine());
    }

    @Test
    @DisplayName("Should generate embeddings correctly")
    void testGenerateEmbeddings() {
        var req = new OpenAiEmbeddingRequest(List.of("public record User()", "public interface Service()"), "text-embedding-3-small", "u1");
        var res = worker.generateEmbeddings(req);

        assertNotNull(res);
        assertEquals(2, res.data().size());
        assertEquals("embedding", res.data().get(0).object());
        assertEquals(worker.getNeuralEngine().getWeights().getHiddenDim(), res.data().get(0).embedding().size());
        assertTrue(res.usage().promptTokens() > 0);
    }

    @Test
    @DisplayName("Should transcribe audio directives correctly")
    void testTranscribeAudio() {
        byte[] audio = new byte[]{1, 2, 3, 4};
        var res = worker.transcribeAudio(audio, "directive.wav");

        assertNotNull(res);
        assertTrue(res.text().contains("Developer Audio Directive"));
        assertEquals("en", res.language());
        assertEquals(3.8, res.duration());

        var resDefault = worker.transcribeAudio(audio, null);
        assertNotNull(resDefault);
    }

    @Test
    @DisplayName("Should generate architecture diagrams and images")
    void testGenerateImage() {
        var res = worker.generateImage("C4 Architecture Diagram", "1024x1024");
        assertNotNull(res);
        assertEquals(1, res.data().size());
        assertNotNull(res.data().get(0).b64Json());
    }

    @Test
    @DisplayName("Should create OpenAI Responses API output with o3 deliberative reasoning")
    void testCreateResponse() {
        var req = new OpenAiResponsesRequest("oenexa-neo-gpt-o1", "Design reactive microservice", List.of(), "high");
        var res = worker.createResponse(req);

        assertNotNull(res);
        assertTrue(res.id().startsWith("resp-oenexa-"));
        assertEquals("response", res.object());
        assertEquals("completed", res.status());
        assertFalse(res.output().isEmpty());
        assertNotNull(res.output().get(0).content());
        assertTrue(res.usage().totalTokens() > 0);
    }

    @Test
    @DisplayName("Should validate null checks and properties")
    void testNullGuardsAndProperties() {
        assertThrows(NullPointerException.class, () -> worker.completeChat(null));
        assertThrows(NullPointerException.class, () -> worker.createResponse(null));
        assertThrows(NullPointerException.class, () -> worker.generateEmbeddings(null));
        assertThrows(NullPointerException.class, () -> worker.transcribeAudio(null, "f.wav"));
        assertThrows(NullPointerException.class, () -> worker.generateImage(null, "1024x1024"));
        assertThrows(NullPointerException.class, () -> new LocalInferenceWorker("http://localhost:11434", true, null));

        LocalInferenceWorker custom = new LocalInferenceWorker("http://custom:11434", false, new DeliberativeReasoningEngine());
        assertEquals("http://custom:11434", custom.getLocalEndpoint());
        assertFalse(custom.isAirGappedOnly());
    }
}
