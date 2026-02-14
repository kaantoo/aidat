package tr.gov.tuketbir.domain.enums;

/**
 * Gelir Kategorisi Enum
 */
public enum GelirKategorisi {
    // Frontend değerleri
    AIDAT_GELIRI("Aidat Geliri"),
    BAGIS("Bağış"),
    FAIZ_GELIRI("Faiz Geliri"),
    KIRA_GELIRI("Kira Geliri"),
    DIGER_GELIR("Diğer Gelir"),
    // Eski değerler (geriye uyumluluk için)
    AIDAT("Aidat Geliri"),
    SPONSORLUK("Sponsorluk"),
    ETKINLIK("Etkinlik Geliri"),
    DEVLET_DESTEGI("Devlet Desteği"),
    FAIZ("Faiz Geliri"),
    DIGER("Diğer Gelir");

    private final String aciklama;

    GelirKategorisi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
