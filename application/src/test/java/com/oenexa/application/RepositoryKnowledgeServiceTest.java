package com.oenexa.application;

import com.oenexa.domain.ChatDomain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepositoryKnowledgeService Zero-Mock Unit Tests")
class RepositoryKnowledgeServiceTest {

    private final RepositoryKnowledgeService service = new RepositoryKnowledgeService();

    @Test
    @DisplayName("Should detect heterogeneous stacks from file paths and contents")
    void shouldDetectStacks() {
        assertEquals("SPRING_BOOT", service.detectStack("src/Main.java", "@SpringBootApplication public class Main {}"));
        assertEquals("JAVA", service.detectStack("src/Helper.java", "public class Helper {}"));
        assertEquals("REACT", service.detectStack("src/App.tsx", "export function App() {}"));
        assertEquals("ANGULAR", service.detectStack("src/app.component.ts", "@Component({}) export class AppComponent {}"));
        assertEquals("VUE", service.detectStack("src/App.vue", "<template><div>App</div></template>"));
        assertEquals("GRADLE", service.detectStack("build.gradle.kts", "plugins { java }"));
        assertEquals("MAVEN", service.detectStack("pom.xml", "<project></project>"));
        assertEquals("DOCKER", service.detectStack("docker-compose.yml", "services:"));
        assertEquals("TERRAFORM", service.detectStack("main.tf", "resource \"aws_s3_bucket\""));
        assertEquals("KUBERNETES", service.detectStack("k8s/deploy.yaml", "apiVersion: apps/v1\nkind: Deployment"));
        assertEquals("YAML", service.detectStack("config.yaml", "key: value"));
        assertEquals("POSTGRESQL", service.detectStack("migration.sql", "CREATE TABLE users (id BIGSERIAL PRIMARY KEY)"));
        assertEquals("SQL", service.detectStack("schema.sql", "SELECT 1"));
        assertEquals("GENERIC", service.detectStack("README.md", "Documentation"));
    }

    @Test
    @DisplayName("Should extract symbols from Java, TypeScript, and SQL")
    void shouldExtractSymbols() {
        String javaCode = """
                package com.example;
                public class UserService {
                    @GetMapping("/api/users")
                    public List<User> list() { return List.of(); }
                }
                """;
        Map<String, String> symbols = service.extractSymbols("UserService.java", javaCode);
        assertTrue(symbols.containsKey("UserService"));
        assertEquals("CLASS", symbols.get("UserService"));
        assertTrue(symbols.containsKey("@GetMapping(\"/api/users\")"));
        assertEquals("API_ENDPOINT", symbols.get("@GetMapping(\"/api/users\")"));

        String tsCode = "export function UserProfile() { return null; }";
        Map<String, String> tsSymbols = service.extractSymbols("UserProfile.tsx", tsCode);
        assertTrue(tsSymbols.containsKey("UserProfile"));
        assertEquals("REACT_COMPONENT", tsSymbols.get("UserProfile"));

        String sqlCode = "CREATE TABLE accounts (id INT PRIMARY KEY);";
        Map<String, String> sqlSymbols = service.extractSymbols("accounts.sql", sqlCode);
        assertTrue(sqlSymbols.containsKey("accounts"));
        assertEquals("DB_TABLE", sqlSymbols.get("accounts"));

        assertTrue(service.extractSymbols("empty.txt", null).isEmpty());
        assertTrue(service.extractSymbols("empty.txt", "   ").isEmpty());
    }

    @Test
    @DisplayName("Should compute embeddings and calculate cosine similarity")
    void shouldComputeEmbeddingsAndCosineSimilarity() {
        List<Double> v1 = service.computeEmbedding("public class AuthenticationFilter");
        List<Double> v2 = service.computeEmbedding("AuthenticationFilter security token");
        List<Double> v3 = service.computeEmbedding("docker kubernetes deployment yaml");

        assertEquals(64, v1.size());
        assertEquals(64, v2.size());
        assertEquals(64, v3.size());

        double sim12 = service.cosineSimilarity(v1, v2);
        double sim13 = service.cosineSimilarity(v1, v3);

        assertTrue(sim12 > sim13, "Auth texts should have higher cosine similarity than Auth and Docker texts");
        assertEquals(1.0, service.cosineSimilarity(v1, v1), 0.001);

        // Edge cases
        assertEquals(0.0, service.cosineSimilarity(null, v1));
        assertEquals(0.0, service.cosineSimilarity(v1, null));
        assertEquals(0.0, service.cosineSimilarity(List.of(), List.of()));
        assertEquals(0.0, service.cosineSimilarity(List.of(0.0, 0.0), List.of(0.0, 0.0)));

        List<Double> empty = service.computeEmbedding(null);
        assertEquals(64, empty.size());
        assertEquals(0.0, empty.get(0));
    }

    @Test
    @DisplayName("Should index repository, build knowledge map, and execute vector search")
    void shouldIndexRepositoryAndSearch() {
        String repoId = "oenexa-core-repo";
        Map<String, String> files = Map.of(
                "backend/src/AuthService.java", "package com.oenexa;\n@Service\npublic class AuthService {}\n@GetMapping(\"/auth/token\")",
                "frontend/src/App.tsx", "export function App() { return <div>Oenexa</div>; }",
                "infra/docker-compose.yml", "services:\n  postgres:\n    image: postgres:16"
        );

        RepositoryKnowledgeMap map = service.indexRepository(repoId, files);
        assertNotNull(map);
        assertEquals(repoId, map.repositoryId());
        assertTrue(map.detectedTechnologies().contains("SPRING_BOOT"));
        assertTrue(map.detectedTechnologies().contains("REACT"));
        assertTrue(map.detectedTechnologies().contains("DOCKER"));
        assertTrue(map.vectorChunksCount() > 0);

        // Search similar chunks
        List<ProjectKnowledgeChunk> results = service.searchSimilarChunks(repoId, "Authentication token service", 2);
        assertFalse(results.isEmpty());
        assertTrue(results.get(0).filePath().contains("AuthService.java"));

        // Retrieve stored knowledge map
        RepositoryKnowledgeMap fetched = service.getKnowledgeMap(repoId);
        assertEquals(map.repositoryId(), fetched.repositoryId());

        // Unknown repo map
        RepositoryKnowledgeMap unknown = service.getKnowledgeMap("unknown-repo");
        assertEquals("UNINDEXED", unknown.primaryStack());

        // Search in unknown or empty query
        assertTrue(service.searchSimilarChunks("unknown-repo", "query", 5).isEmpty());
        assertTrue(service.searchSimilarChunks(repoId, null, 5).isEmpty());
        assertTrue(service.searchSimilarChunks(repoId, "   ", 5).isEmpty());
    }

    @Test
    @DisplayName("Should handle empty content in chunking")
    void shouldHandleEmptyContentInChunking() {
        assertTrue(service.chunkContent("repo", "file.txt", "TEXT", null, Map.of()).isEmpty());
        assertTrue(service.chunkContent("repo", "file.txt", "TEXT", "   ", Map.of()).isEmpty());
    }
}
