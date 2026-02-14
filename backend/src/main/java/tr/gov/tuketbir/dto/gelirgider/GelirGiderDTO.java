package tr.gov.tuketbir.dto.gelirgider;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Gelir/Gider Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GelirGiderDTO {

    private Long id;
    private GelirGiderTipi tipi;
    private GelirKategorisi gelirKategorisi;
    private GiderKategorisi giderKategorisi;
    private String kategoriAdi;
    private BigDecimal tutar;
    private LocalDate islemTarihi;
    private String belgeNo;
    private String aciklama;
    private Long birlikId;
    private String birlikAdi;
    private Long kaydedenkKullaniciId;
    private String kaydedenkKullaniciAdi;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
