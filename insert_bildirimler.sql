INSERT INTO bildirimler (baslik, mesaj, tip, oncelik, okundu, kullanici_id, birlik_id, tenant_id, link, entity_tipi, entity_id) VALUES 
('Yeni Üye Kaydı', 'Ahmet Yılmaz adlı yeni üye kaydedildi.', 'UYE_KAYDI', 'NORMAL', false, 1, 1, 1, '/uyeler/1', 'UYE', 1),
('Aidat Ödemesi', 'Mehmet Demir 500 TL aidat ödemesi yaptı.', 'AIDAT_ODEMESI', 'NORMAL', false, 1, 1, 1, '/aidatlar/1', 'AIDAT', 1),
('Geciken Ödeme', '3 üyenin aidat ödemesi gecikti.', 'GECIKEN_ODEME', 'YUKSEK', false, 1, 1, 1, '/aidatlar', NULL, NULL),
('Dönem Oluşturuldu', '2025 Q1 dönemi başarıyla oluşturuldu.', 'DONEM_OLUSTURULDU', 'NORMAL', true, 1, 1, 1, '/aidatlar/donemler', 'DONEM', 1);
