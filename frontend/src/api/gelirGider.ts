import api from './axios'
import {
  GelirGider,
  GelirGiderFilter,
  ApiResponse,
  PageResponse,
  GelirGiderTipi,
  GelirKategorisi,
  GiderKategorisi,
} from '@/types'

export interface GelirGiderCreateDto {
  birlikId: number
  tip: GelirGiderTipi
  gelirKategorisi?: GelirKategorisi
  giderKategorisi?: GiderKategorisi
  tutar: number
  islemTarihi: string
  aciklama: string
  belgeNo?: string
  karsiTaraf?: string
}

export const gelirGiderApi = {
  getAll: async (
    page = 0,
    size = 20,
    filter?: GelirGiderFilter
  ): Promise<ApiResponse<PageResponse<GelirGider>>> => {
    const response = await api.get('/gelir-gider', {
      params: { page, size, ...filter },
    })
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<GelirGider>> => {
    const response = await api.get(`/gelir-gider/${id}`)
    return response.data
  },

  create: async (data: GelirGiderCreateDto): Promise<ApiResponse<GelirGider>> => {
    const response = await api.post('/gelir-gider', data)
    return response.data
  },

  update: async (id: number, data: Partial<GelirGiderCreateDto>): Promise<ApiResponse<GelirGider>> => {
    const response = await api.put(`/gelir-gider/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/gelir-gider/${id}`)
    return response.data
  },

  getByBirlik: async (
    birlikId: number,
    page = 0,
    size = 20,
    filter?: Omit<GelirGiderFilter, 'birlikId'>
  ): Promise<ApiResponse<PageResponse<GelirGider>>> => {
    const response = await api.get(`/gelir-gider/birlik/${birlikId}`, {
      params: { page, size, ...filter },
    })
    return response.data
  },

  getOzet: async (birlikId?: number, _baslangicTarihi?: string, _bitisTarihi?: string): Promise<ApiResponse<{
    toplamGelir: number
    toplamGider: number
    netDurum: number
    gelirKategorileri: { kategori: string; tutar: number }[]
    giderKategorileri: { kategori: string; tutar: number }[]
  }>> => {
    // Backend'de /stats endpoint'ini kullan
    const now = new Date()
    const yil = now.getFullYear()
    const ay = now.getMonth() + 1
    const response = await api.get('/gelir-gider/stats', {
      params: { birlikId, yil, ay },
    })
    return response.data
  },

  exportExcel: async (filter?: GelirGiderFilter): Promise<Blob> => {
    const response = await api.get('/gelir-gider/export', {
      params: filter,
      responseType: 'blob',
    })
    return response.data
  },
}
