package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.OdemeTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tahsilat Entity - Aidat ödemelerini temsil eder.
 * 
 * Her tahsilat bir aidata bağlıdır.
 * Kısmi ödeme desteklenir - aynı aidat için birden fazla tahsilat olabilir.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "tahsilatlar", indexes = {
    @Index(name = "idx_tahsilat_aidat", columnList = "aidat_id"),
    @Index(name = "idx_tahsilat_uye", columnList = "uye_id"),
    @Index(name = "idx_tahsilat_birlik", columnList = "birlik_id"),
    @Index(name = "idx_tahsilat_tarih", columnList = "odeme_tarihi"),
    @Index(name = "idx_tahsilat_makbuz", columnList = "makbuz_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tahsilat extends BaseEntity {

    /**
     * İlişkili aidat
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aidat_id", nullable = false)
    private Aidat aidat;

    /**
     * Ödeme yapan üye (denormalize)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uye_id", nullable = false)
    private Uye uye;

    /**
     * Birlik (denormalize)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * Makbuz numarası
     */
    @Column(name = "makbuz_no", length = 50)
    private String makbuzNo;

    /**
     * Ödeme tarihi
     */
    @Column(name = "odeme_tarihi", nullable = false)
    private LocalDate odemeTarihi;

    /**
     * Ödeme kaydı zamanı
     */
    @Column(name = "kayit_zamani", nullable = false)
    private LocalDateTime kayitZamani;

    /**
     * Ödenen tutar
     */
    @Column(name = "tutar", nullable = false, precision = 12, scale = 2)
    private BigDecimal tutar;

    /**
     * Ödeme tipi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "odeme_tipi", nullable = false, length = 20)
    private OdemeTipi odemeTipi;

    /**
     * Banka adı (havale/EFT için)
     */
    @Column(name = "banka_adi", length = 100)
    private String bankaAdi;

    /**
     * Dekont / Referans numarası
     */
    @Column(name = "dekont_no", length = 50)
    private String dekontNo;

    /**
     * Açıklama
     */
    @Column(name = "aciklama", length = 500)
    private String aciklama;

    /**
     * İlişkili belge (makbuz/dekont taranmış hali)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belge_id")
    private Belge belge;

    /**
     * Tahsilatı kaydeden kullanıcı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaydeden_kullanici_id")
    private Kullanici kaydedenKullanici;

    /**
     * İptal edildi mi
     */
    @Column(name = "iptal_edildi", nullable = false)
    @Builder.Default
    private Boolean iptalEdildi = false;

    /**
     * İptal tarihi
     */
    @Column(name = "iptal_tarihi")
    private LocalDateTime iptalTarihi;

    /**
     * İptal nedeni
     */
    @Column(name = "iptal_nedeni", length = 500)
    private String iptalNedeni;

    @PrePersist
    protected void onPrePersist() {
        if (this.kayitZamani == null) {
            this.kayitZamani = LocalDateTime.now();
        }
    }

    /**
     * Tahsilatı iptal et
     */
    public void iptalEt(String neden) {
        this.iptalEdildi = true;
        this.iptalTarihi = LocalDateTime.now();
        this.iptalNedeni = neden;
    }
}
