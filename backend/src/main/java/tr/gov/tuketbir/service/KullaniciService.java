package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.config.multitenancy.TenantContext;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.domain.enums.KullaniciRol;
import tr.gov.tuketbir.dto.auth.RegisterRequest;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.kullanici.*;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.DuplicateResourceException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.BirlikRepository;
import tr.gov.tuketbir.repository.KullaniciRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kullanici Service - Kullanıcı yönetimi iş mantığı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KullaniciService {

    private final KullaniciRepository kullaniciRepository;
    private final BirlikRepository birlikRepository;
    private final PasswordEncoder passwordEncoder;

    public PagedResponse<KullaniciDTO> getKullanicilar(Long birlikId, KullaniciRol rol, Boolean aktif, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        Page<Kullanici> kullaniciPage;

        if (birlikId != null && rol != null) {
            kullaniciPage = kullaniciRepository.findByBirlikIdAndRol(birlikId, rol, pageable);
        } else if (birlikId != null) {
            kullaniciPage = kullaniciRepository.findByBirlikId(birlikId, pageable);
        } else if (rol != null) {
            kullaniciPage = kullaniciRepository.findByRol(rol, pageable);
        } else {
            kullaniciPage = kullaniciRepository.findAll(pageable);
        }

        List<KullaniciDTO> content = kullaniciPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return PagedResponse.of(kullaniciPage, content);
    }

    public KullaniciDTO getKullaniciById(Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));
        return toDTO(kullanici);
    }

    @Transactional
    public KullaniciDTO createKullanici(RegisterRequest request) {
        // Kullanıcı adı kontrolü
        if (kullaniciRepository.existsByKullaniciAdi(request.getKullaniciAdi())) {
            throw new DuplicateResourceException("Kullanıcı", "kullaniciAdi", request.getKullaniciAdi());
        }

        // E-posta kontrolü
        if (kullaniciRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Kullanıcı", "email", request.getEmail());
        }

        Kullanici kullanici = new Kullanici();
        kullanici.setKullaniciAdi(request.getKullaniciAdi());
        kullanici.setSifre(passwordEncoder.encode(request.getSifre()));
        kullanici.setAd(request.getAd());
        kullanici.setSoyad(request.getSoyad());
        kullanici.setEmail(request.getEmail());
        kullanici.setTelefon(request.getTelefon());
        kullanici.setRol(request.getRol());
        kullanici.setDurum(KullaniciDurum.AKTIF);
        kullanici.setSonSifreDegisim(LocalDateTime.now());
        kullanici.setTenantId(TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : 0L);

        if (request.getBirlikId() != null) {
            Birlik birlik = birlikRepository.findById(request.getBirlikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", request.getBirlikId()));
            kullanici.setBirlik(birlik);
        }

        kullanici = kullaniciRepository.save(kullanici);
        
        log.info("Created new kullanici: {}", kullanici.getKullaniciAdi());
        return toDTO(kullanici);
    }

    @Transactional
    public KullaniciDTO updateKullanici(Long id, KullaniciUpdateRequest request) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));

        // SISTEM_ADMIN rolü değiştirilemez
        if (kullanici.getRol() == KullaniciRol.SISTEM_ADMIN && request.getRol() != null 
                && request.getRol() != KullaniciRol.SISTEM_ADMIN) {
            throw new tr.gov.tuketbir.exception.BusinessException("Sistem yöneticisi rolü değiştirilemez!");
        }

        // SISTEM_ADMIN durumu değiştirilemez
        if (kullanici.getRol() == KullaniciRol.SISTEM_ADMIN && request.getDurum() != null 
                && request.getDurum() != KullaniciDurum.AKTIF) {
            throw new tr.gov.tuketbir.exception.BusinessException("Sistem yöneticisi hesabı pasifleştirilemez!");
        }

        if (request.getAd() != null) {
            kullanici.setAd(request.getAd());
        }
        if (request.getSoyad() != null) {
            kullanici.setSoyad(request.getSoyad());
        }
        if (request.getEmail() != null) {
            // E-posta benzersizlik kontrolü
            if (!kullanici.getEmail().equals(request.getEmail()) 
                    && kullaniciRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Kullanıcı", "email", request.getEmail());
            }
            kullanici.setEmail(request.getEmail());
        }
        if (request.getTelefon() != null) {
            kullanici.setTelefon(request.getTelefon());
        }
        if (request.getRol() != null) {
            kullanici.setRol(request.getRol());
        }
        if (request.getDurum() != null) {
            kullanici.setDurum(request.getDurum());
        }
        if (request.getBirlikId() != null) {
            Birlik birlik = birlikRepository.findById(request.getBirlikId())
                    .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", request.getBirlikId()));
            kullanici.setBirlik(birlik);
        }
        if (request.getAktif() != null) {
            kullanici.setIsActive(request.getAktif());
        }

        kullanici = kullaniciRepository.save(kullanici);
        
        log.info("Updated kullanici: {}", id);
        return toDTO(kullanici);
    }

    @Transactional
    public void deleteKullanici(Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));

        // SISTEM_ADMIN koruması
        if (kullanici.getRol() == KullaniciRol.SISTEM_ADMIN) {
            throw new tr.gov.tuketbir.exception.BusinessException("Sistem yöneticisi hesabı silinemez!");
        }

        kullanici.softDelete();
        kullanici.setDurum(KullaniciDurum.PASIF);
        kullaniciRepository.save(kullanici);
        
        log.info("Deleted (soft) kullanici: {}", id);
    }

    @Transactional
    public KullaniciDTO activateKullanici(Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));

        kullanici.restore();
        kullanici.setDurum(KullaniciDurum.AKTIF);
        kullanici = kullaniciRepository.save(kullanici);
        
        log.info("Activated kullanici: {}", id);
        return toDTO(kullanici);
    }

    @Transactional
    public KullaniciDTO deactivateKullanici(Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));

        // SISTEM_ADMIN koruması
        if (kullanici.getRol() == KullaniciRol.SISTEM_ADMIN) {
            throw new tr.gov.tuketbir.exception.BusinessException("Sistem yöneticisi hesabı pasifleştirilemez!");
        }

        kullanici.setIsActive(false);
        kullanici.setDurum(KullaniciDurum.PASIF);
        kullanici = kullaniciRepository.save(kullanici);
        
        log.info("Deactivated kullanici: {}", id);
        return toDTO(kullanici);
    }

    @Transactional
    public void resetPassword(Long id) {
        Kullanici kullanici = kullaniciRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı", "id", id));

        // Rastgele şifre oluştur
        String newPassword = UUID.randomUUID().toString().substring(0, 12);
        kullanici.setSifre(passwordEncoder.encode(newPassword));
        kullanici.setSonSifreDegisim(LocalDateTime.now());
        kullaniciRepository.save(kullanici);

        // TODO: E-posta ile yeni şifreyi gönder
        log.info("Password reset for kullanici: {} - New password: {}", id, newPassword);
    }

    public List<KullaniciDTO> getKullanicilarByBirlik(Long birlikId) {
        List<Kullanici> kullanicilar = kullaniciRepository.findByBirlikId(birlikId);
        return kullanicilar.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private KullaniciDTO toDTO(Kullanici kullanici) {
        return KullaniciDTO.builder()
                .id(kullanici.getId())
                .kullaniciAdi(kullanici.getKullaniciAdi())
                .ad(kullanici.getAd())
                .soyad(kullanici.getSoyad())
                .adSoyad(kullanici.getAd() + " " + kullanici.getSoyad())
                .email(kullanici.getEmail())
                .telefon(kullanici.getTelefon())
                .rol(kullanici.getRol())
                .yetkiler(kullanici.getRol().getYetkiler())
                .durum(kullanici.getDurum())
                .birlikId(kullanici.getBirlik() != null ? kullanici.getBirlik().getId() : null)
                .birlikAdi(kullanici.getBirlik() != null ? kullanici.getBirlik().getBirlikAdi() : null)
                .twoFactorEnabled(kullanici.getIkiFaktorAktif())
                .sonGirisTarihi(kullanici.getSonGirisZamani())
                .sifreDegistirilmeTarihi(kullanici.getSonSifreDegisim())
                .aktif(kullanici.getIsActive())
                .createdAt(kullanici.getCreatedAt())
                .updatedAt(kullanici.getUpdatedAt())
                .build();
    }
}
