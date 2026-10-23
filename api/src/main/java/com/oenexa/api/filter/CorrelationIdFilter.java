package com.oenexa.api.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Enterprise Correlation ID Servlet Filter for distributed tracing and observability.
 * Captures or generates 'X-Correlation-Id', populates SLF4J MDC, and appends to response headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_HEADER = "X-Correlation-Id";
    public static final String MDC_CORRELATION_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String correlationId = httpRequest.getHeader(CORRELATION_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = "corr-" + UUID.randomUUID().toString().substring(0, 8);
            }

            MDC.put(MDC_CORRELATION_KEY, correlationId);
            httpResponse.setHeader(CORRELATION_HEADER, correlationId);

            try {
                chain.doFilter(request, response);
            } finally {
                MDC.remove(MDC_CORRELATION_KEY);
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
