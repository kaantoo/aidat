package tr.gov.tuketbir.dto.toplanti;

import lombok.*;

/**
 * Katılımcı DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KatilimciDTO {
    private Long id;
    private Long toplantiId;
    private Long uyeId;
    private String uyeNo;
    private String adSoyad;
    private String gorev;
    private Boolean katildi;
    private String mazeret;
    private Boolean imzaladi;
}
