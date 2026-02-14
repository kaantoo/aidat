import { useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  Card,
  Form,
  Input,
  Button,
  Select,
  Row,
  Col,
  Spin,
  Divider,
  App,
} from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { kullaniciApi, KullaniciCreateDto, KullaniciUpdateDto } from '@/api/kullanici'
import { birlikApi } from '@/api/birlik'
import { KullaniciRol, KullaniciDurum } from '@/types'

const KullaniciFormPage: React.FC = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const [form] = Form.useForm()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const isEdit = !!id

  const { data: kullanici, isLoading: isLoadingKullanici } = useQuery(
    ['kullanici', id],
    () => kullaniciApi.getById(Number(id)),
    { enabled: isEdit }
  )

  const { data: birlikler } = useQuery('birlikler', () => birlikApi.getAll())

  useEffect(() => {
    if (kullanici?.data) {
      form.setFieldsValue({
        ...kullanici.data,
        birlikId: kullanici.data.birlikId,
      })
    }
  }, [kullanici, form])

  const createMutation = useMutation(
    (data: KullaniciCreateDto) => kullaniciApi.create(data),
    {
      onSuccess: () => {
        message.success('Kullanıcı başarıyla oluşturuldu')
        queryClient.invalidateQueries('kullanicilar')
        navigate('/kullanicilar')
      },
      onError: (error: any) => {
        const errorData = error?.response?.data
        if (errorData?.errors && Array.isArray(errorData.errors)) {
          // Validation hataları
          errorData.errors.forEach((err: string) => message.error(err))
        } else if (errorData?.message) {
          message.error(errorData.message)
        } else {
          message.error('Kullanıcı oluşturulurken hata oluştu')
        }
      },
    }
  )

  const updateMutation = useMutation(
    (data: KullaniciUpdateDto) => kullaniciApi.update(Number(id), data),
    {
      onSuccess: () => {
        message.success('Kullanıcı başarıyla güncellendi')
        queryClient.invalidateQueries('kullanicilar')
        navigate('/kullanicilar')
      },
      onError: (error: any) => {
        const errorData = error?.response?.data
        if (errorData?.errors && Array.isArray(errorData.errors)) {
          errorData.errors.forEach((err: string) => message.error(err))
        } else if (errorData?.message) {
          message.error(errorData.message)
        } else {
          message.error('Kullanıcı güncellenirken hata oluştu')
        }
      },
    }
  )

  const handleSubmit = (values: any) => {
    if (isEdit) {
      const updateData: KullaniciUpdateDto = {
        email: values.email,
        ad: values.ad,
        soyad: values.soyad,
        telefon: values.telefon,
        rol: values.rol,
        birlikId: values.birlikId,
        durum: values.durum,
      }
      updateMutation.mutate(updateData)
    } else {
      const createData: KullaniciCreateDto = {
        kullaniciAdi: values.kullaniciAdi,
        email: values.email,
        sifre: values.sifre,
        ad: values.ad,
        soyad: values.soyad,
        telefon: values.telefon,
        rol: values.rol,
        birlikId: values.birlikId,
      }
      createMutation.mutate(createData)
    }
  }

  const roller = [
    { value: KullaniciRol.SISTEM_ADMIN, label: 'Sistem Admin' },
    { value: KullaniciRol.MERKEZ_YONETICI, label: 'Merkez Yönetici' },
    { value: KullaniciRol.BIRLIK_YONETICI, label: 'Birlik Yönetici' },
    { value: KullaniciRol.BIRLIK_PERSONEL, label: 'Birlik Personel' },
    { value: KullaniciRol.MUHASEBE_SORUMLU, label: 'Muhasebe Sorumlu' },
    { value: KullaniciRol.GOZLEMCI, label: 'Gözlemci' },
  ]

  const durumlar = [
    { value: KullaniciDurum.AKTIF, label: 'Aktif' },
    { value: KullaniciDurum.PASIF, label: 'Pasif' },
  ]

  if (isEdit && isLoadingKullanici) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" tip="Yükleniyor..." />
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center mb-4">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/kullanicilar')}
          className="mr-2"
        />
        <h1 className="text-xl font-semibold">
          {isEdit ? 'Kullanıcı Düzenle' : 'Yeni Kullanıcı'}
        </h1>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            rol: KullaniciRol.BIRLIK_PERSONEL,
            durum: KullaniciDurum.AKTIF,
          }}
        >
          <Divider orientation="left">Hesap Bilgileri</Divider>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item
                name="kullaniciAdi"
                label="Kullanıcı Adı"
                rules={[
                  { required: true, message: 'Kullanıcı adı zorunludur' },
                  { min: 3, message: 'En az 3 karakter olmalı' },
                  {
                    pattern: /^[a-zA-Z0-9_]+$/,
                    message: 'Sadece harf, rakam ve alt çizgi kullanılabilir',
                  },
                ]}
              >
                <Input placeholder="kullanici_adi" disabled={isEdit} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                name="email"
                label="E-posta"
                rules={[
                  { required: true, message: 'E-posta zorunludur' },
                  { type: 'email', message: 'Geçerli bir e-posta girin' },
                ]}
              >
                <Input placeholder="ornek@email.com" />
              </Form.Item>
            </Col>
            {!isEdit && (
              <Col xs={24} md={8}>
                <Form.Item
                  name="sifre"
                  label="Şifre"
                  rules={[
                    { required: true, message: 'Şifre zorunludur' },
                    { min: 8, message: 'Şifre en az 8 karakter olmalıdır' },
                    {
                      pattern: /[A-Z]/,
                      message: 'Şifre en az bir büyük harf içermelidir',
                    },
                    {
                      pattern: /[a-z]/,
                      message: 'Şifre en az bir küçük harf içermelidir',
                    },
                    {
                      pattern: /[0-9]/,
                      message: 'Şifre en az bir rakam içermelidir',
                    },
                    {
                      pattern: /[!@#$%^&*(),.?":{}|<>]/,
                      message: 'Şifre en az bir özel karakter içermelidir',
                    },
                  ]}
                  extra="En az 8 karakter, 1 büyük harf, 1 küçük harf, 1 rakam ve 1 özel karakter (!@#$%^&*(),.?)"
                >
                  <Input.Password placeholder="******" />
                </Form.Item>
              </Col>
            )}
          </Row>

          <Divider orientation="left">Kişisel Bilgiler</Divider>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item
                name="ad"
                label="Ad"
                rules={[{ required: true, message: 'Ad zorunludur' }]}
              >
                <Input placeholder="Ad" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                name="soyad"
                label="Soyad"
                rules={[{ required: true, message: 'Soyad zorunludur' }]}
              >
                <Input placeholder="Soyad" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item name="telefon" label="Telefon">
                <Input placeholder="05XX XXX XX XX" />
              </Form.Item>
            </Col>
          </Row>

          <Divider orientation="left">Yetki Bilgileri</Divider>

          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item
                name="rol"
                label="Rol"
                rules={[{ required: true, message: 'Rol seçiniz' }]}
              >
                <Select placeholder="Rol Seçin">
                  {roller.map((r) => (
                    <Select.Option key={r.value} value={r.value}>
                      {r.label}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item
                noStyle
                shouldUpdate={(prevValues, currentValues) =>
                  prevValues.rol !== currentValues.rol
                }
              >
                {({ getFieldValue }) => {
                  const rol = getFieldValue('rol')
                  const showBirlik =
                    rol === KullaniciRol.BIRLIK_YONETICI ||
                    rol === KullaniciRol.BIRLIK_PERSONEL ||
                    rol === KullaniciRol.MUHASEBE_SORUMLU
                  return showBirlik ? (
                    <Form.Item
                      name="birlikId"
                      label="Birlik"
                      rules={[{ required: true, message: 'Birlik seçiniz' }]}
                    >
                      <Select placeholder="Birlik Seçin">
                        {birlikler?.data?.map((b) => (
                          <Select.Option key={b.id} value={b.id}>
                            {b.birlikAdi}
                          </Select.Option>
                        ))}
                      </Select>
                    </Form.Item>
                  ) : null
                }}
              </Form.Item>
            </Col>
            {isEdit && (
              <Col xs={24} md={8}>
                <Form.Item name="durum" label="Durum">
                  <Select placeholder="Durum Seçin">
                    {durumlar.map((d) => (
                      <Select.Option key={d.value} value={d.value}>
                        {d.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              </Col>
            )}
          </Row>

          <Divider />

          <Form.Item className="mb-0">
            <div className="flex justify-end gap-2">
              <Button onClick={() => navigate('/kullanicilar')}>İptal</Button>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={createMutation.isLoading || updateMutation.isLoading}
                style={{ background: '#b91c1c' }}
              >
                {isEdit ? 'Güncelle' : 'Kaydet'}
              </Button>
            </div>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default KullaniciFormPage
