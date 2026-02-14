-- TÜKETBİR Sentetik Test Verisi
-- Bu script test amaçlı örnek veriler oluşturur

-- 1. MERKEZ BİRLİK (Eğer yoksa)
INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'MERKEZ', 'TÜKETBİR Merkez Birliği', 'MERKEZ', 'Ankara', 'Çankaya', 'Atatürk Bulvarı No:100', '03125551234', 'merkez@tuketbir.gov.tr', '1234567890', 0, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_tipi = 'MERKEZ');

-- 2. ALT BİRLİKLER (5 adet)
INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'IST001', 'İstanbul Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'İstanbul', 'Kadıköy', 'Caferağa Mah. Moda Cad. No:50', '02165551234', 'istanbul@tuketbir.gov.tr', '1111111111', 15.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'IST001');

INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'ANK001', 'Ankara Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Ankara', 'Yenimahalle', 'Ragıp Tüzün Cad. No:25', '03125552345', 'ankara@tuketbir.gov.tr', '2222222222', 12.50, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'ANK001');

INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'IZM001', 'İzmir Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'İzmir', 'Konak', 'Cumhuriyet Bulvarı No:80', '02325553456', 'izmir@tuketbir.gov.tr', '3333333333', 10.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'IZM001');

INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'BRS001', 'Bursa Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Bursa', 'Osmangazi', 'Atatürk Cad. No:60', '02245554567', 'bursa@tuketbir.gov.tr', '4444444444', 8.00, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'BRS001');

INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il, ilce, adres, telefon, email, vergi_no, merkez_pay_orani, is_active, created_at, updated_at)
SELECT 'ANT001', 'Antalya Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Antalya', 'Muratpaşa', 'Güllük Cad. No:35', '02425555678', 'antalya@tuketbir.gov.tr', '5555555555', 7.50, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'ANT001');

-- 3. AİDAT DÖNEMLERİ (2024-2025-2026 yılları için aylık dönemler)
-- 2025 Dönemleri
INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-01', '2025 Ocak Aidatı', 2025, 1, '2025-01-01', '2025-01-31', '2025-02-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-01');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-02', '2025 Şubat Aidatı', 2025, 2, '2025-02-01', '2025-02-28', '2025-03-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-02');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-03', '2025 Mart Aidatı', 2025, 3, '2025-03-01', '2025-03-31', '2025-04-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-03');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-04', '2025 Nisan Aidatı', 2025, 4, '2025-04-01', '2025-04-30', '2025-05-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-04');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-05', '2025 Mayıs Aidatı', 2025, 5, '2025-05-01', '2025-05-31', '2025-06-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-05');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-06', '2025 Haziran Aidatı', 2025, 6, '2025-06-01', '2025-06-30', '2025-07-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-06');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-07', '2025 Temmuz Aidatı', 2025, 7, '2025-07-01', '2025-07-31', '2025-08-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-07');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-08', '2025 Ağustos Aidatı', 2025, 8, '2025-08-01', '2025-08-31', '2025-09-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-08');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-09', '2025 Eylül Aidatı', 2025, 9, '2025-09-01', '2025-09-30', '2025-10-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-09');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-10', '2025 Ekim Aidatı', 2025, 10, '2025-10-01', '2025-10-31', '2025-11-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-10');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-11', '2025 Kasım Aidatı', 2025, 11, '2025-11-01', '2025-11-30', '2025-12-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-11');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2025-12', '2025 Aralık Aidatı', 2025, 12, '2025-12-01', '2025-12-31', '2026-01-15', 500.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-12');

-- 2026 Dönemleri (Ocak-Şubat)
INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2026-01', '2026 Ocak Aidatı', 2026, 1, '2026-01-01', '2026-01-31', '2026-02-15', 550.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2026-01');

INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, ay, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faiz_orani, durum, is_active, created_at, updated_at)
SELECT 'AID-2026-02', '2026 Şubat Aidatı', 2026, 2, '2026-02-01', '2026-02-28', '2026-03-15', 550.00, 2.50, 'AKTIF', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2026-02');

-- 4. ÜYELER (Her alt birlik için 20'şer üye - toplam 100 üye)
-- İstanbul Birliği Üyeleri
DO $$
DECLARE
    ist_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(20);
BEGIN
    SELECT id INTO ist_birlik_id FROM birlikler WHERE birlik_kodu = 'IST001';
    
    FOR i IN 1..20 LOOP
        uye_no := 'IST-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, telefon, email, adres, il, ilce, uye_tipi, uye_durum, kayit_tarihi, birlik_id, is_active, created_at, updated_at)
        SELECT uye_no, 
               CASE WHEN i % 5 = 1 THEN 'Ahmet' WHEN i % 5 = 2 THEN 'Mehmet' WHEN i % 5 = 3 THEN 'Ali' WHEN i % 5 = 4 THEN 'Mustafa' ELSE 'Hasan' END,
               CASE WHEN i % 4 = 1 THEN 'Yılmaz' WHEN i % 4 = 2 THEN 'Kaya' WHEN i % 4 = 3 THEN 'Demir' ELSE 'Çelik' END,
               '1000000000' || LPAD(i::TEXT, 1, '0'),
               '0532' || LPAD((5550000 + i)::TEXT, 7, '0'),
               'uye' || i || '@istanbul.tuketbir.gov.tr',
               'Örnek Mahallesi ' || i || '. Sokak No:' || i,
               'İstanbul',
               CASE WHEN i % 3 = 1 THEN 'Kadıköy' WHEN i % 3 = 2 THEN 'Beşiktaş' ELSE 'Üsküdar' END,
               CASE WHEN i % 2 = 0 THEN 'BIREYSEL' ELSE 'KURUMSAL' END,
               CASE WHEN i <= 18 THEN 'AKTIF' ELSE 'PASIF' END,
               NOW() - INTERVAL '1 year' * (i % 3),
               ist_birlik_id,
               true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'IST-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- Ankara Birliği Üyeleri
DO $$
DECLARE
    ank_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(20);
BEGIN
    SELECT id INTO ank_birlik_id FROM birlikler WHERE birlik_kodu = 'ANK001';
    
    FOR i IN 1..20 LOOP
        uye_no := 'ANK-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, telefon, email, adres, il, ilce, uye_tipi, uye_durum, kayit_tarihi, birlik_id, is_active, created_at, updated_at)
        SELECT uye_no, 
               CASE WHEN i % 5 = 1 THEN 'Fatma' WHEN i % 5 = 2 THEN 'Ayşe' WHEN i % 5 = 3 THEN 'Zeynep' WHEN i % 5 = 4 THEN 'Elif' ELSE 'Seda' END,
               CASE WHEN i % 4 = 1 THEN 'Arslan' WHEN i % 4 = 2 THEN 'Şahin' WHEN i % 4 = 3 THEN 'Kurt' ELSE 'Aslan' END,
               '2000000000' || LPAD(i::TEXT, 1, '0'),
               '0533' || LPAD((5550000 + i)::TEXT, 7, '0'),
               'uye' || i || '@ankara.tuketbir.gov.tr',
               'Başkent Mahallesi ' || i || '. Cadde No:' || i,
               'Ankara',
               CASE WHEN i % 3 = 1 THEN 'Çankaya' WHEN i % 3 = 2 THEN 'Yenimahalle' ELSE 'Keçiören' END,
               CASE WHEN i % 2 = 0 THEN 'BIREYSEL' ELSE 'KURUMSAL' END,
               CASE WHEN i <= 17 THEN 'AKTIF' ELSE 'PASIF' END,
               NOW() - INTERVAL '1 year' * (i % 3),
               ank_birlik_id,
               true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'ANK-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- İzmir Birliği Üyeleri
DO $$
DECLARE
    izm_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(20);
BEGIN
    SELECT id INTO izm_birlik_id FROM birlikler WHERE birlik_kodu = 'IZM001';
    
    FOR i IN 1..20 LOOP
        uye_no := 'IZM-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, telefon, email, adres, il, ilce, uye_tipi, uye_durum, kayit_tarihi, birlik_id, is_active, created_at, updated_at)
        SELECT uye_no, 
               CASE WHEN i % 5 = 1 THEN 'Kemal' WHEN i % 5 = 2 THEN 'Cem' WHEN i % 5 = 3 THEN 'Barış' WHEN i % 5 = 4 THEN 'Emre' ELSE 'Burak' END,
               CASE WHEN i % 4 = 1 THEN 'Özdemir' WHEN i % 4 = 2 THEN 'Aydın' WHEN i % 4 = 3 THEN 'Yıldız' ELSE 'Güneş' END,
               '3000000000' || LPAD(i::TEXT, 1, '0'),
               '0534' || LPAD((5550000 + i)::TEXT, 7, '0'),
               'uye' || i || '@izmir.tuketbir.gov.tr',
               'Ege Mahallesi ' || i || '. Sokak No:' || i,
               'İzmir',
               CASE WHEN i % 3 = 1 THEN 'Konak' WHEN i % 3 = 2 THEN 'Karşıyaka' ELSE 'Bornova' END,
               CASE WHEN i % 2 = 0 THEN 'BIREYSEL' ELSE 'KURUMSAL' END,
               CASE WHEN i <= 16 THEN 'AKTIF' ELSE 'PASIF' END,
               NOW() - INTERVAL '1 year' * (i % 3),
               izm_birlik_id,
               true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'IZM-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- Bursa Birliği Üyeleri
DO $$
DECLARE
    brs_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(20);
BEGIN
    SELECT id INTO brs_birlik_id FROM birlikler WHERE birlik_kodu = 'BRS001';
    
    FOR i IN 1..20 LOOP
        uye_no := 'BRS-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, telefon, email, adres, il, ilce, uye_tipi, uye_durum, kayit_tarihi, birlik_id, is_active, created_at, updated_at)
        SELECT uye_no, 
               CASE WHEN i % 5 = 1 THEN 'Oğuz' WHEN i % 5 = 2 THEN 'Kaan' WHEN i % 5 = 3 THEN 'Berk' WHEN i % 5 = 4 THEN 'Arda' ELSE 'Yusuf' END,
               CASE WHEN i % 4 = 1 THEN 'Koç' WHEN i % 4 = 2 THEN 'Öztürk' WHEN i % 4 = 3 THEN 'Polat' ELSE 'Erdem' END,
               '4000000000' || LPAD(i::TEXT, 1, '0'),
               '0535' || LPAD((5550000 + i)::TEXT, 7, '0'),
               'uye' || i || '@bursa.tuketbir.gov.tr',
               'Yeşil Mahalle ' || i || '. Cadde No:' || i,
               'Bursa',
               CASE WHEN i % 3 = 1 THEN 'Osmangazi' WHEN i % 3 = 2 THEN 'Nilüfer' ELSE 'Yıldırım' END,
               CASE WHEN i % 2 = 0 THEN 'BIREYSEL' ELSE 'KURUMSAL' END,
               CASE WHEN i <= 15 THEN 'AKTIF' ELSE 'PASIF' END,
               NOW() - INTERVAL '1 year' * (i % 3),
               brs_birlik_id,
               true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'BRS-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- Antalya Birliği Üyeleri
DO $$
DECLARE
    ant_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(20);
BEGIN
    SELECT id INTO ant_birlik_id FROM birlikler WHERE birlik_kodu = 'ANT001';
    
    FOR i IN 1..20 LOOP
        uye_no := 'ANT-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, telefon, email, adres, il, ilce, uye_tipi, uye_durum, kayit_tarihi, birlik_id, is_active, created_at, updated_at)
        SELECT uye_no, 
               CASE WHEN i % 5 = 1 THEN 'Deniz' WHEN i % 5 = 2 THEN 'Ege' WHEN i % 5 = 3 THEN 'Umut' WHEN i % 5 = 4 THEN 'Can' ELSE 'Mert' END,
               CASE WHEN i % 4 = 1 THEN 'Akdeniz' WHEN i % 4 = 2 THEN 'Denizci' WHEN i % 4 = 3 THEN 'Sahil' ELSE 'Kumsal' END,
               '5000000000' || LPAD(i::TEXT, 1, '0'),
               '0536' || LPAD((5550000 + i)::TEXT, 7, '0'),
               'uye' || i || '@antalya.tuketbir.gov.tr',
               'Akdeniz Mahallesi ' || i || '. Sokak No:' || i,
               'Antalya',
               CASE WHEN i % 3 = 1 THEN 'Muratpaşa' WHEN i % 3 = 2 THEN 'Kepez' ELSE 'Konyaaltı' END,
               CASE WHEN i % 2 = 0 THEN 'BIREYSEL' ELSE 'KURUMSAL' END,
               CASE WHEN i <= 14 THEN 'AKTIF' ELSE 'PASIF' END,
               NOW() - INTERVAL '1 year' * (i % 3),
               ant_birlik_id,
               true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'ANT-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 5. AİDATLAR (Tahakkuklar) - Her aktif üye için 2025 dönemleri
-- Ocak 2025 Aidatları
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(15,2) := 500.00;
    odenen DECIMAL(15,2);
    kalan DECIMAL(15,2);
    durum VARCHAR(20);
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-01';
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' LOOP
        -- Rastgele ödeme durumu: %70 tam ödemiş, %20 kısmi, %10 ödememiş
        IF random() < 0.70 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF random() < 0.67 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'ODENMEDI';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tutari, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, tahakkuk_tutari, odenen, kalan, durum, '2025-02-15', true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- Şubat 2025 Aidatları
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(15,2) := 500.00;
    odenen DECIMAL(15,2);
    kalan DECIMAL(15,2);
    durum VARCHAR(20);
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-02';
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' LOOP
        IF random() < 0.65 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF random() < 0.60 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'ODENMEDI';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tutari, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, tahakkuk_tutari, odenen, kalan, durum, '2025-03-15', true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- Mart-Haziran 2025 Aidatları (Döngüyle)
DO $$
DECLARE
    donem_rec RECORD;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(15,2) := 500.00;
    odenen DECIMAL(15,2);
    kalan DECIMAL(15,2);
    durum VARCHAR(20);
BEGIN
    FOR donem_rec IN SELECT id, donem_kodu FROM aidat_donemleri WHERE donem_kodu IN ('AID-2025-03', 'AID-2025-04', 'AID-2025-05', 'AID-2025-06') LOOP
        FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' LOOP
            IF random() < 0.60 THEN
                odenen := tahakkuk_tutari;
                kalan := 0;
                durum := 'ODENDI';
            ELSIF random() < 0.55 THEN
                odenen := tahakkuk_tutari * 0.5;
                kalan := tahakkuk_tutari * 0.5;
                durum := 'KISMI_ODENDI';
            ELSE
                odenen := 0;
                kalan := tahakkuk_tutari;
                durum := 'ODENMEDI';
            END IF;
            
            INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tutari, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, created_at, updated_at)
            SELECT donem_rec.id, uye_rec.id, uye_rec.birlik_id, tahakkuk_tutari, odenen, kalan, durum, 
                   CASE WHEN donem_rec.donem_kodu = 'AID-2025-03' THEN '2025-04-15'::DATE
                        WHEN donem_rec.donem_kodu = 'AID-2025-04' THEN '2025-05-15'::DATE
                        WHEN donem_rec.donem_kodu = 'AID-2025-05' THEN '2025-06-15'::DATE
                        ELSE '2025-07-15'::DATE END,
                   true, NOW(), NOW()
            WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_rec.id AND uye_id = uye_rec.id);
        END LOOP;
    END LOOP;
END $$;

-- Temmuz-Aralık 2025 Aidatları
DO $$
DECLARE
    donem_rec RECORD;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(15,2) := 500.00;
    odenen DECIMAL(15,2);
    kalan DECIMAL(15,2);
    durum VARCHAR(20);
BEGIN
    FOR donem_rec IN SELECT id, donem_kodu, son_odeme_tarihi FROM aidat_donemleri WHERE donem_kodu IN ('AID-2025-07', 'AID-2025-08', 'AID-2025-09', 'AID-2025-10', 'AID-2025-11', 'AID-2025-12') LOOP
        FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' LOOP
            IF random() < 0.55 THEN
                odenen := tahakkuk_tutari;
                kalan := 0;
                durum := 'ODENDI';
            ELSIF random() < 0.50 THEN
                odenen := tahakkuk_tutari * 0.5;
                kalan := tahakkuk_tutari * 0.5;
                durum := 'KISMI_ODENDI';
            ELSE
                odenen := 0;
                kalan := tahakkuk_tutari;
                durum := 'ODENMEDI';
            END IF;
            
            INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tutari, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, created_at, updated_at)
            SELECT donem_rec.id, uye_rec.id, uye_rec.birlik_id, tahakkuk_tutari, odenen, kalan, durum, donem_rec.son_odeme_tarihi, true, NOW(), NOW()
            WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_rec.id AND uye_id = uye_rec.id);
        END LOOP;
    END LOOP;
END $$;

-- Ocak 2026 Aidatları (550 TL)
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(15,2) := 550.00;
    odenen DECIMAL(15,2);
    kalan DECIMAL(15,2);
    durum VARCHAR(20);
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2026-01';
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' LOOP
        IF random() < 0.40 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF random() < 0.35 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'BEKLEMEDE';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tutari, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, tahakkuk_tutari, odenen, kalan, durum, '2026-02-15', true, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- 6. KULLANICILARI ALT BİRLİKLERE ATAMA (Her alt birlik için bir yönetici)
-- İstanbul Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre_hash, ad, soyad, telefon, rol, birlik_id, is_active, hesap_kilitli, email_dogrulandi, created_at, updated_at)
SELECT 'istanbul_admin', 'admin@istanbul.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Ahmet', 'Yönetici', '05321112233', 'ALT_BIRLIK_ADMIN', 
       (SELECT id FROM birlikler WHERE birlik_kodu = 'IST001'), true, false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'istanbul_admin');

-- Ankara Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre_hash, ad, soyad, telefon, rol, birlik_id, is_active, hesap_kilitli, email_dogrulandi, created_at, updated_at)
SELECT 'ankara_admin', 'admin@ankara.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Fatma', 'Yönetici', '05331112233', 'ALT_BIRLIK_ADMIN', 
       (SELECT id FROM birlikler WHERE birlik_kodu = 'ANK001'), true, false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'ankara_admin');

-- İzmir Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre_hash, ad, soyad, telefon, rol, birlik_id, is_active, hesap_kilitli, email_dogrulandi, created_at, updated_at)
SELECT 'izmir_admin', 'admin@izmir.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Kemal', 'Yönetici', '05341112233', 'ALT_BIRLIK_ADMIN', 
       (SELECT id FROM birlikler WHERE birlik_kodu = 'IZM001'), true, false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'izmir_admin');

-- Bursa Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre_hash, ad, soyad, telefon, rol, birlik_id, is_active, hesap_kilitli, email_dogrulandi, created_at, updated_at)
SELECT 'bursa_admin', 'admin@bursa.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Oğuz', 'Yönetici', '05351112233', 'ALT_BIRLIK_ADMIN', 
       (SELECT id FROM birlikler WHERE birlik_kodu = 'BRS001'), true, false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'bursa_admin');

-- Antalya Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre_hash, ad, soyad, telefon, rol, birlik_id, is_active, hesap_kilitli, email_dogrulandi, created_at, updated_at)
SELECT 'antalya_admin', 'admin@antalya.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Deniz', 'Yönetici', '05361112233', 'ALT_BIRLIK_ADMIN', 
       (SELECT id FROM birlikler WHERE birlik_kodu = 'ANT001'), true, false, true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'antalya_admin');

-- Özet bilgiler
SELECT 'Sentetik veri yükleme tamamlandı!' AS mesaj;
SELECT 'Birlik Sayısı: ' || COUNT(*) FROM birlikler;
SELECT 'Üye Sayısı: ' || COUNT(*) FROM uyeler;
SELECT 'Aidat Dönemi Sayısı: ' || COUNT(*) FROM aidat_donemleri;
SELECT 'Aidat Kaydı Sayısı: ' || COUNT(*) FROM aidatlar;
SELECT 'Kullanıcı Sayısı: ' || COUNT(*) FROM kullanicilar;
