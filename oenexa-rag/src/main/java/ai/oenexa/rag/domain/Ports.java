package ai.oenexa.rag.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Outbound ports of the RAG pipeline. Production adapters: embedding provider via Spring AI, Qdrant. */
public final class Ports {

    private Ports() {
    }

    public interface EmbeddingPort {
        Mono<float[]> embed(String text);
    }

    public record Scored(Chunk chunk, double score) {
    }

    public interface VectorStorePort {
        /** Vectors are partitioned by tenant (one Qdrant collection / payload filter per tenant). */
        Mono<Void> upsert(UUID tenantId, List<Chunk> chunks);

        Flux<Scored> search(UUID tenantId, float[] query, int topK);
    }
}
