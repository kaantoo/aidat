package tr.gov.tuketbir.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.Bildirim;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.BildirimOnceligi;
import tr.gov.tuketbir.domain.enums.BildirimTipi;
import tr.gov.tuketbir.event.AidatOdemeEvent;
import tr.gov.tuketbir.event.BildirimEvent;
import tr.gov.tuketbir.event.GelirGiderEvent;
import tr.gov.tuketbir.event.UyeKayitEvent;
import tr.gov.tuketbir.repository.KullaniciRepository;
import tr.gov.tuketbir.service.BildirimService;

import java.util.List;

/**
 * Bildirim Event Listener
 * Tüm event'leri dinler ve bildirim oluşturur
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BildirimEventListener {

    private final BildirimService bildirimService;
    private final KullaniciRepository kullaniciRepository;

    /**
     * Genel bildirim event'i
     */
    @Async
    @EventListener
    @Transactional
    public void handleBildirimEvent(BildirimEvent event) {
        log.info("BildirimEvent alındı: {} - {}", event.getTip(), event.getBaslik());
        
        try {
            bildirimService.createFromEvent(event);
        } catch (Exception e) {
            log.error("Bildirim oluşturma hatası: {}", e.getMessage(), e);
        }
    }

    /**
     * Üye kayıt event'i
     */
    @Async
    @EventListener
    @Transactional
    public void handleUyeKayitEvent(UyeKayitEvent event) {
        log.info("UyeKayitEvent alındı: {} - Üye: {}", event.getIslemTipi(), event.getUye().getTamAd());
        
        try {
            String baslik;
            String mesaj;
            BildirimOnceligi oncelik = BildirimOnceligi.NORMAL;
            
            switch (event.getIslemTipi()) {
                case OLUSTURULDU:
                    baslik = "Yeni Üye Kaydı";
                    mesaj = String.format("%s adlı yeni üye kaydedildi.", event.getUye().getTamAd());
                    break;
                case GUNCELLENDI:
                    baslik = "Üye Bilgisi Güncellendi";
                    mesaj = String.format("%s adlı üyenin bilgileri güncellendi.", event.getUye().getTamAd());
                    break;
                case PASIFE_ALINDI:
                    baslik = "Üye Pasife Alındı";
                    mesaj = String.format("%s adlı üye pasife alındı.", event.getUye().getTamAd());
                    oncelik = BildirimOnceligi.YUKSEK;
                    break;
                case AKTIFE_ALINDI:
                    baslik = "Üye Aktife Alındı";
                    mesaj = String.format("%s adlı üye tekrar aktife alındı.", event.getUye().getTamAd());
                    break;
                case SILINDI:
                    baslik = "Üye Silindi";
                    mesaj = String.format("%s adlı üye sistemden silindi.", event.getUye().getTamAd());
                    oncelik = BildirimOnceligi.YUKSEK;
                    break;
                default:
                    baslik = "Üye İşlemi";
                    mesaj = event.getUye().getTamAd() + " için işlem yapıldı.";
            }
            
            // Birlik yöneticilerine bildirim gönder
            List<Kullanici> yoneticiler = kullaniciRepository.findByBirlikId(event.getBirlik().getId());
            
            for (Kullanici yonetici : yoneticiler) {
                bildirimService.createBildirim(
                        baslik,
                        mesaj,
                        BildirimTipi.UYE_KAYDI,
                        oncelik,
                        yonetici,
                        event.getBirlik(),
                        "/uyeler/" + event.getUye().getId(),
                        "UYE",
                        event.getUye().getId()
                );
            }
            
        } catch (Exception e) {
            log.error("Üye kayıt bildirimi oluşturma hatası: {}", e.getMessage(), e);
        }
    }

    /**
     * Aidat ödeme event'i
     */
    @Async
    @EventListener
    @Transactional
    public void handleAidatOdemeEvent(AidatOdemeEvent event) {
        log.info("AidatOdemeEvent alındı: {} - Tutar: {}", event.getIslemTipi(), event.getTutar());
        
        try {
            String baslik;
            String mesaj;
            BildirimTipi tip;
            BildirimOnceligi oncelik = BildirimOnceligi.NORMAL;
            
            String uyeAd = event.getAidat().getUye() != null ? event.getAidat().getUye().getTamAd() : "Bilinmeyen Üye";
            
            switch (event.getIslemTipi()) {
                case ODEME_YAPILDI:
                    baslik = "Aidat Ödemesi Yapıldı";
                    mesaj = String.format("%s %.2f₺ aidat ödemesi yaptı.", uyeAd, event.getTutar());
                    tip = BildirimTipi.AIDAT_ODEMESI;
                    break;
                case KISMI_ODEME:
                    baslik = "Kısmi Aidat Ödemesi";
                    mesaj = String.format("%s %.2f₺ kısmi ödeme yaptı.", uyeAd, event.getTutar());
                    tip = BildirimTipi.AIDAT_ODEMESI;
                    break;
                case GECIKME_FAIZI_UYGULANDI:
                    baslik = "Gecikme Faizi Uygulandı";
                    mesaj = String.format("%s için gecikme faizi uygulandı.", uyeAd);
                    tip = BildirimTipi.GECIKEN_ODEME;
                    oncelik = BildirimOnceligi.YUKSEK;
                    break;
                case IPTAL_EDILDI:
                    baslik = "Aidat İptal Edildi";
                    mesaj = String.format("%s aidatı iptal edildi.", uyeAd);
                    tip = BildirimTipi.AIDAT_ODEMESI;
                    oncelik = BildirimOnceligi.YUKSEK;
                    break;
                case HATIRLATMA_GONDERILDI:
                    baslik = "Aidat Hatırlatması Gönderildi";
                    mesaj = String.format("%s için aidat hatırlatması gönderildi.", uyeAd);
                    tip = BildirimTipi.AIDAT_HATIRLATMA;
                    break;
                default:
                    baslik = "Aidat İşlemi";
                    mesaj = uyeAd + " için aidat işlemi yapıldı.";
                    tip = BildirimTipi.AIDAT_ODEMESI;
            }
            
            // Birlik yöneticilerine bildirim gönder
            List<Kullanici> yoneticiler = kullaniciRepository.findByBirlikId(event.getBirlik().getId());
            
            for (Kullanici yonetici : yoneticiler) {
                bildirimService.createBildirim(
                        baslik,
                        mesaj,
                        tip,
                        oncelik,
                        yonetici,
                        event.getBirlik(),
                        "/aidatlar/" + event.getAidat().getId(),
                        "AIDAT",
                        event.getAidat().getId()
                );
            }
            
        } catch (Exception e) {
            log.error("Aidat ödeme bildirimi oluşturma hatası: {}", e.getMessage(), e);
        }
    }

    /**
     * Gelir/Gider event'i
     */
    @Async
    @EventListener
    @Transactional
    public void handleGelirGiderEvent(GelirGiderEvent event) {
        log.info("GelirGiderEvent alındı: {} - {} - Tutar: {}", 
                event.getIslemTipi(), event.getGelirGider().getTip(), event.getGelirGider().getTutar());
        
        try {
            String baslik;
            String mesaj;
            BildirimOnceligi oncelik = BildirimOnceligi.NORMAL;
            
            String tipStr = event.getGelirGider().getTip().toString();
            Double tutar = event.getGelirGider().getTutar() != null ? event.getGelirGider().getTutar().doubleValue() : 0.0;
            
            switch (event.getIslemTipi()) {
                case OLUSTURULDU:
                    baslik = "Yeni " + tipStr + " Kaydı";
                    mesaj = String.format("%.2f₺ tutarında %s kaydı oluşturuldu.", tutar, tipStr.toLowerCase());
                    break;
                case GUNCELLENDI:
                    baslik = tipStr + " Güncellendi";
                    mesaj = String.format("%.2f₺ tutarındaki %s kaydı güncellendi.", tutar, tipStr.toLowerCase());
                    break;
                case ONAYLANDI:
                    baslik = tipStr + " Onaylandı";
                    mesaj = String.format("%.2f₺ tutarındaki %s kaydı onaylandı.", tutar, tipStr.toLowerCase());
                    break;
                case REDDEDILDI:
                    baslik = tipStr + " Reddedildi";
                    mesaj = String.format("%.2f₺ tutarındaki %s kaydı reddedildi.", tutar, tipStr.toLowerCase());
                    oncelik = BildirimOnceligi.YUKSEK;
                    break;
                case SILINDI:
                    baslik = tipStr + " Silindi";
                    mesaj = String.format("%.2f₺ tutarındaki %s kaydı silindi.", tutar, tipStr.toLowerCase());
                    break;
                default:
                    baslik = tipStr + " İşlemi";
                    mesaj = tipStr + " için işlem yapıldı.";
            }
            
            // Birlik yöneticilerine bildirim gönder
            List<Kullanici> yoneticiler = kullaniciRepository.findByBirlikId(event.getBirlik().getId());
            
            for (Kullanici yonetici : yoneticiler) {
                bildirimService.createBildirim(
                        baslik,
                        mesaj,
                        BildirimTipi.GELIR_GIDER,
                        oncelik,
                        yonetici,
                        event.getBirlik(),
                        "/gelir-gider/" + event.getGelirGider().getId(),
                        "GELIR_GIDER",
                        event.getGelirGider().getId()
                );
            }
            
        } catch (Exception e) {
            log.error("Gelir/Gider bildirimi oluşturma hatası: {}", e.getMessage(), e);
        }
    }
}
