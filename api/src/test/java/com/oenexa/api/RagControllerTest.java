package com.oenexa.api;

import com.oenexa.api.controller.*;
import com.oenexa.api.exception.*;
import com.oenexa.application.RagService;
import com.oenexa.domain.RagDocument.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RagController Zero-Mock Unit Tests")
class RagControllerTest {

    private final RagService ragService = new RagService();
    private final RagController controller = new RagController(ragService);

    @Test
    @DisplayName("Should ingest valid document")
    void shouldIngestDocument() {
        DocumentInput input = new DocumentInput("Hexagonal Architecture", "Hexagonal architecture decouples domain logic.", "repo-1");
        ResponseEntity<IngestResult> response = controller.ingest(input);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().chunksIndexed());
    }

    @Test
    @DisplayName("Should reject null title or null content with 400 Bad Request")
    void shouldRejectInvalidDocument() {
        ResponseEntity<IngestResult> nullTitle = controller.ingest(new DocumentInput(null, "Content", "repo-1"));
        assertEquals(HttpStatus.BAD_REQUEST, nullTitle.getStatusCode());

        ResponseEntity<IngestResult> nullContent = controller.ingest(new DocumentInput("Title", null, "repo-1"));
        assertEquals(HttpStatus.BAD_REQUEST, nullContent.getStatusCode());
    }

    @Test
    @DisplayName("Should query indexed documents")
    void shouldQueryDocuments() {
        controller.ingest(new DocumentInput("Microservices", "Event-driven architecture with Kafka streams.", "repo-2"));

        ResponseEntity<List<SearchResult>> response = controller.query("Kafka streams", 5);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());
    }
}
