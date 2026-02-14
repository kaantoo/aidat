import api from './axios'
import { Uye, UyeFilter, ApiResponse, PageResponse, UyelikTipi, UyeDurum, Cinsiyet } from '@/types'

export interface UyeCreateDto {
  // Kimlik Bilgileri
  tcKimlikNo?: string
  ad: string
  soyad: string
  babaAdi?: string
  anaAdi?: string
  cinsiyet?: Cinsiyet
  dogumTarihi?: string
  dogumYeri?: string
  
  // Üyelik Bilgileri
  uyelikTipi: UyelikTipi
  katilimTarihi?: string
  birlikId: number
  
  // İletişim Bilgileri
  cepTelefon?: string
  sabitTelefon?: string
  email?: string
  
  // Adres Bilgileri
  ilKodu?: string
  ilAdi?: string
  ilceKodu?: string
  ilceAdi?: string
  mahalleKoy?: string
  adres?: string
  postaKodu?: string
  
  // Kurumsal Üye Bilgileri
  firmaAdi?: string
  vergiDairesi?: string
  vergiNo?: string
  ticaretSicilNo?: string
  
  // Üretici Bilgileri
  isletmeAdi?: string
  isletmeSicilNo?: string
  hayvanSayisi?: number
  uretimKapasitesi?: number
  
  // Diğer
  aciklama?: string
}

export interface UyeUpdateDto extends Partial<UyeCreateDto> {
  durum?: UyeDurum
}

export const uyeApi = {
  getAll: async (
    page = 0,
    size = 20,
    filter?: UyeFilter
  ): Promise<ApiResponse<PageResponse<Uye>>> => {
    const response = await api.get('/uyeler', {
      params: { page, size, ...filter },
    })
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<Uye>> => {
    const response = await api.get(`/uyeler/${id}`)
    return response.data
  },

  getByUyeNo: async (uyeNo: string): Promise<ApiResponse<Uye>> => {
    const response = await api.get(`/uyeler/uye-no/${uyeNo}`)
    return response.data
  },

  create: async (data: UyeCreateDto): Promise<ApiResponse<Uye>> => {
    const response = await api.post('/uyeler', data)
    return response.data
  },

  update: async (id: number, data: UyeUpdateDto): Promise<ApiResponse<Uye>> => {
    const response = await api.put(`/uyeler/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/uyeler/${id}`)
    return response.data
  },

  search: async (
    searchTerm: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Uye>>> => {
    const response = await api.get('/uyeler/search', {
      params: { q: searchTerm, page, size },
    })
    return response.data
  },

  getByBirlik: async (
    birlikId: number,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Uye>>> => {
    const response = await api.get(`/uyeler/birlik/${birlikId}`, {
      params: { page, size },
    })
    return response.data
  },

  validateTcKimlik: async (tcKimlikNo: string): Promise<ApiResponse<boolean>> => {
    const response = await api.post('/uyeler/validate-tc', { tcKimlikNo })
    return response.data
  },

  updateDurum: async (id: number, durum: UyeDurum): Promise<ApiResponse<Uye>> => {
    const response = await api.patch(`/uyeler/${id}/durum`, { durum })
    return response.data
  },

  exportExcel: async (filter?: UyeFilter): Promise<Blob> => {
    const response = await api.get('/uyeler/export', {
      params: filter,
      responseType: 'blob',
    })
    return response.data
  },
}
