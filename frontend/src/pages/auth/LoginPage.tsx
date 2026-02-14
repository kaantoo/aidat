import { useState } from 'react'
import { Card, Form, Input, Button, Alert, App, Modal } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons'
import { useAuthStore } from '@/store/authStore'
import { useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'

interface LoginFormValues {
  kullaniciAdi: string
  sifre: string
}

const LoginPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [forgotPasswordModal, setForgotPasswordModal] = useState(false)
  const [forgotPasswordLoading, setForgotPasswordLoading] = useState(false)
  const { login } = useAuthStore()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [forgotForm] = Form.useForm()
  const { message } = App.useApp()

  const onFinish = async (values: LoginFormValues) => {
    setLoading(true)
    setError(null)

    try {
      const result = await login(values.kullaniciAdi, values.sifre)
      if (result.twoFactorRequired) {
        navigate('/two-factor')
      } else {
        message.success('Giriş başarılı!')
        navigate('/')
      }
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } }
      setError(error.response?.data?.message || 'Giriş yapılırken bir hata oluştu')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="shadow-2xl">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-semibold text-gray-800">Giriş Yap</h2>
        <p className="text-gray-500 mt-1">Hesabınıza giriş yapın</p>
      </div>

      {error && (
        <Alert
          message={error}
          type="error"
          showIcon
          closable
          onClose={() => setError(null)}
          className="mb-4"
        />
      )}

      <Form
        form={form}
        name="login"
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          name="kullaniciAdi"
          rules={[
            { required: true, message: 'Kullanıcı adı gereklidir' },
            { min: 3, message: 'Kullanıcı adı en az 3 karakter olmalıdır' },
          ]}
        >
          <Input
            prefix={<UserOutlined className="text-gray-400" />}
            placeholder="Kullanıcı Adı"
          />
        </Form.Item>

        <Form.Item
          name="sifre"
          rules={[
            { required: true, message: 'Şifre gereklidir' },
            { min: 6, message: 'Şifre en az 6 karakter olmalıdır' },
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className="text-gray-400" />}
            placeholder="Şifre"
          />
        </Form.Item>

        <Form.Item className="mb-0">
          <Button
            type="primary"
            htmlType="submit"
            loading={loading}
            block
            style={{ background: '#b91c1c', height: 44 }}
          >
            Giriş Yap
          </Button>
        </Form.Item>
      </Form>

      <div className="mt-4 text-center">
        <a 
          className="text-red-700 hover:text-red-800 cursor-pointer"
          onClick={() => setForgotPasswordModal(true)}
        >
          Şifremi Unuttum
        </a>
      </div>

      {/* Şifremi Unuttum Modal */}
      <Modal
        title="Şifremi Unuttum"
        open={forgotPasswordModal}
        onCancel={() => {
          setForgotPasswordModal(false)
          forgotForm.resetFields()
        }}
        footer={null}
      >
        <p className="text-gray-600 mb-4">
          Kayıtlı e-posta adresinizi girin. Şifre sıfırlama bağlantısı e-posta adresinize gönderilecektir.
        </p>
        <Form
          form={forgotForm}
          layout="vertical"
          onFinish={async (values: { email: string }) => {
            setForgotPasswordLoading(true)
            try {
              await authApi.forgotPassword(values.email)
              message.success('Şifre sıfırlama bağlantısı e-posta adresinize gönderildi')
              setForgotPasswordModal(false)
              forgotForm.resetFields()
            } catch (err) {
              message.error('İşlem sırasında bir hata oluştu')
            } finally {
              setForgotPasswordLoading(false)
            }
          }}
        >
          <Form.Item
            name="email"
            rules={[
              { required: true, message: 'E-posta adresi gereklidir' },
              { type: 'email', message: 'Geçerli bir e-posta adresi girin' },
            ]}
          >
            <Input
              prefix={<MailOutlined className="text-gray-400" />}
              placeholder="E-posta Adresi"
              size="large"
            />
          </Form.Item>
          <Form.Item className="mb-0">
            <Button
              type="primary"
              htmlType="submit"
              loading={forgotPasswordLoading}
              block
              style={{ background: '#b91c1c' }}
            >
              Şifre Sıfırlama Bağlantısı Gönder
            </Button>
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  )
}

export default LoginPage
