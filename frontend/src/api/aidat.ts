import api from './axios'
import {
  Aidat,
  AidatDonemi,
  Tahsilat,
  AidatFilter,
  ApiResponse,
  PageResponse,
  AidatDurum,
  OdemeTipi,
  DonemTipi,
} from '@/types'

// AidatDonemi DTOs
export interface AidatDonemCreateDto {
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
  birlikId?: number
}

// Aidat DTOs
export interface AidatCreateDto {
  uyeId: number
  donemId: number
  aidatTutari?: number
  sonOdemeTarihi?: string
  aciklama?: string
}

export interface TopluTahakkukDto {
  donemId: number
  birlikId?: number
}

// Tahsilat DTOs
export interface TahsilatCreateDto {
  aidatId: number
  tutar: number
  odemeTarihi?: string
  odemeTipi: OdemeTipi
  makbuzNo?: string
  dekontNo?: string
  bankaDekontuNo?: string
  aciklama?: string
}

export const aidatApi = {
  // Aidat Dönem işlemleri
  getDonemleri: async (birlikId?: number): Promise<ApiResponse<AidatDonemi[]>> => {
    const response = await api.get('/aidatlar/donemler', {
      params: birlikId ? { birlikId } : undefined,
    })
    return response.data
  },

  getDonemById: async (id: number): Promise<ApiResponse<AidatDonemi>> => {
    const response = await api.get(`/aidatlar/donemler/${id}`)
    return response.data
  },

  createDonem: async (data: AidatDonemCreateDto): Promise<ApiResponse<AidatDonemi>> => {
    const response = await api.post('/aidatlar/donemler', data)
    return response.data
  },

  updateDonem: async (
    id: number,
    data: Partial<AidatDonemCreateDto>
  ): Promise<ApiResponse<AidatDonemi>> => {
    const response = await api.put(`/aidatlar/donemler/${id}`, data)
    return response.data
  },

  deleteDonem: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/aidatlar/donemler/${id}`)
    return response.data
  },

  // Aidat işlemleri
  getAidatlar: async (
    page = 0,
    size = 20,
    filter?: AidatFilter
  ): Promise<ApiResponse<PageResponse<Aidat>>> => {
    const response = await api.get('/aidatlar', {
      params: { page, size, ...filter },
    })
    return response.data
  },

  getAidatById: async (id: number): Promise<ApiResponse<Aidat>> => {
    const response = await api.get(`/aidatlar/${id}`)
    return response.data
  },

  getAidatByUye: async (
    uyeId: number,
    _page = 0,
    _size = 20
  ): Promise<ApiResponse<Aidat[]>> => {
    const response = await api.get(`/aidatlar/uye/${uyeId}`)
    return response.data
  },

  createAidat: async (data: AidatCreateDto): Promise<ApiResponse<Aidat>> => {
    const response = await api.post('/aidatlar', data)
    return response.data
  },

  topluTahakkuk: async (donemId: number): Promise<ApiResponse<number>> => {
    const response = await api.post('/aidatlar/toplu-tahakkuk', { donemId })
    return response.data
  },

  updateAidatDurum: async (id: number, durum: AidatDurum): Promise<ApiResponse<Aidat>> => {
    const response = await api.patch(`/aidatlar/${id}/durum`, { durum })
    return response.data
  },

  hesaplaGecikmeZammi: async (id: number): Promise<ApiResponse<Aidat>> => {
    const response = await api.post(`/aidatlar/${id}/gecikme-zammi`)
    return response.data
  },

  // Tahsilat işlemleri
  getTahsilatlar: async (birlikId?: number): Promise<ApiResponse<Tahsilat[]>> => {
    const response = await api.get('/aidatlar/tahsilatlar', {
      params: { birlikId },
    })
    return response.data
  },

  getTahsilatlarByAidat: async (aidatId: number): Promise<ApiResponse<Tahsilat[]>> => {
    const response = await api.get(`/aidatlar/${aidatId}/tahsilatlar`)
    return response.data
  },

  createTahsilat: async (data: TahsilatCreateDto): Promise<ApiResponse<Tahsilat>> => {
    const response = await api.post('/aidatlar/tahsilat', data)
    return response.data
  },

  deleteTahsilat: async (id: number): Promise<ApiResponse<void>> => {
    const response = await api.delete(`/aidatlar/tahsilat/${id}`)
    return response.data
  },

  getMakbuz: async (tahsilatId: number): Promise<Blob> => {
    const response = await api.get(`/aidatlar/tahsilat/${tahsilatId}/makbuz`, {
      responseType: 'blob',
    })
    return response.data
  },

  // İstatistikler
  getAidatOzet: async (birlikId?: number): Promise<ApiResponse<{
    toplamTahakkuk: number
    toplamTahsilat: number
    kalanBorc: number
    tahsilOrani: number
    gecikmisBorcSayisi: number
  }>> => {
    const response = await api.get('/aidatlar/ozet', {
      params: birlikId ? { birlikId } : undefined,
    })
    return response.data
  },

  // Excel export
  exportExcel: async (filter?: AidatFilter): Promise<Blob> => {
    const response = await api.get('/aidatlar/export', {
      params: filter,
      responseType: 'blob',
    })
    return response.data
  },

  // Dönem Excel Import
  downloadDonemImportTemplate: async (): Promise<Blob> => {
    const response = await api.get('/aidatlar/donemler/import/template', {
      responseType: 'blob',
    })
    return response.data
  },

  importDonemlerFromExcel: async (
    file: File,
    birlikId?: number
  ): Promise<ApiResponse<{
    basarili: number
    atlanan: number
    hatali: number
    toplam: number
    hatalar: string[]
    uyarilar: string[]
  }>> => {
    const formData = new FormData()
    formData.append('file', file)
    if (birlikId) {
      formData.append('birlikId', birlikId.toString())
    }
    const response = await api.post('/aidatlar/donemler/import/excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    return response.data
  },
}
