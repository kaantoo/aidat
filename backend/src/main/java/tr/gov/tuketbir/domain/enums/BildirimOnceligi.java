package tr.gov.tuketbir.domain.enums;

/**
 * Bildirim Önceliği Enum
 */
public enum BildirimOnceligi {
    DUSUK("Düşük"),
    NORMAL("Normal"),
    YUKSEK("Yüksek"),
    KRITIK("Kritik");

    private final String aciklama;

    BildirimOnceligi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
