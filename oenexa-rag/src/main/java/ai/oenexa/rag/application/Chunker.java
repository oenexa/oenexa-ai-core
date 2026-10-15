package ai.oenexa.rag.application;

import java.util.ArrayList;
import java.util.List;

/** Splits text into overlapping windows, preferring paragraph/sentence boundaries. */
public final class Chunker {

    private final int maxChars;
    private final int overlap;

    public Chunker(int maxChars, int overlap) {
        if (maxChars <= 0 || overlap < 0 || overlap >= maxChars) {
            throw new IllegalArgumentException("require 0 <= overlap < maxChars");
        }
        this.maxChars = maxChars;
        this.overlap = overlap;
    }

    public List<String> split(String text) {
        String clean = text == null ? "" : text.strip();
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < clean.length()) {
            int end = Math.min(start + maxChars, clean.length());
            if (end < clean.length()) {
                end = boundary(clean, start, end);
            }
            String piece = clean.substring(start, end).strip();
            if (!piece.isEmpty()) {
                chunks.add(piece);
            }
            if (end >= clean.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return chunks;
    }

    private int boundary(String text, int start, int end) {
        int para = text.lastIndexOf("\n\n", end);
        if (para > start + maxChars / 2) {
            return para;
        }
        int sentence = text.lastIndexOf(". ", end);
        return sentence > start + maxChars / 2 ? sentence + 1 : end;
    }
}
