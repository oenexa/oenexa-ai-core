package ai.oenexa.rag;

import ai.oenexa.common.module.PlatformModule;
import ai.oenexa.rag.application.Chunker;
import ai.oenexa.rag.application.RagService;
import ai.oenexa.rag.domain.Ports.EmbeddingPort;
import ai.oenexa.rag.domain.Ports.VectorStorePort;
import ai.oenexa.rag.infra.HashEmbeddingAdapter;
import ai.oenexa.rag.infra.InMemoryVectorStore;
import ai.oenexa.rag.web.RagController;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RagController.class)
public class RagModule implements PlatformModule {

    @Bean
    @ConditionalOnMissingBean
    EmbeddingPort embeddingPort(@Value("${oenexa.rag.dimensions:256}") int dimensions) {
        return new HashEmbeddingAdapter(dimensions);
    }

    @Bean
    @ConditionalOnMissingBean
    VectorStorePort vectorStorePort() {
        return new InMemoryVectorStore();
    }

    @Bean
    RagService ragService(EmbeddingPort embeddings, VectorStorePort store,
                          @Value("${oenexa.rag.chunk-size:1200}") int chunkSize,
                          @Value("${oenexa.rag.chunk-overlap:150}") int overlap) {
        return new RagService(new Chunker(chunkSize, overlap), embeddings, store);
    }

    @Override
    public String id() {
        return "rag";
    }

    @Override
    public String displayName() {
        return "OENEXA RAG";
    }

    @Override
    public String description() {
        return "Chunking, embedding, tenant-partitioned vector search and cited context construction.";
    }

    @Override
    public List<String> capabilities() {
        return List.of("ingestion", "embedding", "vector-search", "context-construction", "citations");
    }

    @Override
    public ModuleStatus status() {
        return ModuleStatus.ACTIVE;
    }
}
