package ai.oenexa.search.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Request payload for querying code, documentation, and external research.
 */
public record SearchRequest(
        String query,
        SearchType searchType,
        int limit,
        Map<String, String> filters
) {
    public SearchRequest {
        Objects.requireNonNull(query, "query must not be null");
        if (searchType == null) {
            searchType = SearchType.HYBRID;
        }
        if (limit <= 0) {
            limit = 10;
        }
        if (filters == null) {
            filters = Map.of();
        }
    }
}
