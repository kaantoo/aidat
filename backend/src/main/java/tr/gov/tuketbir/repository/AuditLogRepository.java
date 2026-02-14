package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.AuditLog;
import tr.gov.tuketbir.domain.enums.AuditIslemTipi;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Repository
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByKullaniciId(Long kullaniciId, Pageable pageable);
    
    Page<AuditLog> findByBirlikId(Long birlikId, Pageable pageable);
    
    Page<AuditLog> findByIslemTipi(AuditIslemTipi islemTipi, Pageable pageable);
    
    Page<AuditLog> findByIslemZamaniBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    Page<AuditLog> findByEntityTipiAndEntityId(String entityTipi, Long entityId, Pageable pageable);
    
    @Query("SELECT a FROM AuditLog a WHERE " +
           "a.birlikId = :birlikId AND " +
           "a.islemZamani BETWEEN :start AND :end " +
           "ORDER BY a.islemZamani DESC")
    Page<AuditLog> findByBirlikAndTarihAraligi(
        @Param("birlikId") Long birlikId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );
    
    @Query("SELECT a.islemTipi, COUNT(a) FROM AuditLog a " +
           "WHERE a.islemZamani BETWEEN :start AND :end " +
           "GROUP BY a.islemTipi")
    List<Object[]> countByIslemTipi(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT a FROM AuditLog a WHERE a.basarili = false ORDER BY a.islemZamani DESC")
    Page<AuditLog> findBasarisizIslemler(Pageable pageable);
}
