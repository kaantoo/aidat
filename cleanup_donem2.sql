-- First delete tahsilatlar linked to aidatlar of donem 1
DELETE FROM tahsilatlar WHERE aidat_id IN (SELECT id FROM aidatlar WHERE aidat_donemi_id = 1);

-- Then delete aidatlar linked to broken donem 1
DELETE FROM aidatlar WHERE aidat_donemi_id = 1;

-- Then delete the broken donem
DELETE FROM aidat_donemleri WHERE id = 1;

-- Verify
SELECT id, donem_kodu, tutar, birlik_id FROM aidat_donemleri ORDER BY id;
