package ai.oenexa.rag.web;

import ai.oenexa.auth.domain.Role;
import ai.oenexa.auth.token.JwtService.TokenClaims;
import ai.oenexa.common.error.OenexaException;
import ai.oenexa.rag.application.Chunker;
import ai.oenexa.rag.application.RagService;
import ai.oenexa.rag.infra.HashEmbeddingAdapter;
import ai.oenexa.rag.infra.InMemoryVectorStore;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagControllerTest {

    private RagController controller;
    private UsernamePasswordAuthenticationToken auth;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        var rag = new RagService(new Chunker(200, 20), new HashEmbeddingAdapter(256), new InMemoryVectorStore());
        controller = new RagController(rag);
        tenantId = UUID.randomUUID();
        var claims = new TokenClaims(UUID.randomUUID(), tenantId, Role.DEVELOPER);
        auth = new UsernamePasswordAuthenticationToken("user", "pass", List.of());
        auth.setDetails(claims);
    }

    @Test
    void ingestAndQueryFlow() {
        var ingestBody = new RagController.IngestRequest("notes.md", "Spring Boot and Spring AI facilitate microservices.");
        StepVerifier.create(controller.ingest(auth, ingestBody))
                .assertNext(res -> {
                    assertThat(res).containsKey("documentId");
                    assertThat(res.get("chunks")).isEqualTo(1);
                })
                .verifyComplete();

        var queryBody = new RagController.QueryRequest("Spring AI", 3);
        StepVerifier.create(controller.query(auth, queryBody))
                .assertNext(ctx -> {
                    assertThat(ctx.citations()).isNotEmpty();
                    assertThat(ctx.text()).contains("notes.md");
                })
                .verifyComplete();

        var defaultKQuery = new RagController.QueryRequest("Spring", null);
        StepVerifier.create(controller.query(auth, defaultKQuery))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void unauthorizedPrincipalThrows() {
        Principal anonymous = () -> "anon";
        var queryBody = new RagController.QueryRequest("test", null);
        var ingestBody = new RagController.IngestRequest("notes.md", "content");

        assertThatThrownBy(() -> controller.query(anonymous, queryBody))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.query(null, queryBody))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.ingest(anonymous, ingestBody))
                .isInstanceOf(OenexaException.class);

        assertThatThrownBy(() -> controller.ingest(null, ingestBody))
                .isInstanceOf(OenexaException.class);

        var badAuth = new UsernamePasswordAuthenticationToken("user", "pass", List.of());
        badAuth.setDetails("bad-claims");
        assertThatThrownBy(() -> controller.query(badAuth, queryBody))
                .isInstanceOf(OenexaException.class);
    }
}
