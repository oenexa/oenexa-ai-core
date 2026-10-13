package com.oenexa.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InfrastructureConfig {

    // ---------- Flyway ----------
    @Bean(initMethod = "migrate")
    public Flyway flyway(Environment env) {
        String url = env.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/oenexa");
        String username = env.getProperty("spring.datasource.username", "postgres");
        String password = env.getProperty("spring.datasource.password", "postgres");
        return Flyway.configure()
                .dataSource(url, username, password)
                .load();
    }

    // ---------- DataSource (PostgreSQL) ----------
    @Bean
    public DriverManagerDataSource dataSource(Environment env) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(env.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/oenexa"));
        ds.setUsername(env.getProperty("spring.datasource.username", "postgres"));
        ds.setPassword(env.getProperty("spring.datasource.password", "postgres"));
        return ds;
    }

    // ---------- Redis ----------
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(Environment env) {
        String host = env.getProperty("spring.redis.host", "localhost");
        int port = Integer.parseInt(env.getProperty("spring.redis.port", "6379"));
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        return template;
    }

    // ---------- Kafka ----------
    @Bean
    public ProducerFactory<String, String> producerFactory(Environment env) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", env.getProperty("spring.kafka.bootstrap-servers", "localhost:9092"));
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }
}
