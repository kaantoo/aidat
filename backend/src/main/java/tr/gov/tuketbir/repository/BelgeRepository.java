package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Belge;
import tr.gov.tuketbir.domain.enums.BelgeTipi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Belge Repository
 */
@Repository
public interface BelgeRepository extends BaseRepository<Belge, Long> {

    Optional<Belge> findByBelgeNo(String belgeNo);
    
    boolean existsByBelgeNo(String belgeNo);
    
    List<Belge> findByBirlikId(Long birlikId);
    
    Page<Belge> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<Belge> findByUyeId(Long uyeId);
    
    Page<Belge> findByUyeId(Long uyeId, Pageable pageable);
    
    List<Belge> findByAidatId(Long aidatId);
    
    List<Belge> findByBirlikIdAndBelgeTipi(Long birlikId, BelgeTipi belgeTipi);
    
    Page<Belge> findByBirlikIdAndBelgeTipi(Long birlikId, BelgeTipi belgeTipi, Pageable pageable);
    
    // Yeni eklenen metodlar
    @Query("SELECT b FROM Belge b WHERE b.isActive = true")
    Page<Belge> findByIsActiveTrue(Pageable pageable);
    
    @Query("SELECT b FROM Belge b WHERE b.uye.id = :uyeId AND b.isActive = true")
    Page<Belge> findByUyeIdAndIsActiveTrue(@Param("uyeId") Long uyeId, Pageable pageable);
    
    @Query("SELECT b FROM Belge b WHERE b.uye.id = :uyeId AND b.isActive = true")
    List<Belge> findListByUyeIdAndIsActiveTrue(@Param("uyeId") Long uyeId);
    
    @Query("SELECT b FROM Belge b WHERE b.birlik.id = :birlikId AND b.isActive = true")
    Page<Belge> findByBirlikIdAndIsActiveTrue(@Param("birlikId") Long birlikId, Pageable pageable);
    
    @Query("SELECT b FROM Belge b WHERE b.birlik.id = :birlikId AND b.isActive = true")
    List<Belge> findListByBirlikIdAndIsActiveTrue(@Param("birlikId") Long birlikId);
    
    @Query("SELECT b FROM Belge b WHERE b.birlik.id = :birlikId AND b.belgeTipi = :belgeTipi AND b.isActive = true")
    Page<Belge> findByBirlikIdAndBelgeTipiAndIsActiveTrue(@Param("birlikId") Long birlikId, @Param("belgeTipi") BelgeTipi belgeTipi, Pageable pageable);
    
    @Query("SELECT b FROM Belge b WHERE b.birlik.id = :birlikId AND " +
           "(LOWER(b.aciklama) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "b.belgeNo LIKE CONCAT('%', :searchTerm, '%'))")
    Page<Belge> searchInBirlik(@Param("birlikId") Long birlikId, 
                                @Param("searchTerm") String searchTerm, 
                                Pageable pageable);
    
    @Query("SELECT MAX(CAST(SUBSTRING(b.belgeNo, LENGTH(:prefix) + 1) AS int)) " +
           "FROM Belge b WHERE b.belgeNo LIKE CONCAT(:prefix, '%')")
    Integer findMaxBelgeNoByPrefix(@Param("prefix") String prefix);
    
    @Query("SELECT b.belgeTipi, COUNT(b) FROM Belge b WHERE b.birlik.id = :birlikId GROUP BY b.belgeTipi")
    List<Object[]> countByBelgeTipi(@Param("birlikId") Long birlikId);
}
