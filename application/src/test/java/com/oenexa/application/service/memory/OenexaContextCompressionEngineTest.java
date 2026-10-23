package com.oenexa.application.service.memory;

import com.oenexa.application.port.*;
import com.oenexa.application.service.agent.*;
import com.oenexa.application.service.auth.*;
import com.oenexa.application.service.chat.*;
import com.oenexa.application.service.code.*;
import com.oenexa.application.service.knowledge.*;
import com.oenexa.application.service.memory.*;
import com.oenexa.application.service.orchestration.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OenexaContextCompressionEngine Zero-Mock Unit Tests")
class OenexaContextCompressionEngineTest {

    private final OenexaContextCompressionEngine engine = new OenexaContextCompressionEngine();

    @Test
    @DisplayName("Should compress and decompress context with deduplication and token pruning")
    void shouldCompressAndDecompressContext() {
        String input = """
                package com.oenexa.auth;
                
                import java.util.List;
                import java.util.List;
                
                public class TokenService {
                    // Method with redundant duplicate lines
                    public String generate() {
                        return "token-xyz";
                    }
                    public String generate() {
                        return "token-xyz";
                    }
                }
                """.repeat(20);

        var result = engine.compressContext(input);
        assertNotNull(result);
        assertNotNull(result.compressedBytes());
        assertTrue(result.compressedBytes().length < input.length());

        var metrics = result.metrics();
        assertTrue(metrics.compressionRatioPercent() > 50.0);
        assertTrue(metrics.deduplicatedItemsCount() > 0);

        String decompressed = engine.decompressContext(result.compressedBytes());
        assertTrue(decompressed.contains("TokenService"));
        assertTrue(decompressed.contains("token-xyz"));
    }

    @Test
    @DisplayName("Should handle empty and blank inputs safely")
    void shouldHandleEmptyInputs() {
        var result = engine.compressContext("");
        assertEquals(0, result.compressedBytes().length);
        assertEquals(0, result.metrics().originalSizeBytes());

        assertEquals("", engine.decompressContext(new byte[0]));
        assertEquals("", engine.decompressContext(null));
    }
}

