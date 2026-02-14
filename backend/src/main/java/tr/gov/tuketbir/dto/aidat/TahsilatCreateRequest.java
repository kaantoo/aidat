package tr.gov.tuketbir.dto.aidat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.OdemeTipi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tahsilat Create Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TahsilatCreateRequest {

    @NotNull(message = "Aidat ID zorunludur")
    private Long aidatId;

    @NotNull(message = "Ödeme tipi zorunludur")
    private OdemeTipi odemeTipi;

    @NotNull(message = "Tutar zorunludur")
    @Positive(message = "Tutar pozitif olmalıdır")
    private BigDecimal tutar;

    private LocalDate odemeTarihi; // Null ise bugünün tarihi kullanılır

    @Size(max = 50, message = "Makbuz no en fazla 50 karakter olabilir")
    private String makbuzNo;

    @Size(max = 50, message = "Dekont no en fazla 50 karakter olabilir")
    private String dekontNo;

    @Size(max = 50, message = "Banka dekont no en fazla 50 karakter olabilir")
    private String bankaDekontuNo;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String aciklama;
}
