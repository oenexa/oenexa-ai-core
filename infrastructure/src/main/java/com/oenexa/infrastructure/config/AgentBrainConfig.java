package com.oenexa.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Enterprise Configuration Layer for Autonomous Agent Brain & Swarm Orchestration.
 * Configures Java 27 Virtual Thread Executors for high-concurrency cognitive processing.
 */
@Configuration
public class AgentBrainConfig {

    public record AgentProperties(
            int maxParallelAgents,
            int maxDelegationDepth,
            long goalTimeoutSeconds
    ) {
        public AgentProperties() {
            this(16, 10, 300L);
        }
    }

    @Bean
    public AgentProperties agentProperties() {
        return new AgentProperties();
    }

    @Bean(name = "agentVirtualThreadExecutor", destroyMethod = "close")
    public ExecutorService agentVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
