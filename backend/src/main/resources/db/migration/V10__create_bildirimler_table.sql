-- Bildirimler tablosu oluştur
CREATE TABLE IF NOT EXISTS bildirimler (
    id BIGSERIAL PRIMARY KEY,
    baslik VARCHAR(255) NOT NULL,
    mesaj TEXT,
    tip VARCHAR(50) NOT NULL,
    oncelik VARCHAR(20) DEFAULT 'NORMAL',
    okundu BOOLEAN DEFAULT FALSE,
    okunma_tarihi TIMESTAMP,
    link VARCHAR(500),
    entity_tipi VARCHAR(50),
    entity_id BIGINT,
    kullanici_id BIGINT REFERENCES kullanicilar(id),
    birlik_id BIGINT REFERENCES birlikler(id),
    tenant_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Index'ler
CREATE INDEX IF NOT EXISTS idx_bildirimler_kullanici_id ON bildirimler(kullanici_id);
CREATE INDEX IF NOT EXISTS idx_bildirimler_birlik_id ON bildirimler(birlik_id);
CREATE INDEX IF NOT EXISTS idx_bildirimler_okundu ON bildirimler(okundu);
CREATE INDEX IF NOT EXISTS idx_bildirimler_tip ON bildirimler(tip);
CREATE INDEX IF NOT EXISTS idx_bildirimler_created_at ON bildirimler(created_at DESC);

-- Test verileri ekle
INSERT INTO bildirimler (baslik, mesaj, tip, oncelik, okundu, kullanici_id, birlik_id, tenant_id, link, entity_tipi, entity_id)
VALUES 
('Yeni Üye Kaydı', 'Ahmet Yılmaz adlı yeni üye kaydedildi.', 'UYE_KAYDI', 'NORMAL', false, 1, 1, 1, '/uyeler/1', 'UYE', 1),
('Aidat Ödemesi', 'Mehmet Demir 500.00₺ aidat ödemesi yaptı.', 'AIDAT_ODEMESI', 'NORMAL', false, 1, 1, 1, '/aidatlar/1', 'AIDAT', 1),
('Geciken Ödeme', '3 üyenin aidat ödemesi gecikti.', 'GECIKEN_ODEME', 'YUKSEK', false, 1, 1, 1, '/aidatlar', NULL, NULL),
('Dönem Oluşturuldu', '2025 Q1 dönemi başarıyla oluşturuldu.', 'DONEM_OLUSTURULDU', 'NORMAL', true, 1, 1, 1, '/aidatlar/donemler', 'DONEM', 1),
('Sistem Bildirimi', 'Sistem bakımı 01.02.2025 tarihinde yapılacaktır.', 'SISTEM', 'DUSUK', true, 1, 1, 1, NULL, NULL, NULL);

COMMENT ON TABLE bildirimler IS 'Kullanıcı bildirimleri tablosu';
