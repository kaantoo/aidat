package tr.gov.tuketbir.config.multitenancy;

/**
 * Tenant Context - Thread-local tenant ID yönetimi
 * 
 * Her HTTP request için tenant bilgisini tutar.
 * JWT token'dan alınan tenant_id bu context'e set edilir.
 * 
 * @author Tuketbir Development Team
 */
public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Mevcut thread için tenant ID'yi set et
     */
    public static void setCurrentTenant(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Mevcut thread'in tenant ID'sini al
     */
    public static Long getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Tenant context'i temizle (request sonunda çağrılmalı)
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
