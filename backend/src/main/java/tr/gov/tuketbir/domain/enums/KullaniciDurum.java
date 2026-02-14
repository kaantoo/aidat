package tr.gov.tuketbir.domain.enums;

/**
 * Kullanıcı Durumu Enum
 */
public enum KullaniciDurum {
    AKTIF("Aktif"),
    PASIF("Pasif"),
    BEKLEMEDE("Onay Bekliyor"),
    ASKIYA_ALINDI("Askıya Alındı");

    private final String aciklama;

    KullaniciDurum(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
