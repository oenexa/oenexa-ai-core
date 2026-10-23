package com.oenexa.infrastructure.mapper;

import com.oenexa.domain.RagDocument.Chunk;
import com.oenexa.infrastructure.persistence.entity.ProjectKnowledgeChunkEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Enterprise Entity Mapper Layer for Knowledge Base Chunks.
 * Translates between ProjectKnowledgeChunkEntity JPA rows and domain Chunk records.
 */
@Component
public class KnowledgeChunkEntityMapper {

    public ProjectKnowledgeChunkEntity toEntity(Chunk chunk, String repositoryId, String filePath, String language) {
        if (chunk == null) {
            return null;
        }
        String id = chunk.id() != null ? chunk.id() : UUID.randomUUID().toString();
        String repo = (repositoryId != null && !repositoryId.isBlank()) ? repositoryId : "default-repo";
        String path = (filePath != null && !filePath.isBlank()) ? filePath : "docs/knowledge.md";
        String lang = (language != null && !language.isBlank()) ? language : "markdown";

        return new ProjectKnowledgeChunkEntity(
                id,
                repo,
                path,
                lang,
                "PARAGRAPH",
                chunk.title(),
                chunk.text(),
                "[]",
                chunk.chunkIndex()
        );
    }

    public Chunk toDomain(ProjectKnowledgeChunkEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Chunk(
                entity.getId(),
                entity.getSymbolName() != null ? entity.getSymbolName() : "Document Chunk",
                entity.getContent(),
                entity.getChunkIndex()
        );
    }
}
