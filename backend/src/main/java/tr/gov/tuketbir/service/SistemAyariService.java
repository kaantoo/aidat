package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.SistemAyari;
import tr.gov.tuketbir.repository.SistemAyariRepository;

import java.util.Optional;

/**
 * Sistem Ayarları Service
 * Sistem genelinde geçerli ayarları yönetir
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SistemAyariService {

    private final SistemAyariRepository sistemAyariRepository;

    /**
     * Ayar değerini getir
     */
    @Transactional(readOnly = true)
    public String getAyar(String anahtar) {
        return sistemAyariRepository.findByAnahtar(anahtar)
                .map(SistemAyari::getDeger)
                .orElse(null);
    }

    /**
     * Ayar değerini getir (varsayılan değerli)
     */
    @Transactional(readOnly = true)
    public String getAyar(String anahtar, String varsayilan) {
        return sistemAyariRepository.findByAnahtar(anahtar)
                .map(SistemAyari::getDeger)
                .orElse(varsayilan);
    }

    /**
     * Boolean ayar değerini getir
     */
    @Transactional(readOnly = true)
    public boolean getBooleanAyar(String anahtar) {
        String deger = getAyar(anahtar);
        return "true".equalsIgnoreCase(deger) || "1".equals(deger);
    }

    /**
     * Ayar değerini kaydet veya güncelle
     */
    @Transactional
    public void setAyar(String anahtar, String deger, String aciklama, String tip) {
        SistemAyari ayar = sistemAyariRepository.findByAnahtar(anahtar)
                .orElseGet(() -> {
                    SistemAyari yeniAyar = new SistemAyari();
                    yeniAyar.setAnahtar(anahtar);
                    yeniAyar.setTenantId(0L); // Sistem ayarları merkez (SISTEM_ADMIN) için
                    return yeniAyar;
                });

        ayar.setDeger(deger);
        if (aciklama != null) {
            ayar.setAciklama(aciklama);
        }
        if (tip != null) {
            ayar.setTip(tip);
        }
        
        // Eğer tenantId null ise 0 yap
        if (ayar.getTenantId() == null) {
            ayar.setTenantId(0L);
        }

        sistemAyariRepository.save(ayar);
        log.info("Sistem ayarı güncellendi: {} = {}", anahtar, deger);
    }

    /**
     * Ayar değerini kaydet (kısayol)
     */
    @Transactional
    public void setAyar(String anahtar, String deger) {
        setAyar(anahtar, deger, null, null);
    }

    /**
     * Boolean ayar değerini kaydet
     */
    @Transactional
    public void setBooleanAyar(String anahtar, boolean deger) {
        setAyar(anahtar, String.valueOf(deger), null, "BOOLEAN");
    }

    // ======================= Read-Only Mode İşlemleri =======================

    /**
     * Sistem read-only modunda mı?
     */
    @Transactional(readOnly = true)
    public boolean isReadOnlyMode() {
        return getBooleanAyar(SistemAyari.READ_ONLY_MODE);
    }

    /**
     * Read-only mod mesajını getir
     */
    @Transactional(readOnly = true)
    public String getReadOnlyMesaj() {
        return getAyar(SistemAyari.READ_ONLY_MESAJ, "Sistem şu anda salt okunur modunda. Değişiklik yapamazsınız.");
    }

    /**
     * Read-only modu aç/kapat
     */
    @Transactional
    public void setReadOnlyMode(boolean aktif, String mesaj) {
        setBooleanAyar(SistemAyari.READ_ONLY_MODE, aktif);
        if (mesaj != null) {
            setAyar(SistemAyari.READ_ONLY_MESAJ, mesaj, "Read-only mod mesajı", "STRING");
        }
        log.info("Sistem read-only modu {} edildi", aktif ? "aktif" : "deaktif");
    }

    /**
     * Bakım modu aktif mi?
     */
    @Transactional(readOnly = true)
    public boolean isBakimModu() {
        return getBooleanAyar(SistemAyari.BAKIM_MODU);
    }

    /**
     * Bakım modunu aç/kapat
     */
    @Transactional
    public void setBakimModu(boolean aktif) {
        setBooleanAyar(SistemAyari.BAKIM_MODU, aktif);
        log.info("Sistem bakım modu {} edildi", aktif ? "aktif" : "deaktif");
    }
}
