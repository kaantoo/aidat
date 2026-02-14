package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;

/**
 * Sistem Ayarları Entity
 * Sistem genelinde geçerli ayarları tutar
 */
@Entity
@Table(name = "sistem_ayarlari")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SistemAyari extends BaseEntity {

    @Column(name = "anahtar", unique = true, nullable = false)
    private String anahtar;

    @Column(name = "deger", length = 1000)
    private String deger;

    @Column(name = "aciklama")
    private String aciklama;

    @Column(name = "tip")
    private String tip; // STRING, BOOLEAN, INTEGER, JSON

    // Sabit anahtar isimleri
    public static final String READ_ONLY_MODE = "SISTEM_READ_ONLY_MODE";
    public static final String READ_ONLY_MESAJ = "SISTEM_READ_ONLY_MESAJ";
    public static final String BAKIM_MODU = "SISTEM_BAKIM_MODU";
}
