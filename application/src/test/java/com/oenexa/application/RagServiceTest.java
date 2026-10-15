package com.oenexa.application;

import com.oenexa.domain.RagDocument.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RagService Zero-Mock Unit Tests")
class RagServiceTest {

    private final RagService ragService = new RagService();

    @Test
    @DisplayName("Should ingest document and split text into chunks when words exceed maxWords")
    void shouldIngestDocumentWithTrailingWords() {
        // Create content > 300 words with remainder to test both count >= maxWords and trailing !current.isEmpty()
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < 350; i++) {
            contentBuilder.append("word").append(i).append(" ");
        }

        DocumentInput input = new DocumentInput("Architecture Guide", contentBuilder.toString(), "repo-1");
        IngestResult result = ragService.ingest(input);

        assertNotNull(result);
        assertEquals(2, result.chunksIndexed());
        assertEquals(2, result.chunkIds().size());
    }

    @Test
    @DisplayName("Should ingest document whose word count is exactly a multiple of maxWords (empty trailing builder)")
    void shouldIngestDocumentWithExactMultipleWords() {
        // Exactly 300 words -> chunk 1 added, current becomes new StringBuilder() and remains empty
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            contentBuilder.append("exact").append(i).append(" ");
        }

        DocumentInput input = new DocumentInput("Exact Multiple", contentBuilder.toString(), "repo-exact");
        IngestResult result = ragService.ingest(input);

        assertNotNull(result);
        assertEquals(1, result.chunksIndexed());
    }

    @Test
    @DisplayName("Should ingest short document into a single chunk and perform search")
    void shouldIngestAndSearch() {
        DocumentInput input = new DocumentInput("Security Policy", "Authentication uses signed JWT tokens with HMAC SHA256 algorithm and rotating keys.", "repo-sec");
        IngestResult result = ragService.ingest(input);

        assertEquals(1, result.chunksIndexed());

        List<SearchResult> results = ragService.search("signed JWT tokens", 5);
        assertFalse(results.isEmpty());
        SearchResult top = results.get(0);
        assertEquals("Security Policy", top.title());
        assertTrue(top.snippet().contains("Authentication uses signed JWT tokens"));
        assertTrue(top.similarityScore() > 0.0);
    }

    @Test
    @DisplayName("Should return empty search results when query is blank, null, or has no matches")
    void shouldHandleEmptyOrNoMatchSearch() {
        RagService emptyRag = new RagService();
        assertTrue(emptyRag.search("test", 5).isEmpty());

        DocumentInput input = new DocumentInput("Doc", "Clean architecture principles", "repo-arch");
        ragService.ingest(input);

        assertTrue(ragService.search(null, 5).isEmpty());
        assertTrue(ragService.search("", 5).isEmpty());
        assertTrue(ragService.search("   ", 5).isEmpty());
        assertTrue(ragService.search("unmatchedkeywordxyz", 5).isEmpty());
    }

    @Test
    @DisplayName("Should truncate long snippet to 160 chars + ellipsis")
    void shouldTruncateLongSnippet() {
        StringBuilder longText = new StringBuilder("KeywordSearchTarget ");
        for (int i = 0; i < 50; i++) {
            longText.append("additional context information ");
        }

        DocumentInput input = new DocumentInput("Long Document", longText.toString(), "repo-long");
        ragService.ingest(input);

        List<SearchResult> results = ragService.search("KeywordSearchTarget", 1);
        assertFalse(results.isEmpty());
        SearchResult match = results.get(0);
        assertTrue(match.snippet().endsWith("..."));
        assertEquals(163, match.snippet().length());
    }

    @Test
    @DisplayName("Should throw NullPointerException on null document input or null content")
    void shouldThrowNpeOnInvalidInput() {
        assertThrows(NullPointerException.class, () -> ragService.ingest(null));
        assertThrows(NullPointerException.class, () -> ragService.ingest(new DocumentInput("Title", null, "repo-1")));
    }
}
