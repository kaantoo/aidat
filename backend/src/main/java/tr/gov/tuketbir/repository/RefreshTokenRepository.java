package tr.gov.tuketbir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Refresh Token Repository
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    
    List<RefreshToken> findByKullaniciId(Long kullaniciId);
    
    List<RefreshToken> findByKullaniciIdAndRevokedFalse(Long kullaniciId);
    
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.kullanici.id = :kullaniciId")
    void revokeAllByKullaniciId(@Param("kullaniciId") Long kullaniciId, @Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.kullanici.id = :kullaniciId")
    void deleteAllByKullaniciId(@Param("kullaniciId") Long kullaniciId);
}
