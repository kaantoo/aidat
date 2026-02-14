package tr.gov.tuketbir.domain.enums;

/**
 * Gider Kategorisi Enum
 */
public enum GiderKategorisi {
    PERSONEL("Personel"),
    KIRA("Kira"),
    FATURA("Fatura"),
    MALZEME("Malzeme"),
    ULASIM("Ulaşım"),
    TEMSIL_AGIRLAMAM("Temsil & Ağırlama"),
    BAKIM_ONARIM("Bakım/Onarım"),
    DIGER_GIDER("Diğer Gider"),
    // Eski değerler (geriye uyumluluk için)
    KIRTASIYE("Kırtasiye"),
    ETKINLIK("Etkinlik Gideri"),
    MAAS("Maaş/Personel"),
    ELEKTRIK("Elektrik"),
    SU("Su"),
    DOGALGAZ("Doğalgaz"),
    INTERNET("İnternet/Telefon"),
    YEMEK("Yemek"),
    SIGORTA("Sigorta"),
    VERGI("Vergi/Harç"),
    DIGER("Diğer");

    private final String aciklama;

    GiderKategorisi(String aciklama) {
        this.aciklama = aciklama;
    }

    public String getAciklama() {
        return aciklama;
    }
}
