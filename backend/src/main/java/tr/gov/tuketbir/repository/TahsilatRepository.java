package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Tahsilat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Tahsilat Repository
 */
@Repository
public interface TahsilatRepository extends BaseRepository<Tahsilat, Long> {

    List<Tahsilat> findByAidatId(Long aidatId);
    
    List<Tahsilat> findByUyeId(Long uyeId);
    
    Page<Tahsilat> findByUyeId(Long uyeId, Pageable pageable);
    
    List<Tahsilat> findByBirlikId(Long birlikId);
    
    Page<Tahsilat> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<Tahsilat> findByBirlikIdAndOdemeTarihiBetween(Long birlikId, LocalDate start, LocalDate end);
    
    Page<Tahsilat> findByBirlikIdAndIptalEdildiFalse(Long birlikId, Pageable pageable);
    
    @Query("SELECT SUM(t.tutar) FROM Tahsilat t WHERE t.birlik.id = :birlikId AND t.iptalEdildi = false")
    BigDecimal sumTutarByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT SUM(t.tutar) FROM Tahsilat t WHERE t.birlik.id = :birlikId AND t.iptalEdildi = false " +
           "AND t.odemeTarihi BETWEEN :start AND :end")
    BigDecimal sumTutarByBirlikAndTarih(@Param("birlikId") Long birlikId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);
    
    @Query("SELECT t.odemeTipi, COUNT(t), SUM(t.tutar) FROM Tahsilat t " +
           "WHERE t.birlik.id = :birlikId AND t.iptalEdildi = false " +
           "GROUP BY t.odemeTipi")
    List<Object[]> sumByOdemeTipi(@Param("birlikId") Long birlikId);
    
    @Query("SELECT FUNCTION('YEAR', t.odemeTarihi), FUNCTION('MONTH', t.odemeTarihi), SUM(t.tutar) " +
           "FROM Tahsilat t WHERE t.birlik.id = :birlikId AND t.iptalEdildi = false " +
           "GROUP BY FUNCTION('YEAR', t.odemeTarihi), FUNCTION('MONTH', t.odemeTarihi) " +
           "ORDER BY FUNCTION('YEAR', t.odemeTarihi), FUNCTION('MONTH', t.odemeTarihi)")
    List<Object[]> getAylikTahsilatOzeti(@Param("birlikId") Long birlikId);

    List<Tahsilat> findAllByOrderByOdemeTarihiDesc();
    
    @Query("SELECT t FROM Tahsilat t WHERE t.aidat.uye.birlik.id = :birlikId ORDER BY t.odemeTarihi DESC")
    List<Tahsilat> findByAidatUyeBirlikIdOrderByOdemeTarihiDesc(@Param("birlikId") Long birlikId);
    
    // JOIN FETCH metodları - N+1 sorunu önleme
    @Query("SELECT t FROM Tahsilat t " +
           "LEFT JOIN FETCH t.uye " +
           "LEFT JOIN FETCH t.aidat a " +
           "LEFT JOIN FETCH a.aidatDonemi " +
           "LEFT JOIN FETCH t.birlik " +
           "WHERE t.iptalEdildi = false " +
           "ORDER BY t.odemeTarihi DESC")
    List<Tahsilat> findAllWithRelationsOrderByOdemeTarihiDesc();
    
    @Query("SELECT t FROM Tahsilat t " +
           "LEFT JOIN FETCH t.uye " +
           "LEFT JOIN FETCH t.aidat a " +
           "LEFT JOIN FETCH a.aidatDonemi " +
           "LEFT JOIN FETCH t.birlik " +
           "WHERE t.birlik.id = :birlikId AND t.iptalEdildi = false " +
           "ORDER BY t.odemeTarihi DESC")
    List<Tahsilat> findByBirlikIdWithRelationsOrderByOdemeTarihiDesc(@Param("birlikId") Long birlikId);
}
