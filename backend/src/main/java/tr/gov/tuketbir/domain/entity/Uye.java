package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.Cinsiyet;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.domain.enums.UyelikTipi;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Üye Entity - Birlik üyelerini temsil eder.
 * Her üye bir alt birliğe bağlıdır ve benzersiz TC Kimlik numarasına sahiptir.
 * 
 * Üye numarası otomatik oluşturulur: {BirlikKodu}-{YIL}-{SıraNo}
 * Örnek: ANKARA001-2026-00001
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "uyeler", indexes = {
    @Index(name = "idx_uye_no", columnList = "uye_no", unique = true),
    @Index(name = "idx_uye_tc", columnList = "tc_kimlik_no", unique = true),
    @Index(name = "idx_uye_birlik", columnList = "birlik_id"),
    @Index(name = "idx_uye_durum", columnList = "uye_durum"),
    @Index(name = "idx_uye_il", columnList = "il_kodu"),
    @Index(name = "idx_uye_ad_soyad", columnList = "ad, soyad")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Uye extends BaseEntity {

    /**
     * Otomatik üretilen benzersiz üye numarası
     * Format: {BirlikKodu}-{YIL}-{5 haneli sıra no}
     */
    @Column(name = "uye_no", nullable = false, unique = true, length = 30)
    private String uyeNo;

    /**
     * TC Kimlik Numarası (11 haneli, benzersiz)
     */
    @NotBlank(message = "TC Kimlik No zorunludur")
    @Pattern(regexp = "^[1-9][0-9]{10}$", message = "Geçerli bir TC Kimlik No giriniz")
    @Column(name = "tc_kimlik_no", nullable = false, unique = true, length = 11)
    private String tcKimlikNo;

    /**
     * Üyenin adı
     */
    @NotBlank(message = "Ad zorunludur")
    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arasında olmalıdır")
    @Column(name = "ad", nullable = false, length = 100)
    private String ad;

    /**
     * Üyenin soyadı
     */
    @NotBlank(message = "Soyad zorunludur")
    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arasında olmalıdır")
    @Column(name = "soyad", nullable = false, length = 100)
    private String soyad;

    /**
     * Baba adı
     */
    @Column(name = "baba_adi", length = 100)
    private String babaAdi;

    /**
     * Ana adı
     */
    @Column(name = "ana_adi", length = 100)
    private String anaAdi;

    /**
     * Doğum tarihi
     */
    @Column(name = "dogum_tarihi")
    private LocalDate dogumTarihi;

    /**
     * Doğum yeri
     */
    @Column(name = "dogum_yeri", length = 100)
    private String dogumYeri;

    /**
     * Cinsiyet
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "cinsiyet", length = 10)
    private Cinsiyet cinsiyet;

    /**
     * Bağlı olduğu birlik
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * Üyelik tipi (Gerçek Kişi / Tüzel Kişi)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "uyelik_tipi", nullable = false, length = 20)
    @Builder.Default
    private UyelikTipi uyelikTipi = UyelikTipi.GERCEK_KISI;

    /**
     * Üye durumu (Aktif / Pasif / Askıda)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "uye_durum", nullable = false, length = 20)
    @Builder.Default
    private UyeDurum uyeDurum = UyeDurum.AKTIF;

    /**
     * Üyelik başlangıç tarihi
     */
    @Column(name = "katilim_tarihi", nullable = false)
    private LocalDate katilimTarihi;

    /**
     * Üyelik bitiş tarihi (pasifleştirilirse)
     */
    @Column(name = "ayrilik_tarihi")
    private LocalDate ayrilikTarihi;

    /**
     * Ayrılma nedeni
     */
    @Column(name = "ayrilik_nedeni", length = 500)
    private String ayrilikNedeni;

    // ======================= İletişim Bilgileri =======================

    /**
     * Cep telefonu
     */
    @Pattern(regexp = "^(05)[0-9]{9}$", message = "Geçerli bir cep telefonu giriniz")
    @Column(name = "cep_telefon", length = 15)
    private String cepTelefon;

    /**
     * Sabit telefon
     */
    @Column(name = "sabit_telefon", length = 15)
    private String sabitTelefon;

    /**
     * E-posta adresi
     */
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Column(name = "email", length = 150)
    private String email;

    // ======================= Adres Bilgileri =======================

    /**
     * İl kodu
     */
    @Column(name = "il_kodu", length = 10)
    private String ilKodu;

    /**
     * İl adı
     */
    @Column(name = "il_adi", length = 100)
    private String ilAdi;

    /**
     * İlçe kodu
     */
    @Column(name = "ilce_kodu", length = 10)
    private String ilceKodu;

    /**
     * İlçe adı
     */
    @Column(name = "ilce_adi", length = 100)
    private String ilceAdi;

    /**
     * Mahalle / Köy
     */
    @Column(name = "mahalle_koy", length = 100)
    private String mahalleKoy;

    /**
     * Açık adres
     */
    @Column(name = "adres", length = 500)
    private String adres;

    /**
     * Posta kodu
     */
    @Column(name = "posta_kodu", length = 10)
    private String postaKodu;

    // ======================= Kurumsal Üye Bilgileri =======================

    /**
     * Firma / Kurum adı (Kurumsal üyeler için)
     */
    @Column(name = "firma_adi", length = 200)
    private String firmaAdi;

    /**
     * Vergi dairesi
     */
    @Column(name = "vergi_dairesi", length = 100)
    private String vergiDairesi;

    /**
     * Vergi numarası
     */
    @Column(name = "vergi_no", length = 20)
    private String vergiNo;

    /**
     * Ticaret sicil no
     */
    @Column(name = "ticaret_sicil_no", length = 50)
    private String ticaretSicilNo;

    // ======================= Üretici Bilgileri =======================

    /**
     * İşletme / Çiftlik adı
     */
    @Column(name = "isletme_adi", length = 200)
    private String isletmeAdi;

    /**
     * İşletme sicil no
     */
    @Column(name = "isletme_sicil_no", length = 50)
    private String isletmeSicilNo;

    /**
     * Hayvan sayısı
     */
    @Column(name = "hayvan_sayisi")
    private Integer hayvanSayisi;

    /**
     * Üretim kapasitesi (kg/yıl)
     */
    @Column(name = "uretim_kapasitesi")
    private Integer uretimKapasitesi;

    // ======================= İlişkiler =======================

    /**
     * Üyeye ait aidatlar
     */
    @OneToMany(mappedBy = "uye", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Aidat> aidatlar = new HashSet<>();

    /**
     * Üyeye ait belgeler
     */
    @OneToMany(mappedBy = "uye", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Belge> belgeler = new HashSet<>();

    /**
     * Açıklama / Notlar
     */
    @Column(name = "aciklama", length = 1000)
    private String aciklama;

    // ======================= Yardımcı Metodlar =======================

    /**
     * Tam ad döndürür
     */
    public String getTamAd() {
        return this.ad + " " + this.soyad;
    }

    /**
     * Üye aktif mi kontrolü
     */
    public boolean isAktif() {
        return UyeDurum.AKTIF.equals(this.uyeDurum);
    }

    /**
     * Üyeliği pasifleştir
     */
    public void pasifYap(String neden) {
        this.uyeDurum = UyeDurum.PASIF;
        this.ayrilikTarihi = LocalDate.now();
        this.ayrilikNedeni = neden;
    }

    /**
     * Üyeliği tekrar aktif yap
     */
    public void aktifYap() {
        this.uyeDurum = UyeDurum.AKTIF;
        this.ayrilikTarihi = null;
        this.ayrilikNedeni = null;
    }
}
