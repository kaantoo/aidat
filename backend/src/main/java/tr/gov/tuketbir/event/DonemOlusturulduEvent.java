package tr.gov.tuketbir.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tuketbir.domain.entity.AidatDonemi;

/**
 * Aidat Dönemi Oluşturuldu Event
 * 
 * Bu event, bir aidat dönemi oluşturulduğunda tetiklenir.
 * - Merkez Birlik dönemi oluşturursa: Alt birliklere dönem atanır
 * - Alt Birlik dönemi oluşturursa: Üyelere aidat tahakkuk edilir
 * 
 * @author Tuketbir Development Team
 */
@Getter
public class DonemOlusturulduEvent extends ApplicationEvent {

    private final AidatDonemi donem;
    private final boolean merkezBirlikTarafindan;

    public DonemOlusturulduEvent(Object source, AidatDonemi donem, boolean merkezBirlikTarafindan) {
        super(source);
        this.donem = donem;
        this.merkezBirlikTarafindan = merkezBirlikTarafindan;
    }
}
