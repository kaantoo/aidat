package tr.gov.tuketbir.dto.rapor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Birlik İstatistik DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirlikIstatistikDTO {
    private Long birlikId;
    private String birlikAdi;
    private Integer uyeSayisi;
    private Integer aktifUyeSayisi;
    private BigDecimal bekleyenAidatTutari;
    private BigDecimal tahsilOrani;
    private LocalDateTime sonTahsilatTarihi;
}
