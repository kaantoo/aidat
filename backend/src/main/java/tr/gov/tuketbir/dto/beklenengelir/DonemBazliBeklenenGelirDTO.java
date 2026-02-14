package tr.gov.tuketbir.dto.beklenengelir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dönem Bazlı Beklenen Gelir DTO
 * 
 * Yıllık raporlama için dönem kırılımı.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonemBazliBeklenenGelirDTO {

    /**
     * Dönem bilgisi
     */
    private Long donemId;
    private String donemAdi;
    private String donemKodu;
    private Integer yil;

    /**
     * Beklenen ve tahsil edilen tutarlar
     */
    private BigDecimal beklenenGelir;
    private BigDecimal tahsilEdilen;
    private BigDecimal kalanTutar;
    private BigDecimal tahsilatOrani;

    /**
     * Üye sayısı
     */
    private Integer aktifUyeSayisi;

    /**
     * Birim aidat tutarı
     */
    private BigDecimal aidatTutari;
}
