package ai.oenexa.documentation;

import ai.oenexa.documentation.domain.DiagramRequest;
import ai.oenexa.documentation.domain.DiagramResult;
import ai.oenexa.documentation.domain.DocFormat;
import ai.oenexa.documentation.domain.DocType;
import ai.oenexa.documentation.domain.DocumentationRequest;
import ai.oenexa.documentation.domain.DocumentationResult;
import ai.oenexa.documentation.service.DefaultDocumentationService;
import ai.oenexa.documentation.web.DocumentationController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Documentation Service & Controller Tests (Zero-Mock Policy)")
class DocumentationServiceTest {

    private DefaultDocumentationService documentationService;
    private DocumentationController documentationController;

    @BeforeEach
    void setUp() {
        documentationService = new DefaultDocumentationService();
        documentationController = new DocumentationController(documentationService);
    }

    @Test
    @DisplayName("DocumentationModule metadata and initialization")
    void testDocumentationModuleMetadata() {
        DocumentationModule module = new DocumentationModule();
        assertEquals("documentation", module.id());
        assertEquals("OENEXA Documentation", module.displayName());
        assertTrue(module.capabilities().contains("diagram-generation"));
    }

    @Test
    @DisplayName("DocFormat and DocType enum invariants")
    void testEnumInvariants() {
        assertEquals(4, DocFormat.values().length);
        assertEquals(DocFormat.MARKDOWN, DocFormat.valueOf("MARKDOWN"));

        assertEquals(4, DocType.values().length);
        assertEquals(DocType.ARCHITECTURE, DocType.valueOf("ARCHITECTURE"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        DocumentationRequest req = new DocumentationRequest("Title", null, null, null, null);
        assertEquals(DocType.ARCHITECTURE, req.docType());
        assertEquals(DocFormat.MARKDOWN, req.format());
        assertEquals("", req.context());
        assertTrue(req.components().isEmpty());

        assertThrows(NullPointerException.class, () -> new DocumentationRequest(null, DocType.ADR, DocFormat.MARKDOWN, "", List.of()));

        DocumentationResult res = new DocumentationResult("Title", DocType.ADR, DocFormat.MARKDOWN, null, 10, null);
        assertEquals("", res.content());
        assertTrue(res.generatedSections().isEmpty());

        assertThrows(NullPointerException.class, () -> new DocumentationResult(null, DocType.ADR, DocFormat.MARKDOWN, "", 0, List.of()));
        assertThrows(NullPointerException.class, () -> new DocumentationResult("T", null, DocFormat.MARKDOWN, "", 0, List.of()));
        assertThrows(NullPointerException.class, () -> new DocumentationResult("T", DocType.ADR, null, "", 0, List.of()));

        DiagramRequest dReq = new DiagramRequest(null, "Diag", null, null);
        assertEquals("flowchart TD", dReq.diagramType());
        assertTrue(dReq.nodes().isEmpty());
        assertTrue(dReq.connections().isEmpty());

        assertThrows(NullPointerException.class, () -> new DiagramRequest("flowchart TD", null, List.of(), List.of()));

        DiagramResult dRes = new DiagramResult("Diag", "flowchart TD", null);
        assertEquals("", dRes.mermaidSource());

        assertThrows(NullPointerException.class, () -> new DiagramResult(null, "flowchart TD", ""));
        assertThrows(NullPointerException.class, () -> new DiagramResult("Diag", null, ""));
    }

    @Test
    @DisplayName("Generate Architecture Decision Record (ADR)")
    void testGenerateAdr() {
        DocumentationRequest req = new DocumentationRequest(
                "Use Virtual Threads for REST API",
                DocType.ADR,
                DocFormat.MARKDOWN,
                "Need high concurrency without reactive complexity.",
                List.of("api", "oenexa-gateway")
        );
        DocumentationResult result = documentationService.generateDocumentation(req);

        assertEquals("Use Virtual Threads for REST API", result.title());
        assertEquals(DocType.ADR, result.docType());
        assertTrue(result.content().contains("# ADR: Use Virtual Threads for REST API"));
        assertTrue(result.content().contains("## 1. Status"));
        assertTrue(result.content().contains("## 2. Context"));
        assertTrue(result.content().contains("## 3. Decision"));
        assertTrue(result.content().contains("## 4. Consequences"));
        assertTrue(result.wordCount() > 20);
        assertEquals(4, result.generatedSections().size());

        // Empty context test
        DocumentationRequest emptyContextReq = new DocumentationRequest("ADR 2", DocType.ADR, DocFormat.MARKDOWN, "", List.of());
        DocumentationResult emptyRes = documentationService.generateDocumentation(emptyContextReq);
        assertTrue(emptyRes.content().contains("Architectural requirement evaluation."));
    }

    @Test
    @DisplayName("Generate REST API Reference Specification")
    void testGenerateApiReference() {
        DocumentationRequest req = new DocumentationRequest(
                "Code Review Endpoints",
                DocType.API_REFERENCE,
                DocFormat.MARKDOWN,
                "API specs",
                List.of("Code", "Review", "Security")
        );
        DocumentationResult result = documentationService.generateDocumentation(req);

        assertEquals(DocType.API_REFERENCE, result.docType());
        assertTrue(result.content().contains("# API Reference: Code Review Endpoints"));
        assertTrue(result.content().contains("/api/v1/code"));
        assertTrue(result.content().contains("/api/v1/review"));
        assertTrue(result.content().contains("/api/v1/security"));

        // Empty components test
        DocumentationRequest emptyReq = new DocumentationRequest("Default API", DocType.API_REFERENCE, DocFormat.MARKDOWN, "", List.of());
        DocumentationResult emptyRes = documentationService.generateDocumentation(emptyReq);
        assertTrue(emptyRes.content().contains("/api/v1/health"));
    }

    @Test
    @DisplayName("Generate Architecture Specification")
    void testGenerateArchitecture() {
        DocumentationRequest req = new DocumentationRequest(
                "OENEXA Platform",
                DocType.ARCHITECTURE,
                DocFormat.MARKDOWN,
                "Enterprise Architecture",
                List.of("oenexa-auth", "oenexa-agent", "oenexa-rag")
        );
        DocumentationResult result = documentationService.generateDocumentation(req);

        assertEquals(DocType.ARCHITECTURE, result.docType());
        assertTrue(result.content().contains("# Architecture Specification: OENEXA Platform"));
        assertTrue(result.content().contains("oenexa-auth"));
        assertTrue(result.content().contains("Enterprise Architecture"));

        // Empty components and context test
        DocumentationRequest emptyReq = new DocumentationRequest("Default Arch", DocType.ARCHITECTURE, DocFormat.MARKDOWN, "", List.of());
        DocumentationResult emptyRes = documentationService.generateDocumentation(emptyReq);
        assertTrue(emptyRes.content().contains("Core Monolith Engine"));
        assertTrue(emptyRes.content().contains("High-level architecture design."));
    }

    @Test
    @DisplayName("Generate Mermaid Diagram Document")
    void testGenerateMermaidDiagramDoc() {
        DocumentationRequest req = new DocumentationRequest(
                "Data Flow Diagram",
                DocType.MERMAID_DIAGRAM,
                DocFormat.MERMAID,
                "",
                List.of("Auth", "Gateway", "Core")
        );
        DocumentationResult result = documentationService.generateDocumentation(req);

        assertEquals(DocType.MERMAID_DIAGRAM, result.docType());
        assertTrue(result.content().contains("```mermaid"));
        assertTrue(result.content().contains("flowchart TD"));
    }

    @Test
    @DisplayName("Generate Mermaid Diagrams with different node and connection topologies")
    void testGenerateDiagramTopology() {
        // Default fallback topology
        DiagramResult defResult = documentationService.generateDiagram(new DiagramRequest("flowchart TD", "Default", List.of(), List.of()));
        assertTrue(defResult.mermaidSource().contains("Client[\"Browser / Frontend UI\"]"));

        // Auto-connected nodes
        DiagramResult autoResult = documentationService.generateDiagram(new DiagramRequest("flowchart LR", "Pipeline", List.of("Ingest", "Transform", "Load"), List.of()));
        assertTrue(autoResult.mermaidSource().contains("N0[\"Ingest\"]"));
        assertTrue(autoResult.mermaidSource().contains("N0 --> N1"));
        assertTrue(autoResult.mermaidSource().contains("N1 --> N2"));

        // Custom connections
        DiagramResult customResult = documentationService.generateDiagram(new DiagramRequest(
                "sequenceDiagram",
                "Seq",
                List.of("User", "Gateway"),
                List.of("User->>Gateway: POST /login", "Gateway-->>User: 200 OK")
        ));
        assertTrue(customResult.mermaidSource().contains("User->>Gateway: POST /login"));
    }

    @Test
    @DisplayName("DocumentationController endpoints return valid HTTP responses")
    void testControllerEndpoints() {
        DocumentationRequest req = new DocumentationRequest("Test Spec", DocType.ARCHITECTURE, DocFormat.MARKDOWN, "", List.of());
        ResponseEntity<DocumentationResult> docResp = documentationController.generate(req);
        assertEquals(200, docResp.getStatusCode().value());
        assertNotNull(docResp.getBody());

        DiagramRequest dReq = new DiagramRequest("flowchart TD", "Arch", List.of("A", "B"), List.of());
        ResponseEntity<DiagramResult> diagResp = documentationController.diagram(dReq);
        assertEquals(200, diagResp.getStatusCode().value());
        assertNotNull(diagResp.getBody());
    }
}
