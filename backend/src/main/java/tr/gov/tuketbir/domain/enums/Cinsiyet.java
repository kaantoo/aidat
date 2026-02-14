package tr.gov.tuketbir.domain.enums;

/**
 * Cinsiyet Enum
 */
public enum Cinsiyet {
    ERKEK("Erkek"),
    KADIN("Kadın");

    private final String aciklama;

    Cinsiyet(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
