import { Routes, Route, Navigate } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuthStore } from '@/store/authStore'
import MainLayout from '@/layouts/MainLayout'
import AuthLayout from '@/layouts/AuthLayout'

// Auth Pages
import LoginPage from '@/pages/auth/LoginPage'
import TwoFactorPage from '@/pages/auth/TwoFactorPage'
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage'

// Dashboard
import DashboardPage from '@/pages/dashboard/DashboardPage'

// Birlik
import BirlikListPage from '@/pages/birlik/BirlikListPage'
import BirlikDetailPage from '@/pages/birlik/BirlikDetailPage'
import BirlikFormPage from '@/pages/birlik/BirlikFormPage'

// Üye
import UyeListPage from '@/pages/uye/UyeListPage'
import UyeDetailPage from '@/pages/uye/UyeDetailPage'
import UyeFormPage from '@/pages/uye/UyeFormPage'

// Aidat
import AidatListPage from '@/pages/aidat/AidatListPage'
import AidatDonemPage from '@/pages/aidat/AidatDonemPage'
import TahsilatPage from '@/pages/aidat/TahsilatPage'

// Gelir Gider
import GelirGiderListPage from '@/pages/gelirGider/GelirGiderListPage'
import BeklenenGelirPage from '@/pages/gelirGider/BeklenenGelirPage'

// Belge
import BelgeListPage from '@/pages/belge/BelgeListPage'

// Kullanıcı
import KullaniciListPage from '@/pages/kullanici/KullaniciListPage'
import KullaniciFormPage from '@/pages/kullanici/KullaniciFormPage'

// Rapor
import RaporlarPage from '@/pages/rapor/RaporlarPage'

// Sistem Yönetimi
import SistemYonetimiPage from '@/pages/sistem/SistemYonetimiPage'

// Toplantı
import ToplantiListPage from '@/pages/toplanti/ToplantiListPage'
import ToplantiDetailPage from '@/pages/toplanti/ToplantiDetailPage'
import ToplantiFormPage from '@/pages/toplanti/ToplantiFormPage'

// Profil
import ProfilPage from '@/pages/profil/ProfilPage'

// Protected Route
interface ProtectedRouteProps {
  children: React.ReactNode
  roles?: string[]
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuthStore()

  // Yükleme durumunda bekle
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Spin size="large" />
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  return <>{children}</>
}

const AppRoutes: React.FC = () => {
  const { isAuthenticated, twoFactorPending, isLoading } = useAuthStore()

  // Yükleme durumunda bekle
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <Routes>
      {/* Auth Routes */}
      <Route element={<AuthLayout />}>
        <Route
          path="/login"
          element={
            isAuthenticated ? (
              <Navigate to="/" replace />
            ) : twoFactorPending ? (
              <Navigate to="/two-factor" replace />
            ) : (
              <LoginPage />
            )
          }
        />
        <Route
          path="/two-factor"
          element={
            isAuthenticated ? (
              <Navigate to="/" replace />
            ) : !twoFactorPending ? (
              <Navigate to="/login" replace />
            ) : (
              <TwoFactorPage />
            )
          }
        />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Route>

      {/* Protected Routes */}
      <Route
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Dashboard */}
        <Route path="/" element={<DashboardPage />} />
        <Route path="/dashboard" element={<Navigate to="/" replace />} />

        {/* Birlik */}
        <Route path="/birlikler" element={<BirlikListPage />} />
        <Route path="/birlikler/yeni" element={<BirlikFormPage />} />
        <Route path="/birlikler/:id" element={<BirlikDetailPage />} />
        <Route path="/birlikler/:id/duzenle" element={<BirlikFormPage />} />

        {/* Üye */}
        <Route path="/uyeler" element={<UyeListPage />} />
        <Route path="/uyeler/yeni" element={<UyeFormPage />} />
        <Route path="/uyeler/:id" element={<UyeDetailPage />} />
        <Route path="/uyeler/:id/duzenle" element={<UyeFormPage />} />

        {/* Aidat */}
        <Route path="/aidatlar" element={<AidatListPage />} />
        <Route path="/aidatlar/donemler" element={<AidatDonemPage />} />
        <Route path="/aidatlar/tahsilat" element={<TahsilatPage />} />

        {/* Gelir Gider */}
        <Route path="/gelir-gider" element={<GelirGiderListPage />} />
        <Route path="/beklenen-gelir" element={<BeklenenGelirPage />} />

        {/* Belge */}
        <Route path="/belgeler" element={<BelgeListPage />} />

        {/* Kullanıcı */}
        <Route path="/kullanicilar" element={<KullaniciListPage />} />
        <Route path="/kullanicilar/yeni" element={<KullaniciFormPage />} />
        <Route path="/kullanicilar/:id/duzenle" element={<KullaniciFormPage />} />

        {/* Rapor */}
        <Route path="/raporlar" element={<RaporlarPage />} />

        {/* Sistem Yönetimi - Sadece SISTEM_ADMIN */}
        <Route path="/sistem-yonetimi" element={<SistemYonetimiPage />} />

        {/* Toplantı */}
        <Route path="/toplantilar" element={<ToplantiListPage />} />
        <Route path="/toplantilar/yeni" element={<ToplantiFormPage />} />
        <Route path="/toplantilar/:id" element={<ToplantiDetailPage />} />
        <Route path="/toplantilar/:id/duzenle" element={<ToplantiFormPage />} />

        {/* Profil */}
        <Route path="/profil" element={<ProfilPage />} />

        {/* 404 */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes
