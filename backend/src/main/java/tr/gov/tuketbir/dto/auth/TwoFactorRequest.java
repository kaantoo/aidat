package tr.gov.tuketbir.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Two Factor Authentication Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorRequest {

    @NotBlank(message = "Kullanıcı adı zorunludur")
    private String kullaniciAdi;

    @NotBlank(message = "Doğrulama kodu zorunludur")
    private String code;

    private String tempToken;
}
