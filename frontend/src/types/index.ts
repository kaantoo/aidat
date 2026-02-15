// Enum Types - Backend ile uyumlu
export enum BirlikTipi {
  MERKEZ = 'MERKEZ',
  ALT_BIRLIK = 'ALT_BIRLIK',
}

export enum UyelikTipi {
  GERCEK_KISI = 'GERCEK_KISI',
  TUZEL_KISI = 'TUZEL_KISI',
}

export enum UyeDurum {
  AKTIF = 'AKTIF',
  PASIF = 'PASIF',
  ASKIYA_ALINMIS = 'ASKIYA_ALINMIS',
  IHRAC_EDILMIS = 'IHRAC_EDILMIS',
}

export enum Cinsiyet {
  ERKEK = 'ERKEK',
  KADIN = 'KADIN',
}

export enum DonemTipi {
  AYLIK = 'AYLIK',
  UC_AYLIK = 'UC_AYLIK',
  ALTI_AYLIK = 'ALTI_AYLIK',
  YILLIK = 'YILLIK',
}

export enum AidatDurum {
  BEKLIYOR = 'BEKLIYOR',
  KISMI_ODENDI = 'KISMI_ODENDI',
  ODENDI = 'ODENDI',
  GECIKTI = 'GECIKTI',
  IPTAL = 'IPTAL',
}

export enum OdemeTipi {
  NAKIT = 'NAKIT',
  HAVALE = 'HAVALE',
  EFT = 'EFT',
  KREDI_KARTI = 'KREDI_KARTI',
  CEK = 'CEK',
  SENET = 'SENET',
}

export enum GelirGiderTipi {
  GELIR = 'GELIR',
  GIDER = 'GIDER',
}

export enum GelirKategorisi {
  AIDAT_GELIRI = 'AIDAT_GELIRI',
  BAGIS = 'BAGIS',
  FAIZ_GELIRI = 'FAIZ_GELIRI',
  KIRA_GELIRI = 'KIRA_GELIRI',
  DIGER_GELIR = 'DIGER_GELIR',
}

export enum GiderKategorisi {
  PERSONEL = 'PERSONEL',
  KIRA = 'KIRA',
  FATURA = 'FATURA',
  MALZEME = 'MALZEME',
  ULASIM = 'ULASIM',
  TEMSIL_AGIRLAMAM = 'TEMSIL_AGIRLAMAM',
  BAKIM_ONARIM = 'BAKIM_ONARIM',
  DIGER_GIDER = 'DIGER_GIDER',
}

export enum BelgeTipi {
  KIMLIK_FOTOKOPISI = 'KIMLIK_FOTOKOPISI',
  IKAMETGAH = 'IKAMETGAH',
  VESIKALIK_FOTO = 'VESIKALIK_FOTO',
  UYELIK_FORMU = 'UYELIK_FORMU',
  MAKBUZ = 'MAKBUZ',
  FATURA = 'FATURA',
  DIGER = 'DIGER',
}

export enum KullaniciRol {
  SISTEM_ADMIN = 'SISTEM_ADMIN',
  MERKEZ_YONETICI = 'MERKEZ_YONETICI',
  BIRLIK_YONETICI = 'BIRLIK_YONETICI',
  BIRLIK_PERSONEL = 'BIRLIK_PERSONEL',
  MUHASEBE_SORUMLU = 'MUHASEBE_SORUMLU',
  GOZLEMCI = 'GOZLEMCI',
}

export enum KullaniciDurum {
  AKTIF = 'AKTIF',
  PASIF = 'PASIF',
  KILITLI = 'KILITLI',
}

// Domain Types
export interface Birlik {
  id: number
  birlikKodu: string
  birlikAdi: string
  birlikTipi: BirlikTipi
  ilKodu: string
  ilceKodu?: string
  adres?: string
  telefon?: string
  email?: string
  vergiNo?: string
  vergiDairesi?: string
  ibanNo?: string
  yetkiliAdi?: string
  yetkiliTelefon?: string
  aktif: boolean
  createdAt: string
  updatedAt: string
}

export interface Uye {
  id: number
  uyeNo: string
  birlik?: Birlik
  birlikId?: number
  birlikAdi?: string
  birlikKodu?: string
  uyelikTipi: UyelikTipi
  tcKimlikNo?: string
  vergiNo?: string
  ad: string
  soyad: string
  babaAdi?: string
  anaAdi?: string
  cinsiyet?: Cinsiyet
  dogumTarihi?: string
  dogumYeri?: string
  cepTelefon?: string
  sabitTelefon?: string
  email?: string
  ilKodu?: string
  ilAdi?: string
  ilceKodu?: string
  ilceAdi?: string
  mahalleKoy?: string
  adres?: string
  postaKodu?: string
  firmaAdi?: string
  vergiDairesi?: string
  ticaretSicilNo?: string
  isletmeAdi?: string
  isletmeSicilNo?: string
  hayvanSayisi?: number
  uretimKapasitesi?: number
  katilimTarihi?: string
  ayrilikTarihi?: string
  ayrilikNedeni?: string
  uyeDurum: UyeDurum
  aciklama?: string
  createdAt: string
  updatedAt: string
}

export interface AidatDonemi {
  id: number
  birlikId?: number
  birlikAdi?: string
  donemKodu: string
  donemAdi: string
  donemTipi: DonemTipi
  yil: number
  ay?: number
  baslangicTarihi: string
  bitisTarihi: string
  sonOdemeTarihi: string
  tutar?: number // Alt birlik için aidat tutarı
  merkezPayOrani?: number // Merkez birlik için pay oranı (%)
  asgariUcretTutari?: number
  gecikmeFaiziOrani?: number
  asgariUcretAciklama?: string
  aciklama?: string
  aktif: boolean
  createdAt: string
  updatedAt: string
}

export interface Aidat {
  id: number
  uyeId: number
  uyeNo: string
  uyeAdSoyad?: string
  aidatDonemiId: number
  donemAdi: string
  yil: number
  birlikId?: number
  birlikAdi?: string
  tahakkukTutari: number
  odenenTutar: number
  kalanTutar: number
  gecikmeTutari?: number
  toplamBorc: number
  durumu: AidatDurum
  sonOdemeTarihi: string
  vadeTarihi?: string
  gecikmeGunSayisi?: number
  aktif?: boolean
  createdAt: string
  updatedAt: string
  // Nested objeler (bazı endpoint'ler için)
  uye?: Uye
  donem?: AidatDonemi
}

export interface Tahsilat {
  id: number
  aidatId: number
  uyeId: number
  uyeNo: string
  uyeAdSoyad?: string
  donemAdi?: string
  odemeTipi: OdemeTipi
  tutar: number
  odemeTarihi: string
  makbuzNo: string
  dekontNo?: string
  bankaDekontuNo?: string
  aciklama?: string
  islemYapanKullaniciId?: number
  islemYapanKullaniciAdi?: string
  aktif?: boolean
  createdAt: string
}

export interface GelirGider {
  id: number
  birlikId?: number
  birlikAdi?: string
  tip: GelirGiderTipi
  gelirKategorisi?: GelirKategorisi
  giderKategorisi?: GiderKategorisi
  tutar: number
  islemTarihi: string
  aciklama: string
  belgeNo?: string
  karsiTaraf?: string
  createdAt: string
  updatedAt: string
}

export interface Belge {
  id: number
  belgeNo: string
  birlikId?: number
  birlikAdi?: string
  uye?: Uye
  aidat?: Aidat
  belgeTipi: BelgeTipi
  dosyaAdi: string
  dosyaYolu: string
  dosyaBoyutu: number
  mimeType: string
  aciklama?: string
  belgeTarihi: string
  yukleyenKullanici: string
  createdAt: string
  updatedAt: string
}

export interface Kullanici {
  id: number
  kullaniciAdi: string
  email: string
  ad: string
  soyad: string
  telefon?: string
  rol: KullaniciRol
  birlikId?: number
  birlikAdi?: string
  durum: KullaniciDurum
  sonGirisTarihi?: string
  twoFactorEnabled: boolean
  createdAt: string
  updatedAt: string
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean
  data: T
  message?: string
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// Auth Types
export interface LoginRequest {
  kullaniciAdi: string
  sifre: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  kullaniciAdi: string
  tamAd: string
  rol: string
  birlikId?: number
  birlikAdi?: string
  passwordExpired?: boolean
  requires2FA?: boolean
  tempToken?: string
  message?: string
}

export interface KullaniciDto {
  id: number
  kullaniciAdi: string
  email: string
  ad: string
  soyad: string
  adSoyad: string
  rol: KullaniciRol
  birlikId?: number
  birlikAdi?: string
  yetkiler: string[]
}

export interface TwoFactorRequest {
  kullaniciAdi: string
  code: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

// Rapor Types
export interface GenelIstatistikDto {
  toplamBirlikSayisi: number
  toplamUyeSayisi: number
  aktifUyeSayisi?: number
  pasifUyeSayisi?: number
  yeniUyeSayisi?: number
  
  // Aidat İstatistikleri
  toplamAidatSayisi?: number
  odenmisAidatSayisi?: number
  bekleyenAidatSayisi?: number
  gecikmisnAidatSayisi?: number
  toplamTahakkuk?: number
  toplamTahsilat?: number
  toplamBakiye?: number
  tahsilatOrani?: number
  
  // Gelir/Gider İstatistikleri
  aylikGelir?: number
  aylikGider?: number
  aylikNet?: number
  yillikGelir?: number
  yillikGider?: number
  yillikNet?: number
  
  // Eski alanlar - geriye uyumluluk
  bekleyenAidatTutari?: number
  tahsilEdilmisAidatTutari?: number
  aylikTahsilatTrendi?: TrendVeri[]
}

export interface TrendVeri {
  donem: string
  tutar: number
}

export interface BirlikIstatistikDto {
  birlikId: number
  birlikAdi: string
  uyeSayisi: number
  aktifUyeSayisi: number
  bekleyenAidatTutari: number
  tahsilOrani: number
  sonTahsilatTarihi?: string
}

export interface AidatRaporDto {
  donemAdi: string
  toplamTahakkuk: number
  toplamTahsilat: number
  kalanBorc: number
  tahsilOrani: number
  gecikmisBorcSayisi: number
}

// ======================= Toplantı / Karar Types =======================

export enum ToplantiTuru {
  GENEL_KURUL = 'GENEL_KURUL',
  YONETIM_KURULU = 'YONETIM_KURULU',
  DENETIM_KURULU = 'DENETIM_KURULU',
  OLAGAN_TOPLANTI = 'OLAGAN_TOPLANTI',
  OLAGANUSTU_TOPLANTI = 'OLAGANUSTU_TOPLANTI',
  DIGER = 'DIGER',
}

export enum ToplantiDurumu {
  PLANLANMIS = 'PLANLANMIS',
  DEVAM_EDIYOR = 'DEVAM_EDIYOR',
  TAMAMLANDI = 'TAMAMLANDI',
  IPTAL = 'IPTAL',
  ERTELENDI = 'ERTELENDI',
}

export enum KararDurumu {
  KABUL_EDILDI = 'KABUL_EDILDI',
  REDDEDILDI = 'REDDEDILDI',
  ERTELENDI = 'ERTELENDI',
  UYGULAMADA = 'UYGULAMADA',
  TAMAMLANDI = 'TAMAMLANDI',
}

export interface Toplanti {
  id: number
  toplantiNo: string
  birlikId?: number
  birlikAdi?: string
  baslik: string
  toplantiTuru: ToplantiTuru
  durum: ToplantiDurumu
  toplantiTarihi: string
  baslangicSaati?: string
  bitisSaati?: string
  yer?: string
  gundem?: string
  aciklama?: string
  kararSayisi: number
  katilimciSayisi: number
  kararlar?: Karar[]
  katilimcilar?: ToplantiKatilimci[]
  createdAt: string
  updatedAt: string
}

export interface Karar {
  id: number
  kararNo: string
  toplantiId: number
  toplantiNo?: string
  kararSirasi: number
  baslik: string
  kararMetni: string
  durum: KararDurumu
  oyBirligi: boolean
  kabulOyu?: number
  redOyu?: number
  cekimserOyu?: number
  sorumlu?: string
  notlar?: string
  createdAt: string
}

export interface ToplantiKatilimci {
  id: number
  toplantiId: number
  uyeId?: number
  uyeNo?: string
  adSoyad: string
  gorev?: string
  katildi: boolean
  mazeret?: string
  imzaladi: boolean
}

// ======================= Sistem / Audit Types =======================

export interface AuditLog {
  id: number
  kullaniciAdi?: string
  kullaniciId?: number
  islemTipi: string
  entityTipi?: string
  entityId?: number
  birlikId?: number
  aciklama: string
  ipAdresi?: string
  requestUrl?: string
  httpMetod?: string
  basarili: boolean
  hataMesaji?: string
  islemZamani: string
}

export interface YedekBilgi {
  dosyaAdi: string
  dosyaBoyutu: number
  dosyaBoyutuFormatli: string
  olusturmaZamani: string
  yedekTipi: string
  basarili: boolean
  aciklama?: string
}

export interface YedekDurum {
  otomatikYedekAktif: boolean
  yedekDizini: string
  sonYedekTarihi?: string
  sonYedekDosya?: string
  toplamYedekSayisi: number
  toplamBoyut: string
  sonYedekler: YedekBilgi[]
}

// Form/Filter Types
export interface UyeFilter {
  birlikId?: number
  durum?: UyeDurum
  uyelikTipi?: UyelikTipi
  ilKodu?: string
  searchTerm?: string
}

export interface AidatFilter {
  birlikId?: number
  donemId?: number
  durum?: AidatDurum
  baslangicTarihi?: string
  bitisTarihi?: string
}

export interface GelirGiderFilter {
  birlikId?: number
  tip?: GelirGiderTipi
  baslangicTarihi?: string
  bitisTarihi?: string
}

export interface ToplantiFilter {
  birlikId?: number
  toplantiTuru?: ToplantiTuru
  durum?: ToplantiDurumu
  baslangicTarihi?: string
  bitisTarihi?: string
}
