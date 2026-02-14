package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.dto.rapor.*;
import tr.gov.tuketbir.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Rapor Service - Raporlama iş mantığı
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RaporService {

    private final UyeRepository uyeRepository;
    private final BirlikRepository birlikRepository;
    private final AidatRepository aidatRepository;
    private final GelirGiderRepository gelirGiderRepository;

    public DashboardStatsDTO getDashboardStats(Long birlikId) {
        DashboardStatsDTO.DashboardStatsDTOBuilder builder = DashboardStatsDTO.builder();

        // Üye istatistikleri
        builder.toplamUyeSayisi(uyeRepository.count());

        // Birlik istatistikleri
        builder.toplamBirlikSayisi(birlikRepository.count());

        // Aidat istatistikleri - gerçek veriler (native query ile)
        builder.toplamAidatSayisi(aidatRepository.count());
        
        // Toplam tahakkuk ve tahsilat - repository query'leri ile
        BigDecimal toplamTahakkuk = aidatRepository.sumAllTahakkuk();
        BigDecimal toplamTahsilat = aidatRepository.sumAllOdenenTutar();
        
        builder.toplamTahakkuk(toplamTahakkuk != null ? toplamTahakkuk : BigDecimal.ZERO);
        builder.toplamTahsilat(toplamTahsilat != null ? toplamTahsilat : BigDecimal.ZERO);
        
        // Tahsilat oranı
        if (toplamTahakkuk != null && toplamTahakkuk.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal oran = toplamTahsilat.multiply(BigDecimal.valueOf(100))
                    .divide(toplamTahakkuk, 2, java.math.RoundingMode.HALF_UP);
            builder.tahsilatOrani(oran);
        } else {
            builder.tahsilatOrani(BigDecimal.ZERO);
        }
        
        // Aylık gelir/gider - bu ayın verileri
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        BigDecimal aylikGelir = gelirGiderRepository.sumGelirByYilAy(currentYear, currentMonth);
        BigDecimal aylikGider = gelirGiderRepository.sumGiderByYilAy(currentYear, currentMonth);
        
        builder.aylikGelir(aylikGelir != null ? aylikGelir : BigDecimal.ZERO);
        builder.aylikGider(aylikGider != null ? aylikGider : BigDecimal.ZERO);
        builder.aylikNet(aylikGelir != null && aylikGider != null ? aylikGelir.subtract(aylikGider) : BigDecimal.ZERO);

        return builder.build();
    }

    public List<BirlikTahsilatRaporDTO> getBirlikTahsilatRaporu(Integer yil, Long donemId) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public List<DonemAidatRaporDTO> getDonemAidatRaporu(Integer yil, Long birlikId) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public GelirGiderRaporDTO getGelirGiderOzet(Integer yil, Integer ay, Long birlikId) {
        BigDecimal toplamGelir;
        BigDecimal toplamGider;
        
        if (yil != null && ay != null) {
            if (birlikId != null) {
                toplamGelir = gelirGiderRepository.sumGelirByBirlikYilAy(birlikId, yil, ay);
                toplamGider = gelirGiderRepository.sumGiderByBirlikYilAy(birlikId, yil, ay);
            } else {
                toplamGelir = gelirGiderRepository.sumGelirByYilAy(yil, ay);
                toplamGider = gelirGiderRepository.sumGiderByYilAy(yil, ay);
            }
        } else {
            toplamGelir = gelirGiderRepository.sumAllGelir();
            toplamGider = gelirGiderRepository.sumAllGider();
        }
        
        if (toplamGelir == null) toplamGelir = BigDecimal.ZERO;
        if (toplamGider == null) toplamGider = BigDecimal.ZERO;
        
        return GelirGiderRaporDTO.builder()
                .yil(yil != null ? yil : LocalDate.now().getYear())
                .ay(ay)
                .toplamGelir(toplamGelir)
                .toplamGider(toplamGider)
                .netDurum(toplamGelir.subtract(toplamGider))
                .build();
    }

    public List<GelirGiderRaporDTO> getGelirGiderTrend(Integer yil, Long birlikId) {
        List<GelirGiderRaporDTO> trend = new ArrayList<>();
        int currentYear = yil != null ? yil : LocalDate.now().getYear();
        
        for (int ay = 1; ay <= 12; ay++) {
            BigDecimal gelir;
            BigDecimal gider;
            
            if (birlikId != null) {
                gelir = gelirGiderRepository.sumGelirByBirlikYilAy(birlikId, currentYear, ay);
                gider = gelirGiderRepository.sumGiderByBirlikYilAy(birlikId, currentYear, ay);
            } else {
                gelir = gelirGiderRepository.sumGelirByYilAy(currentYear, ay);
                gider = gelirGiderRepository.sumGiderByYilAy(currentYear, ay);
            }
            
            if (gelir == null) gelir = BigDecimal.ZERO;
            if (gider == null) gider = BigDecimal.ZERO;
            
            trend.add(GelirGiderRaporDTO.builder()
                    .yil(currentYear)
                    .ay(ay)
                    .toplamGelir(gelir)
                    .toplamGider(gider)
                    .netDurum(gelir.subtract(gider))
                    .build());
        }
        return trend;
    }

    public byte[] exportTahsilatRaporu(Integer yil, Long birlikId, Long donemId) {
        // TODO: Implement Excel export
        return new byte[0];
    }

    public byte[] exportGelirGiderRaporu(Integer yil, Long birlikId) {
        // TODO: Implement Excel export
        return new byte[0];
    }

    public Object getUyeDagilim(Long birlikId) {
        Map<String, Object> dagilim = new HashMap<>();
        dagilim.put("toplamUye", uyeRepository.count());
        dagilim.put("aktifUye", 0L);
        dagilim.put("pasifUye", 0L);
        return dagilim;
    }

    public List<Map<String, Object>> getAylikTahsilatGrafik(Long birlikId, Integer yil) {
        // TODO: Implement
        return new ArrayList<>();
    }

    public List<Map<String, Object>> getIlBazindaUyeGrafik() {
        // TODO: Implement
        return new ArrayList<>();
    }

    public UyeOzetRaporDTO getUyeOzetRaporu(Long birlikId) {
        UyeOzetRaporDTO.UyeOzetRaporDTOBuilder builder = UyeOzetRaporDTO.builder();
        
        builder.toplamUyeSayisi(uyeRepository.count());
        builder.aktifUyeSayisi(0L);
        builder.pasifUyeSayisi(0L);
        builder.yeniUyeSayisi(0L);
        
        return builder.build();
    }

    public byte[] generateExcelReport(String reportType, Long birlikId, Integer yil, Long donemId) {
        // TODO: Implement Excel generation
        return new byte[0];
    }

    public byte[] generatePdfReport(String reportType, Long birlikId, Integer yil, Long donemId) {
        // TODO: Implement PDF generation
        return new byte[0];
    }

    public List<BirlikIstatistikDTO> getBirlikIstatistikleri() {
        List<BirlikIstatistikDTO> istatistikler = new ArrayList<>();
        
        birlikRepository.findAll().forEach(birlik -> {
            if (birlik.getIsActive()) {
                // N+1 sorunu önleme - count sorgusu kullan
                Long uyeSayisi = birlikRepository.countAktifUyelerByBirlikId(birlik.getId());
                
                // Bekleyen aidat tutarı (kalan borç)
                BigDecimal bekleyenAidat = aidatRepository.sumKalanBorcByBirlikId(birlik.getId());
                
                // Tahsilat oranı hesapla
                BigDecimal tahakkuk = aidatRepository.sumTahakkukByBirlik(birlik.getId());
                BigDecimal tahsilat = aidatRepository.sumOdenenTutarByBirlik(birlik.getId());
                BigDecimal tahsilOrani = BigDecimal.ZERO;
                if (tahakkuk != null && tahakkuk.compareTo(BigDecimal.ZERO) > 0 && tahsilat != null) {
                    tahsilOrani = tahsilat.multiply(BigDecimal.valueOf(100))
                            .divide(tahakkuk, 2, java.math.RoundingMode.HALF_UP);
                }
                
                BirlikIstatistikDTO dto = BirlikIstatistikDTO.builder()
                        .birlikId(birlik.getId())
                        .birlikAdi(birlik.getBirlikAdi())
                        .uyeSayisi(uyeSayisi != null ? uyeSayisi.intValue() : 0)
                        .aktifUyeSayisi(uyeSayisi != null ? uyeSayisi.intValue() : 0)
                        .bekleyenAidatTutari(bekleyenAidat != null ? bekleyenAidat : BigDecimal.ZERO)
                        .tahsilOrani(tahsilOrani)
                        .sonTahsilatTarihi(null)
                        .build();
                istatistikler.add(dto);
            }
        });
        
        return istatistikler;
    }

    public List<AidatRaporDTO> getAidatRaporu(Long birlikId, Integer yil) {
        List<AidatRaporDTO> raporlar = new ArrayList<>();
        
        var birlikler = birlikId != null 
            ? birlikRepository.findById(birlikId).map(List::of).orElse(Collections.emptyList())
            : birlikRepository.findAll();
        
        for (var birlik : birlikler) {
            if (!birlik.getIsActive()) continue;
            
            // N+1 sorunu önleme - count sorgusu kullan
            Long uyeSayisi = birlikRepository.countAktifUyelerByBirlikId(birlik.getId());
            
            // Aidat tutarları
            BigDecimal tahakkuk = aidatRepository.sumTahakkukByBirlik(birlik.getId());
            BigDecimal tahsilat = aidatRepository.sumOdenenTutarByBirlik(birlik.getId());
            BigDecimal kalanBorc = aidatRepository.sumKalanBorcByBirlikId(birlik.getId());
            
            // Tahsilat oranı
            BigDecimal tahsilOrani = BigDecimal.ZERO;
            if (tahakkuk != null && tahakkuk.compareTo(BigDecimal.ZERO) > 0 && tahsilat != null) {
                tahsilOrani = tahsilat.multiply(BigDecimal.valueOf(100))
                        .divide(tahakkuk, 2, java.math.RoundingMode.HALF_UP);
            }
            
            AidatRaporDTO rapor = AidatRaporDTO.builder()
                    .birlikId(birlik.getId())
                    .birlikAdi(birlik.getBirlikAdi())
                    .yil(yil != null ? yil : java.time.Year.now().getValue())
                    .donem("Yıllık")
                    .toplamUye(uyeSayisi != null ? uyeSayisi : 0L)
                    .odeyenUye(0L)
                    .tahakkukTutari(tahakkuk != null ? tahakkuk : BigDecimal.ZERO)
                    .tahsilatTutari(tahsilat != null ? tahsilat : BigDecimal.ZERO)
                    .tahsilatOrani(tahsilOrani)
                    .bekleyenTutar(kalanBorc != null ? kalanBorc : BigDecimal.ZERO)
                    .build();
            raporlar.add(rapor);
        }
        
        return raporlar;
    }

    public byte[] exportAidatRaporuExcel(Long birlikId, Integer yil) {
        // TODO: Implement Excel export with Apache POI
        return new byte[0];
    }

    public List<TahsilatTrendiDTO> getTahsilatTrendi(Long birlikId, Boolean aylik) {
        List<TahsilatTrendiDTO> trend = new ArrayList<>();
        
        String[] aylar = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", 
                         "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        
        for (String ay : aylar) {
            trend.add(TahsilatTrendiDTO.builder()
                    .donem(ay)
                    .tutar(BigDecimal.ZERO)
                    .build());
        }
        
        return trend;
    }

    public UyeDistribusyonDTO getUyeDistribusyonu() {
        return UyeDistribusyonDTO.builder()
                .ilBazinda(new ArrayList<>())
                .durumBazinda(List.of(
                    UyeDistribusyonDTO.DurumDistribusyonDTO.builder()
                        .durum("Aktif")
                        .sayi(uyeRepository.count())
                        .build(),
                    UyeDistribusyonDTO.DurumDistribusyonDTO.builder()
                        .durum("Pasif")
                        .sayi(0L)
                        .build()
                ))
                .tipBazinda(new ArrayList<>())
                .build();
    }

    public GelirGiderDetayDTO getGelirGiderDetay(Long birlikId, String baslangicTarihi, String bitisTarihi) {
        // Toplam gelir ve gider
        BigDecimal toplamGelir = gelirGiderRepository.sumAllGelir();
        BigDecimal toplamGider = gelirGiderRepository.sumAllGider();
        
        if (toplamGelir == null) toplamGelir = BigDecimal.ZERO;
        if (toplamGider == null) toplamGider = BigDecimal.ZERO;
        
        // Aylık trend
        List<GelirGiderDetayDTO.AylikTrendDTO> aylikTrend = new ArrayList<>();
        String[] aylar = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", 
                         "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        
        int currentYear = LocalDate.now().getYear();
        
        for (int i = 0; i < 12; i++) {
            BigDecimal gelir;
            BigDecimal gider;
            
            if (birlikId != null) {
                gelir = gelirGiderRepository.sumGelirByBirlikYilAy(birlikId, currentYear, i + 1);
                gider = gelirGiderRepository.sumGiderByBirlikYilAy(birlikId, currentYear, i + 1);
            } else {
                gelir = gelirGiderRepository.sumGelirByYilAy(currentYear, i + 1);
                gider = gelirGiderRepository.sumGiderByYilAy(currentYear, i + 1);
            }
            
            aylikTrend.add(GelirGiderDetayDTO.AylikTrendDTO.builder()
                    .ay(aylar[i])
                    .gelir(gelir != null ? gelir : BigDecimal.ZERO)
                    .gider(gider != null ? gider : BigDecimal.ZERO)
                    .build());
        }
        
        return GelirGiderDetayDTO.builder()
                .toplamGelir(toplamGelir)
                .toplamGider(toplamGider)
                .netDurum(toplamGelir.subtract(toplamGider))
                .aylikTrend(aylikTrend)
                .build();
    }

    public byte[] exportDashboardPdf() {
        // TODO: Implement PDF export
        return new byte[0];
    }
}
