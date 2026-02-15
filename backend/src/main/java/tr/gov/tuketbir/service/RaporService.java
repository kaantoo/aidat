package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.gov.tuketbir.dto.rapor.*;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.exception.BusinessException;
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
    private final AidatDonemiRepository aidatDonemiRepository;
    private final TahsilatRepository tahsilatRepository;
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
        List<BirlikTahsilatRaporDTO> raporlar = new ArrayList<>();

        birlikRepository.findAll().forEach(birlik -> {
            if (!birlik.getIsActive()) return;

            Long uyeSayisi = birlikRepository.countAktifUyelerByBirlikId(birlik.getId());
            BigDecimal tahakkuk = aidatRepository.sumTahakkukByBirlik(birlik.getId());
            BigDecimal tahsilat = aidatRepository.sumOdenenTutarByBirlik(birlik.getId());
            BigDecimal bakiye = aidatRepository.sumKalanBorcByBirlikId(birlik.getId());

            Long odenmis = aidatRepository.countByBirlikIdAndDurum(birlik.getId(), tr.gov.tuketbir.domain.enums.AidatDurum.ODENDI);
            Long bekleyen = aidatRepository.countByBirlikIdAndDurum(birlik.getId(), tr.gov.tuketbir.domain.enums.AidatDurum.BEKLIYOR);
            Long geciken = aidatRepository.countByBirlikIdAndDurum(birlik.getId(), tr.gov.tuketbir.domain.enums.AidatDurum.GECIKTI);
            long toplamAidat = (odenmis != null ? odenmis : 0) + (bekleyen != null ? bekleyen : 0) + (geciken != null ? geciken : 0);

            BigDecimal tahsilOrani = BigDecimal.ZERO;
            if (tahakkuk != null && tahakkuk.compareTo(BigDecimal.ZERO) > 0 && tahsilat != null) {
                tahsilOrani = tahsilat.multiply(BigDecimal.valueOf(100))
                        .divide(tahakkuk, 2, java.math.RoundingMode.HALF_UP);
            }

            raporlar.add(BirlikTahsilatRaporDTO.builder()
                    .birlikId(birlik.getId())
                    .birlikKodu(birlik.getBirlikKodu())
                    .birlikAdi(birlik.getBirlikAdi())
                    .ilAdi(birlik.getIlKodu())
                    .uyeSayisi(uyeSayisi != null ? uyeSayisi.intValue() : 0)
                    .toplamAidatSayisi((int) toplamAidat)
                    .odenmisAidatSayisi(odenmis != null ? odenmis.intValue() : 0)
                    .bekleyenAidatSayisi(bekleyen != null ? bekleyen.intValue() : 0)
                    .gecikmisnAidatSayisi(geciken != null ? geciken.intValue() : 0)
                    .toplamTahakkuk(tahakkuk != null ? tahakkuk : BigDecimal.ZERO)
                    .toplamTahsilat(tahsilat != null ? tahsilat : BigDecimal.ZERO)
                    .toplamBakiye(bakiye != null ? bakiye : BigDecimal.ZERO)
                    .tahsilatOrani(tahsilOrani)
                    .build());
        });

        return raporlar;
    }

    public List<DonemAidatRaporDTO> getDonemAidatRaporu(Integer yil, Long birlikId) {
        List<DonemAidatRaporDTO> raporlar = new ArrayList<>();
        List<tr.gov.tuketbir.domain.entity.AidatDonemi> donemler;

        if (yil != null) {
            donemler = aidatDonemiRepository.findByYil(yil);
        } else {
            donemler = aidatDonemiRepository.findByDonemAktifTrue();
        }

        for (var donem : donemler) {
            List<tr.gov.tuketbir.domain.entity.Aidat> aidatlar;
            if (birlikId != null) {
                aidatlar = aidatRepository.findByAidatDonemiIdWithRelations(donem.getId()).stream()
                        .filter(a -> a.getBirlik() != null && a.getBirlik().getId().equals(birlikId))
                        .toList();
            } else {
                aidatlar = aidatRepository.findByAidatDonemiId(donem.getId());
            }

            long odenmis = aidatlar.stream().filter(a -> a.getAidatDurum() == tr.gov.tuketbir.domain.enums.AidatDurum.ODENDI).count();
            long kismi = aidatlar.stream().filter(a -> a.getAidatDurum() == tr.gov.tuketbir.domain.enums.AidatDurum.KISMI_ODENDI).count();
            long bekleyen = aidatlar.stream().filter(a -> a.getAidatDurum() == tr.gov.tuketbir.domain.enums.AidatDurum.BEKLIYOR).count();
            long geciken = aidatlar.stream().filter(a -> a.getAidatDurum() == tr.gov.tuketbir.domain.enums.AidatDurum.GECIKTI).count();

            BigDecimal tahakkuk = aidatlar.stream()
                    .map(a -> a.getTahakkukTutari() != null ? a.getTahakkukTutari() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tahsilat = aidatlar.stream()
                    .map(a -> a.getOdenenTutar() != null ? a.getOdenenTutar() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal tahsilOrani = BigDecimal.ZERO;
            if (tahakkuk.compareTo(BigDecimal.ZERO) > 0) {
                tahsilOrani = tahsilat.multiply(BigDecimal.valueOf(100))
                        .divide(tahakkuk, 2, java.math.RoundingMode.HALF_UP);
            }

            raporlar.add(DonemAidatRaporDTO.builder()
                    .donemId(donem.getId())
                    .donemAdi(donem.getDonemAdi())
                    .yil(donem.getYil())
                    .ay(null)
                    .aidatTutari(donem.getTutar())
                    .toplamUyeSayisi(aidatlar.size())
                    .toplamAidatSayisi(aidatlar.size())
                    .odenmisAidatSayisi((int) odenmis)
                    .kısmiOdenmisAidatSayisi((int) kismi)
                    .odenmemisAidatSayisi((int) bekleyen)
                    .gecikmisnAidatSayisi((int) geciken)
                    .toplamTahakkuk(tahakkuk)
                    .toplamTahsilat(tahsilat)
                    .toplamBakiye(tahakkuk.subtract(tahsilat))
                    .tahsilatOrani(tahsilOrani)
                    .build());
        }
        return raporlar;
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
        List<BirlikTahsilatRaporDTO> rapor = getBirlikTahsilatRaporu(yil, donemId);

        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Tahsilat Raporu");
            var headerStyle = createHeaderStyle(workbook);

            var headerRow = sheet.createRow(0);
            String[] headers = {"Birlik Kodu", "Birlik Adı", "Üye Sayısı", "Toplam Aidat", "Ödenmiş", "Bekleyen", "Geciken", "Tahakkuk", "Tahsilat", "Bakiye", "Tahsilat Oranı (%)"};
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (var r : rapor) {
                var row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getBirlikKodu() != null ? r.getBirlikKodu() : "");
                row.createCell(1).setCellValue(r.getBirlikAdi() != null ? r.getBirlikAdi() : "");
                row.createCell(2).setCellValue(r.getUyeSayisi() != null ? r.getUyeSayisi() : 0);
                row.createCell(3).setCellValue(r.getToplamAidatSayisi() != null ? r.getToplamAidatSayisi() : 0);
                row.createCell(4).setCellValue(r.getOdenmisAidatSayisi() != null ? r.getOdenmisAidatSayisi() : 0);
                row.createCell(5).setCellValue(r.getBekleyenAidatSayisi() != null ? r.getBekleyenAidatSayisi() : 0);
                row.createCell(6).setCellValue(r.getGecikmisnAidatSayisi() != null ? r.getGecikmisnAidatSayisi() : 0);
                row.createCell(7).setCellValue(r.getToplamTahakkuk() != null ? r.getToplamTahakkuk().doubleValue() : 0);
                row.createCell(8).setCellValue(r.getToplamTahsilat() != null ? r.getToplamTahsilat().doubleValue() : 0);
                row.createCell(9).setCellValue(r.getToplamBakiye() != null ? r.getToplamBakiye().doubleValue() : 0);
                row.createCell(10).setCellValue(r.getTahsilatOrani() != null ? r.getTahsilatOrani().doubleValue() : 0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            var os = new java.io.ByteArrayOutputStream();
            workbook.write(os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting tahsilat raporu to Excel", e);
            throw new BusinessException("Excel oluşturulurken hata: " + e.getMessage());
        }
    }

    public byte[] exportGelirGiderRaporu(Integer yil, Long birlikId) {
        List<GelirGiderRaporDTO> trend = getGelirGiderTrend(yil, birlikId);

        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Gelir Gider Raporu");
            var headerStyle = createHeaderStyle(workbook);

            var headerRow = sheet.createRow(0);
            String[] headers = {"Yıl", "Ay", "Toplam Gelir", "Toplam Gider", "Net Durum"};
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (var r : trend) {
                var row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getYil());
                row.createCell(1).setCellValue(r.getAy() != null ? r.getAy() : 0);
                row.createCell(2).setCellValue(r.getToplamGelir() != null ? r.getToplamGelir().doubleValue() : 0);
                row.createCell(3).setCellValue(r.getToplamGider() != null ? r.getToplamGider().doubleValue() : 0);
                row.createCell(4).setCellValue(r.getNetDurum() != null ? r.getNetDurum().doubleValue() : 0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            var os = new java.io.ByteArrayOutputStream();
            workbook.write(os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting gelir gider raporu to Excel", e);
            throw new BusinessException("Excel oluşturulurken hata: " + e.getMessage());
        }
    }

    public Object getUyeDagilim(Long birlikId) {
        Map<String, Object> dagilim = new HashMap<>();
        Long toplamUye = uyeRepository.count();
        Long aktifUye = uyeRepository.countByUyeDurum(UyeDurum.AKTIF);
        Long pasifUye = uyeRepository.countByUyeDurum(UyeDurum.PASIF);
        Long askiya = uyeRepository.countByUyeDurum(UyeDurum.ASKIYA_ALINMIS);
        Long ihrac = uyeRepository.countByUyeDurum(UyeDurum.IHRAC_EDILMIS);
        dagilim.put("toplamUye", toplamUye);
        dagilim.put("aktifUye", aktifUye != null ? aktifUye : 0L);
        dagilim.put("pasifUye", pasifUye != null ? pasifUye : 0L);
        dagilim.put("askiyaAlinan", askiya != null ? askiya : 0L);
        dagilim.put("ihracEdilen", ihrac != null ? ihrac : 0L);
        return dagilim;
    }

    public List<Map<String, Object>> getAylikTahsilatGrafik(Long birlikId, Integer yil) {
        List<Map<String, Object>> grafik = new ArrayList<>();
        int currentYear = yil != null ? yil : LocalDate.now().getYear();
        String[] aylar = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
                         "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};

        // Birlik bazlı aylık tahsilat özeti
        if (birlikId != null) {
            List<Object[]> ozet = tahsilatRepository.getAylikTahsilatOzeti(birlikId);
            Map<String, BigDecimal> aylikMap = new HashMap<>();
            for (Object[] row : ozet) {
                int rowYear = ((Number) row[0]).intValue();
                int rowMonth = ((Number) row[1]).intValue();
                if (rowYear == currentYear) {
                    aylikMap.put(aylar[rowMonth - 1], (BigDecimal) row[2]);
                }
            }
            for (String ay : aylar) {
                Map<String, Object> item = new HashMap<>();
                item.put("ay", ay);
                item.put("tutar", aylikMap.getOrDefault(ay, BigDecimal.ZERO));
                grafik.add(item);
            }
        } else {
            // Tüm tahsilatlar için aylık gelir/gider bazlı
            for (int i = 0; i < 12; i++) {
                BigDecimal gelir = gelirGiderRepository.sumGelirByYilAy(currentYear, i + 1);
                Map<String, Object> item = new HashMap<>();
                item.put("ay", aylar[i]);
                item.put("tutar", gelir != null ? gelir : BigDecimal.ZERO);
                grafik.add(item);
            }
        }

        return grafik;
    }

    public List<Map<String, Object>> getIlBazindaUyeGrafik() {
        List<Map<String, Object>> grafik = new ArrayList<>();
        List<Object[]> ilBazinda = uyeRepository.countAktifUyelerByIl();

        for (Object[] row : ilBazinda) {
            Map<String, Object> item = new HashMap<>();
            item.put("ilKodu", row[0]);
            item.put("uyeSayisi", row[1]);
            grafik.add(item);
        }

        return grafik;
    }

    public UyeOzetRaporDTO getUyeOzetRaporu(Long birlikId) {
        UyeOzetRaporDTO.UyeOzetRaporDTOBuilder builder = UyeOzetRaporDTO.builder();

        Long toplamUye = uyeRepository.count();
        Long aktifUye = uyeRepository.countByUyeDurum(UyeDurum.AKTIF);
        Long pasifUye = uyeRepository.countByUyeDurum(UyeDurum.PASIF);

        // Son 30 günde katılan yeni üyeler
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Long yeniUye = (long) uyeRepository.findByKatilimTarihiBetween(thirtyDaysAgo, LocalDate.now()).size();

        builder.toplamUyeSayisi(toplamUye);
        builder.aktifUyeSayisi(aktifUye != null ? aktifUye : 0L);
        builder.pasifUyeSayisi(pasifUye != null ? pasifUye : 0L);
        builder.yeniUyeSayisi(yeniUye);

        return builder.build();
    }

    public byte[] generateExcelReport(String reportType, Long birlikId, Integer yil, Long donemId) {
        switch (reportType) {
            case "tahsilat":
                return exportTahsilatRaporu(yil, birlikId, donemId);
            case "gelir-gider":
                return exportGelirGiderRaporu(yil, birlikId);
            case "aidat":
                return exportAidatRaporuExcel(birlikId, yil);
            default:
                throw new BusinessException("Geçersiz rapor tipi: " + reportType);
        }
    }

    public byte[] generatePdfReport(String reportType, Long birlikId, Integer yil, Long donemId) {
        // iText kullanarak basit PDF oluştur
        try (var os = new java.io.ByteArrayOutputStream()) {
            var writer = new com.itextpdf.kernel.pdf.PdfWriter(os);
            var pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            var document = new com.itextpdf.layout.Document(pdf);
            
            // Başlık
            document.add(new com.itextpdf.layout.element.Paragraph("TÜKETBİR Raporu")
                    .setFontSize(18).setBold());
            document.add(new com.itextpdf.layout.element.Paragraph("Rapor Tipi: " + reportType)
                    .setFontSize(12));
            document.add(new com.itextpdf.layout.element.Paragraph("Oluşturulma Tarihi: " + LocalDate.now())
                    .setFontSize(10));
            document.add(new com.itextpdf.layout.element.Paragraph(""));

            // Rapor içeriği
            switch (reportType) {
                case "tahsilat": {
                    var rapor = getBirlikTahsilatRaporu(yil, donemId);
                    var table = new com.itextpdf.layout.element.Table(new float[]{2, 3, 1, 2, 2, 2, 1});
                    table.addHeaderCell("Birlik Kodu");
                    table.addHeaderCell("Birlik Adı");
                    table.addHeaderCell("Üye");
                    table.addHeaderCell("Tahakkuk");
                    table.addHeaderCell("Tahsilat");
                    table.addHeaderCell("Bakiye");
                    table.addHeaderCell("Oran(%)");
                    for (var r : rapor) {
                        table.addCell(r.getBirlikKodu() != null ? r.getBirlikKodu() : "");
                        table.addCell(r.getBirlikAdi() != null ? r.getBirlikAdi() : "");
                        table.addCell(String.valueOf(r.getUyeSayisi()));
                        table.addCell(r.getToplamTahakkuk() != null ? r.getToplamTahakkuk().toString() : "0");
                        table.addCell(r.getToplamTahsilat() != null ? r.getToplamTahsilat().toString() : "0");
                        table.addCell(r.getToplamBakiye() != null ? r.getToplamBakiye().toString() : "0");
                        table.addCell(r.getTahsilatOrani() != null ? r.getTahsilatOrani().toString() : "0");
                    }
                    document.add(table);
                    break;
                }
                case "gelir-gider": {
                    var trend = getGelirGiderTrend(yil, birlikId);
                    var table = new com.itextpdf.layout.element.Table(new float[]{1, 1, 2, 2, 2});
                    table.addHeaderCell("Yıl");
                    table.addHeaderCell("Ay");
                    table.addHeaderCell("Gelir");
                    table.addHeaderCell("Gider");
                    table.addHeaderCell("Net");
                    for (var r : trend) {
                        table.addCell(String.valueOf(r.getYil()));
                        table.addCell(r.getAy() != null ? String.valueOf(r.getAy()) : "");
                        table.addCell(r.getToplamGelir() != null ? r.getToplamGelir().toString() : "0");
                        table.addCell(r.getToplamGider() != null ? r.getToplamGider().toString() : "0");
                        table.addCell(r.getNetDurum() != null ? r.getNetDurum().toString() : "0");
                    }
                    document.add(table);
                    break;
                }
                default:
                    document.add(new com.itextpdf.layout.element.Paragraph("Rapor verisi bulunamadı."));
            }

            document.close();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF report", e);
            throw new BusinessException("PDF oluşturulurken hata: " + e.getMessage());
        }
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
        List<AidatRaporDTO> rapor = getAidatRaporu(birlikId, yil);

        try (var workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Aidat Raporu");
            var headerStyle = createHeaderStyle(workbook);

            var headerRow = sheet.createRow(0);
            String[] headers = {"Birlik Adı", "Yıl", "Dönem", "Toplam Üye", "Ödeyen Üye", "Tahakkuk", "Tahsilat", "Tahsilat Oranı (%)", "Bekleyen"};
            for (int i = 0; i < headers.length; i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (var r : rapor) {
                var row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getBirlikAdi() != null ? r.getBirlikAdi() : "");
                row.createCell(1).setCellValue(r.getYil());
                row.createCell(2).setCellValue(r.getDonem() != null ? r.getDonem() : "");
                row.createCell(3).setCellValue(r.getToplamUye() != null ? r.getToplamUye() : 0);
                row.createCell(4).setCellValue(r.getOdeyenUye() != null ? r.getOdeyenUye() : 0);
                row.createCell(5).setCellValue(r.getTahakkukTutari() != null ? r.getTahakkukTutari().doubleValue() : 0);
                row.createCell(6).setCellValue(r.getTahsilatTutari() != null ? r.getTahsilatTutari().doubleValue() : 0);
                row.createCell(7).setCellValue(r.getTahsilatOrani() != null ? r.getTahsilatOrani().doubleValue() : 0);
                row.createCell(8).setCellValue(r.getBekleyenTutar() != null ? r.getBekleyenTutar().doubleValue() : 0);
            }

            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);

            var os = new java.io.ByteArrayOutputStream();
            workbook.write(os);
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting aidat raporu to Excel", e);
            throw new BusinessException("Excel oluşturulurken hata: " + e.getMessage());
        }
    }

    public List<TahsilatTrendiDTO> getTahsilatTrendi(Long birlikId, Boolean aylik) {
        List<TahsilatTrendiDTO> trend = new ArrayList<>();
        
        String[] aylar = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", 
                         "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};
        
        int currentYear = LocalDate.now().getYear();

        if (birlikId != null) {
            List<Object[]> ozet = tahsilatRepository.getAylikTahsilatOzeti(birlikId);
            Map<Integer, BigDecimal> aylikMap = new HashMap<>();
            for (Object[] row : ozet) {
                int rowYear = ((Number) row[0]).intValue();
                int rowMonth = ((Number) row[1]).intValue();
                if (rowYear == currentYear) {
                    aylikMap.put(rowMonth, (BigDecimal) row[2]);
                }
            }
            for (int i = 0; i < 12; i++) {
                trend.add(TahsilatTrendiDTO.builder()
                        .donem(aylar[i])
                        .tutar(aylikMap.getOrDefault(i + 1, BigDecimal.ZERO))
                        .build());
            }
        } else {
            for (int i = 0; i < 12; i++) {
                BigDecimal gelir = gelirGiderRepository.sumGelirByYilAy(currentYear, i + 1);
                trend.add(TahsilatTrendiDTO.builder()
                        .donem(aylar[i])
                        .tutar(gelir != null ? gelir : BigDecimal.ZERO)
                        .build());
            }
        }
        
        return trend;
    }

    public UyeDistribusyonDTO getUyeDistribusyonu() {
        // Il bazında dağılım
        List<UyeDistribusyonDTO.IlDistribusyonDTO> ilDist = new ArrayList<>();
        List<Object[]> ilBazinda = uyeRepository.countAktifUyelerByIl();
        for (Object[] row : ilBazinda) {
            ilDist.add(UyeDistribusyonDTO.IlDistribusyonDTO.builder()
                    .il(row[0] != null ? row[0].toString() : "Bilinmiyor")
                    .sayi(((Number) row[1]).longValue())
                    .build());
        }

        // Durum bazında dağılım
        Long aktif = uyeRepository.countByUyeDurum(UyeDurum.AKTIF);
        Long pasif = uyeRepository.countByUyeDurum(UyeDurum.PASIF);
        Long askiya = uyeRepository.countByUyeDurum(UyeDurum.ASKIYA_ALINMIS);
        Long ihrac = uyeRepository.countByUyeDurum(UyeDurum.IHRAC_EDILMIS);

        List<UyeDistribusyonDTO.DurumDistribusyonDTO> durumDist = List.of(
            UyeDistribusyonDTO.DurumDistribusyonDTO.builder().durum("Aktif").sayi(aktif != null ? aktif : 0L).build(),
            UyeDistribusyonDTO.DurumDistribusyonDTO.builder().durum("Pasif").sayi(pasif != null ? pasif : 0L).build(),
            UyeDistribusyonDTO.DurumDistribusyonDTO.builder().durum("Askıya Alınmış").sayi(askiya != null ? askiya : 0L).build(),
            UyeDistribusyonDTO.DurumDistribusyonDTO.builder().durum("İhraç Edilmiş").sayi(ihrac != null ? ihrac : 0L).build()
        );

        // Birlik bazında dağılım (tip bazında olarak)
        List<UyeDistribusyonDTO.TipDistribusyonDTO> tipDist = new ArrayList<>();
        List<Object[]> birlikBazinda = uyeRepository.countAktifUyelerByBirlik();
        for (Object[] row : birlikBazinda) {
            tipDist.add(UyeDistribusyonDTO.TipDistribusyonDTO.builder()
                    .tip(row[0] != null ? row[0].toString() : "Bilinmiyor")
                    .sayi(((Number) row[1]).longValue())
                    .build());
        }

        return UyeDistribusyonDTO.builder()
                .ilBazinda(ilDist)
                .durumBazinda(durumDist)
                .tipBazinda(tipDist)
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
        try (var os = new java.io.ByteArrayOutputStream()) {
            var writer = new com.itextpdf.kernel.pdf.PdfWriter(os);
            var pdf = new com.itextpdf.kernel.pdf.PdfDocument(writer);
            var document = new com.itextpdf.layout.Document(pdf);

            document.add(new com.itextpdf.layout.element.Paragraph("TÜKETBİR Dashboard Raporu")
                    .setFontSize(18).setBold());
            document.add(new com.itextpdf.layout.element.Paragraph("Oluşturulma Tarihi: " + LocalDate.now())
                    .setFontSize(10));
            document.add(new com.itextpdf.layout.element.Paragraph(""));

            // Dashboard Stats
            DashboardStatsDTO stats = getDashboardStats(null);
            document.add(new com.itextpdf.layout.element.Paragraph("Genel İstatistikler").setFontSize(14).setBold());

            var table = new com.itextpdf.layout.element.Table(new float[]{3, 2});
            table.addHeaderCell("Metrik");
            table.addHeaderCell("Değer");
            table.addCell("Toplam Üye Sayısı");
            table.addCell(String.valueOf(stats.getToplamUyeSayisi()));
            table.addCell("Toplam Birlik Sayısı");
            table.addCell(String.valueOf(stats.getToplamBirlikSayisi()));
            table.addCell("Toplam Tahakkuk");
            table.addCell(stats.getToplamTahakkuk() != null ? stats.getToplamTahakkuk().toString() + " ₺" : "0 ₺");
            table.addCell("Toplam Tahsilat");
            table.addCell(stats.getToplamTahsilat() != null ? stats.getToplamTahsilat().toString() + " ₺" : "0 ₺");
            table.addCell("Tahsilat Oranı");
            table.addCell(stats.getTahsilatOrani() != null ? "%" + stats.getTahsilatOrani().toString() : "%0");
            table.addCell("Aylık Gelir");
            table.addCell(stats.getAylikGelir() != null ? stats.getAylikGelir().toString() + " ₺" : "0 ₺");
            table.addCell("Aylık Gider");
            table.addCell(stats.getAylikGider() != null ? stats.getAylikGider().toString() + " ₺" : "0 ₺");
            table.addCell("Aylık Net");
            table.addCell(stats.getAylikNet() != null ? stats.getAylikNet().toString() + " ₺" : "0 ₺");
            document.add(table);

            document.close();
            return os.toByteArray();
        } catch (Exception e) {
            log.error("Error exporting dashboard to PDF", e);
            throw new BusinessException("PDF oluşturulurken hata: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}
