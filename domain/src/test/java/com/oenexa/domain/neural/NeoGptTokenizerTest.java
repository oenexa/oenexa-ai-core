package com.oenexa.domain.neural;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeoGptTokenizer Zero-Mock Unit Tests")
class NeoGptTokenizerTest {

    private final NeoGptTokenizer tokenizer = new NeoGptTokenizer();

    @Test
    @DisplayName("Should encode and decode Java 27 code strings with 100% fidelity")
    void testEncodeDecodeRoundTrip() {
        String code = "public record Solution(String id, boolean active) {}";
        List<Integer> tokens = tokenizer.encode(code);

        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());

        String decoded = tokenizer.decode(tokens);
        assertEquals(code, decoded);
    }

    @Test
    @DisplayName("Should encode and decode special tokens accurately")
    void testSpecialTokens() {
        String prompt = "<|im_start|>user\nSynthesize reactive service<|im_end|>";
        List<Integer> tokens = tokenizer.encode(prompt);

        assertNotNull(tokens);
        assertTrue(tokens.contains(NeoGptTokenizer.TOKEN_IM_START));
        assertTrue(tokens.contains(NeoGptTokenizer.TOKEN_IM_END));

        String decoded = tokenizer.decode(tokens);
        assertEquals(prompt, decoded);
    }

    @Test
    @DisplayName("Should handle empty and null inputs safely")
    void testEmptyAndNullSafety() {
        assertTrue(tokenizer.encode(null).isEmpty());
        assertTrue(tokenizer.encode("").isEmpty());

        assertEquals("", tokenizer.decode(null));
        assertEquals("", tokenizer.decode(List.of()));
    }

    @Test
    @DisplayName("Should verify vocabulary and token lookup")
    void testVocabLookup() {
        assertTrue(tokenizer.getVocabSize() >= 256);
        assertTrue(tokenizer.hasToken("public"));
        assertTrue(tokenizer.hasToken("<|thought_start|>"));

        assertEquals(NeoGptTokenizer.TOKEN_THOUGHT_START, tokenizer.getTokenId("<|thought_start|>"));
        assertEquals(NeoGptTokenizer.TOKEN_UNKNOWN, tokenizer.getTokenId("non_existent_unique_token_xyz"));
    }
}
