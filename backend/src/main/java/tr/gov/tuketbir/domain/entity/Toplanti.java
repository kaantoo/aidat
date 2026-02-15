package tr.gov.tuketbir.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import tr.gov.tuketbir.domain.entity.base.BaseEntity;
import tr.gov.tuketbir.domain.enums.ToplantiDurumu;
import tr.gov.tuketbir.domain.enums.ToplantiTuru;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Toplantı Entity - Birlik toplantılarını temsil eder.
 * 
 * Toplantı türleri: Genel Kurul, Yönetim Kurulu, Denetim Kurulu, vb.
 * Her toplantıya kararlar ve katılımcılar bağlanabilir.
 * 
 * @author Tuketbir Development Team
 */
@Entity
@Table(name = "toplantilar", indexes = {
    @Index(name = "idx_toplanti_birlik", columnList = "birlik_id"),
    @Index(name = "idx_toplanti_tarih", columnList = "toplanti_tarihi"),
    @Index(name = "idx_toplanti_tur", columnList = "toplanti_turu"),
    @Index(name = "idx_toplanti_durum", columnList = "durum"),
    @Index(name = "idx_toplanti_no", columnList = "toplanti_no", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Toplanti extends BaseEntity {

    /**
     * Toplantı numarası (otomatik üretilir)
     */
    @Column(name = "toplanti_no", nullable = false, unique = true, length = 50)
    private String toplantiNo;

    /**
     * Bağlı birlik
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "birlik_id", nullable = false)
    private Birlik birlik;

    /**
     * Toplantı başlığı
     */
    @Column(name = "baslik", nullable = false, length = 300)
    private String baslik;

    /**
     * Toplantı türü
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "toplanti_turu", nullable = false, length = 30)
    private ToplantiTuru toplantiTuru;

    /**
     * Toplantı durumu
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "durum", nullable = false, length = 20)
    @Builder.Default
    private ToplantiDurumu durum = ToplantiDurumu.PLANLANMIS;

    /**
     * Toplantı tarihi
     */
    @Column(name = "toplanti_tarihi", nullable = false)
    private LocalDate toplantiTarihi;

    /**
     * Başlangıç saati
     */
    @Column(name = "baslangic_saati")
    private LocalTime baslangicSaati;

    /**
     * Bitiş saati
     */
    @Column(name = "bitis_saati")
    private LocalTime bitisSaati;

    /**
     * Toplantı yeri
     */
    @Column(name = "yer", length = 300)
    private String yer;

    /**
     * Gündem maddeleri (metin)
     */
    @Column(name = "gundem", columnDefinition = "TEXT")
    private String gundem;

    /**
     * Toplantı açıklaması / notlar
     */
    @Column(name = "aciklama", columnDefinition = "TEXT")
    private String aciklama;

    /**
     * Toplantı kararları
     */
    @OneToMany(mappedBy = "toplanti", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("kararSirasi ASC")
    private List<Karar> kararlar = new ArrayList<>();

    /**
     * Toplantı katılımcıları
     */
    @OneToMany(mappedBy = "toplanti", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ToplantiKatilimci> katilimcilar = new HashSet<>();

    /**
     * Toplantıya karar ekle
     */
    public void addKarar(Karar karar) {
        kararlar.add(karar);
        karar.setToplanti(this);
    }

    /**
     * Toplantıya katılımcı ekle
     */
    public void addKatilimci(ToplantiKatilimci katilimci) {
        katilimcilar.add(katilimci);
        katilimci.setToplanti(this);
    }

    /**
     * Karar sayısı
     */
    public int getKararSayisi() {
        return kararlar != null ? kararlar.size() : 0;
    }

    /**
     * Katılımcı sayısı
     */
    public int getKatilimciSayisi() {
        return katilimcilar != null ? katilimcilar.size() : 0;
    }
}
