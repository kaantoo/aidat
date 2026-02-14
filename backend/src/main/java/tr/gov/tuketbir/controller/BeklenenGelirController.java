package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.dto.beklenengelir.*;
import tr.gov.tuketbir.service.BeklenenGelirService;

/**
 * Beklenen Gelir (Expected Income) REST Controller
 * 
 * Bu controller, aidat tahakkuklarına dayalı beklenen gelir hesaplamalarını sunar.
 * 
 * İş Kuralları:
 * - Alt Birlik: Beklenen Gelir = Aktif Üye Sayısı × Aidat Tutarı
 * - Merkez Birlik: Beklenen Gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)
 * - Hesaplamalar tahsilat durumundan bağımsızdır
 * 
 * @author Tuketbir Development Team
 */
@RestController
@RequestMapping("/api/v1/beklenen-gelir")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Beklenen Gelir", description = "Beklenen gelir hesaplama ve raporlama API'leri")
@SecurityRequirement(name = "bearerAuth")
public class BeklenenGelirController {

    private final BeklenenGelirService beklenenGelirService;

    /**
     * Alt birlik için beklenen gelir hesapla
     */
    @GetMapping("/alt-birlik/{birlikId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERKEZ_ADMIN', 'BIRLIK_YONETICISI', 'ALT_BIRLIK_ADMIN')")
    @Operation(
            summary = "Alt birlik beklenen gelir hesapla",
            description = "Belirli bir alt birlik için seçilen döneme göre beklenen gelir hesaplar. " +
                    "Formül: Beklenen Gelir = Aktif Üye Sayısı × Aidat Tutarı"
    )
    public ResponseEntity<AltBirlikBeklenenGelirDTO> getAltBirlikBeklenenGelir(
            @Parameter(description = "Alt birlik ID") @PathVariable Long birlikId,
            @Parameter(description = "Aidat dönemi ID") @RequestParam Long donemId
    ) {
        log.info("Beklenen gelir hesaplama talebi - birlikId: {}, donemId: {}", birlikId, donemId);
        
        AltBirlikBeklenenGelirDTO result = beklenenGelirService.hesaplaAltBirlikBeklenenGelir(birlikId, donemId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Merkez birlik için beklenen gelir hesapla (tüm alt birliklerin toplamı)
     */
    @GetMapping("/merkez")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERKEZ_ADMIN')")
    @Operation(
            summary = "Merkez beklenen gelir hesapla",
            description = "Tüm alt birliklerden gelen payları toplayarak merkez için beklenen gelir hesaplar. " +
                    "Formül: Merkez Beklenen Gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)"
    )
    public ResponseEntity<MerkezBeklenenGelirDTO> getMerkezBeklenenGelir(
            @Parameter(description = "Aidat dönemi ID") @RequestParam Long donemId
    ) {
        log.info("Merkez beklenen gelir hesaplama talebi - donemId: {}", donemId);
        
        MerkezBeklenenGelirDTO result = beklenenGelirService.hesaplaMerkezBeklenenGelir(donemId);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Belirli bir yıl için yıllık beklenen gelir özeti
     */
    @GetMapping("/yillik/{birlikId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERKEZ_ADMIN', 'BIRLIK_YONETICISI', 'ALT_BIRLIK_ADMIN')")
    @Operation(
            summary = "Yıllık beklenen gelir özeti",
            description = "Belirli bir birlik için yıl bazında tüm dönemlerin beklenen gelir özetini döner"
    )
    public ResponseEntity<BeklenenGelirOzetDTO> getYillikBeklenenGelirOzeti(
            @Parameter(description = "Birlik ID") @PathVariable Long birlikId,
            @Parameter(description = "Yıl (örn: 2024)") @RequestParam Integer yil
    ) {
        log.info("Yıllık beklenen gelir özeti talebi - birlikId: {}, yil: {}", birlikId, yil);
        
        BeklenenGelirOzetDTO result = beklenenGelirService.hesaplaYillikBeklenenGelirOzeti(birlikId, yil);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Dashboard için özet beklenen gelir bilgisi
     */
    @GetMapping("/ozet")
    @PreAuthorize("hasAnyRole('ADMIN', 'MERKEZ_ADMIN', 'BIRLIK_YONETICISI', 'ALT_BIRLIK_ADMIN')")
    @Operation(
            summary = "Dashboard özet",
            description = "Dashboard'da gösterilecek beklenen gelir özet bilgisi. " +
                    "Merkez birlik için tüm alt birliklerden toplam, alt birlik için kendi toplamı döner."
    )
    public ResponseEntity<BeklenenGelirOzetDTO> getDashboardOzet(
            @Parameter(description = "Birlik ID") @RequestParam Long birlikId,
            @Parameter(description = "Aidat dönemi ID") @RequestParam Long donemId
    ) {
        log.info("Dashboard özet talebi - birlikId: {}, donemId: {}", birlikId, donemId);
        
        BeklenenGelirOzetDTO result = beklenenGelirService.getDashboardOzet(birlikId, donemId);
        
        return ResponseEntity.ok(result);
    }
}
