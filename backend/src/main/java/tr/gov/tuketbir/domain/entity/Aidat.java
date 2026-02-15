package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.AidatDurum;
import tr.gov.tuketbir.domain.enums.OdemeTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Aidat Entity - Üye aidat kayıtlarını temsil eder.
 * 
 * Her aidat kaydı:
 * - Bir üyeye
 * - Bir aidat dönemine bağlıdır
 * 
 * Ödeme Durumları:
 * - BEKLIYOR: Henüz ödeme yapılmamış
 * - KISMI_ODENDI: Kısmi ödeme yapılmış
 * - ODENDI: Tam ödeme yapılmış
 * - GECIKTI: Son ödeme tarihi geçmiş, ödenmemiş
 * - IPTAL: İptal edilmiş
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "aidatlar", indexes = {
    @Index(name = "idx_aidat_uye", columnList = "uye_id"),
    @Index(name = "idx_aidat_donem", columnList = "aidat_donemi_id"),
    @Index(name = "idx_aidat_durum", columnList = "aidat_durum"),
    @Index(name = "idx_aidat_birlik", columnList = "birlik_id"),
    @Index(name = "idx_aidat_tarih", columnList = "tahakkuk_tarihi")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_uye_donem", columnNames = {"uye_id", "aidat_donemi_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aidat extends BaseEntity {

    /**
     * Aidat sahibi üye
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uye_id", nullable = false)
    private Uye uye;

    /**
     * Aidat dönemi
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aidat_donemi_id", nullable = false)
    private AidatDonemi aidatDonemi;

    /**
     * Birlik (denormalize - sorgu performansı için)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * Tahakkuk tarihi
     */
    @Column(name = "tahakkuk_tarihi", nullable = false)
    private LocalDate tahakkukTarihi;

    /**
     * Tahakkuk tutarı (dönemdeki tutar)
     */
    @Column(name = "tahakkuk_tutari", nullable = false, precision = 12, scale = 2)
    private BigDecimal tahakkukTutari;

    /**
     * Gecikme faizi tutarı
     */
    @Column(name = "gecikme_faizi", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal gecikmeFaizi = BigDecimal.ZERO;

    /**
     * Toplam borç (tahakkuk + gecikme faizi)
     */
    @Column(name = "toplam_borc", nullable = false, precision = 12, scale = 2)
    private BigDecimal toplamBorc;

    /**
     * Ödenen tutar
     */
    @Column(name = "odenen_tutar", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal odenenTutar = BigDecimal.ZERO;

    /**
     * Kalan borç
     */
    @Column(name = "kalan_borc", precision = 12, scale = 2)
    private BigDecimal kalanBorc;

    /**
     * Aidat durumu
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "aidat_durum", nullable = false, length = 20)
    @Builder.Default
    private AidatDurum aidatDurum = AidatDurum.BEKLIYOR;

    /**
     * Son ödeme tarihi (dönemden kopyalanır)
     */
    @Column(name = "son_odeme_tarihi", nullable = false)
    private LocalDate sonOdemeTarihi;

    /**
     * Tam ödeme tarihi
     */
    @Column(name = "odeme_tamamlanma_tarihi")
    private LocalDateTime odemeTamamlanmaTarihi;

    /**
     * Açıklama / notlar
     */
    @Column(name = "aciklama", length = 1000)
    private String aciklama;

    /**
     * Aidatla ilişkili tahsilatlar
     */
    @OneToMany(mappedBy = "aidat", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Tahsilat> tahsilatlar = new HashSet<>();

    /**
     * İlişkili belgeler (makbuz, dekont vb.)
     */
    @OneToMany(mappedBy = "aidat", cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Belge> belgeler = new HashSet<>();

    // ======================= Yardımcı Metodlar =======================

    /**
     * Ödeme yap
     */
    public void odemeYap(BigDecimal tutar) {
        this.odenenTutar = this.odenenTutar.add(tutar);
        this.kalanBorc = this.toplamBorc.subtract(this.odenenTutar);
        
        if (this.kalanBorc.compareTo(BigDecimal.ZERO) <= 0) {
            this.aidatDurum = AidatDurum.ODENDI;
            this.kalanBorc = BigDecimal.ZERO;
            this.odemeTamamlanmaTarihi = LocalDateTime.now();
        } else {
            this.aidatDurum = AidatDurum.KISMI_ODENDI;
        }
    }

    /**
     * Gecikme faizi hesapla ve ekle
     */
    public void gecikmeFaiziHesapla(BigDecimal faiziOrani) {
        if (LocalDate.now().isAfter(this.sonOdemeTarihi) && this.kalanBorc.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal faiz = this.kalanBorc.multiply(faiziOrani).divide(BigDecimal.valueOf(100));
            this.gecikmeFaizi = this.gecikmeFaizi.add(faiz);
            this.toplamBorc = this.tahakkukTutari.add(this.gecikmeFaizi);
            this.kalanBorc = this.toplamBorc.subtract(this.odenenTutar);
            
            if (this.aidatDurum != AidatDurum.KISMI_ODENDI) {
                this.aidatDurum = AidatDurum.GECIKTI;
            }
        }
    }

    /**
     * Aidat gecikmiş mi
     */
    public boolean isGecikti() {
        return LocalDate.now().isAfter(this.sonOdemeTarihi) && 
               this.kalanBorc.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Aidat ödenmiş mi
     */
    public boolean isOdendi() {
        return AidatDurum.ODENDI.equals(this.aidatDurum);
    }

    @PrePersist
    @PreUpdate
    private void calculateTotals() {
        if (this.tahakkukTutari == null) {
            this.tahakkukTutari = BigDecimal.ZERO;
        }
        if (this.gecikmeFaizi == null) {
            this.gecikmeFaizi = BigDecimal.ZERO;
        }
        if (this.odenenTutar == null) {
            this.odenenTutar = BigDecimal.ZERO;
        }
        if (this.toplamBorc == null) {
            this.toplamBorc = this.tahakkukTutari.add(this.gecikmeFaizi);
        }
        if (this.kalanBorc == null) {
            this.kalanBorc = this.toplamBorc.subtract(this.odenenTutar);
        }
    }
}
