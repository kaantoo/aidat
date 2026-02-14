package tr.gov.tuketbir.domain.enums;

/**
 * Dönem Tipi Enum
 */
public enum DonemTipi {
    ALTI_AYLIK("6 Aylık"),
    YILLIK("Yıllık");

    private final String aciklama;

    DonemTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
