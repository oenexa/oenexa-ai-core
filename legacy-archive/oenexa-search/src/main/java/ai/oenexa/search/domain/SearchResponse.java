package ai.oenexa.search.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result payload containing ranked hits and execution latency.
 */
public record SearchResponse(
        String query,
        SearchType searchType,
        int totalHits,
        List<SearchResultItem> hits,
        long latencyMs
) {
    public SearchResponse {
        Objects.requireNonNull(query, "query must not be null");
        Objects.requireNonNull(searchType, "searchType must not be null");
        if (hits == null) {
            hits = List.of();
        }
    }

    public boolean hasHits() {
        return !hits.isEmpty();
    }
}
