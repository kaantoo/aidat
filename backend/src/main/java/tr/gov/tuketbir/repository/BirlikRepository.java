package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.enums.BirlikTipi;

import java.util.List;
import java.util.Optional;

/**
 * Birlik Repository
 */
@Repository
public interface BirlikRepository extends BaseRepository<Birlik, Long> {

    Optional<Birlik> findByBirlikKodu(String birlikKodu);
    
    boolean existsByBirlikKodu(String birlikKodu);
    
    List<Birlik> findByBirlikTipi(BirlikTipi birlikTipi);
    
    List<Birlik> findByParentBirlikIsNull();
    
    List<Birlik> findByParentBirlikId(Long parentBirlikId);
    
    List<Birlik> findByIlKodu(String ilKodu);
    
    List<Birlik> findByIlKoduAndIlceKodu(String ilKodu, String ilceKodu);
    
    List<Birlik> findByIlKoduOrderByBirlikAdiAsc(String ilKodu);
    
    @Query("SELECT b FROM Birlik b WHERE b.ilKodu = :ilKodu AND b.isActive = :isActive ORDER BY b.birlikAdi")
    List<Birlik> findByIlKoduAndIsActiveOrderByBirlikAdiAsc(@Param("ilKodu") String ilKodu, @Param("isActive") Boolean isActive);
    
    @Query("SELECT b FROM Birlik b WHERE b.isActive = :isActive ORDER BY b.birlikAdi")
    List<Birlik> findByIsActiveOrderByBirlikAdiAsc(@Param("isActive") Boolean isActive);
    
    List<Birlik> findAllByOrderByBirlikAdiAsc();
    
    @Query("SELECT b FROM Birlik b WHERE b.parentBirlik.id = :ustBirlikId AND b.isActive = true")
    List<Birlik> findByUstBirlikIdAndIsActiveTrue(@Param("ustBirlikId") Long ustBirlikId);
    
    @Query("SELECT b FROM Birlik b WHERE b.parentBirlik IS NULL AND b.isActive = true")
    List<Birlik> findByUstBirlikIsNullAndIsActiveTrue();
    
    @Query("SELECT COUNT(b) FROM Birlik b WHERE b.isActive = true")
    Long countByIsActiveTrue();
    
    @Query("SELECT COUNT(b) FROM Birlik b WHERE b.birlikTipi = :birlikTipi AND b.isActive = true")
    Long countByBirlikTipiAndIsActiveTrue(@Param("birlikTipi") BirlikTipi birlikTipi);
    
    @Query("SELECT b FROM Birlik b WHERE b.isActive = true AND b.birlikTipi = :tip")
    List<Birlik> findAktifBirlikler(@Param("tip") BirlikTipi tip);
    
    @Query("SELECT b FROM Birlik b WHERE b.isActive = true ORDER BY b.ilKodu, b.birlikAdi")
    List<Birlik> findAllAktifOrderByIl();
    
    @Query("SELECT COUNT(u) FROM Uye u WHERE u.birlik.id = :birlikId AND u.uyeDurum = 'AKTIF'")
    Long countAktifUyelerByBirlikId(@Param("birlikId") Long birlikId);
    
    @Query("SELECT b FROM Birlik b WHERE " +
           "LOWER(b.birlikAdi) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.birlikKodu) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Birlik> search(@Param("searchTerm") String searchTerm, Pageable pageable);
}
