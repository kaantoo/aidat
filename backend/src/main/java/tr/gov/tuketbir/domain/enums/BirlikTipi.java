package tr.gov.tuketbir.domain.enums;

/**
 * Birlik Tipi Enum
 */
public enum BirlikTipi {
    MERKEZ("Merkez Birliği"),
    ALT_BIRLIK("Alt Birlik");

    private final String aciklama;

    BirlikTipi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
