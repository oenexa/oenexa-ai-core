package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Enterprise Configuration Layer for Memory Hierarchy Subsystem.
 * Configures cache retention periods, tier capacity limits, and smart eviction weights.
 */
@Configuration
public class MemoryDomainConfig {

    public record MemoryProperties(
            int maxRamEntries,
            int maxNvmeMmapMb,
            double evictionFrequencyWeight,
            double evictionRecencyWeight,
            long decayCycleIntervalSeconds
    ) {
        public MemoryProperties() {
            this(10_000, 2048, 0.4, 0.6, 60L);
        }
    }

    @Bean
    public MemoryProperties memoryProperties() {
        return new MemoryProperties();
    }
}
