package tr.gov.tuketbir.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tuketbir.domain.entity.Aidat;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Tahsilat;

import java.math.BigDecimal;

/**
 * Aidat Ödeme Event
 * Aidat ödemesi yapıldığında tetiklenir
 */
@Getter
public class AidatOdemeEvent extends ApplicationEvent {

    private final Aidat aidat;
    private final Birlik birlik;
    private final Tahsilat tahsilat;
    private final BigDecimal tutar;
    private final IslemTipi islemTipi;

    public enum IslemTipi {
        ODEME_YAPILDI,
        KISMI_ODEME,
        IPTAL_EDILDI,
        GECIKME_FAIZI_UYGULANDI,
        HATIRLATMA_GONDERILDI
    }

    public AidatOdemeEvent(Object source, Aidat aidat, Birlik birlik, Tahsilat tahsilat, BigDecimal tutar, IslemTipi islemTipi) {
        super(source);
        this.aidat = aidat;
        this.birlik = birlik;
        this.tahsilat = tahsilat;
        this.tutar = tutar;
        this.islemTipi = islemTipi;
    }
}
