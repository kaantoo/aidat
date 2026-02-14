import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { message } from 'antd'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // Handle 401 - Token expired
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = localStorage.getItem('refreshToken')
      if (refreshToken) {
        try {
          const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          })

          const { accessToken } = response.data.data
          localStorage.setItem('accessToken', accessToken)

          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
        } catch (refreshError) {
          // Refresh token da geçersiz, logout yap
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          window.location.href = '/login'
          return Promise.reject(refreshError)
        }
      } else {
        window.location.href = '/login'
      }
    }

    // Handle other errors
    if (error.response?.status === 403) {
      message.error('Bu işlem için yetkiniz bulunmamaktadır.')
    } else if (error.response?.status === 404) {
      message.error('İstenen kaynak bulunamadı.')
    } else if (error.response?.status === 500) {
      message.error('Sunucu hatası oluştu. Lütfen daha sonra tekrar deneyin.')
    } else if (error.response?.data) {
      const errorData = error.response.data as { message?: string }
      message.error(errorData.message || 'Bir hata oluştu.')
    }

    return Promise.reject(error)
  }
)

export default api
