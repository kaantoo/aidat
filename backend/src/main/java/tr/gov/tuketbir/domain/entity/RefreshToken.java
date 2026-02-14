package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token Entity - JWT yenileme tokenlarını yönetir.
 * 
 * Her kullanıcı için birden fazla refresh token olabilir (farklı cihazlar).
 * Tokenlar belirli bir süre sonra expire olur.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_rt_token", columnList = "token", unique = true),
    @Index(name = "idx_rt_kullanici", columnList = "kullanici_id"),
    @Index(name = "idx_rt_expiry", columnList = "expiry_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Token değeri (UUID)
     */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /**
     * Token sahibi kullanıcı
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    /**
     * Son geçerlilik tarihi
     */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /**
     * Oluşturulma zamanı
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Cihaz bilgisi
     */
    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    /**
     * IP adresi
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * User agent
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * İptal edildi mi
     */
    @Column(name = "revoked", nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    /**
     * İptal tarihi
     */
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Token geçerli mi kontrolü
     */
    public boolean isValid() {
        return !this.revoked && LocalDateTime.now().isBefore(this.expiryDate);
    }

    /**
     * Token'ı iptal et
     */
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}
