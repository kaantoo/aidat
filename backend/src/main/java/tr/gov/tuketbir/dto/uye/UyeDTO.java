package tr.gov.tuketbir.dto.uye;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.Cinsiyet;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.domain.enums.UyelikTipi;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Üye Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UyeDTO {

    private Long id;
    private String uyeNo;
    private String tcKimlikNo;
    private String ad;
    private String soyad;
    private String adSoyad;
    private Cinsiyet cinsiyet;
    private LocalDate dogumTarihi;
    private UyelikTipi uyelikTipi;
    private UyeDurum uyeDurum;
    private LocalDate katilimTarihi;
    private LocalDate ayrilmaTarihi;
    private String adres;
    private String telefon;
    private String email;
    private String isletmeAdi;
    private String isletmeAdresi;
    private Integer hayvanSayisi;
    private Long birlikId;
    private String birlikAdi;
    private String birlikKodu;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // İstatistikler
    private Integer toplamAidatSayisi;
    private Integer odenmisAidatSayisi;
    private Integer bekleyenAidatSayisi;
    private java.math.BigDecimal toplamBorcTutari;
}
