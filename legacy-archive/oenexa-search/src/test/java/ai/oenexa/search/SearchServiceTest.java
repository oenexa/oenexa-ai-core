package ai.oenexa.search;

import ai.oenexa.search.domain.SearchRequest;
import ai.oenexa.search.domain.SearchResponse;
import ai.oenexa.search.domain.SearchResultItem;
import ai.oenexa.search.domain.SearchType;
import ai.oenexa.search.service.DefaultSearchService;
import ai.oenexa.search.web.SearchController;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OENEXA Search Service & Controller Tests (Zero-Mock Policy)")
class SearchServiceTest {

    private DefaultSearchService searchService;
    private SearchController searchController;

    @BeforeEach
    void setUp() {
        searchService = new DefaultSearchService();
        searchController = new SearchController(searchService);
    }

    @Test
    @DisplayName("SearchModule metadata and initialization")
    void testSearchModuleMetadata() {
        SearchModule module = new SearchModule();
        assertEquals("search", module.id());
        assertEquals("OENEXA Search", module.displayName());
        assertTrue(module.capabilities().contains("full-text"));
    }

    @Test
    @DisplayName("SearchType enum invariants")
    void testEnumInvariants() {
        assertEquals(4, SearchType.values().length);
        assertEquals(SearchType.FULL_TEXT, SearchType.valueOf("FULL_TEXT"));
        assertEquals(SearchType.HYBRID, SearchType.valueOf("HYBRID"));
    }

    @Test
    @DisplayName("Domain records null safety and fallback invariants")
    void testDomainRecordsEdgeCases() {
        SearchResultItem item = new SearchResultItem("h-1", "Title", null, null, 0.9, null);
        assertEquals("", item.snippet());
        assertEquals("internal://oenexa", item.sourceUri());
        assertTrue(item.metadata().isEmpty());

        assertThrows(NullPointerException.class, () -> new SearchResultItem(null, "Title", "", "", 1.0, Map.of()));
        assertThrows(NullPointerException.class, () -> new SearchResultItem("h-1", null, "", "", 1.0, Map.of()));

        SearchRequest req = new SearchRequest("query", null, 0, null);
        assertEquals(SearchType.HYBRID, req.searchType());
        assertEquals(10, req.limit());
        assertTrue(req.filters().isEmpty());

        assertThrows(NullPointerException.class, () -> new SearchRequest(null, SearchType.HYBRID, 10, Map.of()));

        SearchResponse res = new SearchResponse("q", SearchType.HYBRID, 0, null, 5);
        assertTrue(res.hits().isEmpty());
        assertFalse(res.hasHits());

        assertThrows(NullPointerException.class, () -> new SearchResponse(null, SearchType.HYBRID, 0, List.of(), 5));
        assertThrows(NullPointerException.class, () -> new SearchResponse("q", null, 0, List.of(), 5));
    }

    @Test
    @DisplayName("Empty query returns 0 hits")
    void testEmptyQuery() {
        SearchRequest req = new SearchRequest("   ", SearchType.FULL_TEXT, 10, Map.of());
        SearchResponse response = searchService.search(req);

        assertEquals(0, response.totalHits());
        assertFalse(response.hasHits());
    }

    @Test
    @DisplayName("Full-text search finds indexed source code matches")
    void testFullTextSearch() {
        SearchRequest req = new SearchRequest("UserService", SearchType.FULL_TEXT, 10, Map.of());
        SearchResponse response = searchService.search(req);

        assertEquals(SearchType.FULL_TEXT, response.searchType());
        assertTrue(response.hasHits());
        assertEquals(2, response.hits().size());
        assertTrue(response.hits().get(0).title().contains("UserService"));
    }

    @Test
    @DisplayName("Semantic vector search computes embedded representations")
    void testSemanticVectorSearch() {
        SearchRequest req = new SearchRequest("authentication token lifecycle", SearchType.SEMANTIC_VECTOR, 5, Map.of());
        SearchResponse response = searchService.search(req);

        assertEquals(SearchType.SEMANTIC_VECTOR, response.searchType());
        assertTrue(response.hasHits());
        assertEquals(1, response.hits().size());
        assertTrue(response.hits().get(0).sourceUri().contains("vector://"));
    }

    @Test
    @DisplayName("Hybrid search combines keyword and vector relevance with limit truncation")
    void testHybridSearchWithLimit() {
        SearchRequest req = new SearchRequest("Hexagonal Architecture", SearchType.HYBRID, 1, Map.of());
        SearchResponse response = searchService.search(req);

        assertEquals(SearchType.HYBRID, response.searchType());
        assertEquals(1, response.hits().size());
        assertTrue(response.hits().get(0).title().contains("Keyword & Vector Combined"));
    }

    @Test
    @DisplayName("Web research mode retrieves authoritative citations")
    void testWebResearch() {
        SearchRequest req = new SearchRequest("Spring Boot 4 Virtual Threads", SearchType.WEB_RESEARCH, 5, Map.of());
        SearchResponse response = searchService.search(req);

        assertEquals(SearchType.WEB_RESEARCH, response.searchType());
        assertTrue(response.hasHits());
        assertTrue(response.hits().get(0).sourceUri().startsWith("https://"));
    }

    @Test
    @DisplayName("SearchController query endpoint returns valid HTTP response")
    void testSearchController() {
        SearchRequest req = new SearchRequest("HealthController", SearchType.FULL_TEXT, 5, Map.of());
        ResponseEntity<SearchResponse> resp = searchController.query(req);

        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().hasHits());
    }
}
