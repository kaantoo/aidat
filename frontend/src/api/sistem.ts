import api from './axios';
import type { AuditLog, YedekDurum, YedekBilgi } from '@/types';

// Backend ApiResponse<T> wrapper
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  errors?: string[];
  timestamp?: string;
}

export interface SistemDurumu {
  readOnlyMode: boolean;
  readOnlyMesaj: string;
  bakimModu: boolean;
}

export interface ReadOnlyModeResponse {
  success?: boolean;
  aktif: boolean;
  mesaj: string;
}

export interface ReadOnlyModeRequest {
  aktif: boolean;
  mesaj?: string;
}

export interface AuditLogParams {
  page?: number;
  size?: number;
  kullaniciId?: number;
  islemTipi?: string;
  entityTipi?: string;
  kullaniciAdi?: string;
  basarili?: boolean;
  baslangicTarihi?: string;
  bitisTarihi?: string;
}

export interface PagedAuditLogResponse {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const sistemApi = {
  // ========== Sistem Durumu ==========
  // SistemController doğrudan Map<String, Object> dönüyor (ApiResponse wrapper'sız)

  getDurum: async (): Promise<SistemDurumu> => {
    const response = await api.get('/sistem/durum');
    return response.data;
  },

  getReadOnlyStatus: async (): Promise<ReadOnlyModeResponse> => {
    const response = await api.get('/sistem/read-only');
    return response.data;
  },

  setReadOnlyMode: async (request: ReadOnlyModeRequest): Promise<ReadOnlyModeResponse> => {
    const response = await api.post('/sistem/read-only', request);
    return response.data;
  },

  getBakimModuStatus: async (): Promise<{ aktif: boolean }> => {
    const response = await api.get('/sistem/bakim-modu');
    return response.data;
  },

  setBakimModu: async (aktif: boolean): Promise<{ success: boolean; aktif: boolean }> => {
    const response = await api.post('/sistem/bakim-modu', { aktif });
    return response.data;
  },

  // ========== Yedekleme ==========
  // YedeklemeController ApiResponse<T> wrapper kullanıyor

  getYedekDurumu: async (): Promise<YedekDurum> => {
    const response = await api.get<ApiResponse<YedekDurum>>('/sistem/yedekleme/durum');
    return response.data.data;
  },

  manuelYedekle: async (): Promise<YedekBilgi> => {
    const response = await api.post<ApiResponse<YedekBilgi>>('/sistem/yedekleme/yedekle');
    return response.data.data;
  },

  deleteYedek: async (dosyaAdi: string): Promise<void> => {
    await api.delete(`/sistem/yedekleme/${encodeURIComponent(dosyaAdi)}`);
  },

  // ========== Audit Log ==========
  // YedeklemeController ApiResponse<List<AuditLogDTO>> dönüyor

  getAuditLogs: async (params?: AuditLogParams): Promise<PagedAuditLogResponse> => {
    const response = await api.get<ApiResponse<AuditLog[]>>('/sistem/audit-log', { params });
    const logs = response.data.data || [];
    // Backend şu an sayfalama bilgisi dönmüyor, client-side simüle ediyoruz
    const page = params?.page || 0;
    const size = params?.size || 50;
    return {
      content: logs,
      totalElements: logs.length < size ? page * size + logs.length : (page + 1) * size + 1,
      totalPages: logs.length < size ? page + 1 : page + 2,
      size: size,
      number: page,
    };
  },
};

export default sistemApi;
