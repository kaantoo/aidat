package tr.gov.tuketbir.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tr.gov.tuketbir.domain.entity.*;
import tr.gov.tuketbir.domain.enums.AuditIslemTipi;
import tr.gov.tuketbir.repository.AuditLogRepository;

import java.time.LocalDateTime;

/**
 * Audit Log Service
 * 
 * Tüm sistem işlemlerinin loglanması.
 * Async olarak çalışır - performansı etkilemez.
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    // ======================= Authentication Logs =======================

    @Async
    public void logBasariliGiris(Kullanici kullanici, String ipAddress) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.GIRIS);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setBirlikId(kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null);
        auditLog.setAciklama("Kullanıcı başarılı giriş yaptı: " + kullanici.getKullaniciAdi());
        auditLog.setIpAdresi(ipAddress);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logBasarisizGiris(String kullaniciAdi, String ipAddress, String neden) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.GIRIS_BASARISIZ);
        auditLog.setKullaniciAdi(kullaniciAdi);
        auditLog.setAciklama("Başarısız giriş denemesi: " + kullaniciAdi + " - " + neden);
        auditLog.setIpAdresi(ipAddress);
        auditLog.setBasarili(false);
        auditLog.setHataMesaji(neden);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logCikis(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.CIKIS);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("Kullanıcı çıkış yaptı: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logSifreDegistirme(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.SIFRE_DEGISTIRME);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("Şifre değiştirildi: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logPasswordResetRequest(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.SIFRE_SIFIRLAMA_TALEBI);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("Şifre sıfırlama talebi: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logPasswordReset(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.SIFRE_SIFIRLAMA);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("Şifre sıfırlandı: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    @Async
    public void log2FAaktif(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.IKI_FAKTOR_AKTIF);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("İki faktörlü doğrulama aktifleştirildi: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    @Async
    public void log2FApasif(Kullanici kullanici) {
        AuditLog auditLog = createBaseLog(AuditIslemTipi.IKI_FAKTOR_PASIF);
        auditLog.setKullaniciId(kullanici.getId());
        auditLog.setKullaniciAdi(kullanici.getKullaniciAdi());
        auditLog.setAciklama("İki faktörlü doğrulama devre dışı bırakıldı: " + kullanici.getKullaniciAdi());
        auditLogRepository.save(auditLog);
    }

    // ======================= Üye Logs =======================

    @Async
    public void logUyeOlusturma(Long uyeId, Long birlikId, String uyeNo, String tamAd) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.OLUSTURMA);
        auditLog.setEntityTipi("Uye");
        auditLog.setEntityId(uyeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Yeni üye oluşturuldu: " + uyeNo + " - " + tamAd);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logUyeGuncelleme(Long uyeId, Long birlikId, String uyeNo, String tamAd) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.GUNCELLEME);
        auditLog.setEntityTipi("Uye");
        auditLog.setEntityId(uyeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Üye güncellendi: " + uyeNo + " - " + tamAd);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logUyePasif(Long uyeId, Long birlikId, String uyeNo, String neden) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.PASIF_YAPMA);
        auditLog.setEntityTipi("Uye");
        auditLog.setEntityId(uyeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Üye pasifleştirildi: " + uyeNo + " - Neden: " + neden);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logUyeAktif(Long uyeId, Long birlikId, String uyeNo) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.AKTIF_YAPMA);
        auditLog.setEntityTipi("Uye");
        auditLog.setEntityId(uyeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Üye aktifleştirildi: " + uyeNo);
        auditLogRepository.save(auditLog);
    }

    // ======================= Aidat Logs =======================

    @Async
    public void logDonemOlusturma(Long donemId, String donemKodu) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.OLUSTURMA);
        auditLog.setEntityTipi("AidatDonemi");
        auditLog.setEntityId(donemId);
        auditLog.setAciklama("Yeni aidat dönemi oluşturuldu: " + donemKodu);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logTopluAidatAtama(String donemKodu, Long birlikId, String birlikAdi, int count) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.TOPLU_EKLEME);
        auditLog.setEntityTipi("Aidat");
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Toplu aidat ataması yapıldı: " + donemKodu + 
            " - " + birlikAdi + " - " + count + " üye");
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logTahsilat(Long tahsilatId, Long birlikId, String tutar, String uyeNo) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.OLUSTURMA);
        auditLog.setEntityTipi("Tahsilat");
        auditLog.setEntityId(tahsilatId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Tahsilat kaydedildi: " + tutar + " TL - " + uyeNo);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logTahsilatIptal(Long tahsilatId, Long birlikId, String tutar, String neden) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.SILME);
        auditLog.setEntityTipi("Tahsilat");
        auditLog.setEntityId(tahsilatId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Tahsilat iptal edildi: " + tutar + " TL - Neden: " + neden);
        auditLogRepository.save(auditLog);
    }

    // ======================= Belge Logs =======================

    @Async
    public void logBelgeYukleme(Long belgeId, Long birlikId, String belgeNo, String baslik) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.DOSYA_YUKLEME);
        auditLog.setEntityTipi("Belge");
        auditLog.setEntityId(belgeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Belge yüklendi: " + belgeNo + " - " + baslik);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logBelgeIndirme(Long belgeId, Long birlikId, String belgeNo) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.DOSYA_INDIRME);
        auditLog.setEntityTipi("Belge");
        auditLog.setEntityId(belgeId);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Belge indirildi: " + belgeNo);
        auditLogRepository.save(auditLog);
    }

    // ======================= Rapor Logs =======================

    @Async
    public void logRaporOlusturma(String raporTipi, Long birlikId) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.RAPOR_OLUSTURMA);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Rapor oluşturuldu: " + raporTipi);
        auditLogRepository.save(auditLog);
    }

    @Async
    public void logRaporExport(String raporTipi, String format, Long birlikId) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.RAPOR_EXPORT);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("Rapor dışa aktarıldı: " + raporTipi + " (" + format + ")");
        auditLogRepository.save(auditLog);
    }

    // ======================= Generic Log =======================

    /**
     * Genel amaçlı log metodu
     * İşlem tipi ve açıklama ile log kaydı oluşturur
     */
    @Async
    public void log(String islemTipi, String aciklama) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.DIGER);
        auditLog.setAciklama("[" + islemTipi + "] " + aciklama);
        auditLogRepository.save(auditLog);
    }

    /**
     * Genel amaçlı log metodu - birlik ID ile
     */
    @Async
    public void log(String islemTipi, String aciklama, Long birlikId) {
        AuditLog auditLog = createBaseLogWithCurrentUser(AuditIslemTipi.DIGER);
        auditLog.setBirlikId(birlikId);
        auditLog.setAciklama("[" + islemTipi + "] " + aciklama);
        auditLogRepository.save(auditLog);
    }

    // ======================= Helper Methods =======================

    private AuditLog createBaseLog(AuditIslemTipi islemTipi) {
        AuditLog auditLog = new AuditLog();
        auditLog.setIslemTipi(islemTipi);
        auditLog.setIslemZamani(LocalDateTime.now());
        auditLog.setBasarili(true);
        
        // Request bilgilerini ekle
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                auditLog.setIpAdresi(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setRequestUrl(request.getRequestURI());
                auditLog.setHttpMetod(request.getMethod());
            }
        } catch (Exception e) {
            log.debug("Could not get request attributes for audit log", e);
        }
        
        return auditLog;
    }

    private AuditLog createBaseLogWithCurrentUser(AuditIslemTipi islemTipi) {
        AuditLog auditLog = createBaseLog(islemTipi);
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                auditLog.setKullaniciAdi(auth.getName());
                // Not: Kullanıcı ID'si için UserDetails'tan alınabilir
            }
        } catch (Exception e) {
            log.debug("Could not get current user for audit log", e);
        }
        
        return auditLog;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
