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
import tr.gov.tuketbir.dto.auth.RegisterRequest;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.kullanici.*;
import tr.gov.tuketbir.domain.enums.KullaniciRol;
import tr.gov.tuketbir.service.KullaniciService;

import java.util.List;

/**
 * Kullanıcı Controller - Kullanıcı yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/kullanicilar")
@RequiredArgsConstructor
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcı CRUD işlemleri")
public class KullaniciController {

    private final KullaniciService kullaniciService;

    @GetMapping
    @Operation(summary = "Kullanıcı listesi", description = "Filtrelenmiş ve sayfalanmış kullanıcı listesi")
    @PreAuthorize("hasAnyAuthority('PERM_USER_READ')")
    public ResponseEntity<ApiResponse<PagedResponse<KullaniciDTO>>> getKullanicilar(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) KullaniciRol rol,
            @RequestParam(required = false) Boolean aktif,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("Getting kullanicilar - birlikId: {}, rol: {}, aktif: {}", birlikId, rol, aktif);
        PagedResponse<KullaniciDTO> response = kullaniciService.getKullanicilar(birlikId, rol, aktif, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Kullanıcı detayı", description = "ID ile kullanıcı detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_USER_READ')")
    public ResponseEntity<ApiResponse<KullaniciDTO>> getKullaniciById(@PathVariable Long id) {
        log.info("Getting kullanici by id: {}", id);
        KullaniciDTO response = kullaniciService.getKullaniciById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "Yeni kullanıcı oluştur", description = "Yeni kullanıcı kaydı oluşturma")
    @PreAuthorize("hasAnyAuthority('PERM_USER_CREATE')")
    public ResponseEntity<ApiResponse<KullaniciDTO>> createKullanici(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("Creating new kullanici: {}", request.getKullaniciAdi());
        KullaniciDTO response = kullaniciService.createKullanici(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Kullanıcı başarıyla oluşturuldu"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Kullanıcı güncelle", description = "Mevcut kullanıcı bilgilerini güncelleme")
    @PreAuthorize("hasAnyAuthority('PERM_USER_UPDATE')")
    public ResponseEntity<ApiResponse<KullaniciDTO>> updateKullanici(
            @PathVariable Long id,
            @Valid @RequestBody KullaniciUpdateRequest request) {
        
        log.info("Updating kullanici: {}", id);
        KullaniciDTO response = kullaniciService.updateKullanici(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı başarıyla güncellendi"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Kullanıcı sil", description = "Kullanıcıyı pasif yapma (soft delete)")
    @PreAuthorize("hasAnyAuthority('PERM_USER_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteKullanici(@PathVariable Long id) {
        log.info("Deleting kullanici: {}", id);
        kullaniciService.deleteKullanici(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Kullanıcı başarıyla silindi"));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Kullanıcı aktiffleştirme", description = "Pasif kullanıcıyı aktif hale getirme")
    @PreAuthorize("hasAnyAuthority('PERM_USER_UPDATE')")
    public ResponseEntity<ApiResponse<KullaniciDTO>> activateKullanici(@PathVariable Long id) {
        log.info("Activating kullanici: {}", id);
        KullaniciDTO response = kullaniciService.activateKullanici(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı aktif edildi"));
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Kullanıcı deaktiffleştirme", description = "Aktif kullanıcıyı pasif hale getirme")
    @PreAuthorize("hasAnyAuthority('PERM_USER_UPDATE')")
    public ResponseEntity<ApiResponse<KullaniciDTO>> deactivateKullanici(@PathVariable Long id) {
        log.info("Deactivating kullanici: {}", id);
        KullaniciDTO response = kullaniciService.deactivateKullanici(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Kullanıcı pasif edildi"));
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "Şifre sıfırlama", description = "Kullanıcı şifresini sıfırlama ve yeni şifre gönderme")
    @PreAuthorize("hasAnyAuthority('PERM_USER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable Long id) {
        log.info("Resetting password for kullanici: {}", id);
        kullaniciService.resetPassword(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Şifre sıfırlandı ve e-posta gönderildi"));
    }

    @GetMapping("/birlik/{birlikId}")
    @Operation(summary = "Birlik kullanıcıları", description = "Belirli birliğe ait kullanıcıları listele")
    @PreAuthorize("hasAnyAuthority('PERM_USER_READ')")
    public ResponseEntity<ApiResponse<List<KullaniciDTO>>> getKullanicilarByBirlik(@PathVariable Long birlikId) {
        log.info("Getting kullanicilar by birlik: {}", birlikId);
        List<KullaniciDTO> response = kullaniciService.getKullanicilarByBirlik(birlikId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/roller")
    @Operation(summary = "Roller listesi", description = "Kullanılabilir rolleri listele")
    @PreAuthorize("hasAnyAuthority('PERM_USER_READ')")
    public ResponseEntity<ApiResponse<List<KullaniciRol>>> getRoller() {
        log.info("Getting roller");
        List<KullaniciRol> roller = List.of(KullaniciRol.values());
        return ResponseEntity.ok(ApiResponse.success(roller));
    }
}
