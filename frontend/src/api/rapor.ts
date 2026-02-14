import api from './axios'
import { ApiResponse, GenelIstatistikDto, BirlikIstatistikDto, AidatRaporDto } from '@/types'

export const raporApi = {
  getGenelIstatistik: async (): Promise<ApiResponse<GenelIstatistikDto>> => {
    const response = await api.get('/raporlar/dashboard')
    return response.data
  },

  getBirlikIstatistikleri: async (): Promise<ApiResponse<BirlikIstatistikDto[]>> => {
    const response = await api.get('/raporlar/birlik-istatistikleri')
    return response.data
  },

  getAidatRaporu: async (
    birlikId?: number,
    yil?: number
  ): Promise<ApiResponse<AidatRaporDto[]>> => {
    const response = await api.get('/raporlar/aidat-raporu', {
      params: { birlikId, yil },
    })
    return response.data
  },

  getTahsilatTrendi: async (
    birlikId?: number,
    aylikMi = true
  ): Promise<ApiResponse<{ donem: string; tutar: number }[]>> => {
    const response = await api.get('/raporlar/tahsilat-trendi', {
      params: { birlikId, aylik: aylikMi },
    })
    return response.data
  },

  getUyeDistribusyonu: async (): Promise<ApiResponse<{
    ilBazinda: { il: string; sayi: number }[]
    durumBazinda: { durum: string; sayi: number }[]
    tipBazinda: { tip: string; sayi: number }[]
  }>> => {
    const response = await api.get('/raporlar/uye-distribusyonu')
    return response.data
  },

  getGelirGiderRaporu: async (
    birlikId?: number,
    baslangicTarihi?: string,
    bitisTarihi?: string
  ): Promise<ApiResponse<{
    toplamGelir: number
    toplamGider: number
    netDurum: number
    aylikTrend: { ay: string; gelir: number; gider: number }[]
  }>> => {
    const response = await api.get('/raporlar/gelir-gider', {
      params: { birlikId, baslangicTarihi, bitisTarihi },
    })
    return response.data
  },

  // PDF/Excel export
  exportDashboardPdf: async (): Promise<Blob> => {
    const response = await api.get('/raporlar/dashboard/pdf', {
      responseType: 'blob',
    })
    return response.data
  },

  exportAidatRaporuExcel: async (birlikId?: number, yil?: number): Promise<Blob> => {
    const response = await api.get('/raporlar/aidat-raporu/excel', {
      params: { birlikId, yil },
      responseType: 'blob',
    })
    return response.data
  },

  exportExcel: async (raporTipi: string, birlikId?: number): Promise<Blob> => {
    const response = await api.get(`/raporlar/${raporTipi}/excel`, {
      params: { birlikId },
      responseType: 'blob',
    })
    return response.data
  },
}
