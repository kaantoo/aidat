package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.BelgeTipi;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Belge Entity - Dijital belge arşivini temsil eder.
 * 
 * Belge Türleri: Makbuz, Rapor, Dilekçe, Fatura, Dekont, Diğer
 * Desteklenen Formatlar: PDF, JPG, PNG
 * 
 * Belgeler:
 * - Bir birliğe
 * - Opsiyonel olarak bir üyeye
 * - Opsiyonel olarak bir aidata bağlanabilir
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "belgeler", indexes = {
    @Index(name = "idx_belge_no", columnList = "belge_no"),
    @Index(name = "idx_belge_birlik", columnList = "birlik_id"),
    @Index(name = "idx_belge_uye", columnList = "uye_id"),
    @Index(name = "idx_belge_tip", columnList = "belge_tipi"),
    @Index(name = "idx_belge_tarih", columnList = "belge_tarihi"),
    @Index(name = "idx_belge_il", columnList = "il_kodu")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Belge extends BaseEntity {

    /**
     * Belge numarası (sistem tarafından üretilir)
     */
    @Column(name = "belge_no", nullable = false, unique = true, length = 50)
    private String belgeNo;

    /**
     * Bağlı birlik
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * İlişkili üye (opsiyonel)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uye_id")
    private Uye uye;

    /**
     * İlişkili aidat (opsiyonel)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aidat_id")
    private Aidat aidat;

    /**
     * Belge tipi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "belge_tipi", nullable = false, length = 30)
    private BelgeTipi belgeTipi;

    /**
     * Belge başlığı
     */
    @Column(name = "baslik", nullable = false, length = 200)
    private String baslik;

    /**
     * Belge açıklaması
     */
    @Column(name = "aciklama", length = 1000)
    private String aciklama;

    /**
     * Belge tarihi
     */
    @Column(name = "belge_tarihi", nullable = false)
    private LocalDate belgeTarihi;

    /**
     * İl kodu (filtreleme için)
     */
    @Column(name = "il_kodu", length = 10)
    private String ilKodu;

    /**
     * İlçe kodu
     */
    @Column(name = "ilce_kodu", length = 10)
    private String ilceKodu;

    // ======================= Dosya Bilgileri =======================

    /**
     * Orijinal dosya adı
     */
    @Column(name = "dosya_adi", nullable = false, length = 255)
    private String dosyaAdi;

    /**
     * Depolama yolu (MinIO object key)
     */
    @Column(name = "dosya_yolu", nullable = false, length = 500)
    private String dosyaYolu;

    /**
     * Dosya boyutu (bytes)
     */
    @Column(name = "dosya_boyutu")
    private Long dosyaBoyutu;

    /**
     * MIME tipi
     */
    @Column(name = "mime_tipi", length = 100)
    private String mimeTipi;

    /**
     * Dosya hash (bütünlük kontrolü için)
     */
    @Column(name = "dosya_hash", length = 128)
    private String dosyaHash;

    /**
     * Yükleme zamanı
     */
    @Column(name = "yukleme_zamani", nullable = false)
    private LocalDateTime yuklemeZamani;

    /**
     * Yükleyen kullanıcı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yukleyen_kullanici_id")
    private Kullanici yukleyenKullanici;

    /**
     * Etiketler (virgülle ayrılmış)
     */
    @Column(name = "etiketler", length = 500)
    private String etiketler;

    /**
     * Gizli belge mi
     */
    @Column(name = "gizli", nullable = false)
    @Builder.Default
    private Boolean gizli = false;

    @PrePersist
    protected void onPrePersist() {
        if (this.yuklemeZamani == null) {
            this.yuklemeZamani = LocalDateTime.now();
        }
    }

    /**
     * Dosya uzantısını döndür
     */
    public String getDosyaUzantisi() {
        if (dosyaAdi != null && dosyaAdi.contains(".")) {
            return dosyaAdi.substring(dosyaAdi.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    /**
     * Boyutu okunabilir formatta döndür
     */
    public String getDosyaBoyutuFormatli() {
        if (dosyaBoyutu == null) return "0 B";
        
        if (dosyaBoyutu < 1024) return dosyaBoyutu + " B";
        if (dosyaBoyutu < 1024 * 1024) return String.format("%.2f KB", dosyaBoyutu / 1024.0);
        if (dosyaBoyutu < 1024 * 1024 * 1024) return String.format("%.2f MB", dosyaBoyutu / (1024.0 * 1024));
        return String.format("%.2f GB", dosyaBoyutu / (1024.0 * 1024 * 1024));
    }
}
