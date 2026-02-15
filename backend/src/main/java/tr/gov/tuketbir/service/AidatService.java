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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
     * Dönem detayını ID ile getir
     */
    @Transactional(readOnly = true)
    public AidatDonemiDTO getAidatDonemiById(Long id) {
        AidatDonemi donem = aidatDonemiRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", id));
        return aidatMapper.toDTO(donem);
    }

    /**
     * Aidat dönemini güncelle
     */
    @Transactional
    public AidatDonemiDTO updateAidatDonemi(Long id, AidatDonemiCreateRequest dto) {
        AidatDonemi donem = aidatDonemiRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", id));

        donem.setDonemAdi(dto.getDonemAdi());
        donem.setDonemTipi(dto.getDonemTipi());
        donem.setYil(dto.getYil());
        donem.setBaslangicTarihi(dto.getBaslangicTarihi());
        donem.setBitisTarihi(dto.getBitisTarihi());
        donem.setSonOdemeTarihi(dto.getSonOdemeTarihi());
        donem.setAciklama(dto.getAciklama());
        if (dto.getTutar() != null) donem.setTutar(dto.getTutar());
        if (dto.getMerkezPayOrani() != null) donem.setMerkezPayOrani(dto.getMerkezPayOrani());
        if (dto.getGecikmeFaiziOrani() != null) donem.setGecikmeFaiziOrani(dto.getGecikmeFaiziOrani());
        if (dto.getAsgariUcretAciklama() != null) donem.setAsgariUcretAciklama(dto.getAsgariUcretAciklama());

        donem = aidatDonemiRepository.save(donem);
        log.info("Aidat donemi updated: {}", donem.getDonemAdi());
        return aidatMapper.toDTO(donem);
    }

    /**
     * Aidat dönemini sil (soft delete)
     */
    @Transactional
    public void deleteAidatDonemi(Long id) {
        AidatDonemi donem = aidatDonemiRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", id));

        // Döneme bağlı aidat var mı kontrol et
        List<Aidat> bagliAidatlar = aidatRepository.findByAidatDonemiId(id);
        if (!bagliAidatlar.isEmpty()) {
            throw new BusinessException("Bu döneme bağlı " + bagliAidatlar.size() + " adet aidat kaydı bulunmaktadır. Önce aidatları silin.");
        }

        donem.setDonemAktif(false);
        aidatDonemiRepository.save(donem);
        log.info("Aidat donemi deleted (soft): {}", id);
    }

    /**
     * Aidat detayını ID ile getir
     */
    @Transactional(readOnly = true)
    public AidatDTO getAidatById(Long id) {
        Aidat aidat = aidatRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Aidat", "id", id));
        return aidatMapper.toDTO(aidat);
    }

    /**
     * Belirli aidatın tahsilatlarını getir
     */
    @Transactional(readOnly = true)
    public List<TahsilatDTO> getTahsilatlarByAidat(Long aidatId) {
        return tahsilatRepository.findByAidatId(aidatId)
            .stream()
            .map(aidatMapper::toTahsilatDTO)
            .toList();
    }

    /**
     * Gecikme faizi hesapla ve güncelle
     */
    @Transactional
    public Map<String, Object> calculateGecikmeFaizi(Long birlikId) {
        List<Aidat> gecikmisBorclar;
        if (birlikId != null) {
            gecikmisBorclar = aidatRepository.findGecikmisBorclarByBirlik(birlikId, LocalDate.now());
        } else {
            gecikmisBorclar = aidatRepository.findGecikmisBorclar(LocalDate.now());
        }

        int guncellenen = 0;
        BigDecimal toplamFaiz = BigDecimal.ZERO;

        for (Aidat aidat : gecikmisBorclar) {
            AidatDonemi donem = aidat.getAidatDonemi();
            BigDecimal faiziOrani = donem.getGecikmeFaiziOrani();
            if (faiziOrani == null || faiziOrani.compareTo(BigDecimal.ZERO) <= 0) continue;

            long gecikmeGun = java.time.temporal.ChronoUnit.DAYS.between(aidat.getSonOdemeTarihi(), LocalDate.now());
            if (gecikmeGun <= 0) continue;

            // Günlük faiz oranı = yıllık oran / 365
            BigDecimal gunlukOran = faiziOrani.divide(BigDecimal.valueOf(36500), 10, java.math.RoundingMode.HALF_UP);
            BigDecimal faiz = aidat.getKalanBorc().multiply(gunlukOran).multiply(BigDecimal.valueOf(gecikmeGun));
            faiz = faiz.setScale(2, java.math.RoundingMode.HALF_UP);

            aidat.setGecikmeFaizi(faiz);
            aidat.setToplamBorc(aidat.getTahakkukTutari().add(faiz));
            aidat.setKalanBorc(aidat.getToplamBorc().subtract(aidat.getOdenenTutar()));
            aidat.setAidatDurum(AidatDurum.GECIKTI);
            aidatRepository.save(aidat);

            toplamFaiz = toplamFaiz.add(faiz);
            guncellenen++;
        }

        log.info("Gecikme faizi hesaplandı: {} aidat güncellendi, toplam faiz: {}", guncellenen, toplamFaiz);
        return Map.of(
            "guncellelenAidatSayisi", guncellenen,
            "toplamGecikmisBorcSayisi", gecikmisBorclar.size(),
            "toplamFaiz", toplamFaiz
        );
    }

    /**
     * Aidat listesini Excel'e aktar
     */
    @Transactional(readOnly = true)
    public byte[] exportAidatlarToExcel(AidatSearchRequest searchRequest) {
        List<Aidat> aidatlar;
        if (searchRequest.getBirlikId() != null) {
            aidatlar = aidatRepository.findByBirlikId(searchRequest.getBirlikId());
        } else {
            aidatlar = aidatRepository.findAll();
        }

        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            var sheet = workbook.createSheet("Aidatlar");

            // Header style
            var headerStyle = workbook.createCellStyle();
            var headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            var headerRow = sheet.createRow(0);
            String[] headers = {"Üye No", "Üye Ad Soyad", "Dönem", "Tahakkuk", "Ödenen", "Kalan", "Durum", "Son Ödeme Tarihi"};
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Aidat aidat : aidatlar) {
                var row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(aidat.getUye() != null ? aidat.getUye().getUyeNo() : "");
                row.createCell(1).setCellValue(aidat.getUye() != null ? aidat.getUye().getAd() + " " + aidat.getUye().getSoyad() : "");
                row.createCell(2).setCellValue(aidat.getAidatDonemi() != null ? aidat.getAidatDonemi().getDonemAdi() : "");
                row.createCell(3).setCellValue(aidat.getTahakkukTutari() != null ? aidat.getTahakkukTutari().doubleValue() : 0);
                row.createCell(4).setCellValue(aidat.getOdenenTutar() != null ? aidat.getOdenenTutar().doubleValue() : 0);
                row.createCell(5).setCellValue(aidat.getKalanBorc() != null ? aidat.getKalanBorc().doubleValue() : 0);
                row.createCell(6).setCellValue(aidat.getAidatDurum() != null ? aidat.getAidatDurum().name() : "");
                row.createCell(7).setCellValue(aidat.getSonOdemeTarihi() != null ? aidat.getSonOdemeTarihi().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            var outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting aidatlar to Excel", e);
            throw new BusinessException("Excel dosyası oluşturulurken hata oluştu: " + e.getMessage());
        }
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
        
        // Tutar null kontrolü (merkez birlik dönemleri için tutar olmayabilir)
        BigDecimal tahakkukTutari = donem.getTutar() != null ? donem.getTutar() : BigDecimal.ZERO;
        if (tahakkukTutari.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Aidat tutarı belirlenemeyen döneme toplu atama yapılamaz. Lütfen dönem tutarını belirleyin.");
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
                .tahakkukTutari(tahakkukTutari)
                .toplamBorc(tahakkukTutari)
                .kalanBorc(tahakkukTutari)
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
        
        // Tutar null kontrolü
        BigDecimal tahakkukTutari = donem.getTutar() != null ? donem.getTutar() : BigDecimal.ZERO;
        if (tahakkukTutari.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Aidat tutarı belirlenemeyen döneme atama yapılamaz. Lütfen dönem tutarını belirleyin.");
        }
        
        Aidat aidat = Aidat.builder()
            .uye(uye)
            .aidatDonemi(donem)
            .birlik(uye.getBirlik())
            .tahakkukTarihi(LocalDate.now())
            .tahakkukTutari(tahakkukTutari)
            .toplamBorc(tahakkukTutari)
            .kalanBorc(tahakkukTutari)
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

    // ======================= Excel Import İşlemleri =======================

    /**
     * Excel şablon dosyası oluştur (dönem import için)
     */
    public byte[] generateDonemImportTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dönem Şablonu");

            // Header stili
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Açıklama satırı
            CellStyle infoStyle = workbook.createCellStyle();
            Font infoFont = workbook.createFont();
            infoFont.setItalic(true);
            infoFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            infoStyle.setFont(infoFont);

            // Header
            String[] headers = {
                "Dönem Kodu*", "Dönem Adı*", "Birlik ID*", "Dönem Tipi*",
                "Yıl*", "Başlangıç Tarihi*", "Bitiş Tarihi*", "Son Ödeme Tarihi*",
                "Aidat Tutarı", "Merkez Pay Oranı (%)", "Gecikme Faizi Oranı (%)", "Açıklama"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Açıklama satırları
            String[] explanations = {
                "Benzersiz kod (ör: 2026-YILLIK)", "Dönem açıklayıcı adı", "Birlik numarası (DB ID)",
                "YILLIK veya ALTI_AYLIK", "Dönem yılı (ör: 2026)",
                "GG.AA.YYYY", "GG.AA.YYYY", "GG.AA.YYYY",
                "Alt birlik için zorunlu", "Merkez birlik için zorunlu (ör: 10)",
                "Aylık gecikme oranı (ör: 2)", "İsteğe bağlı"
            };
            Row infoRow = sheet.createRow(1);
            for (int i = 0; i < explanations.length; i++) {
                Cell cell = infoRow.createCell(i);
                cell.setCellValue(explanations[i]);
                cell.setCellStyle(infoStyle);
            }

            // Örnek veri satırı
            Row exampleRow = sheet.createRow(2);
            exampleRow.createCell(0).setCellValue("2026-YILLIK");
            exampleRow.createCell(1).setCellValue("2026 Yılı Yıllık Aidat");
            exampleRow.createCell(2).setCellValue(2);
            exampleRow.createCell(3).setCellValue("YILLIK");
            exampleRow.createCell(4).setCellValue(2026);
            exampleRow.createCell(5).setCellValue("01.01.2026");
            exampleRow.createCell(6).setCellValue("31.12.2026");
            exampleRow.createCell(7).setCellValue("31.03.2026");
            exampleRow.createCell(8).setCellValue(1500.00);
            exampleRow.createCell(9).setCellValue("");
            exampleRow.createCell(10).setCellValue(2);
            exampleRow.createCell(11).setCellValue("Yıllık aidat dönemi");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating donem import template", e);
            throw new BusinessException("Şablon dosyası oluşturulurken hata oluştu");
        }
    }

    /**
     * Excel'den aidat dönemlerini toplu import et
     * Birlik bazlı import yapılabilir
     */
    @Transactional
    public Map<String, Object> importDonemlerFromExcel(MultipartFile file, Long defaultBirlikId) {
        log.info("Importing aidat donemleri from Excel, defaultBirlikId: {}", defaultBirlikId);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int rowNumber = 0;

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // İlk 2 satır header+açıklama, 3. satırdan itibaren veri
            for (int i = 2; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                rowNumber = i + 1; // 1-based for user display

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    String donemKodu = getCellStringValue(row.getCell(0));
                    String donemAdi = getCellStringValue(row.getCell(1));
                    Long birlikId = getCellLongValue(row.getCell(2));
                    String donemTipiStr = getCellStringValue(row.getCell(3));
                    Integer yil = getCellIntValue(row.getCell(4));
                    String baslangicStr = getCellStringValue(row.getCell(5));
                    String bitisStr = getCellStringValue(row.getCell(6));
                    String sonOdemeStr = getCellStringValue(row.getCell(7));
                    BigDecimal tutar = getCellBigDecimalValue(row.getCell(8));
                    BigDecimal merkezPayOrani = getCellBigDecimalValue(row.getCell(9));
                    BigDecimal gecikmeFaiziOrani = getCellBigDecimalValue(row.getCell(10));
                    String aciklama = getCellStringValue(row.getCell(11));

                    // Birlik ID: Excel'den veya default
                    if (birlikId == null) {
                        birlikId = defaultBirlikId;
                    }

                    // Validasyon
                    List<String> rowErrors = new ArrayList<>();
                    if (donemKodu == null || donemKodu.isBlank()) rowErrors.add("Dönem kodu boş");
                    if (donemAdi == null || donemAdi.isBlank()) rowErrors.add("Dönem adı boş");
                    if (donemTipiStr == null || donemTipiStr.isBlank()) rowErrors.add("Dönem tipi boş");
                    if (yil == null) rowErrors.add("Yıl boş");
                    if (baslangicStr == null || baslangicStr.isBlank()) rowErrors.add("Başlangıç tarihi boş");
                    if (bitisStr == null || bitisStr.isBlank()) rowErrors.add("Bitiş tarihi boş");
                    if (sonOdemeStr == null || sonOdemeStr.isBlank()) rowErrors.add("Son ödeme tarihi boş");

                    if (!rowErrors.isEmpty()) {
                        errors.add("Satır " + rowNumber + ": " + String.join(", ", rowErrors));
                        continue;
                    }

                    // Dönem tipi parse
                    tr.gov.tuketbir.domain.enums.DonemTipi donemTipi;
                    try {
                        donemTipi = tr.gov.tuketbir.domain.enums.DonemTipi.valueOf(donemTipiStr.trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Satır " + rowNumber + ": Geçersiz dönem tipi: " + donemTipiStr + " (YILLIK veya ALTI_AYLIK olmalı)");
                        continue;
                    }

                    // Tarih parse
                    LocalDate baslangicTarihi, bitisTarihi, sonOdemeTarihi;
                    try {
                        baslangicTarihi = LocalDate.parse(baslangicStr.trim(), dateFormat);
                        bitisTarihi = LocalDate.parse(bitisStr.trim(), dateFormat);
                        sonOdemeTarihi = LocalDate.parse(sonOdemeStr.trim(), dateFormat);
                    } catch (Exception e) {
                        errors.add("Satır " + rowNumber + ": Tarih formatı hatalı (GG.AA.YYYY olmalı)");
                        continue;
                    }

                    // Mükerrer dönem kodu kontrolü
                    if (aidatDonemiRepository.existsByDonemKodu(donemKodu.trim())) {
                        warnings.add("Satır " + rowNumber + ": '" + donemKodu + "' dönem kodu zaten mevcut, atlandı");
                        skippedCount++;
                        continue;
                    }

                    // Mükerrer dönem adı kontrolü
                    if (aidatDonemiRepository.existsByDonemAdi(donemAdi.trim())) {
                        warnings.add("Satır " + rowNumber + ": '" + donemAdi + "' dönem adı zaten mevcut, atlandı");
                        skippedCount++;
                        continue;
                    }

                    // Birlik bulma
                    Birlik birlik = null;
                    boolean merkezBirlikTarafindan = false;
                    if (birlikId != null) {
                        birlik = birlikRepository.findById(birlikId).orElse(null);
                        if (birlik == null) {
                            errors.add("Satır " + rowNumber + ": Birlik bulunamadı, ID: " + birlikId);
                            continue;
                        }
                        merkezBirlikTarafindan = BirlikTipi.MERKEZ.equals(birlik.getBirlikTipi());
                    }

                    // Tutar / Pay oranı validasyonu
                    if (merkezBirlikTarafindan) {
                        if (merkezPayOrani == null || merkezPayOrani.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Satır " + rowNumber + ": Merkez birlik dönemi için pay oranı zorunludur");
                            continue;
                        }
                        tutar = null; // Merkez birlik için tutar kullanılmaz
                    } else {
                        if (tutar == null || tutar.compareTo(BigDecimal.ZERO) <= 0) {
                            errors.add("Satır " + rowNumber + ": Alt birlik dönemi için aidat tutarı zorunludur");
                            continue;
                        }
                        merkezPayOrani = null; // Alt birlik için pay oranı kullanılmaz
                    }

                    // Entity oluştur
                    AidatDonemi donem = AidatDonemi.builder()
                        .donemKodu(donemKodu.trim())
                        .donemAdi(donemAdi.trim())
                        .birlik(birlik)
                        .donemTipi(donemTipi)
                        .yil(yil)
                        .baslangicTarihi(baslangicTarihi)
                        .bitisTarihi(bitisTarihi)
                        .sonOdemeTarihi(sonOdemeTarihi)
                        .tutar(tutar)
                        .merkezPayOrani(merkezPayOrani)
                        .gecikmeFaiziOrani(gecikmeFaiziOrani != null ? gecikmeFaiziOrani : BigDecimal.ZERO)
                        .aciklama(aciklama)
                        .donemAktif(true)
                        .build();

                    donem.setTenantId(birlik != null ? birlik.getId() : 0L);
                    aidatDonemiRepository.save(donem);
                    successCount++;

                    log.debug("Imported donem: {} for birlik: {}", donemKodu, birlikId);

                } catch (Exception e) {
                    errors.add("Satır " + rowNumber + ": Beklenmeyen hata - " + e.getMessage());
                    log.error("Error importing row {}", rowNumber, e);
                }
            }

        } catch (Exception e) {
            log.error("Error reading Excel file", e);
            throw new BusinessException("Excel dosyası okunamadı: " + e.getMessage());
        }

        auditLogService.log("DONEM_EXCEL_IMPORT",
            String.format("Excel'den %d dönem import edildi (%d atlandı, %d hata)", successCount, skippedCount, errors.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("basarili", successCount);
        result.put("atlanan", skippedCount);
        result.put("hatali", errors.size());
        result.put("toplam", successCount + skippedCount + errors.size());
        result.put("hatalar", errors);
        result.put("uyarilar", warnings);
        return result;
    }

    // ======================= Aidat (Borç) Excel Import İşlemleri =======================

    /**
     * Excel şablon dosyası oluştur (aidat/borç import için)
     * Hem üye bazlı hem birlik bazlı borçlar import edilebilir
     */
    public byte[] generateAidatImportTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Aidat Borç Şablonu");

            // Header stili
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Açıklama stili
            CellStyle infoStyle = workbook.createCellStyle();
            Font infoFont = workbook.createFont();
            infoFont.setItalic(true);
            infoFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            infoStyle.setFont(infoFont);

            // Zorunlu alan kırmızı stili
            CellStyle requiredStyle = workbook.createCellStyle();
            Font requiredFont = workbook.createFont();
            requiredFont.setItalic(true);
            requiredFont.setColor(IndexedColors.RED.getIndex());
            requiredStyle.setFont(requiredFont);

            // Header
            String[] headers = {
                "Üye No*", "Dönem Kodu*", "Tahakkuk Tutarı*", "Ödenen Tutar",
                "Gecikme Faizi", "Son Ödeme Tarihi", "Durum", "Açıklama"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Açıklama satırları
            String[] explanations = {
                "Üye numarası (zorunlu)", "Dönem kodu (DB'deki, ör: 2024-YILLIK)", "Aidat tutarı (zorunlu, ör: 1500.00)",
                "Ödenen tutar (opsiyonel, ör: 500.00)", "Gecikme faizi (opsiyonel, ör: 100.00)",
                "GG.AA.YYYY (opsiyonel, boşsa dönem son ödeme tarihi)", "BEKLIYOR/KISMI_ODENDI/ODENDI/GECIKTI/IPTAL (opsiyonel)",
                "İsteğe bağlı açıklama"
            };
            Row infoRow = sheet.createRow(1);
            for (int i = 0; i < explanations.length; i++) {
                Cell cell = infoRow.createCell(i);
                cell.setCellValue(explanations[i]);
                cell.setCellStyle(infoStyle);
            }

            // Örnek veri satırları
            Row ex1 = sheet.createRow(2);
            ex1.createCell(0).setCellValue("UYE-001");
            ex1.createCell(1).setCellValue("2024-YILLIK");
            ex1.createCell(2).setCellValue(1500.00);
            ex1.createCell(3).setCellValue(1500.00);
            ex1.createCell(4).setCellValue(0);
            ex1.createCell(5).setCellValue("31.03.2024");
            ex1.createCell(6).setCellValue("ODENDI");
            ex1.createCell(7).setCellValue("2024 yılı aidatı");

            Row ex2 = sheet.createRow(3);
            ex2.createCell(0).setCellValue("UYE-002");
            ex2.createCell(1).setCellValue("2024-YILLIK");
            ex2.createCell(2).setCellValue(1500.00);
            ex2.createCell(3).setCellValue(750.00);
            ex2.createCell(4).setCellValue(50.00);
            ex2.createCell(5).setCellValue("31.03.2024");
            ex2.createCell(6).setCellValue("KISMI_ODENDI");
            ex2.createCell(7).setCellValue("Yarısı ödendi");

            Row ex3 = sheet.createRow(4);
            ex3.createCell(0).setCellValue("UYE-003");
            ex3.createCell(1).setCellValue("2024-YILLIK");
            ex3.createCell(2).setCellValue(1500.00);
            ex3.createCell(3).setCellValue(0);
            ex3.createCell(4).setCellValue(200.00);
            ex3.createCell(5).setCellValue("31.03.2024");
            ex3.createCell(6).setCellValue("GECIKTI");
            ex3.createCell(7).setCellValue("Ödenmedi, gecikme faizi uygulandı");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating aidat import template", e);
            throw new BusinessException("Şablon dosyası oluşturulurken hata oluştu");
        }
    }

    /**
     * Excel'den aidat (borç) kayıtlarını toplu import et.
     * Üye No ve Dönem Kodu ile eşleştirme yapılır.
     * Birlik bilgisi üyenin birliğinden otomatik alınır.
     */
    @Transactional
    public Map<String, Object> importAidatlarFromExcel(MultipartFile file, Long defaultBirlikId) {
        log.info("Importing aidatlar (borçlar) from Excel, defaultBirlikId: {}", defaultBirlikId);

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;
        int rowNumber = 0;

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        // Dönem cache (aynı dönem kodu tekrar aranmasın)
        Map<String, AidatDonemi> donemCache = new HashMap<>();
        // Üye cache
        Map<String, Uye> uyeCache = new HashMap<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // İlk 2 satır header+açıklama, 3. satırdan itibaren veri
            for (int i = 2; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                rowNumber = i + 1; // 1-based for user display

                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    String uyeNo = getCellStringValue(row.getCell(0));
                    String donemKodu = getCellStringValue(row.getCell(1));
                    BigDecimal tahakkukTutari = getCellBigDecimalValue(row.getCell(2));
                    BigDecimal odenenTutar = getCellBigDecimalValue(row.getCell(3));
                    BigDecimal gecikmeFaizi = getCellBigDecimalValue(row.getCell(4));
                    String sonOdemeStr = getCellStringValue(row.getCell(5));
                    String durumStr = getCellStringValue(row.getCell(6));
                    String aciklama = getCellStringValue(row.getCell(7));

                    // Zorunlu alan validasyonu
                    List<String> rowErrors = new ArrayList<>();
                    if (uyeNo == null || uyeNo.isBlank()) rowErrors.add("Üye No boş");
                    if (donemKodu == null || donemKodu.isBlank()) rowErrors.add("Dönem Kodu boş");
                    if (tahakkukTutari == null || tahakkukTutari.compareTo(BigDecimal.ZERO) <= 0) {
                        rowErrors.add("Tahakkuk tutarı boş veya sıfır");
                    }

                    if (!rowErrors.isEmpty()) {
                        errors.add("Satır " + rowNumber + ": " + String.join(", ", rowErrors));
                        continue;
                    }

                    // Üye bul (cache'den veya DB'den)
                    Uye uye = uyeCache.get(uyeNo.trim());
                    if (uye == null) {
                        uye = uyeRepository.findByUyeNo(uyeNo.trim()).orElse(null);
                        if (uye != null) {
                            uyeCache.put(uyeNo.trim(), uye);
                        }
                    }
                    if (uye == null) {
                        errors.add("Satır " + rowNumber + ": Üye bulunamadı, Üye No: " + uyeNo);
                        continue;
                    }

                    // Birlik filtreleme: defaultBirlikId verilmişse, üye o birliğe ait mi?
                    if (defaultBirlikId != null && !defaultBirlikId.equals(uye.getBirlik().getId())) {
                        warnings.add("Satır " + rowNumber + ": Üye '" + uyeNo + "' seçilen birliğe ait değil, atlandı");
                        skippedCount++;
                        continue;
                    }

                    // Dönem bul (cache'den veya DB'den)
                    AidatDonemi donem = donemCache.get(donemKodu.trim());
                    if (donem == null) {
                        donem = aidatDonemiRepository.findByDonemKodu(donemKodu.trim()).orElse(null);
                        if (donem != null) {
                            donemCache.put(donemKodu.trim(), donem);
                        }
                    }
                    if (donem == null) {
                        errors.add("Satır " + rowNumber + ": Dönem bulunamadı, Dönem Kodu: " + donemKodu);
                        continue;
                    }

                    // Mükerrer kontrolü
                    if (aidatRepository.existsByUyeIdAndAidatDonemiId(uye.getId(), donem.getId())) {
                        warnings.add("Satır " + rowNumber + ": Üye '" + uyeNo + "' için '" + donemKodu + "' dönemi zaten mevcut, atlandı");
                        skippedCount++;
                        continue;
                    }

                    // Son ödeme tarihi
                    LocalDate sonOdemeTarihi = donem.getSonOdemeTarihi();
                    if (sonOdemeStr != null && !sonOdemeStr.isBlank()) {
                        try {
                            sonOdemeTarihi = LocalDate.parse(sonOdemeStr.trim(), dateFormat);
                        } catch (Exception e) {
                            warnings.add("Satır " + rowNumber + ": Son ödeme tarihi formatı hatalı, dönem tarihi kullanıldı");
                        }
                    }

                    // Durum parse
                    AidatDurum durum = AidatDurum.BEKLIYOR;
                    if (durumStr != null && !durumStr.isBlank()) {
                        try {
                            durum = AidatDurum.valueOf(durumStr.trim().toUpperCase());
                        } catch (IllegalArgumentException e) {
                            warnings.add("Satır " + rowNumber + ": Geçersiz durum '" + durumStr + "', BEKLIYOR olarak ayarlandı");
                        }
                    }

                    // Varsayılan değerler
                    if (odenenTutar == null) odenenTutar = BigDecimal.ZERO;
                    if (gecikmeFaizi == null) gecikmeFaizi = BigDecimal.ZERO;

                    // Durumu otomatik hesapla (eğer Excel'de belirtilmediyse)
                    if (durumStr == null || durumStr.isBlank()) {
                        if (odenenTutar.compareTo(BigDecimal.ZERO) == 0) {
                            if (sonOdemeTarihi != null && sonOdemeTarihi.isBefore(LocalDate.now())) {
                                durum = AidatDurum.GECIKTI;
                            } else {
                                durum = AidatDurum.BEKLIYOR;
                            }
                        } else if (odenenTutar.compareTo(tahakkukTutari.add(gecikmeFaizi)) >= 0) {
                            durum = AidatDurum.ODENDI;
                        } else {
                            durum = AidatDurum.KISMI_ODENDI;
                        }
                    }

                    // toplamBorc ve kalanBorc hesapla
                    BigDecimal toplamBorc = tahakkukTutari.add(gecikmeFaizi);
                    BigDecimal kalanBorc = toplamBorc.subtract(odenenTutar);
                    if (kalanBorc.compareTo(BigDecimal.ZERO) < 0) kalanBorc = BigDecimal.ZERO;

                    // Ödeme tamamlanma tarihi
                    LocalDateTime odemeTamamlanmaTarihi = null;
                    if (durum == AidatDurum.ODENDI) {
                        odemeTamamlanmaTarihi = sonOdemeTarihi != null ? sonOdemeTarihi.atStartOfDay() : LocalDateTime.now();
                    }

                    // Entity oluştur
                    Aidat aidat = Aidat.builder()
                        .uye(uye)
                        .aidatDonemi(donem)
                        .birlik(uye.getBirlik())
                        .tahakkukTarihi(donem.getBaslangicTarihi() != null ? donem.getBaslangicTarihi() : LocalDate.now())
                        .tahakkukTutari(tahakkukTutari)
                        .gecikmeFaizi(gecikmeFaizi)
                        .toplamBorc(toplamBorc)
                        .odenenTutar(odenenTutar)
                        .kalanBorc(kalanBorc)
                        .aidatDurum(durum)
                        .sonOdemeTarihi(sonOdemeTarihi)
                        .odemeTamamlanmaTarihi(odemeTamamlanmaTarihi)
                        .aciklama(aciklama)
                        .build();

                    aidat.setTenantId(uye.getBirlik().getId());
                    aidatRepository.save(aidat);
                    successCount++;

                    log.debug("Imported aidat: uyeNo={}, donemKodu={}, tutar={}", uyeNo, donemKodu, tahakkukTutari);

                } catch (Exception e) {
                    errors.add("Satır " + rowNumber + ": Beklenmeyen hata - " + e.getMessage());
                    log.error("Error importing aidat row {}", rowNumber, e);
                }
            }

        } catch (Exception e) {
            log.error("Error reading Excel file for aidat import", e);
            throw new BusinessException("Excel dosyası okunamadı: " + e.getMessage());
        }

        auditLogService.log("AIDAT_EXCEL_IMPORT",
            String.format("Excel'den %d aidat borcu import edildi (%d atlandı, %d hata)", successCount, skippedCount, errors.size()));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("basarili", successCount);
        result.put("atlanan", skippedCount);
        result.put("hatali", errors.size());
        result.put("toplam", successCount + skippedCount + errors.size());
        result.put("hatalar", errors);
        result.put("uyarilar", warnings);
        return result;
    }

    // ======================= Excel Yardımcı Metodlar =======================

    private boolean isRowEmpty(Row row) {
        for (int c = 0; c < 8; c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellStringValue(cell);
                if (val != null && !val.isBlank()) return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate()
                        .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> null;
        };
    }

    private Long getCellLongValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> (long) cell.getNumericCellValue();
                case STRING -> {
                    String val = cell.getStringCellValue().trim();
                    yield val.isEmpty() ? null : Long.parseLong(val);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getCellIntValue(Cell cell) {
        Long val = getCellLongValue(cell);
        return val != null ? val.intValue() : null;
    }

    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> {
                    String val = cell.getStringCellValue().trim().replace(",", ".");
                    yield val.isEmpty() ? null : new BigDecimal(val);
                }
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
