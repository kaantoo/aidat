package tr.gov.tuketbir.dto.toplanti;

import lombok.*;

/**
 * Katılımcı Oluşturma Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KatilimciCreateRequest {
    private Long uyeId;
    private String adSoyad;
    private String gorev;
    private Boolean katildi;
    private String mazeret;
}
