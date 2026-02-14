package tr.gov.tuketbir.dto.aidat;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.OdemeTipi;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tahsilat Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TahsilatDTO {

    private Long id;
    private Long aidatId;
    private Long uyeId;
    private String uyeNo;
    private String uyeAdSoyad;
    private String donemAdi;
    private OdemeTipi odemeTipi;
    private BigDecimal tutar;
    private LocalDate odemeTarihi;
    private String makbuzNo;
    private String dekontNo;
    private String bankaDekontuNo;
    private String aciklama;
    private Long islemYapanKullaniciId;
    private String islemYapanKullaniciAdi;
    private Boolean aktif;
    private LocalDateTime createdAt;
}
