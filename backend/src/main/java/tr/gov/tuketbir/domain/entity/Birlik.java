package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.BirlikTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Birlik Entity - Merkez ve Alt Birlikleri temsil eder.
 * Multi-tenant yapının temel taşıdır.
 * 
 * Merkez Birliği: parentBirlik = null, birlikTipi = MERKEZ
 * Alt Birlik: parentBirlik = Merkez Birlik, birlikTipi = ALT_BIRLIK
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "birlikler", indexes = {
    @Index(name = "idx_birlik_kod", columnList = "birlik_kodu", unique = true),
    @Index(name = "idx_birlik_il", columnList = "il_kodu"),
    @Index(name = "idx_birlik_ilce", columnList = "ilce_kodu"),
    @Index(name = "idx_birlik_parent", columnList = "parent_birlik_id"),
    @Index(name = "idx_birlik_tip", columnList = "birlik_tipi")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Birlik extends BaseEntity {

    /**
     * Benzersiz birlik kodu (örn: "ANKARA-001", "MERKEZ")
     */
    @Column(name = "birlik_kodu", nullable = false, unique = true, length = 50)
    private String birlikKodu;

    /**
     * Birlik tam adı
     */
    @Column(name = "birlik_adi", nullable = false, length = 200)
    private String birlikAdi;

    /**
     * Birlik kısa adı
     */
    @Column(name = "kisa_adi", length = 100)
    private String kisaAdi;

    /**
     * Birlik tipi (MERKEZ / ALT_BIRLIK)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "birlik_tipi", nullable = false, length = 20)
    private BirlikTipi birlikTipi;

    /**
     * Merkeze ödenen pay oranı (%)
     * Alt birliklerin merkeze aktaracağı aidat payı yüzdesi
     * Örn: 10.00 = %10
     */
    @Column(name = "merkez_pay_orani", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal merkezPayOrani = BigDecimal.ZERO;

    /**
     * Üst birlik (Alt birlikler için Merkez Birlik)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_birlik_id")
    private Birlik parentBirlik;

    /**
     * Alt birlikler listesi
     */
    @OneToMany(mappedBy = "parentBirlik", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Birlik> altBirlikler = new HashSet<>();

    /**
     * İl kodu (TÜİK kodlaması)
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
     * Açık adres
     */
    @Column(name = "adres", length = 500)
    private String adres;

    /**
     * Posta kodu
     */
    @Column(name = "posta_kodu", length = 10)
    private String postaKodu;

    /**
     * Telefon numarası
     */
    @Column(name = "telefon", length = 20)
    private String telefon;

    /**
     * Faks numarası
     */
    @Column(name = "faks", length = 20)
    private String faks;

    /**
     * E-posta adresi
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * Web sitesi
     */
    @Column(name = "web_sitesi", length = 200)
    private String webSitesi;

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
     * Kuruluş tarihi
     */
    @Column(name = "kurulus_tarihi")
    private LocalDate kurulusTarihi;

    /**
     * Sicil numarası
     */
    @Column(name = "sicil_no", length = 50)
    private String sicilNo;

    /**
     * Birlik başkanı adı
     */
    @Column(name = "baskan_adi", length = 150)
    private String baskanAdi;

    /**
     * Başkan telefonu
     */
    @Column(name = "baskan_telefon", length = 20)
    private String baskanTelefon;

    /**
     * İletişim sorumlusu
     */
    @Column(name = "iletisim_sorumlusu", length = 150)
    private String iletisimSorumlusu;

    /**
     * İletişim sorumlusu telefonu
     */
    @Column(name = "iletisim_telefon", length = 20)
    private String iletisimTelefon;

    /**
     * Birliğe bağlı üyeler
     */
    @OneToMany(mappedBy = "birlik", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Uye> uyeler = new HashSet<>();

    /**
     * Birliğe ait kullanıcılar
     */
    @OneToMany(mappedBy = "birlik", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Kullanici> kullanicilar = new HashSet<>();

    /**
     * Açıklama / Notlar
     */
    @Column(name = "aciklama", length = 1000)
    private String aciklama;

    /**
     * Alt birlik sayısını döndürür
     */
    public int getAltBirlikSayisi() {
        return altBirlikler != null ? altBirlikler.size() : 0;
    }

    /**
     * Toplam üye sayısını döndürür
     */
    public int getUyeSayisi() {
        return uyeler != null ? uyeler.size() : 0;
    }

    /**
     * Merkez birlik mi kontrolü
     */
    public boolean isMerkezBirlik() {
        return BirlikTipi.MERKEZ.equals(this.birlikTipi);
    }
}
