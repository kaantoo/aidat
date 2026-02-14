package tr.gov.tuketbir.dto.aidat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Aidat Tahakkuk Request DTO - Toplu aidat tanımlama
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AidatTahakkukRequest {

    @NotNull(message = "Aidat dönemi ID zorunludur")
    private Long aidatDonemiId;

    // Belirli üyelere tahakkuk için
    private List<Long> uyeIds;
    
    // Birlik bazında toplu tahakkuk için
    private Long birlikId;
    
    // Tüm aktif üyelere tahakkuk için
    private Boolean tumUyeler;

    // Özel tutar belirlemek için (null ise dönem tutarı kullanılır)
    @Positive(message = "Özel tutar pozitif olmalıdır")
    private BigDecimal ozelTutar;
}
