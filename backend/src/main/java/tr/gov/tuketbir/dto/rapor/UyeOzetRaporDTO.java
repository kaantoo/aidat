package tr.gov.tuketbir.dto.rapor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Üye Özet Rapor DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UyeOzetRaporDTO {
    private Long toplamUyeSayisi;
    private Long aktifUyeSayisi;
    private Long pasifUyeSayisi;
    private Long yeniUyeSayisi;
    private Long ilBazindaUyeSayisi;
    private Long ilceBazindaUyeSayisi;
}
