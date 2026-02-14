package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.GelirGider;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Gelir Gider Repository
 */
@Repository
public interface GelirGiderRepository extends BaseRepository<GelirGider, Long> {

    List<GelirGider> findByBirlikId(Long birlikId);
    
    Page<GelirGider> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<GelirGider> findByBirlikIdAndTip(Long birlikId, GelirGiderTipi tip);
    
    Page<GelirGider> findByBirlikIdAndTip(Long birlikId, GelirGiderTipi tip, Pageable pageable);
    
    @Query("SELECT g FROM GelirGider g WHERE g.isActive = true ORDER BY g.islemTarihi DESC")
    Page<GelirGider> findByIsActiveTrueOrderByIslemTarihiDesc(Pageable pageable);
    
    @Query("SELECT g FROM GelirGider g WHERE g.tip = :tip AND g.isActive = true ORDER BY g.islemTarihi DESC")
    Page<GelirGider> findByTipAndIsActiveTrueOrderByIslemTarihiDesc(@Param("tip") GelirGiderTipi tip, Pageable pageable);
    
    @Query("SELECT g FROM GelirGider g WHERE g.birlik.id = :birlikId AND g.isActive = true ORDER BY g.islemTarihi DESC")
    Page<GelirGider> findByBirlikIdAndIsActiveTrueOrderByIslemTarihiDesc(@Param("birlikId") Long birlikId, Pageable pageable);
    
    @Query("SELECT g FROM GelirGider g WHERE g.tip = :tip AND g.birlik.id = :birlikId AND g.isActive = true ORDER BY g.islemTarihi DESC")
    Page<GelirGider> findByTipAndBirlikIdAndIsActiveTrueOrderByIslemTarihiDesc(
            @Param("tip") GelirGiderTipi tip, @Param("birlikId") Long birlikId, Pageable pageable);
    
    List<GelirGider> findByBirlikIdAndIslemTarihiBetween(Long birlikId, LocalDate start, LocalDate end);
    
    List<GelirGider> findByBirlikIdAndTipAndIslemTarihiBetween(
        Long birlikId, GelirGiderTipi tip, LocalDate start, LocalDate end);
    
    @Query("SELECT SUM(g.tutar) FROM GelirGider g WHERE g.birlik.id = :birlikId AND g.tip = 'GELIR'")
    BigDecimal sumGelirByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT SUM(g.tutar) FROM GelirGider g WHERE g.birlik.id = :birlikId AND g.tip = 'GIDER'")
    BigDecimal sumGiderByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT SUM(g.tutar) FROM GelirGider g WHERE g.tip = :tip AND g.isActive = true " +
           "AND (:birlikId IS NULL OR g.birlik.id = :birlikId) " +
           "AND g.islemTarihi BETWEEN :start AND :end")
    BigDecimal sumByTipAndBirlikIdAndDateRange(@Param("tip") GelirGiderTipi tip,
                                                 @Param("birlikId") Long birlikId,
                                                 @Param("start") LocalDate start, 
                                                 @Param("end") LocalDate end);
    
    @Query("SELECT SUM(g.tutar) FROM GelirGider g WHERE g.birlik.id = :birlikId AND g.tip = 'GELIR' " +
           "AND g.islemTarihi BETWEEN :start AND :end")
    BigDecimal sumGelirByBirlikAndTarih(@Param("birlikId") Long birlikId, 
                                         @Param("start") LocalDate start, 
                                         @Param("end") LocalDate end);
    
    @Query("SELECT SUM(g.tutar) FROM GelirGider g WHERE g.birlik.id = :birlikId AND g.tip = 'GIDER' " +
           "AND g.islemTarihi BETWEEN :start AND :end")
    BigDecimal sumGiderByBirlikAndTarih(@Param("birlikId") Long birlikId, 
                                         @Param("start") LocalDate start, 
                                         @Param("end") LocalDate end);
    
    @Query("SELECT g.gelirKategorisi, SUM(g.tutar) FROM GelirGider g " +
           "WHERE g.birlik.id = :birlikId AND g.tip = 'GELIR' GROUP BY g.gelirKategorisi")
    List<Object[]> sumGelirByKategori(@Param("birlikId") Long birlikId);
    
    @Query("SELECT g.giderKategorisi, SUM(g.tutar) FROM GelirGider g " +
           "WHERE g.birlik.id = :birlikId AND g.tip = 'GIDER' GROUP BY g.giderKategorisi")
    List<Object[]> sumGiderByKategori(@Param("birlikId") Long birlikId);
    
    @Query("SELECT FUNCTION('YEAR', g.islemTarihi), FUNCTION('MONTH', g.islemTarihi), g.tip, SUM(g.tutar) " +
           "FROM GelirGider g WHERE g.birlik.id = :birlikId " +
           "GROUP BY FUNCTION('YEAR', g.islemTarihi), FUNCTION('MONTH', g.islemTarihi), g.tip " +
           "ORDER BY FUNCTION('YEAR', g.islemTarihi), FUNCTION('MONTH', g.islemTarihi)")
    List<Object[]> getAylikOzet(@Param("birlikId") Long birlikId);
    
    // Tüm gelir/gider toplamları
    @Query("SELECT COALESCE(SUM(g.tutar), 0) FROM GelirGider g WHERE g.tip = 'GELIR' AND g.isActive = true")
    BigDecimal sumAllGelir();
    
    @Query("SELECT COALESCE(SUM(g.tutar), 0) FROM GelirGider g WHERE g.tip = 'GIDER' AND g.isActive = true")
    BigDecimal sumAllGider();
    
    // Yıl ve ay bazlı gelir/gider - Native query PostgreSQL için
    @Query(value = "SELECT COALESCE(SUM(tutar), 0) FROM gelir_giderler WHERE tip = 'GELIR' AND is_active = true " +
           "AND EXTRACT(YEAR FROM islem_tarihi) = :yil AND EXTRACT(MONTH FROM islem_tarihi) = :ay", nativeQuery = true)
    BigDecimal sumGelirByYilAy(@Param("yil") Integer yil, @Param("ay") Integer ay);
    
    @Query(value = "SELECT COALESCE(SUM(tutar), 0) FROM gelir_giderler WHERE tip = 'GIDER' AND is_active = true " +
           "AND EXTRACT(YEAR FROM islem_tarihi) = :yil AND EXTRACT(MONTH FROM islem_tarihi) = :ay", nativeQuery = true)
    BigDecimal sumGiderByYilAy(@Param("yil") Integer yil, @Param("ay") Integer ay);
    
    // Birlik bazlı yıl ve ay toplamları - Native query PostgreSQL için
    @Query(value = "SELECT COALESCE(SUM(tutar), 0) FROM gelir_giderler WHERE tip = 'GELIR' AND is_active = true " +
           "AND birlik_id = :birlikId AND EXTRACT(YEAR FROM islem_tarihi) = :yil AND EXTRACT(MONTH FROM islem_tarihi) = :ay", nativeQuery = true)
    BigDecimal sumGelirByBirlikYilAy(@Param("birlikId") Long birlikId, @Param("yil") Integer yil, @Param("ay") Integer ay);
    
    @Query(value = "SELECT COALESCE(SUM(tutar), 0) FROM gelir_giderler WHERE tip = 'GIDER' AND is_active = true " +
           "AND birlik_id = :birlikId AND EXTRACT(YEAR FROM islem_tarihi) = :yil AND EXTRACT(MONTH FROM islem_tarihi) = :ay", nativeQuery = true)
    BigDecimal sumGiderByBirlikYilAy(@Param("birlikId") Long birlikId, @Param("yil") Integer yil, @Param("ay") Integer ay);
}
