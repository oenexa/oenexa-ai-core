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

    @Test
    void ragModuleBeanDefinitionsAndMetadata() {
        var module = new RagModule();
        assertThat(module.id()).isEqualTo("rag");
        assertThat(module.displayName()).isEqualTo("OENEXA RAG");
        assertThat(module.description()).contains("Chunking, embedding");
        assertThat(module.capabilities()).contains("ingestion", "embedding", "vector-search", "context-construction", "citations");
        assertThat(module.status()).isEqualTo(ai.oenexa.common.module.PlatformModule.ModuleStatus.ACTIVE);

        var embeddingPort = module.embeddingPort(256);
        assertThat(embeddingPort).isInstanceOf(HashEmbeddingAdapter.class);

        var vectorStorePort = module.vectorStorePort();
        assertThat(vectorStorePort).isInstanceOf(InMemoryVectorStore.class);

        var ragService = module.ragService(embeddingPort, vectorStorePort, 1200, 150);
        assertThat(ragService).isNotNull();
    }

    @Test
    void chunkAndPortsDomainContract() throws Exception {
        var constructor = ai.oenexa.rag.domain.Ports.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var portsInstance = constructor.newInstance();
        assertThat(portsInstance).isNotNull();

        var docId = UUID.randomUUID();
        float[] vector = new float[]{1.0f, 2.0f};
        var chunk = new ai.oenexa.rag.domain.Chunk(docId, 0, "doc.txt", "content", vector);
        assertThat(chunk.documentId()).isEqualTo(docId);
        assertThat(chunk.index()).isEqualTo(0);
        assertThat(chunk.source()).isEqualTo("doc.txt");
        assertThat(chunk.text()).isEqualTo("content");
        assertThat(chunk.embedding()).isEqualTo(vector);
        assertThat(chunk.citation()).isEqualTo("doc.txt#0");

        float[] newVec = new float[]{3.0f, 4.0f};
        var updated = chunk.withEmbedding(newVec);
        assertThat(updated.embedding()).isEqualTo(newVec);
        assertThat(updated.citation()).isEqualTo("doc.txt#0");

        var scored = new ai.oenexa.rag.domain.Ports.Scored(chunk, 0.95);
        assertThat(scored.chunk()).isEqualTo(chunk);
        assertThat(scored.score()).isEqualTo(0.95);
    }
}
