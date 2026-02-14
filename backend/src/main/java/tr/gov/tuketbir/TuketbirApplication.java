package tr.gov.tuketbir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Kırmızı Et Üreticileri Merkez Birliği
 * Aidat ve Yönetim Sistemi Ana Uygulama Sınıfı
 * 
 * @author Tuketbir Development Team
 * @version 1.0.0
 * @since 2026-01-18
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
public class TuketbirApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuketbirApplication.class, args);
    }
}
