package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.config.security.JwtService;
import tr.gov.tuketbir.config.security.TwoFactorAuthService;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.entity.RefreshToken;
import tr.gov.tuketbir.dto.auth.*;
import tr.gov.tuketbir.exception.AuthenticationException;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.repository.KullaniciRepository;
import tr.gov.tuketbir.repository.RefreshTokenRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Authentication Service
 * 
 * Giriş, çıkış, token yenileme ve 2FA işlemlerini yönetir.
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final KullaniciRepository kullaniciRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    /**
     * Kullanıcı girişi
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Login attempt for user: {}", request.getKullaniciAdi());
        
        // Kullanıcıyı bul
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(request.getKullaniciAdi())
            .orElseThrow(() -> {
                auditLogService.logBasarisizGiris(request.getKullaniciAdi(), ipAddress, "Kullanıcı bulunamadı");
                return new AuthenticationException("Geçersiz kullanıcı adı veya şifre");
            });
        
        // Hesap kilitli mi kontrol et
        if (!kullanici.isAccountNonLocked()) {
            auditLogService.logBasarisizGiris(request.getKullaniciAdi(), ipAddress, "Hesap kilitli");
            throw new AuthenticationException("Hesabınız kilitli. Lütfen " + 
                kullanici.getKilitBitisZamani() + " tarihinden sonra tekrar deneyin.");
        }
        
        // Hesap aktif mi kontrol et
        if (!kullanici.isEnabled()) {
            auditLogService.logBasarisizGiris(request.getKullaniciAdi(), ipAddress, "Hesap pasif");
            throw new AuthenticationException("Hesabınız aktif değil. Yöneticinizle iletişime geçin.");
        }
        
        try {
            // Authentication
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getKullaniciAdi(),
                    request.getSifre()
                )
            );
        } catch (Exception e) {
            // Başarısız giriş sayacını artır
            kullanici.basarisizGirisArttir();
            kullaniciRepository.save(kullanici);
            auditLogService.logBasarisizGiris(request.getKullaniciAdi(), ipAddress, "Yanlış şifre");
            throw new AuthenticationException("Geçersiz kullanıcı adı veya şifre");
        }
        
        // 2FA aktif mi kontrol et
        if (kullanici.getIkiFaktorAktif()) {
            // 2FA gerekli - geçici token döndür
            String tempToken = UUID.randomUUID().toString();
            // Not: Gerçek implementasyonda bu token Redis'te saklanmalı
            return LoginResponse.builder()
                .requires2FA(true)
                .tempToken(tempToken)
                .message("İki faktörlü doğrulama gerekli")
                .build();
        }
        
        // Başarılı giriş
        return completeLogin(kullanici, ipAddress, userAgent);
    }

    /**
     * 2FA doğrulama
     */
    @Transactional
    public LoginResponse verify2FA(TwoFactorRequest request, String ipAddress, String userAgent) {
        // Not: Gerçek implementasyonda tempToken Redis'ten alınmalı
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(request.getKullaniciAdi())
            .orElseThrow(() -> new AuthenticationException("Kullanıcı bulunamadı"));
        
        // TOTP kodunu doğrula
        boolean isValidCode = twoFactorAuthService.verifyCode(
            kullanici.getIkiFaktorSecret(), 
            request.getCode()
        );
        
        if (!isValidCode) {
            // Yedek kod dene
            if (request.getCode().length() == 8) {
                isValidCode = twoFactorAuthService.verifyBackupCode(
                    kullanici.getYedekKodlar(),
                    request.getCode(),
                    passwordEncoder
                );
                
                if (isValidCode) {
                    // Kullanılan yedek kodu çıkar
                    String remainingCodes = twoFactorAuthService.removeUsedBackupCode(
                        kullanici.getYedekKodlar(),
                        request.getCode(),
                        passwordEncoder
                    );
                    kullanici.setYedekKodlar(remainingCodes);
                    kullaniciRepository.save(kullanici);
                }
            }
        }
        
        if (!isValidCode) {
            auditLogService.logBasarisizGiris(request.getKullaniciAdi(), ipAddress, "Geçersiz 2FA kodu");
            throw new AuthenticationException("Geçersiz doğrulama kodu");
        }
        
        return completeLogin(kullanici, ipAddress, userAgent);
    }

    /**
     * Giriş işlemini tamamla - token oluştur
     */
    private LoginResponse completeLogin(Kullanici kullanici, String ipAddress, String userAgent) {
        // Başarılı giriş kaydı
        kullanici.basariliGiris();
        kullaniciRepository.save(kullanici);
        
        // Access token oluştur
        String accessToken = jwtService.generateAccessToken(kullanici);
        
        // Refresh token oluştur ve kaydet
        RefreshToken refreshToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .kullanici(kullanici)
            .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshTokenExpiration())))
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        refreshTokenRepository.save(refreshToken);
        
        // Audit log
        auditLogService.logBasariliGiris(kullanici, ipAddress);
        
        // Şifre süresi dolmuş mu kontrol et
        boolean passwordExpired = kullanici.isSifreSuresiDolmus();
        
        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
            .kullaniciAdi(kullanici.getKullaniciAdi())
            .tamAd(kullanici.getTamAd())
            .rol(kullanici.getRol().name())
            .birlikId(kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null)
            .birlikAdi(kullanici.getBirlik() != null ? kullanici.getBirlik().getBirlikAdi() : null)
            .passwordExpired(passwordExpired)
            .requires2FA(false)
            .build();
    }

    /**
     * Token yenileme
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
            .orElseThrow(() -> new AuthenticationException("Geçersiz refresh token"));
        
        if (!refreshToken.isValid()) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token süresi dolmuş veya iptal edilmiş");
        }
        
        Kullanici kullanici = refreshToken.getKullanici();
        
        // Eski refresh token'ı iptal et
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);
        
        // Yeni tokenlar oluştur
        String newAccessToken = jwtService.generateAccessToken(kullanici);
        RefreshToken newRefreshToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .kullanici(kullanici)
            .expiryDate(LocalDateTime.now().plus(Duration.ofMillis(jwtService.getRefreshTokenExpiration())))
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
        refreshTokenRepository.save(newRefreshToken);
        
        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(newRefreshToken.getToken())
            .tokenType("Bearer")
            .expiresIn(jwtService.getAccessTokenExpiration() / 1000)
            .kullaniciAdi(kullanici.getKullaniciAdi())
            .tamAd(kullanici.getTamAd())
            .rol(kullanici.getRol().name())
            .birlikId(kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null)
            .birlikAdi(kullanici.getBirlik() != null ? kullanici.getBirlik().getBirlikAdi() : null)
            .requires2FA(false)
            .build();
    }

    /**
     * Çıkış - Tüm tokenları iptal et
     */
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken).orElse(null);
        if (token != null) {
            Long kullaniciId = token.getKullanici().getId();
            refreshTokenRepository.revokeAllByKullaniciId(kullaniciId, LocalDateTime.now());
            auditLogService.logCikis(token.getKullanici());
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * Şifre değiştirme
     */
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
            .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
        
        // Mevcut şifre kontrolü
        if (!passwordEncoder.matches(request.getCurrentPassword(), kullanici.getSifre())) {
            throw new BusinessException("Mevcut şifre hatalı");
        }
        
        // Yeni şifre kontrolü
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Yeni şifreler eşleşmiyor");
        }
        
        // Şifre güncelle
        kullanici.sifreDegistir(passwordEncoder.encode(request.getNewPassword()));
        kullaniciRepository.save(kullanici);
        
        // Tüm refresh tokenları iptal et
        refreshTokenRepository.revokeAllByKullaniciId(kullanici.getId(), LocalDateTime.now());
        
        auditLogService.logSifreDegistirme(kullanici);
    }

    /**
     * 2FA aktifleştir
     */
    @Transactional
    public TwoFactorSetupResponse setup2FA() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
            .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
        
        // Secret key oluştur
        String secretKey = twoFactorAuthService.generateSecretKey();
        
        // QR kod oluştur
        String qrCodeDataUri = twoFactorAuthService.generateQrCodeDataUri(
            secretKey, 
            kullanici.getKullaniciAdi()
        );
        
        // Yedek kodlar oluştur
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();
        
        // Secret'ı geçici olarak kaydet (doğrulama sonrası kalıcı olacak)
        kullanici.setIkiFaktorSecret(secretKey);
        kullaniciRepository.save(kullanici);
        
        return TwoFactorSetupResponse.builder()
            .secret(secretKey)
            .qrCodeUri(qrCodeDataUri)
            .manualEntryKey(secretKey)
            .issuer("Tuketbir")
            .accountName(kullanici.getKullaniciAdi())
            .build();
    }

    /**
     * 2FA doğrula ve aktifleştir
     */
    @Transactional
    public void confirm2FA(String code) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
            .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
        
        if (kullanici.getIkiFaktorSecret() == null) {
            throw new BusinessException("Önce 2FA kurulumu yapılmalı");
        }
        
        // Kodu doğrula
        boolean isValid = twoFactorAuthService.verifyCode(kullanici.getIkiFaktorSecret(), code);
        if (!isValid) {
            throw new BusinessException("Geçersiz doğrulama kodu");
        }
        
        // Yedek kodları hash'le ve kaydet
        List<String> backupCodes = twoFactorAuthService.generateBackupCodes();
        String hashedCodes = twoFactorAuthService.hashBackupCodes(backupCodes, passwordEncoder);
        
        kullanici.setIkiFaktorAktif(true);
        kullanici.setYedekKodlar(hashedCodes);
        kullaniciRepository.save(kullanici);
        
        auditLogService.log2FAaktif(kullanici);
    }

    /**
     * 2FA devre dışı bırak
     */
    @Transactional
    public void disable2FA(String password) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(auth.getName())
            .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
        
        // Şifre kontrolü
        if (!passwordEncoder.matches(password, kullanici.getSifre())) {
            throw new BusinessException("Şifre hatalı");
        }
        
        kullanici.setIkiFaktorAktif(false);
        kullanici.setIkiFaktorSecret(null);
        kullanici.setYedekKodlar(null);
        kullaniciRepository.save(kullanici);
        
        auditLogService.log2FApasif(kullanici);
    }

    /**
     * Mevcut kullanıcıyı getir
     */
    public Kullanici getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return kullaniciRepository.findByKullaniciAdi(auth.getName())
            .orElseThrow(() -> new BusinessException("Kullanıcı bulunamadı"));
    }

    /**
     * Şifre sıfırlama talebi
     */
    @Transactional
    public void forgotPassword(String email) {
        log.info("Password reset requested for email: {}", email);
        
        Kullanici kullanici = kullaniciRepository.findByEmail(email)
            .orElse(null);
        
        // Güvenlik: Kullanıcı bulunamasa bile başarılı mesaj dön
        if (kullanici == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        // Reset token oluştur
        String resetToken = UUID.randomUUID().toString();
        kullanici.setSifreSifirlamaToken(resetToken);
        kullanici.setTokenGecerlilikZamani(LocalDateTime.now().plusHours(24));
        kullaniciRepository.save(kullanici);
        
        // NOT: E-posta servisi entegre edildiğinde burada reset linki kullanıcıya e-posta ile gönderilecek
        log.info("PASSWORD RESET TOKEN for {}: {}", email, resetToken);
        log.info("Reset URL: http://localhost:5175/reset-password?token={}", resetToken);
        
        auditLogService.logPasswordResetRequest(kullanici);
    }

    /**
     * Şifre sıfırlama
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Password reset with token");
        
        Kullanici kullanici = kullaniciRepository.findBySifreSifirlamaToken(token)
            .orElseThrow(() -> new BusinessException("Geçersiz veya süresi dolmuş token"));
        
        // Token süresi kontrolü
        if (kullanici.getTokenGecerlilikZamani() == null ||
            kullanici.getTokenGecerlilikZamani().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Şifre sıfırlama bağlantısının süresi dolmuş");
        }
        
        // Yeni şifreyi kaydet
        kullanici.setSifre(passwordEncoder.encode(newPassword));
        kullanici.setSifreSifirlamaToken(null);
        kullanici.setTokenGecerlilikZamani(null);
        kullaniciRepository.save(kullanici);
        
        log.info("Password reset successful for user: {}", kullanici.getKullaniciAdi());
        auditLogService.logPasswordReset(kullanici);
    }
}
