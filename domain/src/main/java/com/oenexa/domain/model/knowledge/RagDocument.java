package com.oenexa.domain.model.knowledge;

import com.oenexa.domain.exception.*;
import com.oenexa.domain.model.agent.*;
import com.oenexa.domain.model.auth.*;
import com.oenexa.domain.model.chat.*;
import com.oenexa.domain.model.code.*;
import com.oenexa.domain.model.cognitive.*;
import com.oenexa.domain.model.common.*;
import com.oenexa.domain.model.knowledge.*;
import com.oenexa.domain.model.memory.*;
import com.oenexa.domain.model.orchestration.*;

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

