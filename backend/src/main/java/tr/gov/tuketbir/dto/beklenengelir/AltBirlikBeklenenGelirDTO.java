package tr.gov.tuketbir.dto.beklenengelir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Alt Birlik Beklenen Gelir DTO
 * 
 * Alt birliğin bir dönem için beklenen gelirini temsil eder.
 * Beklenen Gelir = Aktif Üye Sayısı × Aidat Tutarı
 * 
 * Önemli: Bu hesaplama tahsilat bağımsızdır.
 * Üyenin ödeme yapıp yapmaması beklenen geliri etkilemez.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AltBirlikBeklenenGelirDTO {

    /**
     * Birlik ID
     */
    private Long birlikId;

    /**
     * Birlik kodu
     */
    private String birlikKodu;

    /**
     * Birlik adı
     */
    private String birlikAdi;

    /**
     * Aidat dönemi ID
     */
    private Long donemId;

    /**
     * Dönem adı
     */
    private String donemAdi;

    /**
     * Dönem yılı
     */
    private Integer donemYili;

    /**
     * Birim aidat tutarı (dönemde tanımlı)
     */
    private BigDecimal aidatTutari;

    /**
     * Aktif üye sayısı
     */
    private Integer aktifUyeSayisi;

    /**
     * Beklenen gelir = aktifUyeSayisi × aidatTutari
     */
    private BigDecimal beklenenGelir;

    /**
     * Tahsil edilen tutar (ödenen aidatlar toplamı)
     */
    private BigDecimal tahsilEdilen;

    /**
     * Kalan tutar = beklenenGelir - tahsilEdilen
     */
    private BigDecimal kalanTutar;

    /**
     * Tahsilat oranı (%) = (tahsilEdilen / beklenenGelir) × 100
     */
    private BigDecimal tahsilatOrani;

    /**
     * Merkeze aktarılacak pay oranı (%)
     */
    private BigDecimal merkezPayOrani;

    /**
     * Merkeze aktarılacak beklenen tutar = beklenenGelir × (merkezPayOrani / 100)
     */
    private BigDecimal merkezeBeklenenPay;

    /**
     * Merkeze aktarılan tutar (gerçekleşen)
     */
    private BigDecimal merekzeAktarilanPay;
}
