package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;

/**
 * Toplantı Katılımcı Entity - Toplantılara katılan üyeleri temsil eder.
 * 
 * Her katılımcı bir toplantıya ve opsiyonel olarak bir üyeye bağlıdır.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "toplanti_katilimcilar", indexes = {
    @Index(name = "idx_katilimci_toplanti", columnList = "toplanti_id"),
    @Index(name = "idx_katilimci_uye", columnList = "uye_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_toplanti_uye", columnNames = {"toplanti_id", "uye_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToplantiKatilimci extends BaseEntity {

    /**
     * Bağlı toplantı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toplanti_id", nullable = false)
    private Toplanti toplanti;

    /**
     * Katılımcı üye (opsiyonel - üye kayıtlarıyla eşleşme)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uye_id")
    private Uye uye;

    /**
     * Katılımcı adı (üye dışı kişiler için)
     */
    @Column(name = "ad_soyad", nullable = false, length = 150)
    private String adSoyad;

    /**
     * Katılımcının görevi / unvanı
     */
    @Column(name = "gorev", length = 100)
    private String gorev;

    /**
     * Katılım durumu
     */
    @Column(name = "katildi", nullable = false)
    @Builder.Default
    private Boolean katildi = true;

    /**
     * Mazereti varsa açıklaması
     */
    @Column(name = "mazeret", length = 500)
    private String mazeret;

    /**
     * İmza durumu
     */
    @Column(name = "imzaladi", nullable = false)
    @Builder.Default
    private Boolean imzaladi = false;
}
