package com.oenexa.infrastructure;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Infrastructure Configuration for OENEXA AI Engineer.
 * Scans all JPA entities across com.oenexa and enables Spring Data JPA repositories.
 * Executes Flyway schema migrations on the active DataSource.
 * Configures Redis and Kafka conditionally based on environment activation.
 */
@Configuration
@EntityScan(basePackages = "com.oenexa")
@EnableJpaRepositories(basePackages = "com.oenexa.infrastructure.persistence.repository")
public class InfrastructureConfig {

    // ---------- Flyway Migration ----------
    @Bean
    @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = true)
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    // ---------- Redis (Conditional) ----------
    @Bean
    @ConditionalOnProperty(name = "oenexa.redis.enabled", havingValue = "true")
    public LettuceConnectionFactory redisConnectionFactory(Environment env) {
        String host = env.getProperty("spring.data.redis.host", "localhost");
        int port = Integer.parseInt(env.getProperty("spring.data.redis.port", "6379"));
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @ConditionalOnProperty(name = "oenexa.redis.enabled", havingValue = "true")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        return template;
    }

    // ---------- Kafka (Conditional) ----------
    @Bean
    @ConditionalOnProperty(name = "oenexa.kafka.enabled", havingValue = "true")
    public ProducerFactory<String, String> producerFactory(Environment env) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @ConditionalOnProperty(name = "oenexa.kafka.enabled", havingValue = "true")
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }
}
