package com.oenexa.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.flywaydb.core.Flyway;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class InfrastructureConfig {

    // ---------- Flyway ----------
    @Bean(initMethod = "migrate")
    public Flyway flyway(org.springframework.core.env.Environment env) {
        return Flyway.configure()
                .dataSource(env.getProperty("spring.datasource.url"),
                        env.getProperty("spring.datasource.username"),
                        env.getProperty("spring.datasource.password"))
                .load();
    }

    // ---------- DataSource (PostgreSQL) ----------
    @Bean
    public DriverManagerDataSource dataSource(org.springframework.core.env.Environment env) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(env.getProperty("spring.datasource.url"));
        ds.setUsername(env.getProperty("spring.datasource.username"));
        ds.setPassword(env.getProperty("spring.datasource.password"));
        return ds;
    }

    // ---------- Redis ----------
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(org.springframework.core.env.Environment env) {
        return new LettuceConnectionFactory(env.getProperty("spring.redis.host"),
                Integer.parseInt(env.getProperty("spring.redis.port", "6379")));
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);
        return template;
    }

    // ---------- Kafka ----------
    @Bean
    public ProducerFactory<String, String> producerFactory(org.springframework.core.env.Environment env) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", env.getProperty("spring.kafka.bootstrap-servers"));
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }

    // ---------- Elasticsearch ----------
    @Bean
    public ElasticsearchTemplate elasticsearchTemplate(org.springframework.core.env.Environment env) {
        return new ElasticsearchTemplate(ElasticsearchConfiguration.withUrl(env.getProperty("spring.elasticsearch.uris")));
    }
}
