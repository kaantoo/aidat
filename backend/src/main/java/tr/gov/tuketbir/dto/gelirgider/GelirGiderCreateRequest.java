package tr.gov.tuketbir.dto.gelirgider;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Gelir/Gider Create Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GelirGiderCreateRequest {

    @NotNull(message = "İşlem tipi zorunludur")
    private GelirGiderTipi tip;

    // Gelir ise bu alan dolu olmalı
    private GelirKategorisi gelirKategorisi;

    // Gider ise bu alan dolu olmalı
    private GiderKategorisi giderKategorisi;

    @NotNull(message = "Tutar zorunludur")
    @Positive(message = "Tutar pozitif olmalıdır")
    private BigDecimal tutar;

    private LocalDate islemTarihi; // Null ise bugünün tarihi

    @Size(max = 50, message = "Belge no en fazla 50 karakter olabilir")
    private String belgeNo;

    @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
    private String aciklama;

    private Long birlikId; // Null ise merkez birlik için
}
