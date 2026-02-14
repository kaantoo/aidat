package tr.gov.tuketbir.dto.aidat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.DonemTipi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aidat Dönemi Create Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AidatDonemiCreateRequest {

    @NotBlank(message = "Dönem kodu zorunludur")
    @Size(min = 3, max = 20, message = "Dönem kodu 3-20 karakter arasında olmalıdır")
    private String donemKodu;

    @NotBlank(message = "Dönem adı zorunludur")
    @Size(min = 3, max = 200, message = "Dönem adı 3-200 karakter arasında olmalıdır")
    private String donemAdi;

    @NotNull(message = "Dönem tipi zorunludur")
    private DonemTipi donemTipi;

    @NotNull(message = "Yıl zorunludur")
    @Positive(message = "Yıl pozitif olmalıdır")
    private Integer yil;

    private Integer ay; // Aylık dönemler için (1-12)

    @NotNull(message = "Başlangıç tarihi zorunludur")
    private LocalDate baslangicTarihi;

    @NotNull(message = "Bitiş tarihi zorunludur")
    private LocalDate bitisTarihi;

    @NotNull(message = "Son ödeme tarihi zorunludur")
    private LocalDate sonOdemeTarihi;

    /**
     * Aidat tutarı - Alt birlikler için zorunlu
     * Merkez birlik için kullanılmaz
     */
    @Positive(message = "Aidat tutarı pozitif olmalıdır")
    private BigDecimal tutar;

    /**
     * Merkez pay oranı (%) - Merkez birlik dönemleri için zorunlu
     * Alt birlik tahakkuklarından alınacak pay oranı
     * Örn: %10 için 10.00
     */
    @Positive(message = "Pay oranı pozitif olmalıdır")
    private BigDecimal merkezPayOrani;

    private BigDecimal asgariUcretTutari;

    @PositiveOrZero(message = "Gecikme faizi oranı negatif olamaz")
    private BigDecimal gecikmeFaiziOrani;
    
    private String asgariUcretAciklama;
    
    private String aciklama;

    private Long birlikId; // Null ise merkez birlik için
}
