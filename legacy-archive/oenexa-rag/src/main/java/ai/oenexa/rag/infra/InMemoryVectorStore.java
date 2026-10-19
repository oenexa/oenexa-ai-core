package ai.oenexa.rag.infra;

import ai.oenexa.rag.domain.Chunk;
import ai.oenexa.rag.domain.Ports.Scored;
import ai.oenexa.rag.domain.Ports.VectorStorePort;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Brute-force cosine search; tenant partitions are separate lists so tenants can never see each other. */
public class InMemoryVectorStore implements VectorStorePort {

    private final Map<UUID, List<Chunk>> partitions = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> upsert(UUID tenantId, List<Chunk> chunks) {
        return Mono.fromRunnable(() -> {
            List<Chunk> partition = partitions.computeIfAbsent(tenantId, t -> new CopyOnWriteArrayList<>());
            for (Chunk c : chunks) {
                partition.removeIf(e -> e.documentId().equals(c.documentId()) && e.index() == c.index());
                partition.add(c);
            }
        });
    }

    @Override
    public Flux<Scored> search(UUID tenantId, float[] query, int topK) {
        return Flux.defer(() -> Flux.fromStream(partitions.getOrDefault(tenantId, List.of()).stream()
                .map(c -> new Scored(c, cosine(query, c.embedding())))
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(topK)));
    }

    static double cosine(float[] a, float[] b) {
        double dot = 0;
        double na = 0;
        double nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return na == 0 || nb == 0 ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}
