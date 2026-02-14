package tr.gov.tuketbir.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.BirlikTipi;
import tr.gov.tuketbir.domain.enums.KullaniciRol;
import tr.gov.tuketbir.repository.BirlikRepository;
import tr.gov.tuketbir.repository.KullaniciRepository;

import java.time.LocalDateTime;

/**
 * Uygulama başlangıcında varsayılan admin kullanıcı ve merkez birlik oluşturur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final KullaniciRepository kullaniciRepository;
    private final BirlikRepository birlikRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        createMerkezBirlik();
        createAdminUser();
    }

    private void createMerkezBirlik() {
        if (birlikRepository.count() == 0) {
            Birlik merkezBirlik = new Birlik();
            merkezBirlik.setBirlikAdi("Türkiye Kırmızı Et Üreticileri Merkez Birliği");
            merkezBirlik.setBirlikKodu("TUKETBIR");
            merkezBirlik.setBirlikTipi(BirlikTipi.MERKEZ);
            merkezBirlik.setIlKodu("06");
            merkezBirlik.setIlceKodu("0601");
            merkezBirlik.setAdres("Merkez Mahallesi, Birlik Caddesi No:1");
            merkezBirlik.setTelefon("0312 000 00 00");
            merkezBirlik.setEmail("info@tuketbir.gov.tr");
            merkezBirlik.setVergiNo("1234567890");
            merkezBirlik.setVergiDairesi("Çankaya Vergi Dairesi");
            merkezBirlik.setIsActive(true);
            merkezBirlik.setTenantId(1L); // Merkez birlik için tenant ID
            
            birlikRepository.save(merkezBirlik);
            log.info("Merkez Birlik oluşturuldu: {}", merkezBirlik.getBirlikAdi());
        }
    }

    private void createAdminUser() {
        if (kullaniciRepository.count() == 0) {
            Birlik merkezBirlik = birlikRepository.findByBirlikTipi(BirlikTipi.MERKEZ)
                    .stream().findFirst()
                    .orElseGet(() -> birlikRepository.findAll().stream().findFirst().orElse(null));

            if (merkezBirlik == null) {
                log.error("Merkez Birlik bulunamadı, admin kullanıcı oluşturulamadı!");
                return;
            }

            Kullanici admin = new Kullanici();
            admin.setKullaniciAdi("admin");
            admin.setSifre(passwordEncoder.encode("Admin123!"));
            admin.setAd("Sistem");
            admin.setSoyad("Yöneticisi");
            admin.setEmail("admin@tuketbir.gov.tr");
            admin.setTelefon("0312 000 00 01");
            admin.setRol(KullaniciRol.MERKEZ_YONETICI);
            admin.setBirlik(merkezBirlik);
            admin.setIsActive(true);
            admin.setSonSifreDegisim(LocalDateTime.now());
            admin.setIkiFaktorAktif(false);
            admin.setTenantId(1L); // Merkez birlik tenant ID

            kullaniciRepository.save(admin);
            
            log.info("==============================================");
            log.info("VARSAYILAN ADMIN KULLANICI OLUŞTURULDU");
            log.info("Kullanıcı Adı: admin");
            log.info("Şifre: Admin123!");
            log.info("==============================================");
        } else {
            log.info("Kullanıcılar zaten mevcut, varsayılan admin oluşturulmadı.");
        }
    }
}
