import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Form, Input, Select, Button, Space, App, Spin, Switch } from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useBirlikStore } from '@/store/birlikStore'
import { BirlikTipi } from '@/types'
import { BirlikCreateDto } from '@/api/birlik'

const BirlikFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const { message } = App.useApp()
  const { selectedBirlik, isLoading, fetchBirlikById, createBirlik, updateBirlik } =
    useBirlikStore()

  const isEditMode = !!id

  useEffect(() => {
    if (id) {
      fetchBirlikById(parseInt(id))
    }
  }, [id, fetchBirlikById])

  useEffect(() => {
    if (isEditMode && selectedBirlik) {
      form.setFieldsValue(selectedBirlik)
    }
  }, [isEditMode, selectedBirlik, form])

  const onFinish = async (values: BirlikCreateDto & { aktif?: boolean }) => {
    setLoading(true)
    try {
      if (isEditMode && id) {
        await updateBirlik(parseInt(id), values)
        message.success('Birlik başarıyla güncellendi')
      } else {
        await createBirlik(values)
        message.success('Birlik başarıyla oluşturuldu')
      }
      navigate('/birlikler')
    } catch (err: unknown) {
      const error = err as { response?: { status?: number; data?: { message?: string } } }
      if (error.response?.status === 409) {
        message.error('Bu birlik kodu zaten kullanılıyor')
      } else {
        message.error(error.response?.data?.message || 'İşlem sırasında bir hata oluştu')
      }
    } finally {
      setLoading(false)
    }
  }

  if (isEditMode && isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" tip="Yükleniyor..." />
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center gap-4 mb-4">
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/birlikler')}>
          Geri
        </Button>
        <h1 className="text-xl font-semibold m-0">
          {isEditMode ? 'Birlik Düzenle' : 'Yeni Birlik'}
        </h1>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ aktif: true }}
          style={{ maxWidth: 800 }}
        >
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6">
            <Form.Item
              name="birlikKodu"
              label="Birlik Kodu"
              rules={[
                { required: true, message: 'Birlik kodu gereklidir' },
                { min: 2, message: 'En az 2 karakter olmalıdır' },
                { max: 10, message: 'En fazla 10 karakter olabilir' },
                { pattern: /^[A-Z0-9]+$/, message: 'Sadece büyük harf ve rakam kullanınız' },
              ]}
              normalize={(value) => value?.toUpperCase()}
            >
              <Input 
                placeholder="Örn: ANK01" 
                disabled={isEditMode}
                style={{ textTransform: 'uppercase' }}
              />
            </Form.Item>

            <Form.Item
              name="birlikAdi"
              label="Birlik Adı"
              rules={[
                { required: true, message: 'Birlik adı gereklidir' },
                { max: 100, message: 'En fazla 100 karakter olabilir' },
              ]}
            >
              <Input placeholder="Birlik adını girin" />
            </Form.Item>

            <Form.Item
              name="birlikTipi"
              label="Birlik Tipi"
              rules={[{ required: true, message: 'Birlik tipi seçiniz' }]}
            >
              <Select placeholder="Birlik tipi seçin">
                <Select.Option value={BirlikTipi.MERKEZ}>Merkez Birliği</Select.Option>
                <Select.Option value={BirlikTipi.ALT_BIRLIK}>Alt Birlik</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              name="ilKodu"
              label="İl Kodu"
              rules={[
                { required: true, message: 'İl kodu gereklidir' },
                { len: 2, message: 'İl kodu 2 karakter olmalıdır' },
              ]}
            >
              <Input placeholder="Örn: 06" maxLength={2} />
            </Form.Item>

            <Form.Item name="ilceKodu" label="İlçe Kodu">
              <Input placeholder="Örn: 001" />
            </Form.Item>

            <Form.Item name="telefon" label="Telefon">
              <Input placeholder="0312 XXX XX XX" />
            </Form.Item>

            <Form.Item
              name="email"
              label="E-posta"
              rules={[{ type: 'email', message: 'Geçerli bir e-posta girin' }]}
            >
              <Input placeholder="email@example.com" />
            </Form.Item>

            <Form.Item name="vergiNo" label="Vergi No">
              <Input placeholder="Vergi numarası" />
            </Form.Item>

            <Form.Item name="vergiDairesi" label="Vergi Dairesi">
              <Input placeholder="Vergi dairesi" />
            </Form.Item>

            <Form.Item name="ibanNo" label="IBAN">
              <Input placeholder="TR XX XXXX XXXX XXXX XXXX XXXX XX" />
            </Form.Item>

            <Form.Item name="yetkiliAdi" label="Yetkili Adı">
              <Input placeholder="Yetkili kişi adı" />
            </Form.Item>

            <Form.Item name="yetkiliTelefon" label="Yetkili Telefon">
              <Input placeholder="0532 XXX XX XX" />
            </Form.Item>
          </div>

          <Form.Item name="adres" label="Adres">
            <Input.TextArea rows={3} placeholder="Açık adres" />
          </Form.Item>

          {isEditMode && (
            <Form.Item name="aktif" label="Durum" valuePropName="checked">
              <Switch checkedChildren="Aktif" unCheckedChildren="Pasif" />
            </Form.Item>
          )}

          <Form.Item className="mb-0">
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={loading}
                style={{ background: '#b91c1c' }}
              >
                {isEditMode ? 'Güncelle' : 'Kaydet'}
              </Button>
              <Button onClick={() => navigate('/birlikler')}>İptal</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default BirlikFormPage
