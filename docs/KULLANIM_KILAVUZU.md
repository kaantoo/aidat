# 📖 Kırmızı Et Üreticileri Merkez Birliği - Aidat Yönetim Sistemi
## Kullanım Kılavuzu

---

## 📑 İçindekiler

1. [Giriş](#1-giriş)
2. [Sistem Gereksinimleri](#2-sistem-gereksinimleri)
3. [Sisteme Giriş](#3-sisteme-giriş)
4. [Dashboard (Ana Sayfa)](#4-dashboard-ana-sayfa)
5. [Birlik Yönetimi](#5-birlik-yönetimi)
6. [Üye Yönetimi](#6-üye-yönetimi)
7. [Aidat Yönetimi](#7-aidat-yönetimi)
8. [Gelir-Gider Takibi](#8-gelir-gider-takibi)
9. [Belge Yönetimi](#9-belge-yönetimi)
10. [Raporlar](#10-raporlar)
11. [Kullanıcı Yönetimi](#11-kullanıcı-yönetimi)
12. [Sık Sorulan Sorular](#12-sık-sorulan-sorular)

---

## 1. Giriş

Bu sistem, Kırmızı Et Üreticileri Merkez Birliği'ne bağlı 109 alt birlik ve yaklaşık 100.000 üreticinin aidat ve üyelik yönetimini sağlamak amacıyla geliştirilmiştir.

### 1.1 Temel Özellikler

- 🏢 **Birlik Yönetimi**: Alt birliklerin kayıt ve takibi
- 👥 **Üye Yönetimi**: Üreticilerin kayıt, güncelleme ve durum takibi
- 💰 **Aidat Yönetimi**: Dönem tanımlama, tahakkuk ve tahsilat işlemleri
- 📊 **Gelir-Gider**: Mali işlemlerin kaydı ve takibi
- 📁 **Belge Yönetimi**: Üye belgelerinin dijital arşivlenmesi
- 📈 **Raporlama**: Detaylı istatistik ve raporlar

### 1.2 Kullanıcı Rolleri

| Rol | Açıklama |
|-----|----------|
| **Sistem Yöneticisi** | Tüm sistem üzerinde tam yetki |
| **Merkez Yönetici** | Tüm birlikler üzerinde yönetim yetkisi |
| **Birlik Yöneticisi** | Kendi birliği üzerinde tam yetki |
| **Birlik Personeli** | Kendi birliğinde veri girişi ve işlem yapma |
| **Muhasebe Sorumlusu** | Gelir-gider ve mali işlemler |
| **Gözlemci** | Sadece görüntüleme yetkisi |

---

## 2. Sistem Gereksinimleri

### 2.1 Tarayıcı Desteği

- ✅ Google Chrome (v90+) - Önerilen
- ✅ Mozilla Firefox (v88+)
- ✅ Microsoft Edge (v90+)
- ✅ Safari (v14+)

### 2.2 Ekran Çözünürlüğü

- **Minimum**: 1366 x 768 piksel
- **Önerilen**: 1920 x 1080 piksel

### 2.3 İnternet Bağlantısı

- Minimum 2 Mbps bağlantı hızı önerilir

---

## 3. Sisteme Giriş

### 3.1 Giriş Ekranı

1. Tarayıcınızda sistem adresine gidin: `http://localhost:5175`
2. **Kullanıcı Adı** alanına kullanıcı adınızı girin
3. **Şifre** alanına şifrenizi girin
4. **Giriş Yap** butonuna tıklayın

![Giriş Ekranı](images/login.png)

### 3.2 Varsayılan Giriş Bilgileri (Test Ortamı)

| Kullanıcı Adı | Şifre | Rol |
|---------------|-------|-----|
| admin | Admin123! | Sistem Yöneticisi |

> ⚠️ **Güvenlik Uyarısı**: İlk girişten sonra şifrenizi mutlaka değiştirin!

### 3.3 Şifre Kuralları

- En az 8 karakter
- En az 1 büyük harf
- En az 1 küçük harf
- En az 1 rakam
- En az 1 özel karakter (!@#$%^&*)

### 3.4 Şifremi Unuttum

1. Giriş ekranında "Şifremi Unuttum" linkine tıklayın
2. Kayıtlı e-posta adresinizi girin
3. E-postanıza gelen link ile şifrenizi sıfırlayın

---

## 4. Dashboard (Ana Sayfa)

Dashboard, sistemin genel durumunu gösteren özet ekranıdır.

### 4.1 İstatistik Kartları

| Kart | Açıklama |
|------|----------|
| **Toplam Birlik** | Sistemde kayıtlı birlik sayısı |
| **Toplam Üye** | Kayıtlı üretici sayısı |
| **Toplam Tahakkuk** | Dönem içi tahakkuk edilen toplam tutar |
| **Toplam Tahsilat** | Tahsil edilen toplam tutar |
| **Tahsilat Oranı** | Tahsilat/Tahakkuk yüzdesi |

### 4.2 Grafikler

- **Aylık Tahsilat Trendi**: Son 12 ayın tahsilat grafiği
- **Birlik Bazlı Dağılım**: En çok üyesi olan birlikler
- **Aidat Durumu**: Ödenen/Bekleyen aidat dağılımı

---

## 5. Birlik Yönetimi

Sol menüden **Birlikler** seçeneğine tıklayarak birlik yönetimi ekranına ulaşabilirsiniz.

### 5.1 Birlik Listesi

- Tüm birlikleri tablo halinde görüntüler
- Arama ve filtreleme yapabilirsiniz
- Her satırda düzenleme ve silme butonları bulunur

### 5.2 Yeni Birlik Ekleme

1. **+ Yeni Birlik** butonuna tıklayın
2. Formu doldurun:

| Alan | Zorunlu | Açıklama |
|------|---------|----------|
| Birlik Kodu | ✅ | Benzersiz birlik kodu (örn: BRL001) |
| Birlik Adı | ✅ | Birliğin tam adı |
| Birlik Tipi | ✅ | MERKEZ / IL_BIRLIGI / ILCE_BIRLIGI |
| İl | ✅ | Birliğin bulunduğu il |
| İlçe | - | Birliğin bulunduğu ilçe |
| Adres | - | Açık adres |
| Telefon | - | İletişim telefonu |
| E-posta | - | E-posta adresi |
| Vergi No | - | Vergi numarası |
| Vergi Dairesi | - | Bağlı vergi dairesi |

3. **Kaydet** butonuna tıklayın

### 5.3 Birlik Düzenleme

1. Listeden düzenlemek istediğiniz birliği bulun
2. **Düzenle** (kalem) ikonuna tıklayın
3. Gerekli değişiklikleri yapın
4. **Kaydet** butonuna tıklayın

### 5.4 Birlik Silme

1. Listeden silmek istediğiniz birliği bulun
2. **Sil** (çöp kutusu) ikonuna tıklayın
3. Onay kutusunda **Evet** seçeneğini tıklayın

> ⚠️ **Dikkat**: Üyesi olan birlikler silinemez!

---

## 6. Üye Yönetimi

Sol menüden **Üyeler** seçeneğine tıklayarak üye yönetimi ekranına ulaşabilirsiniz.

### 6.1 Üye Listesi

- Tüm üyeleri tablo halinde görüntüler
- TC Kimlik, Ad-Soyad, Birlik gibi alanlarda arama yapabilirsiniz
- Duruma göre (Aktif/Pasif) filtreleme yapabilirsiniz

### 6.2 Yeni Üye Ekleme

1. **+ Yeni Üye** butonuna tıklayın
2. Formu doldurun:

| Alan | Zorunlu | Açıklama |
|------|---------|----------|
| TC Kimlik No | ✅ | 11 haneli TC Kimlik Numarası |
| Ad | ✅ | Üyenin adı |
| Soyad | ✅ | Üyenin soyadı |
| Birlik | ✅ | Bağlı olduğu birlik |
| Üyelik Tipi | ✅ | URETICI / YETISTIRICI / BESICI |
| Telefon | ✅ | Cep telefonu |
| E-posta | - | E-posta adresi |
| Adres | - | Açık adres |
| İl | ✅ | İkamet ili |
| İlçe | - | İkamet ilçesi |
| İşletme No | - | Tarım Bakanlığı işletme numarası |

3. **Kaydet** butonuna tıklayın

> 💡 **İpucu**: TC Kimlik numarası otomatik olarak doğrulanır

### 6.3 Üye Numarası

Üye numarası sistem tarafından otomatik oluşturulur:

```
BİRLİK_KODU-YIL-SIRA_NO
Örnek: ANK-2026-00001
```

### 6.4 Üye Durumu Değiştirme

Üye durumları:
- **Aktif**: Normal üyelik durumu
- **Pasif**: Geçici olarak askıya alınmış
- **Çıkış**: Üyelikten ayrılmış

### 6.5 Toplu Üye Aktarımı (Import)

1. **İçe Aktar** butonuna tıklayın
2. Excel şablonunu indirin
3. Şablonu doldurun
4. Dosyayı yükleyin

---

## 7. Aidat Yönetimi

### 7.1 Aidat Dönemi Tanımlama

Sol menüden **Aidat Dönemleri** seçeneğine tıklayın.

1. **+ Yeni Dönem** butonuna tıklayın
2. Formu doldurun:

| Alan | Açıklama |
|------|----------|
| Dönem Adı | Örn: "2026 Yılı 1. Çeyrek" |
| Periyot | AYLIK / UC_AYLIK / ALTI_AYLIK / YILLIK |
| Başlangıç Tarihi | Dönemin başlangıç tarihi |
| Bitiş Tarihi | Dönemin bitiş tarihi |
| Son Ödeme Tarihi | İndirimli ödeme son tarihi |
| Aidat Tutarı | Dönem aidat miktarı (TL) |
| Gecikme Zammı (%) | Aylık gecikme faiz oranı |

3. **Kaydet** butonuna tıklayın

### 7.2 Toplu Tahakkuk İşlemi

1. Dönem listesinden ilgili dönemi seçin
2. **Toplu Tahakkuk** butonuna tıklayın
3. Tahakkuk yapılacak birlik/birlikleri seçin
4. **Tahakkuk Oluştur** butonuna tıklayın

> 💡 Toplu tahakkuk, seçilen tüm aktif üyelere otomatik aidat kaydı oluşturur.

### 7.3 Tahsilat İşlemi

1. Sol menüden **Aidatlar** seçeneğine tıklayın
2. Ödeme yapılacak üyeyi arayın
3. **Tahsilat** butonuna tıklayın
4. Ödeme bilgilerini girin:

| Alan | Açıklama |
|------|----------|
| Ödeme Tutarı | Alınan miktar |
| Ödeme Yöntemi | NAKİT / HAVALE / EFT / KREDI_KARTI |
| Makbuz No | Oluşturulan makbuz numarası |
| Açıklama | Varsa ek notlar |

5. **Tahsil Et** butonuna tıklayın

### 7.4 Gecikme Zammı Hesaplama

Sistem, son ödeme tarihinden sonraki ödemeler için otomatik gecikme zammı hesaplar:

```
Gecikme Zammı = Aidat Tutarı × Gecikme Oranı × Geciken Ay Sayısı
```

### 7.5 Aidat Durumları

| Durum | Açıklama |
|-------|----------|
| 🟡 BEKLIYOR | Henüz ödenmemiş |
| 🟢 ODENDI | Tam ödeme yapılmış |
| 🟠 KISMI_ODEME | Kısmi ödeme yapılmış |
| 🔴 GECIKTI | Son ödeme tarihi geçmiş |
| ⚪ IPTAL | İptal edilmiş |

---

## 8. Gelir-Gider Takibi

Sol menüden **Gelir/Gider** seçeneğine tıklayın.

### 8.1 Gelir Kaydı

1. **+ Yeni Gelir** butonuna tıklayın
2. Formu doldurun:

| Alan | Açıklama |
|------|----------|
| Tarih | İşlem tarihi |
| Tutar | Gelir miktarı (TL) |
| Kategori | AIDAT / BAGIS / DEVLET_DESTEGI / DIGER |
| Açıklama | İşlem açıklaması |
| Belge No | Fatura/Makbuz numarası |

3. **Kaydet** butonuna tıklayın

### 8.2 Gider Kaydı

1. **+ Yeni Gider** butonuna tıklayın
2. Formu doldurun:

| Alan | Açıklama |
|------|----------|
| Tarih | İşlem tarihi |
| Tutar | Gider miktarı (TL) |
| Kategori | PERSONEL / KIRA / FATURA / DIGER |
| Açıklama | İşlem açıklaması |
| Belge No | Fatura numarası |

3. **Kaydet** butonuna tıklayın

### 8.3 Gelir-Gider Özeti

Dashboard'da aylık gelir-gider özeti görüntülenir:
- Toplam Gelir
- Toplam Gider
- Net Durum (Gelir - Gider)

---

## 9. Belge Yönetimi

### 9.1 Belge Yükleme

1. Üye detay sayfasında **Belgeler** sekmesine tıklayın
2. **+ Belge Ekle** butonuna tıklayın
3. Belge tipini seçin:
   - Kimlik Fotokopisi
   - İşletme Belgesi
   - Başvuru Formu
   - Makbuz/Fatura
   - Diğer

4. Dosyayı sürükleyip bırakın veya **Dosya Seç** butonuna tıklayın
5. **Yükle** butonuna tıklayın

### 9.2 Desteklenen Dosya Formatları

- PDF (.pdf)
- Resim (.jpg, .jpeg, .png)
- Word (.doc, .docx)
- Excel (.xls, .xlsx)

### 9.3 Dosya Boyutu Limiti

- Maksimum dosya boyutu: **10 MB**

---

## 10. Raporlar

Sol menüden **Raporlar** seçeneğine tıklayın.

### 10.1 Mevcut Raporlar

| Rapor | Açıklama |
|-------|----------|
| **Aidat Raporu** | Birlik/dönem bazlı aidat durumu |
| **Tahsilat Raporu** | Tahsilat oranları ve detayları |
| **Üye Raporu** | Üye dağılım istatistikleri |
| **Gelir-Gider Raporu** | Mali durum özeti |
| **Birlik İstatistikleri** | Birlik bazlı performans |

### 10.2 Rapor Filtreleme

Raporları şu kriterlere göre filtreleyebilirsiniz:
- Birlik
- Yıl
- Dönem
- Tarih Aralığı

### 10.3 Rapor Dışa Aktarma

- **Excel**: Detaylı veri analizi için
- **PDF**: Yazdırma ve arşivleme için

---

## 11. Kullanıcı Yönetimi

> ⚠️ Bu bölüm sadece Sistem Yöneticisi ve Merkez Yönetici rollerine açıktır.

### 11.1 Kullanıcı Listesi

Sol menüden **Kullanıcılar** seçeneğine tıklayın.

### 11.2 Yeni Kullanıcı Ekleme

1. **+ Yeni Kullanıcı** butonuna tıklayın
2. Formu doldurun:

| Alan | Açıklama |
|------|----------|
| Kullanıcı Adı | Sisteme giriş için benzersiz ad |
| E-posta | E-posta adresi |
| Ad Soyad | Tam ad |
| Rol | Kullanıcı rolü |
| Birlik | Bağlı birlik (opsiyonel) |
| Şifre | İlk giriş şifresi |

3. **Kaydet** butonuna tıklayın

### 11.3 Kullanıcı Durumu

- **Aktif**: Normal kullanım
- **Pasif**: Giriş yapamaz
- **Kilitli**: Çok fazla hatalı giriş sonrası

### 11.4 Şifre Sıfırlama

1. Kullanıcı listesinden ilgili kullanıcıyı bulun
2. **Şifre Sıfırla** butonuna tıklayın
3. Yeni şifre otomatik oluşturulur ve e-posta ile gönderilir

---

## 12. Sık Sorulan Sorular

### S: Şifremi unuttum, ne yapmalıyım?
**C:** Giriş ekranında "Şifremi Unuttum" linkine tıklayarak kayıtlı e-posta adresinize sıfırlama linki gönderebilirsiniz.

### S: Üye numarası nasıl oluşturuluyor?
**C:** Üye numarası sistem tarafından otomatik oluşturulur: `BİRLİK_KODU-YIL-SIRA_NO` formatında.

### S: Sildiğim bir kaydı geri getirebilir miyim?
**C:** Sistem soft-delete kullanır. Silinen kayıtlar veritabanında tutulur. Sistem yöneticisi ile iletişime geçin.

### S: Birden fazla birliğin verilerini görebilir miyim?
**C:** Bu yetkinize bağlıdır. Merkez Yönetici rolü tüm birlikleri görebilir.

### S: Excel'den toplu üye yükleyebilir miyim?
**C:** Evet, Üye listesinde **İçe Aktar** butonunu kullanarak Excel şablonuyla toplu yükleme yapabilirsiniz.

### S: Gecikme zammı otomatik hesaplanıyor mu?
**C:** Evet, son ödeme tarihinden sonraki ödemeler için sistem otomatik gecikme zammı hesaplar.

### S: Oturum ne kadar süre açık kalıyor?
**C:** Güvenlik nedeniyle 24 saat işlem yapılmazsa oturum otomatik kapanır.

---

## 📞 Destek

Teknik destek için:
- **E-posta:** it@tuketbir.gov.tr
- **Telefon:** 0312 XXX XX XX
- **Çalışma Saatleri:** Hafta içi 09:00 - 18:00

---

*Son Güncelleme: Ocak 2026*
*Versiyon: 1.0*
