package tr.gov.tuketbir.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Aidat;
import tr.gov.tuketbir.domain.enums.AidatDurum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Aidat Repository
 */
@Repository
public interface AidatRepository extends BaseRepository<Aidat, Long> {

    List<Aidat> findByUyeId(Long uyeId);
    
    Page<Aidat> findByUyeId(Long uyeId, Pageable pageable);
    
    List<Aidat> findByBirlikId(Long birlikId);
    
    Page<Aidat> findByBirlikId(Long birlikId, Pageable pageable);
    
    List<Aidat> findByAidatDonemiId(Long aidatDonemiId);
    
    Page<Aidat> findByAidatDonemiId(Long aidatDonemiId, Pageable pageable);
    
    Optional<Aidat> findByUyeIdAndAidatDonemiId(Long uyeId, Long aidatDonemiId);
    
    boolean existsByUyeIdAndAidatDonemiId(Long uyeId, Long aidatDonemiId);
    
    List<Aidat> findByAidatDurum(AidatDurum durum);
    
    List<Aidat> findByBirlikIdAndAidatDurum(Long birlikId, AidatDurum durum);
    
    Page<Aidat> findByBirlikIdAndAidatDurum(Long birlikId, AidatDurum durum, Pageable pageable);
    
    @Query("SELECT a FROM Aidat a WHERE a.sonOdemeTarihi < :today AND a.aidatDurum NOT IN ('ODENDI', 'IPTAL')")
    List<Aidat> findGecikmisBorclar(@Param("today") LocalDate today);
    
    @Query("SELECT a FROM Aidat a WHERE a.birlik.id = :birlikId AND a.sonOdemeTarihi < :today AND a.aidatDurum NOT IN ('ODENDI', 'IPTAL')")
    List<Aidat> findGecikmisBorclarByBirlik(@Param("birlikId") Long birlikId, @Param("today") LocalDate today);
    
    @Query("SELECT SUM(a.toplamBorc) FROM Aidat a WHERE a.birlik.id = :birlikId AND a.aidatDurum NOT IN ('ODENDI', 'IPTAL')")
    BigDecimal sumToplamBorcByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT SUM(a.odenenTutar) FROM Aidat a WHERE a.birlik.id = :birlikId")
    BigDecimal sumOdenenTutarByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT SUM(a.kalanBorc) FROM Aidat a WHERE a.birlik.id = :birlikId AND a.aidatDurum NOT IN ('ODENDI', 'IPTAL')")
    BigDecimal sumKalanBorcByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT COUNT(a) FROM Aidat a WHERE a.birlik.id = :birlikId AND a.aidatDurum = :durum")
    Long countByBirlikIdAndDurum(@Param("birlikId") Long birlikId, @Param("durum") AidatDurum durum);
    
    @Query("SELECT a.aidatDurum, COUNT(a), SUM(a.toplamBorc), SUM(a.odenenTutar) " +
           "FROM Aidat a WHERE a.birlik.id = :birlikId GROUP BY a.aidatDurum")
    List<Object[]> getAidatOzetByBirlik(@Param("birlikId") Long birlikId);
    
    @Query("SELECT a.aidatDonemi.id, COUNT(a), SUM(a.toplamBorc), SUM(a.odenenTutar) " +
           "FROM Aidat a WHERE a.birlik.id = :birlikId GROUP BY a.aidatDonemi.id")
    List<Object[]> getAidatOzetByDonem(@Param("birlikId") Long birlikId);
    
    @Query("SELECT a FROM Aidat a WHERE a.uye.id = :uyeId AND a.aidatDurum NOT IN ('ODENDI', 'IPTAL') ORDER BY a.sonOdemeTarihi")
    List<Aidat> findOdenmemisAidatlarByUye(@Param("uyeId") Long uyeId);
    
    @Query("SELECT a.birlik.id, a.aidatDurum, COUNT(a) FROM Aidat a GROUP BY a.birlik.id, a.aidatDurum")
    List<Object[]> getAidatDurumIstatistikleri();
    
    // JOIN FETCH metodları - N+1 sorunu önleme
    @Query("SELECT a FROM Aidat a " +
           "LEFT JOIN FETCH a.uye " +
           "LEFT JOIN FETCH a.aidatDonemi " +
           "LEFT JOIN FETCH a.birlik " +
           "WHERE a.uye.id = :uyeId")
    List<Aidat> findByUyeIdWithRelations(@Param("uyeId") Long uyeId);
    
    @Query("SELECT a FROM Aidat a " +
           "LEFT JOIN FETCH a.uye " +
           "LEFT JOIN FETCH a.aidatDonemi " +
           "LEFT JOIN FETCH a.birlik " +
           "WHERE a.birlik.id = :birlikId")
    List<Aidat> findByBirlikIdWithRelations(@Param("birlikId") Long birlikId);
    
    @Query("SELECT a FROM Aidat a " +
           "LEFT JOIN FETCH a.uye " +
           "LEFT JOIN FETCH a.aidatDonemi " +
           "LEFT JOIN FETCH a.birlik " +
           "WHERE a.aidatDonemi.id = :donemId")
    List<Aidat> findByAidatDonemiIdWithRelations(@Param("donemId") Long donemId);
    
    // Filtreleme query'leri - her kombinasyon için
    @Query("SELECT a FROM Aidat a " +
           "WHERE (:birlikId IS NULL OR a.birlik.id = :birlikId) " +
           "AND (:donemId IS NULL OR a.aidatDonemi.id = :donemId) " +
           "AND (:durum IS NULL OR a.aidatDurum = :durum)")
    Page<Aidat> findByFiltersWithoutDate(
        @Param("birlikId") Long birlikId,
        @Param("donemId") Long donemId,
        @Param("durum") AidatDurum durum,
        Pageable pageable);
    
    @Query("SELECT a FROM Aidat a " +
           "WHERE (:birlikId IS NULL OR a.birlik.id = :birlikId) " +
           "AND (:donemId IS NULL OR a.aidatDonemi.id = :donemId) " +
           "AND (:durum IS NULL OR a.aidatDurum = :durum) " +
           "AND a.createdAt >= :baslangicTarihi")
    Page<Aidat> findByFiltersWithStartDate(
        @Param("birlikId") Long birlikId,
        @Param("donemId") Long donemId,
        @Param("durum") AidatDurum durum,
        @Param("baslangicTarihi") LocalDate baslangicTarihi,
        Pageable pageable);
    
    @Query("SELECT a FROM Aidat a " +
           "WHERE (:birlikId IS NULL OR a.birlik.id = :birlikId) " +
           "AND (:donemId IS NULL OR a.aidatDonemi.id = :donemId) " +
           "AND (:durum IS NULL OR a.aidatDurum = :durum) " +
           "AND a.createdAt <= :bitisTarihi")
    Page<Aidat> findByFiltersWithEndDate(
        @Param("birlikId") Long birlikId,
        @Param("donemId") Long donemId,
        @Param("durum") AidatDurum durum,
        @Param("bitisTarihi") LocalDate bitisTarihi,
        Pageable pageable);
    
    @Query("SELECT a FROM Aidat a " +
           "WHERE (:birlikId IS NULL OR a.birlik.id = :birlikId) " +
           "AND (:donemId IS NULL OR a.aidatDonemi.id = :donemId) " +
           "AND (:durum IS NULL OR a.aidatDurum = :durum) " +
           "AND a.createdAt >= :baslangicTarihi " +
           "AND a.createdAt <= :bitisTarihi")
    Page<Aidat> findByFiltersWithDateRange(
        @Param("birlikId") Long birlikId,
        @Param("donemId") Long donemId,
        @Param("durum") AidatDurum durum,
        @Param("baslangicTarihi") LocalDate baslangicTarihi,
        @Param("bitisTarihi") LocalDate bitisTarihi,
        Pageable pageable);

    // ======================= Beklenen Gelir Query'leri =======================
    
    /**
     * Birlik ve dönem bazlı tahakkuk toplamı
     */
    @Query("SELECT SUM(a.tahakkukTutari) FROM Aidat a " +
           "WHERE a.birlik.id = :birlikId AND a.aidatDonemi.id = :donemId")
    BigDecimal sumTahakkukByBirlikAndDonem(@Param("birlikId") Long birlikId, @Param("donemId") Long donemId);

    /**
     * Birlik ve dönem bazlı tahsilat toplamı
     */
    @Query("SELECT SUM(a.odenenTutar) FROM Aidat a " +
           "WHERE a.birlik.id = :birlikId AND a.aidatDonemi.id = :donemId")
    BigDecimal sumTahsilatByBirlikAndDonem(@Param("birlikId") Long birlikId, @Param("donemId") Long donemId);

    /**
     * Dönem bazlı tüm birlikler için tahakkuk ve tahsilat özeti
     */
    @Query("SELECT a.birlik.id, a.birlik.birlikKodu, a.birlik.birlikAdi, a.birlik.merkezPayOrani, " +
           "COUNT(a), SUM(a.tahakkukTutari), SUM(a.odenenTutar) " +
           "FROM Aidat a WHERE a.aidatDonemi.id = :donemId " +
           "GROUP BY a.birlik.id, a.birlik.birlikKodu, a.birlik.birlikAdi, a.birlik.merkezPayOrani")
    List<Object[]> getBeklenenGelirOzetByDonem(@Param("donemId") Long donemId);

    /**
     * Dönem bazlı aidat sayısı
     */
    @Query("SELECT COUNT(a) FROM Aidat a WHERE a.birlik.id = :birlikId AND a.aidatDonemi.id = :donemId")
    Long countByBirlikIdAndDonemId(@Param("birlikId") Long birlikId, @Param("donemId") Long donemId);

    /**
     * Tüm aidatların toplam tahakkuk tutarı
     */
    @Query("SELECT COALESCE(SUM(a.tahakkukTutari), 0) FROM Aidat a")
    BigDecimal sumAllTahakkuk();

    /**
     * Tüm aidatların toplam ödenen tutarı
     */
    @Query("SELECT COALESCE(SUM(a.odenenTutar), 0) FROM Aidat a")
    BigDecimal sumAllOdenenTutar();

    /**
     * Tüm aidatların toplam kalan borcu
     */
    @Query("SELECT COALESCE(SUM(a.kalanBorc), 0) FROM Aidat a")
    BigDecimal sumAllKalanBorc();

    /**
     * Birlik bazlı toplam tahakkuk
     */
    @Query("SELECT COALESCE(SUM(a.tahakkukTutari), 0) FROM Aidat a WHERE a.birlik.id = :birlikId")
    BigDecimal sumTahakkukByBirlik(@Param("birlikId") Long birlikId);

    /**
     * Birlik bazlı toplam kalan borç
     */
    @Query("SELECT COALESCE(SUM(a.kalanBorc), 0) FROM Aidat a WHERE a.birlik.id = :birlikId")
    BigDecimal sumKalanBorcByBirlikId(@Param("birlikId") Long birlikId);
}
