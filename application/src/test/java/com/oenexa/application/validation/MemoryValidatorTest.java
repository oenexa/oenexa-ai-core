package com.oenexa.application.validation;

import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemoryValidator Zero-Mock Unit Tests")
class MemoryValidatorTest {

    private final MemoryValidator validator = new MemoryValidator();

    @Test
    @DisplayName("Should accept valid memory record and lookup")
    void shouldAcceptValidMemory() {
        HierarchicalMemoryRecord record = new HierarchicalMemoryRecord(
                "mem-1", "workspace-1", "user-1",
                MemoryLayerTier.L2_JVM_HEAP_RAM, "ast:Solution.java",
                "public class Solution {}", List.of(0.1f, 0.2f),
                1, Instant.now(), 256, false
        );
        assertDoesNotThrow(() -> validator.validateMemoryRecord(record));
        assertDoesNotThrow(() -> validator.validateKeyAndTier("ast:Solution.java", MemoryLayerTier.L2_JVM_HEAP_RAM));
    }

    @Test
    @DisplayName("Should reject blank memory record fields")
    void shouldRejectInvalidMemoryRecord() {
        assertThrows(OenexaValidationException.class, () -> validator.validateMemoryRecord(null));
        HierarchicalMemoryRecord blankKeyRecord = new HierarchicalMemoryRecord(
                "mem-2", "workspace-1", "user-1",
                MemoryLayerTier.L3_REDIS_DISTRIBUTED, "",
                "valid content", List.of(), 1, Instant.now(), 100, false
        );
        assertThrows(OenexaValidationException.class, () -> validator.validateMemoryRecord(blankKeyRecord));
    }

    @Test
    @DisplayName("Should reject blank lookup key or null tier")
    void shouldRejectInvalidLookup() {
        assertThrows(OenexaValidationException.class, () -> validator.validateKeyAndTier("", MemoryLayerTier.L1_CPU_CACHE));
        assertThrows(OenexaValidationException.class, () -> validator.validateKeyAndTier("key", null));
    }
}
