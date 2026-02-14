package tr.gov.tuketbir.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tr.gov.tuketbir.config.multitenancy.TenantContext;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * Her request'te JWT token'ı kontrol eder ve authentication context'i oluşturur.
 * Ayrıca multi-tenant yapı için tenant context'i set eder.
 * 
 * @author Tuketbir Development Team
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        try {
            // Authorization header kontrolü
            final String authHeader = request.getHeader(AUTHORIZATION_HEADER);
            
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                filterChain.doFilter(request, response);
                return;
            }
            
            // Token'ı çıkar
            final String jwt = authHeader.substring(BEARER_PREFIX.length());
            final String username;
            
            try {
                username = jwtService.extractUsername(jwt);
            } catch (ExpiredJwtException e) {
                log.warn("JWT token expired: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"Token süresi dolmuş\", \"code\": \"TOKEN_EXPIRED\"}");
                return;
            }
            
            // Kullanıcı adı var ve henüz authentication yapılmamışsa
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Kullanıcıyı yükle
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Token geçerli mi kontrol et
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    
                    // Authentication token oluştur
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Security context'e set et
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Multi-tenant context'i set et
                    Long tenantId = jwtService.extractTenantId(jwt);
                    if (tenantId != null) {
                        TenantContext.setCurrentTenant(tenantId);
                    }
                    
                    log.debug("User authenticated: {}, Tenant: {}", username, tenantId);
                }
            }
            
            filterChain.doFilter(request, response);
            
        } finally {
            // Tenant context'i temizle
            TenantContext.clear();
        }
    }

    /**
     * Bu filter'ın uygulanmayacağı path'ler
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/v1/auth/login") ||
               path.startsWith("/v1/auth/refresh") ||
               path.startsWith("/v1/auth/forgot-password") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/actuator/health");
    }
}
