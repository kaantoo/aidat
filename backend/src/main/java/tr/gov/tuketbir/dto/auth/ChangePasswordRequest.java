package tr.gov.tuketbir.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Change Password Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Mevcut şifre zorunludur")
    private String currentPassword;

    @NotBlank(message = "Yeni şifre zorunludur")
    @Size(min = 8, max = 128, message = "Şifre en az 8, en fazla 128 karakter olmalıdır")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*(),.?\":{}|<>]).{8,}$",
        message = "Şifre en az bir büyük harf, bir küçük harf, bir rakam ve bir özel karakter içermelidir"
    )
    private String newPassword;

    @NotBlank(message = "Şifre tekrarı zorunludur")
    private String confirmPassword;
}
