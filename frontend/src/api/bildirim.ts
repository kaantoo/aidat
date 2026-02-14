import api from './axios'

export interface BildirimDTO {
  id: number
  baslik: string
  mesaj: string
  tip: string
  oncelik: string
  okundu: boolean
  okunmaTarihi?: string
  link?: string
  entityTipi?: string
  entityId?: number
  createdAt: string
  zamanOnce: string
}

export interface BildirimOzetDTO {
  okunmamisSayisi: number
  sonBildirimler: BildirimDTO[]
}

export interface PagedBildirimResponse {
  content: BildirimDTO[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// Bildirim API
export const bildirimApi = {
  // Bildirim listesi
  getBildirimler: (page = 0, size = 20) =>
    api.get<PagedBildirimResponse>('/bildirimler', { params: { page, size } }),

  // Birlik bildirimleri
  getBirlikBildirimleri: (birlikId: number, page = 0, size = 20) =>
    api.get<PagedBildirimResponse>(`/bildirimler/birlik/${birlikId}`, { params: { page, size } }),

  // Bildirim özeti (header için)
  getOzet: () =>
    api.get<BildirimOzetDTO>('/bildirimler/ozet'),

  // Okunmamış sayısı
  getOkunmamisSayisi: () =>
    api.get<{ okunmamisSayisi: number }>('/bildirimler/okunmamis-sayisi'),

  // Okundu işaretle
  markAsRead: (id: number) =>
    api.put<void>(`/bildirimler/${id}/okundu`),

  // Tümünü okundu işaretle
  markAllAsRead: () =>
    api.put<{ guncellenenSayisi: number }>('/bildirimler/tumunu-okundu-isaretle'),

  // Silme
  delete: (id: number) =>
    api.delete<void>(`/bildirimler/${id}`),
}

export default bildirimApi
