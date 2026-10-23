package com.oenexa.application.vnext;

import com.oenexa.domain.model.cognitive.OenexaVNextDomain.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Phase 5: CapabilityGenerator
 * Autonomously synthesizes interface declarations, immutable records,
 * and zero-mock test templates for missing capabilities.
 */
@Component
public class CapabilityGenerator {

    public CapabilityGenerationArtifact generateCapability(MissingCapabilityDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor must not be null");

        String interfaceCode = String.format("""
                package %s;
                
                public interface %sPort {
                    boolean executeAnalysis(String payload);
                }
                """, descriptor.targetPackage(), descriptor.capabilityName());

        String implCode = String.format("""
                package %s;
                
                import java.util.Objects;
                
                public record %sAdapter() implements %sPort {
                    @Override
                    public boolean executeAnalysis(String payload) {
                        Objects.requireNonNull(payload, "payload must not be null");
                        return !payload.isBlank();
                    }
                }
                """, descriptor.targetPackage(), descriptor.capabilityName(), descriptor.capabilityName());

        String testCode = String.format("""
                package %s;
                
                import org.junit.jupiter.api.Test;
                import static org.junit.jupiter.api.Assertions.*;
                
                class %sAdapterTest {
                    @Test
                    void shouldAnalyzeSuccessfully() {
                        var adapter = new %sAdapter();
                        assertTrue(adapter.executeAnalysis("valid payload"));
                        assertThrows(NullPointerException.class, () -> adapter.executeAnalysis(null));
                    }
                }
                """, descriptor.targetPackage(), descriptor.capabilityName(), descriptor.capabilityName());

        return new CapabilityGenerationArtifact(
                descriptor.capabilityName(),
                interfaceCode,
                implCode,
                testCode,
                true,
                true
        );
    }
}

