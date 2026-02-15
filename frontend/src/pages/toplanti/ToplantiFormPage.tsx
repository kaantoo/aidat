import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Form, Input, Select, DatePicker, TimePicker, Button, Space, message, Spin } from 'antd'
import { ArrowLeftOutlined, SaveOutlined, CalendarOutlined } from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { toplantiApi, ToplantiCreateRequest, ToplantiUpdateRequest } from '@/api/toplanti'
import { useAuthStore } from '@/store/authStore'
import { Toplanti, ToplantiTuru, ToplantiDurumu, KullaniciRol } from '@/types'
import dayjs from 'dayjs'

const toplantiTuruOptions = [
  { value: ToplantiTuru.GENEL_KURUL, label: 'Genel Kurul' },
  { value: ToplantiTuru.YONETIM_KURULU, label: 'Yönetim Kurulu' },
  { value: ToplantiTuru.DENETIM_KURULU, label: 'Denetim Kurulu' },
  { value: ToplantiTuru.OLAGAN_TOPLANTI, label: 'Olağan Toplantı' },
  { value: ToplantiTuru.OLAGANUSTU_TOPLANTI, label: 'Olağanüstü Toplantı' },
  { value: ToplantiTuru.DIGER, label: 'Diğer' },
]

const toplantiDurumuOptions = [
  { value: ToplantiDurumu.PLANLANMIS, label: 'Planlanmış' },
  { value: ToplantiDurumu.DEVAM_EDIYOR, label: 'Devam Ediyor' },
  { value: ToplantiDurumu.TAMAMLANDI, label: 'Tamamlandı' },
  { value: ToplantiDurumu.IPTAL, label: 'İptal' },
  { value: ToplantiDurumu.ERTELENDI, label: 'Ertelendi' },
]

const ToplantiFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const [form] = Form.useForm()
  const { user, hasRole } = useAuthStore()
  const isEditing = !!id

  // Mevcut toplantı verisini çek (edit mode)
  const { data: toplanti, isLoading } = useQuery<Toplanti>(
    ['toplanti', id],
    async () => {
      const res = await toplantiApi.getById(Number(id))
      return res.data
    },
    { enabled: isEditing }
  )

  useEffect(() => {
    if (toplanti) {
      form.setFieldsValue({
        birlikId: toplanti.birlikId,
        baslik: toplanti.baslik,
        toplantiTuru: toplanti.toplantiTuru,
        durum: toplanti.durum,
        toplantiTarihi: toplanti.toplantiTarihi ? dayjs(toplanti.toplantiTarihi) : null,
        baslangicSaati: toplanti.baslangicSaati ? dayjs(toplanti.baslangicSaati, 'HH:mm') : null,
        bitisSaati: toplanti.bitisSaati ? dayjs(toplanti.bitisSaati, 'HH:mm') : null,
        yer: toplanti.yer,
        gundem: toplanti.gundem,
        aciklama: toplanti.aciklama,
      })
    }
  }, [toplanti, form])

  const createMutation = useMutation(
    (data: ToplantiCreateRequest) => toplantiApi.create(data),
    {
      onSuccess: (res) => {
        message.success('Toplantı oluşturuldu')
        queryClient.invalidateQueries('toplantilar')
        navigate(`/toplantilar/${res.data.id}`)
      },
      onError: () => { message.error('Toplantı oluşturulamadı') },
    }
  )

  const updateMutation = useMutation(
    (data: ToplantiUpdateRequest) => toplantiApi.update(Number(id), data),
    {
      onSuccess: () => {
        message.success('Toplantı güncellendi')
        queryClient.invalidateQueries(['toplanti', id])
        queryClient.invalidateQueries('toplantilar')
        navigate(`/toplantilar/${id}`)
      },
      onError: () => { message.error('Toplantı güncellenemedi') },
    }
  )

  const handleFinish = (values: Record<string, unknown>) => {
    const payload = {
      ...values,
      toplantiTarihi: values.toplantiTarihi
        ? (values.toplantiTarihi as dayjs.Dayjs).format('YYYY-MM-DD')
        : undefined,
      baslangicSaati: values.baslangicSaati
        ? (values.baslangicSaati as dayjs.Dayjs).format('HH:mm')
        : undefined,
      bitisSaati: values.bitisSaati
        ? (values.bitisSaati as dayjs.Dayjs).format('HH:mm')
        : undefined,
    }

    if (isEditing) {
      updateMutation.mutate(payload as unknown as ToplantiUpdateRequest)
    } else {
      createMutation.mutate(payload as unknown as ToplantiCreateRequest)
    }
  }

  const isSistemAdmin = hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI])

  if (isEditing && isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" />
      </div>
    )
  }

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6 flex justify-between items-center">
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/toplantilar')}>
            Geri
          </Button>
          <h1 className="text-2xl font-semibold text-gray-800 flex items-center gap-2 m-0">
            <CalendarOutlined /> {isEditing ? 'Toplantı Düzenle' : 'Yeni Toplantı'}
          </h1>
        </Space>
      </div>

      <Card className="shadow-sm">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleFinish}
          initialValues={{
            birlikId: user?.birlikId,
            toplantiTuru: ToplantiTuru.YONETIM_KURULU,
            durum: ToplantiDurumu.PLANLANMIS,
          }}
        >
          {isSistemAdmin && (
            <Form.Item name="birlikId" label="Birlik" rules={[{ required: true, message: 'Birlik seçin' }]}>
              <Select
                placeholder="Birlik seçin"
                showSearch
                optionFilterProp="label"
              />
            </Form.Item>
          )}

          <Form.Item name="baslik" label="Başlık" rules={[{ required: true, message: 'Başlık gerekli' }]}>
            <Input placeholder="Toplantı başlığı" />
          </Form.Item>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Form.Item name="toplantiTuru" label="Toplantı Türü" rules={[{ required: true }]}>
              <Select options={toplantiTuruOptions} />
            </Form.Item>

            {isEditing && (
              <Form.Item name="durum" label="Durum">
                <Select options={toplantiDurumuOptions} />
              </Form.Item>
            )}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Form.Item
              name="toplantiTarihi"
              label="Toplantı Tarihi"
              rules={[{ required: true, message: 'Tarih gerekli' }]}
            >
              <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
            </Form.Item>
            <Form.Item name="baslangicSaati" label="Başlangıç Saati">
              <TimePicker style={{ width: '100%' }} format="HH:mm" />
            </Form.Item>
            <Form.Item name="bitisSaati" label="Bitiş Saati">
              <TimePicker style={{ width: '100%' }} format="HH:mm" />
            </Form.Item>
          </div>

          <Form.Item name="yer" label="Yer">
            <Input placeholder="Toplantı yeri" />
          </Form.Item>

          <Form.Item name="gundem" label="Gündem">
            <Input.TextArea rows={4} placeholder="Toplantı gündemi..." />
          </Form.Item>

          <Form.Item name="aciklama" label="Açıklama">
            <Input.TextArea rows={3} placeholder="Ek açıklama..." />
          </Form.Item>

          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={createMutation.isLoading || updateMutation.isLoading}
              >
                {isEditing ? 'Güncelle' : 'Oluştur'}
              </Button>
              <Button onClick={() => navigate('/toplantilar')}>İptal</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default ToplantiFormPage
