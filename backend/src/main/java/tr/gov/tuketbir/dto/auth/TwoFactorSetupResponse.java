package tr.gov.tuketbir.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Two Factor Authentication Setup Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwoFactorSetupResponse {

    private String secret;
    private String qrCodeUri;
    private String manualEntryKey;
    private String issuer;
    private String accountName;
}
