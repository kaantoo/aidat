import { useState } from 'react'
import { Card, Descriptions, Button, Form, Input, App, Switch, Modal, Steps, QRCode, Alert } from 'antd'
import { SafetyOutlined, LockOutlined } from '@ant-design/icons'
import { useAuthStore } from '@/store/authStore'
import { authApi } from '@/api/auth'
import { useMutation } from 'react-query'
import { KullaniciRol } from '@/types'

const ProfilPage: React.FC = () => {
  const { user } = useAuthStore()
  const [passwordForm] = Form.useForm()
  const { message } = App.useApp()

  // 2FA state
  const [twoFAEnabled, setTwoFAEnabled] = useState(false)
  const [setupModalVisible, setSetupModalVisible] = useState(false)
  const [disableModalVisible, setDisableModalVisible] = useState(false)
  const [setupStep, setSetupStep] = useState(0)
  const [qrData, setQrData] = useState<{ qrCodeUrl: string; secret: string } | null>(null)
  const [verifyCode, setVerifyCode] = useState('')
  const [disableCode, setDisableCode] = useState('')

  const changePasswordMutation = useMutation(
    (data: { eskiSifre: string; yeniSifre: string }) => authApi.changePassword(data),
    {
      onSuccess: () => {
        message.success('Şifre başarıyla değiştirildi')
        passwordForm.resetFields()
      },
      onError: () => { message.error('Şifre değiştirirken hata oluştu') },
    }
  )

  const setup2FAMutation = useMutation(
    () => authApi.setup2FA(),
    {
      onSuccess: (response) => {
        setQrData(response.data)
        setSetupStep(1)
      },
      onError: () => { message.error('2FA kurulumu başlatılamadı') },
    }
  )

  const enable2FAMutation = useMutation(
    (code: string) => authApi.enable2FA(code),
    {
      onSuccess: () => {
        message.success('İki faktörlü doğrulama aktif edildi')
        setTwoFAEnabled(true)
        setSetupModalVisible(false)
        setSetupStep(0)
        setQrData(null)
        setVerifyCode('')
      },
      onError: () => { message.error('Doğrulama kodu hatalı') },
    }
  )

  const disable2FAMutation = useMutation(
    (code: string) => authApi.disable2FA(code),
    {
      onSuccess: () => {
        message.success('İki faktörlü doğrulama kapatıldı')
        setTwoFAEnabled(false)
        setDisableModalVisible(false)
        setDisableCode('')
      },
      onError: () => { message.error('Doğrulama kodu hatalı') },
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

  const handleTwoFAToggle = (checked: boolean) => {
    if (checked) {
      setSetupStep(0)
      setSetupModalVisible(true)
      setup2FAMutation.mutate()
    } else {
      setDisableModalVisible(true)
    }
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

        <Card
          title={
            <span className="flex items-center gap-2">
              <SafetyOutlined /> İki Faktörlü Doğrulama (2FA)
            </span>
          }
          variant="borderless"
          className="shadow-sm lg:col-span-2"
        >
          <Alert
            type="info"
            message="İki faktörlü doğrulama, hesabınıza giriş yaparken telefon uygulamanızdan ek bir doğrulama kodu ister. Bu sayede hesabınız daha güvenli hale gelir."
            showIcon
            className="mb-4"
          />
          <div className="flex items-center justify-between">
            <div>
              <div className="font-medium">İki Faktörlü Doğrulama</div>
              <div className="text-sm text-gray-500">
                {twoFAEnabled ? 'Aktif - Hesabınız ek güvenlik katmanı ile korunuyor' : 'Kapalı - Etkinleştirmeniz önerilir'}
              </div>
            </div>
            <Switch
              checked={twoFAEnabled}
              onChange={handleTwoFAToggle}
              loading={setup2FAMutation.isLoading}
              checkedChildren={<LockOutlined />}
            />
          </div>
        </Card>
      </div>

      {/* 2FA Kurulum Modalı */}
      <Modal
        title="İki Faktörlü Doğrulama Kurulumu"
        open={setupModalVisible}
        onCancel={() => { setSetupModalVisible(false); setSetupStep(0); setQrData(null); setVerifyCode('') }}
        footer={null}
        width={480}
      >
        <Steps
          current={setupStep}
          size="small"
          className="mb-6"
          items={[
            { title: 'QR Kodu Tara' },
            { title: 'Doğrula' },
          ]}
        />

        {setup2FAMutation.isLoading && (
          <div className="text-center py-8">Kurulum hazırlanıyor...</div>
        )}

        {setupStep === 1 && qrData && (
          <div className="text-center">
            <p className="mb-4">Google Authenticator veya benzeri bir uygulama ile aşağıdaki QR kodu tarayın:</p>
            <div className="flex justify-center mb-4">
              <QRCode value={qrData.qrCodeUrl} size={200} />
            </div>
            <div className="bg-gray-50 p-3 rounded mb-4">
              <div className="text-xs text-gray-500 mb-1">Manuel giriş kodu:</div>
              <code className="text-sm font-mono select-all">{qrData.secret}</code>
            </div>
            <Input
              placeholder="Uygulamadaki 6 haneli kodu girin"
              value={verifyCode}
              onChange={(e) => setVerifyCode(e.target.value)}
              maxLength={6}
              className="mb-4"
              size="large"
              style={{ textAlign: 'center', letterSpacing: '0.5em', fontWeight: 'bold' }}
            />
            <Button
              type="primary"
              block
              onClick={() => enable2FAMutation.mutate(verifyCode)}
              loading={enable2FAMutation.isLoading}
              disabled={verifyCode.length !== 6}
              style={{ background: '#b91c1c' }}
            >
              Doğrula ve Aktif Et
            </Button>
          </div>
        )}
      </Modal>

      {/* 2FA Kapatma Modalı */}
      <Modal
        title="İki Faktörlü Doğrulamayı Kapat"
        open={disableModalVisible}
        onCancel={() => { setDisableModalVisible(false); setDisableCode('') }}
        footer={null}
        width={400}
      >
        <Alert type="warning" message="2FA'yı kapattığınızda hesabınız daha az güvenli olacaktır." showIcon className="mb-4" />
        <p className="mb-4">Kapatmak için mevcut 2FA uygulamanızdaki kodu girin:</p>
        <Input
          placeholder="6 haneli doğrulama kodu"
          value={disableCode}
          onChange={(e) => setDisableCode(e.target.value)}
          maxLength={6}
          className="mb-4"
          size="large"
          style={{ textAlign: 'center', letterSpacing: '0.5em', fontWeight: 'bold' }}
        />
        <Button
          type="primary"
          danger
          block
          onClick={() => disable2FAMutation.mutate(disableCode)}
          loading={disable2FAMutation.isLoading}
          disabled={disableCode.length !== 6}
        >
          2FA'yı Kapat
        </Button>
      </Modal>
    </div>
  )
}

export default ProfilPage
