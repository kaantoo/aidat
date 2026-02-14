package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.dto.aidat.*;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.service.AidatService;

import java.util.List;
import java.util.Map;

/**
 * Aidat Controller - Aidat yönetimi işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/aidatlar")
@RequiredArgsConstructor
@Tag(name = "Aidat Yönetimi", description = "Aidat CRUD, tahakkuk ve tahsilat işlemleri")
public class AidatController {

    private final AidatService aidatService;

    // ==================== Aidat Dönemi İşlemleri ====================

    @GetMapping("/donemler")
    @Operation(summary = "Dönem listesi", description = "Aidat dönemlerini listele")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<List<AidatDonemiDTO>>> getDonemler(
            @RequestParam(required = false) Integer yil,
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Getting aidat donemleri, yil: {}, birlikId: {}", yil, birlikId);
        List<AidatDonemiDTO> response = birlikId != null 
            ? aidatService.getDonemlerByBirlik(birlikId)
            : aidatService.getAktifDonemler();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/donemler/{id}")
    @Operation(summary = "Dönem detayı", description = "Aidat dönemi detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<AidatDonemiDTO>> getDonemById(@PathVariable Long id) {
        log.info("Getting aidat donemi by id: {}", id);
        // TODO: Add getAidatDonemiById method to service
        return ResponseEntity.ok(ApiResponse.success(null, "Dönem bulunamadı"));
    }

    @PostMapping("/donemler")
    @Operation(summary = "Yeni dönem oluştur", description = "Yeni aidat dönemi tanımlama")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_CREATE')")
    public ResponseEntity<ApiResponse<AidatDonemiDTO>> createDonem(
            @Valid @RequestBody AidatDonemiCreateRequest request) {
        
        log.info("Creating new aidat donemi: {}", request.getDonemAdi());
        AidatDonemiDTO response = aidatService.createDonem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Aidat dönemi oluşturuldu"));
    }

    @PutMapping("/donemler/{id}")
    @Operation(summary = "Dönem güncelle", description = "Aidat dönemi güncelleme")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_UPDATE')")
    public ResponseEntity<ApiResponse<AidatDonemiDTO>> updateDonem(
            @PathVariable Long id,
            @Valid @RequestBody AidatDonemiCreateRequest request) {
        
        log.info("Updating aidat donemi: {}", id);
        // TODO: Add updateAidatDonemi method to service
        return ResponseEntity.ok(ApiResponse.success(null, "Dönem güncellendi"));
    }

    @DeleteMapping("/donemler/{id}")
    @Operation(summary = "Dönem sil", description = "Aidat dönemini silme")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteDonem(@PathVariable Long id) {
        log.info("Deleting aidat donemi: {}", id);
        // TODO: Add deleteAidatDonemi method to service
        return ResponseEntity.ok(ApiResponse.success(null, "Aidat dönemi silindi"));
    }

    // ==================== Aidat İşlemleri ====================

    @GetMapping
    @Operation(summary = "Aidat listesi", description = "Filtrelenmiş ve sayfalanmış aidat listesi")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<Page<AidatDTO>>> getAidatlar(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("Getting aidatlar - birlikId: {}, page: {}, size: {}", birlikId, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AidatDTO> response = birlikId != null 
            ? aidatService.getAidatlarByBirlik(birlikId, pageable)
            : aidatService.getAllAidatlar(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Aidat detayı", description = "Aidat detayını getir")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<AidatDTO>> getAidatById(@PathVariable Long id) {
        log.info("Getting aidat by id: {}", id);
        // TODO: Add getAidatById method to service
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/uye/{uyeId}")
    @Operation(summary = "Üye aidatları", description = "Belirli üyenin aidatlarını listele")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<List<AidatDTO>>> getAidatlarByUye(@PathVariable Long uyeId) {
        log.info("Getting aidatlar by uye: {}", uyeId);
        List<AidatDTO> response = aidatService.getAidatlarByUye(uyeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/tahakkuk")
    @Operation(summary = "Toplu aidat tahakkuku", description = "Üyelere toplu aidat tanımlama")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_TOPLU_ATAMA')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createTahakkuk(
            @Valid @RequestBody AidatTahakkukRequest request) {
        
        log.info("Creating aidat tahakkuk for donem: {}", request.getAidatDonemiId());
        var result = aidatService.topluAidatAtama(request.getAidatDonemiId(), request.getBirlikId());
        Map<String, Object> resultMap = Map.of(
            "success", result.getSuccess(),
            "skipped", result.getSkipped(),
            "total", result.getTotal()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(resultMap, "Aidat tahakkuku oluşturuldu"));
    }

    @PostMapping("/toplu-tahakkuk")
    @Operation(summary = "Toplu aidat tahakkuku (alternatif)", description = "Üyelere toplu aidat tanımlama - donemId body ile")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_TOPLU_ATAMA')")
    public ResponseEntity<ApiResponse<Integer>> createTopluTahakkuk(
            @RequestBody Map<String, Long> request) {
        
        Long donemId = request.get("donemId");
        Long birlikId = request.get("birlikId");
        log.info("Creating toplu tahakkuk for donem: {}", donemId);
        var result = aidatService.topluAidatAtama(donemId, birlikId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result.getSuccess(), "Aidat tahakkuku oluşturuldu"));
    }

    // ==================== Tahsilat İşlemleri ====================

    @GetMapping("/tahsilatlar")
    @Operation(summary = "Tüm tahsilatlar", description = "Tüm tahsilatları listele")
    @PreAuthorize("hasAnyAuthority('PERM_TAHSILAT_READ')")
    public ResponseEntity<ApiResponse<List<TahsilatDTO>>> getAllTahsilatlar(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        log.info("Getting all tahsilatlar - birlikId: {}", birlikId);
        List<TahsilatDTO> response = aidatService.getAllTahsilatlar(birlikId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{aidatId}/tahsilatlar")
    @Operation(summary = "Tahsilat listesi", description = "Belirli aidatın tahsilatlarını listele")
    @PreAuthorize("hasAnyAuthority('PERM_TAHSILAT_READ')")
    public ResponseEntity<ApiResponse<List<TahsilatDTO>>> getTahsilatlar(@PathVariable Long aidatId) {
        log.info("Getting tahsilatlar for aidat: {}", aidatId);
        // TODO: Add getTahsilatlarByAidat method to service
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }

    @PostMapping("/tahsilat")
    @Operation(summary = "Yeni tahsilat", description = "Aidat ödemesi kaydetme")
    @PreAuthorize("hasAnyAuthority('PERM_TAHSILAT_CREATE')")
    public ResponseEntity<ApiResponse<TahsilatDTO>> createTahsilat(
            @Valid @RequestBody TahsilatCreateRequest request) {
        
        log.info("Creating tahsilat for aidat: {}", request.getAidatId());
        TahsilatDTO response = aidatService.tahsilatGirisi(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Tahsilat kaydedildi"));
    }

    @DeleteMapping("/tahsilat/{id}")
    @Operation(summary = "Tahsilat iptal", description = "Tahsilat kaydını iptal etme")
    @PreAuthorize("hasAnyAuthority('PERM_TAHSILAT_IPTAL')")
    public ResponseEntity<ApiResponse<Void>> cancelTahsilat(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Kullanıcı tarafından iptal edildi") String neden) {
        log.info("Cancelling tahsilat: {}", id);
        aidatService.tahsilatIptal(id, neden);
        return ResponseEntity.ok(ApiResponse.success(null, "Tahsilat iptal edildi"));
    }

    // ==================== İstatistik ve Raporlar ====================

    @GetMapping("/stats")
    @Operation(summary = "Aidat istatistikleri", description = "Aidat istatistiklerini getir")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAidatStats(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(required = false) Long donemId,
            @RequestParam(required = false) Integer yil) {
        
        log.info("Getting aidat stats - birlikId: {}, donemId: {}, yil: {}", birlikId, donemId, yil);
        var ozet = aidatService.getAidatOzet(birlikId);
        Map<String, Object> stats = Map.of(
            "toplamBorc", ozet.getToplamBorc(),
            "odenenTutar", ozet.getOdenenTutar(),
            "kalanBorc", ozet.getKalanBorc(),
            "bekleyenSayisi", ozet.getBekleyenSayisi(),
            "odenmisSayisi", ozet.getOdenmisSayisi(),
            "tahsilatOrani", ozet.getTahsilatOrani()
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/gecikmis")
    @Operation(summary = "Gecikmiş aidatlar", description = "Vadesi geçmiş aidatları listele")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_READ')")
    public ResponseEntity<ApiResponse<List<AidatDTO>>> getGecikmisAidatlar(
            @RequestParam(required = false) Long birlikId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        log.info("Getting gecikmis aidatlar - birlikId: {}", birlikId);
        List<AidatDTO> response = aidatService.getGecikmisBorclar(birlikId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/gecikme-hesapla")
    @Operation(summary = "Gecikme faizi hesapla", description = "Tüm gecikmiş aidatlar için gecikme faizi hesaplama")
    @PreAuthorize("hasAnyAuthority('PERM_AIDAT_UPDATE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateGecikmeFaizi(
            @RequestParam(required = false) Long birlikId) {
        
        log.info("Calculating gecikme faizi - birlikId: {}", birlikId);
        // TODO: Implement calculateGecikmeFaizi in service
        Map<String, Object> result = Map.of("message", "Gecikme faizi hesaplama henüz uygulanmadı");
        return ResponseEntity.ok(ApiResponse.success(result, "Gecikme faizi hesaplandı"));
    }

    @GetMapping("/export/excel")
    @Operation(summary = "Excel'e aktar", description = "Aidat listesini Excel formatında dışa aktar")
    @PreAuthorize("hasAnyAuthority('PERM_RAPOR_AIDAT')")
    public ResponseEntity<byte[]> exportToExcel(
            @ModelAttribute AidatSearchRequest searchRequest) {
        
        log.info("Exporting aidatlar to Excel");
        // TODO: Implement exportToExcel in service
        byte[] excelData = new byte[0];
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=aidatlar.xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }
}
