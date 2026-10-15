package com.oenexa.domain;

import java.util.Objects;

/**
 * ThreadLocal Tenant Context for Multi-Tenant Isolation across virtual threads.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void setTenantId(String tenantId) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : "default";
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
