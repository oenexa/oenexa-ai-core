package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Repository Understanding & Vector Knowledge Service.
 * Parses heterogeneous codebases (Java, Spring Boot, React, Angular, Vue,
 * Gradle, Maven, Docker, Kubernetes, Terraform, PostgreSQL, MySQL, AWS),
 * builds symbol indexes and knowledge maps, and enables semantic vector search.
 */
@Service
public class RepositoryKnowledgeService {

    private static final int VECTOR_DIMENSION = 64;

    // In-memory vector store of project knowledge chunks: repoId -> list of chunks
    private final Map<String, List<ProjectKnowledgeChunk>> chunkStore = new ConcurrentHashMap<>();

    // Aggregated knowledge maps per repository
    private final Map<String, RepositoryKnowledgeMap> knowledgeMaps = new ConcurrentHashMap<>();

    public RepositoryKnowledgeMap indexRepository(String repoId, Map<String, String> fileContents) {
        Objects.requireNonNull(repoId, "repoId must not be null");
        Objects.requireNonNull(fileContents, "fileContents must not be null");

        List<ProjectKnowledgeChunk> allChunks = new ArrayList<>();
        Set<String> detectedTech = new LinkedHashSet<>();
        List<String> fileTree = new ArrayList<>();
        Map<String, String> symbolIndex = new LinkedHashMap<>();
        Set<String> modules = new LinkedHashSet<>();

        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            String path = entry.getKey();
            String content = entry.getValue();
            fileTree.add(path);

            String stack = detectStack(path, content);
            detectedTech.add(stack);

            // Extract module from path
            if (path.contains("/") || path.contains("\\")) {
                String topDir = path.split("[/\\\\]")[0];
                modules.add(topDir);
            }

            // Extract symbols
            Map<String, String> symbols = extractSymbols(path, content);
            symbolIndex.putAll(symbols);

            // Chunk & embed
            List<ProjectKnowledgeChunk> chunks = chunkContent(repoId, path, stack, content, symbols);
            allChunks.addAll(chunks);
        }

        chunkStore.put(repoId, allChunks);

        String primary = detectedTech.isEmpty() ? "GENERIC" : detectedTech.iterator().next();
        RepositoryKnowledgeMap map = new RepositoryKnowledgeMap(
                repoId,
                primary,
                List.copyOf(detectedTech),
                List.copyOf(modules),
                List.copyOf(fileTree),
                Map.copyOf(symbolIndex),
                allChunks.size()
        );
        knowledgeMaps.put(repoId, map);
        return map;
    }

    public List<ProjectKnowledgeChunk> chunkContent(
            String repoId,
            String filePath,
            String language,
            String content,
            Map<String, String> symbols
    ) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        List<ProjectKnowledgeChunk> result = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        StringBuilder currentChunk = new StringBuilder();
        int chunkIdx = 0;

        for (int i = 0; i < lines.length; i++) {
            currentChunk.append(lines[i]).append("\n");
            if (currentChunk.length() >= 400 || i == lines.length - 1) {
                String chunkText = currentChunk.toString();
                List<Double> embedding = computeEmbedding(chunkText);
                result.add(new ProjectKnowledgeChunk(
                        UUID.randomUUID().toString(),
                        repoId,
                        filePath,
                        language,
                        chunkIdx++,
                        chunkText,
                        embedding,
                        symbols != null ? symbols : Map.of(),
                        Instant.now()
                ));
                currentChunk.setLength(0);
            }
        }
        return result;
    }

    public List<ProjectKnowledgeChunk> searchSimilarChunks(String repoId, String query, int topK) {
        List<ProjectKnowledgeChunk> chunks = chunkStore.getOrDefault(repoId, List.of());
        if (chunks.isEmpty() || query == null || query.isBlank()) {
            return List.of();
        }

        List<Double> queryVec = computeEmbedding(query);

        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk, cosineSimilarity(queryVec, chunk.embedding())))
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(Math.max(1, topK))
                .map(ScoredChunk::chunk)
                .toList();
    }

    public RepositoryKnowledgeMap getKnowledgeMap(String repoId) {
        return knowledgeMaps.computeIfAbsent(repoId, id ->
                new RepositoryKnowledgeMap(id, "UNINDEXED", List.of(), List.of(), List.of(), Map.of(), 0)
        );
    }

    public String detectStack(String path, String content) {
        String p = path.toLowerCase();
        String c = content != null ? content.toLowerCase() : "";

        if (p.endsWith(".java")) {
            if (c.contains("@springbootapplication") || c.contains("@restcontroller") || c.contains("@service")) {
                return "SPRING_BOOT";
            }
            return "JAVA";
        } else if (p.endsWith(".tsx") || p.endsWith(".jsx")) {
            return "REACT";
        } else if (p.endsWith(".component.ts") || c.contains("@component")) {
            return "ANGULAR";
        } else if (p.endsWith(".vue") || c.contains("<template>")) {
            return "VUE";
        } else if (p.endsWith("build.gradle") || p.endsWith("build.gradle.kts") || p.endsWith("settings.gradle.kts")) {
            return "GRADLE";
        } else if (p.endsWith("pom.xml")) {
            return "MAVEN";
        } else if (p.contains("dockerfile") || p.endsWith("docker-compose.yml")) {
            return "DOCKER";
        } else if (p.endsWith(".tf") || p.endsWith(".tfvars")) {
            return "TERRAFORM";
        } else if (p.endsWith(".yaml") || p.endsWith(".yml")) {
            if (c.contains("apiversion") && (c.contains("kind: deployment") || c.contains("kind: service"))) {
                return "KUBERNETES";
            }
            return "YAML";
        } else if (p.endsWith(".sql")) {
            if (c.contains("pg_") || c.contains("timestamp default") || c.contains("bigserial")) {
                return "POSTGRESQL";
            }
            return "SQL";
        }
        return "GENERIC";
    }

    public Map<String, String> extractSymbols(String path, String content) {
        Map<String, String> symbols = new LinkedHashMap<>();
        if (content == null || content.isBlank()) {
            return symbols;
        }

        String[] lines = content.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // Java class / interface / record
            if (trimmed.startsWith("public class ") || trimmed.startsWith("public interface ") || trimmed.startsWith("public record ")) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 3) {
                    symbols.put(parts[2].replaceAll("[^a-zA-Z0-9_]", ""), parts[1].toUpperCase());
                }
            }
            // Spring endpoints
            if (trimmed.startsWith("@GetMapping") || trimmed.startsWith("@PostMapping") || trimmed.startsWith("@PutMapping") || trimmed.startsWith("@DeleteMapping")) {
                symbols.put(trimmed, "API_ENDPOINT");
            }
            // React / TS exported functions
            if (trimmed.startsWith("export default function ")) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 4) {
                    symbols.put(parts[3].replaceAll("[^a-zA-Z0-9_]", ""), "REACT_COMPONENT");
                }
            } else if (trimmed.startsWith("export function ")) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 3) {
                    symbols.put(parts[2].replaceAll("[^a-zA-Z0-9_]", ""), "REACT_COMPONENT");
                }
            }
            // SQL Tables
            if (trimmed.toUpperCase().startsWith("CREATE TABLE")) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 3) {
                    symbols.put(parts[2].replaceAll("[^a-zA-Z0-9_]", ""), "DB_TABLE");
                }
            }
        }
        return symbols;
    }

    public List<Double> computeEmbedding(String text) {
        double[] vec = new double[VECTOR_DIMENSION];
        if (text == null || text.isBlank()) {
            List<Double> empty = new ArrayList<>(VECTOR_DIMENSION);
            for (int i = 0; i < VECTOR_DIMENSION; i++) empty.add(0.0);
            return empty;
        }

        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (!token.isBlank()) {
                int bucket = Math.abs(token.hashCode()) % VECTOR_DIMENSION;
                vec[bucket] += 1.0;
            }
        }

        // L2 normalize
        double sumSq = 0.0;
        for (double v : vec) {
            sumSq += v * v;
        }
        double norm = Math.sqrt(sumSq);
        List<Double> normalized = new ArrayList<>(VECTOR_DIMENSION);
        for (double v : vec) {
            normalized.add(norm > 0 ? (v / norm) : 0.0);
        }
        return normalized;
    }

    public double cosineSimilarity(List<Double> v1, List<Double> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size() || v1.isEmpty()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            double a = v1.get(i);
            double b = v2.get(i);
            dot += a * b;
            normA += a * a;
            normB += b * b;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredChunk(ProjectKnowledgeChunk chunk, double score) {}
}
