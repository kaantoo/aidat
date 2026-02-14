package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.rapor.*;
import tr.gov.tuketbir.service.RaporService;

import java.util.List;

/**
 * Rapor Controller - Raporlama işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/raporlar")
@RequiredArgsConstructor
@Tag(name = "Raporlama", description = "Dashboard, istatistik ve rapor işlemleri")
public class RaporController {

    private final RaporService raporService;

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard istatistikleri", description = "Ana sayfa dashboard istatistiklerini getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_GENEL')")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting dashboard stats - birlikId: {}", birlikId);
        DashboardStatsDTO stats = raporService.getDashboardStats(birlikId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/tahsilat/birlik-bazli")
    @Operation(summary = "Birlik bazlı tahsilat raporu", description = "Birlik bazında tahsilat oranları raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<ApiResponse<List<BirlikTahsilatRaporDTO>>> getBirlikTahsilatRaporu(
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Long donemId) {
        
        log.info("Getting birlik tahsilat raporu - yil: {}, donemId: {}", yil, donemId);
        List<BirlikTahsilatRaporDTO> rapor = raporService.getBirlikTahsilatRaporu(yil, donemId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/tahsilat/donem-bazli")
    @Operation(summary = "Dönem bazlı aidat raporu", description = "Dönem bazında aidat tahsilat raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<ApiResponse<List<DonemAidatRaporDTO>>> getDonemAidatRaporu(
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting donem aidat raporu - yil: {}, birlikId: {}", yil, birlikId);
        List<DonemAidatRaporDTO> rapor = raporService.getDonemAidatRaporu(yil, birlikId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/gelir-gider/ozet")
    @Operation(summary = "Gelir/Gider özet raporu", description = "Gelir ve gider özet raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<GelirGiderRaporDTO>> getGelirGiderOzet(
            @RequestParam Integer yil,
            @RequestParam(required = false) Integer ay,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting gelir/gider ozet - yil: {}, ay: {}, birlikId: {}", yil, ay, birlikId);
        GelirGiderRaporDTO rapor = raporService.getGelirGiderOzet(yil, ay, birlikId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/gelir-gider/trend")
    @Operation(summary = "Gelir/Gider trend analizi", description = "Aylık gelir/gider trend raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<List<GelirGiderRaporDTO>>> getGelirGiderTrend(
            @RequestParam Integer yil,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting gelir/gider trend - yil: {}, birlikId: {}", yil, birlikId);
        List<GelirGiderRaporDTO> rapor = raporService.getGelirGiderTrend(yil, birlikId);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/export/tahsilat-raporu")
    @Operation(summary = "Tahsilat raporu Excel", description = "Tahsilat raporunu Excel formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<byte[]> exportTahsilatRaporu(
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) Long donemId) {
        
        log.info("Exporting tahsilat raporu");
        byte[] excelData = raporService.exportTahsilatRaporu(yil, birlikId, donemId);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=tahsilat_raporu.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }

    @GetMapping("/export/gelir-gider-raporu")
    @Operation(summary = "Gelir/Gider raporu Excel", description = "Gelir/gider raporunu Excel formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<byte[]> exportGelirGiderRaporu(
            @RequestParam Integer yil,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Exporting gelir/gider raporu");
        byte[] excelData = raporService.exportGelirGiderRaporu(yil, birlikId);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=gelir_gider_raporu.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }

    @GetMapping("/uye/dagilim")
    @Operation(summary = "Üye dağılım raporu", description = "Üye dağılım istatistikleri (il, üeyelik tipi bazında)")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_UYE')")
    public ResponseEntity<ApiResponse<Object>> getUyeDagilim(
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting uye dagilim - birlikId: {}", birlikId);
        Object dagilim = raporService.getUyeDagilim(birlikId);
        return ResponseEntity.ok(ApiResponse.success(dagilim));
    }

    @GetMapping("/birlik-istatistikleri")
    @Operation(summary = "Birlik istatistikleri", description = "Tüm birliklerin istatistik bilgileri")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_GENEL')")
    public ResponseEntity<ApiResponse<List<BirlikIstatistikDTO>>> getBirlikIstatistikleri() {
        log.info("Getting birlik istatistikleri");
        List<BirlikIstatistikDTO> istatistikler = raporService.getBirlikIstatistikleri();
        return ResponseEntity.ok(ApiResponse.success(istatistikler));
    }

    @GetMapping("/aidat-raporu")
    @Operation(summary = "Aidat raporu", description = "Birlik ve yıl bazında aidat raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<ApiResponse<List<AidatRaporDTO>>> getAidatRaporu(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) Integer yil) {
        
        log.info("Getting aidat raporu - birlikId: {}, yil: {}", birlikId, yil);
        List<AidatRaporDTO> rapor = raporService.getAidatRaporu(birlikId, yil);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/aidat-raporu/excel")
    @Operation(summary = "Aidat raporu Excel", description = "Aidat raporunu Excel formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<byte[]> exportAidatRaporuExcel(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) Integer yil) {
        
        log.info("Exporting aidat raporu excel");
        byte[] excelData = raporService.exportAidatRaporuExcel(birlikId, yil);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=aidat_raporu.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }

    @GetMapping("/tahsilat-trendi")
    @Operation(summary = "Tahsilat trendi", description = "Aylık veya dönemlik tahsilat trendi")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<ApiResponse<List<TahsilatTrendiDTO>>> getTahsilatTrendi(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(defaultValue = "true") Boolean aylik) {
        
        log.info("Getting tahsilat trendi - birlikId: {}, aylik: {}", birlikId, aylik);
        List<TahsilatTrendiDTO> trend = raporService.getTahsilatTrendi(birlikId, aylik);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }

    @GetMapping("/uye-distribusyonu")
    @Operation(summary = "Üye dağılımı", description = "Il, durum ve tip bazında üye dağılımı")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_UYE')")
    public ResponseEntity<ApiResponse<UyeDistribusyonDTO>> getUyeDistribusyonu() {
        log.info("Getting uye distribusyonu");
        UyeDistribusyonDTO distribusyon = raporService.getUyeDistribusyonu();
        return ResponseEntity.ok(ApiResponse.success(distribusyon));
    }

    @GetMapping("/gelir-gider")
    @Operation(summary = "Gelir/Gider raporu", description = "Gelir gider detay raporu")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_MALI')")
    public ResponseEntity<ApiResponse<GelirGiderDetayDTO>> getGelirGider(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) String baslangicTarihi,
            @RequestParam(required = false) String bitisTarihi) {
        
        log.info("Getting gelir/gider - birlikId: {}", birlikId);
        GelirGiderDetayDTO rapor = raporService.getGelirGiderDetay(birlikId, baslangicTarihi, bitisTarihi);
        return ResponseEntity.ok(ApiResponse.success(rapor));
    }

    @GetMapping("/dashboard/pdf")
    @Operation(summary = "Dashboard PDF", description = "Dashboard raporunu PDF formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_GENEL')")
    public ResponseEntity<byte[]> exportDashboardPdf() {
        log.info("Exporting dashboard pdf");
        byte[] pdfData = raporService.exportDashboardPdf();
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=dashboard_raporu.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfData);
    }
}
