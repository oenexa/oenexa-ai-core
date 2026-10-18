package ai.oenexa.chat.infra;

import ai.oenexa.chat.application.OenexaReasoningEngine;
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

@DisplayName("OenexaLocalLlmAdapter Zero-Mock Unit Tests")
class OenexaLocalLlmAdapterTest {

    private final OenexaReasoningEngine engine = new OenexaReasoningEngine();

    @Test
    void testConstructorsAndGetters() {
        var defaultAdapter = new OenexaLocalLlmAdapter(engine);
        assertThat(defaultAdapter.apiKey()).isEmpty();
        assertThat(defaultAdapter.model()).isEqualTo("oenexa-reasoning-v1");
        assertThat(defaultAdapter.endpointUrl()).contains("11434");

        var customAdapter = new OenexaLocalLlmAdapter("my-key", "oenexa-neo-gpt-o3-max", engine);
        assertThat(customAdapter.apiKey()).isEqualTo("my-key");
        assertThat(customAdapter.model()).isEqualTo("oenexa-neo-gpt-o3-max");

        var fallbackModelAdapter = new OenexaLocalLlmAdapter("my-key", "   ", engine);
        assertThat(fallbackModelAdapter.model()).isEqualTo("oenexa-reasoning-v1");
    }

    @Test
    void shouldStreamOfflineWhenHttpClientIsNull() {
        var adapter = new OenexaLocalLlmAdapter("", "oenexa-reasoning-v1", "http://localhost:11434/api/", null, engine);
        var prompt = List.of(ChatMessage.of(Role.USER, "Explain system architecture"));

        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldStreamOfflineWhenEndpointIsOfflineScheme() {
        var client = HttpClient.newHttpClient();
        var adapter = new OenexaLocalLlmAdapter("test-key", "oenexa-reasoning-v1", "offline://local", client, engine);
        var prompt = List.of(ChatMessage.of(Role.USER, "Fix bug"));

        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldBuildLocalJsonPayloadWithEscapedCharacters() {
        var adapter = new OenexaLocalLlmAdapter(engine);
        var prompt = List.of(
                ChatMessage.of(Role.SYSTEM, "System instruction with \"quotes\" and \n newline and \t tabs"),
                ChatMessage.of(Role.USER, "User text with \\ backslash and \"quotes\""),
                ChatMessage.of(Role.ASSISTANT, "Assistant previous response")
        );

        String json = adapter.buildLocalJsonPayload(prompt);
        assertThat(json).contains("\"model\":\"oenexa-reasoning-v1\"");
        assertThat(json).contains("\\\"quotes\\\"");
        assertThat(json).contains("\\n");
        assertThat(json).contains("\\t");
    }

    @Test
    void shouldExtractTextFromLocalResponse() {
        String responseJson = """
                {
                  "model": "oenexa-reasoning-v1",
                  "response": "Local model response with \\"escaped quotes\\" and \\n newline",
                  "done": true
                }
                """;

        List<String> parts = OenexaLocalLlmAdapter.extractTextFromLocalResponse(responseJson);
        assertThat(parts).hasSize(1);
        assertThat(parts.get(0)).contains("Local model response with \"escaped quotes\"");
        assertThat(parts.get(0)).contains("\n newline");
    }

    @Test
    void shouldSendRealHttpRequestToLocalHttpServerAndParseResponse() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/generate", exchange -> {
            String responseJson = """
                    {
                      "response": "Real response chunk from local inference"
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
            String localEndpoint = "http://localhost:" + port + "/";
            var adapter = new OenexaLocalLlmAdapter("test-key", "oenexa-reasoning-v1", localEndpoint, client, engine);

            var prompt = List.of(ChatMessage.of(Role.USER, "Hello Local Model"));
            StepVerifier.create(adapter.stream(prompt))
                    .assertNext(chunk -> assertThat(chunk).isEqualTo("Real response chunk from local inference"))
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void shouldCatchNetworkExceptionAndFallback() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build();
        String unreachableEndpoint = "http://127.0.0.1:54321/api/";
        var adapter = new OenexaLocalLlmAdapter("test-key", "oenexa-reasoning-v1", unreachableEndpoint, client, engine);

        var prompt = List.of(ChatMessage.of(Role.USER, "Query during network failure"));
        StepVerifier.create(adapter.stream(prompt))
                .thenConsumeWhile(chunk -> !chunk.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldHandleNullConstructorParametersGracefully() {
        var adapter = new OenexaLocalLlmAdapter(null, null, null, null, null);
        assertThat(adapter.apiKey()).isEmpty();
        assertThat(adapter.model()).isEqualTo("oenexa-reasoning-v1");
        assertThat(adapter.endpointUrl()).isNotBlank();

        var blankAdapter = new OenexaLocalLlmAdapter("   ", "   ", "   ", null, engine);
        assertThat(blankAdapter.apiKey()).isEmpty();
        assertThat(blankAdapter.model()).isEqualTo("oenexa-reasoning-v1");
        assertThat(blankAdapter.endpointUrl()).isNotBlank();
    }

    @Test
    void shouldHandleEmptyResponseGracefully() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/generate", exchange -> {
            String emptyJson = "{}";
            byte[] bytes = emptyJson.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        });
        server.start();

        try {
            HttpClient client = HttpClient.newHttpClient();
            String localEndpoint = "http://localhost:" + port + "/";
            var adapter = new OenexaLocalLlmAdapter("test-key", "oenexa-reasoning-v1", localEndpoint, client, engine);

            var prompt = List.of(ChatMessage.of(Role.USER, "Hello empty response"));
            StepVerifier.create(adapter.stream(prompt))
                    .thenConsumeWhile(chunk -> !chunk.isEmpty())
                    .verifyComplete();
        } finally {
            server.stop(0);
        }
    }
}
