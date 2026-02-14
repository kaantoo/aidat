package tr.gov.tuketbir.domain.enums;

/**
 * Belge Tipi Enum
 */
public enum BelgeTipi {
    MAKBUZ("Makbuz"),
    FATURA("Fatura"),
    DEKONT("Dekont"),
    RAPOR("Rapor"),
    DILEKCE("Dilekçe"),
    SOZLESME("Sözleşme"),
    TUTANAK("Tutanak"),
    KARAR("Karar"),
    DUYURU("Duyuru"),
    FOTOGRAF("Fotoğraf"),
    DIGER("Diğer");

    private final String aciklama;

    BelgeTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
