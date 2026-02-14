package tr.gov.tuketbir.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.gov.tuketbir.domain.entity.SistemAyari;

import java.util.Optional;

/**
 * Sistem Ayarları Repository
 */
@Repository
public interface SistemAyariRepository extends JpaRepository<SistemAyari, Long> {

    Optional<SistemAyari> findByAnahtar(String anahtar);

    boolean existsByAnahtar(String anahtar);
}
