# Kırmızı Et Üreticileri Merkez Birliği - Aidat ve Yönetim Sistemi

## Mimari Tasarım Dokümanı

### 1. Genel Bakış

Bu sistem, 109 alt birlik ve yaklaşık 100.000 üreticinin aidat ve üyelik yönetimini sağlayan kurumsal ölçekte bir GovTech uygulamasıdır.

### 2. Katmanlı Mimari

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  (React + TypeScript + Ant Design)                          │
├─────────────────────────────────────────────────────────────┤
│                    Application Layer                         │
│  (REST Controllers + DTOs + Validation)                     │
├─────────────────────────────────────────────────────────────┤
│                    Domain/Service Layer                      │
│  (Business Logic + Transaction Management)                  │
├─────────────────────────────────────────────────────────────┤
│                    Infrastructure Layer                      │
│  (JPA Repositories + External Services)                     │
├─────────────────────────────────────────────────────────────┤
│                    Database Layer                            │
│  (PostgreSQL + Flyway Migrations)                           │
└─────────────────────────────────────────────────────────────┘
```

### 3. Multi-Tenancy Stratejisi

Discriminator Column Pattern kullanılmaktadır:
- Her tablo `tenant_id` kolonuna sahiptir
- `TenantContext` ThreadLocal ile tenant bilgisi taşınır
- JPA Entity Listeners ile otomatik filtering
- Merkez kullanıcıları tüm tenant'lara erişebilir

### 4. Güvenlik Mimarisi

#### 4.1 Authentication Flow
```
1. Login Request → AuthController
2. Validate Credentials → AuthenticationService
3. Check 2FA Status
4. Generate JWT + Refresh Token
5. Return Tokens to Client
6. Client stores tokens (localStorage)
7. Subsequent requests include JWT in Authorization header
```

#### 4.2 Token Yapısı
```json
{
  "sub": "kullanici_adi",
  "userId": 123,
  "tenantId": 1,
  "rol": "BIRLIK_YONETICI",
  "birlikId": 5,
  "iat": 1234567890,
  "exp": 1234571490
}
```

### 5. Veritabanı Şeması

#### Ana Tablolar
- `birlikler` - Birlik kayıtları
- `uyeler` - Üye kayıtları  
- `aidat_donemleri` - Aidat dönem tanımları
- `aidatlar` - Üye aidat tahakkukları
- `tahsilatlar` - Ödeme kayıtları
- `gelir_giderler` - Mali hareketler
- `belgeler` - Belge meta verileri
- `kullanicilar` - Sistem kullanıcıları
- `refresh_tokens` - Refresh token kayıtları
- `audit_logs` - İşlem logları

### 6. API Tasarımı

RESTful API prensipleri:
- `GET /api/uyeler` - Liste
- `GET /api/uyeler/{id}` - Detay
- `POST /api/uyeler` - Oluştur
- `PUT /api/uyeler/{id}` - Güncelle
- `DELETE /api/uyeler/{id}` - Sil
- `GET /api/uyeler/search?q=...` - Arama

### 7. Ölçeklenebilirlik

- Stateless backend (JWT-based auth)
- Connection pooling (HikariCP)
- Redis cache layer
- Horizontal scaling ready
- Load balancer (Nginx)

### 8. Deployment

#### Development
```bash
docker-compose up -d postgres redis
cd backend && ./mvnw spring-boot:run
cd frontend && npm run dev
```

#### Production
```bash
docker-compose -f docker-compose.prod.yml up -d
```

### 9. Monitoring & Logging

- Spring Boot Actuator endpoints
- Structured logging (JSON format)
- Audit log tablosu
- Health checks

### 10. Performans Hedefleri

| Metrik | Hedef |
|--------|-------|
| API Response Time (p95) | < 200ms |
| Dashboard Load Time | < 2s |
| Concurrent Users | 500+ |
| Uptime | 99.9% |
