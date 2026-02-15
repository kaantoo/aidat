package tr.gov.tuketbir.domain.enums;

/**
 * Toplantı Durumu Enum
 */
public enum ToplantiDurumu {
    PLANLANMIS("Planlanmış"),
    DEVAM_EDIYOR("Devam Ediyor"),
    TAMAMLANDI("Tamamlandı"),
    IPTAL("İptal Edildi"),
    ERTELENDI("Ertelendi");

    private final String aciklama;

    ToplantiDurumu(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
