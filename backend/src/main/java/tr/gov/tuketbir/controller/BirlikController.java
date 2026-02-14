package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.dto.birlik.*;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.service.BirlikService;

import java.util.List;
import java.util.Map;

/**
 * Birlik Controller - Birlik yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/birlikler")
@RequiredArgsConstructor
@Tag(name = "Birlik Yönetimi", description = "Birlik CRUD işlemleri")
public class BirlikController {

    private final BirlikService birlikService;

    @GetMapping
    @Operation(summary = "Birlik listesi", description = "Tüm birlikleri listele")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<List<BirlikDTO>>> getBirlikler(
            @RequestParam(required = false) String ilKodu,
            @RequestParam(required = false) Boolean aktif) {
        
        log.info("Getting birlikler - ilKodu: {}, aktif: {}", ilKodu, aktif);
        List<BirlikDTO> response = birlikService.getBirlikler(ilKodu, aktif);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Birlik detayı", description = "ID ile birlik detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<BirlikDTO>> getBirlikById(@PathVariable Long id) {
        log.info("Getting birlik by id: {}", id);
        BirlikDTO response = birlikService.getBirlikById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/kod/{birlikKodu}")
    @Operation(summary = "Birlik kodu ile arama", description = "Birlik kodu ile birlik detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<BirlikDTO>> getBirlikByKod(@PathVariable String birlikKodu) {
        log.info("Getting birlik by kod: {}", birlikKodu);
        BirlikDTO response = birlikService.getBirlikByKod(birlikKodu);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni birlik oluştur", description = "Yeni birlik kaydı oluşturma")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_CREATE')")
    public ResponseEntity<ApiResponse<BirlikDTO>> createBirlik(
            @Valid @RequestBody BirlikCreateRequest request) {
        
        log.info("Creating new birlik: {}", request.getBirlikAdi());
        BirlikDTO response = birlikService.createBirlik(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Birlik başarıyla oluşturuldu"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Birlik güncelle", description = "Mevcut birlik bilgilerini güncelleme")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_UPDATE')")
    public ResponseEntity<ApiResponse<BirlikDTO>> updateBirlik(
            @PathVariable Long id,
            @Valid @RequestBody BirlikUpdateRequest request) {
        
        log.info("Updating birlik: {}", id);
        BirlikDTO response = birlikService.updateBirlik(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Birlik başarıyla güncellendi"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Birlik sil", description = "Birliği pasif yapma (soft delete)")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteBirlik(@PathVariable Long id) {
        log.info("Deleting birlik: {}", id);
        birlikService.deleteBirlik(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Birlik başarıyla silindi"));
    }

    @GetMapping("/{id}/alt-birlikler")
    @Operation(summary = "Alt birlikler", description = "Belirli birliğin alt birliklerini listele")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<List<BirlikDTO>>> getAltBirlikler(@PathVariable Long id) {
        log.info("Getting alt birlikler for: {}", id);
        List<BirlikDTO> response = birlikService.getAltBirlikler(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/hierarchy")
    @Operation(summary = "Birlik hiyerarşisi", description = "Birlik hiyerarşi ağacını getir")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<List<BirlikDTO>>> getBirlikHierarchy() {
        log.info("Getting birlik hierarchy");
        List<BirlikDTO> response = birlikService.getBirlikHierarchy();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Birlik istatistikleri", description = "Birlik istatistiklerini getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_GENEL')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBirlikStats() {
        log.info("Getting birlik stats");
        Map<String, Object> stats = birlikService.getBirlikStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/il/{ilKodu}")
    @Operation(summary = "İl bazlı birlikler", description = "Belirli ildeki birlikleri listele")
    @PreAuthorize("hasAnyAuthority('PERM_BIRLIK_READ')")
    public ResponseEntity<ApiResponse<List<BirlikDTO>>> getBirliklerByIl(@PathVariable String ilKodu) {
        log.info("Getting birlikler by il: {}", ilKodu);
        List<BirlikDTO> response = birlikService.getBirliklerByIl(ilKodu);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
