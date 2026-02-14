package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.domain.enums.KullaniciRol;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kullanıcı Entity - Sistem kullanıcılarını temsil eder.
 * Spring Security UserDetails implementasyonu içerir.
 * 
 * Roller:
 * - MERKEZ_YONETICI: Tüm sistem erişimi
 * - BIRLIK_YONETICI: Kendi birliği yönetimi
 * - BIRLIK_PERSONEL: Operasyonel işlemler
 * - MUHASEBE_SORUMLU: Mali işlemler
 * - GOZLEMCI: Salt-okunur erişim
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "kullanicilar", indexes = {
    @Index(name = "idx_kullanici_username", columnList = "kullanici_adi", unique = true),
    @Index(name = "idx_kullanici_email", columnList = "email", unique = true),
    @Index(name = "idx_kullanici_birlik", columnList = "birlik_id"),
    @Index(name = "idx_kullanici_rol", columnList = "rol"),
    @Index(name = "idx_kullanici_durum", columnList = "durum")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Kullanici extends BaseEntity implements UserDetails {

    /**
     * Kullanıcı adı (login)
     */
    @NotBlank(message = "Kullanıcı adı zorunludur")
    @Size(min = 4, max = 50, message = "Kullanıcı adı 4-50 karakter arasında olmalıdır")
    @Column(name = "kullanici_adi", nullable = false, unique = true, length = 50)
    private String kullaniciAdi;

    /**
     * Şifre (hash'lenmiş)
     */
    @NotBlank(message = "Şifre zorunludur")
    @Column(name = "sifre", nullable = false, length = 255)
    private String sifre;

    /**
     * Ad
     */
    @NotBlank(message = "Ad zorunludur")
    @Column(name = "ad", nullable = false, length = 100)
    private String ad;

    /**
     * Soyad
     */
    @NotBlank(message = "Soyad zorunludur")
    @Column(name = "soyad", nullable = false, length = 100)
    private String soyad;

    /**
     * E-posta
     */
    @NotBlank(message = "E-posta zorunludur")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    /**
     * Telefon
     */
    @Column(name = "telefon", length = 20)
    private String telefon;

    /**
     * Bağlı birlik
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id")
    private Birlik birlik;

    /**
     * Ana rol
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 30)
    private KullaniciRol rol;

    /**
     * Ek roller (virgülle ayrılmış)
     */
    @Column(name = "ek_roller", length = 200)
    private String ekRoller;

    /**
     * Kullanıcı durumu
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false, length = 20)
    @Builder.Default
    private KullaniciDurum durum = KullaniciDurum.AKTIF;

    /**
     * İl kodu (veri erişim kısıtlaması)
     */
    @Column(name = "il_kodu", length = 10)
    private String ilKodu;

    /**
     * İlçe kodu (veri erişim kısıtlaması)
     */
    @Column(name = "ilce_kodu", length = 10)
    private String ilceKodu;

    // ======================= Güvenlik Alanları =======================

    /**
     * Son giriş zamanı
     */
    @Column(name = "son_giris_zamani")
    private LocalDateTime sonGirisZamani;

    /**
     * Son şifre değiştirme zamanı
     */
    @Column(name = "son_sifre_degisim")
    private LocalDateTime sonSifreDegisim;

    /**
     * Başarısız giriş sayısı
     */
    @Column(name = "basarisiz_giris_sayisi", nullable = false)
    @Builder.Default
    private Integer basarisizGirisSayisi = 0;

    /**
     * Hesap kilitli mi
     */
    @Column(name = "hesap_kilitli", nullable = false)
    @Builder.Default
    private Boolean hesapKilitli = false;

    /**
     * Kilit bitiş zamanı
     */
    @Column(name = "kilit_bitis_zamani")
    private LocalDateTime kilitBitisZamani;

    /**
     * Şifre sıfırlama token
     */
    @Column(name = "sifre_sifirlama_token", length = 255)
    private String sifreSifirlamaToken;

    /**
     * Token son geçerlilik zamanı
     */
    @Column(name = "token_gecerlilik_zamani")
    private LocalDateTime tokenGecerlilikZamani;

    // ======================= 2FA Alanları =======================

    /**
     * 2FA aktif mi
     */
    @Column(name = "iki_faktor_aktif", nullable = false)
    @Builder.Default
    private Boolean ikiFaktorAktif = false;

    /**
     * 2FA gizli anahtarı
     */
    @Column(name = "iki_faktor_secret", length = 255)
    private String ikiFaktorSecret;

    /**
     * Yedek kodlar (virgülle ayrılmış, hash'lenmiş)
     */
    @Column(name = "yedek_kodlar", length = 1000)
    private String yedekKodlar;

    // ======================= Refresh Token =======================

    /**
     * Refresh tokenlar
     */
    @OneToMany(mappedBy = "kullanici", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokenlar = new HashSet<>();

    // ======================= Profil =======================

    /**
     * Profil fotoğrafı yolu
     */
    @Column(name = "profil_foto", length = 500)
    private String profilFoto;

    /**
     * Tercih edilen dil
     */
    @Column(name = "dil", length = 5)
    @Builder.Default
    private String dil = "tr";

    /**
     * Zaman dilimi
     */
    @Column(name = "zaman_dilimi", length = 50)
    @Builder.Default
    private String zamanDilimi = "Europe/Istanbul";

    // ======================= UserDetails Implementasyonu =======================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.rol.name()));
        
        if (ekRoller != null && !ekRoller.isEmpty()) {
            for (String ekRol : ekRoller.split(",")) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + ekRol.trim()));
            }
        }
        
        // Rol bazlı yetkiler
        authorities.addAll(this.rol.getYetkiler().stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet()));
        
        return authorities;
    }

    @Override
    public String getPassword() {
        return this.sifre;
    }

    @Override
    public String getUsername() {
        return this.kullaniciAdi;
    }

    @Override
    public boolean isAccountNonExpired() {
        return KullaniciDurum.AKTIF.equals(this.durum);
    }

    @Override
    public boolean isAccountNonLocked() {
        if (this.hesapKilitli && this.kilitBitisZamani != null) {
            if (LocalDateTime.now().isAfter(this.kilitBitisZamani)) {
                this.hesapKilitli = false;
                this.kilitBitisZamani = null;
                this.basarisizGirisSayisi = 0;
                return true;
            }
            return false;
        }
        return !this.hesapKilitli;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 30 gün şifre değiştirme kontrolü
        if (this.sonSifreDegisim == null) return true;
        return this.sonSifreDegisim.plusDays(30).isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isEnabled() {
        return KullaniciDurum.AKTIF.equals(this.durum) && this.getIsActive();
    }

    // ======================= Yardımcı Metodlar =======================

    /**
     * Tam ad döndür
     */
    public String getTamAd() {
        return this.ad + " " + this.soyad;
    }

    /**
     * Başarısız giriş sayacını artır
     */
    public void basarisizGirisArttir() {
        this.basarisizGirisSayisi++;
        if (this.basarisizGirisSayisi >= 5) {
            this.hesapKilitli = true;
            this.kilitBitisZamani = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * Başarılı giriş
     */
    public void basariliGiris() {
        this.basarisizGirisSayisi = 0;
        this.hesapKilitli = false;
        this.kilitBitisZamani = null;
        this.sonGirisZamani = LocalDateTime.now();
    }

    /**
     * Şifre değişikliği
     */
    public void sifreDegistir(String yeniSifreHash) {
        this.sifre = yeniSifreHash;
        this.sonSifreDegisim = LocalDateTime.now();
    }

    /**
     * Merkez yönetici mi kontrolü
     */
    public boolean isMerkezYonetici() {
        return KullaniciRol.MERKEZ_YONETICI.equals(this.rol);
    }

    /**
     * Şifre süresi dolmuş mu
     */
    public boolean isSifreSuresiDolmus() {
        if (this.sonSifreDegisim == null) return false;
        return this.sonSifreDegisim.plusDays(30).isBefore(LocalDateTime.now());
    }
}
