import { create } from 'zustand'
import { KullaniciDto, KullaniciRol } from '@/types'
import { authApi } from '@/api/auth'

// localStorage'dan user'ı oku
const getStoredUser = (): KullaniciDto | null => {
  try {
    const stored = localStorage.getItem('auth-user')
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

// localStorage'a user'ı kaydet
const setStoredUser = (user: KullaniciDto | null) => {
  if (user) {
    localStorage.setItem('auth-user', JSON.stringify(user))
  } else {
    localStorage.removeItem('auth-user')
  }
}

// Başlangıç state'ini hesapla - sayfa yüklenirken senkron çalışır
const getInitialState = () => {
  const token = localStorage.getItem('accessToken')
  const user = getStoredUser()
  
  return {
    user,
    isAuthenticated: !!(token && user),
  }
}

interface AuthState {
  user: KullaniciDto | null
  isAuthenticated: boolean
  isLoading: boolean
  twoFactorPending: boolean
  pendingUsername: string | null

  // Actions
  login: (kullaniciAdi: string, sifre: string) => Promise<{ twoFactorRequired: boolean }>
  verifyTwoFactor: (code: string) => Promise<void>
  logout: () => Promise<void>
  setUser: (user: KullaniciDto) => void
  hasRole: (roles: KullaniciRol[]) => boolean
  hasPermission: (permission: string) => boolean
}

const initialState = getInitialState()

export const useAuthStore = create<AuthState>()((set, get) => ({
  user: initialState.user,
  isAuthenticated: initialState.isAuthenticated,
  isLoading: false,
  twoFactorPending: false,
  pendingUsername: null,

  login: async (kullaniciAdi: string, sifre: string) => {
    try {
      const response = await authApi.login({ kullaniciAdi, sifre })
      const data = response.data

      if (data.requires2FA) {
        set({ twoFactorPending: true, pendingUsername: kullaniciAdi })
        return { twoFactorRequired: true }
      }

      // Backend'den gelen response'u KullaniciDto formatına dönüştür
      const kullanici: KullaniciDto = {
        id: 0, // Backend'den gelmiyor, token'dan alınabilir
        kullaniciAdi: data.kullaniciAdi,
        email: '',
        ad: data.tamAd?.split(' ')[0] || '',
        soyad: data.tamAd?.split(' ').slice(1).join(' ') || '',
        adSoyad: data.tamAd || '',
        rol: data.rol as KullaniciRol,
        birlikId: data.birlikId,
        birlikAdi: data.birlikAdi,
        yetkiler: [], // Token'dan parse edilebilir
      }

      localStorage.setItem('accessToken', data.accessToken)
      localStorage.setItem('refreshToken', data.refreshToken)
      setStoredUser(kullanici)

      set({
        user: kullanici,
        isAuthenticated: true,
        isLoading: false,
        twoFactorPending: false,
        pendingUsername: null,
      })

      return { twoFactorRequired: false }
    } catch (error) {
      set({ isLoading: false })
      throw error
    }
  },

  verifyTwoFactor: async (code: string) => {
    const { pendingUsername } = get()
    if (!pendingUsername) throw new Error('No pending login')

    const response = await authApi.verifyTwoFactor({ kullaniciAdi: pendingUsername, code })
    const data = response.data

    // Backend'den gelen response'u KullaniciDto formatına dönüştür
    const kullanici: KullaniciDto = {
      id: 0,
      kullaniciAdi: data.kullaniciAdi,
      email: '',
      ad: data.tamAd?.split(' ')[0] || '',
      soyad: data.tamAd?.split(' ').slice(1).join(' ') || '',
      adSoyad: data.tamAd || '',
      rol: data.rol as KullaniciRol,
      birlikId: data.birlikId,
      birlikAdi: data.birlikAdi,
      yetkiler: [],
    }

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    setStoredUser(kullanici)

    set({
      user: kullanici,
      isAuthenticated: true,
      isLoading: false,
      twoFactorPending: false,
      pendingUsername: null,
    })
  },

  logout: async () => {
    try {
      await authApi.logout()
    } catch {
      // Ignore error
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      setStoredUser(null)
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        twoFactorPending: false,
        pendingUsername: null,
      })
    }
  },

  setUser: (user: KullaniciDto) => {
    setStoredUser(user)
    set({ user })
  },

  hasRole: (roles: KullaniciRol[]) => {
    const { user } = get()
    if (!user) return false
    return roles.includes(user.rol)
  },

  hasPermission: (permission: string) => {
    const { user } = get()
    if (!user) return false
    return user.yetkiler.includes(permission)
  },
}))
