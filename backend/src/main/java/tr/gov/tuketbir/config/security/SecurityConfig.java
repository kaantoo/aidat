package tr.gov.tuketbir.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security Konfigürasyonu
 * 
 * - Stateless session (JWT tabanlı)
 * - RBAC (Role-Based Access Control)
 * - CORS ayarları
 * - Method-level security
 * 
 * @author Tuketbir Development Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ReadOnlyModeFilter readOnlyModeFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Public erişime açık endpoint'ler
     */
    private static final String[] PUBLIC_ENDPOINTS = {
        "/",
        "/api/v1/auth/**",
        "/v3/api-docs/**",
        "/v3/api-docs",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/swagger-resources/**",
        "/webjars/**",
        "/actuator/**",
        "/management/**",
        "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF devre dışı (stateless JWT kullanıyoruz)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS konfigürasyonu
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Session yönetimi - Stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Authorization kuralları
            .authorizeHttpRequests(auth -> auth
                // Public endpoint'ler
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                
                // Merkez Yönetici - tüm erişim
                .requestMatchers("/api/v1/admin/**").hasRole("MERKEZ_YONETICI")
                .requestMatchers("/api/v1/sistem/**").hasRole("MERKEZ_YONETICI")
                
                // Birlik yönetimi
                .requestMatchers("/api/v1/birlik/**").hasAnyRole("MERKEZ_YONETICI", "BIRLIK_YONETICI")
                
                // Üye yönetimi
                .requestMatchers("/api/v1/uye/**").hasAnyRole(
                    "MERKEZ_YONETICI", "BIRLIK_YONETICI", "BIRLIK_PERSONEL", "MUHASEBE_SORUMLU"
                )
                
                // Aidat yönetimi
                .requestMatchers("/api/v1/aidat/**").hasAnyRole(
                    "MERKEZ_YONETICI", "BIRLIK_YONETICI", "MUHASEBE_SORUMLU"
                )
                
                // Tahsilat yönetimi
                .requestMatchers("/api/v1/tahsilat/**").hasAnyRole(
                    "MERKEZ_YONETICI", "BIRLIK_YONETICI", "BIRLIK_PERSONEL", "MUHASEBE_SORUMLU"
                )
                
                // Gelir-Gider yönetimi
                .requestMatchers("/api/v1/gelir-gider/**").hasAnyRole(
                    "MERKEZ_YONETICI", "BIRLIK_YONETICI", "MUHASEBE_SORUMLU"
                )
                
                // Toplantı ve Karar yönetimi
                .requestMatchers("/api/v1/toplantilar/**").hasAnyRole(
                    "MERKEZ_YONETICI", "BIRLIK_YONETICI"
                )
                
                // Belge yönetimi
                .requestMatchers("/api/v1/belge/**").authenticated()
                
                // Raporlar
                .requestMatchers("/api/v1/rapor/**").authenticated()
                
                // Kullanıcı yönetimi
                .requestMatchers("/api/v1/kullanici/**").authenticated()
                
                // Bildirimler
                .requestMatchers("/api/v1/bildirim/**").authenticated()
                
                // Dashboard
                .requestMatchers("/api/v1/dashboard/**").authenticated()
                
                // Profil
                .requestMatchers("/api/v1/profil/**").authenticated()
                
                // Diğer tüm istekler authentication gerektirir
                .anyRequest().authenticated()
            )
            
            // Authentication provider
            .authenticationProvider(authenticationProvider())
            
            // JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Read-only mode filter (JWT'den sonra çalışır)
            .addFilterAfter(readOnlyModeFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173",
            "http://localhost:5174",
            "http://localhost:5175",
            "https://aidat.tuketbir.gov.tr"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "X-Tenant-ID"
        ));
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Disposition",
            "X-Total-Count"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Argon2 Password Encoder
     * Argon2, bcrypt'e göre daha güvenli ve modern bir hash algoritmasıdır.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
            16,     // salt length
            32,     // hash length
            1,      // parallelism
            65536,  // memory (64 MB)
            3       // iterations
        );
    }
}
