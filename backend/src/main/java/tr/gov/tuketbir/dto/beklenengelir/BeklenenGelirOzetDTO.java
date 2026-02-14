package tr.gov.tuketbir.dto.beklenengelir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Beklenen Gelir Özet DTO
 * 
 * Dashboard ve raporlama için kullanılır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeklenenGelirOzetDTO {

    /**
     * Dönem bilgisi
     */
    private Long donemId;
    private String donemAdi;
    private Integer donemYili;

    /**
     * Birlik bilgisi (null ise merkez birlik için özet)
     */
    private Long birlikId;
    private String birlikAdi;
    private String birlikTipi;

    /**
     * Üye istatistikleri
     */
    private Integer toplamUyeSayisi;
    private Integer aktifUyeSayisi;

    /**
     * Gelir istatistikleri
     */
    private BigDecimal birimAidatTutari;
    private BigDecimal beklenenGelir;
    private BigDecimal tahsilEdilen;
    private BigDecimal kalanTutar;
    private BigDecimal tahsilatOrani;

    /**
     * Merkez pay bilgileri (alt birlik için)
     */
    private BigDecimal merkezPayOrani;
    private BigDecimal merkezeBeklenenPay;

    /**
     * Dönem bazlı kırılım (yıllık özet için)
     */
    private List<DonemBazliBeklenenGelirDTO> donemKirilimi;
}
