-- Üyeleri doğrudan ekle - TC kimlik numaraları düzeltildi
DO $$
DECLARE
    ist_id BIGINT;
    ank_id BIGINT;
    izm_id BIGINT;
    brs_id BIGINT;
    ant_id BIGINT;
    i INTEGER;
    tc_no VARCHAR(11);
BEGIN
    SELECT id INTO ist_id FROM birlikler WHERE birlik_kodu = 'IST001';
    SELECT id INTO ank_id FROM birlikler WHERE birlik_kodu = 'ANK001';
    SELECT id INTO izm_id FROM birlikler WHERE birlik_kodu = 'IZM001';
    SELECT id INTO brs_id FROM birlikler WHERE birlik_kodu = 'BRS001';
    SELECT id INTO ant_id FROM birlikler WHERE birlik_kodu = 'ANT001';

    -- İstanbul üyeleri (TC: 10000000001 - 10000000020)
    FOR i IN 1..20 LOOP
        tc_no := LPAD((10000000000 + i)::TEXT, 11, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        VALUES (
            'IST-' || LPAD(i::TEXT, 4, '0'),
            CASE WHEN i % 5 = 1 THEN 'Ahmet' WHEN i % 5 = 2 THEN 'Mehmet' WHEN i % 5 = 3 THEN 'Ali' WHEN i % 5 = 4 THEN 'Mustafa' ELSE 'Hasan' END,
            CASE WHEN i % 4 = 1 THEN 'Yilmaz' WHEN i % 4 = 2 THEN 'Kaya' WHEN i % 4 = 3 THEN 'Demir' ELSE 'Celik' END,
            tc_no,
            '0532555' || LPAD(i::TEXT, 4, '0'),
            'ist_uye' || i || '@email.com',
            'Istanbul',
            CASE WHEN i % 3 = 1 THEN 'Kadikoy' WHEN i % 3 = 2 THEN 'Besiktas' ELSE 'Uskudar' END,
            CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
            CASE WHEN i <= 18 THEN 'AKTIF' ELSE 'PASIF' END,
            CURRENT_DATE - (i * 30),
            ist_id, true, 1, NOW(), NOW()
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- Ankara üyeleri (TC: 20000000001 - 20000000020)
    FOR i IN 1..20 LOOP
        tc_no := LPAD((20000000000 + i)::TEXT, 11, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        VALUES (
            'ANK-' || LPAD(i::TEXT, 4, '0'),
            CASE WHEN i % 5 = 1 THEN 'Fatma' WHEN i % 5 = 2 THEN 'Ayse' WHEN i % 5 = 3 THEN 'Zeynep' WHEN i % 5 = 4 THEN 'Elif' ELSE 'Seda' END,
            CASE WHEN i % 4 = 1 THEN 'Arslan' WHEN i % 4 = 2 THEN 'Sahin' WHEN i % 4 = 3 THEN 'Kurt' ELSE 'Aslan' END,
            tc_no,
            '0533555' || LPAD(i::TEXT, 4, '0'),
            'ank_uye' || i || '@email.com',
            'Ankara',
            CASE WHEN i % 3 = 1 THEN 'Cankaya' WHEN i % 3 = 2 THEN 'Yenimahalle' ELSE 'Kecioren' END,
            CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
            CASE WHEN i <= 17 THEN 'AKTIF' ELSE 'PASIF' END,
            CURRENT_DATE - (i * 30),
            ank_id, true, 1, NOW(), NOW()
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- İzmir üyeleri (TC: 30000000001 - 30000000020)
    FOR i IN 1..20 LOOP
        tc_no := LPAD((30000000000 + i)::TEXT, 11, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        VALUES (
            'IZM-' || LPAD(i::TEXT, 4, '0'),
            CASE WHEN i % 5 = 1 THEN 'Serkan' WHEN i % 5 = 2 THEN 'Tolga' WHEN i % 5 = 3 THEN 'Onur' WHEN i % 5 = 4 THEN 'Baris' ELSE 'Guven' END,
            CASE WHEN i % 4 = 1 THEN 'Ozdemir' WHEN i % 4 = 2 THEN 'Aydin' WHEN i % 4 = 3 THEN 'Yildiz' ELSE 'Gunes' END,
            tc_no,
            '0534555' || LPAD(i::TEXT, 4, '0'),
            'izm_uye' || i || '@email.com',
            'Izmir',
            CASE WHEN i % 3 = 1 THEN 'Konak' WHEN i % 3 = 2 THEN 'Karsiyaka' ELSE 'Bornova' END,
            CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
            CASE WHEN i <= 16 THEN 'AKTIF' ELSE 'PASIF' END,
            CURRENT_DATE - (i * 30),
            izm_id, true, 1, NOW(), NOW()
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- Bursa üyeleri (TC: 40000000001 - 40000000020)
    FOR i IN 1..20 LOOP
        tc_no := LPAD((40000000000 + i)::TEXT, 11, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        VALUES (
            'BRS-' || LPAD(i::TEXT, 4, '0'),
            CASE WHEN i % 5 = 1 THEN 'Oguz' WHEN i % 5 = 2 THEN 'Kaan' WHEN i % 5 = 3 THEN 'Berk' WHEN i % 5 = 4 THEN 'Arda' ELSE 'Yusuf' END,
            CASE WHEN i % 4 = 1 THEN 'Koc' WHEN i % 4 = 2 THEN 'Ozturk' WHEN i % 4 = 3 THEN 'Polat' ELSE 'Erdem' END,
            tc_no,
            '0535555' || LPAD(i::TEXT, 4, '0'),
            'brs_uye' || i || '@email.com',
            'Bursa',
            CASE WHEN i % 3 = 1 THEN 'Osmangazi' WHEN i % 3 = 2 THEN 'Nilufer' ELSE 'Yildirim' END,
            CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
            CASE WHEN i <= 15 THEN 'AKTIF' ELSE 'PASIF' END,
            CURRENT_DATE - (i * 30),
            brs_id, true, 1, NOW(), NOW()
        ) ON CONFLICT DO NOTHING;
    END LOOP;

    -- Antalya üyeleri (TC: 50000000001 - 50000000020)
    FOR i IN 1..20 LOOP
        tc_no := LPAD((50000000000 + i)::TEXT, 11, '0');
        INSERT INTO uyeler (uye_no, ad, soyad, tc_kimlik_no, cep_telefon, email, il_adi, ilce_adi, uyelik_tipi, uye_durum, katilim_tarihi, birlik_id, is_active, tenant_id, created_at, updated_at)
        VALUES (
            'ANT-' || LPAD(i::TEXT, 4, '0'),
            CASE WHEN i % 5 = 1 THEN 'Deniz' WHEN i % 5 = 2 THEN 'Ege' WHEN i % 5 = 3 THEN 'Umut' WHEN i % 5 = 4 THEN 'Can' ELSE 'Mert' END,
            CASE WHEN i % 4 = 1 THEN 'Akdeniz' WHEN i % 4 = 2 THEN 'Denizci' WHEN i % 4 = 3 THEN 'Sahil' ELSE 'Kumsal' END,
            tc_no,
            '0536555' || LPAD(i::TEXT, 4, '0'),
            'ant_uye' || i || '@email.com',
            'Antalya',
            CASE WHEN i % 3 = 1 THEN 'Muratpasa' WHEN i % 3 = 2 THEN 'Kepez' ELSE 'Konyaalti' END,
            CASE WHEN i % 2 = 0 THEN 'GERCEK_KISI' ELSE 'TUZEL_KISI' END,
            CASE WHEN i <= 14 THEN 'AKTIF' ELSE 'PASIF' END,
            CURRENT_DATE - (i * 30),
            ant_id, true, 1, NOW(), NOW()
        ) ON CONFLICT DO NOTHING;
    END LOOP;
    
    RAISE NOTICE 'Üyeler eklendi!';
END $$;

-- Üyelere aidat tahakkuku oluştur
DO $$
DECLARE
    donem_rec RECORD;
    uye_rec RECORD;
    tahakkuk_tutari DECIMAL(12,2);
    odenen DECIMAL(12,2);
    kalan DECIMAL(12,2);
    durum VARCHAR(20);
    rnd FLOAT;
BEGIN
    -- Her dönem için
    FOR donem_rec IN SELECT id, donem_kodu, tutar, son_odeme_tarihi FROM aidat_donemleri WHERE donem_kodu LIKE 'AID-%' LOOP
        tahakkuk_tutari := donem_rec.tutar;
        
        -- Her aktif üye için
        FOR uye_rec IN SELECT id, birlik_id FROM uyeler WHERE uye_durum = 'AKTIF' AND is_active = true LOOP
            rnd := random();
            
            IF donem_rec.donem_kodu = 'AID-2026-1' THEN
                -- Güncel dönem - daha az ödeme
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
            ELSIF donem_rec.donem_kodu = 'AID-2025-2' THEN
                -- 2025 2. yarıyıl
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
            ELSE
                -- 2025 1. yarıyıl
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
            END IF;
            
            INSERT INTO aidatlar (aidat_donemi_id, uye_id, birlik_id, tahakkuk_tarihi, tahakkuk_tutari, toplam_borc, odenen_tutar, kalan_borc, aidat_durum, son_odeme_tarihi, is_active, tenant_id, created_at, updated_at)
            VALUES (donem_rec.id, uye_rec.id, uye_rec.birlik_id, CURRENT_DATE, tahakkuk_tutari, tahakkuk_tutari, odenen, kalan, durum, donem_rec.son_odeme_tarihi, true, 1, NOW(), NOW())
            ON CONFLICT (uye_id, aidat_donemi_id) DO NOTHING;
        END LOOP;
    END LOOP;
    
    RAISE NOTICE 'Aidatlar oluşturuldu!';
END $$;

-- Sonuçları göster
SELECT '========================================' AS "---";
SELECT 'Birlik bazlı üye sayıları:' AS bilgi;
SELECT b.birlik_kodu, b.birlik_adi, COUNT(u.id) AS toplam_uye, 
       SUM(CASE WHEN u.uye_durum = 'AKTIF' THEN 1 ELSE 0 END) AS aktif_uye
FROM birlikler b
LEFT JOIN uyeler u ON u.birlik_id = b.id
WHERE b.birlik_kodu LIKE 'IST%' OR b.birlik_kodu LIKE 'ANK%' OR b.birlik_kodu LIKE 'IZM%' OR b.birlik_kodu LIKE 'BRS%' OR b.birlik_kodu LIKE 'ANT%'
GROUP BY b.birlik_kodu, b.birlik_adi
ORDER BY b.birlik_kodu;

SELECT '========================================' AS "---";
SELECT 'Dönem bazlı aidat sayıları:' AS bilgi;
SELECT ad.donem_kodu, ad.donem_adi, ad.tutar, COUNT(a.id) AS aidat_sayisi,
       SUM(CASE WHEN a.aidat_durum = 'ODENDI' THEN 1 ELSE 0 END) AS odenen,
       SUM(CASE WHEN a.aidat_durum = 'KISMI_ODENDI' THEN 1 ELSE 0 END) AS kismi,
       SUM(CASE WHEN a.aidat_durum IN ('BEKLIYOR', 'GECIKTI') THEN 1 ELSE 0 END) AS bekleyen
FROM aidat_donemleri ad
LEFT JOIN aidatlar a ON a.aidat_donemi_id = ad.id
WHERE ad.donem_kodu LIKE 'AID-%'
GROUP BY ad.donem_kodu, ad.donem_adi, ad.tutar
ORDER BY ad.donem_kodu;

SELECT '========================================' AS "---";
SELECT 'Toplam özet:' AS bilgi;
SELECT 
    (SELECT COUNT(*) FROM birlikler WHERE birlik_tipi = 'ALT_BIRLIK') AS alt_birlik_sayisi,
    (SELECT COUNT(*) FROM uyeler) AS toplam_uye,
    (SELECT COUNT(*) FROM uyeler WHERE uye_durum = 'AKTIF') AS aktif_uye,
    (SELECT COUNT(*) FROM aidatlar) AS toplam_aidat,
    (SELECT COALESCE(SUM(tahakkuk_tutari), 0) FROM aidatlar) AS toplam_tahakkuk,
    (SELECT COALESCE(SUM(odenen_tutar), 0) FROM aidatlar) AS toplam_tahsilat;
