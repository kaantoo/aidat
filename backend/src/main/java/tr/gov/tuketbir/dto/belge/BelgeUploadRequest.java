package tr.gov.tuketbir.dto.belge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.BelgeTipi;

/**
 * Belge Upload Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BelgeUploadRequest {

    @NotNull(message = "Belge tipi zorunludur")
    private BelgeTipi belgeTipi;

    @Size(max = 50, message = "Belge no en fazla 50 karakter olabilir")
    private String belgeNo;

    @NotBlank(message = "Dosya adı zorunludur")
    @Size(max = 255, message = "Dosya adı en fazla 255 karakter olabilir")
    private String dosyaAdi;

    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String aciklama;

    private Long uyeId;
    
    private Long birlikId;
}
