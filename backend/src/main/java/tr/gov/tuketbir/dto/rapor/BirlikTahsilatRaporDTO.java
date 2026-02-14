package tr.gov.tuketbir.dto.rapor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Birlik Bazlı Tahsilat Raporu DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BirlikTahsilatRaporDTO {

    private Long birlikId;
    private String birlikKodu;
    private String birlikAdi;
    private String ilAdi;
    private Integer uyeSayisi;
    private Integer toplamAidatSayisi;
    private Integer odenmisAidatSayisi;
    private Integer bekleyenAidatSayisi;
    private Integer gecikmisnAidatSayisi;
    private BigDecimal toplamTahakkuk;
    private BigDecimal toplamTahsilat;
    private BigDecimal toplamBakiye;
    private BigDecimal tahsilatOrani;
    
    // Alt birlik detayları (varsa)
    private List<BirlikTahsilatRaporDTO> altBirlikler;
}
