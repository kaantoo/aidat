import { useState, useMemo } from 'react'
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
  Popconfirm,
  App,
  Alert,
} from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined, SendOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { aidatApi, AidatDonemCreateDto } from '@/api/aidat'
import { birlikApi } from '@/api/birlik'
import { AidatDonemi, DonemTipi, BirlikTipi, KullaniciRol } from '@/types'
import { useAuthStore } from '@/store/authStore'
import numeral from 'numeral'
import dayjs from 'dayjs'

const AidatDonemPage: React.FC = () => {
  const [form] = Form.useForm()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingRecord, setEditingRecord] = useState<AidatDonemi | null>(null)
  const [selectedBirlikId, setSelectedBirlikId] = useState<number | null>(null)
  const [topluTahakkukModal, setTopluTahakkukModal] = useState<{
    visible: boolean
    donemId?: number
    donemAdi?: string
  }>({ visible: false })
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  
  // Kullanıcı bilgilerini al
  const { user } = useAuthStore()
  const isSistemAdmin = user?.rol === KullaniciRol.SISTEM_ADMIN
  // Admin birlik seçebilir, diğerleri kendi birliğine sabitlenir
  const canSelectBirlik = isSistemAdmin

  const { data, isLoading } = useQuery('aidat-donemleri', () => aidatApi.getDonemleri())
  const { data: birlikler } = useQuery('birlikler', () => birlikApi.getAll())

  // Seçili birliğin merkez birlik olup olmadığını kontrol et
  const selectedBirlik = useMemo(() => {
    if (!selectedBirlikId || !birlikler?.data) return null
    return birlikler.data.find(b => b.id === selectedBirlikId)
  }, [selectedBirlikId, birlikler?.data])

  const isMerkezBirlik = selectedBirlik?.birlikTipi === BirlikTipi.MERKEZ

  const createMutation = useMutation(
    (data: AidatDonemCreateDto) => aidatApi.createDonem(data),
    {
      onSuccess: () => {
        message.success('Dönem başarıyla oluşturuldu')
        queryClient.invalidateQueries('aidat-donemleri')
        handleCloseModal()
      },
      onError: () => { message.error('Dönem oluşturulurken hata oluştu') },
    }
  )

  const updateMutation = useMutation(
    ({ id, data }: { id: number; data: Partial<AidatDonemCreateDto> }) =>
      aidatApi.updateDonem(id, data),
    {
      onSuccess: () => {
        message.success('Dönem başarıyla güncellendi')
        queryClient.invalidateQueries('aidat-donemleri')
        handleCloseModal()
      },
      onError: () => { message.error('Dönem güncellenirken hata oluştu') },
    }
  )

  const deleteMutation = useMutation((id: number) => aidatApi.deleteDonem(id), {
    onSuccess: () => {
      message.success('Dönem silindi')
      queryClient.invalidateQueries('aidat-donemleri')
    },
    onError: () => { message.error('Dönem silinemedi') },
  })

  const topluTahakkukMutation = useMutation(
    (donemId: number) => aidatApi.topluTahakkuk(donemId),
    {
      onSuccess: (result) => {
        message.success(`Toplu tahakkuk tamamlandı. ${result.data || 0} üyeye aidat oluşturuldu.`)
        queryClient.invalidateQueries('aidatlar')
        setTopluTahakkukModal({ visible: false })
      },
      onError: () => { message.error('Toplu tahakkuk işlemi başarısız') },
    }
  )

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const handleOpenModal = (record?: AidatDonemi) => {
    if (record) {
      // Düzenleme modu
      setEditingRecord(record)
      setSelectedBirlikId(record.birlikId || null)
      form.setFieldsValue({
        ...record,
        birlikId: record.birlikId,
        baslangicTarihi: dayjs(record.baslangicTarihi),
        bitisTarihi: dayjs(record.bitisTarihi),
        sonOdemeTarihi: dayjs(record.sonOdemeTarihi),
      })
    } else {
      // Yeni dönem ekleme - kullanıcının birliğini otomatik seç
      setEditingRecord(null)
      form.resetFields()
      
      // Kullanıcı sistem admin değilse kendi birliğini otomatik seç
      if (!canSelectBirlik && user?.birlikId) {
        setSelectedBirlikId(user.birlikId)
        form.setFieldsValue({ birlikId: user.birlikId })
      } else {
        setSelectedBirlikId(null)
      }
    }
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    setEditingRecord(null)
    setSelectedBirlikId(null)
    form.resetFields()
  }

  const handleBirlikChange = (value: number) => {
    setSelectedBirlikId(value)
    // Birlik değişince tutar/pay oranı alanlarını temizle
    form.setFieldsValue({ tutar: undefined, merkezPayOrani: undefined })
  }

  const handleSubmit = (values: any) => {
    const yil = values.baslangicTarihi.year()
    const ay = values.donemTipi === DonemTipi.AYLIK ? values.baslangicTarihi.month() + 1 : undefined
    
    // Dönem kodu otomatik oluştur: YIL-TIP-AY (örn: 2026-AYLIK-01)
    const donemKodu = ay 
      ? `${yil}-${values.donemTipi}-${String(ay).padStart(2, '0')}`
      : `${yil}-${values.donemTipi}`
    
    const data: AidatDonemCreateDto = {
      donemKodu,
      donemAdi: values.donemAdi,
      donemTipi: values.donemTipi,
      yil,
      ay,
      baslangicTarihi: values.baslangicTarihi.format('YYYY-MM-DD'),
      bitisTarihi: values.bitisTarihi.format('YYYY-MM-DD'),
      sonOdemeTarihi: values.sonOdemeTarihi.format('YYYY-MM-DD'),
      // Merkez birlik için pay oranı, alt birlik için tutar
      tutar: isMerkezBirlik ? undefined : values.tutar,
      merkezPayOrani: isMerkezBirlik ? values.merkezPayOrani : undefined,
      gecikmeFaiziOrani: values.gecikmeFaiziOrani,
      birlikId: values.birlikId,
      aciklama: values.aciklama,
    }

    if (editingRecord) {
      updateMutation.mutate({ id: editingRecord.id, data })
    } else {
      createMutation.mutate(data)
    }
  }

  const donemTipleri = [
    { value: DonemTipi.AYLIK, label: 'Aylık' },
    { value: DonemTipi.UC_AYLIK, label: '3 Aylık' },
    { value: DonemTipi.ALTI_AYLIK, label: '6 Aylık' },
    { value: DonemTipi.YILLIK, label: 'Yıllık' },
  ]

  const columns: ColumnsType<AidatDonemi> = [
    {
      title: 'Dönem Kodu',
      dataIndex: 'donemKodu',
      key: 'donemKodu',
      width: 120,
    },
    {
      title: 'Dönem Adı',
      dataIndex: 'donemAdi',
      key: 'donemAdi',
    },
    {
      title: 'Yıl',
      dataIndex: 'yil',
      key: 'yil',
      width: 80,
    },
    {
      title: 'Birlik',
      dataIndex: 'birlikAdi',
      key: 'birlik',
      render: (birlikAdi) => birlikAdi || 'Merkez',
    },
    {
      title: 'Tip',
      dataIndex: 'donemTipi',
      key: 'donemTipi',
      render: (tip: DonemTipi) => {
        const found = donemTipleri.find((t) => t.value === tip)
        return <Tag color="blue">{found?.label || tip}</Tag>
      },
    },
    {
      title: 'Başlangıç',
      dataIndex: 'baslangicTarihi',
      key: 'baslangicTarihi',
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
    {
      title: 'Bitiş',
      dataIndex: 'bitisTarihi',
      key: 'bitisTarihi',
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
    {
      title: 'Son Ödeme',
      dataIndex: 'sonOdemeTarihi',
      key: 'sonOdemeTarihi',
      render: (date) => {
        const isOverdue = dayjs(date).isBefore(dayjs())
        return (
          <span style={{ color: isOverdue ? '#ef4444' : 'inherit' }}>
            {dayjs(date).format('DD.MM.YYYY')}
          </span>
        )
      },
    },
    {
      title: 'Aidat Tutarı / Pay Oranı',
      key: 'tutarVeyaPayOrani',
      align: 'right',
      render: (_, record) => {
        if (record.merkezPayOrani && record.merkezPayOrani > 0) {
          return <Tag color="purple">%{record.merkezPayOrani}</Tag>
        }
        return formatCurrency(record.tutar || 0)
      },
    },
    {
      title: 'Gecikme %',
      dataIndex: 'gecikmeFaiziOrani',
      key: 'gecikmeFaiziOrani',
      align: 'center',
      render: (value) => value ? `%${value}` : '-',
    },
    {
      title: 'Durum',
      dataIndex: 'aktif',
      key: 'aktif',
      render: (aktif) => (
        <Tag color={aktif ? 'green' : 'default'}>{aktif ? 'Aktif' : 'Pasif'}</Tag>
      ),
    },
    {
      title: 'İşlemler',
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button
            type="primary"
            size="small"
            icon={<SendOutlined />}
            onClick={() =>
              setTopluTahakkukModal({
                visible: true,
                donemId: record.id,
                donemAdi: record.donemAdi,
              })
            }
            style={{ background: '#10b981' }}
          >
            Tahakkuk
          </Button>
          <Button
            type="text"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleOpenModal(record)}
          />
          <Popconfirm
            title="Bu dönemi silmek istediğinize emin misiniz?"
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

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Aidat Dönem Tanımları</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => handleOpenModal()}
          style={{ background: '#b91c1c' }}
        >
          Yeni Dönem
        </Button>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Table
          columns={columns}
          dataSource={data?.data}
          rowKey="id"
          loading={isLoading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} dönem`,
          }}
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>

      {/* Dönem Ekleme/Düzenleme Modal */}
      <Modal
        title={editingRecord ? 'Dönem Düzenle' : 'Yeni Dönem Tanımla'}
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={null}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{ gecikmeFaiziOrani: 2 }}
        >
          {isMerkezBirlik && (
            <Alert
              message="Merkez Birlik Dönemi"
              description="Merkez birlik dönemleri için tutar yerine pay oranı tanımlanır. Bu oran, alt birliklerin tahakkuklarından merkeze aktarılacak yüzdeyi belirler."
              type="info"
              showIcon
              className="mb-4"
            />
          )}
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="birlikId"
                label="Birlik"
                rules={[{ required: true, message: 'Birlik seçiniz' }]}
              >
                <Select 
                  placeholder="Birlik Seçin" 
                  onChange={handleBirlikChange}
                  disabled={!canSelectBirlik} // Sadece sistem admin değiştirebilir
                >
                  {birlikler?.data?.map((b) => (
                    <Select.Option key={b.id} value={b.id}>
                      {b.birlikAdi} {b.birlikTipi === BirlikTipi.MERKEZ ? '(Merkez)' : ''}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="donemTipi"
                label="Dönem Tipi"
                rules={[{ required: true, message: 'Dönem tipi seçiniz' }]}
              >
                <Select placeholder="Dönem Tipi Seçin">
                  {donemTipleri.map((t) => (
                    <Select.Option key={t.value} value={t.value}>
                      {t.label}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="donemAdi"
            label="Dönem Adı"
            rules={[{ required: true, message: 'Dönem adı giriniz' }]}
          >
            <Input placeholder="Örn: 2025 Ocak Aidatı" />
          </Form.Item>

          <Row gutter={16}>
            <Col span={8}>
              <Form.Item
                name="baslangicTarihi"
                label="Başlangıç Tarihi"
                rules={[{ required: true, message: 'Başlangıç tarihi seçiniz' }]}
              >
                <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="bitisTarihi"
                label="Bitiş Tarihi"
                rules={[{ required: true, message: 'Bitiş tarihi seçiniz' }]}
              >
                <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item
                name="sonOdemeTarihi"
                label="Son Ödeme Tarihi"
                rules={[{ required: true, message: 'Son ödeme tarihi seçiniz' }]}
              >
                <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={8}>
              {isMerkezBirlik ? (
                <Form.Item
                  name="merkezPayOrani"
                  label="Merkez Pay Oranı (%)"
                  rules={[{ required: true, message: 'Pay oranı giriniz' }]}
                  tooltip="Alt birlik tahakkuklarından merkeze aktarılacak yüzde"
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    min={0}
                    max={100}
                    precision={2}
                    placeholder="Örn: 10"
                    addonAfter="%"
                  />
                </Form.Item>
              ) : (
                <Form.Item
                  name="tutar"
                  label="Aidat Tutarı"
                  rules={[{ required: true, message: 'Aidat tutarı giriniz' }]}
                >
                  <InputNumber
                    style={{ width: '100%' }}
                    min={0}
                    precision={2}
                    formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                    suffix="₺"
                  />
                </Form.Item>
              )}
            </Col>
            <Col span={8}>
              <Form.Item
                name="gecikmeFaiziOrani"
                label="Gecikme Oranı (%)"
              >
                <InputNumber style={{ width: '100%' }} min={0} max={100} suffix="%" />
              </Form.Item>
            </Col>
            <Col span={8}>
              <Form.Item name="aciklama" label="Açıklama">
                <Input placeholder="Dönem açıklaması" />
              </Form.Item>
            </Col>
          </Row>

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

      {/* Toplu Tahakkuk Modal */}
      <Modal
        title="Toplu Tahakkuk"
        open={topluTahakkukModal.visible}
        onCancel={() => setTopluTahakkukModal({ visible: false })}
        onOk={() => {
          if (topluTahakkukModal.donemId) {
            topluTahakkukMutation.mutate(topluTahakkukModal.donemId)
          }
        }}
        okText="Tahakkuk Oluştur"
        okButtonProps={{
          loading: topluTahakkukMutation.isLoading,
          style: { background: '#10b981' },
        }}
        cancelText="İptal"
      >
        <p>
          <strong>"{topluTahakkukModal.donemAdi}"</strong> dönemi için tüm aktif üyelere
          aidat tahakkuku oluşturulacaktır.
        </p>
        <p className="text-gray-500 mt-2">
          Bu işlem geri alınamaz. Devam etmek istediğinize emin misiniz?
        </p>
      </Modal>
    </div>
  )
}

export default AidatDonemPage
