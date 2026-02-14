package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.service.SistemAyariService;

import java.util.Map;

/**
 * Sistem Controller
 * Sistem ayarları ve yönetim API'leri
 * Sadece SISTEM_ADMIN erişebilir
 */
@RestController
@RequestMapping("/api/v1/sistem")
@RequiredArgsConstructor
@Tag(name = "Sistem", description = "Sistem yönetimi API'leri")
public class SistemController {

    private final SistemAyariService sistemAyariService;

    /**
     * Read-only mod durumunu getir
     */
    @GetMapping("/read-only")
    @Operation(summary = "Read-only mod durumunu getir")
    public ResponseEntity<Map<String, Object>> getReadOnlyStatus() {
        return ResponseEntity.ok(Map.of(
                "aktif", sistemAyariService.isReadOnlyMode(),
                "mesaj", sistemAyariService.getReadOnlyMesaj()
        ));
    }

    /**
     * Read-only modu aç/kapat (Sadece SISTEM_ADMIN)
     */
    @PostMapping("/read-only")
    @PreAuthorize("hasRole('SISTEM_ADMIN')")
    @Operation(summary = "Read-only modunu aç/kapat")
    public ResponseEntity<Map<String, Object>> setReadOnlyMode(@RequestBody ReadOnlyModeRequest request) {
        sistemAyariService.setReadOnlyMode(request.aktif, request.mesaj);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "aktif", sistemAyariService.isReadOnlyMode(),
                "mesaj", sistemAyariService.getReadOnlyMesaj()
        ));
    }

    /**
     * Bakım modu durumunu getir
     */
    @GetMapping("/bakim-modu")
    @Operation(summary = "Bakım modu durumunu getir")
    public ResponseEntity<Map<String, Object>> getBakimModuStatus() {
        return ResponseEntity.ok(Map.of(
                "aktif", sistemAyariService.isBakimModu()
        ));
    }

    /**
     * Bakım modunu aç/kapat (Sadece SISTEM_ADMIN)
     */
    @PostMapping("/bakim-modu")
    @PreAuthorize("hasRole('SISTEM_ADMIN')")
    @Operation(summary = "Bakım modunu aç/kapat")
    public ResponseEntity<Map<String, Object>> setBakimModu(@RequestBody BakimModuRequest request) {
        sistemAyariService.setBakimModu(request.aktif);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "aktif", sistemAyariService.isBakimModu()
        ));
    }

    /**
     * Sistem durumu özeti
     */
    @GetMapping("/durum")
    @Operation(summary = "Sistem durumu özeti")
    public ResponseEntity<Map<String, Object>> getSistemDurumu() {
        return ResponseEntity.ok(Map.of(
                "readOnlyMode", sistemAyariService.isReadOnlyMode(),
                "readOnlyMesaj", sistemAyariService.getReadOnlyMesaj(),
                "bakimModu", sistemAyariService.isBakimModu()
        ));
    }

    // Request DTOs
    public static class ReadOnlyModeRequest {
        public boolean aktif;
        public String mesaj;
    }

    public static class BakimModuRequest {
        public boolean aktif;
    }
}
