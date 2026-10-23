package com.oenexa.application.service.orchestration;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance, sovereign air-gapped local LLM client for Ollama and local OpenAI-compatible backends.
 * Single Responsibility: Communicates with local inference daemons with dynamic model discovery,
 * zero external network exposure, and structured chain-of-thought extraction.
 */
public class LocalOllamaClient {

    public record GenerationResult(String thoughtProcess, String reply, String modelUsed) {}

    private static final String DEFAULT_ENDPOINT = "http://localhost:11434/api/";
    private static final Pattern THINK_PATTERN = Pattern.compile("(?s)<think>(.*?)</think>");
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("\"response\":\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern MODEL_TAG_PATTERN = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");

    private final String endpointUrl;
    private final HttpClient httpClient;

    public LocalOllamaClient() {
        this(resolveDefaultEndpoint(), HttpClient.newBuilder().connectTimeout(Duration.ofMillis(800)).build());
    }

    public LocalOllamaClient(String endpointUrl, HttpClient httpClient) {
        this.endpointUrl = (endpointUrl != null && !endpointUrl.isBlank()) ? endpointUrl : resolveDefaultEndpoint();
        this.httpClient = httpClient;
    }

    public static String resolveDefaultEndpoint() {
        return resolveDefaultEndpoint(System.getenv("OENEXA_AI_ENDPOINT"), System.getenv("OLLAMA_HOST"));
    }

    public static String resolveDefaultEndpoint(String envEndpoint, String envHost) {
        if (envEndpoint != null && !envEndpoint.isBlank()) {
            String ep = envEndpoint.trim();
            return ep.endsWith("/") ? ep : ep + "/";
        }
        if (envHost != null && !envHost.isBlank()) {
            String host = envHost.trim();
            if (!host.startsWith("http://") && !host.startsWith("https://")) {
                host = "http://" + host;
            }
            if (!host.endsWith("/")) {
                host = host + "/";
            }
            if (!host.endsWith("api/")) {
                host = host + "api/";
            }
            return host;
        }
        return DEFAULT_ENDPOINT;
    }

    public GenerationResult generate(String prompt, String preferredModel) {
        if (httpClient == null || endpointUrl.startsWith("offline://") || prompt == null || prompt.isBlank()) {
            return null;
        }

        try {
            String effectiveModel = resolveEffectiveModel(preferredModel);
            String payload = buildPayload(prompt, effectiveModel);
            String targetUrl = endpointUrl.endsWith("/") ? endpointUrl + "generate" : endpointUrl + "/generate";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMillis(1200))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200 && resp.body() != null && !resp.body().isBlank()) {
                String fullText = extractResponseText(resp.body());
                if (fullText != null && !fullText.isBlank()) {
                    return parseThinkingAndReply(fullText, effectiveModel);
                }
            }
        } catch (Exception ignored) {
            // Graceful fallback to offline deterministic synthesis
        }
        return null;
    }

    public String resolveEffectiveModel(String preferredModel) {
        if (preferredModel != null && !preferredModel.isBlank() && !preferredModel.startsWith("oenexa-") && !preferredModel.equalsIgnoreCase("auto")) {
            return preferredModel;
        }
        if (httpClient != null && !endpointUrl.startsWith("offline://")) {
            try {
                String tagsUrl = endpointUrl.endsWith("/") ? endpointUrl + "tags" : endpointUrl + "/tags";
                HttpRequest tagsReq = HttpRequest.newBuilder()
                        .uri(URI.create(tagsUrl))
                        .timeout(Duration.ofMillis(800))
                        .GET()
                        .build();
                HttpResponse<String> tagsResp = httpClient.send(tagsReq, HttpResponse.BodyHandlers.ofString());
                if (tagsResp.statusCode() == 200 && tagsResp.body() != null) {
                    Matcher m = MODEL_TAG_PATTERN.matcher(tagsResp.body());
                    if (m.find()) {
                        return m.group(1);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return "deepseek-r1:8b";
    }

    public static GenerationResult parseThinkingAndReply(String fullText, String modelUsed) {
        Matcher thinkMatcher = THINK_PATTERN.matcher(fullText);
        if (thinkMatcher.find()) {
            String thought = thinkMatcher.group(1).trim();
            String reply = fullText.replace(thinkMatcher.group(0), "").trim();
            return new GenerationResult(
                    thought.isEmpty() ? null : thought,
                    reply.isEmpty() ? fullText.trim() : reply,
                    modelUsed
            );
        }
        return new GenerationResult(null, fullText.trim(), modelUsed);
    }

    public static String extractResponseText(String json) {
        Matcher matcher = RESPONSE_PATTERN.matcher(json);
        if (matcher.find()) {
            return unescapeJson(matcher.group(1));
        }
        return null;
    }

    public static String buildPayload(String prompt, String model) {
        return "{\"model\":\"" + escapeJson(model) + "\",\"prompt\":\"" + escapeJson(prompt) + "\",\"stream\":false}";
    }

    public static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String unescapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}

