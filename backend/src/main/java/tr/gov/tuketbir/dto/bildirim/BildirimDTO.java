package tr.gov.tuketbir.dto.bildirim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.BildirimOnceligi;
import tr.gov.tuketbir.domain.enums.BildirimTipi;

import java.time.LocalDateTime;

/**
 * Bildirim DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BildirimDTO {
    private Long id;
    private String baslik;
    private String mesaj;
    private BildirimTipi tip;
    private BildirimOnceligi oncelik;
    private Boolean okundu;
    private LocalDateTime okunmaTarihi;
    private String link;
    private String entityTipi;
    private Long entityId;
    private LocalDateTime createdAt;
    private String zamanOnce; // "5 dk önce", "1 saat önce" gibi
}
