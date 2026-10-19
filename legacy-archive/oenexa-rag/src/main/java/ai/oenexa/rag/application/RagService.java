package ai.oenexa.rag.application;

import ai.oenexa.rag.domain.Chunk;
import ai.oenexa.rag.domain.Ports.EmbeddingPort;
import ai.oenexa.rag.domain.Ports.Scored;
import ai.oenexa.rag.domain.Ports.VectorStorePort;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Ingestion -> embedding -> vector search -> retrieval -> context construction. */
public class RagService {

    public record Context(String text, List<String> citations) {
    }

    private final Chunker chunker;
    private final EmbeddingPort embeddings;
    private final VectorStorePort store;

    public RagService(Chunker chunker, EmbeddingPort embeddings, VectorStorePort store) {
        this.chunker = chunker;
        this.embeddings = embeddings;
        this.store = store;
    }

    public Mono<Integer> ingest(UUID tenantId, UUID documentId, String source, String text) {
        List<String> pieces = chunker.split(text);
        return Flux.range(0, pieces.size())
                .concatMap(i -> embeddings.embed(pieces.get(i))
                        .map(v -> new Chunk(documentId, i, source, pieces.get(i), v)))
                .collectList()
                .flatMap(chunks -> store.upsert(tenantId, chunks).thenReturn(chunks.size()));
    }

    public Flux<Scored> retrieve(UUID tenantId, String query, int topK) {
        return embeddings.embed(query).flatMapMany(v -> store.search(tenantId, v, topK))
                .filter(s -> s.score() > 0);
    }

    /** Builds a prompt-ready context block, best chunks first, capped at {@code maxChars}. */
    public Mono<Context> buildContext(UUID tenantId, String query, int topK, int maxChars) {
        return retrieve(tenantId, query, topK).collectList().map(scored -> {
            StringBuilder sb = new StringBuilder();
            List<String> citations = new java.util.ArrayList<>();
            for (Scored s : scored) {
                String block = "[" + s.chunk().citation() + "]\n" + s.chunk().text() + "\n\n";
                if (sb.length() + block.length() > maxChars && !citations.isEmpty()) {
                    break;
                }
                sb.append(block);
                citations.add(s.chunk().citation());
            }
            return new Context(sb.toString().strip(), List.copyOf(citations));
        });
    }
}
