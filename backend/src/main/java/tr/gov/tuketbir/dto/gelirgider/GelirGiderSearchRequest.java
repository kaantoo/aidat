package tr.gov.tuketbir.dto.gelirgider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.GelirGiderTipi;
import tr.gov.tuketbir.domain.enums.GelirKategorisi;
import tr.gov.tuketbir.domain.enums.GiderKategorisi;

import java.time.LocalDate;

/**
 * Gelir/Gider Search Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GelirGiderSearchRequest {

    private GelirGiderTipi tip;
    private GelirKategorisi gelirKategorisi;
    private GiderKategorisi giderKategorisi;
    private Long birlikId;
    private LocalDate baslangicTarihi;
    private LocalDate bitisTarihi;
    private Boolean aktif;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    
    // Sorting
    private String sortBy = "islemTarihi";
    private String sortDirection = "DESC";
}
