package tr.gov.tuketbir.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import tr.gov.tuketbir.domain.entity.Kullanici;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JWT Token Service
 * 
 * Access ve Refresh token üretimi ve doğrulaması.
 * 
 * Token yapısı:
 * - subject: kullanıcı adı
 * - claims: rol, birlik_id, il_kodu, yetkiler
 * - expiration: token süresi
 * 
 * @author Tuketbir Development Team
 */
@Service
@Slf4j
public class JwtService {

    @Value("${tuketbir.security.jwt.secret-key}")
    private String secretKey;

    @Value("${tuketbir.security.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${tuketbir.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Kullanıcı adını token'dan çıkar
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Belirli bir claim'i token'dan çıkar
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Kullanıcı için Access Token üret
     */
    public String generateAccessToken(Kullanici kullanici) {
        Map<String, Object> extraClaims = new HashMap<>();
        
        // Rol bilgisi
        extraClaims.put("rol", kullanici.getRol().name());
        
        // Birlik bilgisi
        if (kullanici.getBirlik() != null) {
            extraClaims.put("birlik_id", kullanici.getBirlik().getId());
            extraClaims.put("birlik_kodu", kullanici.getBirlik().getBirlikKodu());
        }
        
        // İl/İlçe kısıtlaması
        if (kullanici.getIlKodu() != null) {
            extraClaims.put("il_kodu", kullanici.getIlKodu());
        }
        if (kullanici.getIlceKodu() != null) {
            extraClaims.put("ilce_kodu", kullanici.getIlceKodu());
        }
        
        // Yetkiler
        extraClaims.put("yetkiler", kullanici.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));
        
        // Tenant ID
        extraClaims.put("tenant_id", kullanici.getTenantId());
        
        return generateToken(extraClaims, kullanici, accessTokenExpiration);
    }

    /**
     * Refresh Token üret (minimal claims)
     */
    public String generateRefreshToken(Kullanici kullanici) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, kullanici, refreshTokenExpiration);
    }

    /**
     * Token üret
     */
    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(userDetails.getUsername())
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), Jwts.SIG.HS512)
            .compact();
    }

    /**
     * Token geçerliliğini kontrol et
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Token süresi dolmuş mu
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Token son geçerlilik tarihini çıkar
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Tüm claims'leri çıkar
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSignInKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Signing key
     */
    private SecretKey getSignInKey() {
        // Secret key'i UTF-8 bytes olarak kullan (base64 değil)
        byte[] keyBytes = secretKey.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Token'dan birlik ID'sini al
     */
    public Long extractBirlikId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object birlikId = claims.get("birlik_id");
            return birlikId != null ? Long.valueOf(birlikId.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Token'dan tenant ID'sini al
     */
    public Long extractTenantId(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object tenantId = claims.get("tenant_id");
            return tenantId != null ? Long.valueOf(tenantId.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Token'dan rol bilgisini al
     */
    public String extractRole(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.get("rol");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Access token süresini döndür (ms)
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /**
     * Refresh token süresini döndür (ms)
     */
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
