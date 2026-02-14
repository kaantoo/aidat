package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.KullaniciDurum;
import tr.gov.tuketbir.repository.KullaniciRepository;

/**
 * Custom UserDetailsService implementation
 * Spring Security authentication için kullanılır
 * Kullanici entity'si UserDetails implement ettiği için direkt döndürülür
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final KullaniciRepository kullaniciRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Kullanici kullanici = kullaniciRepository.findByKullaniciAdi(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        // Kullanıcı aktif mi kontrolü
        if (kullanici.getDurum() != KullaniciDurum.AKTIF) {
            throw new UsernameNotFoundException("Kullanıcı hesabı aktif değil: " + username);
        }

        // Kullanici zaten UserDetails implement ediyor, direkt döndür
        return kullanici;
    }
}
