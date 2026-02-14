package tr.gov.tuketbir.dto.kullanici;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.domain.enums.KullaniciRol;

/**
 * Kullanıcı Update Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KullaniciUpdateRequest {

    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arasında olmalıdır")
    private String ad;

    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arasında olmalıdır")
    private String soyad;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefon numarası 10-11 rakam olmalıdır")
    private String telefon;

    private KullaniciRol rol;

    private KullaniciDurum durum;

    private Long birlikId;

    private Boolean aktif;
}
