package tr.gov.tuketbir.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import tr.gov.tuketbir.domain.entity.Bildirim;
import tr.gov.tuketbir.domain.entity.Birlik;
import tr.gov.tuketbir.domain.entity.Kullanici;
import tr.gov.tuketbir.domain.enums.BildirimOnceligi;
import tr.gov.tuketbir.domain.enums.BildirimTipi;

/**
 * Bildirim Event
 * Bildirim oluşturulması için tetiklenir
 */
@Getter
public class BildirimEvent extends ApplicationEvent {

    private final String baslik;
    private final String mesaj;
    private final BildirimTipi tip;
    private final BildirimOnceligi oncelik;
    private final Kullanici kullanici;
    private final Birlik birlik;
    private final String link;
    private final String entityTipi;
    private final Long entityId;

    private BildirimEvent(Builder builder) {
        super(builder.source);
        this.baslik = builder.baslik;
        this.mesaj = builder.mesaj;
        this.tip = builder.tip;
        this.oncelik = builder.oncelik;
        this.kullanici = builder.kullanici;
        this.birlik = builder.birlik;
        this.link = builder.link;
        this.entityTipi = builder.entityTipi;
        this.entityId = builder.entityId;
    }

    public static Builder builder(Object source) {
        return new Builder(source);
    }

    public static class Builder {
        private final Object source;
        private String baslik;
        private String mesaj;
        private BildirimTipi tip;
        private BildirimOnceligi oncelik = BildirimOnceligi.NORMAL;
        private Kullanici kullanici;
        private Birlik birlik;
        private String link;
        private String entityTipi;
        private Long entityId;

        public Builder(Object source) {
            this.source = source;
        }

        public Builder baslik(String baslik) {
            this.baslik = baslik;
            return this;
        }

        public Builder mesaj(String mesaj) {
            this.mesaj = mesaj;
            return this;
        }

        public Builder tip(BildirimTipi tip) {
            this.tip = tip;
            return this;
        }

        public Builder oncelik(BildirimOnceligi oncelik) {
            this.oncelik = oncelik;
            return this;
        }

        public Builder kullanici(Kullanici kullanici) {
            this.kullanici = kullanici;
            return this;
        }

        public Builder birlik(Birlik birlik) {
            this.birlik = birlik;
            return this;
        }

        public Builder link(String link) {
            this.link = link;
            return this;
        }

        public Builder entity(String entityTipi, Long entityId) {
            this.entityTipi = entityTipi;
            this.entityId = entityId;
            return this;
        }

        public BildirimEvent build() {
            return new BildirimEvent(this);
        }
    }
}
