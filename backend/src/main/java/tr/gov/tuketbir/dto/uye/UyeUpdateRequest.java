package tr.gov.tuketbir.dto.uye;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.Cinsiyet;
import tr.gov.tuketbir.domain.enums.UyeDurum;
import tr.gov.tuketbir.domain.enums.UyelikTipi;

import java.time.LocalDate;

/**
 * Üye Update Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UyeUpdateRequest {

    // ======================= Kimlik Bilgileri =======================
    
    @Size(min = 2, max = 100, message = "Ad 2-100 karakter arasında olmalıdır")
    private String ad;

    @Size(min = 2, max = 100, message = "Soyad 2-100 karakter arasında olmalıdır")
    private String soyad;

    @Size(max = 100, message = "Baba adı en fazla 100 karakter olabilir")
    private String babaAdi;

    @Size(max = 100, message = "Ana adı en fazla 100 karakter olabilir")
    private String anaAdi;

    private Cinsiyet cinsiyet;

    @Past(message = "Doğum tarihi geçmiş bir tarih olmalıdır")
    private LocalDate dogumTarihi;

    @Size(max = 100, message = "Doğum yeri en fazla 100 karakter olabilir")
    private String dogumYeri;

    // ======================= Üyelik Bilgileri =======================

    private UyelikTipi uyelikTipi;

    private LocalDate katilimTarihi;

    private Long birlikId;

    private UyeDurum uyeDurum;

    private LocalDate ayrilmaTarihi;

    // ======================= İletişim Bilgileri =======================

    @Pattern(regexp = "^(05)[0-9]{9}$", message = "Geçerli bir cep telefonu giriniz (05XXXXXXXXX)")
    private String cepTelefon;

    @Size(max = 15, message = "Sabit telefon en fazla 15 karakter olabilir")
    private String sabitTelefon;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    // ======================= Adres Bilgileri =======================

    @Size(max = 10, message = "İl kodu en fazla 10 karakter olabilir")
    private String ilKodu;

    @Size(max = 100, message = "İl adı en fazla 100 karakter olabilir")
    private String ilAdi;

    @Size(max = 10, message = "İlçe kodu en fazla 10 karakter olabilir")
    private String ilceKodu;

    @Size(max = 100, message = "İlçe adı en fazla 100 karakter olabilir")
    private String ilceAdi;

    @Size(max = 100, message = "Mahalle/Köy en fazla 100 karakter olabilir")
    private String mahalleKoy;

    @Size(max = 500, message = "Adres en fazla 500 karakter olabilir")
    private String adres;

    @Size(max = 10, message = "Posta kodu en fazla 10 karakter olabilir")
    private String postaKodu;

    // ======================= Kurumsal Üye Bilgileri =======================

    @Size(max = 200, message = "Firma adı en fazla 200 karakter olabilir")
    private String firmaAdi;

    @Size(max = 100, message = "Vergi dairesi en fazla 100 karakter olabilir")
    private String vergiDairesi;

    @Size(max = 20, message = "Vergi no en fazla 20 karakter olabilir")
    private String vergiNo;

    @Size(max = 50, message = "Ticaret sicil no en fazla 50 karakter olabilir")
    private String ticaretSicilNo;

    // ======================= Üretici Bilgileri =======================

    @Size(max = 200, message = "İşletme adı en fazla 200 karakter olabilir")
    private String isletmeAdi;

    @Size(max = 50, message = "İşletme sicil no en fazla 50 karakter olabilir")
    private String isletmeSicilNo;

    @PositiveOrZero(message = "Hayvan sayısı negatif olamaz")
    private Integer hayvanSayisi;

    @PositiveOrZero(message = "Üretim kapasitesi negatif olamaz")
    private Integer uretimKapasitesi;

    // ======================= Diğer =======================

    @Size(max = 1000, message = "Açıklama en fazla 1000 karakter olabilir")
    private String aciklama;
}
