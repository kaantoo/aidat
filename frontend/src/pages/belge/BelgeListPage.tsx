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
  Upload,
  Row,
  Col,
  Popconfirm,
  Tooltip,
  DatePicker,
  App,
} from 'antd'
import {
  UploadOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EyeOutlined,
  FileOutlined,
  FilePdfOutlined,
  FileImageOutlined,
  FileExcelOutlined,
  FileWordOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { UploadFile } from 'antd/es/upload/interface'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { belgeApi } from '@/api/belge'
import { birlikApi } from '@/api/birlik'
import { uyeApi } from '@/api/uye'
import { Belge, BelgeTipi } from '@/types'
import dayjs from 'dayjs'

const BelgeListPage: React.FC = () => {
  const [form] = Form.useForm()
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [filters, setFilters] = useState<{
    birlikId?: number
    uyeId?: number
    belgeTipi?: BelgeTipi
    searchTerm?: string
  }>({})
  const [previewVisible, setPreviewVisible] = useState(false)
  const [previewUrl, setPreviewUrl] = useState('')
  const queryClient = useQueryClient()
  const { message } = App.useApp()

  const { data, isLoading } = useQuery(['belgeler', filters], () =>
    belgeApi.getAll(0, 100)
  )

  const { data: birlikler } = useQuery('birlikler', () => birlikApi.getAll())
  const { data: uyeler } = useQuery(['uyeler', filters.birlikId], () =>
    filters.birlikId ? uyeApi.getByBirlik(filters.birlikId) : null
  )

  const uploadMutation = useMutation(
    (formData: FormData) => belgeApi.upload(formData),
    {
      onSuccess: () => {
        message.success('Belge başarıyla yüklendi')
        queryClient.invalidateQueries('belgeler')
        handleCloseModal()
      },
      onError: () => { message.error('Belge yüklenirken hata oluştu') },
    }
  )

  const deleteMutation = useMutation((id: number) => belgeApi.delete(id), {
    onSuccess: () => {
      message.success('Belge başarıyla silindi')
      queryClient.invalidateQueries('belgeler')
    },
    onError: () => { message.error('Belge silinirken hata oluştu') },
  })

  const handleOpenModal = () => {
    setIsModalOpen(true)
    form.resetFields()
    setFileList([])
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
    form.resetFields()
    setFileList([])
  }

  const handleSubmit = async (values: any) => {
    if (fileList.length === 0) {
      message.error('Lütfen bir dosya seçin')
      return
    }

    const formData = new FormData()
    formData.append('file', fileList[0].originFileObj as File)
    formData.append('belgeTipi', values.belgeTipi)
    if (values.birlikId) formData.append('birlikId', values.birlikId.toString())
    if (values.uyeId) formData.append('uyeId', values.uyeId.toString())
    if (values.aciklama) formData.append('aciklama', values.aciklama)
    if (values.belgeTarihi)
      formData.append('belgeTarihi', values.belgeTarihi.format('YYYY-MM-DD'))

    uploadMutation.mutate(formData)
  }

  const handleDownload = async (belge: Belge) => {
    try {
      const blob = await belgeApi.download(belge.id)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = belge.dosyaAdi
      a.click()
      window.URL.revokeObjectURL(url)
    } catch {
      message.error('Dosya indirilirken hata oluştu')
    }
  }

  const handlePreview = async (belge: Belge) => {
    try {
      const blob = await belgeApi.download(belge.id)
      const url = window.URL.createObjectURL(blob)
      setPreviewUrl(url)
      setPreviewVisible(true)
    } catch {
      message.error('Dosya önizlenemedi')
    }
  }

  const getFileIcon = (mimeType: string) => {
    if (mimeType.includes('pdf')) return <FilePdfOutlined style={{ color: '#ef4444' }} />
    if (mimeType.includes('image')) return <FileImageOutlined style={{ color: '#10b981' }} />
    if (mimeType.includes('excel') || mimeType.includes('spreadsheet'))
      return <FileExcelOutlined style={{ color: '#22c55e' }} />
    if (mimeType.includes('word') || mimeType.includes('document'))
      return <FileWordOutlined style={{ color: '#3b82f6' }} />
    return <FileOutlined />
  }

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return bytes + ' B'
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
  }

  const belgeTipleri = [
    { value: BelgeTipi.KIMLIK_FOTOKOPISI, label: 'Kimlik Fotokopisi' },
    { value: BelgeTipi.IKAMETGAH, label: 'İkametgah' },
    { value: BelgeTipi.VESIKALIK_FOTO, label: 'Vesikalık Fotoğraf' },
    { value: BelgeTipi.UYELIK_FORMU, label: 'Üyelik Formu' },
    { value: BelgeTipi.MAKBUZ, label: 'Makbuz' },
    { value: BelgeTipi.FATURA, label: 'Fatura' },
    { value: BelgeTipi.DIGER, label: 'Diğer' },
  ]

  const columns: ColumnsType<Belge> = [
    {
      title: 'Dosya',
      key: 'dosya',
      width: 250,
      render: (_, record) => (
        <Space>
          {getFileIcon(record.mimeType)}
          <div>
            <div className="font-medium">{record.dosyaAdi}</div>
            <div className="text-xs text-gray-500">{formatFileSize(record.dosyaBoyutu)}</div>
          </div>
        </Space>
      ),
    },
    {
      title: 'Belge No',
      dataIndex: 'belgeNo',
      key: 'belgeNo',
      width: 120,
    },
    {
      title: 'Tip',
      dataIndex: 'belgeTipi',
      key: 'belgeTipi',
      width: 140,
      render: (tip: BelgeTipi) => {
        const found = belgeTipleri.find((t) => t.value === tip)
        return <Tag color="blue">{found?.label || tip}</Tag>
      },
    },
    {
      title: 'İlişkili',
      key: 'iliskili',
      width: 200,
      render: (_, record) => (
        <div className="text-sm">
          {record.birlikAdi && (
            <div>
              <span className="text-gray-500">Birlik:</span> {record.birlikAdi}
            </div>
          )}
          {record.uye && (
            <div>
              <span className="text-gray-500">Üye:</span> {record.uye.ad} {record.uye.soyad}
            </div>
          )}
        </div>
      ),
    },
    {
      title: 'Açıklama',
      dataIndex: 'aciklama',
      key: 'aciklama',
      ellipsis: true,
    },
    {
      title: 'Tarih',
      dataIndex: 'belgeTarihi',
      key: 'belgeTarihi',
      width: 100,
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
    {
      title: 'Yükleyen',
      dataIndex: 'yukleyenKullanici',
      key: 'yukleyenKullanici',
      width: 120,
    },
    {
      title: 'İşlemler',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Space>
          {record.mimeType.includes('image') || record.mimeType.includes('pdf') ? (
            <Tooltip title="Önizle">
              <Button
                type="text"
                size="small"
                icon={<EyeOutlined />}
                onClick={() => handlePreview(record)}
              />
            </Tooltip>
          ) : null}
          <Tooltip title="İndir">
            <Button
              type="text"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            />
          </Tooltip>
          <Popconfirm
            title="Bu belgeyi silmek istediğinize emin misiniz?"
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
        <h1 className="text-xl font-semibold">Belge Yönetimi</h1>
        <Button
          type="primary"
          icon={<UploadOutlined />}
          onClick={handleOpenModal}
          style={{ background: '#b91c1c' }}
        >
          Belge Yükle
        </Button>
      </div>

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
              placeholder="Belge Tipi"
              style={{ width: '100%' }}
              allowClear
              onChange={(value) => setFilters((f) => ({ ...f, belgeTipi: value }))}
            >
              {belgeTipleri.map((t) => (
                <Select.Option key={t.value} value={t.value}>
                  {t.label}
                </Select.Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Input.Search
              placeholder="Belge ara..."
              allowClear
              onSearch={(value) => setFilters((f) => ({ ...f, searchTerm: value }))}
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
            showTotal: (total) => `Toplam ${total} belge`,
          }}
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>

      {/* Yükleme Modal */}
      <Modal
        title="Belge Yükle"
        open={isModalOpen}
        onCancel={handleCloseModal}
        footer={null}
        width={500}
      >
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item
            name="file"
            label="Dosya"
            rules={[{ required: true, message: 'Dosya seçiniz' }]}
          >
            <Upload
              beforeUpload={() => false}
              fileList={fileList}
              onChange={({ fileList }) => setFileList(fileList)}
              maxCount={1}
            >
              <Button icon={<UploadOutlined />}>Dosya Seç</Button>
            </Upload>
          </Form.Item>

          <Form.Item
            name="belgeTipi"
            label="Belge Tipi"
            rules={[{ required: true, message: 'Belge tipi seçiniz' }]}
          >
            <Select placeholder="Belge Tipi Seçin">
              {belgeTipleri.map((t) => (
                <Select.Option key={t.value} value={t.value}>
                  {t.label}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="birlikId" label="Birlik (Opsiyonel)">
            <Select placeholder="Birlik Seçin" allowClear>
              {birlikler?.data?.map((b) => (
                <Select.Option key={b.id} value={b.id}>
                  {b.birlikAdi}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) =>
              prevValues.birlikId !== currentValues.birlikId
            }
          >
            {({ getFieldValue }) =>
              getFieldValue('birlikId') ? (
                <Form.Item name="uyeId" label="Üye (Opsiyonel)">
                  <Select placeholder="Üye Seçin" allowClear>
                    {uyeler?.data?.content?.map((u) => (
                      <Select.Option key={u.id} value={u.id}>
                        {u.uyeNo} - {u.ad} {u.soyad}
                      </Select.Option>
                    ))}
                  </Select>
                </Form.Item>
              ) : null
            }
          </Form.Item>

          <Form.Item name="belgeTarihi" label="Belge Tarihi">
            <DatePicker style={{ width: '100%' }} format="DD.MM.YYYY" />
          </Form.Item>

          <Form.Item name="aciklama" label="Açıklama">
            <Input.TextArea rows={3} placeholder="Belge açıklaması" />
          </Form.Item>

          <Form.Item className="mb-0 text-right">
            <Space>
              <Button onClick={handleCloseModal}>İptal</Button>
              <Button
                type="primary"
                htmlType="submit"
                loading={uploadMutation.isLoading}
                style={{ background: '#b91c1c' }}
              >
                Yükle
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* Önizleme Modal */}
      <Modal
        title="Belge Önizleme"
        open={previewVisible}
        onCancel={() => {
          setPreviewVisible(false)
          window.URL.revokeObjectURL(previewUrl)
        }}
        footer={null}
        width={800}
      >
        <iframe
          src={previewUrl}
          style={{ width: '100%', height: '500px', border: 'none' }}
        />
      </Modal>
    </div>
  )
}

export default BelgeListPage
