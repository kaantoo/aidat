# Kırmızı Et Üreticileri Merkez Birliği - Aidat ve Yönetim Sistemi

Bu proje, 109 alt birlik ve yaklaşık 100.000 üreticinin aidat ve üyelik yönetimini sağlayan kurumsal ölçekte bir web uygulamasıdır.

## 🏗️ Proje Mimarisi

### Backend (Spring Boot 3.2.0)
- **Java 21 LTS**
- **Spring Security 6** - JWT + Refresh Token tabanlı kimlik doğrulama
- **Spring Data JPA** - PostgreSQL veritabanı ile
- **Flyway** - Veritabanı migration yönetimi
- **Multi-tenant Architecture** - Discriminator column pattern

### Frontend (React 18)
- **TypeScript**
- **Ant Design 5** - UI Component Library
- **Vite** - Build tool
- **TailwindCSS** - Utility-first CSS
- **Zustand** - State management
- **React Query** - Server state management

### Infrastructure
- **PostgreSQL 16** - Ana veritabanı
- **Redis 7** - Cache ve session yönetimi
- **MinIO** - Belge depolama (S3 uyumlu)
- **Nginx** - Reverse proxy ve load balancer
- **Docker & Docker Compose** - Containerization

## 📁 Proje Yapısı

```
tuketbir_aidat/
├── backend/
│   └── src/main/java/tr/gov/tuketbir/
│       ├── config/          # Güvenlik, multitenancy yapılandırmaları
│       ├── controller/      # REST API endpoint'leri
│       ├── domain/
│       │   ├── entity/      # JPA entity sınıfları
│       │   └── enums/       # Enum tanımları
│       ├── dto/             # Data Transfer Objects
│       ├── exception/       # Exception handling
│       ├── repository/      # Spring Data repositories
│       └── service/         # Business logic katmanı
├── frontend/
│   └── src/
│       ├── api/             # API client modülleri
│       ├── layouts/         # Sayfa layout'ları
│       ├── pages/           # Sayfa bileşenleri
│       ├── routes/          # React Router yapılandırması
│       ├── store/           # Zustand state stores
│       └── types/           # TypeScript type tanımları
├── docker/
│   └── nginx/               # Nginx yapılandırması
├── docs/                    # Proje dokümantasyonu
└── docker-compose.yml
```

## 🔐 Güvenlik

### Kimlik Doğrulama
- JWT Access Token (1 saat geçerlilik)
- Refresh Token (7 gün geçerlilik)
- İki Faktörlü Doğrulama (TOTP)

### Yetkilendirme (RBAC)
| Rol | Yetkiler |
|-----|----------|
| SISTEM_ADMIN | Tam yetki |
| MERKEZ_YONETICI | Tüm birlikler üzerinde yönetim |
| BIRLIK_YONETICI | Kendi birliği üzerinde tam yetki |
| BIRLIK_PERSONEL | Kendi birliğinde işlem yapma |
| IZLEYICI | Sadece görüntüleme |

### Multi-tenancy
- Her birlik kendi verilerini görebilir
- Merkez yöneticiler tüm verilere erişebilir
- Tenant izolasyonu discriminator column ile sağlanır

## 🚀 Kurulum

### Gereksinimler
- Java 21+
- Node.js 20+
- Docker & Docker Compose
- PostgreSQL 16 (Docker ile sağlanır)

### Development Ortamı

1. **Backend başlatma:**
```bash
cd backend
./mvnw spring-boot:run
```

2. **Frontend başlatma:**
```bash
cd frontend
npm install
npm run dev
```

3. **Docker ile tam ortam:**
```bash
docker-compose up -d
```

### Ortam Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|------------|
| SPRING_DATASOURCE_URL | PostgreSQL bağlantı URL'i | jdbc:postgresql://localhost:5432/tuketbir_db |
| JWT_SECRET | JWT imzalama anahtarı | - |
| JWT_EXPIRATION | Access token geçerlilik süresi (ms) | 3600000 |
| MINIO_ENDPOINT | MinIO endpoint URL'i | http://localhost:9000 |

## 📊 Modüller

### 1. Birlik Yönetimi
- 109 alt birliğin kayıt ve yönetimi
- İl/İlçe bazlı organizasyon
- Birlik istatistikleri

### 2. Üye Yönetimi
- ~100.000 üretici kaydı
- TC Kimlik doğrulaması (Luhn algoritması)
- Otomatik üye numarası: `BİRLİK_KODU-YIL-SIRA`

### 3. Aidat Yönetimi
- Dönem tanımlama (Aylık/Üç Aylık/Altı Aylık/Yıllık)
- Toplu tahakkuk
- Gecikme zammı hesaplaması
- Tahsilat işlemleri

### 4. Gelir-Gider Takibi
- Kategori bazlı gelir/gider kaydı
- Mali raporlama
- Bütçe takibi

### 5. Belge Yönetimi
- Üye belgeleri
- Makbuz/Fatura arşivi
- MinIO ile dosya depolama

### 6. Raporlama
- Dashboard istatistikleri
- Birlik bazlı raporlar
- Aidat tahsilat raporları
- Excel/PDF export

## 🔧 API Dokümantasyonu

API dokümantasyonuna erişim:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

## 📝 Lisans

Bu proje Kırmızı Et Üreticileri Merkez Birliği'ne aittir.

## 👥 İletişim

- **Proje Yöneticisi:** TÜRKK-BİR IT Ekibi
- **E-posta:** it@tuketbir.gov.tr
