package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.KararDurumu;

/**
 * Karar Entity - Toplantı kararlarını temsil eder.
 * 
 * Her karar bir toplantıya bağlıdır.
 * Kararlar PDF olarak dışa aktarılabilir.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "kararlar", indexes = {
    @Index(name = "idx_karar_toplanti", columnList = "toplanti_id"),
    @Index(name = "idx_karar_no", columnList = "karar_no"),
    @Index(name = "idx_karar_durum", columnList = "durum")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Karar extends BaseEntity {

    /**
     * Karar numarası
     */
    @Column(name = "karar_no", nullable = false, length = 50)
    private String kararNo;

    /**
     * Bağlı toplantı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toplanti_id", nullable = false)
    private Toplanti toplanti;

    /**
     * Karar sırası (toplantı içindeki sıralama)
     */
    @Column(name = "karar_sirasi", nullable = false)
    @Builder.Default
    private Integer kararSirasi = 1;

    /**
     * Karar başlığı
     */
    @Column(name = "baslik", nullable = false, length = 300)
    private String baslik;

    /**
     * Karar metni (detaylı açıklama)
     */
    @Column(name = "karar_metni", nullable = false, columnDefinition = "TEXT")
    private String kararMetni;

    /**
     * Karar durumu
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false, length = 20)
    @Builder.Default
    private KararDurumu durum = KararDurumu.KABUL_EDILDI;

    /**
     * Oy birliği ile mi alındı
     */
    @Column(name = "oy_birligi")
    @Builder.Default
    private Boolean oyBirligi = true;

    /**
     * Kabul oyu sayısı
     */
    @Column(name = "kabul_oyu")
    private Integer kabulOyu;

    /**
     * Red oyu sayısı
     */
    @Column(name = "red_oyu")
    private Integer redOyu;

    /**
     * Çekimser oy sayısı
     */
    @Column(name = "cekimser_oyu")
    private Integer cekimserOyu;

    /**
     * Sorumlu kişi/birim
     */
    @Column(name = "sorumlu", length = 200)
    private String sorumlu;

    /**
     * Notlar
     */
    @Column(name = "notlar", columnDefinition = "TEXT")
    private String notlar;
}
