package tr.gov.tuketbir.domain.enums;

/**
 * Bildirim Tipi Enum
 */
public enum BildirimTipi {
    UYE_KAYDI("Üye Kaydı", "Yeni üye kaydı ile ilgili bildirim"),
    AIDAT_ODEMESI("Aidat Ödemesi", "Aidat ödemesi ile ilgili bildirim"),
    AIDAT_HATIRLATMA("Aidat Hatırlatma", "Aidat ödeme hatırlatması"),
    DONEM_OLUSTURULDU("Dönem Oluşturuldu", "Yeni dönem oluşturuldu bildirimi"),
    DONEM_KAPANDI("Dönem Kapandı", "Dönem kapanışı bildirimi"),
    TAHAKKUK_OLUSTURULDU("Tahakkuk Oluşturuldu", "Tahakkuk oluşturuldu bildirimi"),
    GECIKEN_ODEME("Geciken Ödeme", "Geciken ödeme uyarısı"),
    GELIR_GIDER("Gelir/Gider", "Gelir veya gider kaydı bildirimi"),
    SISTEM("Sistem", "Sistem bildirimi"),
    DUYURU("Duyuru", "Genel duyuru"),
    ONAY_BEKLIYOR("Onay Bekliyor", "Onay bekleyen işlem bildirimi"),
    ONAYLANDI("Onaylandı", "Onaylanan işlem bildirimi"),
    REDDEDILDI("Reddedildi", "Reddedilen işlem bildirimi");

    private final String baslik;
    private final String aciklama;

    BildirimTipi(String baslik, String aciklama) {
        this.baslik = baslik;
        this.aciklama = aciklama;
    }

    public String getBaslik() {
        return baslik;
    }

    public String getAciklama() {
        return aciklama;
    }
}
