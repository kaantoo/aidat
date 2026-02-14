package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.*;
import tr.gov.tuketbir.domain.enums.AidatDurum;
import tr.gov.tuketbir.domain.enums.BirlikTipi;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.dto.aidat.*;
import tr.gov.tuketbir.event.DonemOlusturulduEvent;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.mapper.AidatMapper;
import tr.gov.tuketbir.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aidat Service
 * 
 * Aidat dönem yönetimi, tahakkuk ve tahsilat işlemleri.
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AidatService {

    private final AidatRepository aidatRepository;
    private final AidatDonemiRepository aidatDonemiRepository;
    private final TahsilatRepository tahsilatRepository;
    private final UyeRepository uyeRepository;
    private final BirlikRepository birlikRepository;
    private final AidatMapper aidatMapper;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    // ======================= Aidat Dönemi İşlemleri =======================

    /**
     * Yeni aidat dönemi oluştur
     * 
     * Merkez Birlik dönem oluşturduğunda:
     * - Tüm alt birliklere otomatik dönem atanır
     * - Alt birlik üyelerine aidat tahakkuk edilir
     * 
     * Alt Birlik dönem oluşturduğunda:
     * - Sadece kendi üyelerine aidat tahakkuk edilir
     */
    @Transactional
    public AidatDonemiDTO createDonem(AidatDonemiCreateRequest dto) {
        log.info("Creating new aidat donemi: {}", dto.getDonemAdi());
        
        // Dönem kodu kontrolü
        if (aidatDonemiRepository.existsByDonemKodu(dto.getDonemKodu())) {
            throw new BusinessException("Bu dönem kodu zaten kullanılıyor: " + dto.getDonemKodu());
        }
        
        // Dönem adı kontrolü
        if (aidatDonemiRepository.existsByDonemAdi(dto.getDonemAdi())) {
            throw new BusinessException("Bu dönem adı zaten kullanılıyor: " + dto.getDonemAdi());
        }
        
        AidatDonemi donem = aidatMapper.toEntity(dto);
        
        boolean merkezBirlikTarafindan = false;
        
        // Birlik varsa set et
        if (dto.getBirlikId() != null) {
            Birlik birlik = birlikRepository.findById(dto.getBirlikId())
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", dto.getBirlikId()));
            donem.setBirlik(birlik);
            donem.setTenantId(birlik.getId());
            
            // Merkez birlik mi kontrol et
            merkezBirlikTarafindan = BirlikTipi.MERKEZ.equals(birlik.getBirlikTipi());
            
            // Merkez birlik için pay oranı zorunlu, tutar kullanılmaz
            if (merkezBirlikTarafindan) {
                if (dto.getMerkezPayOrani() == null || dto.getMerkezPayOrani().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Merkez birlik dönemleri için pay oranı zorunludur");
                }
                donem.setTutar(null); // Merkez birlik için tutar kullanılmaz
                donem.setMerkezPayOrani(dto.getMerkezPayOrani());
            } else {
                // Alt birlik için tutar zorunlu
                if (dto.getTutar() == null || dto.getTutar().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException("Alt birlik dönemleri için aidat tutarı zorunludur");
                }
                donem.setMerkezPayOrani(null); // Alt birlik için pay oranı kullanılmaz
            }
        } else {
            donem.setTenantId(0L); // Merkez birlik (birlik bağımsız)
            merkezBirlikTarafindan = true;
            
            // Birlik belirtilmemişse ve tutar varsa alt birlikler için genel dönem
            // Pay oranı varsa merkez birlik dönemi
            if (dto.getMerkezPayOrani() != null && dto.getMerkezPayOrani().compareTo(BigDecimal.ZERO) > 0) {
                donem.setTutar(null);
                donem.setMerkezPayOrani(dto.getMerkezPayOrani());
            } else if (dto.getTutar() == null || dto.getTutar().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Aidat tutarı veya merkez pay oranı belirtilmelidir");
            }
        }
        
        donem = aidatDonemiRepository.save(donem);
        
        auditLogService.logDonemOlusturma(donem.getId(), donem.getDonemKodu());
        log.info("Aidat donemi created: {}", donem.getDonemAdi());
        
        // Event yayınla - alt birliklere veya üyelere otomatik atama için
        eventPublisher.publishEvent(new DonemOlusturulduEvent(this, donem, merkezBirlikTarafindan));
        
        return aidatMapper.toDTO(donem);
    }

    /**
     * Tüm aktif dönemleri getir
     */
    @Transactional(readOnly = true)
    public List<AidatDonemiDTO> getAktifDonemler() {
        return aidatDonemiRepository.findByDonemAktifTrue()
            .stream()
            .map(aidatMapper::toDTO)
            .toList();
    }

    /**
     * Birliğe göre dönemleri getir
     */
    @Transactional(readOnly = true)
    public List<AidatDonemiDTO> getDonemlerByBirlik(Long birlikId) {
        return aidatDonemiRepository.findByBirlikIdOrBirlikIdIsNull(birlikId)
            .stream()
            .map(aidatMapper::toDTO)
            .toList();
    }

    // ======================= Aidat Tahakkuk İşlemleri =======================

    /**
     * Tüm aktif üyelere toplu aidat atama
     */
    @Transactional
    public TopluAtamaResult topluAidatAtama(Long donemId, Long birlikId) {
        log.info("Starting bulk aidat assignment for donem: {}, birlik: {}", donemId, birlikId);
        
        AidatDonemi donem = aidatDonemiRepository.findById(donemId)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", donemId));
        
        // Birlik ID yoksa dönemin birliğini kullan
        Birlik birlik;
        List<Uye> aktifUyeler;
        
        if (birlikId != null) {
            birlik = birlikRepository.findById(birlikId)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", birlikId));
            aktifUyeler = uyeRepository.findByBirlikIdAndUyeDurum(birlikId, UyeDurum.AKTIF);
        } else if (donem.getBirlik() != null) {
            birlik = donem.getBirlik();
            aktifUyeler = uyeRepository.findByBirlikIdAndUyeDurum(birlik.getId(), UyeDurum.AKTIF);
        } else {
            // Dönem merkez birlik için - tüm aktif üyelere ata
            aktifUyeler = uyeRepository.findByUyeDurum(UyeDurum.AKTIF);
            birlik = null;
        }
        
        int success = 0;
        int skipped = 0;
        
        for (Uye uye : aktifUyeler) {
            // Zaten bu dönem için aidat var mı?
            if (aidatRepository.existsByUyeIdAndAidatDonemiId(uye.getId(), donemId)) {
                skipped++;
                continue;
            }
            
            Birlik uyeBirlik = birlik != null ? birlik : uye.getBirlik();
            
            Aidat aidat = Aidat.builder()
                .uye(uye)
                .aidatDonemi(donem)
                .birlik(uyeBirlik)
                .tahakkukTarihi(LocalDate.now())
                .tahakkukTutari(donem.getTutar())
                .toplamBorc(donem.getTutar())
                .kalanBorc(donem.getTutar())
                .odenenTutar(BigDecimal.ZERO)
                .gecikmeFaizi(BigDecimal.ZERO)
                .aidatDurum(AidatDurum.BEKLIYOR)
                .sonOdemeTarihi(donem.getSonOdemeTarihi())
                .build();
            aidat.setTenantId(uyeBirlik != null ? uyeBirlik.getId() : 1L);
            
            aidatRepository.save(aidat);
            success++;
        }
        
        log.info("Bulk assignment completed: {} success, {} skipped", success, skipped);
        if (birlik != null) {
            auditLogService.logTopluAidatAtama(donem.getDonemKodu(), birlik.getId(), birlik.getBirlikAdi(), success);
        } else {
            auditLogService.logTopluAidatAtama(donem.getDonemKodu(), null, "Tüm Birlikler", success);
        }
        
        return new TopluAtamaResult(success, skipped, aktifUyeler.size());
    }

    /**
     * Tek üyeye aidat ata
     */
    @Transactional
    public AidatDTO aidatAta(Long uyeId, Long donemId) {
        Uye uye = uyeRepository.findById(uyeId)
            .orElseThrow(() -> new ResourceNotFoundException("Üye", "id", uyeId));
        
        AidatDonemi donem = aidatDonemiRepository.findById(donemId)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", donemId));
        
        // Mükerrer kontrol
        if (aidatRepository.existsByUyeIdAndAidatDonemiId(uyeId, donemId)) {
            throw new BusinessException("Bu üye için bu dönemde zaten aidat kaydı bulunmaktadır");
        }
        
        Aidat aidat = Aidat.builder()
            .uye(uye)
            .aidatDonemi(donem)
            .birlik(uye.getBirlik())
            .tahakkukTarihi(LocalDate.now())
            .tahakkukTutari(donem.getTutar())
            .toplamBorc(donem.getTutar())
            .kalanBorc(donem.getTutar())
            .odenenTutar(BigDecimal.ZERO)
            .gecikmeFaizi(BigDecimal.ZERO)
            .aidatDurum(AidatDurum.BEKLIYOR)
            .sonOdemeTarihi(donem.getSonOdemeTarihi())
            .build();
        aidat.setTenantId(uye.getBirlik().getId());
        
        aidat = aidatRepository.save(aidat);
        return aidatMapper.toDTO(aidat);
    }

    // ======================= Aidat Sorgulama =======================

    /**
     * Tüm aidatları getir (sayfalı)
     */
    @Transactional(readOnly = true)
    public Page<AidatDTO> getAllAidatlar(Pageable pageable) {
        return aidatRepository.findAll(pageable)
            .map(aidatMapper::toDTO);
    }

    /**
     * Filtrelenmiş aidatları getir
     */
    @Transactional(readOnly = true)
    public Page<AidatDTO> getAidatlarFiltered(Long birlikId, Long donemId, String durum, 
            String baslangicTarihi, String bitisTarihi, Pageable pageable) {
        
        AidatDurum aidatDurum = null;
        if (durum != null && !durum.isEmpty()) {
            try {
                aidatDurum = AidatDurum.valueOf(durum);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid aidat durum: {}", durum);
            }
        }
        
        LocalDate startDate = null;
        LocalDate endDate = null;
        if (baslangicTarihi != null && !baslangicTarihi.isEmpty()) {
            startDate = LocalDate.parse(baslangicTarihi);
        }
        if (bitisTarihi != null && !bitisTarihi.isEmpty()) {
            endDate = LocalDate.parse(bitisTarihi);
        }
        
        Page<Aidat> result;
        if (startDate != null && endDate != null) {
            result = aidatRepository.findByFiltersWithDateRange(birlikId, donemId, aidatDurum, startDate, endDate, pageable);
        } else if (startDate != null) {
            result = aidatRepository.findByFiltersWithStartDate(birlikId, donemId, aidatDurum, startDate, pageable);
        } else if (endDate != null) {
            result = aidatRepository.findByFiltersWithEndDate(birlikId, donemId, aidatDurum, endDate, pageable);
        } else {
            result = aidatRepository.findByFiltersWithoutDate(birlikId, donemId, aidatDurum, pageable);
        }
        
        return result.map(aidatMapper::toDTO);
    }

    /**
     * Birliğe göre aidatları getir
     */
    @Transactional(readOnly = true)
    public Page<AidatDTO> getAidatlarByBirlik(Long birlikId, Pageable pageable) {
        return aidatRepository.findByBirlikId(birlikId, pageable)
            .map(aidatMapper::toDTO);
    }

    /**
     * Duruma göre aidatları getir
     */
    @Transactional(readOnly = true)
    public Page<AidatDTO> getAidatlarByDurum(Long birlikId, AidatDurum durum, Pageable pageable) {
        return aidatRepository.findByBirlikIdAndAidatDurum(birlikId, durum, pageable)
            .map(aidatMapper::toDTO);
    }

    /**
     * Üyeye göre aidatları getir
     */
    @Transactional(readOnly = true)
    public List<AidatDTO> getAidatlarByUye(Long uyeId) {
        return aidatRepository.findByUyeId(uyeId)
            .stream()
            .map(aidatMapper::toDTO)
            .toList();
    }

    /**
     * Üyenin ödenmemiş aidatlarını getir
     */
    @Transactional(readOnly = true)
    public List<AidatDTO> getOdenmemisAidatlar(Long uyeId) {
        return aidatRepository.findOdenmemisAidatlarByUye(uyeId)
            .stream()
            .map(aidatMapper::toDTO)
            .toList();
    }

    /**
     * Gecikmiş borçları getir
     */
    @Transactional(readOnly = true)
    public List<AidatDTO> getGecikmisBorclar(Long birlikId) {
        return aidatRepository.findGecikmisBorclarByBirlik(birlikId, LocalDate.now())
            .stream()
            .map(aidatMapper::toDTO)
            .toList();
    }

    // ======================= Tahsilat İşlemleri =======================

    /**
     * Tahsilat girişi
     */
    @Transactional
    public TahsilatDTO tahsilatGirisi(TahsilatCreateRequest dto) {
        log.info("Recording payment for aidat: {}", dto.getAidatId());
        
        Aidat aidat = aidatRepository.findById(dto.getAidatId())
            .orElseThrow(() -> new ResourceNotFoundException("Aidat", "id", dto.getAidatId()));
        
        // Aidat durumu kontrolü
        if (aidat.getAidatDurum() == AidatDurum.ODENDI) {
            throw new BusinessException("Bu aidat zaten ödenmiş");
        }
        if (aidat.getAidatDurum() == AidatDurum.IPTAL) {
            throw new BusinessException("İptal edilmiş aidat için tahsilat yapılamaz");
        }
        
        // Tutar kontrolü
        if (dto.getTutar().compareTo(aidat.getKalanBorc()) > 0) {
            throw new BusinessException("Ödenen tutar kalan borçtan fazla olamaz. Kalan borç: " + aidat.getKalanBorc());
        }
        
        // Tahsilat kaydı oluştur
        Tahsilat tahsilat = Tahsilat.builder()
            .aidat(aidat)
            .uye(aidat.getUye())
            .birlik(aidat.getBirlik())
            .tutar(dto.getTutar())
            .odemeTarihi(dto.getOdemeTarihi())
            .odemeTipi(dto.getOdemeTipi())
            .makbuzNo(dto.getMakbuzNo())
            .dekontNo(dto.getDekontNo())
            .aciklama(dto.getAciklama())
            .build();
        tahsilat.setTenantId(aidat.getTenantId());
        
        tahsilat = tahsilatRepository.save(tahsilat);
        
        // Aidat durumunu güncelle
        aidat.odemeYap(dto.getTutar());
        aidatRepository.save(aidat);
        
        // Audit log - primitive değerler ile
        Long birlikId = tahsilat.getBirlik() != null ? tahsilat.getBirlik().getId() : null;
        String uyeNo = tahsilat.getUye() != null ? tahsilat.getUye().getUyeNo() : "";
        auditLogService.logTahsilat(tahsilat.getId(), birlikId, tahsilat.getTutar().toString(), uyeNo);
        log.info("Payment recorded: {} for aidat: {}", dto.getTutar(), aidat.getId());
        
        return aidatMapper.toTahsilatDTO(tahsilat);
    }

    /**
     * Tahsilatı iptal et
     */
    @Transactional
    public void tahsilatIptal(Long tahsilatId, String neden) {
        log.info("Cancelling payment: {}", tahsilatId);
        
        Tahsilat tahsilat = tahsilatRepository.findById(tahsilatId)
            .orElseThrow(() -> new ResourceNotFoundException("Tahsilat", "id", tahsilatId));
        
        if (tahsilat.getIptalEdildi()) {
            throw new BusinessException("Bu tahsilat zaten iptal edilmiş");
        }
        
        // Tahsilatı iptal et
        tahsilat.iptalEt(neden);
        tahsilatRepository.save(tahsilat);
        
        // Aidatı geri güncelle
        Aidat aidat = tahsilat.getAidat();
        aidat.setOdenenTutar(aidat.getOdenenTutar().subtract(tahsilat.getTutar()));
        aidat.setKalanBorc(aidat.getToplamBorc().subtract(aidat.getOdenenTutar()));
        
        if (aidat.getKalanBorc().compareTo(BigDecimal.ZERO) > 0) {
            if (aidat.getOdenenTutar().compareTo(BigDecimal.ZERO) > 0) {
                aidat.setAidatDurum(AidatDurum.KISMI_ODENDI);
            } else if (LocalDate.now().isAfter(aidat.getSonOdemeTarihi())) {
                aidat.setAidatDurum(AidatDurum.GECIKTI);
            } else {
                aidat.setAidatDurum(AidatDurum.BEKLIYOR);
            }
            aidat.setOdemeTamamlanmaTarihi(null);
        }
        
        aidatRepository.save(aidat);
        
        // Audit log - primitive değerler ile
        Long birlikId = tahsilat.getBirlik() != null ? tahsilat.getBirlik().getId() : null;
        auditLogService.logTahsilatIptal(tahsilat.getId(), birlikId, tahsilat.getTutar().toString(), neden);
        log.info("Payment cancelled: {}", tahsilatId);
    }

    // ======================= İstatistikler =======================

    /**
     * Aidat özet istatistikleri
     */
    @Transactional(readOnly = true)
    public AidatOzet getAidatOzet(Long birlikId) {
        BigDecimal toplamBorc = aidatRepository.sumToplamBorcByBirlik(birlikId);
        BigDecimal odenen = aidatRepository.sumOdenenTutarByBirlik(birlikId);
        BigDecimal kalan = aidatRepository.sumKalanBorcByBirlik(birlikId);
        
        Long bekleyen = aidatRepository.countByBirlikIdAndDurum(birlikId, AidatDurum.BEKLIYOR);
        Long odenmis = aidatRepository.countByBirlikIdAndDurum(birlikId, AidatDurum.ODENDI);
        Long gecikMis = aidatRepository.countByBirlikIdAndDurum(birlikId, AidatDurum.GECIKTI);
        Long kismiOdendi = aidatRepository.countByBirlikIdAndDurum(birlikId, AidatDurum.KISMI_ODENDI);
        
        return new AidatOzet(
            toplamBorc != null ? toplamBorc : BigDecimal.ZERO,
            odenen != null ? odenen : BigDecimal.ZERO,
            kalan != null ? kalan : BigDecimal.ZERO,
            bekleyen, odenmis, gecikMis, kismiOdendi
        );
    }

    // ======================= Inner Classes =======================

    public static class TopluAtamaResult {
        private final int success;
        private final int skipped;
        private final int total;
        
        public TopluAtamaResult(int success, int skipped, int total) {
            this.success = success;
            this.skipped = skipped;
            this.total = total;
        }
        
        public int getSuccess() { return success; }
        public int getSkipped() { return skipped; }
        public int getTotal() { return total; }
    }
    
    public static class AidatOzet {
        private final BigDecimal toplamBorc;
        private final BigDecimal odenenTutar;
        private final BigDecimal kalanBorc;
        private final Long bekleyenSayisi;
        private final Long odenmisSayisi;
        private final Long gecikmisSayisi;
        private final Long kismiOdenmisSayisi;
        
        public AidatOzet(BigDecimal toplamBorc, BigDecimal odenenTutar, BigDecimal kalanBorc,
                         Long bekleyenSayisi, Long odenmisSayisi, Long gecikmisSayisi, Long kismiOdenmisSayisi) {
            this.toplamBorc = toplamBorc;
            this.odenenTutar = odenenTutar;
            this.kalanBorc = kalanBorc;
            this.bekleyenSayisi = bekleyenSayisi;
            this.odenmisSayisi = odenmisSayisi;
            this.gecikmisSayisi = gecikmisSayisi;
            this.kismiOdenmisSayisi = kismiOdenmisSayisi;
        }
        
        public BigDecimal getToplamBorc() { return toplamBorc; }
        public BigDecimal getOdenenTutar() { return odenenTutar; }
        public BigDecimal getKalanBorc() { return kalanBorc; }
        public Long getBekleyenSayisi() { return bekleyenSayisi; }
        public Long getOdenmisSayisi() { return odenmisSayisi; }
        public Long getGecikmisSayisi() { return gecikmisSayisi; }
        public Long getKismiOdenmisSayisi() { return kismiOdenmisSayisi; }
        
        public Long getToplamSayisi() {
            return bekleyenSayisi + odenmisSayisi + gecikmisSayisi + kismiOdenmisSayisi;
        }
        
        public double getTahsilatOrani() {
            if (toplamBorc.compareTo(BigDecimal.ZERO) == 0) return 100.0;
            return odenenTutar.divide(toplamBorc, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        }
    }

    /**
     * Tüm tahsilatları getir
     */
    @Transactional(readOnly = true)
    public List<TahsilatDTO> getAllTahsilatlar(Long birlikId) {
        List<Tahsilat> tahsilatlar;
        if (birlikId != null) {
            // JOIN FETCH ile N+1 sorunu önleme
            tahsilatlar = tahsilatRepository.findByBirlikIdWithRelationsOrderByOdemeTarihiDesc(birlikId);
        } else {
            // JOIN FETCH ile N+1 sorunu önleme
            tahsilatlar = tahsilatRepository.findAllWithRelationsOrderByOdemeTarihiDesc();
        }
        return tahsilatlar.stream()
            .map(aidatMapper::toTahsilatDTO)
            .toList();
    }
}
