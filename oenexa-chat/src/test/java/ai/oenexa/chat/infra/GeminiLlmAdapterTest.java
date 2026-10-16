package ai.oenexa.chat.infra;

import ai.oenexa.chat.application.GeminiReasoningEngine;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.ChatMessage.Role;
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GeminiLlmAdapter Zero-Mock Unit Tests")
class GeminiLlmAdapterTest {

    private final GeminiReasoningEngine engine = new GeminiReasoningEngine();

    @Test
    void testConstructorsAndGetters() {
        var defaultAdapter = new GeminiLlmAdapter(engine);
        assertThat(defaultAdapter.apiKey()).isEmpty();
        assertThat(defaultAdapter.model()).isEqualTo("gemini-2.5-flash");
        assertThat(defaultAdapter.endpointUrl()).contains("generativelanguage.googleapis.com");

        var customAdapter = new GeminiLlmAdapter("my-key", "gemini-2.5-pro", engine);
        assertThat(customAdapter.apiKey()).isEqualTo("my-key");
        assertThat(customAdapter.model()).isEqualTo("gemini-2.5-pro");

        var fallbackModelAdapter = new GeminiLlmAdapter("my-key", "   ", engine);
        assertThat(fallbackModelAdapter.model()).isEqualTo("gemini-2.5-flash");
    }

    @Test
    void shouldStreamOfflineWhenApiKeyIsBlank() {
        var adapter = new GeminiLlmAdapter("", "gemini-2.5-flash", engine);
        var prompt = List.of(ChatMessage.of(Role.USER, "Explain system architecture"));

        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldStreamOfflineWhenEndpointIsOfflineScheme() {
        var client = HttpClient.newHttpClient();
        var adapter = new GeminiLlmAdapter("test-key", "gemini-2.5-flash", "offline://local", client, engine);
        var prompt = List.of(ChatMessage.of(Role.USER, "Fix bug"));

        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldBuildGeminiJsonPayloadWithEscapedCharacters() {
        var adapter = new GeminiLlmAdapter(engine);
        var prompt = List.of(
                ChatMessage.of(Role.SYSTEM, "System instruction with \"quotes\" and \n newline and \t tabs"),
                ChatMessage.of(Role.USER, "User text with \\ backslash and \"quotes\""),
                ChatMessage.of(Role.ASSISTANT, "Assistant previous response")
        );

        String json = adapter.buildGeminiJsonPayload(prompt);
        assertThat(json).contains("\"role\":\"user\"");
        assertThat(json).contains("\"role\":\"model\"");
        assertThat(json).contains("\"system_instruction\"");
        assertThat(json).contains("\\\"quotes\\\"");
        assertThat(json).contains("\\n");
        assertThat(json).contains("\\t");
    }

    @Test
    void shouldExtractTextFromGeminiResponse() {
        String geminiJson = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Gemini response with \\"escaped quotes\\" and \\n newline"
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """;

        List<String> parts = GeminiLlmAdapter.extractTextFromGeminiResponse(geminiJson);
        assertThat(parts).hasSize(1);
        assertThat(parts.get(0)).contains("Gemini response with \"escaped quotes\"");
        assertThat(parts.get(0)).contains("\n newline");
    }

    @Test
    void shouldSendRealHttpRequestToLocalHttpServerAndParseResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/models/gemini-2.5-flash:generateContent", exchange -> {
            String responseJson = """
                    {
                      "candidates": [
                        {
                          "content": {
                            "parts": [
                              { "text": "Real response chunk from Gemini API" }
                            ]
                          }
                        }
                      ]
                    }
                    """;
            byte[] bytes = responseJson.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
            String localEndpoint = "http://localhost:" + port + "/models/";
            var adapter = new GeminiLlmAdapter("test-gemini-key", "gemini-2.5-flash", localEndpoint, client, engine);

            var prompt = List.of(ChatMessage.of(Role.USER, "Hello Gemini"));
            StepVerifier.create(adapter.stream(prompt))
                    .assertNext(chunk -> assertThat(chunk).isEqualTo("Real response chunk from Gemini API"))
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldCatchNetworkExceptionAndFallback() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build();
        // Unreachable local address to trigger ConnectException inside try block
        String unreachableEndpoint = "http://127.0.0.1:54321/models/";
        var adapter = new GeminiLlmAdapter("test-key", "gemini-2.5-flash", unreachableEndpoint, client, engine);

        var prompt = List.of(ChatMessage.of(Role.USER, "Query during network failure"));
        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldHandleNullConstructorParametersGracefully() {
        var adapter = new GeminiLlmAdapter(null, null, null, null, null);
        assertThat(adapter.apiKey()).isEmpty();
        assertThat(adapter.model()).isEqualTo("gemini-2.5-flash");
        assertThat(adapter.endpointUrl()).isNotBlank();

        // Test with blank strings
        var blankAdapter = new GeminiLlmAdapter("   ", "   ", "   ", null, engine);
        assertThat(blankAdapter.apiKey()).isEmpty();
        assertThat(blankAdapter.model()).isEqualTo("gemini-2.5-flash");
        assertThat(blankAdapter.endpointUrl()).isNotBlank();
    }

    @Test
    void shouldHandleEmptyResponseGracefully() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/models/gemini-2.5-flash:generateContent", exchange -> {
            String emptyCandidatesJson = "{\"candidates\": []}";
            byte[] bytes = emptyCandidatesJson.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String localEndpoint = "http://localhost:" + port + "/models/";
            var adapter = new GeminiLlmAdapter("test-key", "gemini-2.5-flash", localEndpoint, client, engine);

            var prompt = List.of(ChatMessage.of(Role.USER, "Hello empty response"));
            StepVerifier.create(adapter.stream(prompt))
                    .thenConsumeWhile(chunk -> !chunk.isEmpty())
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }
}
