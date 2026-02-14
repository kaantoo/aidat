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
public class AidatRaporDTO {
    private Long birlikId;
    private String birlikAdi;
    private Integer yil;
    private String donem;
    private Long toplamUye;
    private Long odeyenUye;
    private BigDecimal tahakkukTutari;
    private BigDecimal tahsilatTutari;
    private BigDecimal tahsilatOrani;
    private BigDecimal bekleyenTutar;
}
