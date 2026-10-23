package com.oenexa.application.service.knowledge;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import com.oenexa.domain.model.knowledge.EnterpriseKnowledgeDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseKnowledgeService Zero-Mock Unit Tests")
class EnterpriseKnowledgeServiceTest {

    private EnterpriseKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseKnowledgeService();
    }

    @Test
    @DisplayName("Should create and list knowledge bases partitioned by tenant")
    void shouldCreateAndListKnowledgeBases() {
        KnowledgeBase kb1 = service.createKnowledgeBase("tenant-omega", "Alpha Architecture", "Core system designs");
        KnowledgeBase kb2 = service.createKnowledgeBase("tenant-beta", "Beta Finance", "FinOps models");

        assertNotNull(kb1);
        assertNotNull(kb2);
        assertEquals("Alpha Architecture", kb1.name());

        List<KnowledgeBase> omegaBases = service.listKnowledgeBases("tenant-omega");
        assertEquals(1, omegaBases.size());
        assertEquals("Alpha Architecture", omegaBases.get(0).name());

        List<KnowledgeBase> allBases = service.listKnowledgeBases(null);
        assertEquals(2, allBases.size());
    }

    @Test
    @DisplayName("Should upload document, chunk, embed, and update statistics")
    void shouldUploadAndIndexDocument() {
        KnowledgeBase kb = service.createKnowledgeBase("tenant-omega", "Engineering Docs", "Specs");
        String docContent = """
                # Microservice Communication Guidelines

                All services must communicate via reactive gRPC or REST over HTTP/2.
                Direct database access across boundary lines is strictly prohibited.

                # Resilience & Circuit Breakers

                Resilience4j must be configured with a 50% failure rate threshold and a 5s wait duration.
                """;

        KnowledgeDocument doc = service.uploadDocument(
                kb.kbId(),
                "Architecture_Spec.md",
                docContent.getBytes(StandardCharsets.UTF_8),
                DocumentFormat.MARKDOWN
        );

        assertNotNull(doc);
        assertEquals("Architecture_Spec.md", doc.title());
        assertEquals(DocumentStatus.INDEXED, doc.status());
        assertTrue(doc.chunkCount() >= 2);

        KnowledgeBase updatedKb = service.getKnowledgeBase(kb.kbId()).orElseThrow();
        assertEquals(1, updatedKb.documentCount());
        assertTrue(updatedKb.totalTokens() > 0);
    }

    @Test
    @DisplayName("Should crawl website and enrich knowledge base with graph relationships")
    void shouldCrawlWebsiteAndEnrichGraph() {
        KnowledgeBase kb = service.createKnowledgeBase("tenant-omega", "Cloud Docs", "Web sources");
        KnowledgeDocument doc = service.crawlWebsite(kb.kbId(), "https://docs.oenexa.ai/architecture", 2, true);

        assertNotNull(doc);
        assertEquals(KnowledgeSourceType.WEBSITE_CRAWL, doc.sourceType());
        assertTrue(doc.title().contains("docs.oenexa.ai"));

        KnowledgeGraph graph = service.getKnowledgeGraph(kb.kbId());
        assertNotNull(graph);
        assertFalse(graph.nodes().isEmpty());
        assertFalse(graph.edges().isEmpty());

        // Verify graph contains concepts
        boolean hasSpringConcept = graph.nodes().stream().anyMatch(n -> n.label().equalsIgnoreCase("Spring Boot"));
        boolean hasHexagonalConcept = graph.nodes().stream().anyMatch(n -> n.label().equalsIgnoreCase("Hexagonal"));
        assertTrue(hasSpringConcept);
        assertTrue(hasHexagonalConcept);
    }

    @Test
    @DisplayName("Should execute hybrid semantic search, generate citations, and synthesize answer")
    void shouldExecuteHybridSemanticSearch() {
        KnowledgeBase kb = service.createKnowledgeBase("tenant-omega", "Security Docs", "Auth and Security");
        String securityDoc = """
                # Security Architecture

                OENEXA enforces banking-grade authentication with JWT and 2FA TOTP RFC 6238.
                Device fingerprinting and IP velocity tracking trigger account lockouts after 5 failed attempts.
                """;

        service.uploadDocument(
                kb.kbId(),
                "Security_Whitepaper.txt",
                securityDoc.getBytes(StandardCharsets.UTF_8),
                DocumentFormat.TXT
        );

        SemanticSearchResult result = service.searchKnowledge(kb.kbId(), "How does authentication and 2FA work?", 3);

        assertNotNull(result);
        assertEquals("How does authentication and 2FA work?", result.query());
        assertFalse(result.citations().isEmpty());
        assertTrue(result.synthesizedAnswer().contains("Security_Whitepaper.txt"));
        assertTrue(result.confidenceScore() > 50.0);
        assertTrue(result.retrievalLatencyMs() >= 0);

        Citation firstCitation = result.citations().get(0);
        assertEquals("Security_Whitepaper.txt", firstCitation.documentTitle());
        assertTrue(firstCitation.snippet().contains("banking-grade authentication"));
        assertTrue(firstCitation.relevanceScore() > 0.0);
    }
}


