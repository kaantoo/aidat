-- Check aidatlar linked to donem 1
SELECT count(*) as aidat_count FROM aidatlar WHERE aidat_donemi_id = 1;

-- Check synthetic data donemleri
SELECT id, donem_kodu, donem_adi FROM aidat_donemleri ORDER BY id;
