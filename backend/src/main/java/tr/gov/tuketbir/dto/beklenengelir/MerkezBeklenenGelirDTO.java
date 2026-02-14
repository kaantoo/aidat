package tr.gov.tuketbir.dto.beklenengelir;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Merkez Birlik Beklenen Gelir DTO
 * 
 * Merkez birliğin tüm alt birliklerden beklediği geliri temsil eder.
 * 
 * Formül:
 * Merkez Beklenen Gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)
 * 
 * Önemli: Bu hesaplama tahsilat bağımsızdır.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerkezBeklenenGelirDTO {

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
     * Merkez birlik pay oranı (%) - Dönemde tanımlı
     */
    private BigDecimal merkezPayOrani;

    /**
     * Toplam alt birlik sayısı
     */
    private Integer toplamAltBirlikSayisi;

    /**
     * Toplam aktif üye sayısı (tüm alt birliklerin toplamı)
     */
    private Integer toplamAktifUyeSayisi;

    /**
     * Alt birliklerin toplam beklenen geliri
     */
    private BigDecimal altBirliklerToplamBeklenenGelir;

    /**
     * Merkez beklenen gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)
     */
    private BigDecimal merkezBeklenenGelir;

    /**
     * Merkeze aktarılan toplam tutar (gerçekleşen)
     */
    private BigDecimal merkezTahsilEdilen;

    /**
     * Kalan tutar = merkezBeklenenGelir - merkezTahsilEdilen
     */
    private BigDecimal merkezKalanTutar;

    /**
     * Tahsilat oranı (%) = (merkezTahsilEdilen / merkezBeklenenGelir) × 100
     */
    private BigDecimal tahsilatOrani;

    /**
     * Alt birlik bazlı detaylar
     */
    private List<AltBirlikBeklenenGelirDTO> altBirlikDetaylari;
}
