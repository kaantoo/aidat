package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.GelirGider;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.gelirgider.*;
import tr.gov.tuketbir.dto.rapor.GelirGiderRaporDTO;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.BirlikRepository;
import tr.gov.tuketbir.repository.GelirGiderRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GelirGider Service - Gelir/Gider yönetimi iş mantığı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GelirGiderService {

    private final GelirGiderRepository gelirGiderRepository;
    private final BirlikRepository birlikRepository;

    public PagedResponse<GelirGiderDTO> searchGelirGiderler(GelirGiderSearchRequest request) {
        Sort sort = Sort.by(
                request.getSortDirection().equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC,
                request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // Filtreleme kriterleri ile arama
        Page<GelirGider> page;
        if (request.getBirlikId() != null && request.getTip() != null) {
            page = gelirGiderRepository.findByTipAndBirlikIdAndIsActiveTrueOrderByIslemTarihiDesc(
                    request.getTip(), request.getBirlikId(), pageable);
        } else if (request.getBirlikId() != null) {
            page = gelirGiderRepository.findByBirlikIdAndIsActiveTrueOrderByIslemTarihiDesc(
                    request.getBirlikId(), pageable);
        } else if (request.getTip() != null) {
            page = gelirGiderRepository.findByTipAndIsActiveTrueOrderByIslemTarihiDesc(
                    request.getTip(), pageable);
        } else {
            page = gelirGiderRepository.findByIsActiveTrueOrderByIslemTarihiDesc(pageable);
        }

        List<GelirGiderDTO> content = page.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(page, content);
    }

    public GelirGiderDTO getGelirGiderById(Long id) {
        GelirGider gelirGider = gelirGiderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GelirGider", "id", id));
        return toDTO(gelirGider);
    }

    @Transactional
    public GelirGiderDTO createGelirGider(GelirGiderCreateRequest request) {
        // Validasyon
        if (request.getTip() == GelirGiderTipi.GELIR && request.getGelirKategorisi() == null) {
            throw new BusinessException("Gelir kaydı için gelir kategorisi zorunludur");
        }
        if (request.getTip() == GelirGiderTipi.GIDER && request.getGiderKategorisi() == null) {
            throw new BusinessException("Gider kaydı için gider kategorisi zorunludur");
        }

        GelirGider gelirGider = new GelirGider();
        gelirGider.setTip(request.getTip());
        gelirGider.setGelirKategorisi(request.getGelirKategorisi());
        gelirGider.setGiderKategorisi(request.getGiderKategorisi());
        gelirGider.setTutar(request.getTutar());
        gelirGider.setIslemTarihi(request.getIslemTarihi() != null ? request.getIslemTarihi() : LocalDate.now());
        gelirGider.setBelgeNo(request.getBelgeNo());
        gelirGider.setAciklama(request.getAciklama());

        if (request.getBirlikId() != null) {
            Birlik birlik = birlikRepository.findById(request.getBirlikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", request.getBirlikId()));
            gelirGider.setBirlik(birlik);
            gelirGider.setTenantId(birlik.getId()); // Multi-tenancy için tenantId ayarla
        } else {
            gelirGider.setTenantId(1L); // Varsayılan tenant
        }

        gelirGider = gelirGiderRepository.save(gelirGider);
        
        log.info("Created new gelir/gider: {} - {}", gelirGider.getTip(), gelirGider.getTutar());
        return toDTO(gelirGider);
    }

    @Transactional
    public GelirGiderDTO updateGelirGider(Long id, GelirGiderCreateRequest request) {
        GelirGider gelirGider = gelirGiderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GelirGider", "id", id));

        gelirGider.setTip(request.getTip());
        gelirGider.setGelirKategorisi(request.getGelirKategorisi());
        gelirGider.setGiderKategorisi(request.getGiderKategorisi());
        gelirGider.setTutar(request.getTutar());
        if (request.getIslemTarihi() != null) {
            gelirGider.setIslemTarihi(request.getIslemTarihi());
        }
        if (request.getBelgeNo() != null) {
            gelirGider.setBelgeNo(request.getBelgeNo());
        }
        if (request.getAciklama() != null) {
            gelirGider.setAciklama(request.getAciklama());
        }

        gelirGider = gelirGiderRepository.save(gelirGider);
        
        log.info("Updated gelir/gider: {}", id);
        return toDTO(gelirGider);
    }

    @Transactional
    public void deleteGelirGider(Long id) {
        GelirGider gelirGider = gelirGiderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GelirGider", "id", id));

        gelirGider.setIsActive(false);
        gelirGiderRepository.save(gelirGider);
        
        log.info("Deleted (soft) gelir/gider: {}", id);
    }

    public Map<String, Object> getStats(Long birlikId, Integer yil, Integer ay) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("toplamGelir", BigDecimal.ZERO);
        stats.put("toplamGider", BigDecimal.ZERO);
        stats.put("netDurum", BigDecimal.ZERO);
        return stats;
    }

    public GelirGiderRaporDTO getAylikRapor(Integer yil, Integer ay, Long birlikId) {
        return GelirGiderRaporDTO.builder()
                .yil(yil)
                .ay(ay)
                .toplamGelir(BigDecimal.ZERO)
                .toplamGider(BigDecimal.ZERO)
                .netDurum(BigDecimal.ZERO)
                .build();
    }

    public List<GelirGiderRaporDTO> getYillikRapor(Integer yil, Long birlikId) {
        List<GelirGiderRaporDTO> raporlar = new ArrayList<>();
        
        for (int ay = 1; ay <= 12; ay++) {
            GelirGiderRaporDTO aylikRapor = getAylikRapor(yil, ay, birlikId);
            raporlar.add(aylikRapor);
        }
        
        return raporlar;
    }

    public byte[] exportToExcel(GelirGiderSearchRequest searchRequest) {
        // Excel export implementasyonu
        // Apache POI kullanılarak yapılacak
        return new byte[0];
    }

    private GelirGiderDTO toDTO(GelirGider gelirGider) {
        String kategoriAdi = gelirGider.getTip() == GelirGiderTipi.GELIR
                ? (gelirGider.getGelirKategorisi() != null ? gelirGider.getGelirKategorisi().name() : null)
                : (gelirGider.getGiderKategorisi() != null ? gelirGider.getGiderKategorisi().name() : null);

        return GelirGiderDTO.builder()
                .id(gelirGider.getId())
                .tipi(gelirGider.getTip())
                .gelirKategorisi(gelirGider.getGelirKategorisi())
                .giderKategorisi(gelirGider.getGiderKategorisi())
                .kategoriAdi(kategoriAdi)
                .tutar(gelirGider.getTutar())
                .islemTarihi(gelirGider.getIslemTarihi())
                .belgeNo(gelirGider.getBelgeNo())
                .aciklama(gelirGider.getAciklama())
                .birlikId(gelirGider.getBirlik() != null ? gelirGider.getBirlik().getId() : null)
                .birlikAdi(gelirGider.getBirlik() != null ? gelirGider.getBirlik().getBirlikAdi() : null)
                .aktif(gelirGider.getIsActive())
                .createdAt(gelirGider.getCreatedAt())
                .updatedAt(gelirGider.getUpdatedAt())
                .build();
    }
}
