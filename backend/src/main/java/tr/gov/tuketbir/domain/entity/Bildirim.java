package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.BildirimTipi;
import tr.gov.tuketbir.domain.enums.BildirimOnceligi;

import java.time.LocalDateTime;

/**
 * Bildirim Entity
 * Sistem bildirimleri için entity
 */
@Entity
@Table(name = "bildirimler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bildirim extends BaseEntity {

    @Column(nullable = false)
    private String baslik;

    @Column(length = 1000)
    private String mesaj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BildirimTipi tip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BildirimOnceligi oncelik = BildirimOnceligi.NORMAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id")
    private Kullanici kullanici;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id")
    private Birlik birlik;

    @Column(nullable = false)
    @Builder.Default
    private Boolean okundu = false;

    @Column
    private LocalDateTime okunmaTarihi;

    @Column
    private String link;

    @Column
    private String entityTipi;

    @Column
    private Long entityId;

    public void markAsRead() {
        this.okundu = true;
        this.okunmaTarihi = LocalDateTime.now();
    }
}
