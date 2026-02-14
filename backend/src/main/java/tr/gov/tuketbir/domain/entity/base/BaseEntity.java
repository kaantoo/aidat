package tr.gov.tuketbir.domain.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Tüm entity'ler için temel sınıf.
 * Audit bilgilerini ve soft-delete mantığını içerir.
 * 
 * @author Tuketbir Development Team
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Multi-tenant yapısı için birlik ID'si.
     * Merkez birlik için 0, alt birlikler için kendi ID'leri kullanılır.
     */
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * Soft delete için aktiflik durumu.
     * Kayıtlar silinmez, pasifleştirilir.
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Soft delete tarihi
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    /**
     * Soft delete işlemi
     */
    public void softDelete() {
        this.isActive = false;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Kaydı tekrar aktif hale getir
     */
    public void restore() {
        this.isActive = true;
        this.deletedAt = null;
    }
}
