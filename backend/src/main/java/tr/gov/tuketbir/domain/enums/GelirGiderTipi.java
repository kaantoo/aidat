package tr.gov.tuketbir.domain.enums;

/**
 * Gelir/Gider Tipi Enum
 */
public enum GelirGiderTipi {
    GELIR("Gelir"),
    GIDER("Gider");

    private final String aciklama;

    GelirGiderTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
