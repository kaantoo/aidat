package tr.gov.tuketbir.dto.rapor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dönem Bazlı Aidat Raporu DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonemAidatRaporDTO {

    private Long donemId;
    private String donemAdi;
    private Integer yil;
    private Integer ay;
    private BigDecimal aidatTutari;
    private Integer toplamUyeSayisi;
    private Integer toplamAidatSayisi;
    private Integer odenmisAidatSayisi;
    private Integer kısmiOdenmisAidatSayisi;
    private Integer odenmemisAidatSayisi;
    private Integer gecikmisnAidatSayisi;
    private BigDecimal toplamTahakkuk;
    private BigDecimal toplamTahsilat;
    private BigDecimal toplamBakiye;
    private BigDecimal tahsilatOrani;
}
