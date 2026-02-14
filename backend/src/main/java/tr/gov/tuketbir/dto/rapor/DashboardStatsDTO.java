package tr.gov.tuketbir.dto.rapor;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Dashboard İstatistikleri DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsDTO {

    // Üye İstatistikleri
    private Long toplamUyeSayisi;
    private Long aktifUyeSayisi;
    private Long pasifUyeSayisi;
    private Long yeniUyeSayisi; // Bu ay katılan

    // Birlik İstatistikleri
    private Long toplamBirlikSayisi;
    private Long ilBirligiSayisi;
    private Long ilceBirligiSayisi;

    // Aidat İstatistikleri
    private Long toplamAidatSayisi;
    private Long odenmisAidatSayisi;
    private Long bekleyenAidatSayisi;
    private Long gecikmisnAidatSayisi;
    private BigDecimal toplamTahakkuk;
    private BigDecimal toplamTahsilat;
    private BigDecimal toplamBakiye;
    private BigDecimal tahsilatOrani; // Yüzde

    // Gelir/Gider İstatistikleri
    private BigDecimal aylikGelir;
    private BigDecimal aylikGider;
    private BigDecimal aylikNet;
    private BigDecimal yillikGelir;
    private BigDecimal yillikGider;
    private BigDecimal yillikNet;
}
