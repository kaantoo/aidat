package tr.gov.tuketbir.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.ToplantiKatilimci;

import java.util.List;

/**
 * Toplantı Katılımcı Repository
 */
@Repository
public interface ToplantiKatilimciRepository extends BaseRepository<ToplantiKatilimci, Long> {

    List<ToplantiKatilimci> findByToplantiIdAndIsActiveTrue(Long toplantiId);

    @Query("SELECT tk FROM ToplantiKatilimci tk WHERE tk.uye.id = :uyeId AND tk.isActive = true")
    List<ToplantiKatilimci> findByUyeId(@Param("uyeId") Long uyeId);

    @Query("SELECT COUNT(tk) FROM ToplantiKatilimci tk WHERE tk.toplanti.id = :toplantiId AND tk.katildi = true AND tk.isActive = true")
    long countKatilanByToplantiId(@Param("toplantiId") Long toplantiId);

    boolean existsByToplantiIdAndUyeIdAndIsActiveTrue(Long toplantiId, Long uyeId);
}
