package tr.gov.tuketbir.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.*;
import tr.gov.tuketbir.domain.enums.AidatDurum;
import tr.gov.tuketbir.domain.enums.BirlikTipi;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.repository.*;
import tr.gov.tuketbir.service.AuditLogService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Aidat Dönemi Event Listener
 * 
 * Dönem oluşturulduğunda otomatik işlemler yapar:
 * 
 * 1. Merkez Birlik dönem tanımlarsa:
 *    - Tüm alt birliklere aynı dönem şablonu atanır
 *    - Alt birlikler bu dönemi kendi üyelerine atayabilir
 * 
 * 2. Alt Birlik dönem tanımlarsa:
 *    - Tüm aktif üyelere aidat tahakkuk edilir
 * 
 * @author Tuketbir Development Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DonemEventListener {

    private final BirlikRepository birlikRepository;
    private final AidatDonemiRepository aidatDonemiRepository;
    private final AidatRepository aidatRepository;
    private final UyeRepository uyeRepository;
    private final AuditLogService auditLogService;

    /**
     * Dönem oluşturulduğunda tetiklenen event handler
     */
    @EventListener
    @Transactional
    public void handleDonemOlusturuldu(DonemOlusturulduEvent event) {
        AidatDonemi donem = event.getDonem();
        
        log.info("DonemOlusturulduEvent received: donem={}, merkezBirlikTarafindan={}", 
            donem.getDonemAdi(), event.isMerkezBirlikTarafindan());
        
        if (event.isMerkezBirlikTarafindan()) {
            // Merkez birlik dönemi oluşturduysa, alt birliklere ata
            altBirliklereDonemAta(donem);
        } else {
            // Alt birlik dönemi oluşturduysa, üyelere aidat tahakkuk et
            uyelereAidatTahakkukEt(donem);
        }
    }

    /**
     * Merkez birlik tarafından oluşturulan dönemi tüm alt birliklere atar
     */
    private void altBirliklereDonemAta(AidatDonemi merkezDonem) {
        log.info("Assigning donem to alt birlikler: {}", merkezDonem.getDonemAdi());
        
        // Tüm alt birlikleri getir
        List<Birlik> altBirlikler = birlikRepository.findByBirlikTipi(BirlikTipi.ALT_BIRLIK);
        
        int atanan = 0;
        int atlanan = 0;
        
        for (Birlik altBirlik : altBirlikler) {
            // Bu birlik için aynı dönem kodu var mı kontrol et
            String altBirlikDonemKodu = merkezDonem.getDonemKodu() + "-" + altBirlik.getBirlikKodu();
            
            if (aidatDonemiRepository.existsByDonemKodu(altBirlikDonemKodu)) {
                log.debug("Donem already exists for birlik: {}", altBirlik.getBirlikAdi());
                atlanan++;
                continue;
            }
            
            // Alt birlik için dönem oluştur
            AidatDonemi altBirlikDonem = AidatDonemi.builder()
                .donemKodu(altBirlikDonemKodu)
                .donemAdi(merkezDonem.getDonemAdi() + " - " + altBirlik.getKisaAdi())
                .birlik(altBirlik)
                .donemTipi(merkezDonem.getDonemTipi())
                .yil(merkezDonem.getYil())
                .baslangicTarihi(merkezDonem.getBaslangicTarihi())
                .bitisTarihi(merkezDonem.getBitisTarihi())
                .sonOdemeTarihi(merkezDonem.getSonOdemeTarihi())
                .tutar(merkezDonem.getTutar())
                .gecikmeFaiziOrani(merkezDonem.getGecikmeFaiziOrani())
                .asgariUcretAciklama(merkezDonem.getAsgariUcretAciklama())
                .aciklama("Merkez birlik tarafından otomatik oluşturuldu. Kaynak dönem: " + merkezDonem.getDonemKodu())
                .donemAktif(true)
                .build();
            
            altBirlikDonem.setTenantId(altBirlik.getId());
            aidatDonemiRepository.save(altBirlikDonem);
            
            log.debug("Donem assigned to birlik: {}", altBirlik.getBirlikAdi());
            atanan++;
            
            // Alt birliğin üyelerine de aidat tahakkuk et
            uyelereAidatTahakkukEt(altBirlikDonem);
        }
        
        log.info("Donem assignment to alt birlikler completed: {} assigned, {} skipped", atanan, atlanan);
        auditLogService.log("MERKEZ_DONEM_DAGITIMI", 
            String.format("Merkez dönem '%s' %d alt birliğe dağıtıldı", merkezDonem.getDonemAdi(), atanan));
    }

    /**
     * Alt birlik tarafından oluşturulan dönemi tüm aktif üyelere tahakkuk eder
     */
    private void uyelereAidatTahakkukEt(AidatDonemi donem) {
        if (donem.getBirlik() == null) {
            log.warn("Donem has no birlik, skipping uye tahakkuk: {}", donem.getDonemAdi());
            return;
        }
        
        Long birlikId = donem.getBirlik().getId();
        log.info("Creating aidat tahakkuk for birlik: {} donem: {}", 
            donem.getBirlik().getBirlikAdi(), donem.getDonemAdi());
        
        // Birliğin aktif üyelerini getir
        List<Uye> aktifUyeler = uyeRepository.findByBirlikIdAndUyeDurum(birlikId, UyeDurum.AKTIF);
        
        int olusturulan = 0;
        int atlanan = 0;
        
        for (Uye uye : aktifUyeler) {
            // Bu üye için bu dönemde zaten aidat var mı?
            if (aidatRepository.existsByUyeIdAndAidatDonemiId(uye.getId(), donem.getId())) {
                log.debug("Aidat already exists for uye: {} donem: {}", uye.getUyeNo(), donem.getDonemKodu());
                atlanan++;
                continue;
            }
            
            // Aidat tahakkuk oluştur
            Aidat aidat = Aidat.builder()
                .uye(uye)
                .aidatDonemi(donem)
                .birlik(donem.getBirlik())
                .tahakkukTarihi(LocalDate.now())
                .tahakkukTutari(donem.getTutar())
                .toplamBorc(donem.getTutar())
                .kalanBorc(donem.getTutar())
                .odenenTutar(BigDecimal.ZERO)
                .gecikmeFaizi(BigDecimal.ZERO)
                .aidatDurum(AidatDurum.BEKLIYOR)
                .sonOdemeTarihi(donem.getSonOdemeTarihi())
                .build();
            
            aidat.setTenantId(birlikId);
            aidatRepository.save(aidat);
            
            log.debug("Aidat created for uye: {}", uye.getUyeNo());
            olusturulan++;
        }
        
        log.info("Aidat tahakkuk completed: {} created, {} skipped for donem: {}", 
            olusturulan, atlanan, donem.getDonemAdi());
        
        auditLogService.log("TOPLU_AIDAT_TAHAKKUK", 
            String.format("Dönem '%s' için %d üyeye aidat tahakkuk edildi", donem.getDonemAdi(), olusturulan));
    }
}
