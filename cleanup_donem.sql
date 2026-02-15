-- Delete aidatlar linked to broken donem 1
DELETE FROM aidatlar WHERE aidat_donemi_id = 1;

-- Delete the broken donem
DELETE FROM aidat_donemleri WHERE id = 1;

-- Verify
SELECT id, donem_kodu, tutar, birlik_id FROM aidat_donemleri ORDER BY id;
