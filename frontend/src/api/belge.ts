import api from './axios'
import { Belge, ApiResponse, PageResponse, BelgeTipi } from '@/types'

export interface BelgeUploadDto {
  file: File
  belgeTipi: BelgeTipi
  birlikId?: number
  uyeId?: number
  aidatId?: number
  aciklama?: string
}

export const belgeApi = {
  getAll: async (page = 0, size = 20): Promise<ApiResponse<PageResponse<Belge>>> => {
    const response = await api.get('/belgeler', {
      params: { page, size },
    })
    return response.data
  },

  getById: async (id: number): Promise<ApiResponse<Belge>> => {
    const response = await api.get(`/belgeler/${id}`)
    return response.data
  },

  getByBirlik: async (
    birlikId: number,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Belge>>> => {
    const response = await api.get(`/belgeler/birlik/${birlikId}`, {
      params: { page, size },
    })
    return response.data
  },

  getByUye: async (uyeId: number): Promise<ApiResponse<Belge[]>> => {
    const response = await api.get(`/belgeler/uye/${uyeId}`)
    return response.data
  },

  getByAidat: async (aidatId: number): Promise<ApiResponse<Belge[]>> => {
    const response = await api.get(`/belgeler/aidat/${aidatId}`)
    return response.data
  },

  upload: async (data: FormData): Promise<ApiResponse<Belge>> => {
    const response = await api.post('/belgeler/upload', data, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  download: async (id: number): Promise<Blob> => {
    const response = await api.get(`/belgeler/${id}/download`, {
      responseType: 'blob',
    })
    return response.data
  },

  delete: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/belgeler/${id}`)
    return response.data
  },

  search: async (
    birlikId: number,
    searchTerm: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Belge>>> => {
    const response = await api.get('/belgeler/search', {
      params: { birlikId, q: searchTerm, page, size },
    })
    return response.data
  },
}
