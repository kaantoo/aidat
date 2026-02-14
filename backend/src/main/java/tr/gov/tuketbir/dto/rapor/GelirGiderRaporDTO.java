package tr.gov.tuketbir.dto.rapor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.math.BigDecimal;
import java.util.List;

/**
 * Gelir/Gider Raporu DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GelirGiderRaporDTO {

    private Integer yil;
    private Integer ay;
    private BigDecimal toplamGelir;
    private BigDecimal toplamGider;
    private BigDecimal netDurum;
    
    private List<KategoriOzet> gelirKategorileri;
    private List<KategoriOzet> giderKategorileri;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KategoriOzet {
        private String kategoriKodu;
        private String kategoriAdi;
        private BigDecimal tutar;
        private BigDecimal oran; // Toplam içindeki yüzdesi
        private Integer islemSayisi;
    }
}
