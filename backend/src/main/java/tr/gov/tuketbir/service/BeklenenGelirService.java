package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.domain.entity.AidatDonemi;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.enums.BirlikTipi;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.dto.beklenengelir.*;
import tr.gov.tuketbir.exception.ResourceNotFoundException;
import tr.gov.tuketbir.repository.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Beklenen Gelir Hesaplama Service
 * 
 * İş Kuralları:
 * 
 * 1. Alt Birlik Beklenen Gelir:
 *    Beklenen Gelir = (Aktif Üye Sayısı) × (Aidat Tutarı)
 *    - Üyenin ödeme yapıp yapmaması beklenen geliri etkilemez
 *    - Sadece aktif üyeler hesaba katılır
 *    - Hesaplama dönem bazlıdır
 * 
 * 2. Merkez Birlik Beklenen Gelir:
 *    Merkez Beklenen Gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)
 *    - Her alt birlik için tanımlı pay oranı (%) kullanılır
 *    - Toplam tüm alt birliklerden hesaplanır
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BeklenenGelirService {

    private final BirlikRepository birlikRepository;
    private final AidatDonemiRepository aidatDonemiRepository;
    private final AidatRepository aidatRepository;
    private final UyeRepository uyeRepository;

    /**
     * Alt birlik için beklenen gelir hesapla
     * 
     * Formül: Beklenen Gelir = Aktif Üye Sayısı × Aidat Tutarı
     * 
     * @param birlikId Alt birlik ID
     * @param donemId Aidat dönemi ID
     * @param merkezPayOrani Merkez birlik pay oranı (opsiyonel, merkez döneminden alınır)
     */
    public AltBirlikBeklenenGelirDTO hesaplaAltBirlikBeklenenGelir(Long birlikId, Long donemId, BigDecimal merkezPayOrani) {
        log.debug("Calculating expected revenue for birlik: {}, donem: {}", birlikId, donemId);

        Birlik birlik = birlikRepository.findById(birlikId)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", birlikId));

        AidatDonemi donem = aidatDonemiRepository.findById(donemId)
                .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", donemId));

        // Aktif üye sayısı
        Long aktifUyeCount = uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.AKTIF);
        int aktifUyeSayisi = aktifUyeCount != null ? aktifUyeCount.intValue() : 0;

        // Aidat tutarı (dönemde tanımlı)
        BigDecimal aidatTutari = donem.getTutar();
        if (aidatTutari == null) {
            aidatTutari = BigDecimal.ZERO;
        }

        // Beklenen gelir = aktifUyeSayisi × aidatTutari
        BigDecimal beklenenGelir = aidatTutari.multiply(BigDecimal.valueOf(aktifUyeSayisi));

        // Tahsil edilen tutar
        BigDecimal tahsilEdilen = aidatRepository.sumTahsilatByBirlikAndDonem(birlikId, donemId);
        if (tahsilEdilen == null) {
            tahsilEdilen = BigDecimal.ZERO;
        }

        // Kalan tutar
        BigDecimal kalanTutar = beklenenGelir.subtract(tahsilEdilen);

        // Tahsilat oranı
        BigDecimal tahsilatOrani = BigDecimal.ZERO;
        if (beklenenGelir.compareTo(BigDecimal.ZERO) > 0) {
            tahsilatOrani = tahsilEdilen.multiply(BigDecimal.valueOf(100))
                    .divide(beklenenGelir, 2, RoundingMode.HALF_UP);
        }

        // Merkez pay oranı (parametre olarak gelmezse dönemden al)
        BigDecimal payOrani = merkezPayOrani;
        if (payOrani == null) {
            payOrani = BigDecimal.ZERO;
        }

        // Merkeze beklenen pay
        BigDecimal merkezeBeklenenPay = beklenenGelir.multiply(payOrani)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return AltBirlikBeklenenGelirDTO.builder()
                .birlikId(birlik.getId())
                .birlikKodu(birlik.getBirlikKodu())
                .birlikAdi(birlik.getBirlikAdi())
                .donemId(donem.getId())
                .donemAdi(donem.getDonemAdi())
                .donemYili(donem.getYil())
                .aidatTutari(aidatTutari)
                .aktifUyeSayisi(aktifUyeSayisi)
                .beklenenGelir(beklenenGelir)
                .tahsilEdilen(tahsilEdilen)
                .kalanTutar(kalanTutar)
                .tahsilatOrani(tahsilatOrani)
                .merkezPayOrani(payOrani)
                .merkezeBeklenenPay(merkezeBeklenenPay)
                .merekzeAktarilanPay(BigDecimal.ZERO) // TODO: Merkeze aktarım takibi
                .build();
    }

    /**
     * Alt birlik için beklenen gelir hesapla (overload - merkez pay oranı olmadan)
     */
    public AltBirlikBeklenenGelirDTO hesaplaAltBirlikBeklenenGelir(Long birlikId, Long donemId) {
        return hesaplaAltBirlikBeklenenGelir(birlikId, donemId, null);
    }

    /**
     * Merkez birlik için beklenen gelir hesapla
     * 
     * Formül: Merkez Beklenen Gelir = Σ (Alt Birlik Beklenen Geliri × Pay Oranı)
     * 
     * Pay oranı dönemde tanımlanır (merkezPayOrani alanı)
     */
    public MerkezBeklenenGelirDTO hesaplaMerkezBeklenenGelir(Long donemId) {
        log.debug("Calculating expected revenue for merkez birlik, donem: {}", donemId);

        AidatDonemi donem = aidatDonemiRepository.findById(donemId)
                .orElseThrow(() -> new ResourceNotFoundException("Aidat Dönemi", "id", donemId));

        // Dönemde tanımlı merkez pay oranını al
        BigDecimal merkezPayOrani = donem.getMerkezPayOrani();
        if (merkezPayOrani == null) {
            merkezPayOrani = BigDecimal.ZERO;
        }

        // Tüm alt birlikleri getir
        List<Birlik> altBirlikler = birlikRepository.findByBirlikTipi(BirlikTipi.ALT_BIRLIK);

        List<AltBirlikBeklenenGelirDTO> altBirlikDetaylari = new ArrayList<>();
        BigDecimal altBirliklerToplamBeklenenGelir = BigDecimal.ZERO;
        BigDecimal merkezBeklenenGelir = BigDecimal.ZERO;
        BigDecimal merkezTahsilEdilen = BigDecimal.ZERO;
        int toplamAktifUyeSayisi = 0;

        for (Birlik altBirlik : altBirlikler) {
            // Alt birlik beklenen gelirini hesapla, merkez pay oranını geçir
            AltBirlikBeklenenGelirDTO altBirlikGelir = hesaplaAltBirlikBeklenenGelir(altBirlik.getId(), donemId, merkezPayOrani);
            altBirlikDetaylari.add(altBirlikGelir);

            // Toplamları hesapla
            altBirliklerToplamBeklenenGelir = altBirliklerToplamBeklenenGelir.add(altBirlikGelir.getBeklenenGelir());
            merkezBeklenenGelir = merkezBeklenenGelir.add(altBirlikGelir.getMerkezeBeklenenPay());
            merkezTahsilEdilen = merkezTahsilEdilen.add(
                    altBirlikGelir.getMerekzeAktarilanPay() != null ? altBirlikGelir.getMerekzeAktarilanPay() : BigDecimal.ZERO
            );
            toplamAktifUyeSayisi += altBirlikGelir.getAktifUyeSayisi();
        }

        // Kalan tutar
        BigDecimal merkezKalanTutar = merkezBeklenenGelir.subtract(merkezTahsilEdilen);

        // Tahsilat oranı
        BigDecimal tahsilatOrani = BigDecimal.ZERO;
        if (merkezBeklenenGelir.compareTo(BigDecimal.ZERO) > 0) {
            tahsilatOrani = merkezTahsilEdilen.multiply(BigDecimal.valueOf(100))
                    .divide(merkezBeklenenGelir, 2, RoundingMode.HALF_UP);
        }

        return MerkezBeklenenGelirDTO.builder()
                .donemId(donem.getId())
                .donemAdi(donem.getDonemAdi())
                .donemYili(donem.getYil())
                .merkezPayOrani(merkezPayOrani) // Dönemde tanımlı pay oranı
                .toplamAltBirlikSayisi(altBirlikler.size())
                .toplamAktifUyeSayisi(toplamAktifUyeSayisi)
                .altBirliklerToplamBeklenenGelir(altBirliklerToplamBeklenenGelir)
                .merkezBeklenenGelir(merkezBeklenenGelir)
                .merkezTahsilEdilen(merkezTahsilEdilen)
                .merkezKalanTutar(merkezKalanTutar)
                .tahsilatOrani(tahsilatOrani)
                .altBirlikDetaylari(altBirlikDetaylari)
                .build();
    }

    /**
     * Belirli bir yıl için tüm dönemlerin beklenen gelir özeti
     */
    public BeklenenGelirOzetDTO hesaplaYillikBeklenenGelirOzeti(Long birlikId, Integer yil) {
        log.debug("Calculating yearly expected revenue for birlik: {}, yil: {}", birlikId, yil);

        Birlik birlik = birlikRepository.findById(birlikId)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", birlikId));

        // Yıla ait dönemleri getir
        List<AidatDonemi> donemler = aidatDonemiRepository.findByYilAndBirlikIdOrBirlikIdIsNull(yil, birlikId);

        List<DonemBazliBeklenenGelirDTO> donemKirilimi = new ArrayList<>();
        BigDecimal toplamBeklenenGelir = BigDecimal.ZERO;
        BigDecimal toplamTahsilEdilen = BigDecimal.ZERO;

        Long aktifUyeCount = uyeRepository.countByBirlikIdAndDurum(birlikId, UyeDurum.AKTIF);
        int aktifUyeSayisi = aktifUyeCount != null ? aktifUyeCount.intValue() : 0;

        for (AidatDonemi donem : donemler) {
            AltBirlikBeklenenGelirDTO donemGelir = hesaplaAltBirlikBeklenenGelir(birlikId, donem.getId());

            DonemBazliBeklenenGelirDTO donemDTO = DonemBazliBeklenenGelirDTO.builder()
                    .donemId(donem.getId())
                    .donemAdi(donem.getDonemAdi())
                    .donemKodu(donem.getDonemKodu())
                    .yil(donem.getYil())
                    .beklenenGelir(donemGelir.getBeklenenGelir())
                    .tahsilEdilen(donemGelir.getTahsilEdilen())
                    .kalanTutar(donemGelir.getKalanTutar())
                    .tahsilatOrani(donemGelir.getTahsilatOrani())
                    .aktifUyeSayisi(donemGelir.getAktifUyeSayisi())
                    .aidatTutari(donemGelir.getAidatTutari())
                    .build();

            donemKirilimi.add(donemDTO);
            toplamBeklenenGelir = toplamBeklenenGelir.add(donemGelir.getBeklenenGelir());
            toplamTahsilEdilen = toplamTahsilEdilen.add(donemGelir.getTahsilEdilen());
        }

        BigDecimal kalanTutar = toplamBeklenenGelir.subtract(toplamTahsilEdilen);
        BigDecimal tahsilatOrani = BigDecimal.ZERO;
        if (toplamBeklenenGelir.compareTo(BigDecimal.ZERO) > 0) {
            tahsilatOrani = toplamTahsilEdilen.multiply(BigDecimal.valueOf(100))
                    .divide(toplamBeklenenGelir, 2, RoundingMode.HALF_UP);
        }

        Long toplamUyeCount = uyeRepository.countByBirlikId(birlikId);
        return BeklenenGelirOzetDTO.builder()
                .birlikId(birlik.getId())
                .birlikAdi(birlik.getBirlikAdi())
                .birlikTipi(birlik.getBirlikTipi().name())
                .toplamUyeSayisi(toplamUyeCount != null ? toplamUyeCount.intValue() : 0)
                .aktifUyeSayisi(aktifUyeSayisi)
                .beklenenGelir(toplamBeklenenGelir)
                .tahsilEdilen(toplamTahsilEdilen)
                .kalanTutar(kalanTutar)
                .tahsilatOrani(tahsilatOrani)
                .merkezPayOrani(birlik.getMerkezPayOrani())
                .merkezeBeklenenPay(toplamBeklenenGelir.multiply(birlik.getMerkezPayOrani() != null ? birlik.getMerkezPayOrani() : BigDecimal.ZERO)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .donemKirilimi(donemKirilimi)
                .build();
    }

    /**
     * Dashboard için özet beklenen gelir bilgisi
     */
    public BeklenenGelirOzetDTO getDashboardOzet(Long birlikId, Long donemId) {
        Birlik birlik = birlikRepository.findById(birlikId)
                .orElseThrow(() -> new ResourceNotFoundException("Birlik", "id", birlikId));

        if (BirlikTipi.MERKEZ.equals(birlik.getBirlikTipi())) {
            // Merkez birlik için tüm alt birliklerden özet
            MerkezBeklenenGelirDTO merkezGelir = hesaplaMerkezBeklenenGelir(donemId);
            return BeklenenGelirOzetDTO.builder()
                    .donemId(merkezGelir.getDonemId())
                    .donemAdi(merkezGelir.getDonemAdi())
                    .donemYili(merkezGelir.getDonemYili())
                    .birlikId(birlikId)
                    .birlikAdi(birlik.getBirlikAdi())
                    .birlikTipi(birlik.getBirlikTipi().name())
                    .toplamUyeSayisi(merkezGelir.getToplamAktifUyeSayisi())
                    .aktifUyeSayisi(merkezGelir.getToplamAktifUyeSayisi())
                    .beklenenGelir(merkezGelir.getMerkezBeklenenGelir())
                    .tahsilEdilen(merkezGelir.getMerkezTahsilEdilen())
                    .kalanTutar(merkezGelir.getMerkezKalanTutar())
                    .tahsilatOrani(merkezGelir.getTahsilatOrani())
                    .build();
        } else {
            // Alt birlik için kendi beklenen geliri
            AltBirlikBeklenenGelirDTO altBirlikGelir = hesaplaAltBirlikBeklenenGelir(birlikId, donemId);
            return BeklenenGelirOzetDTO.builder()
                    .donemId(altBirlikGelir.getDonemId())
                    .donemAdi(altBirlikGelir.getDonemAdi())
                    .donemYili(altBirlikGelir.getDonemYili())
                    .birlikId(birlikId)
                    .birlikAdi(birlik.getBirlikAdi())
                    .birlikTipi(birlik.getBirlikTipi().name())
                    .aktifUyeSayisi(altBirlikGelir.getAktifUyeSayisi())
                    .birimAidatTutari(altBirlikGelir.getAidatTutari())
                    .beklenenGelir(altBirlikGelir.getBeklenenGelir())
                    .tahsilEdilen(altBirlikGelir.getTahsilEdilen())
                    .kalanTutar(altBirlikGelir.getKalanTutar())
                    .tahsilatOrani(altBirlikGelir.getTahsilatOrani())
                    .merkezPayOrani(altBirlikGelir.getMerkezPayOrani())
                    .merkezeBeklenenPay(altBirlikGelir.getMerkezeBeklenenPay())
                    .build();
        }
    }
}
