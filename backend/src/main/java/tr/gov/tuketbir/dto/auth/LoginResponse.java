package tr.gov.tuketbir.dto.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Login Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String kullaniciAdi;
    private String tamAd;
    private String rol;
    private Long birlikId;
    private String birlikAdi;
    private Boolean passwordExpired;
    private Boolean requires2FA;
    private String tempToken;
    private String message;
}
