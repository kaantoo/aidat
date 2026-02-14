package tr.gov.tuketbir.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Uye;

/**
 * Üye Kayıt Event
 * Yeni üye kaydedildiğinde tetiklenir
 */
@Getter
public class UyeKayitEvent extends ApplicationEvent {

    private final Uye uye;
    private final Birlik birlik;
    private final IslemTipi islemTipi;

    public enum IslemTipi {
        OLUSTURULDU,
        GUNCELLENDI,
        SILINDI,
        AKTIFE_ALINDI,
        PASIFE_ALINDI
    }

    public UyeKayitEvent(Object source, Uye uye, Birlik birlik, IslemTipi islemTipi) {
        super(source);
        this.uye = uye;
        this.birlik = birlik;
        this.islemTipi = islemTipi;
    }
}
