package ai.oenexa.repository;

import ai.oenexa.repository.domain.ArchitectureGraph;
import ai.oenexa.repository.domain.CloneRepositoryRequest;
import ai.oenexa.repository.domain.DependencyNode;
import ai.oenexa.repository.domain.RepositoryMetadata;
import ai.oenexa.repository.domain.VcsProvider;
import ai.oenexa.repository.service.DefaultRepositoryService;
import ai.oenexa.repository.web.RepositoryController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Repository Service & Controller Tests (Zero-Mock Policy)")
class RepositoryServiceTest {

    private DefaultRepositoryService repositoryService;
    private RepositoryController repositoryController;

    @BeforeEach
    void setUp() {
        repositoryService = new DefaultRepositoryService();
        repositoryController = new RepositoryController(repositoryService);
    }

    @Test
    @DisplayName("RepositoryModule metadata and initialization")
    void testRepositoryModuleMetadata() {
        RepositoryModule module = new RepositoryModule();
        assertEquals("repository", module.id());
        assertEquals("Repository Intelligence", module.displayName());
        assertTrue(module.capabilities().contains("service-map"));
    }

    @Test
    @DisplayName("VcsProvider enum invariants")
    void testEnumInvariants() {
        assertEquals(4, VcsProvider.values().length);
        assertEquals(VcsProvider.GITHUB, VcsProvider.valueOf("GITHUB"));
        assertEquals(VcsProvider.LOCAL_GIT, VcsProvider.valueOf("LOCAL_GIT"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        DependencyNode node = new DependencyNode("spring-boot", null, null, null);
        assertEquals("latest", node.version());
        assertEquals("implementation", node.scope());
        assertTrue(node.transitiveDependencies().isEmpty());

        assertThrows(NullPointerException.class, () -> new DependencyNode(null, "1.0", "compile", List.of()));

        RepositoryMetadata meta = new RepositoryMetadata("https://github.com/org/repo", null, null, 50, null);
        assertEquals(VcsProvider.GITHUB, meta.provider());
        assertEquals("main", meta.defaultBranch());
        assertTrue(meta.detectedLanguages().isEmpty());

        assertThrows(NullPointerException.class, () -> new RepositoryMetadata(null, VcsProvider.GITHUB, "main", 0, List.of()));

        ArchitectureGraph graph = new ArchitectureGraph("repo", null, null, null);
        assertTrue(graph.modules().isEmpty());
        assertTrue(graph.dependencies().isEmpty());
        assertEquals("", graph.mermaidDependencyMap());

        assertThrows(NullPointerException.class, () -> new ArchitectureGraph(null, List.of(), List.of(), ""));

        CloneRepositoryRequest req = new CloneRepositoryRequest("https://gitlab.com/test", null, null, null);
        assertEquals(VcsProvider.GITHUB, req.provider());
        assertEquals("main", req.targetBranch());
        assertEquals("", req.token());

        assertThrows(NullPointerException.class, () -> new CloneRepositoryRequest(null, VcsProvider.GITHUB, "main", ""));
    }

    @Test
    @DisplayName("Inspect repository metadata and detect languages")
    void testInspectRepository() {
        RepositoryMetadata meta = repositoryService.inspectRepository("https://github.com/oenexa/backend", VcsProvider.GITHUB);
        assertEquals("https://github.com/oenexa/backend", meta.repositoryUrl());
        assertEquals(VcsProvider.GITHUB, meta.provider());
        assertTrue(meta.detectedLanguages().contains("Java"));

        RepositoryMetadata pyMeta = repositoryService.inspectRepository("https://github.com/oenexa/python-agent", null);
        assertEquals(VcsProvider.GITHUB, pyMeta.provider());
        assertTrue(pyMeta.detectedLanguages().contains("Python"));
    }

    @Test
    @DisplayName("Analyze architecture and generate dependency graph")
    void testAnalyzeArchitecture() {
        ArchitectureGraph graph = repositoryService.analyzeArchitecture(
                "https://github.com/oenexa/oenexa-ai-engineer-backend.git",
                List.of("oenexa-common", "oenexa-core", "oenexa-gateway")
        );

        assertEquals("oenexa-ai-engineer-backend", graph.repoName());
        assertEquals(3, graph.modules().size());
        assertTrue(graph.mermaidDependencyMap().contains("graph TD"));
        assertTrue(graph.mermaidDependencyMap().contains("spring-boot-starter-web"));

        // Fallback default modules test
        ArchitectureGraph defGraph = repositoryService.analyzeArchitecture("https://github.com/oenexa/", List.of());
        assertEquals("oenexa-repo", defGraph.repoName());
        assertEquals(4, defGraph.modules().size());
    }

    @Test
    @DisplayName("Generate Mermaid dependency diagram with transitives and empty handling")
    void testGenerateMermaidDiagram() {
        assertEquals("graph TD\n    Root[\"Empty Architecture\"]", repositoryService.generateDependencyMermaid(List.of()));
        assertEquals("graph TD\n    Root[\"Empty Architecture\"]", repositoryService.generateDependencyMermaid(null));

        DependencyNode parent = new DependencyNode("parent-lib", "1.0", "api", List.of("child-lib-1", "child-lib-2"));
        String mermaid = repositoryService.generateDependencyMermaid(List.of(parent));

        assertTrue(mermaid.contains("D0[\"parent-lib (1.0)\"]"));
        assertTrue(mermaid.contains("D0_0[\"child-lib-1\"]"));
        assertTrue(mermaid.contains("D0 --> D0_0"));
    }

    @Test
    @DisplayName("RepositoryController endpoints return valid HTTP responses")
    void testControllerEndpoints() {
        ResponseEntity<RepositoryMetadata> inspResp = repositoryController.inspect(Map.of(
                "url", "https://github.com/oenexa/oenexa-ai-engineer-backend",
                "provider", "GITHUB"
        ));
        assertEquals(200, inspResp.getStatusCode().value());
        assertNotNull(inspResp.getBody());

        ResponseEntity<ArchitectureGraph> archResp = repositoryController.architecture(Map.of(
                "url", "https://github.com/oenexa/oenexa-ai-engineer-backend",
                "modules", List.of("domain", "application", "infrastructure", "api")
        ));
        assertEquals(200, archResp.getStatusCode().value());
        assertNotNull(archResp.getBody());
    }
}
