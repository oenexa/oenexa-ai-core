package ai.oenexa.common.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void mapsDomainExceptionToItsStatus() {
        var response = handler.handle(new OenexaException(ErrorCode.NOT_FOUND, "missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo("missing");
    }

    @Test
    void doesNotLeakUnexpectedErrorDetails() {
        var response = handler.handleUnexpected(new IllegalStateException("db password is hunter2"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Internal error");
    }
}
