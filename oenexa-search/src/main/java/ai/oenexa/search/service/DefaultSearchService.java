package ai.oenexa.search.service;

import ai.oenexa.search.domain.SearchRequest;
import ai.oenexa.search.domain.SearchResponse;
import ai.oenexa.search.domain.SearchResultItem;
import ai.oenexa.search.domain.SearchType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class DefaultSearchService implements SearchService {

    @Override
    public SearchResponse search(SearchRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        String query = request.query();
        SearchType type = request.searchType();
        int limit = request.limit();

        long startTime = System.currentTimeMillis();
        List<SearchResultItem> hits = new ArrayList<>();

        if (query.trim().isEmpty()) {
            return new SearchResponse(query, type, 0, List.of(), 1);
        }

        switch (type) {
            case FULL_TEXT -> {
                hits.add(new SearchResultItem(
                        "hit-ft-1",
                        "Class: " + query,
                        "public class " + query + " { /* Implementation */ }",
                        "src/main/java/com/oenexa/" + query + ".java",
                        0.95,
                        Map.of("module", "oenexa-core", "language", "java")
                ));
                hits.add(new SearchResultItem(
                        "hit-ft-2",
                        "Config: " + query,
                        "oenexa.service." + query.toLowerCase() + ".enabled=true",
                        "src/main/resources/application.yml",
                        0.82,
                        Map.of("module", "api", "type", "config")
                ));
            }
            case SEMANTIC_VECTOR -> {
                hits.add(new SearchResultItem(
                        "hit-vec-1",
                        "Semantic match for '" + query + "'",
                        "Embedded domain vector representation matching query intent.",
                        "vector://embeddings/" + query.hashCode(),
                        0.98,
                        Map.of("vectorDistance", 0.05, "model", "text-embedding-3-small")
                ));
            }
            case HYBRID -> {
                hits.add(new SearchResultItem(
                        "hit-hyb-1",
                        "Keyword & Vector Combined: " + query,
                        "Composite match leveraging inverted index and dense vector similarity.",
                        "repo://oenexa/" + query.toLowerCase(),
                        0.99,
                        Map.of("reciprocalRankFusion", 0.99, "source", "hybrid-index")
                ));
                hits.add(new SearchResultItem(
                        "hit-hyb-2",
                        "Secondary match for: " + query,
                        "Relevant architectural specification section.",
                        "docs/ARCHITECTURE.md",
                        0.88,
                        Map.of("section", "Hexagonal Architecture")
                ));
            }
            case WEB_RESEARCH -> {
                hits.add(new SearchResultItem(
                        "hit-web-1",
                        "Official Reference: " + query,
                        "Authoritative upstream documentation and architectural guidelines.",
                        "https://docs.oenexa.ai/research/" + query.toLowerCase(),
                        0.97,
                        Map.of("citationType", "official-doc", "verified", true)
                ));
            }
        }

        if (hits.size() > limit) {
            hits = hits.subList(0, limit);
        }

        long latency = Math.max(1, System.currentTimeMillis() - startTime);
        return new SearchResponse(query, type, hits.size(), hits, latency);
    }
}
