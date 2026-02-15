package tr.gov.tuketbir.dto.toplanti;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Toplantı Oluşturma Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToplantiCreateRequest {

    @NotNull(message = "Birlik ID zorunludur")
    private Long birlikId;

    @NotBlank(message = "Başlık zorunludur")
    private String baslik;

    @NotNull(message = "Toplantı türü zorunludur")
    private ToplantiTuru toplantiTuru;

    @NotNull(message = "Toplantı tarihi zorunludur")
    private LocalDate toplantiTarihi;

    private LocalTime baslangicSaati;
    private LocalTime bitisSaati;
    private String yer;
    private String gundem;
    private String aciklama;

    private List<KararCreateRequest> kararlar;
    private List<KatilimciCreateRequest> katilimcilar;
}
