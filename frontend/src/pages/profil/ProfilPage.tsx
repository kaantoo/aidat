import { Card, Descriptions, Button, Form, Input, App, Switch } from 'antd'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/api/auth'
import { useMutation } from 'react-query'
import { KullaniciRol } from '@/types'

const ProfilPage: React.FC = () => {
  const { user } = useAuthStore()
  const [passwordForm] = Form.useForm()
  const { message } = App.useApp()

  const changePasswordMutation = useMutation(
    (data: { eskiSifre: string; yeniSifre: string }) => authApi.changePassword(data),
    {
      onSuccess: () => {
        message.success('Şifre başarıyla değiştirildi')
        passwordForm.resetFields()
      },
      onError: () => {
        message.error('Şifre değiştirirken hata oluştu')
      },
    }
  )

  const getRolLabel = (rol: KullaniciRol): string => {
    const labels: Record<KullaniciRol, string> = {
      [KullaniciRol.SISTEM_ADMIN]: 'Sistem Admin',
      [KullaniciRol.MERKEZ_YONETICI]: 'Merkez Yönetici',
      [KullaniciRol.BIRLIK_YONETICI]: 'Birlik Yönetici',
      [KullaniciRol.BIRLIK_PERSONEL]: 'Birlik Personel',
      [KullaniciRol.MUHASEBE_SORUMLU]: 'Muhasebe Sorumlu',
      [KullaniciRol.GOZLEMCI]: 'Gözlemci',
    }
    return labels[rol] || rol
  }

  const onChangePassword = (values: {
    eskiSifre: string
    yeniSifre: string
    yeniSifreTekrar: string
  }) => {
    if (values.yeniSifre !== values.yeniSifreTekrar) {
      message.error('Yeni şifreler eşleşmiyor')
      return
    }
    changePasswordMutation.mutate({
      eskiSifre: values.eskiSifre,
      yeniSifre: values.yeniSifre,
    })
  }

  return (
    <div>
      <h1 className="text-xl font-semibold mb-4">Profilim</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card title="Kullanıcı Bilgileri" variant="borderless" className="shadow-sm">
          <Descriptions column={1}>
            <Descriptions.Item label="Kullanıcı Adı">
              {user?.kullaniciAdi}
            </Descriptions.Item>
            <Descriptions.Item label="Ad Soyad">{user?.adSoyad}</Descriptions.Item>
            <Descriptions.Item label="E-posta">{user?.email}</Descriptions.Item>
            <Descriptions.Item label="Rol">{user && getRolLabel(user.rol)}</Descriptions.Item>
            {user?.birlikAdi && (
              <Descriptions.Item label="Birlik">{user.birlikAdi}</Descriptions.Item>
            )}
          </Descriptions>
        </Card>

        <Card title="Şifre Değiştir" variant="borderless" className="shadow-sm">
          <Form form={passwordForm} layout="vertical" onFinish={onChangePassword}>
            <Form.Item
              name="eskiSifre"
              label="Mevcut Şifre"
              rules={[{ required: true, message: 'Mevcut şifrenizi girin' }]}
            >
              <Input.Password />
            </Form.Item>

            <Form.Item
              name="yeniSifre"
              label="Yeni Şifre"
              rules={[
                { required: true, message: 'Yeni şifrenizi girin' },
                { min: 8, message: 'Şifre en az 8 karakter olmalıdır' },
              ]}
            >
              <Input.Password />
            </Form.Item>

            <Form.Item
              name="yeniSifreTekrar"
              label="Yeni Şifre (Tekrar)"
              rules={[
                { required: true, message: 'Yeni şifrenizi tekrar girin' },
                ({ getFieldValue }) => ({
                  validator(_, value) {
                    if (!value || getFieldValue('yeniSifre') === value) {
                      return Promise.resolve()
                    }
                    return Promise.reject(new Error('Şifreler eşleşmiyor'))
                  },
                }),
              ]}
            >
              <Input.Password />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                loading={changePasswordMutation.isLoading}
                style={{ background: '#b91c1c' }}
              >
                Şifreyi Değiştir
              </Button>
            </Form.Item>
          </Form>
        </Card>

        <Card title="İki Faktörlü Doğrulama" variant="borderless" className="shadow-sm">
          <p className="mb-4">
            İki faktörlü doğrulama, hesabınıza ekstra bir güvenlik katmanı ekler.
          </p>
          <div className="flex items-center justify-between">
            <span>İki Faktörlü Doğrulama</span>
            <Switch defaultChecked={false} />
          </div>
        </Card>
      </div>
    </div>
  )
}

export default ProfilPage
