package ai.oenexa.rag.infra;

import ai.oenexa.rag.domain.Ports.EmbeddingPort;
import java.util.Locale;
import reactor.core.publisher.Mono;

/**
 * Deterministic feature-hashing embedder (bag of words -> unit vector). Lets the pipeline run and be
 * tested without a provider; swap for a Spring AI EmbeddingModel adapter in production.
 */
public class HashEmbeddingAdapter implements EmbeddingPort {

    private final int dimensions;

    public HashEmbeddingAdapter(int dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public Mono<float[]> embed(String text) {
        return Mono.fromSupplier(() -> {
            float[] v = new float[dimensions];
            for (String token : text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+")) {
                if (!token.isEmpty()) {
                    v[Math.floorMod(token.hashCode(), dimensions)] += 1f;
                }
            }
            double norm = 0;
            for (float x : v) {
                norm += x * x;
            }
            norm = Math.sqrt(norm);
            if (norm > 0) {
                for (int i = 0; i < v.length; i++) {
                    v[i] /= (float) norm;
                }
            }
            return v;
        });
    }
}
