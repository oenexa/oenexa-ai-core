package ai.oenexa.search.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Individual search hit with relevance score and metadata.
 */
public record SearchResultItem(
        String id,
        String title,
        String snippet,
        String sourceUri,
        double relevanceScore,
        Map<String, Object> metadata
) {
    public SearchResultItem {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(title, "title must not be null");
        if (snippet == null) {
            snippet = "";
        }
        if (sourceUri == null) {
            sourceUri = "internal://oenexa";
        }
        if (metadata == null) {
            metadata = Map.of();
        }
    }
}
