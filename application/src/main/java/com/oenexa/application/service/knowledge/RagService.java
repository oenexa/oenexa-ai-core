package com.oenexa.application.service.knowledge;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.knowledge.RagDocument.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise RAG Indexing and Vector Retrieval Application Service.
 * Why: Semantic retrieval over codebases, documentation, and architecture notes.
 */
@Service
public class RagService {

    private final Map<String, Chunk> chunkStore = new ConcurrentHashMap<>();

    public IngestResult ingest(DocumentInput input) {
        Objects.requireNonNull(input, "DocumentInput must not be null");
        Objects.requireNonNull(input.content(), "Content must not be null");

        List<String> rawChunks = chunkContent(input.content(), 300);
        List<String> chunkIds = new ArrayList<>();

        for (int i = 0; i < rawChunks.size(); i++) {
            String chunkId = UUID.randomUUID().toString();
            Chunk chunk = new Chunk(chunkId, input.title(), rawChunks.get(i), i);
            chunkStore.put(chunkId, chunk);
            chunkIds.add(chunkId);
        }

        return new IngestResult(chunkIds.size(), chunkIds);
    }

    public List<SearchResult> search(String query, int limit) {
        if (query == null || query.isBlank() || chunkStore.isEmpty()) {
            return List.of();
        }

        String normalizedQuery = query.toLowerCase().trim();
        List<SearchResult> results = new ArrayList<>();

        for (Chunk chunk : chunkStore.values()) {
            double score = calculateRelevance(normalizedQuery, chunk.text().toLowerCase());
            if (score > 0.0) {
                String snippet = chunk.text().length() > 160
                        ? chunk.text().substring(0, 160) + "..."
                        : chunk.text();
                results.add(new SearchResult(chunk.id(), chunk.title(), snippet, score));
            }
        }

        results.sort((a, b) -> Double.compare(b.similarityScore(), a.similarityScore()));
        return results.stream().limit(limit).toList();
    }

    private List<String> chunkContent(String text, int maxWords) {
        String[] words = text.split("\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int count = 0;

        for (String word : words) {
            current.append(word).append(" ");
            count++;
            if (count >= maxWords) {
                chunks.add(current.toString().trim());
                current = new StringBuilder();
                count = 0;
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    private double calculateRelevance(String query, String text) {
        String[] tokens = query.split("\\s+");
        int matched = 0;
        for (String token : tokens) {
            if (text.contains(token)) {
                matched++;
            }
        }
        return (double) matched / tokens.length;
    }
}


