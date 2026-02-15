package tr.gov.tuketbir.domain.enums;

/**
 * Karar Durumu Enum
 */
public enum KararDurumu {
    KABUL_EDILDI("Kabul Edildi"),
    REDDEDILDI("Reddedildi"),
    ERTELENDI("Ertelendi"),
    UYGULAMADA("Uygulamada"),
    TAMAMLANDI("Tamamlandı");

    private final String aciklama;

    KararDurumu(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
