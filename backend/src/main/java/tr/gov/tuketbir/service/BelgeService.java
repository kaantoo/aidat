package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tr.gov.tuketbir.domain.entity.Belge;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Uye;
import tr.gov.tuketbir.domain.enums.BelgeTipi;
import tr.gov.tuketbir.dto.belge.*;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.BelgeRepository;
import tr.gov.tuketbir.repository.BirlikRepository;
import tr.gov.tuketbir.repository.UyeRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Belge Service - Belge yönetimi iş mantığı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BelgeService {

    private final BelgeRepository belgeRepository;
    private final UyeRepository uyeRepository;
    private final BirlikRepository birlikRepository;

    private static final String UPLOAD_DIR = "uploads/belgeler";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public PagedResponse<BelgeDTO> getBelgeler(Long uyeId, Long birlikId, BelgeTipi belgeTipi, Integer page, Integer size) {
        PageRequest pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        Page<Belge> belgePage;

        if (uyeId != null) {
            belgePage = belgeRepository.findByUyeId(uyeId, pageable);
        } else if (birlikId != null && belgeTipi != null) {
            belgePage = belgeRepository.findByBirlikIdAndBelgeTipi(birlikId, belgeTipi, pageable);
        } else if (birlikId != null) {
            belgePage = belgeRepository.findByBirlikId(birlikId, pageable);
        } else {
            belgePage = belgeRepository.findAll(pageable);
        }

        List<BelgeDTO> content = belgePage.getContent().stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(belgePage, content);
    }

    public BelgeDTO getBelgeById(Long id) {
        Belge belge = belgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Belge", "id", id));
        return toDTO(belge);
    }

    @Transactional
    public BelgeDTO uploadBelge(MultipartFile file, BelgeUploadRequest request) {
        // Dosya boyutu kontrolü
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Dosya boyutu maksimum 10MB olabilir");
        }

        // Dosya uzantısı kontrolü
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("Geçersiz dosya adı");
        }

        try {
            // Dosya yolunu oluştur
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path uploadPath = Paths.get(UPLOAD_DIR, dateFolder);
            
            // Klasör yoksa oluştur
            Files.createDirectories(uploadPath);
            
            // Dosyayı kaydet
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);

            // Belge entity oluştur
            Belge belge = new Belge();
            belge.setBelgeTipi(request.getBelgeTipi());
            belge.setBelgeNo(request.getBelgeNo());
            belge.setDosyaAdi(originalFilename);
            belge.setDosyaYolu(filePath.toString());
            belge.setDosyaBoyutu(file.getSize());
            belge.setMimeTipi(file.getContentType());
            belge.setAciklama(request.getAciklama());
            belge.setYuklemeZamani(LocalDateTime.now());

            if (request.getUyeId() != null) {
                Uye uye = uyeRepository.findById(request.getUyeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Üye", "id", request.getUyeId()));
                belge.setUye(uye);
                belge.setBirlik(uye.getBirlik());
                belge.setTenantId(uye.getBirlik().getId());
            } else if (request.getBirlikId() != null) {
                Birlik birlik = birlikRepository.findById(request.getBirlikId())
                        .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", request.getBirlikId()));
                belge.setBirlik(birlik);
                belge.setTenantId(birlik.getId());
            }

            belge = belgeRepository.save(belge);
            
            log.info("Uploaded belge: {} - {}", belge.getId(), originalFilename);
            return toDTO(belge);

        } catch (IOException e) {
            log.error("File upload error: ", e);
            throw new BusinessException("Dosya yüklenirken bir hata oluştu");
        }
    }

    public byte[] downloadBelge(Long id) {
        Belge belge = belgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Belge", "id", id));

        try {
            Path filePath = Paths.get(belge.getDosyaYolu());
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("File download error: ", e);
            throw new BusinessException("Dosya indirilirken bir hata oluştu");
        }
    }

    @Transactional
    public void deleteBelge(Long id) {
        Belge belge = belgeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Belge", "id", id));

        belge.softDelete();
        belgeRepository.save(belge);
        
        log.info("Deleted (soft) belge: {}", id);
    }

    public List<BelgeDTO> getBelgelerByUye(Long uyeId) {
        List<Belge> belgeler = belgeRepository.findByUyeId(uyeId);
        return belgeler.stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<BelgeDTO> getBelgelerByBirlik(Long birlikId) {
        List<Belge> belgeler = belgeRepository.findByBirlikId(birlikId);
        return belgeler.stream()
                .filter(b -> b.getIsActive())
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BelgeDTO toDTO(Belge belge) {
        return BelgeDTO.builder()
                .id(belge.getId())
                .belgeTipi(belge.getBelgeTipi())
                .belgeNo(belge.getBelgeNo())
                .dosyaAdi(belge.getDosyaAdi())
                .dosyaYolu(belge.getDosyaYolu())
                .dosyaBoyutu(belge.getDosyaBoyutu())
                .mimeType(belge.getMimeTipi())
                .aciklama(belge.getAciklama())
                .uyeId(belge.getUye() != null ? belge.getUye().getId() : null)
                .uyeNo(belge.getUye() != null ? belge.getUye().getUyeNo() : null)
                .uyeAdSoyad(belge.getUye() != null ? belge.getUye().getAd() + " " + belge.getUye().getSoyad() : null)
                .birlikId(belge.getBirlik() != null ? belge.getBirlik().getId() : null)
                .birlikAdi(belge.getBirlik() != null ? belge.getBirlik().getBirlikAdi() : null)
                .aktif(belge.getIsActive())
                .createdAt(belge.getCreatedAt())
                .updatedAt(belge.getUpdatedAt())
                .downloadUrl("/api/v1/belgeler/" + belge.getId() + "/download")
                .build();
    }
}
