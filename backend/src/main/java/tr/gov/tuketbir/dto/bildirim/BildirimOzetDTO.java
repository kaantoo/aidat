package tr.gov.tuketbir.dto.bildirim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bildirim Özet DTO - Header'da gösterilecek özet bilgi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BildirimOzetDTO {
    private Long okunmamisSayisi;
    private List<BildirimDTO> sonBildirimler;
}
