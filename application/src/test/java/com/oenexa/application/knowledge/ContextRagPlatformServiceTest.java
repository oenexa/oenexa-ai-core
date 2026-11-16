package com.oenexa.application.knowledge;

import com.oenexa.application.service.knowledge.RagService;
import com.oenexa.application.service.knowledge.RepositoryKnowledgeService;
import com.oenexa.domain.model.knowledge.ContextRagDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextRagPlatformService Zero-Mock Unit Tests")
class ContextRagPlatformServiceTest {

    private ContextRagPlatformService ragPlatformService;

    @BeforeEach
    void setUp() {
        RagService ragService = new RagService();
        RepositoryKnowledgeService repoKnowledgeService = new RepositoryKnowledgeService();
        ragPlatformService = new ContextRagPlatformService(ragService, repoKnowledgeService);
    }

    @Test
    @DisplayName("Should expand context across seeded ADR and Architecture scopes")
    void shouldExpandContextAcrossScopes() {
        ExpandedContext expanded = ragPlatformService.expandContext(
                "Clean Hexagonal Architecture Java 25",
                List.of(ContextScope.ADR_RECORD, ContextScope.ARCHITECTURE),
                2_000
        );

        assertNotNull(expanded);
        assertFalse(expanded.relevantChunks().isEmpty());
        assertTrue(expanded.estimatedTokens() > 0);
        assertTrue(expanded.relevantChunks().stream().anyMatch(c -> c.content().toLowerCase().contains("hexagonal")));
    }

    @Test
    @DisplayName("Should execute RAG query and synthesize verified answer with citations")
    void shouldExecuteRagQuery() {
        RagQueryResponse response = ragPlatformService.executeRagQuery(
                "Java 25 Virtual Threads concurrency",
                List.of(ContextScope.ARCHITECTURE)
        );

        assertNotNull(response);
        assertFalse(response.attributedSources().isEmpty());
        assertTrue(response.synthesizedAnswer().contains("Virtual Threads"));
        assertTrue(response.verification().verified());
        assertEquals(0.94, response.verification().faithfulnessScore());
    }

    @Test
    @DisplayName("Should handle queries without matching documents gracefully")
    void shouldHandleEmptyQueries() {
        RagQueryResponse response = ragPlatformService.executeRagQuery(
                "Unrelated ancient roman pottery query",
                List.of(ContextScope.RUNBOOK)
        );

        assertNotNull(response);
        assertTrue(response.attributedSources().isEmpty());
        assertFalse(response.verification().verified());
        assertTrue(response.synthesizedAnswer().contains("No enterprise documentation"));
    }

    @Test
    @DisplayName("Should index new custom context chunk and retrieve it")
    void shouldIndexCustomChunkAndRetrieve() {
        ContextChunk customChunk = new ContextChunk(
                "custom-rb-01",
                ContextScope.RUNBOOK,
                "runbooks/RB-002.md",
                "RB-002: Memory Pressure Runbook",
                "When L4 SSD cache exceeds 90 percent, trigger background eviction cycle.",
                1.0
        );

        ragPlatformService.indexContextChunk(customChunk);

        ExpandedContext expanded = ragPlatformService.expandContext(
                "Memory Pressure SSD eviction",
                List.of(ContextScope.RUNBOOK),
                1_000
        );

        assertNotNull(expanded);
        assertTrue(expanded.relevantChunks().stream().anyMatch(c -> c.chunkId().equals("custom-rb-01")));
    }
}
