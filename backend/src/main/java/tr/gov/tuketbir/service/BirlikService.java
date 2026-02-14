package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.config.multitenancy.TenantContext;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.dto.birlik.*;
import tr.gov.tuketbir.exception.DuplicateResourceException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.BirlikRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Birlik Service - Birlik yönetimi iş mantığı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirlikService {

    private final BirlikRepository birlikRepository;

    public List<BirlikDTO> getBirlikler(String ilKodu, Boolean aktif) {
        List<Birlik> birlikler;
        
        if (ilKodu != null) {
            birlikler = birlikRepository.findByIlKodu(ilKodu);
        } else {
            birlikler = birlikRepository.findAll();
        }
        
        // Varsayılan olarak sadece aktif (soft-delete olmayan) kayıtları göster
        // aktif parametresi null ise sadece aktif olanları göster
        // aktif = false ise sadece silinmişleri göster
        // aktif = true ise sadece aktif olanları göster
        boolean showActive = aktif == null || aktif;
        
        return birlikler.stream()
                .filter(b -> b.getIsActive().equals(showActive))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public BirlikDTO getBirlikById(Long id) {
        Birlik birlik = birlikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", id));
        return toDTO(birlik);
    }

    public BirlikDTO getBirlikByKod(String birlikKodu) {
        Birlik birlik = birlikRepository.findByBirlikKodu(birlikKodu)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "birlikKodu", birlikKodu));
        return toDTO(birlik);
    }

    @Transactional
    public BirlikDTO createBirlik(BirlikCreateRequest request) {
        // Birlik kodu kontrolü
        if (birlikRepository.existsByBirlikKodu(request.getBirlikKodu())) {
            throw new DuplicateResourceException("Birlik", "birlikKodu", request.getBirlikKodu());
        }

        Birlik birlik = new Birlik();
        birlik.setBirlikKodu(request.getBirlikKodu());
        birlik.setBirlikAdi(request.getBirlikAdi());
        birlik.setBirlikTipi(request.getBirlikTipi());
        birlik.setIlKodu(request.getIlKodu());
        birlik.setIlceKodu(request.getIlceKodu());
        birlik.setAdres(request.getAdres());
        birlik.setTelefon(request.getTelefon());
        birlik.setEmail(request.getEmail());
        birlik.setVergiNo(request.getVergiNo());
        birlik.setVergiDairesi(request.getVergiDairesi());
        birlik.setTenantId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : 0L);

        if (request.getUstBirlikId() != null) {
            Birlik parentBirlik = birlikRepository.findById(request.getUstBirlikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Üst Birlik", "id", request.getUstBirlikId()));
            birlik.setParentBirlik(parentBirlik);
        }

        birlik = birlikRepository.save(birlik);
        
        log.info("Created new birlik: {} - {}", birlik.getBirlikKodu(), birlik.getBirlikAdi());
        return toDTO(birlik);
    }

    @Transactional
    public BirlikDTO updateBirlik(Long id, BirlikUpdateRequest request) {
        Birlik birlik = birlikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", id));

        if (request.getBirlikAdi() != null) {
            birlik.setBirlikAdi(request.getBirlikAdi());
        }
        if (request.getBirlikTipi() != null) {
            birlik.setBirlikTipi(request.getBirlikTipi());
        }
        if (request.getIlKodu() != null) {
            birlik.setIlKodu(request.getIlKodu());
        }
        if (request.getIlceKodu() != null) {
            birlik.setIlceKodu(request.getIlceKodu());
        }
        if (request.getAdres() != null) {
            birlik.setAdres(request.getAdres());
        }
        if (request.getTelefon() != null) {
            birlik.setTelefon(request.getTelefon());
        }
        if (request.getEmail() != null) {
            birlik.setEmail(request.getEmail());
        }
        if (request.getVergiNo() != null) {
            birlik.setVergiNo(request.getVergiNo());
        }
        if (request.getVergiDairesi() != null) {
            birlik.setVergiDairesi(request.getVergiDairesi());
        }
        if (request.getAktif() != null) {
            birlik.setIsActive(request.getAktif());
        }
        if (request.getUstBirlikId() != null) {
            Birlik parentBirlik = birlikRepository.findById(request.getUstBirlikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Üst Birlik", "id", request.getUstBirlikId()));
            birlik.setParentBirlik(parentBirlik);
        }

        birlik = birlikRepository.save(birlik);
        
        log.info("Updated birlik: {}", id);
        return toDTO(birlik);
    }

    @Transactional
    public void deleteBirlik(Long id) {
        Birlik birlik = birlikRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", id));

        birlik.softDelete();
        birlikRepository.save(birlik);
        
        log.info("Deleted (soft) birlik: {}", id);
    }

    public List<BirlikDTO> getAltBirlikler(Long id) {
        List<Birlik> altBirlikler = birlikRepository.findByParentBirlikId(id);
        return altBirlikler.stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BirlikDTO> getBirlikHierarchy() {
        List<Birlik> rootBirlikler = birlikRepository.findByParentBirlikIsNull();
        return rootBirlikler.stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getBirlikStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long total = birlikRepository.count();
        long aktif = birlikRepository.countByIsActiveTrue(); // Optimize edildi
        
        stats.put("toplamBirlikSayisi", total);
        stats.put("aktifBirlikSayisi", aktif);
        
        return stats;
    }

    public List<BirlikDTO> getBirliklerByIl(String ilKodu) {
        List<Birlik> birlikler = birlikRepository.findByIlKodu(ilKodu);
        return birlikler.stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BirlikDTO toDTO(Birlik birlik) {
        // Üye ve alt birlik sayıları için ayrı count sorgusu kullan (N+1 sorunu önleme)
        Long uyeSayisi = birlikRepository.countAktifUyelerByBirlikId(birlik.getId());
        int altBirlikSayisi = birlik.getAltBirlikler() != null ? birlik.getAltBirlikler().size() : 0;
        
        return BirlikDTO.builder()
                .id(birlik.getId())
                .birlikKodu(birlik.getBirlikKodu())
                .birlikAdi(birlik.getBirlikAdi())
                .birlikTipi(birlik.getBirlikTipi())
                .ilKodu(birlik.getIlKodu())
                .ilceKodu(birlik.getIlceKodu())
                .adres(birlik.getAdres())
                .telefon(birlik.getTelefon())
                .email(birlik.getEmail())
                .vergiNo(birlik.getVergiNo())
                .vergiDairesi(birlik.getVergiDairesi())
                .ustBirlikId(birlik.getParentBirlik() != null ? birlik.getParentBirlik().getId() : null)
                .ustBirlikAdi(birlik.getParentBirlik() != null ? birlik.getParentBirlik().getBirlikAdi() : null)
                .uyeSayisi(uyeSayisi != null ? uyeSayisi.intValue() : 0)
                .altBirlikSayisi(altBirlikSayisi)
                .aktif(birlik.getIsActive())
                .createdAt(birlik.getCreatedAt())
                .updatedAt(birlik.getUpdatedAt())
                .build();
    }
}
