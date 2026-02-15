package tr.gov.tuketbir.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.Karar;
import tr.gov.tuketbir.domain.enums.KararDurumu;

import java.util.List;

/**
 * Karar Repository
 */
@Repository
public interface KararRepository extends BaseRepository<Karar, Long> {

    List<Karar> findByToplantiIdAndIsActiveTrueOrderByKararSirasiAsc(Long toplantiId);

    @Query("SELECT k FROM Karar k WHERE k.toplanti.birlik.id = :birlikId AND k.isActive = true ORDER BY k.createdAt DESC")
    List<Karar> findByBirlikId(@Param("birlikId") Long birlikId);

    @Query("SELECT k FROM Karar k WHERE k.durum = :durum AND k.isActive = true")
    List<Karar> findByDurum(@Param("durum") KararDurumu durum);

    @Query("SELECT COUNT(k) FROM Karar k WHERE k.toplanti.id = :toplantiId AND k.isActive = true")
    long countByToplantiId(@Param("toplantiId") Long toplantiId);

    @Query("SELECT MAX(CAST(SUBSTRING(k.kararNo, LENGTH(:prefix) + 1) AS int)) FROM Karar k WHERE k.kararNo LIKE CONCAT(:prefix, '%')")
    Integer findMaxKararNoByPrefix(@Param("prefix") String prefix);
}
