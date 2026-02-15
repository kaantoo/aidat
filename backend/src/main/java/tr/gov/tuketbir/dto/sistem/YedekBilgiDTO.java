package tr.gov.tuketbir.dto.sistem;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Yedekleme Bilgisi DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YedekBilgiDTO {
    private String dosyaAdi;
    private Long dosyaBoyutu;
    private String dosyaBoyutuFormatli;
    private LocalDateTime olusturmaZamani;
    private String yedekTipi; // MANUEL / OTOMATIK
    private boolean basarili;
    private String aciklama;
}
