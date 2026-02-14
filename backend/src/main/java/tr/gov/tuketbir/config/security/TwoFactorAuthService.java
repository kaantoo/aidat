package tr.gov.tuketbir.config.security;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * İki Faktörlü Doğrulama (2FA) Service
 * 
 * TOTP (Time-based One-Time Password) algoritması kullanılır.
 * Google Authenticator, Microsoft Authenticator vb. uygulamalarla uyumludur.
 * 
 * @author Tuketbir Development Team
 */
@Service
@Slf4j
public class TwoFactorAuthService {

    @Value("${tuketbir.security.two-factor.issuer}")
    private String issuer;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * Yeni 2FA secret key üret
     */
    public String generateSecretKey() {
        return secretGenerator.generate();
    }

    /**
     * QR kod data URI üret (Base64 encoded PNG)
     */
    public String generateQrCodeDataUri(String secret, String username) {
        try {
            QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

            byte[] imageData = qrGenerator.generate(data);
            return getDataUriForImage(imageData, qrGenerator.getImageMimeType());
        } catch (QrGenerationException e) {
            log.error("QR code generation failed", e);
            throw new RuntimeException("QR kod oluşturulamadı", e);
        }
    }

    /**
     * TOTP kodunu doğrula
     */
    public boolean verifyCode(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }

    /**
     * Yedek kodlar üret (10 adet, 8 karakterlik)
     */
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        SecureRandom random = new SecureRandom();
        
        for (int i = 0; i < 10; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < 8; j++) {
                code.append(random.nextInt(10));
            }
            codes.add(code.toString());
        }
        
        return codes;
    }

    /**
     * Yedek kodu doğrula
     * @param storedCodes Virgülle ayrılmış hash'lenmiş kodlar
     * @param inputCode Kullanıcının girdiği kod
     * @param passwordEncoder Şifre encoder (hash karşılaştırması için)
     */
    public boolean verifyBackupCode(String storedCodes, String inputCode, 
                                     org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return false;
        }
        
        String[] codes = storedCodes.split(",");
        for (String hashedCode : codes) {
            if (passwordEncoder.matches(inputCode, hashedCode.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Kullanılan yedek kodu listeden çıkar
     */
    public String removeUsedBackupCode(String storedCodes, String usedCode,
                                        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        if (storedCodes == null || storedCodes.isEmpty()) {
            return "";
        }
        
        String[] codes = storedCodes.split(",");
        StringBuilder remaining = new StringBuilder();
        
        for (String hashedCode : codes) {
            if (!passwordEncoder.matches(usedCode, hashedCode.trim())) {
                if (remaining.length() > 0) {
                    remaining.append(",");
                }
                remaining.append(hashedCode.trim());
            }
        }
        
        return remaining.toString();
    }

    /**
     * Yedek kodları hash'le
     */
    public String hashBackupCodes(List<String> codes, 
                                   org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        StringBuilder hashed = new StringBuilder();
        for (int i = 0; i < codes.size(); i++) {
            if (i > 0) {
                hashed.append(",");
            }
            hashed.append(passwordEncoder.encode(codes.get(i)));
        }
        return hashed.toString();
    }
}
