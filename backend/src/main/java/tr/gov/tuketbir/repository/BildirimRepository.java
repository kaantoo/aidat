package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Bildirim;
import tr.gov.tuketbir.domain.enums.BildirimTipi;

import java.util.List;

/**
 * Bildirim Repository
 */
@Repository
public interface BildirimRepository extends BaseRepository<Bildirim, Long> {

    // Kullanıcıya ait bildirimler
    Page<Bildirim> findByKullaniciIdOrderByCreatedAtDesc(Long kullaniciId, Pageable pageable);
    
    List<Bildirim> findByKullaniciIdAndOkunduFalseOrderByCreatedAtDesc(Long kullaniciId);
    
    // Birliğe ait bildirimler
    Page<Bildirim> findByBirlikIdOrderByCreatedAtDesc(Long birlikId, Pageable pageable);
    
    List<Bildirim> findByBirlikIdAndOkunduFalseOrderByCreatedAtDesc(Long birlikId);
    
    // Okunmamış bildirim sayısı
    @Query("SELECT COUNT(b) FROM Bildirim b WHERE b.kullanici.id = :kullaniciId AND b.okundu = false")
    Long countUnreadByKullaniciId(@Param("kullaniciId") Long kullaniciId);
    
    @Query("SELECT COUNT(b) FROM Bildirim b WHERE b.birlik.id = :birlikId AND b.okundu = false")
    Long countUnreadByBirlikId(@Param("birlikId") Long birlikId);
    
    // Tip bazlı bildirimler
    List<Bildirim> findByKullaniciIdAndTipOrderByCreatedAtDesc(Long kullaniciId, BildirimTipi tip);
    
    // Tümünü okundu işaretle
    @Modifying
    @Query("UPDATE Bildirim b SET b.okundu = true, b.okunmaTarihi = CURRENT_TIMESTAMP WHERE b.kullanici.id = :kullaniciId AND b.okundu = false")
    int markAllAsReadByKullaniciId(@Param("kullaniciId") Long kullaniciId);
    
    @Modifying
    @Query("UPDATE Bildirim b SET b.okundu = true, b.okunmaTarihi = CURRENT_TIMESTAMP WHERE b.birlik.id = :birlikId AND b.okundu = false")
    int markAllAsReadByBirlikId(@Param("birlikId") Long birlikId);
    
    // Son X bildirim
    @Query("SELECT b FROM Bildirim b WHERE b.kullanici.id = :kullaniciId ORDER BY b.createdAt DESC")
    List<Bildirim> findTopByKullaniciId(@Param("kullaniciId") Long kullaniciId, Pageable pageable);
    
    // Entity bazlı bildirimler
    List<Bildirim> findByEntityTipiAndEntityId(String entityTipi, Long entityId);
}
