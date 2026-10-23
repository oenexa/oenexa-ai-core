package com.oenexa.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Enterprise Standard API Response Envelope for OENEXA REST APIs.
 * Guarantees uniform structure, status codes, payload data, and correlation trace IDs.
 */
public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        String correlationId,
        Instant timestamp
) {
    public ApiResponse {
        status = (status >= 100 && status <= 599) ? status : 200;
        message = (message != null) ? message : (success ? "Success" : "Error");
        correlationId = (correlationId != null && !correlationId.isBlank()) ? correlationId : "corr-" + UUID.randomUUID();
        timestamp = (timestamp != null) ? timestamp : Instant.now();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, 200, "Operation successful", data, null, null);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, 200, message, data, null, null);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(true, 201, "Resource created successfully", data, null, null);
    }

    public static <T> ApiResponse<T> failure(int status, String message) {
        return new ApiResponse<>(false, status, message, null, null, null);
    }
}
