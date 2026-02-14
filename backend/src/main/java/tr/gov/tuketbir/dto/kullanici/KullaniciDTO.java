package tr.gov.tuketbir.dto.kullanici;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.domain.enums.KullaniciRol;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Kullanıcı Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KullaniciDTO {

    private Long id;
    private String kullaniciAdi;
    private String ad;
    private String soyad;
    private String adSoyad;
    private String email;
    private String telefon;
    private String tcKimlikNo;
    private KullaniciRol rol;
    private Set<String> yetkiler;
    private KullaniciDurum durum;
    private Long birlikId;
    private String birlikAdi;
    private Boolean twoFactorEnabled;
    private LocalDateTime sonGirisTarihi;
    private LocalDateTime sifreDegistirilmeTarihi;
    private Boolean sifreSuresiDolmus;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
