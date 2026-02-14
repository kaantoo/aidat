import { useState, useEffect } from 'react'
import { Card, Form, Input, Button, Alert, App, Result } from 'antd'
import { LockOutlined } from '@ant-design/icons'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { authApi } from '@/api/auth'

const ResetPasswordPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const { message } = App.useApp()

  const token = searchParams.get('token')

  useEffect(() => {
    if (!token) {
      setError('Geçersiz şifre sıfırlama bağlantısı')
    }
  }, [token])

  const onFinish = async (values: { newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('Şifreler eşleşmiyor')
      return
    }

    if (!token) {
      message.error('Geçersiz token')
      return
    }

    setLoading(true)
    setError(null)

    try {
      await authApi.resetPassword(token, values.newPassword)
      setSuccess(true)
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } }
      setError(error.response?.data?.message || 'Şifre sıfırlanırken bir hata oluştu')
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <Card className="shadow-2xl max-w-md mx-auto mt-20">
        <Result
          status="success"
          title="Şifreniz Başarıyla Değiştirildi"
          subTitle="Yeni şifreniz ile giriş yapabilirsiniz."
          extra={[
            <Button 
              type="primary" 
              key="login"
              style={{ background: '#b91c1c' }}
              onClick={() => navigate('/login')}
            >
              Giriş Yap
            </Button>
          ]}
        />
      </Card>
    )
  }

  if (!token) {
    return (
      <Card className="shadow-2xl max-w-md mx-auto mt-20">
        <Result
          status="error"
          title="Geçersiz Bağlantı"
          subTitle="Şifre sıfırlama bağlantısı geçersiz veya süresi dolmuş olabilir."
          extra={[
            <Button 
              type="primary" 
              key="login"
              style={{ background: '#b91c1c' }}
              onClick={() => navigate('/login')}
            >
              Giriş Sayfasına Dön
            </Button>
          ]}
        />
      </Card>
    )
  }

  return (
    <Card className="shadow-2xl max-w-md mx-auto mt-20">
      <div className="text-center mb-6">
        <h2 className="text-2xl font-semibold text-gray-800">Şifre Sıfırla</h2>
        <p className="text-gray-500 mt-1">Yeni şifrenizi belirleyin</p>
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
        name="resetPassword"
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          name="newPassword"
          rules={[
            { required: true, message: 'Yeni şifre gereklidir' },
            { min: 8, message: 'Şifre en az 8 karakter olmalıdır' },
            {
              pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])/,
              message: 'Şifre büyük/küçük harf, rakam ve özel karakter içermelidir'
            }
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className="text-gray-400" />}
            placeholder="Yeni Şifre"
          />
        </Form.Item>

        <Form.Item
          name="confirmPassword"
          dependencies={['newPassword']}
          rules={[
            { required: true, message: 'Şifre tekrarı gereklidir' },
            ({ getFieldValue }) => ({
              validator(_, value) {
                if (!value || getFieldValue('newPassword') === value) {
                  return Promise.resolve()
                }
                return Promise.reject(new Error('Şifreler eşleşmiyor'))
              },
            }),
          ]}
        >
          <Input.Password
            prefix={<LockOutlined className="text-gray-400" />}
            placeholder="Şifre Tekrarı"
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
            Şifreyi Değiştir
          </Button>
        </Form.Item>
      </Form>

      <div className="mt-4 text-center">
        <a 
          className="text-red-700 hover:text-red-800 cursor-pointer"
          onClick={() => navigate('/login')}
        >
          Giriş Sayfasına Dön
        </a>
      </div>
    </Card>
  )
}

export default ResetPasswordPage
