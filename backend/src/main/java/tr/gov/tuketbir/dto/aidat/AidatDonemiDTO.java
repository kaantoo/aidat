package tr.gov.tuketbir.dto.aidat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.DonemTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Aidat Dönemi Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AidatDonemiDTO {

    private Long id;
    private String donemKodu;
    private String donemAdi;
    private DonemTipi donemTipi;
    private Integer yil;
    private Integer ay; // Aylık dönemler için
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private LocalDate sonOdemeTarihi;
    private BigDecimal tutar; // Alt birlikler için aidat tutarı
    private BigDecimal merkezPayOrani; // Merkez birlik için pay oranı (%)
    private BigDecimal asgariUcretTutari;
    private BigDecimal gecikmeFaiziOrani; // Yüzde olarak
    private String asgariUcretAciklama;
    private String aciklama;
    private Long birlikId;
    private String birlikAdi;
    private Boolean aktif;
    private LocalDateTime createdAt;
    
    // İstatistikler
    private Integer toplamAidatSayisi;
    private Integer odenmisAidatSayisi;
    private Integer bekleyenAidatSayisi;
    private BigDecimal toplamTahakkuk;
    private BigDecimal toplamTahsilat;
    private BigDecimal tahsilatOrani;
}
