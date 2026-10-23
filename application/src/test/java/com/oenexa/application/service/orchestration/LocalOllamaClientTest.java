package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LocalOllamaClient Zero-Mock Unit Tests")
class LocalOllamaClientTest {

    @Test
    void shouldParseThinkingAndReplyCorrectly() {
        String fullText = "<think>\nAnalyzing the architecture constraints and calculating invariants.\n</think>\n\nHere is the production-ready solution.";
        var result = LocalOllamaClient.parseThinkingAndReply(fullText, "deepseek-r1:8b");

        assertNotNull(result);
        assertEquals("deepseek-r1:8b", result.modelUsed());
        assertEquals("Analyzing the architecture constraints and calculating invariants.", result.thoughtProcess());
        assertEquals("Here is the production-ready solution.", result.reply());
    }

    @Test
    void shouldParseReplyWithoutThinkingTag() {
        String fullText = "Direct answer without reasoning tag.";
        var result = LocalOllamaClient.parseThinkingAndReply(fullText, "qwen2.5-coder:7b");

        assertNotNull(result);
        assertNull(result.thoughtProcess());
        assertEquals("Direct answer without reasoning tag.", result.reply());
    }

    @Test
    void shouldEscapeAndUnescapeJsonStrings() {
        String original = "Quotes: \"hello\", newlines: \n, backslashes: \\";
        String escaped = LocalOllamaClient.escapeJson(original);
        assertTrue(escaped.contains("\\\"hello\\\""));
        assertTrue(escaped.contains("\\n"));

        String payload = LocalOllamaClient.buildPayload("prompt with \"quotes\"", "deepseek-r1:8b");
        assertTrue(payload.contains("\"model\":\"deepseek-r1:8b\""));
        assertTrue(payload.contains("\\\"quotes\\\""));

        String unescaped = LocalOllamaClient.unescapeJson("hello\\nworld\\\"quotes\\\"");
        assertEquals("hello\nworld\"quotes\"", unescaped);

        assertEquals("", LocalOllamaClient.escapeJson(null));
        assertEquals("", LocalOllamaClient.unescapeJson(null));
    }

    @Test
    void shouldExtractResponseTextFromJson() {
        String json = "{\"model\":\"deepseek-r1:8b\",\"response\":\"Hello World from local LLM\\nDone.\",\"done\":true}";
        String extracted = LocalOllamaClient.extractResponseText(json);
        assertEquals("Hello World from local LLM\nDone.", extracted);

        assertNull(LocalOllamaClient.extractResponseText("{}"));
    }

    @Test
    void shouldReturnNullForOfflineOrEmptyInput() {
        var client = new LocalOllamaClient("offline://local", null);
        assertNull(client.generate("hello", "deepseek-r1:8b"));
        assertNull(client.generate(null, "deepseek-r1:8b"));
        assertNull(client.generate("   ", "deepseek-r1:8b"));
    }

    @Test
    void shouldExecuteRealHttpCallAndReturnParsedResult() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/generate", exchange -> {
            String json = "{\"response\":\"<think>Thinking process.</think>Real answer.\"}";
            byte[] bytes = json.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            var client = new LocalOllamaClient("http://localhost:" + port + "/", httpClient);
            var result = client.generate("test prompt", "deepseek-r1:8b");

            assertNotNull(result);
            assertEquals("Thinking process.", result.thoughtProcess());
            assertEquals("Real answer.", result.reply());
            assertEquals("deepseek-r1:8b", result.modelUsed());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldDiscoverModelFromTagsEndpoint() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/tags", exchange -> {
            String json = "{\"models\":[{\"name\":\"deepseek-r1:8b\"}]}";
            byte[] bytes = json.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            var client = new LocalOllamaClient("http://localhost:" + port + "/", httpClient);

            String model = client.resolveEffectiveModel("auto");
            assertEquals("deepseek-r1:8b", model);

            String explicit = client.resolveEffectiveModel("qwen2.5-coder:14b");
            assertEquals("qwen2.5-coder:14b", explicit);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldResolveCloudAndLocalEndpointsCorrectly() {
        assertEquals("http://localhost:11434/api/", LocalOllamaClient.resolveDefaultEndpoint(null, null));
        assertEquals("http://localhost:11434/api/", LocalOllamaClient.resolveDefaultEndpoint("", ""));

        // Direct endpoint override
        assertEquals("https://cloud.oenexa.ai/api/", LocalOllamaClient.resolveDefaultEndpoint("https://cloud.oenexa.ai/api", null));
        assertEquals("https://cloud.oenexa.ai/api/", LocalOllamaClient.resolveDefaultEndpoint("https://cloud.oenexa.ai/api/", ""));

        // OLLAMA_HOST formatting
        assertEquals("http://ollama-service:11434/api/", LocalOllamaClient.resolveDefaultEndpoint(null, "ollama-service:11434"));
        assertEquals("https://remote-gpu.internal:11434/api/", LocalOllamaClient.resolveDefaultEndpoint("", "https://remote-gpu.internal:11434"));
    }
}

