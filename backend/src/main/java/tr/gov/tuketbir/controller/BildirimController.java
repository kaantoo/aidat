package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.dto.bildirim.BildirimDTO;
import tr.gov.tuketbir.dto.bildirim.BildirimOzetDTO;
import tr.gov.tuketbir.service.BildirimService;

import java.util.Map;

/**
 * Bildirim Controller
 * Bildirim API endpoints
 */
@RestController
@RequestMapping("/api/v1/bildirimler")
@RequiredArgsConstructor
@Tag(name = "Bildirimler", description = "Bildirim yönetimi API'leri")
public class BildirimController {

    private final BildirimService bildirimService;

    /**
     * Kullanıcının bildirimlerini getir
     */
    @GetMapping
    @Operation(summary = "Kullanıcı bildirimlerini listele")
    public ResponseEntity<Page<BildirimDTO>> getBildirimler(
            @AuthenticationPrincipal Kullanici kullanici,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Long kullaniciId = kullanici.getId();
        Long birlikId = kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null;
        
        Page<BildirimDTO> bildirimler;
        if (kullaniciId != null) {
            bildirimler = bildirimService.getKullaniciBildirimleri(kullaniciId, PageRequest.of(page, size));
        } else {
            bildirimler = bildirimService.getBirlikBildirimleri(birlikId, PageRequest.of(page, size));
        }
        
        return ResponseEntity.ok(bildirimler);
    }

    /**
     * Birlik bildirimlerini getir
     */
    @GetMapping("/birlik/{birlikId}")
    @Operation(summary = "Birlik bildirimlerini listele")
    public ResponseEntity<Page<BildirimDTO>> getBirlikBildirimleri(
            @PathVariable Long birlikId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Page<BildirimDTO> bildirimler = bildirimService.getBirlikBildirimleri(birlikId, PageRequest.of(page, size));
        return ResponseEntity.ok(bildirimler);
    }

    /**
     * Bildirim özetini getir (header için)
     */
    @GetMapping("/ozet")
    @Operation(summary = "Bildirim özeti - okunmamış sayısı ve son bildirimler")
    public ResponseEntity<BildirimOzetDTO> getOzet(
            @AuthenticationPrincipal Kullanici kullanici) {
        
        Long kullaniciId = kullanici.getId();
        Long birlikId = kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null;
        
        BildirimOzetDTO ozet = bildirimService.getBildirimOzeti(kullaniciId, birlikId);
        return ResponseEntity.ok(ozet);
    }

    /**
     * Okunmamış bildirim sayısı
     */
    @GetMapping("/okunmamis-sayisi")
    @Operation(summary = "Okunmamış bildirim sayısını getir")
    public ResponseEntity<Map<String, Long>> getOkunmamisSayisi(
            @AuthenticationPrincipal Kullanici kullanici) {
        
        Long kullaniciId = kullanici.getId();
        Long birlikId = kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null;
        
        Long sayisi = bildirimService.getOkunmamisSayisi(kullaniciId, birlikId);
        return ResponseEntity.ok(Map.of("okunmamisSayisi", sayisi));
    }

    /**
     * Bildirimi okundu işaretle
     */
    @PutMapping("/{id}/okundu")
    @Operation(summary = "Bildirimi okundu olarak işaretle")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        bildirimService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Tüm bildirimleri okundu işaretle
     */
    @PutMapping("/tumunu-okundu-isaretle")
    @Operation(summary = "Tüm bildirimleri okundu olarak işaretle")
    public ResponseEntity<Map<String, Integer>> markAllAsRead(
            @AuthenticationPrincipal Kullanici kullanici) {
        
        Long kullaniciId = kullanici.getId();
        Long birlikId = kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null;
        
        int guncellenenSayisi = bildirimService.markAllAsRead(kullaniciId, birlikId);
        return ResponseEntity.ok(Map.of("guncellenenSayisi", guncellenenSayisi));
    }

    /**
     * Bildirim sil
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Bildirim sil")
    public ResponseEntity<Void> deleteBildirim(@PathVariable Long id) {
        bildirimService.deleteBildirim(id);
        return ResponseEntity.noContent().build();
    }
}
