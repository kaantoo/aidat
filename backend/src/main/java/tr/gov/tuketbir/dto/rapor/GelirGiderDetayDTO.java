package tr.gov.tuketbir.dto.rapor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GelirGiderDetayDTO {
    private BigDecimal toplamGelir;
    private BigDecimal toplamGider;
    private BigDecimal netDurum;
    private List<AylikTrendDTO> aylikTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AylikTrendDTO {
        private String ay;
        private BigDecimal gelir;
        private BigDecimal gider;
    }
}
