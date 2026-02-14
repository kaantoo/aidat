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
import org.springframework.web.multipart.MultipartFile;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.uye.*;
import tr.gov.tuketbir.service.UyeService;

import java.util.List;
import java.util.Map;

/**
 * Üye Controller - Üye yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/uyeler")
@RequiredArgsConstructor
@Tag(name = "Üye Yönetimi", description = "Üye CRUD ve arama işlemleri")
public class UyeController {

    private final UyeService uyeService;

    @GetMapping
    @Operation(summary = "Üye listesi", description = "Filtrelenmiş ve sayfalanmış üye listesi")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<UyeDTO>>> getUyeler(
            @ModelAttribute UyeSearchRequest searchRequest) {
        
        log.info("Getting uyeler with filters: {}", searchRequest);
        PagedResponse<UyeDTO> response = uyeService.searchUyeler(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @Operation(summary = "Üye arama", description = "Ad, soyad, TC veya üye no ile üye arama")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<UyeDTO>>> searchUyeler(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Searching uyeler with query: {}", query);
        UyeSearchRequest searchRequest = new UyeSearchRequest();
        searchRequest.setSearchTerm(query);
        searchRequest.setPage(page);
        searchRequest.setSize(size);
        PagedResponse<UyeDTO> response = uyeService.searchUyeler(searchRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Üye detayı", description = "ID ile üye detayını getirme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<UyeDTO>> getUyeById(@PathVariable Long id) {
        log.info("Getting uye by id: {}", id);
        UyeDTO response = uyeService.getUyeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/uye-no/{uyeNo}")
    @Operation(summary = "Üye no ile arama", description = "Üye numarası ile üye detayını getirme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<UyeDTO>> getUyeByUyeNo(@PathVariable String uyeNo) {
        log.info("Getting uye by uyeNo: {}", uyeNo);
        UyeDTO response = uyeService.getUyeByUyeNo(uyeNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/tc/{tcKimlikNo}")
    @Operation(summary = "TC Kimlik No ile arama", description = "TC Kimlik numarası ile üye detayını getirme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<UyeDTO>> getUyeByTcKimlikNo(@PathVariable String tcKimlikNo) {
        log.info("Getting uye by tcKimlikNo: {}", tcKimlikNo);
        UyeDTO response = uyeService.getUyeByTcKimlikNo(tcKimlikNo);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni üye ekleme", description = "Yeni üye kaydı oluşturma")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_CREATE')")
    public ResponseEntity<ApiResponse<UyeDTO>> createUye(
            @Valid @RequestBody UyeCreateRequest request) {
        
        log.info("Creating new uye: {} {}", request.getAd(), request.getSoyad());
        UyeDTO response = uyeService.createUye(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Üye başarıyla oluşturuldu"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Üye güncelleme", description = "Mevcut üye bilgilerini güncelleme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_UPDATE')")
    public ResponseEntity<ApiResponse<UyeDTO>> updateUye(
            @PathVariable Long id,
            @Valid @RequestBody UyeUpdateRequest request) {
        
        log.info("Updating uye: {}", id);
        UyeDTO response = uyeService.updateUye(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Üye başarıyla güncellendi"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Üye silme", description = "Üyeyi pasif yapma (soft delete)")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteUye(@PathVariable Long id) {
        log.info("Deleting uye: {}", id);
        uyeService.deleteUye(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Üye başarıyla silindi"));
    }

    @PostMapping("/import/excel")
    @Operation(summary = "Excel ile toplu üye aktarımı", description = "Excel dosyasından toplu üye aktarımı")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_IMPORT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importFromExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam Long birlikId) {
        
        log.info("Importing uyeler from Excel for birlik: {}", birlikId);
        Map<String, Object> result = uyeService.importFromExcel(file, birlikId);
        return ResponseEntity.ok(ApiResponse.success(result, "Excel aktarımı tamamlandı"));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Excel'e aktarım", description = "Üye listesini Excel formatında dışa aktarma")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_UYE')")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute UyeSearchRequest searchRequest) {
        
        log.info("Exporting uyeler to Excel");
        byte[] excelData = uyeService.exportToExcel(searchRequest);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=uyeler.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }

    @GetMapping("/birlik/{birlikId}")
    @Operation(summary = "Birlik üyeleri", description = "Belirli birliğe ait üye listesi")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_READ')")
    public ResponseEntity<ApiResponse<List<UyeDTO>>> getUyelerByBirlik(
            @PathVariable Long birlikId) {
        
        log.info("Getting uyeler by birlik: {}", birlikId);
        List<UyeDTO> response = uyeService.getUyelerByBirlik(birlikId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/stats")
    @Operation(summary = "Üye istatistikleri", description = "Üye istatistiklerini getirme")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_UYE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUyeStats(
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting uye stats for birlik: {}", birlikId);
        Map<String, Object> stats = uyeService.getUyeStats(birlikId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Üye aktivasyonu", description = "Pasif üyeyi aktif hale getirme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_UPDATE')")
    public ResponseEntity<ApiResponse<UyeDTO>> activateUye(@PathVariable Long id) {
        log.info("Activating uye: {}", id);
        UyeDTO response = uyeService.activateUye(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Üye aktif edildi"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Üye pasiffleştirme", description = "Aktif üyeyi pasif hale getirme")
    @PreAuthorize("hasAnyAuthority('PERM_UYE_UPDATE')")
    public ResponseEntity<ApiResponse<UyeDTO>> deactivateUye(@PathVariable Long id) {
        log.info("Deactivating uye: {}", id);
        UyeDTO response = uyeService.deactivateUye(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Üye pasif edildi"));
    }
}
