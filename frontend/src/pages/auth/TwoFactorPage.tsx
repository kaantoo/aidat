import { useState } from 'react'
import { Card, Form, Input, Button, Alert, App } from 'antd'
import { SafetyOutlined } from '@ant-design/icons'
import { useAuthStore } from '@/store/authStore'
import { useNavigate } from 'react-router-dom'

interface TwoFactorFormValues {
  code: string
}

const TwoFactorPage: React.FC = () => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { verifyTwoFactor, pendingUsername } = useAuthStore()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const { message } = App.useApp()

  const onFinish = async (values: TwoFactorFormValues) => {
    setLoading(true)
    setError(null)

    try {
      await verifyTwoFactor(values.code)
      message.success('Giriş başarılı!')
      navigate('/')
    } catch (err: unknown) {
      const error = err as { response?: { data?: { message?: string } } }
      setError(error.response?.data?.message || 'Doğrulama kodu hatalı')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="shadow-2xl">
      <div className="text-center mb-6">
        <SafetyOutlined style={{ fontSize: 48, color: '#b91c1c' }} />
        <h2 className="text-2xl font-semibold text-gray-800 mt-4">
          İki Faktörlü Doğrulama
        </h2>
        <p className="text-gray-500 mt-1">
          {pendingUsername} için doğrulama kodunu girin
        </p>
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
        name="twoFactor"
        onFinish={onFinish}
        layout="vertical"
        size="large"
      >
        <Form.Item
          name="code"
          rules={[
            { required: true, message: 'Doğrulama kodu gereklidir' },
            { len: 6, message: 'Doğrulama kodu 6 haneli olmalıdır' },
            { pattern: /^\d+$/, message: 'Sadece rakam giriniz' },
          ]}
        >
          <Input
            placeholder="000000"
            maxLength={6}
            style={{ textAlign: 'center', fontSize: 24, letterSpacing: 8 }}
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
            Doğrula
          </Button>
        </Form.Item>
      </Form>

      <div className="mt-4 text-center text-gray-500 text-sm">
        Authenticator uygulamanızdaki 6 haneli kodu girin
      </div>
    </Card>
  )
}

export default TwoFactorPage
