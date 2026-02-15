package tr.gov.tuketbir.dto.toplanti;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import tr.gov.tuketbir.domain.enums.KararDurumu;

/**
 * Karar Oluşturma Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KararCreateRequest {

    @NotBlank(message = "Karar başlığı zorunludur")
    private String baslik;

    @NotBlank(message = "Karar metni zorunludur")
    private String kararMetni;

    private KararDurumu durum;
    private Integer kararSirasi;
    private Boolean oyBirligi;
    private Integer kabulOyu;
    private Integer redOyu;
    private Integer cekimserOyu;
    private String sorumlu;
    private String notlar;
}
