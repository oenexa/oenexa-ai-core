package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Configuration Layer for Knowledge & RAG Domain.
 * Configures chunking size, overlap tokens, and similarity score cutoffs.
 */
@Configuration
public class KnowledgeDomainConfig {

    public record KnowledgeProperties(
            int maxChunkSizeChars,
            int chunkOverlapChars,
            double minimumSimilarityThreshold,
            int topKResults
    ) {
        public KnowledgeProperties() {
            this(500, 50, 0.70, 5);
        }
    }

    @Bean
    public KnowledgeProperties knowledgeProperties() {
        return new KnowledgeProperties();
    }
}
