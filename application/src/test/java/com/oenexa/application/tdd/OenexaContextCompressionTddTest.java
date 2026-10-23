package com.oenexa.application.tdd;

import com.oenexa.application.service.memory.OenexaContextCompressionEngine;
import com.oenexa.application.service.memory.OenexaContextCompressionEngine.CompressionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Contract Specification for OENEXA Context Compression Engine.
 * Verifies compression ratio bounds, lossless decompression of AST symbols, and deduplication.
 */
@Tag("tdd")
@DisplayName("OENEXA Context Compression Engine - TDD Specifications")
public class OenexaContextCompressionTddTest {

    private OenexaContextCompressionEngine engine;

    @BeforeEach
    void setUp() {
        engine = new OenexaContextCompressionEngine();
    }

    @Test
    @DisplayName("TDD-1: Compression Ratio Invariant on Redundant Context (>= 50%)")
    void testCompressionRatioInvariant() {
        // Arrange: Generate repetitive context
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("package com.oenexa.service;\n");
            sb.append("// Auto-generated license header block\n");
            sb.append("// Copyright 2026 Oenexa AI Corporation\n");
        }
        sb.append("public class TargetService { public void run() {} }\n");

        // Act
        CompressionResult result = engine.compressContext(sb.toString());

        // Assert: Red-Green Invariant
        assertNotNull(result);
        assertTrue(result.metrics().compressionRatioPercent() >= 50.0,
                "Compression ratio must achieve at least 50% on redundant prompts");
        assertTrue(result.metrics().compressedSizeBytes() < result.metrics().originalSizeBytes());
    }

    @Test
    @DisplayName("TDD-2: AST Symbol Lossless Retention Invariant")
    void testAstSymbolLosslessRetention() {
        // Arrange
        String sourceCode = """
                public interface DataMovementContract {
                    byte[] compressPayload(String payload);
                    String decompressPayload(byte[] bytes);
                }
                """;

        // Act
        CompressionResult compressed = engine.compressContext(sourceCode);
        String decompressed = engine.decompressContext(compressed.compressedBytes());

        // Assert
        assertTrue(decompressed.contains("DataMovementContract"), "Interface name must be preserved");
        assertTrue(decompressed.contains("compressPayload"), "Method name 1 must be preserved");
        assertTrue(decompressed.contains("decompressPayload"), "Method name 2 must be preserved");
    }

    @Test
    @DisplayName("TDD-3: Graceful Handling of Empty or Null Context")
    void testEmptyOrNullContextContract() {
        CompressionResult nullResult = engine.compressContext(null);
        assertNotNull(nullResult);
        assertEquals(0, nullResult.compressedBytes().length);
        assertEquals(0, nullResult.metrics().originalSizeBytes());

        CompressionResult blankResult = engine.compressContext("   ");
        assertNotNull(blankResult);
        assertEquals(0, blankResult.compressedBytes().length);
    }
}

