package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Toplanti;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Toplantı Repository
 */
@Repository
public interface ToplantiRepository extends BaseRepository<Toplanti, Long> {

    Optional<Toplanti> findByToplantiNo(String toplantiNo);

    @Query("SELECT t FROM Toplanti t LEFT JOIN FETCH t.kararlar LEFT JOIN FETCH t.katilimcilar WHERE t.id = :id AND t.isActive = true")
    Optional<Toplanti> findByIdWithDetails(@Param("id") Long id);

    Page<Toplanti> findByBirlikIdAndIsActiveTrue(Long birlikId, Pageable pageable);

    Page<Toplanti> findByIsActiveTrue(Pageable pageable);

    @Query("SELECT t FROM Toplanti t WHERE t.isActive = true " +
           "AND (:birlikId IS NULL OR t.birlik.id = :birlikId) " +
           "AND (:toplantiTuru IS NULL OR t.toplantiTuru = :toplantiTuru) " +
           "AND (:durum IS NULL OR t.durum = :durum) " +
           "AND (:baslangicTarihi IS NULL OR t.toplantiTarihi >= :baslangicTarihi) " +
           "AND (:bitisTarihi IS NULL OR t.toplantiTarihi <= :bitisTarihi)")
    Page<Toplanti> findByFilters(
            @Param("birlikId") Long birlikId,
            @Param("toplantiTuru") ToplantiTuru toplantiTuru,
            @Param("durum") ToplantiDurumu durum,
            @Param("baslangicTarihi") LocalDate baslangicTarihi,
            @Param("bitisTarihi") LocalDate bitisTarihi,
            Pageable pageable);

    @Query("SELECT t FROM Toplanti t WHERE t.birlik.id = :birlikId AND t.isActive = true ORDER BY t.toplantiTarihi DESC")
    List<Toplanti> findRecentByBirlik(@Param("birlikId") Long birlikId, Pageable pageable);

    @Query("SELECT COUNT(t) FROM Toplanti t WHERE t.birlik.id = :birlikId AND t.isActive = true")
    long countByBirlikId(@Param("birlikId") Long birlikId);

    @Query("SELECT MAX(CAST(SUBSTRING(t.toplantiNo, LENGTH(:prefix) + 1) AS int)) FROM Toplanti t WHERE t.toplantiNo LIKE CONCAT(:prefix, '%')")
    Integer findMaxToplantiNoByPrefix(@Param("prefix") String prefix);
}
