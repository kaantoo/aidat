import { Outlet } from 'react-router-dom'

const AuthLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-red-800 to-red-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">
            Kırmızı Et Üreticileri Merkez Birliği
          </h1>
          <p className="text-red-200">Aidat ve Yönetim Sistemi</p>
        </div>
        <Outlet />
      </div>
    </div>
  )
}

export default AuthLayout
