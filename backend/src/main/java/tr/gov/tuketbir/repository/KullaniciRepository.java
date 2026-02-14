package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.domain.enums.KullaniciRol;

import java.util.List;
import java.util.Optional;

/**
 * Kullanıcı Repository
 */
@Repository
public interface KullaniciRepository extends BaseRepository<Kullanici, Long> {

    Optional<Kullanici> findByKullaniciAdi(String kullaniciAdi);
    
    Optional<Kullanici> findByEmail(String email);
    
    boolean existsByKullaniciAdi(String kullaniciAdi);
    
    boolean existsByEmail(String email);
    
    List<Kullanici> findByBirlikId(Long birlikId);
    
    Page<Kullanici> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<Kullanici> findByRol(KullaniciRol rol);
    
    List<Kullanici> findByDurum(KullaniciDurum durum);
    
    List<Kullanici> findByBirlikIdAndDurum(Long birlikId, KullaniciDurum durum);
    
    Page<Kullanici> findByRol(KullaniciRol rol, Pageable pageable);
    
    Page<Kullanici> findByBirlikIdAndRol(Long birlikId, KullaniciRol rol, Pageable pageable);
    
    @Query("SELECT k FROM Kullanici k WHERE k.isActive = true AND k.durum = 'AKTIF'")
    List<Kullanici> findAllAktif();
    
    @Query("SELECT k FROM Kullanici k WHERE " +
           "LOWER(k.ad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(k.soyad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(k.kullaniciAdi) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(k.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Kullanici> search(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    Optional<Kullanici> findBySifreSifirlamaToken(String token);
    
    @Query("SELECT k.rol, COUNT(k) FROM Kullanici k WHERE k.durum = 'AKTIF' GROUP BY k.rol")
    List<Object[]> countByRol();
}
