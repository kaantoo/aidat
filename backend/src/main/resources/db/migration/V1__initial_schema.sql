-- Flyway Migration V1: Initial Schema
-- Tuketbir Aidat Yönetim Sistemi

-- =====================================================
-- ENUM TYPES (PostgreSQL)
-- =====================================================

CREATE TYPE birlik_tipi AS ENUM ('MERKEZ_BIRLIGI', 'IL_BIRLIGI', 'ILCE_BIRLIGI');
CREATE TYPE uyelik_tipi AS ENUM ('URETICI', 'ISLETME', 'KOOPERATIF', 'DIGER');
CREATE TYPE uye_durum AS ENUM ('AKTIF', 'PASIF', 'ASKIYA_ALINMIS', 'IHRAC_EDILMIS');
CREATE TYPE cinsiyet AS ENUM ('ERKEK', 'KADIN');
CREATE TYPE donem_tipi AS ENUM ('AYLIK', 'UCAYLIK', 'ALTIAYLIK', 'YILLIK');
CREATE TYPE aidat_durum AS ENUM ('BEKLIYOR', 'KISMI_ODENDI', 'ODENDI', 'GECIKTI', 'IPTAL');
CREATE TYPE odeme_tipi AS ENUM ('NAKIT', 'KREDI_KARTI', 'HAVALE_EFT', 'CEK', 'SENET', 'DIGER');
CREATE TYPE gelir_gider_tipi AS ENUM ('GELIR', 'GIDER');
CREATE TYPE gelir_kategorisi AS ENUM ('AIDAT_GELIRI', 'DEVLET_DESTEGI', 'BAGIS', 'FAIZ_GELIRI', 'DIGER_GELIR');
CREATE TYPE gider_kategorisi AS ENUM ('PERSONEL_GIDERI', 'KIRA_GIDERI', 'ELEKTRIK_SU', 'ILETISIM_GIDERI', 'SEYAHAT_GIDERI', 'MALZEME_GIDERI', 'BAKIM_ONARIM', 'VERGI_HARÇ', 'DIGER_GIDER');
CREATE TYPE belge_tipi AS ENUM ('KIMLIK_FOTOKOPISI', 'IKAMETGAH', 'TAPU_BELGESI', 'ISLETME_BELGESI', 'UYELIK_FORMU', 'AIDAT_MAKBUZU', 'DIGER');
CREATE TYPE kullanici_rol AS ENUM ('MERKEZ_YONETICI', 'BIRLIK_YONETICISI', 'BIRLIK_PERSONELI', 'MUHASEBE_SORUMLUSU', 'GOZLEMCI');
CREATE TYPE kullanici_durum AS ENUM ('AKTIF', 'PASIF', 'KILITLI', 'SIFRESI_SURESI_DOLMUS');
CREATE TYPE audit_islem_tipi AS ENUM ('GIRIS', 'CIKIS', 'OLUSTURMA', 'GUNCELLEME', 'SILME', 'GORUNTULEME', 'DIGER');

-- =====================================================
-- TABLES
-- =====================================================

-- Birlik Tablosu
CREATE TABLE birlikler (
    id BIGSERIAL PRIMARY KEY,
    birlik_kodu VARCHAR(10) NOT NULL UNIQUE,
    birlik_adi VARCHAR(200) NOT NULL,
    birlik_tipi birlik_tipi NOT NULL,
    il_kodu VARCHAR(2) NOT NULL,
    ilce_kodu VARCHAR(10),
    adres VARCHAR(500),
    telefon VARCHAR(20),
    email VARCHAR(150),
    vergi_no VARCHAR(11),
    vergi_dairesi VARCHAR(100),
    iban_no VARCHAR(34),
    ust_birlik_id BIGINT REFERENCES birlikler(id),
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_birlikler_birlik_kodu ON birlikler(birlik_kodu);
CREATE INDEX idx_birlikler_il_kodu ON birlikler(il_kodu);
CREATE INDEX idx_birlikler_tenant_id ON birlikler(tenant_id);
CREATE INDEX idx_birlikler_ust_birlik ON birlikler(ust_birlik_id);

-- Üye Tablosu
CREATE TABLE uyeler (
    id BIGSERIAL PRIMARY KEY,
    uye_no VARCHAR(30) NOT NULL UNIQUE,
    tc_kimlik_no VARCHAR(11) NOT NULL,
    ad VARCHAR(100) NOT NULL,
    soyad VARCHAR(100) NOT NULL,
    cinsiyet cinsiyet,
    dogum_tarihi DATE,
    uyelik_tipi uyelik_tipi NOT NULL,
    uye_durum uye_durum DEFAULT 'AKTIF',
    katilim_tarihi DATE,
    ayrilma_tarihi DATE,
    adres VARCHAR(500),
    telefon VARCHAR(20),
    email VARCHAR(150),
    isletme_adi VARCHAR(200),
    isletme_adresi VARCHAR(500),
    hayvan_sayisi INTEGER DEFAULT 0,
    birlik_id BIGINT NOT NULL REFERENCES birlikler(id),
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_uyeler_uye_no ON uyeler(uye_no);
CREATE INDEX idx_uyeler_tc_kimlik_no ON uyeler(tc_kimlik_no);
CREATE INDEX idx_uyeler_birlik_id ON uyeler(birlik_id);
CREATE INDEX idx_uyeler_tenant_id ON uyeler(tenant_id);
CREATE INDEX idx_uyeler_ad_soyad ON uyeler(ad, soyad);
CREATE INDEX idx_uyeler_uyelik_tipi ON uyeler(uyelik_tipi);

-- Aidat Dönemi Tablosu
CREATE TABLE aidat_donemleri (
    id BIGSERIAL PRIMARY KEY,
    donem_adi VARCHAR(100) NOT NULL,
    donem_tipi donem_tipi NOT NULL,
    yil INTEGER NOT NULL,
    ay INTEGER,
    baslangic_tarihi DATE NOT NULL,
    bitis_tarihi DATE NOT NULL,
    son_odeme_tarihi DATE,
    aidat_tutari DECIMAL(15,2) NOT NULL,
    asgari_ucret_tutari DECIMAL(15,2),
    gecikme_orani DECIMAL(5,2) DEFAULT 0,
    birlik_id BIGINT REFERENCES birlikler(id),
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_aidat_donemleri_yil ON aidat_donemleri(yil);
CREATE INDEX idx_aidat_donemleri_birlik ON aidat_donemleri(birlik_id);
CREATE INDEX idx_aidat_donemleri_tenant_id ON aidat_donemleri(tenant_id);

-- Aidat Tablosu
CREATE TABLE aidatlar (
    id BIGSERIAL PRIMARY KEY,
    uye_id BIGINT NOT NULL REFERENCES uyeler(id),
    aidat_donemi_id BIGINT NOT NULL REFERENCES aidat_donemleri(id),
    birlik_id BIGINT NOT NULL REFERENCES birlikler(id),
    tahakkuk_tutari DECIMAL(15,2) NOT NULL,
    odenen_tutar DECIMAL(15,2) DEFAULT 0,
    kalan_tutar DECIMAL(15,2) NOT NULL,
    gecikme_tutari DECIMAL(15,2) DEFAULT 0,
    durumu aidat_durum DEFAULT 'BEKLIYOR',
    son_odeme_tarihi DATE,
    vade_tarihi DATE,
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE(uye_id, aidat_donemi_id)
);

CREATE INDEX idx_aidatlar_uye_id ON aidatlar(uye_id);
CREATE INDEX idx_aidatlar_aidat_donemi_id ON aidatlar(aidat_donemi_id);
CREATE INDEX idx_aidatlar_birlik_id ON aidatlar(birlik_id);
CREATE INDEX idx_aidatlar_durumu ON aidatlar(durumu);
CREATE INDEX idx_aidatlar_tenant_id ON aidatlar(tenant_id);

-- Tahsilat Tablosu
CREATE TABLE tahsilatlar (
    id BIGSERIAL PRIMARY KEY,
    aidat_id BIGINT NOT NULL REFERENCES aidatlar(id),
    odeme_tipi odeme_tipi NOT NULL,
    tutar DECIMAL(15,2) NOT NULL,
    odeme_tarihi DATE NOT NULL,
    makbuz_no VARCHAR(50),
    dekont_no VARCHAR(50),
    banka_dekontu_no VARCHAR(50),
    aciklama VARCHAR(500),
    islem_yapan_kullanici_id BIGINT,
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_tahsilatlar_aidat_id ON tahsilatlar(aidat_id);
CREATE INDEX idx_tahsilatlar_odeme_tarihi ON tahsilatlar(odeme_tarihi);
CREATE INDEX idx_tahsilatlar_tenant_id ON tahsilatlar(tenant_id);

-- Gelir/Gider Tablosu
CREATE TABLE gelir_giderler (
    id BIGSERIAL PRIMARY KEY,
    tipi gelir_gider_tipi NOT NULL,
    gelir_kategorisi gelir_kategorisi,
    gider_kategorisi gider_kategorisi,
    tutar DECIMAL(15,2) NOT NULL,
    islem_tarihi DATE NOT NULL,
    belge_no VARCHAR(50),
    aciklama VARCHAR(1000),
    birlik_id BIGINT REFERENCES birlikler(id),
    kaydeden_kullanici_id BIGINT,
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_gelir_giderler_tipi ON gelir_giderler(tipi);
CREATE INDEX idx_gelir_giderler_islem_tarihi ON gelir_giderler(islem_tarihi);
CREATE INDEX idx_gelir_giderler_birlik_id ON gelir_giderler(birlik_id);
CREATE INDEX idx_gelir_giderler_tenant_id ON gelir_giderler(tenant_id);

-- Belge Tablosu
CREATE TABLE belgeler (
    id BIGSERIAL PRIMARY KEY,
    belge_tipi belge_tipi NOT NULL,
    belge_no VARCHAR(50),
    dosya_adi VARCHAR(255) NOT NULL,
    dosya_yolu VARCHAR(500) NOT NULL,
    dosya_boyutu BIGINT,
    mime_type VARCHAR(100),
    aciklama VARCHAR(500),
    uye_id BIGINT REFERENCES uyeler(id),
    birlik_id BIGINT REFERENCES birlikler(id),
    yukleyen_kullanici_id BIGINT,
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_belgeler_uye_id ON belgeler(uye_id);
CREATE INDEX idx_belgeler_birlik_id ON belgeler(birlik_id);
CREATE INDEX idx_belgeler_belge_tipi ON belgeler(belge_tipi);
CREATE INDEX idx_belgeler_tenant_id ON belgeler(tenant_id);

-- Kullanıcı Tablosu
CREATE TABLE kullanicilar (
    id BIGSERIAL PRIMARY KEY,
    kullanici_adi VARCHAR(50) NOT NULL UNIQUE,
    sifre VARCHAR(256) NOT NULL,
    ad VARCHAR(100) NOT NULL,
    soyad VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    telefon VARCHAR(20),
    tc_kimlik_no VARCHAR(11),
    rol kullanici_rol NOT NULL,
    durum kullanici_durum DEFAULT 'AKTIF',
    birlik_id BIGINT REFERENCES birlikler(id),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(64),
    son_giris_tarihi TIMESTAMP,
    sifre_degistirilme_tarihi TIMESTAMP,
    basarisiz_giris_sayisi INTEGER DEFAULT 0,
    tenant_id VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_kullanicilar_kullanici_adi ON kullanicilar(kullanici_adi);
CREATE INDEX idx_kullanicilar_email ON kullanicilar(email);
CREATE INDEX idx_kullanicilar_birlik_id ON kullanicilar(birlik_id);
CREATE INDEX idx_kullanicilar_rol ON kullanicilar(rol);
CREATE INDEX idx_kullanicilar_tenant_id ON kullanicilar(tenant_id);

-- Refresh Token Tablosu
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    kullanici_id BIGINT NOT NULL REFERENCES kullanicilar(id),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_kullanici_id ON refresh_tokens(kullanici_id);

-- Audit Log Tablosu
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    islem_tipi audit_islem_tipi NOT NULL,
    kullanici_id BIGINT,
    kullanici_adi VARCHAR(50),
    entity_tipi VARCHAR(100),
    entity_id BIGINT,
    eski_deger TEXT,
    yeni_deger TEXT,
    ip_adresi VARCHAR(45),
    user_agent VARCHAR(500),
    tenant_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_islem_tipi ON audit_logs(islem_tipi);
CREATE INDEX idx_audit_logs_kullanici_id ON audit_logs(kullanici_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_tipi, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);

-- =====================================================
-- INITIAL DATA
-- =====================================================

-- Merkez Birliği
INSERT INTO birlikler (birlik_kodu, birlik_adi, birlik_tipi, il_kodu, adres, telefon, email, tenant_id, active)
VALUES ('TUKETBIR', 'Türkiye Kırmızı Et Üreticileri Merkez Birliği', 'MERKEZ_BIRLIGI', '06', 
        'Ankara', '0312 000 00 00', 'info@tuketbir.org.tr', 'tuketbir', true);

-- Admin Kullanıcı (şifre: Admin123!)
INSERT INTO kullanicilar (kullanici_adi, sifre, ad, soyad, email, rol, durum, tenant_id, sifre_degistirilme_tarihi)
VALUES ('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4rCzQjRRByVfCMrS', 
        'Sistem', 'Yöneticisi', 'admin@tuketbir.org.tr', 'MERKEZ_YONETICI', 'AKTIF', 'tuketbir', CURRENT_TIMESTAMP);

-- =====================================================
-- FUNCTIONS & TRIGGERS
-- =====================================================

-- updated_at trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all tables with updated_at
CREATE TRIGGER update_birlikler_updated_at BEFORE UPDATE ON birlikler FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_uyeler_updated_at BEFORE UPDATE ON uyeler FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_aidat_donemleri_updated_at BEFORE UPDATE ON aidat_donemleri FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_aidatlar_updated_at BEFORE UPDATE ON aidatlar FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tahsilatlar_updated_at BEFORE UPDATE ON tahsilatlar FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_gelir_giderler_updated_at BEFORE UPDATE ON gelir_giderler FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_belgeler_updated_at BEFORE UPDATE ON belgeler FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_kullanicilar_updated_at BEFORE UPDATE ON kullanicilar FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Üye No Sequence (birlik bazında)
CREATE SEQUENCE IF NOT EXISTS uye_no_seq START 1;
