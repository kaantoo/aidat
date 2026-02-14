package tr.gov.tuketbir.dto.aidat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.AidatDurum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Aidat Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AidatDTO {

    private Long id;
    private Long uyeId;
    private String uyeNo;
    private String uyeAdSoyad;
    private Long aidatDonemiId;
    private String donemAdi;
    private Integer yil;
    private Long birlikId;
    private String birlikAdi;
    private BigDecimal tahakkukTutari;
    private BigDecimal odenenTutar;
    private BigDecimal kalanTutar;
    private BigDecimal gecikmeTutari;
    private BigDecimal toplamBorc;
    private AidatDurum durumu;
    private LocalDate sonOdemeTarihi;
    private LocalDate vadeTarihi;
    private Integer gecikmeGunSayisi;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Tahsilat listesi
    private List<TahsilatDTO> tahsilatlar;
}
