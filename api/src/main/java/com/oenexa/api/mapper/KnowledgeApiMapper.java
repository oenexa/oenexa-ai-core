package com.oenexa.api.mapper;

import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentChunkDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.DocumentIngestRequestDto;
import com.oenexa.api.dto.knowledge.KnowledgeDtos.KnowledgeSearchResultDto;
import com.oenexa.domain.RagDocument.Chunk;
import com.oenexa.domain.RagDocument.DocumentInput;
import com.oenexa.domain.RagDocument.SearchResult;
import org.springframework.stereotype.Component;

/**
 * Enterprise API Mapper for Knowledge & RAG DTOs.
 */
@Component
public class KnowledgeApiMapper {

    public DocumentInput toDomainInput(DocumentIngestRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return new DocumentInput(dto.title(), dto.content(), dto.repositoryId());
    }

    public DocumentChunkDto toChunkDto(Chunk chunk) {
        if (chunk == null) {
            return null;
        }
        return new DocumentChunkDto(chunk.id(), chunk.title(), chunk.text(), chunk.chunkIndex());
    }

    public KnowledgeSearchResultDto toSearchDto(SearchResult result) {
        if (result == null) {
            return null;
        }
        return new KnowledgeSearchResultDto(result.chunkId(), result.title(), result.snippet(), result.similarityScore());
    }
}
