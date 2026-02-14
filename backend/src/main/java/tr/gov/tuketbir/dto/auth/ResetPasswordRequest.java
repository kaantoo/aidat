package tr.gov.tuketbir.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    
    @NotBlank(message = "Token gereklidir")
    private String token;
    
    @NotBlank(message = "Yeni şifre gereklidir")
    @Size(min = 8, message = "Şifre en az 8 karakter olmalıdır")
    private String newPassword;
}
