package tr.gov.tuketbir.dto.birlik;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.BirlikTipi;

import java.time.LocalDateTime;

/**
 * Birlik Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BirlikDTO {

    private Long id;
    private String birlikKodu;
    private String birlikAdi;
    private BirlikTipi birlikTipi;
    private String ilKodu;
    private String ilAdi;
    private String ilceKodu;
    private String ilceAdi;
    private String adres;
    private String telefon;
    private String email;
    private String vergiNo;
    private String vergiDairesi;
    private String ibanNo;
    private Long ustBirlikId;
    private String ustBirlikAdi;
    private Integer uyeSayisi;
    private Integer altBirlikSayisi;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
