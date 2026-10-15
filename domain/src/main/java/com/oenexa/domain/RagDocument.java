package com.oenexa.domain;

import java.util.List;

/**
 * Domain contracts for Retrieval Augmented Generation (RAG).
 */
public class RagDocument {

    public record DocumentInput(String title, String content, String repositoryId) {}

    public record Chunk(String id, String title, String text, int chunkIndex) {}

    public record SearchResult(String chunkId, String title, String snippet, double similarityScore) {}

    public record IngestResult(int chunksIndexed, List<String> chunkIds) {}
}
