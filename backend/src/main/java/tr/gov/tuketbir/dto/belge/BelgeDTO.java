package tr.gov.tuketbir.dto.belge;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.BelgeTipi;

import java.time.LocalDateTime;

/**
 * Belge Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BelgeDTO {

    private Long id;
    private BelgeTipi belgeTipi;
    private String belgeNo;
    private String dosyaAdi;
    private String dosyaYolu;
    private Long dosyaBoyutu;
    private String mimeType;
    private String aciklama;
    private Long uyeId;
    private String uyeNo;
    private String uyeAdSoyad;
    private Long birlikId;
    private String birlikAdi;
    private Long yukleyenKullaniciId;
    private String yukleyenKullaniciAdi;
    private Boolean aktif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // İndirme URL'i
    private String downloadUrl;
}
