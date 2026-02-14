import { useState } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  InputNumber,
  Row,
  Col,
  Statistic,
  Descriptions,
  AutoComplete,
  App,
} from 'antd'
import {
  DollarOutlined,
  PrinterOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { aidatApi, TahsilatCreateDto } from '@/api/aidat'
import { uyeApi } from '@/api/uye'
import { Tahsilat, OdemeTipi, AidatDurum } from '@/types'
import numeral from 'numeral'
import dayjs from 'dayjs'

const TahsilatPage: React.FC = () => {
  const [form] = Form.useForm()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedUye, setSelectedUye] = useState<any>(null)
  const [selectedAidat, setSelectedAidat] = useState<any>(null)
  const queryClient = useQueryClient()
  const { message } = App.useApp()

  const { data: tahsilatlar, isLoading } = useQuery('tahsilatlar', () =>
    aidatApi.getTahsilatlar()
  )

  const { data: uyeler } = useQuery(['uyeler-search', searchTerm], () =>
    searchTerm.length >= 2 ? uyeApi.search(searchTerm) : null,
    { enabled: searchTerm.length >= 2 }
  )

  const { data: uyeAidatlari } = useQuery(
    ['uye-aidatlar', selectedUye?.id],
    () => (selectedUye ? aidatApi.getAidatByUye(selectedUye.id) : null),
    { enabled: !!selectedUye }
  )

  const createMutation = useMutation(
    (data: TahsilatCreateDto) => aidatApi.createTahsilat(data),
    {
      onSuccess: () => {
        message.success('Tahsilat başarıyla kaydedildi')
        queryClient.invalidateQueries('tahsilatlar')
        queryClient.invalidateQueries('aidatlar')
        handleCloseModal()
      },
      onError: () => { message.error('Tahsilat kaydedilirken hata oluştu') },
    }
  )

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const handleOpenModal = () => {
    setIsModalOpen(true)
    form.resetFields()
    setSelectedUye(null)
    setSelectedAidat(null)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    form.resetFields()
    setSelectedUye(null)
    setSelectedAidat(null)
  }

  const handleSubmit = (values: any) => {
    const data: TahsilatCreateDto = {
      aidatId: values.aidatId,
      tutar: values.odemeTutari,
      odemeTarihi: values.odemeTarihi.format('YYYY-MM-DD'),
      odemeTipi: values.odemeTipi,
      dekontNo: values.dekontNo,
      aciklama: values.aciklama,
    }
    createMutation.mutate(data)
  }

  const handleUyeSelect = (_value: string, option: any) => {
    const uye = uyeler?.data?.content.find((u) => u.id === option.key)
    setSelectedUye(uye)
    form.setFieldValue('uyeId', uye?.id)
  }

  const handleAidatSelect = (aidatId: number) => {
    const aidat = uyeAidatlari?.data?.find((a) => a.id === aidatId)
    setSelectedAidat(aidat)
    if (aidat) {
      form.setFieldValue('odemeTutari', aidat.kalanTutar)
    }
  }

  const odemeTipleri = [
    { value: OdemeTipi.NAKIT, label: 'Nakit' },
    { value: OdemeTipi.HAVALE, label: 'Havale' },
    { value: OdemeTipi.EFT, label: 'EFT' },
    { value: OdemeTipi.KREDI_KARTI, label: 'Kredi Kartı' },
    { value: OdemeTipi.CEK, label: 'Çek' },
    { value: OdemeTipi.SENET, label: 'Senet' },
  ]

  const columns: ColumnsType<Tahsilat> = [
    {
      title: 'Makbuz No',
      dataIndex: 'makbuzNo',
      key: 'makbuzNo',
      width: 130,
    },
    {
      title: 'Üye',
      key: 'uye',
      render: (_, record) => (
        <div>
          <div className="font-medium">
            {record.uyeAdSoyad}
          </div>
          <div className="text-xs text-gray-500">{record.uyeNo}</div>
        </div>
      ),
    },
    {
      title: 'Dönem',
      dataIndex: 'donemAdi',
      key: 'donem',
    },
    {
      title: 'Tutar',
      dataIndex: 'tutar',
      key: 'tutar',
      align: 'right',
      render: (value) => (
        <span style={{ color: '#10b981', fontWeight: 'bold' }}>
          {formatCurrency(value)}
        </span>
      ),
    },
    {
      title: 'Ödeme Tarihi',
      dataIndex: 'odemeTarihi',
      key: 'odemeTarihi',
      width: 120,
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
    {
      title: 'Ödeme Tipi',
      dataIndex: 'odemeTipi',
      key: 'odemeTipi',
      render: (tip: OdemeTipi) => {
        const found = odemeTipleri.find((t) => t.value === tip)
        return <Tag color="blue">{found?.label || tip}</Tag>
      },
    },
    {
      title: 'Banka / Dekont',
      key: 'banka',
      render: (_, record) => (
        <div className="text-sm">
          {record.bankaDekontuNo && <div>{record.bankaDekontuNo}</div>}
          {record.dekontNo && (
            <div className="text-gray-500">Dekont: {record.dekontNo}</div>
          )}
        </div>
      ),
    },
    {
      title: 'İşlemi Yapan',
      dataIndex: 'islemYapanKullaniciAdi',
      key: 'islemYapanKullaniciAdi',
      width: 120,
    },
    {
      title: '',
      key: 'actions',
      width: 50,
      render: (_) => (
        <Button type="text" size="small" icon={<PrinterOutlined />} title="Makbuz Yazdır" />
      ),
    },
  ]

  const toplamTahsilat = tahsilatlar?.data.reduce(
    (sum, t) => sum + t.tutar,
    0
  ) || 0

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Tahsilat İşlemleri</h1>
        <Button
          type="primary"
          icon={<DollarOutlined />}
          onClick={handleOpenModal}
          style={{ background: '#10b981' }}
        >
          Yeni Tahsilat
        </Button>
      </div>

      {/* Özet */}
      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Bugünkü Tahsilat"
              value={toplamTahsilat}
              precision={2}
              suffix="₺"
              prefix={<CheckCircleOutlined style={{ color: '#10b981' }} />}
              valueStyle={{ color: '#10b981' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="İşlem Sayısı"
              value={tahsilatlar?.data.length || 0}
            />
          </Card>
        </Col>
      </Row>

      {/* Tablo */}
      <Card variant="borderless" className="shadow-sm">
        <Table
          columns={columns}
          dataSource={tahsilatlar?.data}
          rowKey="id"
          loading={isLoading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} tahsilat`,
          }}
          scroll={{ x: 1100 }}
          size="middle"
        />
      </Card>

      {/* Tahsilat Modal */}
      <Modal
        title="Yeni Tahsilat"
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={null}
        width={700}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          {/* Üye Arama */}
          <Form.Item
            name="uyeArama"
            label="Üye Ara"
            rules={[{ required: !selectedUye, message: 'Üye seçiniz' }]}
          >
            <AutoComplete
              placeholder="Üye no, ad veya TC ile arayın..."
              options={uyeler?.data?.content.map((u) => ({
                key: u.id,
                value: `${u.uyeNo} - ${u.ad} ${u.soyad}`,
                label: (
                  <div>
                    <div className="font-medium">
                      {u.ad} {u.soyad}
                    </div>
                    <div className="text-xs text-gray-500">
                      {u.uyeNo} | {u.birlikAdi || 'Birlik belirtilmemiş'}
                    </div>
                  </div>
                ),
              }))}
              onSearch={setSearchTerm}
              onSelect={handleUyeSelect}
              style={{ width: '100%' }}
            />
          </Form.Item>

          {selectedUye && (
            <>
              <Card size="small" className="mb-4 bg-gray-50">
                <Descriptions size="small" column={2}>
                  <Descriptions.Item label="Üye No">{selectedUye.uyeNo}</Descriptions.Item>
                  <Descriptions.Item label="Ad Soyad">
                    {selectedUye.ad} {selectedUye.soyad}
                  </Descriptions.Item>
                  <Descriptions.Item label="Birlik">
                    {selectedUye.birlikAdi || 'Birlik belirtilmemiş'}
                  </Descriptions.Item>
                  <Descriptions.Item label="Telefon">{selectedUye.telefon}</Descriptions.Item>
                </Descriptions>
              </Card>

              <Form.Item
                name="aidatId"
                label="Aidat Seçin"
                rules={[{ required: true, message: 'Aidat seçiniz' }]}
              >
                <Select
                  placeholder="Ödenecek aidatı seçin"
                  onChange={handleAidatSelect}
                >
                  {(uyeAidatlari?.data || [])
                    .filter((a) => a.durumu !== AidatDurum.ODENDI)
                    .map((a) => (
                      <Select.Option key={a.id} value={a.id}>
                        <div className="flex justify-between">
                          <span>{a.donemAdi}</span>
                          <span className="text-red-500">
                            Kalan: {formatCurrency(a.kalanTutar)}
                          </span>
                        </div>
                      </Select.Option>
                    ))}
                </Select>
              </Form.Item>

              {selectedAidat && (
                <Card size="small" className="mb-4 bg-blue-50">
                  <Row gutter={16}>
                    <Col span={8}>
                      <Statistic
                        title="Aidat Tutarı"
                        value={selectedAidat.tahakkukTutari}
                        precision={2}
                        suffix="₺"
                        valueStyle={{ fontSize: 16 }}
                      />
                    </Col>
                    <Col span={8}>
                      <Statistic
                        title="Ödenen"
                        value={selectedAidat.odenenTutar}
                        precision={2}
                        suffix="₺"
                        valueStyle={{ fontSize: 16, color: '#10b981' }}
                      />
                    </Col>
                    <Col span={8}>
                      <Statistic
                        title="Kalan"
                        value={selectedAidat.kalanTutar}
                        precision={2}
                        suffix="₺"
                        valueStyle={{ fontSize: 16, color: '#ef4444' }}
                      />
                    </Col>
                  </Row>
                </Card>
              )}

              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    name="odemeTutari"
                    label="Ödeme Tutarı"
                    rules={[{ required: true, message: 'Tutar giriniz' }]}
                  >
                    <InputNumber
                      style={{ width: '100%' }}
                      min={0.01}
                      max={selectedAidat?.kalanTutar}
                      precision={2}
                      formatter={(value) =>
                        `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')
                      }
                      suffix="₺"
                    />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item
                    name="odemeTarihi"
                    label="Ödeme Tarihi"
                    rules={[{ required: true, message: 'Tarih seçiniz' }]}
                    initialValue={dayjs()}
                  >
                    <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
                  </Form.Item>
                </Col>
              </Row>

              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item
                    name="odemeTipi"
                    label="Ödeme Tipi"
                    rules={[{ required: true, message: 'Ödeme tipi seçiniz' }]}
                  >
                    <Select placeholder="Ödeme Tipi Seçin">
                      {odemeTipleri.map((t) => (
                        <Select.Option key={t.value} value={t.value}>
                          {t.label}
                        </Select.Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="bankaAdi" label="Banka Adı">
                    <Input placeholder="Banka adı (havale/EFT için)" />
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item name="dekontNo" label="Dekont No">
                <Input placeholder="Dekont/İşlem numarası" />
              </Form.Item>

              <Form.Item name="aciklama" label="Açıklama">
                <Input.TextArea rows={2} placeholder="Ek açıklama" />
              </Form.Item>
            </>
          )}

          <Form.Item className="mb-0 text-right">
            <Space>
              <Button onClick={handleCloseModal}>İptal</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createMutation.isLoading}
                disabled={!selectedUye || !selectedAidat}
                style={{ background: '#10b981' }}
              >
                Tahsilatı Kaydet
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default TahsilatPage
