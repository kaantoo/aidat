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
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.gelirgider.*;
import tr.gov.tuketbir.dto.rapor.GelirGiderRaporDTO;
import tr.gov.tuketbir.service.GelirGiderService;

import java.util.List;
import java.util.Map;

/**
 * Gelir/Gider Controller - Gelir ve gider yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/gelir-gider")
@RequiredArgsConstructor
@Tag(name = "Gelir/Gider Yönetimi", description = "Gelir ve gider kayıt işlemleri")
public class GelirGiderController {

    private final GelirGiderService gelirGiderService;

    @GetMapping
    @Operation(summary = "Gelir/Gider listesi", description = "Filtrelenmiş ve sayfalanmış gelir/gider listesi")
    @PreAuthorize("hasAnyAuthority('PERM_GELIR_GIDER_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<GelirGiderDTO>>> getGelirGiderler(
            @ModelAttribute GelirGiderSearchRequest searchRequest) {
        
        log.info("Getting gelir/gider with filters: {}", searchRequest);
        PagedResponse<GelirGiderDTO> response = gelirGiderService.searchGelirGiderler(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Gelir/Gider detayı", description = "ID ile gelir/gider detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_GELIR_GIDER_READ')")
    public ResponseEntity<ApiResponse<GelirGiderDTO>> getGelirGiderById(@PathVariable Long id) {
        log.info("Getting gelir/gider by id: {}", id);
        GelirGiderDTO response = gelirGiderService.getGelirGiderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni gelir/gider kaydı", description = "Yeni gelir veya gider kaydı oluşturma")
    @PreAuthorize("hasAnyAuthority('PERM_GELIR_GIDER_CREATE')")
    public ResponseEntity<ApiResponse<GelirGiderDTO>> createGelirGider(
            @Valid @RequestBody GelirGiderCreateRequest request) {
        
        log.info("Creating new gelir/gider: {}", request.getTip());
        GelirGiderDTO response = gelirGiderService.createGelirGider(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kayıt başarıyla oluşturuldu"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Gelir/Gider güncelle", description = "Mevcut gelir/gider kaydını güncelleme")
    @PreAuthorize("hasAnyAuthority('PERM_GELIR_GIDER_UPDATE')")
    public ResponseEntity<ApiResponse<GelirGiderDTO>> updateGelirGider(
            @PathVariable Long id,
            @Valid @RequestBody GelirGiderCreateRequest request) {
        
        log.info("Updating gelir/gider: {}", id);
        GelirGiderDTO response = gelirGiderService.updateGelirGider(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Kayıt başarıyla güncellendi"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Gelir/Gider sil", description = "Gelir/gider kaydını silme (soft delete)")
    @PreAuthorize("hasAnyAuthority('PERM_GELIR_GIDER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteGelirGider(@PathVariable Long id) {
        log.info("Deleting gelir/gider: {}", id);
        gelirGiderService.deleteGelirGider(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Kayıt başarıyla silindi"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Gelir/Gider istatistikleri", description = "Gelir/gider istatistiklerini getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGelirGiderStats(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Integer ay) {
        
        log.info("Getting gelir/gider stats - birlikId: {}, yil: {}, ay: {}", birlikId, yil, ay);
        Map<String, Object> stats = gelirGiderService.getStats(birlikId, yil, ay);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/rapor/aylik")
    @Operation(summary = "Aylık rapor", description = "Aylık gelir/gider raporunu getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<GelirGiderRaporDTO>> getAylikRapor(
            @RequestParam Integer yil,
            @RequestParam Integer ay,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting aylik rapor - yil: {}, ay: {}, birlikId: {}", yil, ay, birlikId);
        GelirGiderRaporDTO rapor = gelirGiderService.getAylikRapor(yil, ay, birlikId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/rapor/yillik")
    @Operation(summary = "Yıllık rapor", description = "Yıllık gelir/gider raporunu getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<List<GelirGiderRaporDTO>>> getYillikRapor(
            @RequestParam Integer yil,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting yillik rapor - yil: {}, birlikId: {}", yil, birlikId);
        List<GelirGiderRaporDTO> rapor = gelirGiderService.getYillikRapor(yil, birlikId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Excel'e aktar", description = "Gelir/gider listesini Excel formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute GelirGiderSearchRequest searchRequest) {
        
        log.info("Exporting gelir/gider to Excel");
        byte[] excelData = gelirGiderService.exportToExcel(searchRequest);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=gelir_gider.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }
}
