package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.config.multitenancy.TenantContext;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Uye;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.uye.UyeCreateRequest;
import tr.gov.tuketbir.dto.uye.UyeDTO;
import tr.gov.tuketbir.dto.uye.UyeSearchRequest;
import tr.gov.tuketbir.dto.uye.UyeUpdateRequest;
import tr.gov.tuketbir.event.UyeKayitEvent;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.mapper.UyeMapper;
import tr.gov.tuketbir.repository.BirlikRepository;
import tr.gov.tuketbir.repository.UyeRepository;
import tr.gov.tuketbir.util.TcKimlikValidator;

import java.time.LocalDate;
import java.util.List;

/**
 * Üye Service
 * 
 * Üye CRUD işlemleri ve iş mantığı.
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UyeService {

    private final UyeRepository uyeRepository;
    private final BirlikRepository birlikRepository;
    private final UyeMapper uyeMapper;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Tüm üyeleri getir (tenant bazlı)
     */
    @Transactional(readOnly = true)
    public Page<UyeDTO> getAllUyeler(Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenant();
        
        Page<Uye> uyeler;
        if (tenantId != null && tenantId > 0) {
            // Alt birlik - sadece kendi üyeleri
            uyeler = uyeRepository.findByBirlikId(tenantId, pageable);
        } else {
            // Merkez - tüm üyeler
            uyeler = uyeRepository.findAll(pageable);
        }
        
        return uyeler.map(uyeMapper::toDTO);
    }

    /**
     * Birliğe göre üyeleri getir
     */
    @Transactional(readOnly = true)
    public Page<UyeDTO> getUyelerByBirlik(Long birlikId, Pageable pageable) {
        return uyeRepository.findByBirlikId(birlikId, pageable)
            .map(uyeMapper::toDTO);
    }

    /**
     * Duruma göre üyeleri getir
     */
    @Transactional(readOnly = true)
    public Page<UyeDTO> getUyelerByDurum(Long birlikId, UyeDurum durum, Pageable pageable) {
        return uyeRepository.findByBirlikIdAndUyeDurum(birlikId, durum, pageable)
            .map(uyeMapper::toDTO);
    }

    /**
     * Üye ara
     */
    @Transactional(readOnly = true)
    public Page<UyeDTO> searchUyeler(Long birlikId, String searchTerm, Pageable pageable) {
        Page<Uye> uyeler;
        if (birlikId != null) {
            uyeler = uyeRepository.searchInBirlik(birlikId, searchTerm, pageable);
        } else {
            uyeler = uyeRepository.searchAll(searchTerm, pageable);
        }
        return uyeler.map(uyeMapper::toDTO);
    }

    /**
     * ID ile üye getir
     */
    @Transactional(readOnly = true)
    public UyeDTO getUyeById(Long id) {
        Uye uye = findById(id);
        return uyeMapper.toDTO(uye);
    }

    /**
     * TC Kimlik No ile üye getir
     */
    @Transactional(readOnly = true)
    public UyeDTO getUyeByTcKimlikNo(String tcKimlikNo) {
        Uye uye = uyeRepository.findByTcKimlikNo(tcKimlikNo)
            .orElseThrow(() -> new ResourceNotFoundException("Üye", "tcKimlikNo", tcKimlikNo));
        return uyeMapper.toDTO(uye);
    }

    /**
     * Üye No ile üye getir
     */
    @Transactional(readOnly = true)
    public UyeDTO getUyeByUyeNo(String uyeNo) {
        Uye uye = uyeRepository.findByUyeNo(uyeNo)
            .orElseThrow(() -> new ResourceNotFoundException("Üye", "uyeNo", uyeNo));
        return uyeMapper.toDTO(uye);
    }

    /**
     * Yeni üye oluştur
     */
    @Transactional
    public UyeDTO createUye(UyeCreateRequest dto) {
        log.info("Creating new member: {}", dto.getTcKimlikNo());
        
        // TC Kimlik No doğrulama
        if (!TcKimlikValidator.isValid(dto.getTcKimlikNo())) {
            throw new BusinessException("Geçersiz TC Kimlik Numarası. 11 haneli, 0 ile başlamayan ve algoritması geçerli bir TC numarası giriniz.");
        }
        
        // Mükerrer kayıt kontrolü
        if (uyeRepository.existsByTcKimlikNo(dto.getTcKimlikNo())) {
            throw new BusinessException("Bu TC Kimlik Numarası ile kayıtlı üye bulunmaktadır");
        }
        
        // Birliği bul
        Birlik birlik = birlikRepository.findById(dto.getBirlikId())
            .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", dto.getBirlikId()));
        
        // Entity oluştur
        Uye uye = uyeMapper.toEntity(dto);
        uye.setBirlik(birlik);
        uye.setTenantId(birlik.getId());
        uye.setUyeDurum(UyeDurum.AKTIF);
        
        // Katılım tarihini ayarla (eğer belirtilmemişse bugünün tarihi)
        if (uye.getKatilimTarihi() == null) {
            uye.setKatilimTarihi(java.time.LocalDate.now());
        }
        
        // Otomatik üye numarası üret
        String uyeNo = generateUyeNo(birlik);
        uye.setUyeNo(uyeNo);
        
        // Kaydet
        uye = uyeRepository.save(uye);
        
        // Audit log - primitive değerler ile çağır (@Async için)
        auditLogService.logUyeOlusturma(uye.getId(), birlik.getId(), uye.getUyeNo(), uye.getTamAd());
        
        // Event publish
        eventPublisher.publishEvent(new UyeKayitEvent(this, uye, birlik, UyeKayitEvent.IslemTipi.OLUSTURULDU));
        
        log.info("Member created successfully: {}", uye.getUyeNo());
        return uyeMapper.toDTO(uye);
    }

    /**
     * Üye güncelle
     */
    @Transactional
    public UyeDTO updateUye(Long id, UyeUpdateRequest dto) {
        log.info("Updating member: {}", id);
        
        Uye uye = findById(id);
        
        // Birlik değişikliği kontrolü
        if (dto.getBirlikId() != null && !dto.getBirlikId().equals(uye.getBirlik().getId())) {
            Birlik yeniBirlik = birlikRepository.findById(dto.getBirlikId())
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", dto.getBirlikId()));
            uye.setBirlik(yeniBirlik);
            uye.setTenantId(yeniBirlik.getId());
            log.info("Member {} moved to new birlik: {}", id, yeniBirlik.getBirlikAdi());
        }
        
        // Güncelle - parametre sırası: request, entity
        uyeMapper.updateEntity(dto, uye);
        uye = uyeRepository.save(uye);
        
        // Audit log - primitive değerler ile çağır (@Async için)
        Long birlikId = uye.getBirlik() != null ? uye.getBirlik().getId() : null;
        auditLogService.logUyeGuncelleme(uye.getId(), birlikId, uye.getUyeNo(), uye.getTamAd());
        
        log.info("Member updated successfully: {}", uye.getUyeNo());
        return uyeMapper.toDTO(uye);
    }

    /**
     * Üyeyi pasifleştir (soft delete)
     */
    @Transactional
    public void pasifYap(Long id, String neden) {
        log.info("Deactivating member: {}", id);
        
        Uye uye = findById(id);
        Birlik birlik = uye.getBirlik();
        uye.pasifYap(neden);
        uyeRepository.save(uye);
        
        Long birlikId = birlik != null ? birlik.getId() : null;
        auditLogService.logUyePasif(uye.getId(), birlikId, uye.getUyeNo(), neden);
        
        // Event publish
        if (birlik != null) {
            eventPublisher.publishEvent(new UyeKayitEvent(this, uye, birlik, UyeKayitEvent.IslemTipi.PASIFE_ALINDI));
        }
        
        log.info("Member deactivated: {}", uye.getUyeNo());
    }

    /**
     * Üyeyi tekrar aktifleştir
     */
    @Transactional
    public void aktifYap(Long id) {
        log.info("Activating member: {}", id);
        
        Uye uye = findById(id);
        Birlik birlik = uye.getBirlik();
        uye.aktifYap();
        uyeRepository.save(uye);
        
        Long birlikId = birlik != null ? birlik.getId() : null;
        auditLogService.logUyeAktif(uye.getId(), birlikId, uye.getUyeNo());
        
        // Event publish
        if (birlik != null) {
            eventPublisher.publishEvent(new UyeKayitEvent(this, uye, birlik, UyeKayitEvent.IslemTipi.AKTIFE_ALINDI));
        }
        
        log.info("Member activated: {}", uye.getUyeNo());
    }

    /**
     * Toplu üye aktarımı (CSV/Excel)
     */
    @Transactional
    public BulkImportResult importUyeler(Long birlikId, List<UyeCreateRequest> uyeler) {
        log.info("Importing {} members for birlik: {}", uyeler.size(), birlikId);
        
        Birlik birlik = birlikRepository.findById(birlikId)
            .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", birlikId));
        
        int success = 0;
        int failed = 0;
        List<String> errors = new java.util.ArrayList<>();
        
        for (UyeCreateRequest dto : uyeler) {
            try {
                dto.setBirlikId(birlikId);
                createUye(dto);
                success++;
            } catch (Exception e) {
                failed++;
                errors.add(dto.getTcKimlikNo() + ": " + e.getMessage());
            }
        }
        
        log.info("Import completed: {} success, {} failed", success, failed);
        return new BulkImportResult(success, failed, errors);
    }

    /**
     * Üye istatistikleri
     */
    @Transactional(readOnly = true)
    public UyeIstatistik getIstatistikler(Long birlikId) {
        Long aktif = uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.AKTIF);
        Long pasif = uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.PASIF);
        Long askida = uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.ASKIYA_ALINMIS);
        
        return new UyeIstatistik(aktif, pasif, askida);
    }

    /**
     * Üye ara (PagedResponse döner)
     */
    @Transactional(readOnly = true)
    public PagedResponse<UyeDTO> searchUyeler(UyeSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
            searchRequest.getPage() != null ? searchRequest.getPage() : 0,
            searchRequest.getSize() != null ? searchRequest.getSize() : 20
        );
        
        Page<Uye> uyeler;
        Long birlikId = searchRequest.getBirlikId();
        String searchTerm = searchRequest.getSearchTerm();
        
        if (birlikId != null && searchTerm != null && !searchTerm.isEmpty()) {
            uyeler = uyeRepository.searchInBirlik(birlikId, searchTerm, pageable);
        } else if (birlikId != null) {
            uyeler = uyeRepository.findByBirlikId(birlikId, pageable);
        } else if (searchTerm != null && !searchTerm.isEmpty()) {
            uyeler = uyeRepository.searchAll(searchTerm, pageable);
        } else {
            uyeler = uyeRepository.findAll(pageable);
        }
        
        List<UyeDTO> content = uyeler.getContent().stream()
            .map(uyeMapper::toDTO)
            .collect(java.util.stream.Collectors.toList());
            
        return PagedResponse.of(uyeler, content);
    }

    /**
     * Üye silme (soft delete)
     */
    @Transactional
    public void deleteUye(Long id) {
        Uye uye = findById(id);
        uye.setUyeDurum(UyeDurum.PASIF);
        uye.setIsActive(false);
        uyeRepository.save(uye);
        Long birlikId = uye.getBirlik() != null ? uye.getBirlik().getId() : null;
        auditLogService.logUyePasif(uye.getId(), birlikId, uye.getUyeNo(), "Silinme işlemi");
        log.info("Member deleted (soft): {}", uye.getUyeNo());
    }

    /**
     * Excel'den import
     */
    @Transactional
    public java.util.Map<String, Object> importFromExcel(org.springframework.web.multipart.MultipartFile file, Long birlikId) {
        // Basit implementasyon
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", 0);
        result.put("failed", 0);
        result.put("message", "Excel import henüz implemente edilmedi");
        return result;
    }

    /**
     * Excel'e export
     */
    @Transactional(readOnly = true)
    public byte[] exportToExcel(UyeSearchRequest searchRequest) {
        // Basit implementasyon
        return new byte[0];
    }

    /**
     * Birliğe göre üye listesi
     */
    @Transactional(readOnly = true)
    public List<UyeDTO> getUyelerByBirlik(Long birlikId) {
        return uyeRepository.findByBirlikId(birlikId, Pageable.unpaged())
            .getContent().stream()
            .map(uyeMapper::toDTO)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Üye istatistikleri (Map döner)
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getUyeStats(Long birlikId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        if (birlikId != null) {
            stats.put("aktif", uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.AKTIF));
            stats.put("pasif", uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.PASIF));
            stats.put("askida", uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.ASKIYA_ALINMIS));
        } else {
            stats.put("aktif", uyeRepository.countByUyeDurum(UyeDurum.AKTIF));
            stats.put("pasif", uyeRepository.countByUyeDurum(UyeDurum.PASIF));
            stats.put("askida", uyeRepository.countByUyeDurum(UyeDurum.ASKIYA_ALINMIS));
        }
        return stats;
    }

    /**
     * Üye aktif et
     */
    @Transactional
    public UyeDTO activateUye(Long id) {
        Uye uye = findById(id);
        uye.setUyeDurum(UyeDurum.AKTIF);
        uyeRepository.save(uye);
        Long birlikId = uye.getBirlik() != null ? uye.getBirlik().getId() : null;
        auditLogService.logUyeAktif(uye.getId(), birlikId, uye.getUyeNo());
        log.info("Member activated: {}", uye.getUyeNo());
        return uyeMapper.toDTO(uye);
    }

    /**
     * Üye pasif et
     */
    @Transactional
    public UyeDTO deactivateUye(Long id) {
        Uye uye = findById(id);
        uye.setUyeDurum(UyeDurum.PASIF);
        uyeRepository.save(uye);
        Long birlikId = uye.getBirlik() != null ? uye.getBirlik().getId() : null;
        auditLogService.logUyePasif(uye.getId(), birlikId, uye.getUyeNo(), "Deaktivasyon işlemi");
        log.info("Member deactivated: {}", uye.getUyeNo());
        return uyeMapper.toDTO(uye);
    }

    // ======================= Private Methods =======================

    private Uye findById(Long id) {
        return uyeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Üye", "id", id));
    }

    /**
     * Otomatik üye numarası üret
     * Format: {BirlikKodu}-{YIL}-{5 haneli sıra no}
     * Örnek: ANK001-2026-00001
     */
    private String generateUyeNo(Birlik birlik) {
        String prefix = birlik.getBirlikKodu() + "-" + LocalDate.now().getYear() + "-";
        Integer maxNo = uyeRepository.findMaxUyeNoByPrefix(prefix);
        int nextNo = (maxNo == null ? 0 : maxNo) + 1;
        return prefix + String.format("%05d", nextNo);
    }

    // ======================= Inner Classes =======================

    public static class BulkImportResult {
        private final int success;
        private final int failed;
        private final List<String> errors;
        
        public BulkImportResult(int success, int failed, List<String> errors) {
            this.success = success;
            this.failed = failed;
            this.errors = errors;
        }
        
        public int getSuccess() { return success; }
        public int getFailed() { return failed; }
        public List<String> getErrors() { return errors; }
    }
    
    public static class UyeIstatistik {
        private final Long aktif;
        private final Long pasif;
        private final Long askida;
        
        public UyeIstatistik(Long aktif, Long pasif, Long askida) {
            this.aktif = aktif;
            this.pasif = pasif;
            this.askida = askida;
        }
        
        public Long getAktif() { return aktif; }
        public Long getPasif() { return pasif; }
        public Long getAskida() { return askida; }
        public Long getToplam() { return aktif + pasif + askida; }
    }
}
