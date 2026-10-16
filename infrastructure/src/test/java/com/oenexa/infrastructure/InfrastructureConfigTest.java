package com.oenexa.infrastructure;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InfrastructureConfig Zero-Mock Unit Tests")
class InfrastructureConfigTest {

    private final InfrastructureConfig config = new InfrastructureConfig();

    static class StubDataSource implements DataSource {
        @Override public Connection getConnection() { return null; }
        @Override public Connection getConnection(String username, String password) { return null; }
        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(PrintWriter out) {}
        @Override public void setLoginTimeout(int seconds) {}
        @Override public int getLoginTimeout() { return 0; }
        @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { throw new SQLFeatureNotSupportedException(); }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }

    @Test
    @DisplayName("Should configure and load Flyway without throwing exception when datasource is provided")
    void shouldConfigureFlyway() {
        // Flyway configuration test with stubbed DataSource (verifies configuration bean factory method)
        // Using an H2 in-memory URL via DriverManagerDataSource or basic Stub
        org.springframework.jdbc.datasource.DriverManagerDataSource ds =
                new org.springframework.jdbc.datasource.DriverManagerDataSource("jdbc:h2:mem:infratest;DB_CLOSE_DELAY=-1;MODE=PostgreSQL", "sa", "");
        
        Flyway flyway = config.flyway(ds);
        assertNotNull(flyway);
        assertNotNull(flyway.getConfiguration());
    }

    @Test
    @DisplayName("Should create Redis connection factory and template with environment properties")
    void shouldCreateRedisBeans() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.data.redis.host", "127.0.0.1");
        env.setProperty("spring.data.redis.port", "6380");

        LettuceConnectionFactory cf = config.redisConnectionFactory(env);
        assertNotNull(cf);
        assertEquals("127.0.0.1", cf.getHostName());
        assertEquals(6380, cf.getPort());

        RedisTemplate<String, Object> template = config.redisTemplate(cf);
        assertNotNull(template);
        assertSame(cf, template.getConnectionFactory());
    }

    @Test
    @DisplayName("Should create Redis connection factory with default properties when absent")
    void shouldCreateRedisBeansWithDefaults() {
        MockEnvironment env = new MockEnvironment();

        LettuceConnectionFactory cf = config.redisConnectionFactory(env);
        assertNotNull(cf);
        assertEquals("localhost", cf.getHostName());
        assertEquals(6379, cf.getPort());
    }

    @Test
    @DisplayName("Should create Kafka producer factory and template with environment properties")
    void shouldCreateKafkaBeans() {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("spring.kafka.bootstrap-servers", "kafka-broker:9092");

        ProducerFactory<String, String> pf = config.producerFactory(env);
        assertNotNull(pf);
        assertEquals("kafka-broker:9092", pf.getConfigurationProperties().get("bootstrap.servers"));

        KafkaTemplate<String, String> template = config.kafkaTemplate(pf);
        assertNotNull(template);
        assertSame(pf, template.getProducerFactory());
    }

    @Test
    @DisplayName("Should create Kafka producer factory with default properties when absent")
    void shouldCreateKafkaBeansWithDefaults() {
        MockEnvironment env = new MockEnvironment();

        ProducerFactory<String, String> pf = config.producerFactory(env);
        assertNotNull(pf);
        assertEquals("localhost:9092", pf.getConfigurationProperties().get("bootstrap.servers"));
    }
}
