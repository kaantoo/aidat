package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.Bildirim;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.BildirimOnceligi;
import tr.gov.tuketbir.domain.enums.BildirimTipi;
import tr.gov.tuketbir.dto.bildirim.BildirimDTO;
import tr.gov.tuketbir.dto.bildirim.BildirimOzetDTO;
import tr.gov.tuketbir.event.BildirimEvent;
import tr.gov.tuketbir.repository.BildirimRepository;
import tr.gov.tuketbir.repository.KullaniciRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bildirim Service
 * Bildirim yönetimi ve event handling
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BildirimService {

    private final BildirimRepository bildirimRepository;
    private final KullaniciRepository kullaniciRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Yeni bildirim oluştur
     */
    @Transactional
    public Bildirim createBildirim(String baslik, String mesaj, BildirimTipi tip, 
                                    BildirimOnceligi oncelik, Kullanici kullanici, 
                                    Birlik birlik, String link, String entityTipi, Long entityId) {
        
        Bildirim bildirim = Bildirim.builder()
                .baslik(baslik)
                .mesaj(mesaj)
                .tip(tip)
                .oncelik(oncelik)
                .kullanici(kullanici)
                .birlik(birlik)
                .link(link)
                .entityTipi(entityTipi)
                .entityId(entityId)
                .okundu(false)
                .build();
        
        bildirim.setTenantId(birlik != null ? birlik.getId() : 1L);
        
        Bildirim saved = bildirimRepository.save(bildirim);
        log.info("Bildirim oluşturuldu: {} - {}", tip, baslik);
        
        return saved;
    }

    /**
     * Event ile bildirim oluştur
     */
    @Transactional
    public Bildirim createFromEvent(BildirimEvent event) {
        return createBildirim(
                event.getBaslik(),
                event.getMesaj(),
                event.getTip(),
                event.getOncelik(),
                event.getKullanici(),
                event.getBirlik(),
                event.getLink(),
                event.getEntityTipi(),
                event.getEntityId()
        );
    }

    /**
     * Bildirim event'i yayınla
     */
    public void publishBildirimEvent(BildirimEvent event) {
        eventPublisher.publishEvent(event);
    }

    /**
     * Kullanıcının bildirimlerini getir
     */
    @Transactional(readOnly = true)
    public Page<BildirimDTO> getKullaniciBildirimleri(Long kullaniciId, Pageable pageable) {
        Page<Bildirim> bildirimler = bildirimRepository.findByKullaniciIdOrderByCreatedAtDesc(kullaniciId, pageable);
        return bildirimler.map(this::toDTO);
    }

    /**
     * Birliğin bildirimlerini getir
     */
    @Transactional(readOnly = true)
    public Page<BildirimDTO> getBirlikBildirimleri(Long birlikId, Pageable pageable) {
        Page<Bildirim> bildirimler = bildirimRepository.findByBirlikIdOrderByCreatedAtDesc(birlikId, pageable);
        return bildirimler.map(this::toDTO);
    }

    /**
     * Bildirim özetini getir (header için)
     */
    @Transactional(readOnly = true)
    public BildirimOzetDTO getBildirimOzeti(Long kullaniciId, Long birlikId) {
        Long okunmamisSayisi;
        List<Bildirim> sonBildirimler;
        
        if (kullaniciId != null) {
            okunmamisSayisi = bildirimRepository.countUnreadByKullaniciId(kullaniciId);
            sonBildirimler = bildirimRepository.findTopByKullaniciId(kullaniciId, PageRequest.of(0, 5));
        } else if (birlikId != null) {
            okunmamisSayisi = bildirimRepository.countUnreadByBirlikId(birlikId);
            sonBildirimler = bildirimRepository.findByBirlikIdAndOkunduFalseOrderByCreatedAtDesc(birlikId)
                    .stream().limit(5).collect(Collectors.toList());
        } else {
            okunmamisSayisi = 0L;
            sonBildirimler = List.of();
        }
        
        return BildirimOzetDTO.builder()
                .okunmamisSayisi(okunmamisSayisi)
                .sonBildirimler(sonBildirimler.stream().map(this::toDTO).collect(Collectors.toList()))
                .build();
    }

    /**
     * Bildirimi okundu işaretle
     */
    @Transactional
    public void markAsRead(Long bildirimId) {
        bildirimRepository.findById(bildirimId).ifPresent(bildirim -> {
            bildirim.markAsRead();
            bildirimRepository.save(bildirim);
        });
    }

    /**
     * Tüm bildirimleri okundu işaretle
     */
    @Transactional
    public int markAllAsRead(Long kullaniciId, Long birlikId) {
        if (kullaniciId != null) {
            return bildirimRepository.markAllAsReadByKullaniciId(kullaniciId);
        } else if (birlikId != null) {
            return bildirimRepository.markAllAsReadByBirlikId(birlikId);
        }
        return 0;
    }

    /**
     * Okunmamış bildirim sayısı
     */
    @Transactional(readOnly = true)
    public Long getOkunmamisSayisi(Long kullaniciId, Long birlikId) {
        if (kullaniciId != null) {
            return bildirimRepository.countUnreadByKullaniciId(kullaniciId);
        } else if (birlikId != null) {
            return bildirimRepository.countUnreadByBirlikId(birlikId);
        }
        return 0L;
    }

    /**
     * Bildirim sil
     */
    @Transactional
    public void deleteBildirim(Long bildirimId) {
        bildirimRepository.deleteById(bildirimId);
    }

    /**
     * Entity to DTO converter
     */
    private BildirimDTO toDTO(Bildirim bildirim) {
        return BildirimDTO.builder()
                .id(bildirim.getId())
                .baslik(bildirim.getBaslik())
                .mesaj(bildirim.getMesaj())
                .tip(bildirim.getTip())
                .oncelik(bildirim.getOncelik())
                .okundu(bildirim.getOkundu())
                .okunmaTarihi(bildirim.getOkunmaTarihi())
                .link(bildirim.getLink())
                .entityTipi(bildirim.getEntityTipi())
                .entityId(bildirim.getEntityId())
                .createdAt(bildirim.getCreatedAt())
                .zamanOnce(calculateZamanOnce(bildirim.getCreatedAt()))
                .build();
    }

    /**
     * Zaman farkını hesapla ("5 dk önce" gibi)
     */
    private String calculateZamanOnce(LocalDateTime tarih) {
        if (tarih == null) return "";
        
        Duration duration = Duration.between(tarih, LocalDateTime.now());
        long dakika = duration.toMinutes();
        long saat = duration.toHours();
        long gun = duration.toDays();
        
        if (dakika < 1) {
            return "Az önce";
        } else if (dakika < 60) {
            return dakika + " dk önce";
        } else if (saat < 24) {
            return saat + " saat önce";
        } else if (gun < 7) {
            return gun + " gün önce";
        } else if (gun < 30) {
            return (gun / 7) + " hafta önce";
        } else {
            return (gun / 30) + " ay önce";
        }
    }
}
