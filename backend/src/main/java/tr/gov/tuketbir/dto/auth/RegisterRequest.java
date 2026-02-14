package tr.gov.tuketbir.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.KullaniciRol;

/**
 * User Registration Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Kullanıcı adı zorunludur")
    @Size(min = 4, max = 50, message = "Kullanıcı adı 4-50 karakter arasında olmalıdır")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Kullanıcı adı sadece harf, rakam, nokta, tire ve alt çizgi içerebilir")
    private String kullaniciAdi;

    @NotBlank(message = "Şifre zorunludur")
    @Size(min = 8, max = 128, message = "Şifre en az 8, en fazla 128 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
        message = "Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir"
    )
    private String sifre;

    @NotBlank(message = "Ad zorunludur")
    @Size(max = 100, message = "Ad en fazla 100 karakter olabilir")
    private String ad;

    @NotBlank(message = "Soyad zorunludur")
    @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir")
    private String soyad;

    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefon numarası 10-11 rakam olmalıdır")
    private String telefon;

    @NotNull(message = "Rol zorunludur")
    private KullaniciRol rol;

    private Long birlikId;

    @Size(max = 11, message = "TC Kimlik No 11 karakter olmalıdır")
    @Pattern(regexp = "^[0-9]{11}$", message = "TC Kimlik No 11 rakamdan oluşmalıdır")
    private String tcKimlikNo;
}
