package com.enterprise.assetmanagement.tenant;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static Long requireTenantId() {
        Long tenantId = CURRENT_TENANT.get();

        if (tenantId == null) {
            throw new IllegalStateException("Tenant context is not available");
        }

        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}