package tr.gov.tuketbir.domain.enums;

/**
 * Üyelik Tipi Enum
 */
public enum UyelikTipi {
    GERCEK_KISI("Gerçek Kişi"),
    TUZEL_KISI("Tüzel Kişi");

    private final String aciklama;

    UyelikTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
