CREATE TABLE IF NOT EXISTS sistem_ayarlari (
    id BIGSERIAL PRIMARY KEY,
    anahtar VARCHAR(255) NOT NULL UNIQUE,
    deger TEXT,
    aciklama VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    version BIGINT DEFAULT 0
);
