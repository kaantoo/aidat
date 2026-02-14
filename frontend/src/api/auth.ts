import api from './axios'
import {
  LoginRequest,
  LoginResponse,
  TwoFactorRequest,
  RefreshTokenRequest,
  ApiResponse,
} from '@/types'

export const authApi = {
  login: async (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
    const response = await api.post('/auth/login', data)
    return response.data
  },

  verifyTwoFactor: async (data: TwoFactorRequest): Promise<ApiResponse<LoginResponse>> => {
    const response = await api.post('/auth/verify-2fa', data)
    return response.data
  },

  refreshToken: async (data: RefreshTokenRequest): Promise<ApiResponse<{ accessToken: string }>> => {
    const response = await api.post('/auth/refresh', data)
    return response.data
  },

  logout: async (): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/logout')
    return response.data
  },

  changePassword: async (data: {
    eskiSifre: string
    yeniSifre: string
  }): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/change-password', data)
    return response.data
  },

  setup2FA: async (): Promise<ApiResponse<{ qrCodeUrl: string; secret: string }>> => {
    const response = await api.post('/auth/setup-2fa')
    return response.data
  },

  enable2FA: async (code: string): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/enable-2fa', { code })
    return response.data
  },

  disable2FA: async (code: string): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/disable-2fa', { code })
    return response.data
  },

  forgotPassword: async (email: string): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/forgot-password', null, {
      params: { email }
    })
    return response.data
  },

  resetPassword: async (token: string, newPassword: string): Promise<ApiResponse<void>> => {
    const response = await api.post('/auth/reset-password', {
      token,
      newPassword
    })
    return response.data
  },

  getCurrentUser: async () => {
    const response = await api.get('/auth/me')
    return response.data
  },
}
