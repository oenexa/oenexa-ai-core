package ai.oenexa.common.error;

import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

/** Maps exceptions to a uniform, non-leaking error body. */
@RestControllerAdvice
public class GlobalErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorHandler.class);

    public record ErrorResponse(String code, String message, Instant timestamp) {
    }

    @ExceptionHandler(OenexaException.class)
    public ResponseEntity<ErrorResponse> handle(OenexaException e) {
        return ResponseEntity.status(e.code().status())
                .body(new ErrorResponse(e.code().name(), e.getMessage(), Instant.now()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ErrorResponse> handle(WebExchangeBindException e) {
        String message = e.getFieldErrors().stream()
                .map(f -> f.getField() + " " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b).orElse("invalid request");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.VALIDATION.name(), message, Instant.now()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Unhandled error", e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(ErrorCode.INTERNAL.name(), "Internal error", Instant.now()));
    }
}
