package tr.gov.tuketbir.config.multitenancy;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Tenant Identifier Resolver
 * 
 * Hibernate'in multi-tenant desteği için tenant ID'yi çözümler.
 * TenantContext'ten mevcut tenant ID'yi alır.
 * 
 * @author Tuketbir Development Team
 */
@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver, HibernatePropertiesCustomizer {

    /**
     * Merkez birlik için varsayılan tenant ID
     */
    private static final String DEFAULT_TENANT_ID = "0";

    @Override
    public String resolveCurrentTenantIdentifier() {
        Long tenantId = TenantContext.getCurrentTenant();
        return tenantId != null ? tenantId.toString() : DEFAULT_TENANT_ID;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER, this);
    }
}
