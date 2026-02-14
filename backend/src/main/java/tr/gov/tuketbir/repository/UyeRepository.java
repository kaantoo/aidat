package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Uye;
import tr.gov.tuketbir.domain.enums.UyeDurum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Üye Repository
 */
@Repository
public interface UyeRepository extends BaseRepository<Uye, Long> {

    Optional<Uye> findByUyeNo(String uyeNo);
    
    Optional<Uye> findByTcKimlikNo(String tcKimlikNo);
    
    boolean existsByTcKimlikNo(String tcKimlikNo);
    
    boolean existsByUyeNo(String uyeNo);
    
    List<Uye> findByBirlikId(Long birlikId);
    
    Page<Uye> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<Uye> findByBirlikIdAndUyeDurum(Long birlikId, UyeDurum durum);
    
    List<Uye> findByUyeDurum(UyeDurum durum);
    
    Page<Uye> findByBirlikIdAndUyeDurum(Long birlikId, UyeDurum durum, Pageable pageable);
    
    List<Uye> findByIlKodu(String ilKodu);
    
    List<Uye> findByIlKoduAndIlceKodu(String ilKodu, String ilceKodu);
    
    @Query("SELECT u FROM Uye u WHERE u.tenantId = :tenantId AND u.uyeDurum = 'AKTIF'")
    List<Uye> findAktifUyelerByTenant(@Param("tenantId") Long tenantId);
    
    @Query("SELECT COUNT(u) FROM Uye u WHERE u.birlik.id = :birlikId AND u.uyeDurum = :durum")
    Long countByBirlikIdAndDurum(@Param("birlikId") Long birlikId, @Param("durum") UyeDurum durum);
    
    @Query("SELECT COUNT(u) FROM Uye u WHERE u.birlik.id = :birlikId")
    Long countByBirlikId(@Param("birlikId") Long birlikId);
    
    @Query("SELECT COUNT(u) FROM Uye u WHERE u.tenantId = :tenantId AND u.uyeDurum = 'AKTIF'")
    Long countAktifByTenant(@Param("tenantId") Long tenantId);
    
    @Query("SELECT u FROM Uye u WHERE " +
           "u.birlik.id = :birlikId AND (" +
           "LOWER(u.ad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.soyad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "u.tcKimlikNo LIKE CONCAT('%', :searchTerm, '%') OR " +
           "u.uyeNo LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Uye> searchInBirlik(@Param("birlikId") Long birlikId, 
                             @Param("searchTerm") String searchTerm, 
                             Pageable pageable);
    
    @Query("SELECT u FROM Uye u WHERE " +
           "LOWER(u.ad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.soyad) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "u.tcKimlikNo LIKE CONCAT('%', :searchTerm, '%') OR " +
           "u.uyeNo LIKE CONCAT('%', :searchTerm, '%')")
    Page<Uye> searchAll(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT MAX(CAST(SUBSTRING(u.uyeNo, LENGTH(:prefix) + 1) AS int)) " +
           "FROM Uye u WHERE u.uyeNo LIKE CONCAT(:prefix, '%')")
    Integer findMaxUyeNoByPrefix(@Param("prefix") String prefix);
    
    @Query("SELECT u FROM Uye u WHERE u.katilimTarihi BETWEEN :startDate AND :endDate")
    List<Uye> findByKatilimTarihiBetween(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    @Query("SELECT u.ilKodu, COUNT(u) FROM Uye u WHERE u.uyeDurum = 'AKTIF' GROUP BY u.ilKodu")
    List<Object[]> countAktifUyelerByIl();
    
    @Query("SELECT u.birlik.id, COUNT(u) FROM Uye u WHERE u.uyeDurum = 'AKTIF' GROUP BY u.birlik.id")
    List<Object[]> countAktifUyelerByBirlik();
    
    @Query("SELECT COUNT(u) FROM Uye u WHERE u.uyeDurum = :durum")
    Long countByUyeDurum(@Param("durum") UyeDurum durum);
}
