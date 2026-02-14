package tr.gov.tuketbir.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Event Configuration
 * Async event handling için Spring configuration
 */
@Configuration
@EnableAsync
public class EventConfig {
    // Async event handling için @EnableAsync aktifleştirme
}
