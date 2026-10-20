package com.oenexa.application;

import com.oenexa.domain.OenexaMemoryEngineV2Domain.IntelligentPrefetchPrediction;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Intelligent Prefetch Engine for OENEXA Memory Engine V2.
 * Predictively anticipates and loads context files into RAM before an AI agent
 * or user requests them.
 *
 * Employs structural AST heuristic associations:
 * e.g., UserService.java -> UserRepository.java, UserController.java, JwtService.java.
 * Harnesses Java 25 Virtual Threads for zero-overhead background prefetching.
 */
@Service
public final class OenexaIntelligentPrefetchEngine {

    private final Map<String, List<String>> associationGraph;
    private final AtomicLong prefetchTriggers = new AtomicLong(0);
    private final AtomicLong prefetchHits = new AtomicLong(0);

    public OenexaIntelligentPrefetchEngine() {
        this.associationGraph = new ConcurrentHashMap<>(createDefaultAssociations());
    }

    private static Map<String, List<String>> createDefaultAssociations() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("UserService.java", List.of("UserRepository.java", "UserController.java", "JwtService.java"));
        map.put("UserController.java", List.of("UserService.java", "UserDto.java", "SecurityConfig.java"));
        map.put("OrderService.java", List.of("OrderRepository.java", "PaymentGatewayService.java", "InventoryClient.java"));
        map.put("ChatService.java", List.of("ChatRepository.java", "MemoryService.java", "AiModelRouter.java"));
        map.put("App.tsx", List.of("ChatTab.tsx", "PlatformNavigationBar.tsx", "scenarioResponseEngine.ts"));
        map.put("ChatTab.tsx", List.of("ChatMessageStream.tsx", "chatService.ts", "ChatHeaderBar.tsx"));
        return map;
    }

    public void registerAssociation(String sourceKey, List<String> targets) {
        if (sourceKey != null && targets != null) {
            associationGraph.put(sourceKey, new ArrayList<>(targets));
        }
    }

    public IntelligentPrefetchPrediction predictNextContext(String currentKey) {
        prefetchTriggers.incrementAndGet();
        if (currentKey == null || currentKey.isBlank()) {
            return new IntelligentPrefetchPrediction("unknown", List.of(), 0.0, "Empty key");
        }

        // Direct graph lookup
        List<String> known = associationGraph.get(currentKey);
        if (known != null && !known.isEmpty()) {
            return new IntelligentPrefetchPrediction(
                    currentKey,
                    known,
                    0.95,
                    "Direct architectural dependency in repository graph"
            );
        }

        // Heuristic derivation based on file naming patterns
        List<String> derived = deriveHeuristicTargets(currentKey);
        double confidence = derived.isEmpty() ? 0.20 : 0.75;
        String rationale = derived.isEmpty()
                ? "No direct pattern identified; generalized fallback"
                : "Triad association derived from naming conventions";

        return new IntelligentPrefetchPrediction(currentKey, derived, confidence, rationale);
    }

    public void triggerBackgroundPrefetch(String currentKey, Consumer<String> prefetchLoader) {
        IntelligentPrefetchPrediction prediction = predictNextContext(currentKey);
        if (prediction.predictedKeys().isEmpty() || prefetchLoader == null) {
            return;
        }

        // Java 25 Virtual Thread asynchronous dispatch
        Thread.ofVirtual().name("oenexa-prefetch-" + currentKey).start(() -> {
            for (String key : prediction.predictedKeys()) {
                try {
                    prefetchLoader.accept(key);
                } catch (Exception ignored) {
                    // Non-blocking background prefetch
                }
            }
        });
    }

    public void recordHit() {
        prefetchHits.incrementAndGet();
    }

    public long getPrefetchTriggers() {
        return prefetchTriggers.get();
    }

    public long getPrefetchHits() {
        return prefetchHits.get();
    }

    public double getPrefetchAccuracy() {
        long triggers = prefetchTriggers.get();
        return (triggers > 0) ? Math.round(((double) prefetchHits.get() / triggers) * 1000.0) / 10.0 : 0.0;
    }

    private List<String> deriveHeuristicTargets(String key) {
        List<String> targets = new ArrayList<>();
        if (key.endsWith("Service.java")) {
            String base = key.substring(0, key.length() - "Service.java".length());
            targets.add(base + "Repository.java");
            targets.add(base + "Controller.java");
        } else if (key.endsWith("Controller.java")) {
            String base = key.substring(0, key.length() - "Controller.java".length());
            targets.add(base + "Service.java");
            targets.add(base + "Dto.java");
        } else if (key.endsWith("Repository.java")) {
            String base = key.substring(0, key.length() - "Repository.java".length());
            targets.add(base + "Entity.java");
            targets.add(base + "Service.java");
        } else if (key.endsWith(".tsx")) {
            targets.add(key.replace(".tsx", ".test.tsx"));
        }
        return targets;
    }
}
