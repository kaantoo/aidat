package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.*;
import tr.gov.tuketbir.domain.enums.KararDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;
import tr.gov.tuketbir.dto.common.PagedResponse;
import tr.gov.tuketbir.dto.toplanti.*;
import tr.gov.tuketbir.exception.BusinessException;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Toplantı ve Karar Yönetimi Service
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ToplantiService {

    private final ToplantiRepository toplantiRepository;
    private final KararRepository kararRepository;
    private final ToplantiKatilimciRepository katilimciRepository;
    private final BirlikRepository birlikRepository;
    private final UyeRepository uyeRepository;
    private final AuditLogService auditLogService;

    // ======================= Toplantı İşlemleri =======================

    /**
     * Toplantıları filtreli listele
     */
    @Transactional(readOnly = true)
    public PagedResponse<ToplantiDTO> getToplantilar(
            Long birlikId, ToplantiTuru toplantiTuru, ToplantiDurumu durum,
            LocalDate baslangicTarihi, LocalDate bitisTarihi,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("toplantiTarihi").descending());
        Page<Toplanti> toplantiPage = toplantiRepository.findByFilters(
                birlikId, toplantiTuru, durum, baslangicTarihi, bitisTarihi, pageable);

        List<ToplantiDTO> content = toplantiPage.getContent().stream()
                .map(this::toToplantiDTO)
                .collect(Collectors.toList());

        return PagedResponse.<ToplantiDTO>builder()
                .content(content)
                .totalElements(toplantiPage.getTotalElements())
                .totalPages(toplantiPage.getTotalPages())
                .size(toplantiPage.getSize())
                .page(toplantiPage.getNumber())
                .first(toplantiPage.isFirst())
                .last(toplantiPage.isLast())
                .build();
    }

    /**
     * Toplantı detayı (kararlar ve katılımcılarla birlikte)
     */
    @Transactional(readOnly = true)
    public ToplantiDTO getToplantiById(Long id) {
        Toplanti toplanti = toplantiRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı: " + id));
        return toToplantiDetailDTO(toplanti);
    }

    /**
     * Yeni toplantı oluştur
     */
    public ToplantiDTO createToplanti(ToplantiCreateRequest request) {
        Birlik birlik = birlikRepository.findById(request.getBirlikId())
                .orElseThrow(() -> new ResourceNotFoundException("Birlik bulunamadı: " + request.getBirlikId()));

        String toplantiNo = generateToplantiNo(request.getToplantiTarihi());

        Toplanti toplanti = Toplanti.builder()
                .toplantiNo(toplantiNo)
                .birlik(birlik)
                .baslik(request.getBaslik())
                .toplantiTuru(request.getToplantiTuru())
                .durum(ToplantiDurumu.PLANLANMIS)
                .toplantiTarihi(request.getToplantiTarihi())
                .baslangicSaati(request.getBaslangicSaati())
                .bitisSaati(request.getBitisSaati())
                .yer(request.getYer())
                .gundem(request.getGundem())
                .aciklama(request.getAciklama())
                .build();

        toplanti.setTenantId(birlik.getTenantId());
        toplanti = toplantiRepository.save(toplanti);

        // Kararlar ekle
        if (request.getKararlar() != null && !request.getKararlar().isEmpty()) {
            int sira = 1;
            for (KararCreateRequest kararReq : request.getKararlar()) {
                addKararToToplanti(toplanti, kararReq, sira++);
            }
        }

        // Katılımcılar ekle
        if (request.getKatilimcilar() != null && !request.getKatilimcilar().isEmpty()) {
            for (KatilimciCreateRequest katReq : request.getKatilimcilar()) {
                addKatilimciToToplanti(toplanti, katReq);
            }
        }

        log.info("Toplantı oluşturuldu: {} - {}", toplantiNo, request.getBaslik());
        auditLogService.log("TOPLANTI_OLUSTURMA", "Toplantı oluşturuldu: " + toplantiNo + " - " + request.getBaslik(), birlik.getId());

        return toToplantiDetailDTO(toplanti);
    }

    /**
     * Toplantı güncelle
     */
    public ToplantiDTO updateToplanti(Long id, ToplantiUpdateRequest request) {
        Toplanti toplanti = toplantiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı: " + id));

        if (!toplanti.getIsActive()) {
            throw new BusinessException("Silinmiş toplantı güncellenemez");
        }

        if (request.getBaslik() != null) toplanti.setBaslik(request.getBaslik());
        if (request.getToplantiTuru() != null) toplanti.setToplantiTuru(request.getToplantiTuru());
        if (request.getDurum() != null) toplanti.setDurum(request.getDurum());
        if (request.getToplantiTarihi() != null) toplanti.setToplantiTarihi(request.getToplantiTarihi());
        if (request.getBaslangicSaati() != null) toplanti.setBaslangicSaati(request.getBaslangicSaati());
        if (request.getBitisSaati() != null) toplanti.setBitisSaati(request.getBitisSaati());
        if (request.getYer() != null) toplanti.setYer(request.getYer());
        if (request.getGundem() != null) toplanti.setGundem(request.getGundem());
        if (request.getAciklama() != null) toplanti.setAciklama(request.getAciklama());

        toplanti = toplantiRepository.save(toplanti);
        log.info("Toplantı güncellendi: {}", toplanti.getToplantiNo());
        auditLogService.log("TOPLANTI_GUNCELLEME", "Toplantı güncellendi: " + toplanti.getToplantiNo(), toplanti.getBirlik().getId());

        return toToplantiDTO(toplanti);
    }

    /**
     * Toplantı sil (soft delete)
     */
    public void deleteToplanti(Long id) {
        Toplanti toplanti = toplantiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı: " + id));
        toplanti.softDelete();
        toplantiRepository.save(toplanti);
        log.info("Toplantı silindi: {}", toplanti.getToplantiNo());
        auditLogService.log("TOPLANTI_SILME", "Toplantı silindi: " + toplanti.getToplantiNo(), toplanti.getBirlik().getId());
    }

    // ======================= Karar İşlemleri =======================

    /**
     * Toplantıya karar ekle
     */
    public KararDTO addKarar(Long toplantiId, KararCreateRequest request) {
        Toplanti toplanti = toplantiRepository.findById(toplantiId)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı: " + toplantiId));

        int nextSira = (int) kararRepository.countByToplantiId(toplantiId) + 1;
        Karar karar = addKararToToplanti(toplanti, request, request.getKararSirasi() != null ? request.getKararSirasi() : nextSira);

        log.info("Karar eklendi: {} - Toplantı: {}", karar.getKararNo(), toplanti.getToplantiNo());
        return toKararDTO(karar);
    }

    /**
     * Karar güncelle
     */
    public KararDTO updateKarar(Long kararId, KararCreateRequest request) {
        Karar karar = kararRepository.findById(kararId)
                .orElseThrow(() -> new ResourceNotFoundException("Karar bulunamadı: " + kararId));

        if (request.getBaslik() != null) karar.setBaslik(request.getBaslik());
        if (request.getKararMetni() != null) karar.setKararMetni(request.getKararMetni());
        if (request.getDurum() != null) karar.setDurum(request.getDurum());
        if (request.getOyBirligi() != null) karar.setOyBirligi(request.getOyBirligi());
        if (request.getKabulOyu() != null) karar.setKabulOyu(request.getKabulOyu());
        if (request.getRedOyu() != null) karar.setRedOyu(request.getRedOyu());
        if (request.getCekimserOyu() != null) karar.setCekimserOyu(request.getCekimserOyu());
        if (request.getSorumlu() != null) karar.setSorumlu(request.getSorumlu());
        if (request.getNotlar() != null) karar.setNotlar(request.getNotlar());

        karar = kararRepository.save(karar);
        log.info("Karar güncellendi: {}", karar.getKararNo());
        return toKararDTO(karar);
    }

    /**
     * Karar sil
     */
    public void deleteKarar(Long kararId) {
        Karar karar = kararRepository.findById(kararId)
                .orElseThrow(() -> new ResourceNotFoundException("Karar bulunamadı: " + kararId));
        karar.softDelete();
        kararRepository.save(karar);
        log.info("Karar silindi: {}", karar.getKararNo());
    }

    /**
     * Toplantının kararlarını getir
     */
    @Transactional(readOnly = true)
    public List<KararDTO> getKararlarByToplanti(Long toplantiId) {
        return kararRepository.findByToplantiIdAndIsActiveTrueOrderByKararSirasiAsc(toplantiId)
                .stream()
                .map(this::toKararDTO)
                .collect(Collectors.toList());
    }

    // ======================= Katılımcı İşlemleri =======================

    /**
     * Toplantıya katılımcı ekle
     */
    public KatilimciDTO addKatilimci(Long toplantiId, KatilimciCreateRequest request) {
        Toplanti toplanti = toplantiRepository.findById(toplantiId)
                .orElseThrow(() -> new ResourceNotFoundException("Toplantı bulunamadı: " + toplantiId));

        ToplantiKatilimci katilimci = addKatilimciToToplanti(toplanti, request);
        log.info("Katılımcı eklendi: {} - Toplantı: {}", katilimci.getAdSoyad(), toplanti.getToplantiNo());
        return toKatilimciDTO(katilimci);
    }

    /**
     * Katılımcı sil
     */
    public void removeKatilimci(Long katilimciId) {
        ToplantiKatilimci katilimci = katilimciRepository.findById(katilimciId)
                .orElseThrow(() -> new ResourceNotFoundException("Katılımcı bulunamadı: " + katilimciId));
        katilimci.softDelete();
        katilimciRepository.save(katilimci);
    }

    /**
     * Toplantının katılımcılarını getir
     */
    @Transactional(readOnly = true)
    public List<KatilimciDTO> getKatilimcilarByToplanti(Long toplantiId) {
        return katilimciRepository.findByToplantiIdAndIsActiveTrue(toplantiId)
                .stream()
                .map(this::toKatilimciDTO)
                .collect(Collectors.toList());
    }

    // ======================= Helper Methods =======================

    private Karar addKararToToplanti(Toplanti toplanti, KararCreateRequest request, int sira) {
        String kararNo = generateKararNo(toplanti.getToplantiNo(), sira);

        Karar karar = Karar.builder()
                .kararNo(kararNo)
                .toplanti(toplanti)
                .kararSirasi(sira)
                .baslik(request.getBaslik())
                .kararMetni(request.getKararMetni())
                .durum(request.getDurum() != null ? request.getDurum() : KararDurumu.KABUL_EDILDI)
                .oyBirligi(request.getOyBirligi() != null ? request.getOyBirligi() : true)
                .kabulOyu(request.getKabulOyu())
                .redOyu(request.getRedOyu())
                .cekimserOyu(request.getCekimserOyu())
                .sorumlu(request.getSorumlu())
                .notlar(request.getNotlar())
                .build();

        karar.setTenantId(toplanti.getTenantId());
        return kararRepository.save(karar);
    }

    private ToplantiKatilimci addKatilimciToToplanti(Toplanti toplanti, KatilimciCreateRequest request) {
        String adSoyad = request.getAdSoyad();
        Uye uye = null;

        if (request.getUyeId() != null) {
            uye = uyeRepository.findById(request.getUyeId()).orElse(null);
            if (uye != null && (adSoyad == null || adSoyad.isBlank())) {
                adSoyad = uye.getAd() + " " + uye.getSoyad();
            }
        }

        if (adSoyad == null || adSoyad.isBlank()) {
            throw new BusinessException("Katılımcı adı zorunludur");
        }

        ToplantiKatilimci katilimci = ToplantiKatilimci.builder()
                .toplanti(toplanti)
                .uye(uye)
                .adSoyad(adSoyad)
                .gorev(request.getGorev())
                .katildi(request.getKatildi() != null ? request.getKatildi() : true)
                .mazeret(request.getMazeret())
                .build();

        katilimci.setTenantId(toplanti.getTenantId());
        return katilimciRepository.save(katilimci);
    }

    private String generateToplantiNo(LocalDate tarih) {
        String prefix = "TOP-" + tarih.format(DateTimeFormatter.ofPattern("yyyy")) + "-";
        Integer maxNo = toplantiRepository.findMaxToplantiNoByPrefix(prefix);
        int nextNo = (maxNo != null ? maxNo : 0) + 1;
        return prefix + String.format("%04d", nextNo);
    }

    private String generateKararNo(String toplantiNo, int sira) {
        return "KRR-" + toplantiNo.replace("TOP-", "") + "-" + String.format("%02d", sira);
    }

    // ======================= DTO Mapping =======================

    private ToplantiDTO toToplantiDTO(Toplanti t) {
        return ToplantiDTO.builder()
                .id(t.getId())
                .toplantiNo(t.getToplantiNo())
                .birlikId(t.getBirlik() != null ? t.getBirlik().getId() : null)
                .birlikAdi(t.getBirlik() != null ? t.getBirlik().getBirlikAdi() : null)
                .baslik(t.getBaslik())
                .toplantiTuru(t.getToplantiTuru())
                .durum(t.getDurum())
                .toplantiTarihi(t.getToplantiTarihi())
                .baslangicSaati(t.getBaslangicSaati())
                .bitisSaati(t.getBitisSaati())
                .yer(t.getYer())
                .gundem(t.getGundem())
                .aciklama(t.getAciklama())
                .kararSayisi(t.getKararSayisi())
                .katilimciSayisi(t.getKatilimciSayisi())
                .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)
                .updatedAt(t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null)
                .build();
    }

    private ToplantiDTO toToplantiDetailDTO(Toplanti t) {
        ToplantiDTO dto = toToplantiDTO(t);
        if (t.getKararlar() != null) {
            dto.setKararlar(t.getKararlar().stream()
                    .filter(k -> k.getIsActive())
                    .map(this::toKararDTO)
                    .collect(Collectors.toList()));
        }
        if (t.getKatilimcilar() != null) {
            dto.setKatilimcilar(t.getKatilimcilar().stream()
                    .filter(k -> k.getIsActive())
                    .map(this::toKatilimciDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    private KararDTO toKararDTO(Karar k) {
        return KararDTO.builder()
                .id(k.getId())
                .kararNo(k.getKararNo())
                .toplantiId(k.getToplanti() != null ? k.getToplanti().getId() : null)
                .toplantiNo(k.getToplanti() != null ? k.getToplanti().getToplantiNo() : null)
                .kararSirasi(k.getKararSirasi())
                .baslik(k.getBaslik())
                .kararMetni(k.getKararMetni())
                .durum(k.getDurum())
                .oyBirligi(k.getOyBirligi())
                .kabulOyu(k.getKabulOyu())
                .redOyu(k.getRedOyu())
                .cekimserOyu(k.getCekimserOyu())
                .sorumlu(k.getSorumlu())
                .notlar(k.getNotlar())
                .createdAt(k.getCreatedAt() != null ? k.getCreatedAt().toString() : null)
                .build();
    }

    private KatilimciDTO toKatilimciDTO(ToplantiKatilimci k) {
        return KatilimciDTO.builder()
                .id(k.getId())
                .toplantiId(k.getToplanti() != null ? k.getToplanti().getId() : null)
                .uyeId(k.getUye() != null ? k.getUye().getId() : null)
                .uyeNo(k.getUye() != null ? k.getUye().getUyeNo() : null)
                .adSoyad(k.getAdSoyad())
                .gorev(k.getGorev())
                .katildi(k.getKatildi())
                .mazeret(k.getMazeret())
                .imzaladi(k.getImzaladi())
                .build();
    }
}
