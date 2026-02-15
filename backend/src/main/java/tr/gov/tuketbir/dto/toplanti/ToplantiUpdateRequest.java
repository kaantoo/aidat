package tr.gov.tuketbir.dto.toplanti;

import lombok.*;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Toplantı Güncelleme Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToplantiUpdateRequest {
    private String baslik;
    private ToplantiTuru toplantiTuru;
    private ToplantiDurumu durum;
    private LocalDate toplantiTarihi;
    private LocalTime baslangicSaati;
    private LocalTime bitisSaati;
    private String yer;
    private String gundem;
    private String aciklama;
}
