package com.oenexa.api.controller;

import com.oenexa.api.dto.ApiResponse;
import com.oenexa.api.dto.knowledge.ContextRagDtos.*;
import com.oenexa.application.knowledge.ContextRagPlatformService;
import com.oenexa.application.service.knowledge.RagService;
import com.oenexa.application.service.knowledge.RepositoryKnowledgeService;
import com.oenexa.domain.model.knowledge.ContextRagDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ContextRagController Zero-Mock Unit Tests")
class ContextRagControllerTest {

    private ContextRagController controller;

    @BeforeEach
    void setUp() {
        RagService ragService = new RagService();
        RepositoryKnowledgeService repoService = new RepositoryKnowledgeService();
        ContextRagPlatformService platformService = new ContextRagPlatformService(ragService, repoService);
        controller = new ContextRagController(platformService);
    }

    @Test
    @DisplayName("Should execute RAG query and return verified answer via REST endpoint")
    void shouldExecuteRagQuery() {
        ExecuteRagQueryRequest request = new ExecuteRagQueryRequest(
                "Java 25 Virtual Threads architecture",
                List.of(ContextScope.ARCHITECTURE)
        );

        ResponseEntity<ApiResponse<RagQueryResponse>> response = controller.executeRagQuery(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        RagQueryResponse data = response.getBody().data();
        assertNotNull(data);
        assertTrue(data.verification().verified());
        assertFalse(data.attributedSources().isEmpty());
    }

    @Test
    @DisplayName("Should expand context across multiple scopes via REST endpoint")
    void shouldExpandContext() {
        ExpandContextRequest request = new ExpandContextRequest(
                "Clean Hexagonal Architecture ADR",
                List.of(ContextScope.ADR_RECORD),
                1_500
        );

        ResponseEntity<ApiResponse<ExpandedContext>> response = controller.expandContext(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());

        ExpandedContext data = response.getBody().data();
        assertNotNull(data);
        assertFalse(data.relevantChunks().isEmpty());
    }

    @Test
    @DisplayName("Should index new context chunk via REST endpoint")
    void shouldIndexContextChunk() {
        IndexContextChunkRequest request = new IndexContextChunkRequest(
                "sec-pol-01",
                ContextScope.POLICY_STANDARD,
                "SECURITY.md",
                "Zero-Mock Testing Standard",
                "All unit and integration tests must run with zero mocks.",
                1.0
        );

        ResponseEntity<ApiResponse<String>> response = controller.indexChunk(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
        assertTrue(response.getBody().data().contains("sec-pol-01"));
    }
}
