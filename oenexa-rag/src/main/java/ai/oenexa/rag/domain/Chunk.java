package ai.oenexa.rag.domain;

import java.util.UUID;

public record Chunk(UUID documentId, int index, String source, String text, float[] embedding) {

    public Chunk withEmbedding(float[] vector) {
        return new Chunk(documentId, index, source, text, vector);
    }

    public String citation() {
        return source + "#" + index;
    }
}
