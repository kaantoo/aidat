package tr.gov.tuketbir.dto.birlik;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.gov.tuketbir.domain.enums.BirlikTipi;

/**
 * Birlik Create Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirlikCreateRequest {

    @NotBlank(message = "Birlik kodu zorunludur")
    @Size(min = 2, max = 10, message = "Birlik kodu 2-10 karakter arasında olmalıdır")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Birlik kodu sadece büyük harf ve rakam içerebilir")
    private String birlikKodu;

    @NotBlank(message = "Birlik adı zorunludur")
    @Size(min = 3, max = 200, message = "Birlik adı 3-200 karakter arasında olmalıdır")
    private String birlikAdi;

    @NotNull(message = "Birlik tipi zorunludur")
    private BirlikTipi birlikTipi;

    @NotBlank(message = "İl kodu zorunludur")
    @Size(min = 2, max = 2, message = "İl kodu 2 karakter olmalıdır")
    private String ilKodu;

    @Size(max = 10, message = "İlçe kodu en fazla 10 karakter olabilir")
    private String ilceKodu;

    @Size(max = 500, message = "Adres en fazla 500 karakter olabilir")
    private String adres;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Telefon numarası 10-11 rakam olmalıdır")
    private String telefon;

    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir")
    private String email;

    @Size(min = 10, max = 11, message = "Vergi numarası 10-11 karakter olmalıdır")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Vergi numarası sadece rakamlardan oluşmalıdır")
    private String vergiNo;

    @Size(max = 100, message = "Vergi dairesi en fazla 100 karakter olabilir")
    private String vergiDairesi;

    @Size(max = 34, message = "IBAN en fazla 34 karakter olabilir")
    @Pattern(regexp = "^TR[0-9]{24}$", message = "IBAN TR ile başlamalı ve 26 karakter olmalıdır")
    private String ibanNo;

    private Long ustBirlikId;
}
