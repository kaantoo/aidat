package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.DonemTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Aidat Dönemi Entity - Aidat tahakkuk dönemlerini temsil eder.
 * 
 * Dönem Mantığı:
 * - 6 aylık veya yıllık dönemler tanımlanabilir
 * - Her dönem için tutar belirlenir
 * - Asgari ücret değişimlerinde açıklama eklenebilir
 * - Tüm aktif üyelere toplu atama yapılabilir
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "aidat_donemleri", indexes = {
    @Index(name = "idx_donem_kod", columnList = "donem_kodu", unique = true),
    @Index(name = "idx_donem_birlik", columnList = "birlik_id"),
    @Index(name = "idx_donem_tarih", columnList = "baslangic_tarihi, bitis_tarihi")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AidatDonemi extends BaseEntity {

    /**
     * Dönem kodu (örn: 2025/01, 2025/02, 2025-YILLIK)
     */
    @Column(name = "donem_kodu", nullable = false, unique = true, length = 20)
    private String donemKodu;

    /**
     * Dönem adı (örn: "2025 Yılı 1. Yarıyıl Aidatı")
     */
    @Column(name = "donem_adi", nullable = false, length = 200)
    private String donemAdi;

    /**
     * Bağlı birlik (null ise merkez birlik tarafından tüm birlikler için geçerli)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id")
    private Birlik birlik;

    /**
     * Dönem tipi (6 aylık / yıllık)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "donem_tipi", nullable = false, length = 20)
    private DonemTipi donemTipi;

    /**
     * Yıl
     */
    @Column(name = "yil", nullable = false)
    private Integer yil;

    /**
     * Dönem başlangıç tarihi
     */
    @Column(name = "baslangic_tarihi", nullable = false)
    private LocalDate baslangicTarihi;

    /**
     * Dönem bitiş tarihi
     */
    @Column(name = "bitis_tarihi", nullable = false)
    private LocalDate bitisTarihi;

    /**
     * Son ödeme tarihi
     */
    @Column(name = "son_odeme_tarihi", nullable = false)
    private LocalDate sonOdemeTarihi;

    /**
     * Aidat tutarı (Alt birlikler için zorunlu)
     * Merkez birlik için bu alan kullanılmaz, merkezPayOrani kullanılır
     */
    @Column(name = "tutar", precision = 12, scale = 2)
    private BigDecimal tutar;

    /**
     * Merkez pay oranı (%) - Sadece merkez birlik dönemleri için
     * Alt birlik tahakkuklarından alınacak pay oranı
     * Örn: %10 için 10.00
     */
    @Column(name = "merkez_pay_orani", precision = 5, scale = 2)
    private BigDecimal merkezPayOrani;

    /**
     * Gecikme faizi oranı (%)
     */
    @Column(name = "gecikme_faizi_orani", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gecikmeFaiziOrani = BigDecimal.ZERO;

    /**
     * Asgari ücret değişimi açıklaması
     */
    @Column(name = "asgari_ucret_aciklama", length = 500)
    private String asgariUcretAciklama;

    /**
     * Dönem açıklaması / notlar
     */
    @Column(name = "aciklama", length = 1000)
    private String aciklama;

    /**
     * Dönem aktif mi (tahakkuk yapılabilir mi)
     */
    @Column(name = "donem_aktif", nullable = false)
    @Builder.Default
    private Boolean donemAktif = true;

    /**
     * Döneme ait aidatlar
     */
    @OneToMany(mappedBy = "aidatDonemi", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Aidat> aidatlar = new HashSet<>();

    /**
     * Dönem geçmiş mi kontrolü
     */
    public boolean isGecmis() {
        return LocalDate.now().isAfter(this.bitisTarihi);
    }

    /**
     * Son ödeme tarihi geçmiş mi
     */
    public boolean isSonOdemeTarihiGecmis() {
        return LocalDate.now().isAfter(this.sonOdemeTarihi);
    }
}
