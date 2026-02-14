package tr.gov.tuketbir.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.KullaniciRol;
import tr.gov.tuketbir.service.SistemAyariService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Read-Only Mode Filter
 * Sistem read-only modundayken yazma işlemlerini engeller
 * SISTEM_ADMIN rolündeki kullanıcılar hariç
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReadOnlyModeFilter extends OncePerRequestFilter {

    private final SistemAyariService sistemAyariService;
    private final ObjectMapper objectMapper;

    // Yazma işlemi yapan HTTP metodları
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    // Read-only modda bile izin verilen endpoint'ler
    private static final Set<String> ALLOWED_ENDPOINTS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/api/v1/sistem/read-only",  // Admin read-only mod'u kapatabilmeli
            "/api/v1/bildirimler"  // Bildirim okundu işaretleme
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod();
        String path = request.getRequestURI();

        // Sadece yazma metodlarını kontrol et
        if (!WRITE_METHODS.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // İzin verilen endpoint'leri kontrol et
        if (isAllowedEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read-only mod aktif mi kontrol et
        if (!sistemAyariService.isReadOnlyMode()) {
            filterChain.doFilter(request, response);
            return;
        }

        // SISTEM_ADMIN kullanıcıları her zaman yazabilir
        if (isSystemAdmin()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Read-only modda - yazma işlemini engelle
        log.warn("Read-only mod aktif - Yazma işlemi engellendi: {} {}", method, path);
        sendReadOnlyResponse(response);
    }

    private boolean isAllowedEndpoint(String path) {
        return ALLOWED_ENDPOINTS.stream().anyMatch(path::startsWith);
    }

    private boolean isSystemAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Kullanici) {
            Kullanici kullanici = (Kullanici) principal;
            return kullanici.getRol() == KullaniciRol.SISTEM_ADMIN;
        }

        return false;
    }

    private void sendReadOnlyResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Read-Only Mode");
        errorResponse.put("message", sistemAyariService.getReadOnlyMesaj());
        errorResponse.put("readOnlyMode", true);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
