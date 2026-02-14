package tr.gov.tuketbir.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.AidatDonemi;
import tr.gov.tuketbir.domain.enums.DonemTipi;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Aidat Dönemi Repository
 */
@Repository
public interface AidatDonemiRepository extends BaseRepository<AidatDonemi, Long> {

    Optional<AidatDonemi> findByDonemKodu(String donemKodu);
    
    boolean existsByDonemKodu(String donemKodu);
    
    boolean existsByDonemAdi(String donemAdi);
    
    List<AidatDonemi> findByYil(Integer yil);
    
    List<AidatDonemi> findByDonemTipi(DonemTipi donemTipi);
    
    @Query("SELECT d FROM AidatDonemi d LEFT JOIN FETCH d.birlik WHERE d.donemAktif = true")
    List<AidatDonemi> findByDonemAktifTrue();
    
    @Query("SELECT d FROM AidatDonemi d LEFT JOIN FETCH d.birlik WHERE d.birlik IS NULL AND d.donemAktif = true")
    List<AidatDonemi> findByBirlikIdIsNullAndDonemAktifTrue();
    
    @Query("SELECT d FROM AidatDonemi d LEFT JOIN FETCH d.birlik WHERE d.birlik.id = :birlikId OR d.birlik IS NULL")
    List<AidatDonemi> findByBirlikIdOrBirlikIdIsNull(Long birlikId);
    
    @Query("SELECT d FROM AidatDonemi d LEFT JOIN FETCH d.birlik WHERE d.yil = :yil AND (d.birlik.id = :birlikId OR d.birlik IS NULL)")
    List<AidatDonemi> findByYilAndBirlikIdOrBirlikIdIsNull(Integer yil, Long birlikId);
    
    List<AidatDonemi> findByBaslangicTarihiBetween(LocalDate start, LocalDate end);
    
    List<AidatDonemi> findByBitisTarihiBeforeAndDonemAktifTrue(LocalDate date);
}
