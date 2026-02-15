package tr.gov.tuketbir.dto.sistem;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Yedekleme Durumu DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YedekDurumDTO {
    private boolean otomatikYedekAktif;
    private String yedekDizini;
    private LocalDateTime sonYedekTarihi;
    private String sonYedekDosya;
    private int toplamYedekSayisi;
    private String toplamBoyut;
    private List<YedekBilgiDTO> sonYedekler;
}
