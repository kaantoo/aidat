-- TÜKETBİR Sentetik Test Verisi - Düzeltilmiş Versiyon
-- Bu script mevcut DB şemasına uygun örnek veriler oluşturur

-- 1. MERKEZ BİRLİK (Eğer yoksa)
INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, is_active, tenant_id, created_at, updated_at)
SELECT 'MERKEZ', 'TÜKETBİR Merkez Birliği', 'MERKEZ', 'Ankara', 'Çankaya', 'Atatürk Bulvarı No:100', '03125551234', 'merkez@tuketbir.gov.tr', '1234567890', true, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_tipi = 'MERKEZ');

-- Merkez birlik ID'sini al
DO $$
DECLARE
    merkez_id BIGINT;
BEGIN
    SELECT id INTO merkez_id FROM birlikler WHERE birlik_tipi = 'MERKEZ' LIMIT 1;
    
    -- 2. ALT BİRLİKLER (5 adet)
    INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, parent_birlik_id, is_active, tenant_id, created_at, updated_at)
    SELECT 'IST001', 'İstanbul Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'İstanbul', 'Kadıköy', 'Caferağa Mah. Moda Cad. No:50', '02165551234', 'istanbul@tuketbir.gov.tr', '1111111111', merkez_id, true, 1, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'IST001');

    INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, parent_birlik_id, is_active, tenant_id, created_at, updated_at)
    SELECT 'ANK001', 'Ankara Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Ankara', 'Yenimahalle', 'Ragıp Tüzün Cad. No:25', '03125552345', 'ankara@tuketbir.gov.tr', '2222222222', merkez_id, true, 1, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'ANK001');

    INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, parent_birlik_id, is_active, tenant_id, created_at, updated_at)
    SELECT 'IZM001', 'İzmir Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'İzmir', 'Konak', 'Cumhuriyet Bulvarı No:80', '02325553456', 'izmir@tuketbir.gov.tr', '3333333333', merkez_id, true, 1, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'IZM001');

    INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, parent_birlik_id, is_active, tenant_id, created_at, updated_at)
    SELECT 'BRS001', 'Bursa Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Bursa', 'Osmangazi', 'Atatürk Cad. No:60', '02245554567', 'bursa@tuketbir.gov.tr', '4444444444', merkez_id, true, 1, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'BRS001');

    INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_adi, ilce_adi, adres, telefon, email, vergi_no, parent_birlik_id, is_active, tenant_id, created_at, updated_at)
    SELECT 'ANT001', 'Antalya Tüketim Kooperatifleri Birliği', 'ALT_BIRLIK', 'Antalya', 'Muratpaşa', 'Güllük Cad. No:35', '02425555678', 'antalya@tuketbir.gov.tr', '5555555555', merkez_id, true, 1, NOW(), NOW()
    WHERE NOT EXISTS (SELECT 1 FROM birlikler WHERE birlik_kodu = 'ANT001');
END $$;

-- 3. AİDAT DÖNEMLERİ (2025 yılı için 6 aylık dönemler)
-- 2025 1. Yarıyıl
INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, donem_tipi, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faizi_orani, donem_aktif, is_active, tenant_id, created_at, updated_at)
SELECT 'AID-2025-1', '2025 Yılı 1. Yarıyıl Aidatı', 2025, 'ALTI_AYLIK', '2025-01-01', '2025-06-30', '2025-07-15', 3000.00, 2.50, true, true, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-1');

-- 2025 2. Yarıyıl
INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, donem_tipi, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faizi_orani, donem_aktif, is_active, tenant_id, created_at, updated_at)
SELECT 'AID-2025-2', '2025 Yılı 2. Yarıyıl Aidatı', 2025, 'ALTI_AYLIK', '2025-07-01', '2025-12-31', '2026-01-15', 3000.00, 2.50, true, true, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-2');

-- 2026 1. Yarıyıl (Güncel)
INSERT INTO aidat_donemleri (donem_kodu, donem_adi, yil, donem_tipi, baslangic_tarihi, bitis_tarihi, son_odeme_tarihi, tutar, gecikme_faizi_orani, donem_aktif, is_active, tenant_id, created_at, updated_at)
SELECT 'AID-2026-1', '2026 Yılı 1. Yarıyıl Aidatı', 2026, 'ALTI_AYLIK', '2026-01-01', '2026-06-30', '2026-07-15', 3500.00, 2.50, true, true, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM aidat_donemleri WHERE donem_kodu = 'AID-2026-1');

-- 4. İSTANBUL BİRLİĞİ ÜYELERİ (20 üye)
DO $$
DECLARE
    ist_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(30);
    isimler TEXT[] := ARRAY['Ahmet', 'Mehmet', 'Ali', 'Mustafa', 'Hasan', 'Hüseyin', 'İbrahim', 'Osman', 'Yusuf', 'Kemal', 
                            'Ömer', 'Murat', 'Burak', 'Emre', 'Cem', 'Selim', 'Kaan', 'Berk', 'Arda', 'Can'];
    soyadlar TEXT[] := ARRAY['Yılmaz', 'Kaya', 'Demir', 'Çelik', 'Şahin', 'Yıldız', 'Öztürk', 'Aydın', 'Özdemir', 'Arslan',
                            'Koç', 'Çetin', 'Kurt', 'Aslan', 'Polat', 'Doğan', 'Kılıç', 'Erdoğan', 'Çakır', 'Korkmaz'];
BEGIN
    SELECT id INTO ist_birlik_id FROM birlikler WHERE birlik_kodu = 'IST001';
    IF ist_birlik_id IS NULL THEN RETURN; END IF;
    
    FOR i IN 1..20 LOOP
        uye_no := 'IST-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, adres, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        SELECT uye_no, 
               isimler[i],
               soyadlar[i],
               '1' || LPAD((10000000000 + i)::TEXT, 10, '0'),
               '0532' || LPAD((5550000 + i)::TEXT, 7, '0'),
               LOWER(isimler[i]) || '.' || LOWER(soyadlar[i]) || '@email.com',
               'Örnek Mahallesi ' || i || '. Sokak No:' || i,
               'İstanbul',
               CASE WHEN i % 3 = 1 THEN 'Kadıköy' WHEN i % 3 = 2 THEN 'Beşiktaş' ELSE 'Üsküdar' END,
               CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
               CASE WHEN i <= 18 THEN 'AKTIF' ELSE 'PASIF' END,
               (NOW() - INTERVAL '1 year' * (i % 3))::DATE,
               ist_birlik_id,
               true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'IST-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 5. ANKARA BİRLİĞİ ÜYELERİ (20 üye)
DO $$
DECLARE
    ank_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(30);
    isimler TEXT[] := ARRAY['Fatma', 'Ayşe', 'Zeynep', 'Elif', 'Seda', 'Esra', 'Merve', 'Büşra', 'Gizem', 'Ebru',
                            'Derya', 'Burcu', 'Özlem', 'Hülya', 'Sibel', 'Sevgi', 'Dilek', 'Pınar', 'Serap', 'Nuray'];
    soyadlar TEXT[] := ARRAY['Arslan', 'Şahin', 'Kurt', 'Aslan', 'Güneş', 'Aksu', 'Acar', 'Uzun', 'Güler', 'Kara',
                            'Bulut', 'Sarı', 'Beyaz', 'Yavuz', 'Tekin', 'Turan', 'Karaca', 'Eren', 'Sezer', 'Deniz'];
BEGIN
    SELECT id INTO ank_birlik_id FROM birlikler WHERE birlik_kodu = 'ANK001';
    IF ank_birlik_id IS NULL THEN RETURN; END IF;
    
    FOR i IN 1..20 LOOP
        uye_no := 'ANK-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, adres, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        SELECT uye_no, 
               isimler[i],
               soyadlar[i],
               '2' || LPAD((20000000000 + i)::TEXT, 10, '0'),
               '0533' || LPAD((5550000 + i)::TEXT, 7, '0'),
               LOWER(isimler[i]) || '.' || LOWER(soyadlar[i]) || '@email.com',
               'Başkent Mahallesi ' || i || '. Cadde No:' || i,
               'Ankara',
               CASE WHEN i % 3 = 1 THEN 'Çankaya' WHEN i % 3 = 2 THEN 'Yenimahalle' ELSE 'Keçiören' END,
               CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
               CASE WHEN i <= 17 THEN 'AKTIF' ELSE 'PASIF' END,
               (NOW() - INTERVAL '1 year' * (i % 3))::DATE,
               ank_birlik_id,
               true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'ANK-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 6. İZMİR BİRLİĞİ ÜYELERİ (20 üye)
DO $$
DECLARE
    izm_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(30);
    isimler TEXT[] := ARRAY['Serkan', 'Tolga', 'Onur', 'Barış', 'Güven', 'Erhan', 'Volkan', 'Taner', 'Cenk', 'Levent',
                            'Tarık', 'Alper', 'Koray', 'Sinan', 'Adem', 'Turgut', 'Necip', 'Mete', 'Rıza', 'Ferhat'];
    soyadlar TEXT[] := ARRAY['Özdemir', 'Aydın', 'Yıldız', 'Güneş', 'Koçak', 'Özer', 'Durmuş', 'Aksoy', 'Baran', 'Sönmez',
                            'Başar', 'Kaplan', 'Ateş', 'Çiçek', 'Duman', 'Kahraman', 'Kılınç', 'Taş', 'Türk', 'Vural'];
BEGIN
    SELECT id INTO izm_birlik_id FROM birlikler WHERE birlik_kodu = 'IZM001';
    IF izm_birlik_id IS NULL THEN RETURN; END IF;
    
    FOR i IN 1..20 LOOP
        uye_no := 'IZM-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, adres, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        SELECT uye_no, 
               isimler[i],
               soyadlar[i],
               '3' || LPAD((30000000000 + i)::TEXT, 10, '0'),
               '0534' || LPAD((5550000 + i)::TEXT, 7, '0'),
               LOWER(isimler[i]) || '.' || LOWER(soyadlar[i]) || '@email.com',
               'Ege Mahallesi ' || i || '. Sokak No:' || i,
               'İzmir',
               CASE WHEN i % 3 = 1 THEN 'Konak' WHEN i % 3 = 2 THEN 'Karşıyaka' ELSE 'Bornova' END,
               CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
               CASE WHEN i <= 16 THEN 'AKTIF' ELSE 'PASIF' END,
               (NOW() - INTERVAL '1 year' * (i % 3))::DATE,
               izm_birlik_id,
               true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'IZM-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 7. BURSA BİRLİĞİ ÜYELERİ (20 üye)
DO $$
DECLARE
    brs_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(30);
    isimler TEXT[] := ARRAY['Oğuz', 'Kaan', 'Berk', 'Arda', 'Yusuf', 'Semih', 'Taylan', 'Berke', 'Doruk', 'Batuhan',
                            'Emir', 'Kutay', 'Korel', 'Kerem', 'Sercan', 'Tuncay', 'Umut', 'Yaşar', 'Zafer', 'Baran'];
    soyadlar TEXT[] := ARRAY['Koç', 'Öztürk', 'Polat', 'Erdem', 'Ünal', 'Işık', 'Çam', 'Genç', 'Korkut', 'Yalçın',
                            'Altın', 'Bakır', 'Çevik', 'Durak', 'Güçlü', 'Karakuş', 'Mutlu', 'Öktem', 'Soylu', 'Tuncer'];
BEGIN
    SELECT id INTO brs_birlik_id FROM birlikler WHERE birlik_kodu = 'BRS001';
    IF brs_birlik_id IS NULL THEN RETURN; END IF;
    
    FOR i IN 1..20 LOOP
        uye_no := 'BRS-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, adres, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        SELECT uye_no, 
               isimler[i],
               soyadlar[i],
               '4' || LPAD((40000000000 + i)::TEXT, 10, '0'),
               '0535' || LPAD((5550000 + i)::TEXT, 7, '0'),
               LOWER(isimler[i]) || '.' || LOWER(soyadlar[i]) || '@email.com',
               'Yeşil Mahalle ' || i || '. Cadde No:' || i,
               'Bursa',
               CASE WHEN i % 3 = 1 THEN 'Osmangazi' WHEN i % 3 = 2 THEN 'Nilüfer' ELSE 'Yıldırım' END,
               CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
               CASE WHEN i <= 15 THEN 'AKTIF' ELSE 'PASIF' END,
               (NOW() - INTERVAL '1 year' * (i % 3))::DATE,
               brs_birlik_id,
               true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'BRS-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 8. ANTALYA BİRLİĞİ ÜYELERİ (20 üye)
DO $$
DECLARE
    ant_birlik_id BIGINT;
    i INTEGER;
    uye_no VARCHAR(30);
    isimler TEXT[] := ARRAY['Deniz', 'Ege', 'Umut', 'Can', 'Mert', 'Efe', 'Yağız', 'Çağlar', 'Buğra', 'Görkem',
                            'Harun', 'Ilgaz', 'Kağan', 'Melih', 'Orkun', 'Poyraz', 'Sarp', 'Tuna', 'Utku', 'Vedat'];
    soyadlar TEXT[] := ARRAY['Akdeniz', 'Denizci', 'Sahil', 'Kumsal', 'Mavi', 'Dalga', 'Rüzgar', 'Yaz', 'Güneşli', 'Kıyı',
                            'Ada', 'Liman', 'Marina', 'Plaj', 'Körfez', 'Boğaz', 'Mendil', 'Balıkçı', 'Denizel', 'Martı'];
BEGIN
    SELECT id INTO ant_birlik_id FROM birlikler WHERE birlik_kodu = 'ANT001';
    IF ant_birlik_id IS NULL THEN RETURN; END IF;
    
    FOR i IN 1..20 LOOP
        uye_no := 'ANT-' || LPAD(i::TEXT, 4, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, adres, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        SELECT uye_no, 
               isimler[i],
               soyadlar[i],
               '5' || LPAD((50000000000 + i)::TEXT, 10, '0'),
               '0536' || LPAD((5550000 + i)::TEXT, 7, '0'),
               LOWER(isimler[i]) || '.' || LOWER(soyadlar[i]) || '@email.com',
               'Akdeniz Mahallesi ' || i || '. Sokak No:' || i,
               'Antalya',
               CASE WHEN i % 3 = 1 THEN 'Muratpaşa' WHEN i % 3 = 2 THEN 'Kepez' ELSE 'Konyaaltı' END,
               CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
               CASE WHEN i <= 14 THEN 'AKTIF' ELSE 'PASIF' END,
               (NOW() - INTERVAL '1 year' * (i % 3))::DATE,
               ant_birlik_id,
               true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM uyeler WHERE uye_no = 'ANT-' || LPAD(i::TEXT, 4, '0'));
    END LOOP;
END $$;

-- 9. AİDATLAR - 2025 1. Yarıyıl
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(12,2) := 3000.00;
    odenen DECIMAL(12,2);
    kalan DECIMAL(12,2);
    durum VARCHAR(20);
    rnd FLOAT;
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-1';
    IF donem_id IS NULL THEN RETURN; END IF;
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' AND u.is_active = true LOOP
        rnd := random();
        IF rnd < 0.70 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF rnd < 0.85 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'GECIKTI';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tarihi, tahakkuk_tutari, toplam_borc, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, tenant_id, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, '2025-01-01', tahakkuk_tutari, tahakkuk_tutari, odenen, kalan, durum, '2025-07-15', true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- 10. AİDATLAR - 2025 2. Yarıyıl
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(12,2) := 3000.00;
    odenen DECIMAL(12,2);
    kalan DECIMAL(12,2);
    durum VARCHAR(20);
    rnd FLOAT;
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2025-2';
    IF donem_id IS NULL THEN RETURN; END IF;
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' AND u.is_active = true LOOP
        rnd := random();
        IF rnd < 0.60 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF rnd < 0.80 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'GECIKTI';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tarihi, tahakkuk_tutari, toplam_borc, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, tenant_id, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, '2025-07-01', tahakkuk_tutari, tahakkuk_tutari, odenen, kalan, durum, '2026-01-15', true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- 11. AİDATLAR - 2026 1. Yarıyıl (Güncel dönem)
DO $$
DECLARE
    donem_id BIGINT;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(12,2) := 3500.00;
    odenen DECIMAL(12,2);
    kalan DECIMAL(12,2);
    durum VARCHAR(20);
    rnd FLOAT;
BEGIN
    SELECT id INTO donem_id FROM aidat_donemleri WHERE donem_kodu = 'AID-2026-1';
    IF donem_id IS NULL THEN RETURN; END IF;
    
    FOR uye_rec IN SELECT u.id, u.birlik_id FROM uyeler u WHERE u.uye_durum = 'AKTIF' AND u.is_active = true LOOP
        rnd := random();
        IF rnd < 0.35 THEN
            odenen := tahakkuk_tutari;
            kalan := 0;
            durum := 'ODENDI';
        ELSIF rnd < 0.55 THEN
            odenen := tahakkuk_tutari * 0.5;
            kalan := tahakkuk_tutari * 0.5;
            durum := 'KISMI_ODENDI';
        ELSE
            odenen := 0;
            kalan := tahakkuk_tutari;
            durum := 'BEKLIYOR';
        END IF;
        
        INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tarihi, tahakkuk_tutari, toplam_borc, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, tenant_id, created_at, updated_at)
        SELECT donem_id, uye_rec.id, uye_rec.birlik_id, '2026-01-01', tahakkuk_tutari, tahakkuk_tutari, odenen, kalan, durum, '2026-07-15', true, 1, NOW(), NOW()
        WHERE NOT EXISTS (SELECT 1 FROM aidatlar WHERE aidat_donemi_id = donem_id AND uye_id = uye_rec.id);
    END LOOP;
END $$;

-- 12. ALT BİRLİK YÖNETİCİLERİ
-- İstanbul Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre, ad, soyad, telefon, rol, durum, birlik_id, is_active, hesap_kilitli, iki_faktor_aktif, basarisiz_giris_sayisi, tenant_id, created_at, updated_at)
SELECT 'istanbul_admin', 'admin@istanbul.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Ahmet', 'Yönetici', '05321112233', 'BIRLIK_YONETICI', 'AKTIF',
       (SELECT id FROM birlikler WHERE birlik_kodu = 'IST001'), true, false, false, 0, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'istanbul_admin');

-- Ankara Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre, ad, soyad, telefon, rol, durum, birlik_id, is_active, hesap_kilitli, iki_faktor_aktif, basarisiz_giris_sayisi, tenant_id, created_at, updated_at)
SELECT 'ankara_admin', 'admin@ankara.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Fatma', 'Yönetici', '05331112233', 'BIRLIK_YONETICI', 'AKTIF',
       (SELECT id FROM birlikler WHERE birlik_kodu = 'ANK001'), true, false, false, 0, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'ankara_admin');

-- İzmir Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre, ad, soyad, telefon, rol, durum, birlik_id, is_active, hesap_kilitli, iki_faktor_aktif, basarisiz_giris_sayisi, tenant_id, created_at, updated_at)
SELECT 'izmir_admin', 'admin@izmir.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Kemal', 'Yönetici', '05341112233', 'BIRLIK_YONETICI', 'AKTIF',
       (SELECT id FROM birlikler WHERE birlik_kodu = 'IZM001'), true, false, false, 0, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'izmir_admin');

-- Bursa Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre, ad, soyad, telefon, rol, durum, birlik_id, is_active, hesap_kilitli, iki_faktor_aktif, basarisiz_giris_sayisi, tenant_id, created_at, updated_at)
SELECT 'bursa_admin', 'admin@bursa.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Oğuz', 'Yönetici', '05351112233', 'BIRLIK_YONETICI', 'AKTIF',
       (SELECT id FROM birlikler WHERE birlik_kodu = 'BRS001'), true, false, false, 0, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'bursa_admin');

-- Antalya Birliği Yöneticisi
INSERT INTO kullanicilar (kullanici_adi, email, sifre, ad, soyad, telefon, rol, durum, birlik_id, is_active, hesap_kilitli, iki_faktor_aktif, basarisiz_giris_sayisi, tenant_id, created_at, updated_at)
SELECT 'antalya_admin', 'admin@antalya.tuketbir.gov.tr', '$2a$10$N9qo8uLOickgx2ZMRZoMy.q1H7VYGDnU8X6pSeCc1k1T6.H6SQoKS', 'Deniz', 'Yönetici', '05361112233', 'BIRLIK_YONETICI', 'AKTIF',
       (SELECT id FROM birlikler WHERE birlik_kodu = 'ANT001'), true, false, false, 0, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM kullanicilar WHERE kullanici_adi = 'antalya_admin');

-- Özet bilgiler
SELECT '=======================================' AS "---";
SELECT 'Sentetik veri yükleme tamamlandı!' AS mesaj;
SELECT '=======================================' AS "---";
SELECT 'Birlik Sayısı: ' || COUNT(*) AS sonuc FROM birlikler;
SELECT 'Üye Sayısı: ' || COUNT(*) AS sonuc FROM uyeler;
SELECT 'Aktif Üye Sayısı: ' || COUNT(*) AS sonuc FROM uyeler WHERE uye_durum = 'AKTIF';
SELECT 'Aidat Dönemi Sayısı: ' || COUNT(*) AS sonuc FROM aidat_donemleri;
SELECT 'Aidat Kaydı Sayısı: ' || COUNT(*) AS sonuc FROM aidatlar;
SELECT 'Kullanıcı Sayısı: ' || COUNT(*) AS sonuc FROM kullanicilar;
SELECT '=======================================' AS "---";
SELECT 'Birlik bazlı üye dağılımı:' AS bilgi;
SELECT b.birlik_adi, COUNT(u.id) AS uye_sayisi
FROM birlikler b
LEFT JOIN uyeler u ON u.birlik_id = b.id
GROUP BY b.id, b.birlik_adi
ORDER BY b.birlik_adi;
