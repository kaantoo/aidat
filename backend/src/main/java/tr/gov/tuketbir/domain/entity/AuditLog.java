package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.enums.AuditIslemTipi;

import java.time.LocalDateTime;

/**
 * Audit Log Entity - Tüm sistem işlemlerinin kaydını tutar.
 * 
 * Denetim ve güvenlik amacıyla tüm önemli işlemler loglanır:
 * - Giriş/çıkış işlemleri
 * - Veri ekleme/güncelleme/silme
 * - Rapor oluşturma
 * - Belge yükleme/indirme
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_kullanici", columnList = "kullanici_id"),
    @Index(name = "idx_audit_tarih", columnList = "islem_zamani"),
    @Index(name = "idx_audit_tip", columnList = "islem_tipi"),
    @Index(name = "idx_audit_entity", columnList = "entity_tipi, entity_id"),
    @Index(name = "idx_audit_birlik", columnList = "birlik_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * İşlemi yapan kullanıcı ID
     */
    @Column(name = "kullanici_id")
    private Long kullaniciId;

    /**
     * Kullanıcı adı (denormalize)
     */
    @Column(name = "kullanici_adi", length = 100)
    private String kullaniciAdi;

    /**
     * Birlik ID
     */
    @Column(name = "birlik_id")
    private Long birlikId;

    /**
     * İşlem tipi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "islem_tipi", nullable = false, length = 30)
    private AuditIslemTipi islemTipi;

    /**
     * İşlem zamanı
     */
    @Column(name = "islem_zamani", nullable = false)
    private LocalDateTime islemZamani;

    /**
     * Etkilenen entity tipi (Uye, Aidat, Belge vb.)
     */
    @Column(name = "entity_tipi", length = 50)
    private String entityTipi;

    /**
     * Etkilenen entity ID
     */
    @Column(name = "entity_id")
    private Long entityId;

    /**
     * İşlem açıklaması
     */
    @Column(name = "aciklama", nullable = false, length = 500)
    private String aciklama;

    /**
     * Eski değerler (JSON)
     */
    @Column(name = "eski_degerler", columnDefinition = "TEXT")
    private String eskiDegerler;

    /**
     * Yeni değerler (JSON)
     */
    @Column(name = "yeni_degerler", columnDefinition = "TEXT")
    private String yeniDegerler;

    /**
     * IP adresi
     */
    @Column(name = "ip_adresi", length = 50)
    private String ipAdresi;

    /**
     * User agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Request URL
     */
    @Column(name = "request_url", length = 500)
    private String requestUrl;

    /**
     * HTTP metodu
     */
    @Column(name = "http_metod", length = 10)
    private String httpMetod;

    /**
     * İşlem süresi (ms)
     */
    @Column(name = "islem_suresi")
    private Long islemSuresi;

    /**
     * Başarılı mı
     */
    @Column(name = "basarili", nullable = false)
    @Builder.Default
    private Boolean basarili = true;

    /**
     * Hata mesajı (başarısız işlemler için)
     */
    @Column(name = "hata_mesaji", length = 2000)
    private String hataMesaji;

    @PrePersist
    protected void onCreate() {
        if (this.islemZamani == null) {
            this.islemZamani = LocalDateTime.now();
        }
    }
}
