package ai.oenexa.chat.infra;

import ai.oenexa.chat.application.OenexaReasoningEngine;
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
 * Enterprise OENEXA Local-First LLM Adapter.
 * Single Responsibility: High-throughput local model execution with autonomous offline reasoning fallback.
 */
public class OenexaLocalLlmAdapter implements LlmPort {

    private static final String DEFAULT_ENDPOINT = "http://localhost:11434/api/";
    private static final Pattern TEXT_PATTERN = Pattern.compile("\"response\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"|\"text\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern TAGS_NAME_PATTERN = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");

    private final String apiKey;
    private final String model;
    private final String endpointUrl;
    private final HttpClient httpClient;
    private final OenexaReasoningEngine reasoningEngine;

    public OenexaLocalLlmAdapter(OenexaReasoningEngine reasoningEngine) {
        this("", "oenexa-reasoning-v1", DEFAULT_ENDPOINT, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(), reasoningEngine);
    }

    public OenexaLocalLlmAdapter(String apiKey, String model, OenexaReasoningEngine reasoningEngine) {
        this(
                apiKey,
                (model != null && !model.isBlank()) ? model : "oenexa-reasoning-v1",
                DEFAULT_ENDPOINT,
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build(),
                reasoningEngine
        );
    }

    public OenexaLocalLlmAdapter(
            String apiKey,
            String model,
            String endpointUrl,
            HttpClient httpClient,
            OenexaReasoningEngine reasoningEngine
    ) {
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
        this.model = (model != null && !model.isBlank()) ? model.trim() : "oenexa-reasoning-v1";
        this.endpointUrl = (endpointUrl != null && !endpointUrl.isBlank()) ? endpointUrl : DEFAULT_ENDPOINT;
        this.httpClient = httpClient;
        this.reasoningEngine = (reasoningEngine != null) ? reasoningEngine : new OenexaReasoningEngine();
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
        if (httpClient == null || endpointUrl.startsWith("offline://")) {
            return reasoningEngine.stream(prompt, model);
        }

        try {
            String targetModel = discoverOrResolveModel();
            String requestBody = buildLocalJsonPayload(prompt, targetModel);
            String targetUrl = endpointUrl.endsWith("/") ? endpointUrl + "generate" : endpointUrl + "/generate";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null && !response.body().isBlank()) {
                List<String> chunks = extractTextFromLocalResponse(response.body());
                if (!chunks.isEmpty()) {
                    return Flux.fromIterable(chunks);
                }
            }
        } catch (Exception e) {
            // Graceful fallback to offline OENEXA sovereign reasoning engine
        }
        return reasoningEngine.stream(prompt, model);
    }

    public String discoverOrResolveModel() {
        if (model != null && !model.isBlank() && !model.startsWith("oenexa-")) {
            return model;
        }
        if (httpClient != null && !endpointUrl.startsWith("offline://")) {
            try {
                String tagsUrl = endpointUrl.endsWith("/") ? endpointUrl + "tags" : endpointUrl + "/tags";
                HttpRequest tagsRequest = HttpRequest.newBuilder()
                        .uri(URI.create(tagsUrl))
                        .timeout(Duration.ofSeconds(2))
                        .GET()
                        .build();
                HttpResponse<String> tagsResponse = httpClient.send(tagsRequest, HttpResponse.BodyHandlers.ofString());
                if (tagsResponse.statusCode() == 200 && tagsResponse.body() != null) {
                    Matcher matcher = TAGS_NAME_PATTERN.matcher(tagsResponse.body());
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return model;
    }

    public String buildLocalJsonPayload(List<ChatMessage> prompt) {
        return buildLocalJsonPayload(prompt, model);
    }

    public String buildLocalJsonPayload(List<ChatMessage> prompt, String targetModel) {
        StringBuilder sb = new StringBuilder();
        String effective = (targetModel != null && !targetModel.isBlank()) ? targetModel : model;
        sb.append("{\"model\":\"").append(effective).append("\",\"prompt\":\"");
        for (ChatMessage msg : prompt) {
            sb.append("[").append(msg.role()).append("] ")
              .append(escapeJson(msg.content())).append("\\n");
        }
        sb.append("\",\"stream\":false}");
        return sb.toString();
    }

    public static List<String> extractTextFromLocalResponse(String json) {
        List<String> parts = new ArrayList<>();
        Matcher matcher = TEXT_PATTERN.matcher(json);
        while (matcher.find()) {
            String val = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (val != null) {
                parts.add(unescapeJson(val));
            }
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
