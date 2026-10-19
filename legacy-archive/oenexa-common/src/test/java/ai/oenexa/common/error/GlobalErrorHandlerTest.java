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
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    void dummyTargetMethod(String dummyParam) {
    }

    @Test
    void mapsWebExchangeBindExceptionWithFieldErrors() throws Exception {
        java.lang.reflect.Method method = GlobalErrorHandlerTest.class.getDeclaredMethod("dummyTargetMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);

        org.springframework.validation.MapBindingResult bindingResult =
                new org.springframework.validation.MapBindingResult(java.util.Map.of(), "target");
        bindingResult.addError(new org.springframework.validation.FieldError("target", "email", "must not be blank"));
        bindingResult.addError(new org.springframework.validation.FieldError("target", "password", "too short"));

        var ex = new org.springframework.web.bind.support.WebExchangeBindException(methodParameter, bindingResult);
        var response = handler.handle(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION");
        assertThat(response.getBody().message()).contains("email must not be blank", "password too short");
    }

    @Test
    void mapsWebExchangeBindExceptionWithoutFieldErrors() throws Exception {
        java.lang.reflect.Method method = GlobalErrorHandlerTest.class.getDeclaredMethod("dummyTargetMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);

        org.springframework.validation.MapBindingResult bindingResult =
                new org.springframework.validation.MapBindingResult(java.util.Map.of(), "target");

        var ex = new org.springframework.web.bind.support.WebExchangeBindException(methodParameter, bindingResult);
        var response = handler.handle(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().code()).isEqualTo("VALIDATION");
        assertThat(response.getBody().message()).isEqualTo("invalid request");
    }

    @Test
    void oenexaExceptionConstructorsAndErrorCodeStatus() {
        var cause = new RuntimeException("root cause");
        var ex = new OenexaException(ErrorCode.CONFLICT, "conflict occurred", cause);
        assertThat(ex.code()).isEqualTo(ErrorCode.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("conflict occurred");
        assertThat(ex.getCause()).isEqualTo(cause);

        assertThat(ErrorCode.FORBIDDEN.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorCode.VALIDATION.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.UNAUTHORIZED.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.INTERNAL.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
