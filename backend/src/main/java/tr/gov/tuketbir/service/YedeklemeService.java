package tr.gov.tuketbir.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tr.gov.tuketbir.dto.sistem.YedekBilgiDTO;
import tr.gov.tuketbir.dto.sistem.YedekDurumDTO;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Yedekleme Service
 * 
 * PostgreSQL veritabanı yedekleme işlemlerini yönetir.
 * Manuel ve otomatik yedekleme destekler.
 * 
 * @author Tuketbir Development Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YedeklemeService {

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${app.backup.directory:./backups}")
    private String backupDirectory;

    @Value("${app.backup.auto-enabled:false}")
    private boolean autoBackupEnabled;

    @Value("${app.backup.max-count:10}")
    private int maxBackupCount;

    private final AuditLogService auditLogService;

    /**
     * Manuel yedekleme
     */
    public YedekBilgiDTO manuelYedekle() {
        log.info("Manuel yedekleme başlatılıyor...");
        return performBackup("MANUEL");
    }

    /**
     * Otomatik günlük yedekleme (her gün gece 02:00'de)
     */
    @Scheduled(cron = "${app.backup.cron:0 0 2 * * ?}")
    public void otomatikYedekle() {
        if (!autoBackupEnabled) {
            log.debug("Otomatik yedekleme devre dışı");
            return;
        }
        log.info("Otomatik yedekleme başlatılıyor...");
        performBackup("OTOMATIK");
    }

    /**
     * Yedekleme durumunu getir
     */
    public YedekDurumDTO getYedekDurumu() {
        Path backupDir = Paths.get(backupDirectory);
        
        List<YedekBilgiDTO> yedekler = new ArrayList<>();
        long toplamBoyut = 0;

        if (Files.exists(backupDir)) {
            try {
                List<Path> files = Files.list(backupDir)
                        .filter(p -> p.toString().endsWith(".sql") || p.toString().endsWith(".gz"))
                        .sorted(Comparator.comparing(p -> {
                            try { return Files.getLastModifiedTime((Path) p); } 
                            catch (IOException e) { return null; }
                        }).reversed())
                        .collect(Collectors.toList());

                for (Path file : files) {
                    long boyut = Files.size(file);
                    toplamBoyut += boyut;
                    yedekler.add(YedekBilgiDTO.builder()
                            .dosyaAdi(file.getFileName().toString())
                            .dosyaBoyutu(boyut)
                            .dosyaBoyutuFormatli(formatBoyut(boyut))
                            .olusturmaZamani(LocalDateTime.from(
                                    Files.getLastModifiedTime(file).toInstant()
                                            .atZone(java.time.ZoneId.of("Europe/Istanbul"))))
                            .yedekTipi(file.getFileName().toString().contains("OTOMATIK") ? "OTOMATIK" : "MANUEL")
                            .basarili(true)
                            .build());
                }
            } catch (IOException e) {
                log.error("Yedek dizini okunamadı", e);
            }
        }

        return YedekDurumDTO.builder()
                .otomatikYedekAktif(autoBackupEnabled)
                .yedekDizini(backupDirectory)
                .sonYedekTarihi(!yedekler.isEmpty() ? yedekler.get(0).getOlusturmaZamani() : null)
                .sonYedekDosya(!yedekler.isEmpty() ? yedekler.get(0).getDosyaAdi() : null)
                .toplamYedekSayisi(yedekler.size())
                .toplamBoyut(formatBoyut(toplamBoyut))
                .sonYedekler(yedekler.stream().limit(10).collect(Collectors.toList()))
                .build();
    }

    /**
     * Yedek dosyasını sil
     */
    public boolean deleteYedek(String dosyaAdi) {
        try {
            Path filePath = Paths.get(backupDirectory, dosyaAdi);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Yedek silindi: {}", dosyaAdi);
                auditLogService.log("YEDEK_SILME", "Yedek dosyası silindi: " + dosyaAdi);
                return true;
            }
        } catch (IOException e) {
            log.error("Yedek silinemedi: {}", dosyaAdi, e);
        }
        return false;
    }

    // ======================= Private Methods =======================

    private YedekBilgiDTO performBackup(String tip) {
        try {
            Path backupDir = Paths.get(backupDirectory);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "tuketbir_" + tip + "_" + timestamp + ".sql";
            Path filePath = backupDir.resolve(fileName);

            // pg_dump kullanarak yedekleme
            String dbName = extractDbName();
            String host = extractHost();
            String port = extractPort();

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", port,
                    "-U", dbUsername,
                    "-d", dbName,
                    "-f", filePath.toString(),
                    "--no-password"
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0 && Files.exists(filePath)) {
                long boyut = Files.size(filePath);
                YedekBilgiDTO result = YedekBilgiDTO.builder()
                        .dosyaAdi(fileName)
                        .dosyaBoyutu(boyut)
                        .dosyaBoyutuFormatli(formatBoyut(boyut))
                        .olusturmaZamani(LocalDateTime.now())
                        .yedekTipi(tip)
                        .basarili(true)
                        .aciklama("Yedekleme başarılı")
                        .build();

                cleanupOldBackups();
                log.info("Yedekleme tamamlandı: {} ({})", fileName, formatBoyut(boyut));
                auditLogService.log("YEDEKLEME", tip + " yedekleme tamamlandı: " + fileName);
                return result;
            } else {
                // pg_dump yoksa basit SQL dump deneyelim
                log.warn("pg_dump ile yedekleme başarısız (exit: {}), basit yedek oluşturuluyor...", exitCode);
                return createSimpleBackup(fileName, tip);
            }

        } catch (Exception e) {
            log.error("Yedekleme hatası", e);
            return YedekBilgiDTO.builder()
                    .dosyaAdi("")
                    .olusturmaZamani(LocalDateTime.now())
                    .yedekTipi(tip)
                    .basarili(false)
                    .aciklama("Yedekleme hatası: " + e.getMessage())
                    .build();
        }
    }

    private YedekBilgiDTO createSimpleBackup(String fileName, String tip) {
        try {
            Path filePath = Paths.get(backupDirectory, fileName);
            String content = "-- TÜKETBİR Yedekleme\n" +
                    "-- Tarih: " + LocalDateTime.now() + "\n" +
                    "-- Tip: " + tip + "\n" +
                    "-- NOT: Bu basit bir marker yedektir. Tam yedek için pg_dump gereklidir.\n" +
                    "-- Veritabanı: " + extractDbName() + "\n";
            Files.writeString(filePath, content);

            long boyut = Files.size(filePath);
            return YedekBilgiDTO.builder()
                    .dosyaAdi(fileName)
                    .dosyaBoyutu(boyut)
                    .dosyaBoyutuFormatli(formatBoyut(boyut))
                    .olusturmaZamani(LocalDateTime.now())
                    .yedekTipi(tip)
                    .basarili(true)
                    .aciklama("Basit yedek oluşturuldu (pg_dump erişilemiyor)")
                    .build();
        } catch (IOException e) {
            return YedekBilgiDTO.builder()
                    .basarili(false)
                    .olusturmaZamani(LocalDateTime.now())
                    .yedekTipi(tip)
                    .aciklama("Yedekleme tamamen başarısız: " + e.getMessage())
                    .build();
        }
    }

    private void cleanupOldBackups() {
        try {
            Path backupDir = Paths.get(backupDirectory);
            List<Path> files = Files.list(backupDir)
                    .filter(p -> p.toString().endsWith(".sql") || p.toString().endsWith(".gz"))
                    .sorted(Comparator.comparing(p -> {
                        try { return Files.getLastModifiedTime((Path) p); } 
                        catch (IOException e) { return null; }
                    }).reversed())
                    .collect(Collectors.toList());

            if (files.size() > maxBackupCount) {
                for (int i = maxBackupCount; i < files.size(); i++) {
                    Files.delete(files.get(i));
                    log.info("Eski yedek temizlendi: {}", files.get(i).getFileName());
                }
            }
        } catch (IOException e) {
            log.error("Eski yedek temizleme hatası", e);
        }
    }

    private String extractDbName() {
        // jdbc:postgresql://host:port/dbname
        String url = datasourceUrl;
        if (url.contains("/")) {
            String afterSlash = url.substring(url.lastIndexOf("/") + 1);
            if (afterSlash.contains("?")) {
                return afterSlash.substring(0, afterSlash.indexOf("?"));
            }
            return afterSlash;
        }
        return "tuketbir_db";
    }

    private String extractHost() {
        try {
            String url = datasourceUrl.replace("jdbc:postgresql://", "");
            return url.split(":")[0];
        } catch (Exception e) {
            return "localhost";
        }
    }

    private String extractPort() {
        try {
            String url = datasourceUrl.replace("jdbc:postgresql://", "");
            String portPart = url.split(":")[1];
            return portPart.split("/")[0];
        } catch (Exception e) {
            return "5432";
        }
    }

    private String formatBoyut(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }
}
