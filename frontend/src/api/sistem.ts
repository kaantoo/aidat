import api from './axios';

export interface SistemDurumu {
  readOnlyMode: boolean;
  readOnlyMesaj: string;
  bakimModu: boolean;
}

export interface ReadOnlyModeResponse {
  aktif: boolean;
  mesaj: string;
}

export interface ReadOnlyModeRequest {
  aktif: boolean;
  mesaj?: string;
}

export const sistemApi = {
  /**
   * Sistem durumunu getir
   */
  getDurum: async (): Promise<SistemDurumu> => {
    const response = await api.get('/sistem/durum');
    return response.data;
  },

  /**
   * Read-only mod durumunu getir
   */
  getReadOnlyStatus: async (): Promise<ReadOnlyModeResponse> => {
    const response = await api.get('/sistem/read-only');
    return response.data;
  },

  /**
   * Read-only modu aç/kapat
   */
  setReadOnlyMode: async (request: ReadOnlyModeRequest): Promise<ReadOnlyModeResponse> => {
    const response = await api.post('/sistem/read-only', request);
    return response.data;
  },

  /**
   * Bakım modu durumunu getir
   */
  getBakimModuStatus: async (): Promise<{ aktif: boolean }> => {
    const response = await api.get('/sistem/bakim-modu');
    return response.data;
  },

  /**
   * Bakım modunu aç/kapat
   */
  setBakimModu: async (aktif: boolean): Promise<{ success: boolean; aktif: boolean }> => {
    const response = await api.post('/sistem/bakim-modu', { aktif });
    return response.data;
  },
};

export default sistemApi;
