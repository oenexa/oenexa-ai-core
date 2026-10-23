package com.oenexa.application.validation;

import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.HierarchicalMemoryRecord;
import com.oenexa.domain.EnterpriseMemoryHierarchyDomain.MemoryLayerTier;
import com.oenexa.domain.OenexaExceptions.OenexaValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enterprise Validation Layer for Memory Engine Domain.
 * Validates memory keys, hierarchical tier constraints, and payload properties.
 */
@Component
public class MemoryValidator {

    public void validateMemoryRecord(HierarchicalMemoryRecord record) {
        if (record == null) {
            throw new OenexaValidationException("Memory record must not be null");
        }
        List<String> violations = new ArrayList<>();
        if (record.memoryKey() == null || record.memoryKey().trim().isBlank()) {
            violations.add("memoryKey must not be blank");
        }
        if (record.layer() == null) {
            violations.add("layer must not be null");
        }
        if (record.content() == null || record.content().trim().isBlank()) {
            violations.add("content must not be blank");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Memory record validation failed", violations);
        }
    }

    public void validateKeyAndTier(String key, MemoryLayerTier tier) {
        List<String> violations = new ArrayList<>();
        if (key == null || key.trim().isBlank()) {
            violations.add("memory key must not be blank");
        }
        if (tier == null) {
            violations.add("target memory tier must not be null");
        }
        if (!violations.isEmpty()) {
            throw new OenexaValidationException("Memory lookup validation failed", violations);
        }
    }
}
