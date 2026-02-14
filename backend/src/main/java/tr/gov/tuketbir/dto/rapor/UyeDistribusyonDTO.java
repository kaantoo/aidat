package tr.gov.tuketbir.dto.rapor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UyeDistribusyonDTO {
    private List<IlDistribusyonDTO> ilBazinda;
    private List<DurumDistribusyonDTO> durumBazinda;
    private List<TipDistribusyonDTO> tipBazinda;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IlDistribusyonDTO {
        private String il;
        private Long sayi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DurumDistribusyonDTO {
        private String durum;
        private Long sayi;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipDistribusyonDTO {
        private String tip;
        private Long sayi;
    }
}
