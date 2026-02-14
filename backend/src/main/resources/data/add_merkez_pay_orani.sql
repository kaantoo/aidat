-- Merkez pay oranı kolonu ekle
ALTER TABLE aidat_donemleri ADD COLUMN IF NOT EXISTS merkez_pay_orani NUMERIC(5,2);

-- Tutar kolonunu nullable yap (merkez birlik için tutar yerine pay oranı kullanılacak)
ALTER TABLE aidat_donemleri ALTER COLUMN tutar DROP NOT NULL;

-- Mevcut dönemlere varsayılan merkez pay oranı ata (%10)
UPDATE aidat_donemleri SET merkez_pay_orani = 10.00 WHERE merkez_pay_orani IS NULL;
