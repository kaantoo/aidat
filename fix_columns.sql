-- Hangi tablolarda varchar created_by/updated_by var?
SELECT table_name, column_name, data_type 
FROM information_schema.columns 
WHERE column_name IN ('created_by','updated_by') 
AND data_type='character varying' 
ORDER BY table_name;

-- Fix: VARCHAR -> BIGINT using CAST
ALTER TABLE sistem_ayarlari ALTER COLUMN created_by TYPE bigint USING CASE WHEN created_by IS NULL THEN NULL WHEN created_by ~ '^\d+$' THEN created_by::bigint ELSE NULL END;
ALTER TABLE sistem_ayarlari ALTER COLUMN updated_by TYPE bigint USING CASE WHEN updated_by IS NULL THEN NULL WHEN updated_by ~ '^\d+$' THEN updated_by::bigint ELSE NULL END;
