package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.toplanti.*;
import tr.gov.tuketbir.service.ToplantiService;

import java.time.LocalDate;
import java.util.List;

/**
 * Toplantı ve Karar Controller
 * 
 * Toplantı CRUD, karar yönetimi ve katılımcı yönetimi işlemleri.
 * 
 * @author Tuketbir Development Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/toplantilar")
@RequiredArgsConstructor
@Tag(name = "Toplantı ve Karar Yönetimi", description = "Toplantı, karar ve katılımcı yönetim işlemleri")
public class ToplantiController {

    private final ToplantiService toplantiService;

    // ======================= Toplantı Endpoints =======================

    @GetMapping
    @Operation(summary = "Toplantı listesi", description = "Filtrelenmiş ve sayfalanmış toplantı listesi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<ToplantiDTO>>> getToplantilar(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) ToplantiTuru toplantiTuru,
            @RequestParam(required = false) ToplantiDurumu durum,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baslangicTarihi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate bitisTarihi,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("Getting toplantilar - birlikId: {}, tur: {}, durum: {}", birlikId, toplantiTuru, durum);
        PagedResponse<ToplantiDTO> response = toplantiService.getToplantilar(
                birlikId, toplantiTuru, durum, baslangicTarihi, bitisTarihi, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Toplantı detayı", description = "Toplantı detayı, kararlar ve katılımcılarla birlikte")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ToplantiDTO>> getToplantiById(@PathVariable Long id) {
        log.info("Getting toplanti by id: {}", id);
        ToplantiDTO response = toplantiService.getToplantiById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Toplantı oluştur", description = "Yeni toplantı oluştur (kararlar ve katılımcılarla)")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_CREATE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<ToplantiDTO>> createToplanti(
            @Valid @RequestBody ToplantiCreateRequest request) {
        log.info("Creating toplanti: {}", request.getBaslik());
        ToplantiDTO response = toplantiService.createToplanti(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Toplantı başarıyla oluşturuldu"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Toplantı güncelle", description = "Toplantı bilgilerini güncelle")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_UPDATE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<ToplantiDTO>> updateToplanti(
            @PathVariable Long id,
            @Valid @RequestBody ToplantiUpdateRequest request) {
        log.info("Updating toplanti: {}", id);
        ToplantiDTO response = toplantiService.updateToplanti(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Toplantı güncellendi"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Toplantı sil", description = "Toplantıyı soft delete")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_DELETE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteToplanti(@PathVariable Long id) {
        log.info("Deleting toplanti: {}", id);
        toplantiService.deleteToplanti(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Toplantı silindi"));
    }

    // ======================= Karar Endpoints =======================

    @GetMapping("/{toplantiId}/kararlar")
    @Operation(summary = "Toplantı kararları", description = "Toplantının kararlarını listele")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<KararDTO>>> getKararlar(@PathVariable Long toplantiId) {
        log.info("Getting kararlar for toplanti: {}", toplantiId);
        List<KararDTO> response = toplantiService.getKararlarByToplanti(toplantiId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{toplantiId}/kararlar")
    @Operation(summary = "Karar ekle", description = "Toplantıya yeni karar ekle")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_CREATE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<KararDTO>> addKarar(
            @PathVariable Long toplantiId,
            @Valid @RequestBody KararCreateRequest request) {
        log.info("Adding karar to toplanti: {}", toplantiId);
        KararDTO response = toplantiService.addKarar(toplantiId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Karar eklendi"));
    }

    @PutMapping("/kararlar/{kararId}")
    @Operation(summary = "Karar güncelle", description = "Karar bilgilerini güncelle")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_UPDATE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<KararDTO>> updateKarar(
            @PathVariable Long kararId,
            @Valid @RequestBody KararCreateRequest request) {
        log.info("Updating karar: {}", kararId);
        KararDTO response = toplantiService.updateKarar(kararId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Karar güncellendi"));
    }

    @DeleteMapping("/kararlar/{kararId}")
    @Operation(summary = "Karar sil", description = "Kararı sil")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_DELETE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteKarar(@PathVariable Long kararId) {
        log.info("Deleting karar: {}", kararId);
        toplantiService.deleteKarar(kararId);
        return ResponseEntity.ok(ApiResponse.success(null, "Karar silindi"));
    }

    // ======================= Katılımcı Endpoints =======================

    @GetMapping("/{toplantiId}/katilimcilar")
    @Operation(summary = "Toplantı katılımcıları", description = "Toplantının katılımcılarını listele")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<KatilimciDTO>>> getKatilimcilar(@PathVariable Long toplantiId) {
        log.info("Getting katilimcilar for toplanti: {}", toplantiId);
        List<KatilimciDTO> response = toplantiService.getKatilimcilarByToplanti(toplantiId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{toplantiId}/katilimcilar")
    @Operation(summary = "Katılımcı ekle", description = "Toplantıya katılımcı ekle")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_CREATE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<KatilimciDTO>> addKatilimci(
            @PathVariable Long toplantiId,
            @Valid @RequestBody KatilimciCreateRequest request) {
        log.info("Adding katilimci to toplanti: {}", toplantiId);
        KatilimciDTO response = toplantiService.addKatilimci(toplantiId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Katılımcı eklendi"));
    }

    @DeleteMapping("/katilimcilar/{katilimciId}")
    @Operation(summary = "Katılımcı çıkar", description = "Katılımcıyı toplantıdan çıkar")
    @PreAuthorize("hasAnyAuthority('PERM_TOPLANTI_DELETE', 'PERM_BIRLIK_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> removeKatilimci(@PathVariable Long katilimciId) {
        log.info("Removing katilimci: {}", katilimciId);
        toplantiService.removeKatilimci(katilimciId);
        return ResponseEntity.ok(ApiResponse.success(null, "Katılımcı çıkarıldı"));
    }
}
