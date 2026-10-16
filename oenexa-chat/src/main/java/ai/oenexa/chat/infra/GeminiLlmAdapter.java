package ai.oenexa.chat.infra;

import ai.oenexa.chat.application.GeminiReasoningEngine;
import ai.oenexa.chat.domain.ChatMessage;
import ai.oenexa.chat.domain.LlmPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import reactor.core.publisher.Flux;

/**
 * Enterprise Google Gemini LLM Adapter.
 * Why: Real Gemini 2.5 API connectivity with autonomous offline reasoning fallback.
 */
public class GeminiLlmAdapter implements LlmPort {

    private static final String DEFAULT_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/";
    private static final Pattern TEXT_PATTERN = Pattern.compile("\"text\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    private final String apiKey;
    private final String model;
    private final String endpointUrl;
    private final HttpClient httpClient;
    private final GeminiReasoningEngine reasoningEngine;

    public GeminiLlmAdapter(GeminiReasoningEngine reasoningEngine) {
        this("", "gemini-2.5-flash", DEFAULT_ENDPOINT, null, reasoningEngine);
    }

    public GeminiLlmAdapter(String apiKey, String model, GeminiReasoningEngine reasoningEngine) {
        this(
                apiKey,
                (model != null && !model.isBlank()) ? model : "gemini-2.5-flash",
                DEFAULT_ENDPOINT,
                (apiKey != null && !apiKey.isBlank()) ? HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build() : null,
                reasoningEngine
        );
    }

    public GeminiLlmAdapter(
            String apiKey,
            String model,
            String endpointUrl,
            HttpClient httpClient,
            GeminiReasoningEngine reasoningEngine
    ) {
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null && !model.isBlank()) ? model.trim() : "gemini-2.5-flash";
        this.endpointUrl = (endpointUrl != null && !endpointUrl.isBlank()) ? endpointUrl : DEFAULT_ENDPOINT;
        this.httpClient = httpClient;
        this.reasoningEngine = (reasoningEngine != null) ? reasoningEngine : new GeminiReasoningEngine();
    }

    public String apiKey() {
        return apiKey;
    }

    public String model() {
        return model;
    }

    public String endpointUrl() {
        return endpointUrl;
    }

    @Override
    public Flux<String> stream(List<ChatMessage> prompt) {
        if (apiKey.isBlank() || httpClient == null || endpointUrl.startsWith("offline://")) {
            return reasoningEngine.stream(prompt, model);
        }

        try {
            String requestBody = buildGeminiJsonPayload(prompt);
            String targetUrl = endpointUrl + model + ":generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                List<String> chunks = extractTextFromGeminiResponse(response.body());
                if (!chunks.isEmpty()) {
                    return Flux.fromIterable(chunks);
                }
            }
        } catch (Exception e) {
            // Graceful fallback to offline Gemini reasoning engine
        }
        return reasoningEngine.stream(prompt, model);
    }

    public String buildGeminiJsonPayload(List<ChatMessage> prompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"contents\":[");
        boolean first = true;
        for (ChatMessage msg : prompt) {
            if (msg.role() == ChatMessage.Role.SYSTEM) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            first = false;
            String role = (msg.role() == ChatMessage.Role.USER) ? "user" : "model";
            sb.append("{\"role\":\"").append(role).append("\",\"parts\":[{\"text\":\"")
                    .append(escapeJson(msg.content())).append("\"}]}");
        }
        sb.append("]");

        // Extract system prompt if present
        for (ChatMessage msg : prompt) {
            if (msg.role() == ChatMessage.Role.SYSTEM) {
                sb.append(",\"system_instruction\":{\"parts\":[{\"text\":\"")
                        .append(escapeJson(msg.content())).append("\"}]}");
                break;
            }
        }

        sb.append(",\"generationConfig\":{\"temperature\":0.2,\"topP\":0.95}}");
        return sb.toString();
    }

    public static List<String> extractTextFromGeminiResponse(String json) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = TEXT_PATTERN.matcher(json);
        while (matcher.find()) {
            String unescaped = unescapeJson(matcher.group(1));
            parts.add(unescaped);
        }
        return parts;
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
