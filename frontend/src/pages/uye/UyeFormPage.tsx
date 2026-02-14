import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Form,
  Input,
  Select,
  Button,
  Space,
  Spin,
  DatePicker,
  InputNumber,
  Divider,
  App,
  Row,
  Col,
} from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { useMutation, useQuery, useQueryClient } from 'react-query'
import { uyeApi, UyeCreateDto } from '@/api/uye'
import { birlikApi } from '@/api/birlik'
import { UyelikTipi, Cinsiyet, UyeDurum } from '@/types'
import dayjs from 'dayjs'

/**
 * TC Kimlik Numarası doğrulama fonksiyonu
 * Türk vatandaşlık numarası algoritmasını kontrol eder
 */
const validateTcKimlik = (tcKimlik: string): boolean => {
  if (!tcKimlik || tcKimlik.length !== 11) return false
  if (!/^\d{11}$/.test(tcKimlik)) return false
  if (tcKimlik.charAt(0) === '0') return false

  const digits = tcKimlik.split('').map(Number)

  // Algoritma kontrolü
  const oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8]
  const evenSum = digits[1] + digits[3] + digits[5] + digits[7]

  let tenthDigit = ((oddSum * 7) - evenSum) % 10
  if (tenthDigit < 0) tenthDigit += 10

  if (digits[9] !== tenthDigit) return false

  // İlk 10 hanenin toplamının mod 10'u 11. hane ile eşit olmalı
  const totalSum = digits.slice(0, 10).reduce((a, b) => a + b, 0)
  if (digits[10] !== (totalSum % 10)) return false

  return true
}

// Türkiye illeri listesi
const iller = [
  { kod: '01', ad: 'Adana' }, { kod: '02', ad: 'Adıyaman' }, { kod: '03', ad: 'Afyonkarahisar' },
  { kod: '04', ad: 'Ağrı' }, { kod: '05', ad: 'Amasya' }, { kod: '06', ad: 'Ankara' },
  { kod: '07', ad: 'Antalya' }, { kod: '08', ad: 'Artvin' }, { kod: '09', ad: 'Aydın' },
  { kod: '10', ad: 'Balıkesir' }, { kod: '11', ad: 'Bilecik' }, { kod: '12', ad: 'Bingöl' },
  { kod: '13', ad: 'Bitlis' }, { kod: '14', ad: 'Bolu' }, { kod: '15', ad: 'Burdur' },
  { kod: '16', ad: 'Bursa' }, { kod: '17', ad: 'Çanakkale' }, { kod: '18', ad: 'Çankırı' },
  { kod: '19', ad: 'Çorum' }, { kod: '20', ad: 'Denizli' }, { kod: '21', ad: 'Diyarbakır' },
  { kod: '22', ad: 'Edirne' }, { kod: '23', ad: 'Elazığ' }, { kod: '24', ad: 'Erzincan' },
  { kod: '25', ad: 'Erzurum' }, { kod: '26', ad: 'Eskişehir' }, { kod: '27', ad: 'Gaziantep' },
  { kod: '28', ad: 'Giresun' }, { kod: '29', ad: 'Gümüşhane' }, { kod: '30', ad: 'Hakkari' },
  { kod: '31', ad: 'Hatay' }, { kod: '32', ad: 'Isparta' }, { kod: '33', ad: 'Mersin' },
  { kod: '34', ad: 'İstanbul' }, { kod: '35', ad: 'İzmir' }, { kod: '36', ad: 'Kars' },
  { kod: '37', ad: 'Kastamonu' }, { kod: '38', ad: 'Kayseri' }, { kod: '39', ad: 'Kırklareli' },
  { kod: '40', ad: 'Kırşehir' }, { kod: '41', ad: 'Kocaeli' }, { kod: '42', ad: 'Konya' },
  { kod: '43', ad: 'Kütahya' }, { kod: '44', ad: 'Malatya' }, { kod: '45', ad: 'Manisa' },
  { kod: '46', ad: 'Kahramanmaraş' }, { kod: '47', ad: 'Mardin' }, { kod: '48', ad: 'Muğla' },
  { kod: '49', ad: 'Muş' }, { kod: '50', ad: 'Nevşehir' }, { kod: '51', ad: 'Niğde' },
  { kod: '52', ad: 'Ordu' }, { kod: '53', ad: 'Rize' }, { kod: '54', ad: 'Sakarya' },
  { kod: '55', ad: 'Samsun' }, { kod: '56', ad: 'Siirt' }, { kod: '57', ad: 'Sinop' },
  { kod: '58', ad: 'Sivas' }, { kod: '59', ad: 'Tekirdağ' }, { kod: '60', ad: 'Tokat' },
  { kod: '61', ad: 'Trabzon' }, { kod: '62', ad: 'Tunceli' }, { kod: '63', ad: 'Şanlıurfa' },
  { kod: '64', ad: 'Uşak' }, { kod: '65', ad: 'Van' }, { kod: '66', ad: 'Yozgat' },
  { kod: '67', ad: 'Zonguldak' }, { kod: '68', ad: 'Aksaray' }, { kod: '69', ad: 'Bayburt' },
  { kod: '70', ad: 'Karaman' }, { kod: '71', ad: 'Kırıkkale' }, { kod: '72', ad: 'Batman' },
  { kod: '73', ad: 'Şırnak' }, { kod: '74', ad: 'Bartın' }, { kod: '75', ad: 'Ardahan' },
  { kod: '76', ad: 'Iğdır' }, { kod: '77', ad: 'Yalova' }, { kod: '78', ad: 'Karabük' },
  { kod: '79', ad: 'Kilis' }, { kod: '80', ad: 'Osmaniye' }, { kod: '81', ad: 'Düzce' },
]

const UyeFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const [form] = Form.useForm()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const isEditMode = !!id

  const { data: birliklerData } = useQuery('birlikler-select', () =>
    birlikApi.getAll(0, 1000)
  )

  const { data: uyeData, isLoading: isLoadingUye } = useQuery(
    ['uye', id],
    () => uyeApi.getById(parseInt(id!)),
    { enabled: isEditMode }
  )

  const createMutation = useMutation(uyeApi.create, {
    onSuccess: () => {
      message.success('Üye başarıyla oluşturuldu')
      queryClient.invalidateQueries('uyeler')
      navigate('/uyeler')
    },
    onError: (error: any) => {
      const errorMsg = error?.response?.data?.message || 'Üye oluşturulurken hata oluştu'
      message.error(errorMsg)
    },
  })

  const updateMutation = useMutation(
    (data: UyeCreateDto) => uyeApi.update(parseInt(id!), data),
    {
      onSuccess: () => {
        message.success('Üye başarıyla güncellendi')
        queryClient.invalidateQueries('uyeler')
        navigate('/uyeler')
      },
      onError: (error: any) => {
        const errorMsg = error?.response?.data?.message || 'Üye güncellenirken hata oluştu'
        message.error(errorMsg)
      },
    }
  )

  useEffect(() => {
    if (isEditMode && uyeData?.data) {
      const uye = uyeData.data
      form.setFieldsValue({
        ...uye,
        birlikId: uye.birlikId,
        dogumTarihi: uye.dogumTarihi ? dayjs(uye.dogumTarihi) : undefined,
        katilimTarihi: uye.katilimTarihi ? dayjs(uye.katilimTarihi) : undefined,
      })
    }
  }, [isEditMode, uyeData, form])

  const onFinish = async (values: any) => {
    // İl adını otomatik olarak set et
    if (values.ilKodu) {
      const il = iller.find(i => i.kod === values.ilKodu)
      if (il) {
        values.ilAdi = il.ad
      }
    }

    const submitData: UyeCreateDto = {
      ...values,
      dogumTarihi: values.dogumTarihi?.format('YYYY-MM-DD'),
      katilimTarihi: values.katilimTarihi?.format('YYYY-MM-DD'),
    }

    if (isEditMode) {
      updateMutation.mutate(submitData)
    } else {
      createMutation.mutate(submitData)
    }
  }

  const uyelikTipiValue = Form.useWatch('uyelikTipi', form)

  if (isEditMode && isLoadingUye) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" tip="Yükleniyor..." />
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center gap-4 mb-4">
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/uyeler')}>
          Geri
        </Button>
        <h1 className="text-xl font-semibold m-0">
          {isEditMode ? 'Üye Düzenle' : 'Yeni Üye Kaydı'}
        </h1>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          initialValues={{ uyelikTipi: UyelikTipi.GERCEK_KISI }}
        >
          {/* ==================== Birlik ve Üyelik Bilgileri ==================== */}
          <Divider orientation="left">Birlik ve Üyelik Bilgileri</Divider>

          <Row gutter={24}>
            <Col xs={24} md={8}>
              <Form.Item
                name="birlikId"
                label="Birlik"
                rules={[{ required: true, message: 'Birlik seçiniz' }]}
              >
                <Select
                  placeholder="Birlik seçin"
                  showSearch
                  optionFilterProp="children"
                >
                  {birliklerData?.data?.map((birlik) => (
                    <Select.Option key={birlik.id} value={birlik.id}>
                      {birlik.birlikAdi}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item
                name="uyelikTipi"
                label="Üyelik Tipi"
                rules={[{ required: true, message: 'Üyelik tipi seçiniz' }]}
              >
                <Select>
                  <Select.Option value={UyelikTipi.GERCEK_KISI}>
                    Gerçek Kişi
                  </Select.Option>
                  <Select.Option value={UyelikTipi.TUZEL_KISI}>
                    Tüzel Kişi
                  </Select.Option>
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="katilimTarihi" label="Katılım Tarihi">
                <DatePicker
                  format="DD.MM.YYYY"
                  style={{ width: '100%' }}
                  placeholder="Katılım tarihi seçin"
                />
              </Form.Item>
            </Col>

            {isEditMode && (
              <Col xs={24} md={8}>
                <Form.Item name="uyeDurum" label="Durum">
                  <Select>
                    <Select.Option value={UyeDurum.AKTIF}>Aktif</Select.Option>
                    <Select.Option value={UyeDurum.PASIF}>Pasif</Select.Option>
                    <Select.Option value={UyeDurum.ASKIYA_ALINMIS}>
                      Askıya Alınmış
                    </Select.Option>
                    <Select.Option value={UyeDurum.IHRAC_EDILMIS}>
                      İhraç Edilmiş
                    </Select.Option>
                  </Select>
                </Form.Item>
              </Col>
            )}
          </Row>

          {/* ==================== Kimlik Bilgileri ==================== */}
          <Divider orientation="left">Kimlik Bilgileri</Divider>

          <Row gutter={24}>
            {uyelikTipiValue === UyelikTipi.GERCEK_KISI ? (
              <Col xs={24} md={8}>
                <Form.Item
                  name="tcKimlikNo"
                  label="TC Kimlik No"
                  rules={[
                    { required: true, message: 'TC Kimlik No gereklidir' },
                    { len: 11, message: 'TC Kimlik No 11 haneli olmalıdır' },
                    { pattern: /^\d+$/, message: 'Sadece rakam giriniz' },
                    { pattern: /^[1-9]/, message: 'TC Kimlik No 0 ile başlayamaz' },
                    {
                      validator: (_, value) => {
                        if (!value || value.length !== 11) return Promise.resolve()
                        if (!validateTcKimlik(value)) {
                          return Promise.reject(new Error('Geçersiz TC Kimlik Numarası'))
                        }
                        return Promise.resolve()
                      },
                    },
                  ]}
                >
                  <Input placeholder="11 haneli TC Kimlik No" maxLength={11} />
                </Form.Item>
              </Col>
            ) : (
              <Col xs={24} md={8}>
                <Form.Item
                  name="vergiNo"
                  label="Vergi No"
                  rules={[
                    { required: true, message: 'Vergi No gereklidir' },
                    { len: 10, message: 'Vergi No 10 haneli olmalıdır' },
                  ]}
                >
                  <Input placeholder="10 haneli Vergi No" maxLength={10} />
                </Form.Item>
              </Col>
            )}

            <Col xs={24} md={8}>
              <Form.Item
                name="ad"
                label={uyelikTipiValue === UyelikTipi.GERCEK_KISI ? 'Ad' : 'Firma Ünvanı'}
                rules={[{ required: true, message: 'Bu alan gereklidir' }]}
              >
                <Input
                  placeholder={
                    uyelikTipiValue === UyelikTipi.GERCEK_KISI ? 'Ad' : 'Firma Ünvanı'
                  }
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item
                name="soyad"
                label="Soyad"
                rules={[
                  {
                    required: uyelikTipiValue === UyelikTipi.GERCEK_KISI,
                    message: 'Soyad gereklidir',
                  },
                ]}
              >
                <Input placeholder="Soyad" />
              </Form.Item>
            </Col>

            {uyelikTipiValue === UyelikTipi.GERCEK_KISI && (
              <>
                <Col xs={24} md={8}>
                  <Form.Item name="babaAdi" label="Baba Adı">
                    <Input placeholder="Baba adı" />
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="anaAdi" label="Ana Adı">
                    <Input placeholder="Ana adı" />
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="cinsiyet" label="Cinsiyet">
                    <Select placeholder="Seçiniz" allowClear>
                      <Select.Option value={Cinsiyet.ERKEK}>Erkek</Select.Option>
                      <Select.Option value={Cinsiyet.KADIN}>Kadın</Select.Option>
                    </Select>
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="dogumTarihi" label="Doğum Tarihi">
                    <DatePicker
                      format="DD.MM.YYYY"
                      style={{ width: '100%' }}
                      placeholder="Doğum tarihi seçin"
                    />
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="dogumYeri" label="Doğum Yeri">
                    <Input placeholder="Doğum yeri" />
                  </Form.Item>
                </Col>
              </>
            )}
          </Row>

          {/* ==================== Kurumsal Üye Bilgileri (Tüzel Kişi) ==================== */}
          {uyelikTipiValue === UyelikTipi.TUZEL_KISI && (
            <>
              <Divider orientation="left">Kurumsal Bilgiler</Divider>
              <Row gutter={24}>
                <Col xs={24} md={8}>
                  <Form.Item name="firmaAdi" label="Firma Adı">
                    <Input placeholder="Firma adı" />
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="vergiDairesi" label="Vergi Dairesi">
                    <Input placeholder="Vergi dairesi" />
                  </Form.Item>
                </Col>

                <Col xs={24} md={8}>
                  <Form.Item name="ticaretSicilNo" label="Ticaret Sicil No">
                    <Input placeholder="Ticaret sicil numarası" />
                  </Form.Item>
                </Col>
              </Row>
            </>
          )}

          {/* ==================== İletişim Bilgileri ==================== */}
          <Divider orientation="left">İletişim Bilgileri</Divider>

          <Row gutter={24}>
            <Col xs={24} md={8}>
              <Form.Item
                name="cepTelefon"
                label="Cep Telefonu"
                rules={[
                  { pattern: /^(05)[0-9]{9}$/, message: 'Geçerli bir cep telefonu giriniz (05XXXXXXXXX)' },
                ]}
              >
                <Input placeholder="05XX XXX XX XX" maxLength={11} />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="sabitTelefon" label="Sabit Telefon">
                <Input placeholder="0XXX XXX XX XX" />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item
                name="email"
                label="E-posta"
                rules={[{ type: 'email', message: 'Geçerli bir e-posta girin' }]}
              >
                <Input placeholder="email@example.com" />
              </Form.Item>
            </Col>
          </Row>

          {/* ==================== Adres Bilgileri ==================== */}
          <Divider orientation="left">Adres Bilgileri</Divider>

          <Row gutter={24}>
            <Col xs={24} md={8}>
              <Form.Item name="ilKodu" label="İl">
                <Select
                  placeholder="İl seçiniz"
                  showSearch
                  optionFilterProp="children"
                  allowClear
                >
                  {iller.map((il) => (
                    <Select.Option key={il.kod} value={il.kod}>
                      {il.ad}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="ilceAdi" label="İlçe">
                <Input placeholder="İlçe adı" />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="mahalleKoy" label="Mahalle / Köy">
                <Input placeholder="Mahalle veya köy adı" />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="postaKodu" label="Posta Kodu">
                <Input placeholder="Posta kodu" maxLength={5} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={24}>
            <Col xs={24}>
              <Form.Item name="adres" label="Açık Adres">
                <Input.TextArea rows={2} placeholder="Açık adres" />
              </Form.Item>
            </Col>
          </Row>

          {/* ==================== İşletme / Üretici Bilgileri ==================== */}
          <Divider orientation="left">İşletme / Üretici Bilgileri</Divider>

          <Row gutter={24}>
            <Col xs={24} md={8}>
              <Form.Item name="isletmeAdi" label="İşletme Adı">
                <Input placeholder="İşletme / Çiftlik adı" />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="isletmeSicilNo" label="İşletme Sicil No">
                <Input placeholder="İşletme sicil numarası" />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="hayvanSayisi" label="Hayvan Sayısı">
                <InputNumber
                  min={0}
                  style={{ width: '100%' }}
                  placeholder="Hayvan sayısı"
                />
              </Form.Item>
            </Col>

            <Col xs={24} md={8}>
              <Form.Item name="uretimKapasitesi" label="Üretim Kapasitesi (kg/yıl)">
                <InputNumber
                  min={0}
                  style={{ width: '100%' }}
                  placeholder="Yıllık üretim kapasitesi"
                />
              </Form.Item>
            </Col>
          </Row>

          {/* ==================== Notlar ==================== */}
          <Divider orientation="left">Notlar</Divider>

          <Row gutter={24}>
            <Col xs={24}>
              <Form.Item name="aciklama" label="Açıklama / Notlar">
                <Input.TextArea rows={3} placeholder="Üye hakkında notlar" />
              </Form.Item>
            </Col>
          </Row>

          {/* ==================== Kaydet Butonu ==================== */}
          <Form.Item className="mb-0 mt-6">
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={createMutation.isLoading || updateMutation.isLoading}
                style={{ background: '#b91c1c' }}
              >
                {isEditMode ? 'Güncelle' : 'Kaydet'}
              </Button>
              <Button onClick={() => navigate('/uyeler')}>İptal</Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default UyeFormPage
