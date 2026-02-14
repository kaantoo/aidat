package tr.gov.tuketbir.dto.uye;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.domain.enums.UyelikTipi;

/**
 * Üye Search/Filter Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UyeSearchRequest {

    private String searchTerm; // Ad, soyad, TC, üye no içinde arama
    private Long birlikId;
    private UyelikTipi uyelikTipi;
    private UyeDurum uyeDurum;
    private Boolean aktif;
    private String ilKodu;
    private String ilceKodu;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
