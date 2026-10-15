package ai.oenexa.search.service;

import ai.oenexa.search.domain.SearchRequest;
import ai.oenexa.search.domain.SearchResponse;

/**
 * Service port for indexing, querying, and retrieving semantic code and documentation.
 */
public interface SearchService {

    SearchResponse search(SearchRequest request);
}
