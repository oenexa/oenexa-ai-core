package ai.oenexa.search.web;

import ai.oenexa.search.domain.SearchRequest;
import ai.oenexa.search.domain.SearchResponse;
import ai.oenexa.search.service.SearchService;
import java.util.Objects;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = Objects.requireNonNull(searchService, "searchService must not be null");
    }

    @PostMapping("/query")
    public ResponseEntity<SearchResponse> query(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }
}
