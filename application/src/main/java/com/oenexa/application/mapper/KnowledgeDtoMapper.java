package com.oenexa.application.mapper;

import com.oenexa.domain.model.knowledge.RagDocument.Chunk;
import com.oenexa.domain.model.knowledge.RagDocument.DocumentInput;
import com.oenexa.domain.model.knowledge.RagDocument.SearchResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Enterprise Mapper Layer for Knowledge & RAG Domain.
 * Maps between user document inputs, storage chunks, and semantic search results.
 */
@Component
public class KnowledgeDtoMapper {

    public List<Chunk> toChunks(DocumentInput input, int maxChunkSize) {
        if (input == null || input.content() == null) {
            return List.of();
        }
        int chunkSize = maxChunkSize >= 50 ? maxChunkSize : 500;
        String content = input.content();
        List<Chunk> chunks = new ArrayList<>();
        int index = 0;

        for (int i = 0; i < content.length(); i += chunkSize) {
            int end = Math.min(content.length(), i + chunkSize);
            String snippet = content.substring(i, end);
            chunks.add(new Chunk(
                    UUID.randomUUID().toString(),
                    input.title() != null ? input.title() : "Document",
                    snippet,
                    index++
            ));
        }
        return List.copyOf(chunks);
    }

    public SearchResult toSearchResult(Chunk chunk, double score) {
        if (chunk == null) {
            return null;
        }
        return new SearchResult(
                chunk.id(),
                chunk.title(),
                chunk.text(),
                score
        );
    }
}

