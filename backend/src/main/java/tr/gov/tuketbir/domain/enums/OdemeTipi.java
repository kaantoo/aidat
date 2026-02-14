package tr.gov.tuketbir.domain.enums;

/**
 * Ödeme Tipi Enum
 */
public enum OdemeTipi {
    NAKIT("Nakit"),
    HAVALE("Havale"),
    EFT("EFT"),
    KREDI_KARTI("Kredi Kartı"),
    CEK("Çek"),
    SENET("Senet"),
    DIGER("Diğer");

    private final String aciklama;

    OdemeTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
