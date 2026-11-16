package com.oenexa.domain.model.knowledge;

import com.oenexa.domain.model.knowledge.ContextRagDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextRagDomain Zero-Mock Unit Tests")
class ContextRagDomainTest {

    @Test
    @DisplayName("Should create ContextChunk and clamp relevance scores within [0.0, 1.0]")
    void shouldCreateContextChunk() {
        ContextChunk chunk = new ContextChunk(
                "chk-01",
                ContextScope.ADR_RECORD,
                "docs/adr/0001-architecture.md",
                "ADR 0001",
                "Clean Hexagonal Architecture chosen for modularity.",
                1.5 // Out of bounds -> clamps to 1.0
        );

        assertEquals("chk-01", chunk.chunkId());
        assertEquals(ContextScope.ADR_RECORD, chunk.scope());
        assertEquals(1.0, chunk.relevanceScore());
        assertEquals("docs/adr/0001-architecture.md", chunk.sourcePath());
    }

    @Test
    @DisplayName("Should assemble ExpandedContext with token estimations")
    void shouldAssembleExpandedContext() {
        ContextChunk chunk = new ContextChunk("chk-02", ContextScope.RUNBOOK, "runbooks/RB-001.md", "RB-001", "Service degradation steps.", 0.88);
        ExpandedContext context = new ExpandedContext(
                "How to recover from service degradation?",
                List.of(ContextScope.RUNBOOK),
                List.of(chunk),
                150,
                null
        );

        assertEquals("How to recover from service degradation?", context.query());
        assertEquals(1, context.queriedScopes().size());
        assertEquals(1, context.relevantChunks().size());
        assertEquals(150, context.estimatedTokens());
        assertNotNull(context.assembledAt());
    }

    @Test
    @DisplayName("Should assess verification outcome based on ungrounded claims")
    void shouldAssessVerificationOutcome() {
        RagVerificationResult verified = RagVerificationResult.verified(0.95, List.of("Hexagonal architecture is used."));
        assertTrue(verified.verified());
        assertEquals(0.95, verified.faithfulnessScore());
        assertTrue(verified.ungroundedClaims().isEmpty());

        RagVerificationResult unverified = RagVerificationResult.unverified(
                0.50,
                List.of("Uses Java 25."),
                List.of("OENEXA uses Ruby on Rails backend.") // Hallucination
        );
        assertFalse(unverified.verified());
        assertEquals(1, unverified.ungroundedClaims().size());
    }

    @Test
    @DisplayName("Should instantiate RagQueryResponse with immutable attributes")
    void shouldInstantiateRagQueryResponse() {
        RagVerificationResult verification = RagVerificationResult.verified(0.92, List.of("PostgreSQL 17 is used."));
        RagQueryResponse response = new RagQueryResponse(
                "q-101",
                "Which database does OENEXA use?",
                "OENEXA uses PostgreSQL 17 with pgvector.",
                List.of(new ContextChunk("c-01", ContextScope.ARCHITECTURE, "ARCHITECTURE.md", "Architecture", "PostgreSQL 17", 0.99)),
                verification,
                24,
                null
        );

        assertEquals("q-101", response.queryId());
        assertTrue(response.synthesizedAnswer().contains("PostgreSQL 17"));
        assertEquals(1, response.attributedSources().size());
        assertTrue(response.verification().verified());
        assertEquals(24, response.retrievalLatencyMs());
    }
}
