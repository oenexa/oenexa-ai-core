package com.oenexa.api;

import com.oenexa.application.EnterpriseKnowledgeService;
import com.oenexa.domain.EnterpriseKnowledgeDomain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EnterpriseKnowledgeController Zero-Mock Unit Tests")
class EnterpriseKnowledgeControllerTest {

    private EnterpriseKnowledgeController controller;
    private EnterpriseKnowledgeService service;

    @BeforeEach
    void setUp() {
        service = new EnterpriseKnowledgeService();
        controller = new EnterpriseKnowledgeController(service);
    }

    @Test
    @DisplayName("Should create and list knowledge bases via controller")
    void shouldCreateAndListBases() {
        var createReq = new EnterpriseKnowledgeController.CreateBaseRequest(
                "tenant-alpha", "Architecture Base", "System docs"
        );
        ResponseEntity<KnowledgeBase> createResp = controller.createBase(createReq);
        assertEquals(HttpStatus.CREATED, createResp.getStatusCode());
        assertNotNull(createResp.getBody());
        assertEquals("Architecture Base", createResp.getBody().name());

        ResponseEntity<List<KnowledgeBase>> listResp = controller.listBases("tenant-alpha");
        assertEquals(HttpStatus.OK, listResp.getStatusCode());
        assertNotNull(listResp.getBody());
        assertEquals(1, listResp.getBody().size());
    }

    @Test
    @DisplayName("Should upload document, execute search, and retrieve knowledge graph via controller")
    void shouldUploadSearchAndRetrieveGraph() {
        var createReq = new EnterpriseKnowledgeController.CreateBaseRequest(
                "tenant-alpha", "Security Base", "Security docs"
        );
        KnowledgeBase kb = controller.createBase(createReq).getBody();
        assertNotNull(kb);

        var uploadReq = new EnterpriseKnowledgeController.UploadDocumentRequest(
                "ZeroTrust.md",
                "# Zero Trust Architecture\nEvery request must be authenticated with 2FA TOTP and validated with non-null checks.",
                DocumentFormat.MARKDOWN
        );
        ResponseEntity<KnowledgeDocument> uploadResp = controller.uploadDocument(kb.kbId(), uploadReq);
        assertEquals(HttpStatus.CREATED, uploadResp.getStatusCode());
        assertNotNull(uploadResp.getBody());
        assertEquals("ZeroTrust.md", uploadResp.getBody().title());

        var searchReq = new EnterpriseKnowledgeController.SearchKnowledgeRequest("Zero Trust and 2FA", 3);
        ResponseEntity<SemanticSearchResult> searchResp = controller.searchKnowledge(kb.kbId(), searchReq);
        assertEquals(HttpStatus.OK, searchResp.getStatusCode());
        assertNotNull(searchResp.getBody());
        assertFalse(searchResp.getBody().citations().isEmpty());

        ResponseEntity<KnowledgeGraph> graphResp = controller.getGraph(kb.kbId());
        assertEquals(HttpStatus.OK, graphResp.getStatusCode());
        assertNotNull(graphResp.getBody());
    }

    @Test
    @DisplayName("Should crawl website via controller")
    void shouldCrawlWebsite() {
        var createReq = new EnterpriseKnowledgeController.CreateBaseRequest(
                "tenant-alpha", "Web Base", "Crawl docs"
        );
        KnowledgeBase kb = controller.createBase(createReq).getBody();
        assertNotNull(kb);

        var crawlReq = new EnterpriseKnowledgeController.CrawlWebsiteRequest(
                "https://oenexa.ai/docs", 2, true
        );
        ResponseEntity<KnowledgeDocument> crawlResp = controller.crawlWebsite(kb.kbId(), crawlReq);
        assertEquals(HttpStatus.CREATED, crawlResp.getStatusCode());
        assertNotNull(crawlResp.getBody());
        assertEquals(KnowledgeSourceType.WEBSITE_CRAWL, crawlResp.getBody().sourceType());
    }
}
