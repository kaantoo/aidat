import api from './axios'
import type {
  Toplanti,
  Karar,
  ToplantiKatilimci,
  ToplantiTuru,
  ToplantiDurumu,
  KararDurumu,
} from '@/types'

export interface ToplantiListParams {
  page?: number
  size?: number
  birlikId?: number
  toplantiTuru?: ToplantiTuru
  durum?: ToplantiDurumu
  baslangicTarihi?: string
  bitisTarihi?: string
}

export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface ToplantiCreateRequest {
  birlikId: number
  baslik: string
  toplantiTuru: ToplantiTuru
  toplantiTarihi: string
  baslangicSaati?: string
  bitisSaati?: string
  yer?: string
  gundem?: string
  aciklama?: string
}

export interface ToplantiUpdateRequest {
  baslik?: string
  toplantiTuru?: ToplantiTuru
  durum?: ToplantiDurumu
  toplantiTarihi?: string
  baslangicSaati?: string
  bitisSaati?: string
  yer?: string
  gundem?: string
  aciklama?: string
}

export interface KararCreateRequest {
  baslik: string
  kararMetni: string
  durum?: KararDurumu
  oyBirligi?: boolean
  kabulOyu?: number
  redOyu?: number
  cekimserOyu?: number
  sorumlu?: string
  notlar?: string
}

export interface KatilimciCreateRequest {
  uyeId?: number
  adSoyad: string
  gorev?: string
  katildi?: boolean
  mazeret?: string
  imzaladi?: boolean
}

export const toplantiApi = {
  // ========== Toplantı CRUD ==========

  getAll: (params?: ToplantiListParams) =>
    api.get<PagedResponse<Toplanti>>('/toplantilar', { params }),

  getById: (id: number) =>
    api.get<Toplanti>(`/toplantilar/${id}`),

  create: (data: ToplantiCreateRequest) =>
    api.post<Toplanti>('/toplantilar', data),

  update: (id: number, data: ToplantiUpdateRequest) =>
    api.put<Toplanti>(`/toplantilar/${id}`, data),

  delete: (id: number) =>
    api.delete<void>(`/toplantilar/${id}`),

  // ========== Karar İşlemleri ==========

  getKararlar: (toplantiId: number) =>
    api.get<Karar[]>(`/toplantilar/${toplantiId}/kararlar`),

  addKarar: (toplantiId: number, data: KararCreateRequest) =>
    api.post<Karar>(`/toplantilar/${toplantiId}/kararlar`, data),

  updateKarar: (toplantiId: number, kararId: number, data: Partial<KararCreateRequest>) =>
    api.put<Karar>(`/toplantilar/${toplantiId}/kararlar/${kararId}`, data),

  deleteKarar: (toplantiId: number, kararId: number) =>
    api.delete<void>(`/toplantilar/${toplantiId}/kararlar/${kararId}`),

  // ========== Katılımcı İşlemleri ==========

  getKatilimcilar: (toplantiId: number) =>
    api.get<ToplantiKatilimci[]>(`/toplantilar/${toplantiId}/katilimcilar`),

  addKatilimci: (toplantiId: number, data: KatilimciCreateRequest) =>
    api.post<ToplantiKatilimci>(`/toplantilar/${toplantiId}/katilimcilar`, data),

  removeKatilimci: (toplantiId: number, katilimciId: number) =>
    api.delete<void>(`/toplantilar/${toplantiId}/katilimcilar/${katilimciId}`),
}

export default toplantiApi
