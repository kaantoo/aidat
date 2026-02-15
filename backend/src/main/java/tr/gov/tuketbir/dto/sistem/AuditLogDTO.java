package tr.gov.tuketbir.dto.sistem;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Audit Log DTO - İşlem kayıtlarını frontend'e göstermek için
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String kullaniciAdi;
    private Long kullaniciId;
    private String islemTipi;
    private String entityTipi;
    private Long entityId;
    private Long birlikId;
    private String aciklama;
    private String ipAdresi;
    private String userAgent;
    private String requestUrl;
    private String httpMetod;
    private boolean basarili;
    private String hataMesaji;
    private LocalDateTime islemZamani;
}
