package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tr.gov.tuketbir.dto.belge.*;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.domain.enums.BelgeTipi;
import tr.gov.tuketbir.service.BelgeService;

import java.util.List;

/**
 * Belge Controller - Belge yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/belgeler")
@RequiredArgsConstructor
@Tag(name = "Belge Yönetimi", description = "Belge yükleme, indirme ve yönetim işlemleri")
public class BelgeController {

    private final BelgeService belgeService;

    @GetMapping
    @Operation(summary = "Belge listesi", description = "Filtrelenmiş ve sayfalanmış belge listesi")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<BelgeDTO>>> getBelgeler(
            @RequestParam(required = false) Long uyeId,
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) BelgeTipi belgeTipi,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("Getting belgeler - uyeId: {}, birlikId: {}, belgeTipi: {}", uyeId, birlikId, belgeTipi);
        PagedResponse<BelgeDTO> response = belgeService.getBelgeler(uyeId, birlikId, belgeTipi, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Belge detayı", description = "ID ile belge detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_READ')")
    public ResponseEntity<ApiResponse<BelgeDTO>> getBelgeById(@PathVariable Long id) {
        log.info("Getting belge by id: {}", id);
        BelgeDTO response = belgeService.getBelgeById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Belge yükleme", description = "Yeni belge yükleme")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_CREATE')")
    public ResponseEntity<ApiResponse<BelgeDTO>> uploadBelge(
            @RequestParam("file") MultipartFile file,
            @RequestParam BelgeTipi belgeTipi,
            @RequestParam(required = false) String belgeNo,
            @RequestParam(required = false) String aciklama,
            @RequestParam(required = false) Long uyeId,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Uploading belge - belgeTipi: {}, uyeId: {}, birlikId: {}", belgeTipi, uyeId, birlikId);
        
        BelgeUploadRequest request = BelgeUploadRequest.builder()
                .belgeTipi(belgeTipi)
                .belgeNo(belgeNo)
                .dosyaAdi(file.getOriginalFilename())
                .aciklama(aciklama)
                .uyeId(uyeId)
                .birlikId(birlikId)
                .build();
        
        BelgeDTO response = belgeService.uploadBelge(file, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Belge başarıyla yüklendi"));
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Belge indirme", description = "Belge dosyasını indirme")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_DOWNLOAD')")
    public ResponseEntity<byte[]> downloadBelge(@PathVariable Long id) {
        log.info("Downloading belge: {}", id);
        
        BelgeDTO belge = belgeService.getBelgeById(id);
        byte[] fileData = belgeService.downloadBelge(id);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + belge.getDosyaAdi() + "\"")
                .header("Content-Type", belge.getMimeType())
                .body(fileData);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Belge silme", description = "Belgeyi silme (soft delete)")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteBelge(@PathVariable Long id) {
        log.info("Deleting belge: {}", id);
        belgeService.deleteBelge(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Belge başarıyla silindi"));
    }

    @GetMapping("/uye/{uyeId}")
    @Operation(summary = "Üye belgeleri", description = "Belirli üyeye ait belgeleri listele")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_READ')")
    public ResponseEntity<ApiResponse<List<BelgeDTO>>> getBelgelerByUye(@PathVariable Long uyeId) {
        log.info("Getting belgeler by uye: {}", uyeId);
        List<BelgeDTO> response = belgeService.getBelgelerByUye(uyeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/birlik/{birlikId}")
    @Operation(summary = "Birlik belgeleri", description = "Belirli birliğe ait belgeleri listele")
    @PreAuthorize("hasAnyAuthority('PERM_BELGE_READ')")
    public ResponseEntity<ApiResponse<List<BelgeDTO>>> getBelgelerByBirlik(@PathVariable Long birlikId) {
        log.info("Getting belgeler by birlik: {}", birlikId);
        List<BelgeDTO> response = belgeService.getBelgelerByBirlik(birlikId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
