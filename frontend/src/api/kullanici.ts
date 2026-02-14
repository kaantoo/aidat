import api from './axios'
import { Kullanici, ApiResponse, PageResponse, KullaniciRol, KullaniciDurum } from '@/types'

export interface KullaniciCreateDto {
  kullaniciAdi: string
  email: string
  sifre: string
  ad: string
  soyad: string
  telefon?: string
  rol: KullaniciRol
  birlikId?: number
}

export interface KullaniciUpdateDto {
  email?: string
  ad?: string
  soyad?: string
  telefon?: string
  rol?: KullaniciRol
  birlikId?: number
  durum?: KullaniciDurum
}

export const kullaniciApi = {
  getAll: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<Kullanici>>> => {
    const response = await api.get('/kullanicilar', {
      params: { page, size },
    })
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<Kullanici>> => {
    const response = await api.get(`/kullanicilar/${id}`)
    return response.data
  },

  create: async (data: KullaniciCreateDto): Promise<ApiResponse<Kullanici>> => {
    const response = await api.post('/kullanicilar', data)
    return response.data
  },

  update: async (id: number, data: KullaniciUpdateDto): Promise<ApiResponse<Kullanici>> => {
    const response = await api.put(`/kullanicilar/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/kullanicilar/${id}`)
    return response.data
  },

  updateDurum: async (id: number, durum: KullaniciDurum): Promise<ApiResponse<Kullanici>> => {
    const response = await api.patch(`/kullanicilar/${id}/durum`, { durum })
    return response.data
  },

  resetPassword: async (id: number, yeniSifre: string): Promise<ApiResponse<void>> => {
    const response = await api.post(`/kullanicilar/${id}/reset-password`, { yeniSifre })
    return response.data
  },

  unlockAccount: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.post(`/kullanicilar/${id}/unlock`)
    return response.data
  },

  getByBirlik: async (
    birlikId: number,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Kullanici>>> => {
    const response = await api.get(`/kullanicilar/birlik/${birlikId}`, {
      params: { page, size },
    })
    return response.data
  },

  search: async (
    searchTerm: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Kullanici>>> => {
    const response = await api.get('/kullanicilar/search', {
      params: { q: searchTerm, page, size },
    })
    return response.data
  },
}
