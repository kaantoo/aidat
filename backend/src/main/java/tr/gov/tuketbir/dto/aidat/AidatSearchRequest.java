package tr.gov.tuketbir.dto.aidat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.AidatDurum;

/**
 * Aidat Search/Filter Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AidatSearchRequest {

    private String searchTerm; // Üye no, ad, soyad içinde arama
    private Long birlikId;
    private Long aidatDonemiId;
    private Long uyeId;
    private AidatDurum durumu;
    private Integer yil;
    private Boolean gecikmisMi;
    private Boolean aktif;
    
    // Pagination
    private Integer page = 0;
    private Integer size = 20;
    
    // Sorting
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
