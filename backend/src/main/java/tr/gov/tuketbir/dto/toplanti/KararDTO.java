package tr.gov.tuketbir.dto.toplanti;

import lombok.*;
import tr.gov.tuketbir.domain.enums.KararDurumu;

/**
 * Karar DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KararDTO {
    private Long id;
    private String kararNo;
    private Long toplantiId;
    private String toplantiNo;
    private Integer kararSirasi;
    private String baslik;
    private String kararMetni;
    private KararDurumu durum;
    private Boolean oyBirligi;
    private Integer kabulOyu;
    private Integer redOyu;
    private Integer cekimserOyu;
    private String sorumlu;
    private String notlar;
    private String createdAt;
}
