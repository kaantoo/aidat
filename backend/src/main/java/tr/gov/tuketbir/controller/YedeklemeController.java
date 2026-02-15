package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.domain.entity.AuditLog;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.sistem.AuditLogDTO;
import tr.gov.tuketbir.dto.sistem.YedekBilgiDTO;
import tr.gov.tuketbir.dto.sistem.YedekDurumDTO;
import tr.gov.tuketbir.repository.AuditLogRepository;
import tr.gov.tuketbir.service.YedeklemeService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Yedekleme ve Audit Log Controller
 * 
 * @author Tuketbir Development Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sistem")
@RequiredArgsConstructor
@Tag(name = "Sistem Yönetimi", description = "Yedekleme ve audit log işlemleri")
public class YedeklemeController {

    private final YedeklemeService yedeklemeService;
    private final AuditLogRepository auditLogRepository;

    // ======================= Yedekleme Endpoints =======================

    @GetMapping("/yedekleme/durum")
    @Operation(summary = "Yedekleme durumu", description = "Yedekleme durumunu getir")
    @PreAuthorize("hasAnyAuthority('PERM_SISTEM_AYAR')")
    public ResponseEntity<ApiResponse<YedekDurumDTO>> getYedekDurumu() {
        log.info("Getting yedek durumu");
        YedekDurumDTO durum = yedeklemeService.getYedekDurumu();
        return ResponseEntity.ok(ApiResponse.success(durum));
    }

    @PostMapping("/yedekleme/yedekle")
    @Operation(summary = "Manuel yedekleme", description = "Manuel yedekleme başlat")
    @PreAuthorize("hasAnyAuthority('PERM_SISTEM_AYAR')")
    public ResponseEntity<ApiResponse<YedekBilgiDTO>> manuelYedekle() {
        log.info("Manuel yedekleme istendi");
        YedekBilgiDTO result = yedeklemeService.manuelYedekle();
        String msg = result.isBasarili() ? "Yedekleme başarıyla tamamlandı" : "Yedekleme başarısız: " + result.getAciklama();
        return ResponseEntity.ok(ApiResponse.success(result, msg));
    }

    @DeleteMapping("/yedekleme/{dosyaAdi}")
    @Operation(summary = "Yedek sil", description = "Yedek dosyasını sil")
    @PreAuthorize("hasAnyAuthority('PERM_SISTEM_AYAR')")
    public ResponseEntity<ApiResponse<Void>> deleteYedek(@PathVariable String dosyaAdi) {
        log.info("Deleting yedek: {}", dosyaAdi);
        boolean deleted = yedeklemeService.deleteYedek(dosyaAdi);
        return ResponseEntity.ok(ApiResponse.success(null, deleted ? "Yedek silindi" : "Yedek bulunamadı"));
    }

    // ======================= Audit Log Endpoints =======================

    @GetMapping("/audit-log")
    @Operation(summary = "Audit logları", description = "İşlem kayıtlarını listele")
    @PreAuthorize("hasAnyAuthority('PERM_SISTEM_AYAR', 'PERM_AUDIT_READ')")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String islemTipi,
            @RequestParam(required = false) String kullaniciAdi) {

        log.info("Getting audit logs - page: {}, islemTipi: {}, kullanici: {}", page, islemTipi, kullaniciAdi);
        
        var pageable = PageRequest.of(page, size, Sort.by("islemZamani").descending());
        var auditPage = auditLogRepository.findAll(pageable);

        List<AuditLogDTO> logs = auditPage.getContent().stream()
                .map(this::toAuditLogDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    private AuditLogDTO toAuditLogDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .kullaniciAdi(log.getKullaniciAdi())
                .kullaniciId(log.getKullaniciId())
                .islemTipi(log.getIslemTipi() != null ? log.getIslemTipi().name() : null)
                .entityTipi(log.getEntityTipi())
                .entityId(log.getEntityId())
                .birlikId(log.getBirlikId())
                .aciklama(log.getAciklama())
                .ipAdresi(log.getIpAdresi())
                .userAgent(log.getUserAgent())
                .requestUrl(log.getRequestUrl())
                .httpMetod(log.getHttpMetod())
                .basarili(log.getBasarili())
                .hataMesaji(log.getHataMesaji())
                .islemZamani(log.getIslemZamani())
                .build();
    }
}
