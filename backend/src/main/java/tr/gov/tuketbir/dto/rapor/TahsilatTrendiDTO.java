package tr.gov.tuketbir.dto.rapor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TahsilatTrendiDTO {
    private String donem;
    private BigDecimal tutar;
}
