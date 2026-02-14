import api from './axios'
import { Birlik, ApiResponse } from '@/types'

export interface BirlikCreateDto {
  birlikKodu: string
  birlikAdi: string
  birlikTipi: string
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
}

export interface BirlikUpdateDto extends Partial<BirlikCreateDto> {
  aktif?: boolean
}

export const birlikApi = {
  getAll: async (page = 0, size = 20): Promise<ApiResponse<Birlik[]>> => {
    const response = await api.get('/birlikler', { params: { page, size } })
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<Birlik>> => {
    const response = await api.get(`/birlikler/${id}`)
    return response.data
  },

  create: async (data: BirlikCreateDto): Promise<ApiResponse<Birlik>> => {
    const response = await api.post('/birlikler', data)
    return response.data
  },

  update: async (id: number, data: BirlikUpdateDto): Promise<ApiResponse<Birlik>> => {
    const response = await api.put(`/birlikler/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/birlikler/${id}`)
    return response.data
  },

  search: async (searchTerm: string): Promise<ApiResponse<Birlik[]>> => {
    const response = await api.get('/birlikler/search', { params: { q: searchTerm } })
    return response.data
  },

  getByIlKodu: async (ilKodu: string): Promise<ApiResponse<Birlik[]>> => {
    const response = await api.get(`/birlikler/il/${ilKodu}`)
    return response.data
  },

  getStatistics: async (id: number): Promise<ApiResponse<{
    uyeSayisi: number
    aktifUyeSayisi: number
    bekleyenAidatTutari: number
    tahsilOrani: number
  }>> => {
    const response = await api.get(`/birlikler/${id}/statistics`)
    return response.data
  },
}
