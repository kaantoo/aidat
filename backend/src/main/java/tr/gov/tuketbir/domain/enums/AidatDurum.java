package tr.gov.tuketbir.domain.enums;

/**
 * Aidat Durumu Enum
 */
public enum AidatDurum {
    BEKLIYOR("Ödeme Bekleniyor"),
    KISMI_ODENDI("Kısmi Ödendi"),
    ODENDI("Ödendi"),
    GECIKTI("Gecikmiş"),
    IPTAL("İptal Edildi");

    private final String aciklama;

    AidatDurum(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
