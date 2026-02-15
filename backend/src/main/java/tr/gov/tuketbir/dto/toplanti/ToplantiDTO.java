package tr.gov.tuketbir.dto.toplanti;

import lombok.*;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Toplantı DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToplantiDTO {
    private Long id;
    private String toplantiNo;
    private Long birlikId;
    private String birlikAdi;
    private String baslik;
    private ToplantiTuru toplantiTuru;
    private ToplantiDurumu durum;
    private LocalDate toplantiTarihi;
    private LocalTime baslangicSaati;
    private LocalTime bitisSaati;
    private String yer;
    private String gundem;
    private String aciklama;
    private int kararSayisi;
    private int katilimciSayisi;
    private List<KararDTO> kararlar;
    private List<KatilimciDTO> katilimcilar;
    private String createdAt;
    private String updatedAt;
}
