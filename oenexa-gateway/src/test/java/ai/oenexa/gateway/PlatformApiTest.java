package ai.oenexa.gateway;

import ai.oenexa.OenexaApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;

/** End-to-end through the real security chain; persistence autoconfig is excluded (no DB needed). */
@SpringBootTest(classes = OenexaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "oenexa.auth.jwt-secret=integration-test-secret-integration-test-secret",
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration,"
                        + "org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration"
        })
@AutoConfigureWebTestClient
class PlatformApiTest {

    @Autowired
    WebTestClient client;

    private String registerAndGetToken(String email) {
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = client.post().uri("/api/v1/auth/register")
                .bodyValue("{\"email\":\"" + email + "\",\"password\":\"correct-horse-battery\"}")
                .header("Content-Type", "application/json")
                .exchange().expectStatus().isCreated()
                .expectBody(java.util.Map.class).returnResult().getResponseBody();
        return (String) body.get("accessToken");
    }

    @Test
    void modulesEndpointIsPublicAndReportsActiveBeforePlanned() {
        client.get().uri("/api/v1/modules").exchange().expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].status").isEqualTo("ACTIVE")
                .jsonPath("$.length()").isEqualTo(16);
    }

    @Test
    void protectedEndpointsRejectAnonymousCalls() {
        client.get().uri("/api/v1/agents/workflows").exchange().expectStatus().isUnauthorized();
        client.get().uri("/api/v1/chat/" + java.util.UUID.randomUUID() + "/messages")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void invalidBearerTokenIsRejected() {
        client.get().uri("/api/v1/agents/workflows").header("Authorization", "Bearer not-a-jwt")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void registeredUserCanRunAgentWorkflow() {
        String token = registerAndGetToken("agent-user@example.com");
        client.post().uri("/api/v1/agents/runs").header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"workflow\":\"REPOSITORY_ANALYSIS\",\"objective\":\"analyse repo\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.steps.length()").isEqualTo(6);
    }

    @Test
    void chatStreamsAndStoresHistoryPerTenant() {
        String token = registerAndGetToken("chat-user@example.com");
        String conversation = java.util.UUID.randomUUID().toString();

        String streamed = client.post().uri("/api/v1/chat/" + conversation + "/messages")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"content\":\"hello platform\"}")
                .exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        assertThat(streamed).contains("Echo:");

        client.get().uri("/api/v1/chat/" + conversation + "/messages")
                .header("Authorization", "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(2);

        String otherTenant = registerAndGetToken("other-tenant@example.com");
        client.get().uri("/api/v1/chat/" + conversation + "/messages")
                .header("Authorization", "Bearer " + otherTenant)
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void ragIngestAndQueryRoundTrip() {
        String token = registerAndGetToken("rag-user@example.com");
        client.post().uri("/api/v1/rag/documents").header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"source\":\"notes.md\",\"text\":\"Qdrant stores embeddings for semantic retrieval.\"}")
                .exchange().expectStatus().isCreated();

        client.post().uri("/api/v1/rag/query").header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"query\":\"where are embeddings stored\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.citations[0]").isEqualTo("notes.md#0");
    }

    @Test
    void validationErrorsUseUniformErrorBody() {
        client.post().uri("/api/v1/auth/login").header("Content-Type", "application/json")
                .bodyValue("{\"email\":\"not-an-email\",\"password\":\"\"}")
                .exchange().expectStatus().isBadRequest()
                .expectBody().jsonPath("$.code").isEqualTo("VALIDATION");
    }

    @Test
    void actuatorHealthIsPublic() {
        client.get().uri("/actuator/health").exchange().expectStatus().isOk();
    }

    @Test
    void authenticatedUserCanAccessPlatformSubmodules() {
        String token = registerAndGetToken("submodules-user@example.com");

        client.post().uri("/api/v1/code/analyze")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"code\":\"public class Sample {}\",\"language\":\"java\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.language").isEqualTo("java");

        client.post().uri("/api/v1/security/scan")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"repository\":\"local\",\"sourceCode\":\"public void exec() {}\",\"filename\":\"Source.java\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.passed").isEqualTo(true)
                .jsonPath("$.totalIssues").isEqualTo(0);

        client.post().uri("/api/v1/review/diff")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"diff\":\"--- a/File.java\\n+++ b/File.java\\n@@ -1 +1 @@\\n-old\\n+new\"}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.approved").isEqualTo(true);

        client.post().uri("/api/v1/search/query")
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .bodyValue("{\"query\":\"auth\",\"limit\":5}")
                .exchange().expectStatus().isOk()
                .expectBody().jsonPath("$.query").isEqualTo("auth");
    }
}

