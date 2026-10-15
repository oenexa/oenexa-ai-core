package ai.oenexa.rag;

import ai.oenexa.rag.application.Chunker;
import ai.oenexa.rag.application.RagService;
import ai.oenexa.rag.infra.HashEmbeddingAdapter;
import ai.oenexa.rag.infra.InMemoryVectorStore;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RagPipelineTest {

    private final UUID tenant = UUID.randomUUID();
    private final RagService rag = new RagService(new Chunker(200, 20), new HashEmbeddingAdapter(256),
            new InMemoryVectorStore());

    @Test
    void retrievesMostRelevantChunkWithCitation() {
        rag.ingest(tenant, UUID.randomUUID(), "k8s.md",
                "Kubernetes pods enter CrashLoopBackOff when the container exits repeatedly.").block();
        rag.ingest(tenant, UUID.randomUUID(), "cooking.md",
                "Whisk the eggs and fold in the flour to make a light pancake batter.").block();

        StepVerifier.create(rag.buildContext(tenant, "why is my pod in CrashLoopBackOff", 2, 5000))
                .assertNext(ctx -> {
                    assertThat(ctx.citations().get(0)).isEqualTo("k8s.md#0");
                    assertThat(ctx.text()).contains("[k8s.md#0]").contains("CrashLoopBackOff");
                })
                .verifyComplete();
    }

    @Test
    void tenantsCannotSeeEachOthersVectors() {
        rag.ingest(tenant, UUID.randomUUID(), "private.md", "confidential roadmap for the migration").block();

        StepVerifier.create(rag.retrieve(UUID.randomUUID(), "confidential roadmap", 5))
                .verifyComplete();
    }

    @Test
    void reingestingSameDocumentReplacesChunks() {
        var doc = UUID.randomUUID();
        rag.ingest(tenant, doc, "a.md", "alpha beta gamma").block();
        rag.ingest(tenant, doc, "a.md", "alpha beta gamma").block();

        assertThat(rag.retrieve(tenant, "alpha", 10).collectList().block()).hasSize(1);
    }

    @Test
    void contextRespectsCharacterBudgetButKeepsBestChunk() {
        rag.ingest(tenant, UUID.randomUUID(), "big.md", ("spring boot service " .repeat(40)).strip()).block();

        var ctx = rag.buildContext(tenant, "spring boot service", 10, 50).block();

        assertThat(ctx.citations()).hasSize(1);
    }

    @Test
    void chunkerSplitsWithOverlapAndValidatesArguments() {
        var chunks = new Chunker(50, 10).split("word ".repeat(40));
        assertThat(chunks).hasSizeGreaterThan(2).allMatch(c -> c.length() <= 50);
        assertThat(new Chunker(50, 10).split("   ")).isEmpty();
        assertThat(new Chunker(50, 10).split(null)).isEmpty();
        assertThatThrownBy(() -> new Chunker(10, 10)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void chunkerPrefersParagraphBoundaries() {
        String text = "First paragraph about auth tokens and sessions.\n\nSecond paragraph about kafka topics.";
        var chunks = new Chunker(60, 5).split(text);
        assertThat(chunks.get(0)).endsWith("sessions.");
    }

    @Test
    void emptyQueryYieldsNoResultsAndEmptyContext() {
        var ctx = rag.buildContext(tenant, "   ", 3, 1000).block();
        assertThat(ctx.citations()).isEmpty();
        assertThat(ctx.text()).isEmpty();
    }
}
