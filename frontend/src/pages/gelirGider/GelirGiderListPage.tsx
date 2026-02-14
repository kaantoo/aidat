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
  App,
  Popconfirm,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  ExportOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { gelirGiderApi, GelirGiderCreateDto } from '@/api/gelirGider'
import { birlikApi } from '@/api/birlik'
import {
  GelirGider,
  GelirGiderTipi,
  GelirKategorisi,
  GiderKategorisi,
} from '@/types'
import numeral from 'numeral'
import dayjs from 'dayjs'

const GelirGiderListPage: React.FC = () => {
  const [form] = Form.useForm()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingRecord, setEditingRecord] = useState<GelirGider | null>(null)
  const [selectedTip, setSelectedTip] = useState<GelirGiderTipi | null>(null)
  const [filters, setFilters] = useState<{
    birlikId?: number
    tip?: GelirGiderTipi
    baslangicTarihi?: string
    bitisTarihi?: string
  }>({})
  const queryClient = useQueryClient()
  const { message } = App.useApp()

  const { data, isLoading } = useQuery(['gelir-gider', filters], () =>
    gelirGiderApi.getAll(0, 100, filters)
  )

  const { data: birlikler } = useQuery('birlikler', () => birlikApi.getAll())

  const { data: ozet } = useQuery(['gelir-gider-ozet', filters], () =>
    gelirGiderApi.getOzet(filters.birlikId, filters.baslangicTarihi, filters.bitisTarihi)
  )

  const createMutation = useMutation(
    (data: GelirGiderCreateDto) => gelirGiderApi.create(data),
    {
      onSuccess: () => {
        message.success('İşlem başarıyla kaydedildi')
        queryClient.invalidateQueries('gelir-gider')
        queryClient.invalidateQueries('gelir-gider-ozet')
        handleCloseModal()
      },
      onError: () => { message.error('İşlem kaydedilirken hata oluştu') },
    }
  )

  const updateMutation = useMutation(
    ({ id, data }: { id: number; data: Partial<GelirGiderCreateDto> }) =>
      gelirGiderApi.update(id, data),
    {
      onSuccess: () => {
        message.success('İşlem başarıyla güncellendi')
        queryClient.invalidateQueries('gelir-gider')
        queryClient.invalidateQueries('gelir-gider-ozet')
        handleCloseModal()
      },
      onError: () => { message.error('İşlem güncellenirken hata oluştu') },
    }
  )

  const deleteMutation = useMutation((id: number) => gelirGiderApi.delete(id), {
    onSuccess: () => {
      message.success('İşlem başarıyla silindi')
      queryClient.invalidateQueries('gelir-gider')
      queryClient.invalidateQueries('gelir-gider-ozet')
    },
    onError: () => { message.error('İşlem silinirken hata oluştu') },
  })

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const handleOpenModal = (record?: GelirGider) => {
    if (record) {
      setEditingRecord(record)
      setSelectedTip(record.tip)
      form.setFieldsValue({
        ...record,
        birlikId: record.birlikId,
        islemTarihi: dayjs(record.islemTarihi),
      })
    } else {
      setEditingRecord(null)
      setSelectedTip(null)
      form.resetFields()
    }
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setEditingRecord(null)
    setSelectedTip(null)
    form.resetFields()
  }

  const handleSubmit = async (values: any) => {
    const data: GelirGiderCreateDto = {
      ...values,
      islemTarihi: values.islemTarihi.format('YYYY-MM-DD'),
    }

    if (editingRecord) {
      updateMutation.mutate({ id: editingRecord.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const handleExportExcel = async () => {
    try {
      const blob = await gelirGiderApi.exportExcel(filters)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `gelir-gider-${dayjs().format('YYYY-MM-DD')}.xlsx`
      a.click()
      window.URL.revokeObjectURL(url)
    } catch {
      message.error('Excel dışa aktarma başarısız')
    }
  }

  const gelirKategorileri = [
    { value: GelirKategorisi.AIDAT_GELIRI, label: 'Aidat Geliri' },
    { value: GelirKategorisi.BAGIS, label: 'Bağış' },
    { value: GelirKategorisi.FAIZ_GELIRI, label: 'Faiz Geliri' },
    { value: GelirKategorisi.KIRA_GELIRI, label: 'Kira Geliri' },
    { value: GelirKategorisi.DIGER_GELIR, label: 'Diğer Gelir' },
  ]

  const giderKategorileri = [
    { value: GiderKategorisi.PERSONEL, label: 'Personel' },
    { value: GiderKategorisi.KIRA, label: 'Kira' },
    { value: GiderKategorisi.FATURA, label: 'Fatura' },
    { value: GiderKategorisi.MALZEME, label: 'Malzeme' },
    { value: GiderKategorisi.ULASIM, label: 'Ulaşım' },
    { value: GiderKategorisi.TEMSIL_AGIRLAMAM, label: 'Temsil & Ağırlama' },
    { value: GiderKategorisi.BAKIM_ONARIM, label: 'Bakım & Onarım' },
    { value: GiderKategorisi.DIGER_GIDER, label: 'Diğer Gider' },
  ]

  const columns: ColumnsType<GelirGider> = [
    {
      title: 'Tarih',
      dataIndex: 'islemTarihi',
      key: 'islemTarihi',
      width: 110,
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
      sorter: (a, b) => dayjs(a.islemTarihi).unix() - dayjs(b.islemTarihi).unix(),
    },
    {
      title: 'Tip',
      dataIndex: 'tip',
      key: 'tip',
      width: 100,
      render: (tip: GelirGiderTipi) => (
        <Tag color={tip === GelirGiderTipi.GELIR ? 'green' : 'red'}>
          {tip === GelirGiderTipi.GELIR ? (
            <><ArrowUpOutlined /> Gelir</>
          ) : (
            <><ArrowDownOutlined /> Gider</>
          )}
        </Tag>
      ),
    },
    {
      title: 'Birlik',
      dataIndex: ['birlik', 'birlikAdi'],
      key: 'birlik',
      ellipsis: true,
    },
    {
      title: 'Kategori',
      key: 'kategori',
      render: (_, record) => {
        if (record.tip === GelirGiderTipi.GELIR) {
          const kat = gelirKategorileri.find((k) => k.value === record.gelirKategorisi)
          return kat?.label || '-'
        } else {
          const kat = giderKategorileri.find((k) => k.value === record.giderKategorisi)
          return kat?.label || '-'
        }
      },
    },
    {
      title: 'Açıklama',
      dataIndex: 'aciklama',
      key: 'aciklama',
      ellipsis: true,
    },
    {
      title: 'Belge No',
      dataIndex: 'belgeNo',
      key: 'belgeNo',
      width: 120,
    },
    {
      title: 'Tutar',
      dataIndex: 'tutar',
      key: 'tutar',
      width: 130,
      align: 'right',
      render: (value, record) => (
        <span style={{ color: record.tip === GelirGiderTipi.GELIR ? '#10b981' : '#ef4444' }}>
          {record.tip === GelirGiderTipi.GELIR ? '+' : '-'} {formatCurrency(value)}
        </span>
      ),
      sorter: (a, b) => a.tutar - b.tutar,
    },
    {
      title: 'İşlemler',
      key: 'actions',
      width: 100,
      render: (_, record) => (
        <Space>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleOpenModal(record)}
          />
          <Popconfirm
            title="Bu kaydı silmek istediğinize emin misiniz?"
            onConfirm={() => deleteMutation.mutate(record.id)}
            okText="Evet"
            cancelText="Hayır"
          >
            <Button type="text" size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ]

  const ozetData = ozet?.data

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Gelir / Gider Yönetimi</h1>
        <Space>
          <Button icon={<ExportOutlined />} onClick={handleExportExcel}>
            Excel İndir
          </Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => handleOpenModal()}
            style={{ background: '#b91c1c' }}
          >
            Yeni Kayıt
          </Button>
        </Space>
      </div>

      {/* Özet Kartları */}
      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Toplam Gelir"
              value={ozetData?.toplamGelir || 0}
              precision={2}
              suffix="₺"
              valueStyle={{ color: '#10b981' }}
              prefix={<ArrowUpOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Toplam Gider"
              value={ozetData?.toplamGider || 0}
              precision={2}
              suffix="₺"
              valueStyle={{ color: '#ef4444' }}
              prefix={<ArrowDownOutlined />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Net Durum"
              value={ozetData?.netDurum || 0}
              precision={2}
              suffix="₺"
              valueStyle={{ color: (ozetData?.netDurum || 0) >= 0 ? '#10b981' : '#ef4444' }}
            />
          </Card>
        </Col>
      </Row>

      {/* Filtreler */}
      <Card variant="borderless" className="shadow-sm mb-4">
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Birlik Seçin"
              style={{ width: '100%' }}
              allowClear
              onChange={(value) => setFilters((f) => ({ ...f, birlikId: value }))}
            >
              {birlikler?.data?.map((b) => (
                <Select.Option key={b.id} value={b.id}>
                  {b.birlikAdi}
                </Select.Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select
              placeholder="Tip Seçin"
              style={{ width: '100%' }}
              allowClear
              onChange={(value) => setFilters((f) => ({ ...f, tip: value }))}
            >
              <Select.Option value={GelirGiderTipi.GELIR}>Gelir</Select.Option>
              <Select.Option value={GelirGiderTipi.GIDER}>Gider</Select.Option>
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <DatePicker.RangePicker
              style={{ width: '100%' }}
              onChange={(dates) => {
                if (dates) {
                  setFilters((f) => ({
                    ...f,
                    baslangicTarihi: dates[0]?.format('YYYY-MM-DD'),
                    bitisTarihi: dates[1]?.format('YYYY-MM-DD'),
                  }))
                } else {
                  setFilters((f) => ({
                    ...f,
                    baslangicTarihi: undefined,
                    bitisTarihi: undefined,
                  }))
                }
              }}
            />
          </Col>
        </Row>
      </Card>

      {/* Tablo */}
      <Card variant="borderless" className="shadow-sm">
        <Table
          columns={columns}
          dataSource={data?.data?.content}
          rowKey="id"
          loading={isLoading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} kayıt`,
          }}
          scroll={{ x: 1000 }}
          size="middle"
        />
      </Card>

      {/* Ekleme/Düzenleme Modal */}
      <Modal
        title={editingRecord ? 'Kayıt Düzenle' : 'Yeni Kayıt Ekle'}
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={null}
        width={600}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={16}>
            <Col span={12}>
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
            </Col>
            <Col span={12}>
              <Form.Item
                name="tip"
                label="Tip"
                rules={[{ required: true, message: 'Tip seçiniz' }]}
              >
                <Select
                  placeholder="Tip Seçin"
                  onChange={(value) => setSelectedTip(value)}
                >
                  <Select.Option value={GelirGiderTipi.GELIR}>Gelir</Select.Option>
                  <Select.Option value={GelirGiderTipi.GIDER}>Gider</Select.Option>
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              {selectedTip === GelirGiderTipi.GELIR && (
                <Form.Item
                  name="gelirKategorisi"
                  label="Gelir Kategorisi"
                  rules={[{ required: true, message: 'Kategori seçiniz' }]}
                >
                  <Select placeholder="Kategori Seçin">
                    {gelirKategorileri.map((k) => (
                      <Select.Option key={k.value} value={k.value}>
                        {k.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              )}
              {selectedTip === GelirGiderTipi.GIDER && (
                <Form.Item
                  name="giderKategorisi"
                  label="Gider Kategorisi"
                  rules={[{ required: true, message: 'Kategori seçiniz' }]}
                >
                  <Select placeholder="Kategori Seçin">
                    {giderKategorileri.map((k) => (
                      <Select.Option key={k.value} value={k.value}>
                        {k.label}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              )}
            </Col>
            <Col span={12}>
              <Form.Item
                name="tutar"
                label="Tutar"
                rules={[{ required: true, message: 'Tutar giriniz' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  precision={2}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  suffix="₺"
                />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="islemTarihi"
                label="İşlem Tarihi"
                rules={[{ required: true, message: 'Tarih seçiniz' }]}
              >
                <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="belgeNo" label="Belge No">
                <Input placeholder="Fatura/Makbuz numarası" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item name="karsiTaraf" label="Karşı Taraf">
            <Input placeholder="Ödemenin yapıldığı/alındığı kişi/kurum" />
          </Form.Item>

          <Form.Item
            name="aciklama"
            label="Açıklama"
            rules={[{ required: true, message: 'Açıklama giriniz' }]}
          >
            <Input.TextArea rows={3} placeholder="İşlem açıklaması" />
          </Form.Item>

          <Form.Item className="mb-0 text-right">
            <Space>
              <Button onClick={handleCloseModal}>İptal</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={createMutation.isLoading || updateMutation.isLoading}
                style={{ background: '#b91c1c' }}
              >
                {editingRecord ? 'Güncelle' : 'Kaydet'}
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default GelirGiderListPage
