package com.oenexa.api;

import com.oenexa.domain.OpenAiDomain.*;
import com.oenexa.infrastructure.openai.LocalInferenceWorker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAiProtocolController Zero-Mock Unit Tests")
class OpenAiProtocolControllerTest {

    private final LocalInferenceWorker worker = new LocalInferenceWorker();
    private final OpenAiProtocolController controller = new OpenAiProtocolController(worker);

    @Test
    @DisplayName("Should create chat completion in non-streaming mode")
    void testCreateChatCompletionNonStreaming() {
        var req = OpenAiChatCompletionRequest.of("qwen2.5-coder:32b", "Write a reactive service");
        Object result = controller.createChatCompletion(req);

        assertTrue(result instanceof ResponseEntity<?>);
        ResponseEntity<?> resp = (ResponseEntity<?>) result;
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertTrue(resp.getBody() instanceof OpenAiChatCompletionResponse);

        OpenAiChatCompletionResponse body = (OpenAiChatCompletionResponse) resp.getBody();
        assertEquals("qwen2.5-coder:32b", body.model());
        assertEquals(1, body.choices().size());
        assertNotNull(body.choices().get(0).message().content());
    }

    @Test
    @DisplayName("Should create chat completion in streaming mode returning SseEmitter")
    void testCreateChatCompletionStreaming() {
        var req = new OpenAiChatCompletionRequest(
                "qwen2.5-coder:32b",
                List.of(OpenAiChatMessage.user("Write a test")),
                0.7, 1.0, 1, true, List.of(), 500, null, null, Map.of(), "user-1",
                null, null, List.of(), "auto", "low"
        );
        Object result = controller.createChatCompletion(req);
        assertTrue(result instanceof SseEmitter);
    }

    @Test
    @DisplayName("Should return bad request for invalid chat completion requests")
    void testInvalidChatCompletionRequests() {
        Object res1 = controller.createChatCompletion(null);
        assertTrue(res1 instanceof ResponseEntity<?>);
        assertEquals(HttpStatus.BAD_REQUEST, ((ResponseEntity<?>) res1).getStatusCode());
    }

    @Test
    @DisplayName("Should list and get models")
    void testModels() {
        var listResp = controller.listModels();
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertNotNull(listResp.getBody());
        assertTrue(listResp.getBody().data().size() >= 4);

        var modelResp = controller.getModel("qwen2.5-coder:32b");
        assertEquals(HttpStatus.OK, modelResp.getStatusCode());
        assertEquals("qwen2.5-coder:32b", modelResp.getBody().id());

        assertEquals(HttpStatus.BAD_REQUEST, controller.getModel(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getModel("   ").getStatusCode());
    }

    @Test
    @DisplayName("Should create embeddings")
    void testEmbeddings() {
        var req = new OpenAiEmbeddingRequest(List.of("text input"), "text-embedding-3-small", "u1");
        var resp = controller.createEmbeddings(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(1, resp.getBody().data().size());

        assertEquals(HttpStatus.BAD_REQUEST, controller.createEmbeddings(null).getStatusCode());
    }

    @Test
    @DisplayName("Should transcribe audio and generate images")
    void testAudioAndImages() {
        var audioResp = controller.transcribeAudio(new byte[]{1, 2, 3});
        assertEquals(HttpStatus.OK, audioResp.getStatusCode());
        assertNotNull(audioResp.getBody().text());

        var imgResp = controller.generateImage(Map.of("prompt", "Architecture diagram", "size", "1024x1024"));
        assertEquals(HttpStatus.OK, imgResp.getStatusCode());
        assertEquals(1, imgResp.getBody().data().size());

        var imgDefaultResp = controller.generateImage(null);
        assertEquals(HttpStatus.OK, imgDefaultResp.getStatusCode());
    }

    @Test
    @DisplayName("Should list and get files")
    void testListAndGetFiles() {
        var filesResp = controller.listFiles();
        assertEquals(HttpStatus.OK, filesResp.getStatusCode());
        assertTrue(filesResp.getBody().data().size() >= 2);

        var fileResp = controller.getFile("file-oenexa-spec");
        assertEquals(HttpStatus.OK, fileResp.getStatusCode());
        assertEquals("file-oenexa-spec", fileResp.getBody().id());

        assertEquals(HttpStatus.BAD_REQUEST, controller.getFile(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.getFile("   ").getStatusCode());
    }

    @Test
    @DisplayName("Should create responses using OpenAI Responses API")
    void testCreateResponse() {
        var req = new OpenAiResponsesRequest("oenexa-neo-gpt-o1", "Synthesize domain model", List.of(), "high");
        var resp = controller.createResponse(req);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals("response", resp.getBody().object());
        assertFalse(resp.getBody().output().isEmpty());

        assertEquals(HttpStatus.BAD_REQUEST, controller.createResponse(null).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.createResponse(new OpenAiResponsesRequest("m", "", List.of(), "low")).getStatusCode());
    }

    @Test
    @DisplayName("Should throw NullPointerException for null constructor dependencies")
    void testNullConstructorGuard() {
        assertThrows(NullPointerException.class, () -> new OpenAiProtocolController(null));
    }
}
