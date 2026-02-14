package tr.gov.tuketbir.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.gov.tuketbir.dto.auth.*;
import tr.gov.tuketbir.dto.common.ApiResponse;
import tr.gov.tuketbir.service.AuthenticationService;

/**
 * Authentication Controller - Kimlik doğrulama işlemleri
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Kimlik doğrulama ve yetkilendirme işlemleri")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    @Operation(summary = "Kullanıcı girişi", description = "Kullanıcı adı ve şifre ile giriş yapma")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login attempt for user: {}", request.getKullaniciAdi());
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authenticationService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "Giriş başarılı"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Token yenileme", description = "Refresh token ile yeni access token alma")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Token refresh requested");
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authenticationService.refreshToken(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "Token yenilendi"));
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "2FA doğrulama", description = "İki faktörlü kimlik doğrulama kodu doğrulama")
    public ResponseEntity<ApiResponse<LoginResponse>> verify2FA(
            @Valid @RequestBody TwoFactorRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("2FA verification for user: {}", request.getKullaniciAdi());
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        LoginResponse response = authenticationService.verify2FA(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success(response, "2FA doğrulama başarılı"));
    }

    @PostMapping("/2fa/setup")
    @Operation(summary = "2FA kurulumu", description = "İki faktörlü kimlik doğrulama kurulumu")
    public ResponseEntity<ApiResponse<TwoFactorSetupResponse>> setup2FA() {
        log.info("2FA setup requested");
        TwoFactorSetupResponse response = authenticationService.setup2FA();
        return ResponseEntity.ok(ApiResponse.success(response, "2FA kurulumu hazırlandı"));
    }

    @PostMapping("/2fa/enable")
    @Operation(summary = "2FA etkinleştirme", description = "İki faktörlü kimlik doğrulamayı etkinleştirme")
    public ResponseEntity<ApiResponse<Void>> enable2FA(
            @RequestParam String code) {
        
        log.info("2FA enable requested");
        authenticationService.confirm2FA(code);
        return ResponseEntity.ok(ApiResponse.success(null, "2FA etkinleştirildi"));
    }

    @PostMapping("/2fa/disable")
    @Operation(summary = "2FA devre dışı bırakma", description = "İki faktörlü kimlik doğrulamayı devre dışı bırakma")
    public ResponseEntity<ApiResponse<Void>> disable2FA(
            @RequestParam String code) {
        
        log.info("2FA disable requested");
        authenticationService.disable2FA(code);
        return ResponseEntity.ok(ApiResponse.success(null, "2FA devre dışı bırakıldı"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Çıkış yapma", description = "Kullanıcı oturumunu sonlandırma")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {
        
        log.info("Logout requested");
        String token = authHeader.replace("Bearer ", "");
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Çıkış yapıldı"));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Şifre değiştirme", description = "Kullanıcı şifresini değiştirme")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        log.info("Password change requested");
        authenticationService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Şifre başarıyla değiştirildi"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Şifre sıfırlama talebi", description = "E-posta ile şifre sıfırlama bağlantısı gönderme")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestParam String email) {
        
        log.info("Password reset requested for email: {}", email);
        authenticationService.forgotPassword(email);
        return ResponseEntity.ok(ApiResponse.success(null, "Şifre sıfırlama bağlantısı e-posta adresinize gönderildi"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Şifre sıfırlama", description = "Token ile şifre sıfırlama")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody ResetPasswordRequest request) {
        
        log.info("Password reset with token");
        authenticationService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Şifreniz başarıyla değiştirildi"));
    }

    @GetMapping("/me")
    @Operation(summary = "Mevcut kullanıcı bilgileri", description = "Giriş yapmış kullanıcının bilgilerini getirme")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser() {
        log.info("Get current user info");
        var kullanici = authenticationService.getCurrentUser();
        LoginResponse response = LoginResponse.builder()
                .kullaniciAdi(kullanici.getKullaniciAdi())
                .tamAd(kullanici.getTamAd())
                .rol(kullanici.getRol().name())
                .birlikId(kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null)
                .birlikAdi(kullanici.getBirlik() != null ? kullanici.getBirlik().getBirlikAdi() : null)
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
