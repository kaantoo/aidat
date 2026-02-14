package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gelir-Gider Entity - Birlik mali hareketlerini temsil eder.
 * 
 * Gelir Kategorileri: Aidat, Bağış, Sponsorluk, Diğer
 * Gider Kategorileri: Kira, Kırtasiye, Etkinlik, Maaş, Diğer
 * 
 * Her kayıt bir birliğe bağlıdır.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "gelir_giderler", indexes = {
    @Index(name = "idx_gg_birlik", columnList = "birlik_id"),
    @Index(name = "idx_gg_tip", columnList = "tip"),
    @Index(name = "idx_gg_tarih", columnList = "islem_tarihi"),
    @Index(name = "idx_gg_kategori", columnList = "gelir_kategorisi, gider_kategorisi"),
    @Index(name = "idx_gg_belge_no", columnList = "belge_no")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GelirGider extends BaseEntity {

    /**
     * Bağlı birlik
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * İşlem tipi (Gelir / Gider)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tip", nullable = false, length = 10)
    private GelirGiderTipi tip;

    /**
     * Gelir kategorisi (tip = GELIR ise)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gelir_kategorisi", length = 30)
    private GelirKategorisi gelirKategorisi;

    /**
     * Gider kategorisi (tip = GIDER ise)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gider_kategorisi", length = 30)
    private GiderKategorisi giderKategorisi;

    /**
     * İşlem tarihi
     */
    @Column(name = "islem_tarihi", nullable = false)
    private LocalDate islemTarihi;

    /**
     * Tutar
     */
    @Column(name = "tutar", nullable = false, precision = 12, scale = 2)
    private BigDecimal tutar;

    /**
     * Belge / Fatura numarası
     */
    @Column(name = "belge_no", length = 50)
    private String belgeNo;

    /**
     * Açıklama
     */
    @Column(name = "aciklama", nullable = false, length = 500)
    private String aciklama;

    /**
     * Karşı taraf (kime ödendi / kimden alındı)
     */
    @Column(name = "karsi_taraf", length = 200)
    private String karsiTaraf;

    /**
     * Ödeme yöntemi
     */
    @Column(name = "odeme_yontemi", length = 50)
    private String odemeYontemi;

    /**
     * Banka adı
     */
    @Column(name = "banka_adi", length = 100)
    private String bankaAdi;

    /**
     * Dekont / Referans no
     */
    @Column(name = "referans_no", length = 50)
    private String referansNo;

    /**
     * İlişkili aidat (Gelir - Aidat kategorisi için)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aidat_id")
    private Aidat aidat;

    /**
     * İlişkili belge (fatura, makbuz taraması vb.)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belge_id")
    private Belge belge;

    /**
     * Kaydeden kullanıcı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaydeden_kullanici_id")
    private Kullanici kaydedenKullanici;

    /**
     * Notlar
     */
    @Column(name = "notlar", length = 1000)
    private String notlar;

    /**
     * Onay durumu
     */
    @Column(name = "onaylandi", nullable = false)
    @Builder.Default
    private Boolean onaylandi = false;

    /**
     * Onaylayan kullanıcı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "onaylayan_kullanici_id")
    private Kullanici onaylayanKullanici;

    /**
     * Gelir mi kontrolü
     */
    public boolean isGelir() {
        return GelirGiderTipi.GELIR.equals(this.tip);
    }

    /**
     * Gider mi kontrolü
     */
    public boolean isGider() {
        return GelirGiderTipi.GIDER.equals(this.tip);
    }

    /**
     * Kategori adını döndür
     */
    public String getKategoriAdi() {
        if (isGelir() && gelirKategorisi != null) {
            return gelirKategorisi.getAciklama();
        } else if (isGider() && giderKategorisi != null) {
            return giderKategorisi.getAciklama();
        }
        return "Belirtilmemiş";
    }
}
