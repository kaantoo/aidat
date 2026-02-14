package tr.gov.tuketbir.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.GelirGider;

/**
 * Gelir Gider Event
 * Gelir veya gider kaydı yapıldığında tetiklenir
 */
@Getter
public class GelirGiderEvent extends ApplicationEvent {

    private final GelirGider gelirGider;
    private final Birlik birlik;
    private final IslemTipi islemTipi;

    public enum IslemTipi {
        OLUSTURULDU,
        GUNCELLENDI,
        SILINDI,
        ONAYLANDI,
        REDDEDILDI
    }

    public GelirGiderEvent(Object source, GelirGider gelirGider, Birlik birlik, IslemTipi islemTipi) {
        super(source);
        this.gelirGider = gelirGider;
        this.birlik = birlik;
        this.islemTipi = islemTipi;
    }
}
