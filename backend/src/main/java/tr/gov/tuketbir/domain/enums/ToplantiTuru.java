package tr.gov.tuketbir.domain.enums;

/**
 * Toplantı Türü Enum
 */
public enum ToplantiTuru {
    GENEL_KURUL("Genel Kurul"),
    YONETIM_KURULU("Yönetim Kurulu"),
    DENETIM_KURULU("Denetim Kurulu"),
    OLAGAN_TOPLANTI("Olağan Toplantı"),
    OLAGANUSTU_TOPLANTI("Olağanüstü Toplantı"),
    DIGER("Diğer");

    private final String aciklama;

    ToplantiTuru(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
