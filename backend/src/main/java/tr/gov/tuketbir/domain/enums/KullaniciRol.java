package tr.gov.tuketbir.domain.enums;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Kullanıcı Rol Enum - RBAC yapısının temelini oluşturur.
 * 
 * Her rol, belirli yetkilere sahiptir:
 * - SISTEM_ADMIN: Süper admin, tüm sistem erişimi (birlik bağımsız)
 * - MERKEZ_YONETICI: Tüm sistem erişimi
 * - BIRLIK_YONETICI: Kendi birliği yönetimi
 * - BIRLIK_PERSONEL: Operasyonel işlemler
 * - MUHASEBE_SORUMLU: Mali işlemler
 * - GOZLEMCI: Salt-okunur erişim
 */
public enum KullaniciRol {
    SISTEM_ADMIN("Sistem Yöneticisi", new String[]{
        // Tüm yetkiler + sistem yönetimi
        "PERM_USER_CREATE", "PERM_USER_READ", "PERM_USER_UPDATE", "PERM_USER_DELETE",
        "PERM_BIRLIK_CREATE", "PERM_BIRLIK_READ", "PERM_BIRLIK_UPDATE", "PERM_BIRLIK_DELETE",
        "PERM_UYE_CREATE", "PERM_UYE_READ", "PERM_UYE_UPDATE", "PERM_UYE_DELETE", "PERM_UYE_IMPORT",
        "PERM_AIDAT_CREATE", "PERM_AIDAT_READ", "PERM_AIDAT_UPDATE", "PERM_AIDAT_DELETE", "PERM_AIDAT_TOPLU_ATAMA",
        "PERM_TAHSILAT_CREATE", "PERM_TAHSILAT_READ", "PERM_TAHSILAT_UPDATE", "PERM_TAHSILAT_IPTAL",
        "PERM_GELIR_GIDER_CREATE", "PERM_GELIR_GIDER_READ", "PERM_GELIR_GIDER_UPDATE", "PERM_GELIR_GIDER_DELETE",
        "PERM_BELGE_CREATE", "PERM_BELGE_READ", "PERM_BELGE_UPDATE", "PERM_BELGE_DELETE", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_AIDAT", "PERM_RAPOR_MALI", "PERM_RAPOR_UYE", "PERM_RAPOR_GENEL",
        "PERM_SISTEM_AYAR", "PERM_YEDEKLEME", "PERM_AUDIT_LOG",
        "PERM_TUM_BIRLIK_ERISIM", "PERM_TUM_IL_ERISIM",
        "PERM_SISTEM_ADMIN", "PERM_TENANT_YONETIM"
    }),

    MERKEZ_YONETICI("Merkez Yöneticisi", new String[]{
        // Tüm yetkiler
        "PERM_USER_CREATE", "PERM_USER_READ", "PERM_USER_UPDATE", "PERM_USER_DELETE",
        "PERM_BIRLIK_CREATE", "PERM_BIRLIK_READ", "PERM_BIRLIK_UPDATE", "PERM_BIRLIK_DELETE",
        "PERM_UYE_CREATE", "PERM_UYE_READ", "PERM_UYE_UPDATE", "PERM_UYE_DELETE", "PERM_UYE_IMPORT",
        "PERM_AIDAT_CREATE", "PERM_AIDAT_READ", "PERM_AIDAT_UPDATE", "PERM_AIDAT_DELETE", "PERM_AIDAT_TOPLU_ATAMA",
        "PERM_TAHSILAT_CREATE", "PERM_TAHSILAT_READ", "PERM_TAHSILAT_UPDATE", "PERM_TAHSILAT_IPTAL",
        "PERM_GELIR_GIDER_CREATE", "PERM_GELIR_GIDER_READ", "PERM_GELIR_GIDER_UPDATE", "PERM_GELIR_GIDER_DELETE",
        "PERM_BELGE_CREATE", "PERM_BELGE_READ", "PERM_BELGE_UPDATE", "PERM_BELGE_DELETE", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_AIDAT", "PERM_RAPOR_MALI", "PERM_RAPOR_UYE", "PERM_RAPOR_GENEL",
        "PERM_SISTEM_AYAR", "PERM_YEDEKLEME", "PERM_AUDIT_LOG",
        "PERM_TUM_BIRLIK_ERISIM", "PERM_TUM_IL_ERISIM"
    }),

    BIRLIK_YONETICI("Birlik Yöneticisi", new String[]{
        "PERM_USER_READ",
        "PERM_BIRLIK_READ", "PERM_BIRLIK_UPDATE",
        "PERM_UYE_CREATE", "PERM_UYE_READ", "PERM_UYE_UPDATE", "PERM_UYE_IMPORT",
        "PERM_AIDAT_CREATE", "PERM_AIDAT_READ", "PERM_AIDAT_UPDATE", "PERM_AIDAT_TOPLU_ATAMA",
        "PERM_TAHSILAT_CREATE", "PERM_TAHSILAT_READ", "PERM_TAHSILAT_UPDATE",
        "PERM_GELIR_GIDER_CREATE", "PERM_GELIR_GIDER_READ", "PERM_GELIR_GIDER_UPDATE",
        "PERM_BELGE_CREATE", "PERM_BELGE_READ", "PERM_BELGE_UPDATE", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_AIDAT", "PERM_RAPOR_MALI", "PERM_RAPOR_UYE"
    }),

    BIRLIK_PERSONEL("Birlik Personeli", new String[]{
        "PERM_UYE_CREATE", "PERM_UYE_READ", "PERM_UYE_UPDATE",
        "PERM_AIDAT_READ",
        "PERM_TAHSILAT_CREATE", "PERM_TAHSILAT_READ",
        "PERM_BELGE_CREATE", "PERM_BELGE_READ", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_UYE"
    }),

    MUHASEBE_SORUMLU("Muhasebe Sorumlusu", new String[]{
        "PERM_UYE_READ",
        "PERM_AIDAT_CREATE", "PERM_AIDAT_READ", "PERM_AIDAT_UPDATE", "PERM_AIDAT_TOPLU_ATAMA",
        "PERM_TAHSILAT_CREATE", "PERM_TAHSILAT_READ", "PERM_TAHSILAT_UPDATE", "PERM_TAHSILAT_IPTAL",
        "PERM_GELIR_GIDER_CREATE", "PERM_GELIR_GIDER_READ", "PERM_GELIR_GIDER_UPDATE",
        "PERM_BELGE_CREATE", "PERM_BELGE_READ", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_AIDAT", "PERM_RAPOR_MALI"
    }),

    GOZLEMCI("Gözlemci / Denetçi", new String[]{
        "PERM_BIRLIK_READ",
        "PERM_UYE_READ",
        "PERM_AIDAT_READ",
        "PERM_TAHSILAT_READ",
        "PERM_GELIR_GIDER_READ",
        "PERM_BELGE_READ", "PERM_BELGE_DOWNLOAD",
        "PERM_RAPOR_AIDAT", "PERM_RAPOR_MALI", "PERM_RAPOR_UYE", "PERM_RAPOR_GENEL",
        "PERM_AUDIT_LOG"
    });

    private final String aciklama;
    private final Set<String> yetkiler;

    KullaniciRol(String aciklama, String[] yetkiler) {
        this.aciklama = aciklama;
        this.yetkiler = new HashSet<>(Arrays.asList(yetkiler));
    }

    public String getAciklama() {
        return aciklama;
    }

    public Set<String> getYetkiler() {
        return yetkiler;
    }

    /**
     * Belirli bir yetkiye sahip mi kontrolü
     */
    public boolean hasPermission(String permission) {
        return yetkiler.contains(permission);
    }
}
