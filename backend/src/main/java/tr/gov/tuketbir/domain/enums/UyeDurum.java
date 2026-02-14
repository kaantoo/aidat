package tr.gov.tuketbir.domain.enums;

/**
 * Üye Durumu Enum
 */
public enum UyeDurum {
    AKTIF("Aktif"),
    PASIF("Pasif"),
    ASKIYA_ALINMIS("Askıya Alınmış"),
    IHRAC_EDILMIS("İhraç Edilmiş");

    private final String aciklama;

    UyeDurum(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
