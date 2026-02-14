package tr.gov.tuketbir.domain.enums;

/**
 * Audit İşlem Tipi Enum
 */
public enum AuditIslemTipi {
    // Kimlik doğrulama
    GIRIS("Giriş"),
    CIKIS("Çıkış"),
    GIRIS_BASARISIZ("Başarısız Giriş"),
    SIFRE_DEGISTIRME("Şifre Değiştirme"),
    SIFRE_SIFIRLAMA("Şifre Sıfırlama"),
    SIFRE_SIFIRLAMA_TALEBI("Şifre Sıfırlama Talebi"),
    IKI_FAKTOR_AKTIF("2FA Aktifleştirme"),
    IKI_FAKTOR_PASIF("2FA Deaktifleştirme"),
    
    // CRUD işlemleri
    OLUSTURMA("Oluşturma"),
    OKUMA("Okuma"),
    GUNCELLEME("Güncelleme"),
    SILME("Silme"),
    PASIF_YAPMA("Pasif Yapma"),
    AKTIF_YAPMA("Aktif Yapma"),
    
    // Toplu işlemler
    TOPLU_EKLEME("Toplu Ekleme"),
    TOPLU_GUNCELLEME("Toplu Güncelleme"),
    IMPORT("İçe Aktarma"),
    EXPORT("Dışa Aktarma"),
    
    // Dosya işlemleri
    DOSYA_YUKLEME("Dosya Yükleme"),
    DOSYA_INDIRME("Dosya İndirme"),
    DOSYA_SILME("Dosya Silme"),
    
    // Rapor işlemleri
    RAPOR_OLUSTURMA("Rapor Oluşturma"),
    RAPOR_EXPORT("Rapor Dışa Aktarma"),
    
    // Sistem işlemleri
    YEDEKLEME("Yedekleme"),
    GERI_YUKLEME("Geri Yükleme"),
    SISTEM_AYAR("Sistem Ayarı Değiştirme"),
    
    // Otomatik işlemler
    OTOMATIK_ATAMA("Otomatik Atama"),
    DIGER("Diğer");

    private final String aciklama;

    AuditIslemTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
