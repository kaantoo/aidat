import { useState, useMemo } from 'react'
import { Card, Table, Tag, Button, Space, Select, Row, Col, Input, Modal, Alert, Typography, Divider, Upload, message } from 'antd'
import { ExportOutlined, DollarOutlined, SearchOutlined, FileExcelOutlined, DownloadOutlined, UploadOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { aidatApi } from '@/api/aidat'
import { birlikApi } from '@/api/birlik'
import { Aidat, AidatDurum, AidatDonemi, BirlikTipi, KullaniciRol } from '@/types'
import { useAuthStore } from '@/store/authStore'
import numeral from 'numeral'

const AidatListPage: React.FC = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [searchText, setSearchText] = useState('')
  const [selectedDonemId, setSelectedDonemId] = useState<number | undefined>()
  const [selectedDurum, setSelectedDurum] = useState<AidatDurum | undefined>()
  const [importModalOpen, setImportModalOpen] = useState(false)
  const [importBirlikId, setImportBirlikId] = useState<number | undefined>(undefined)
  const [importResult, setImportResult] = useState<{
    basarili: number
    atlanan: number
    hatali: number
    toplam: number
    hatalar: string[]
    uyarilar: string[]
  } | null>(null)

  const canSelectBirlik = user?.rol === KullaniciRol.SISTEM_ADMIN || user?.rol === KullaniciRol.MERKEZ_YONETICI

  // Tüm aidatları çek, frontend'de filtrele
  const { data, isLoading } = useQuery(
    ['aidatlar'],
    () => aidatApi.getAidatlar(0, 1000)
  )

  const { data: donemlerData } = useQuery(
    ['donemler', user?.birlikId],
    () => aidatApi.getDonemleri(user?.birlikId)
  )

  // Birlik listesi (import modal için)
  const { data: birlikler } = useQuery(
    ['birlikler'],
    () => birlikApi.getAll(),
    { enabled: canSelectBirlik }
  )

  // Import mutation
  const importMutation = useMutation(
    ({ file, birlikId }: { file: File; birlikId?: number }) =>
      aidatApi.importAidatlarFromExcel(file, birlikId),
    {
      onSuccess: (result) => {
        const data = result.data
        setImportResult(data)
        if (data.basarili > 0) {
          message.success(`${data.basarili} aidat borcu başarıyla import edildi`)
          queryClient.invalidateQueries('aidatlar')
        }
        if (data.hatali > 0) {
          message.warning(`${data.hatali} satırda hata oluştu`)
        }
      },
      onError: () => {
        message.error('Excel dosyası import edilirken hata oluştu')
      },
    }
  )

  const handleDownloadAidatTemplate = async () => {
    try {
      const blob = await aidatApi.downloadAidatImportTemplate()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'aidat_borc_import_sablonu.xlsx'
      a.click()
      window.URL.revokeObjectURL(url)
      message.success('Şablon indirildi')
    } catch {
      message.error('Şablon indirilemedi')
    }
  }

  const handleImportUpload = (file: File) => {
    importMutation.mutate({ file, birlikId: importBirlikId })
    return false
  }

  const handleCloseImportModal = () => {
    setImportModalOpen(false)
    setImportResult(null)
    setImportBirlikId(undefined)
  }

  const filteredData = useMemo(() => {
    let aidatlar = data?.data?.content || data?.data || []
    if (!Array.isArray(aidatlar)) aidatlar = []
    
    // Dönem filtresi
    if (selectedDonemId) {
      aidatlar = aidatlar.filter((a: Aidat) => a.aidatDonemiId === selectedDonemId)
    }
    
    // Durum filtresi
    if (selectedDurum) {
      aidatlar = aidatlar.filter((a: Aidat) => a.durumu === selectedDurum)
    }
    
    // Arama filtresi
    if (searchText) {
      const searchLower = searchText.toLowerCase()
      aidatlar = aidatlar.filter((aidat: Aidat) => 
        aidat.uyeNo?.toLowerCase().includes(searchLower) ||
        aidat.uyeAdSoyad?.toLowerCase().includes(searchLower) ||
        aidat.donemAdi?.toLowerCase().includes(searchLower)
      )
    }
    
    return aidatlar
  }, [data, searchText, selectedDonemId, selectedDurum])

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const getDurumTag = (durum: AidatDurum) => {
    const config: Record<AidatDurum, { color: string; text: string }> = {
      [AidatDurum.BEKLIYOR]: { color: 'red', text: 'Ödeme Bekleniyor' },
      [AidatDurum.KISMI_ODENDI]: { color: 'orange', text: 'Kısmi Ödendi' },
      [AidatDurum.ODENDI]: { color: 'green', text: 'Ödendi' },
      [AidatDurum.GECIKTI]: { color: 'volcano', text: 'Gecikti' },
      [AidatDurum.IPTAL]: { color: 'default', text: 'İptal' },
    }
    const { color, text } = config[durum] || { color: 'default', text: durum }
    return <Tag color={color}>{text}</Tag>
  }

  const columns: ColumnsType<Aidat> = [
    {
      title: 'Üye No',
      dataIndex: 'uyeNo',
      key: 'uyeNo',
      width: 120,
    },
    {
      title: 'Üye Adı',
      dataIndex: 'uyeAdSoyad',
      key: 'uyeAdi',
    },
    {
      title: 'Dönem',
      dataIndex: 'donemAdi',
      key: 'donem',
    },
    {
      title: 'Tutar',
      dataIndex: 'tahakkukTutari',
      key: 'tahakkukTutari',
      align: 'right',
      render: (value) => formatCurrency(value),
    },
    {
      title: 'Gecikme',
      dataIndex: 'gecikmeTutari',
      key: 'gecikmeTutari',
      align: 'right',
      render: (value) => (value > 0 ? formatCurrency(value) : '-'),
    },
    {
      title: 'Toplam',
      dataIndex: 'toplamBorc',
      key: 'toplamBorc',
      align: 'right',
      render: (value) => <strong>{formatCurrency(value)}</strong>,
    },
    {
      title: 'Ödenen',
      dataIndex: 'odenenTutar',
      key: 'odenenTutar',
      align: 'right',
      render: (value) => formatCurrency(value),
    },
    {
      title: 'Kalan',
      dataIndex: 'kalanTutar',
      key: 'kalanTutar',
      align: 'right',
      render: (value) => (value > 0 ? formatCurrency(value) : '-'),
    },
    {
      title: 'Durum',
      dataIndex: 'durumu',
      key: 'durumu',
      render: (durum: AidatDurum) => getDurumTag(durum),
    },
    {
      title: 'İşlemler',
      key: 'actions',
      render: (_, record) => (
        <Space>
          <Button
            type="primary"
            size="small"
            icon={<DollarOutlined />}
            disabled={record.durumu === AidatDurum.ODENDI}
            style={{ background: '#10b981' }}
          >
            Tahsilat
          </Button>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Aidat Listesi</h1>
        <Space>
          <Button 
            icon={<FileExcelOutlined />} 
            onClick={() => setImportModalOpen(true)}
            style={{ borderColor: '#059669', color: '#059669' }}
          >
            Excel İle Borç Aktar
          </Button>
          <Button icon={<ExportOutlined />}>Excel İndir</Button>
          <Button type="primary" style={{ background: '#b91c1c' }}>
            Toplu Tahakkuk
          </Button>
        </Space>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Row gutter={[16, 16]} className="mb-4">
          <Col xs={24} sm={12} md={6}>
            <Input
              placeholder="Üye No veya Ad Ara..."
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select 
              placeholder="Dönem Seçin" 
              style={{ width: '100%' }} 
              allowClear
              value={selectedDonemId}
              onChange={(value) => setSelectedDonemId(value)}
            >
              {(donemlerData?.data || []).map((donem: AidatDonemi) => (
                <Select.Option key={donem.id} value={donem.id}>
                  {donem.donemAdi}
                </Select.Option>
              ))}
            </Select>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Select 
              placeholder="Durum" 
              style={{ width: '100%' }} 
              allowClear
              value={selectedDurum}
              onChange={(value) => setSelectedDurum(value)}
            >
              <Select.Option value={AidatDurum.BEKLIYOR}>Ödeme Bekleniyor</Select.Option>
              <Select.Option value={AidatDurum.KISMI_ODENDI}>Kısmi Ödendi</Select.Option>
              <Select.Option value={AidatDurum.ODENDI}>Ödendi</Select.Option>
              <Select.Option value={AidatDurum.GECIKTI}>Gecikti</Select.Option>
            </Select>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={filteredData}
          rowKey="id"
          loading={isLoading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} aidat`,
          }}
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>

      {/* Aidat Borç Import Modal */}
      <Modal
        title={
          <Space>
            <FileExcelOutlined style={{ color: '#059669' }} />
            <span>Excel&apos;den Aidat Borç Import</span>
          </Space>
        }
        open={importModalOpen}
        onCancel={handleCloseImportModal}
        footer={
          importResult ? (
            <Button type="primary" onClick={handleCloseImportModal}>
              Kapat
            </Button>
          ) : null
        }
        width={650}
      >
        {!importResult ? (
          <>
            <Alert
              message="Excel ile Geçmiş Dönem Borçlarını Aktarma"
              description={
                <div>
                  <p>Excel dosyası ile üyelerin geçmiş dönem borçlarını (aidatlarını) topluca sisteme aktarabilirsiniz.</p>
                  <p className="mt-1">
                    <strong>Önemli:</strong> Dönemlerin önceden tanımlanmış olması gerekir. Üye No ve Dönem Kodu ile eşleştirme yapılır.
                  </p>
                </div>
              }
              type="info"
              showIcon
              className="mb-4"
            />

            <div className="mb-4">
              <Typography.Text strong>1. Şablonu İndirin:</Typography.Text>
              <div className="mt-2">
                <Button
                  icon={<DownloadOutlined />}
                  onClick={handleDownloadAidatTemplate}
                  type="dashed"
                  block
                >
                  Aidat Borç Import Şablonu İndir (.xlsx)
                </Button>
              </div>
            </div>

            <Divider />

            {canSelectBirlik && (
              <>
                <div className="mb-4">
                  <Typography.Text strong>2. Birlik Filtresi (opsiyonel):</Typography.Text>
                  <div className="mt-2">
                    <Select
                      placeholder="Sadece bu birliğin üyelerinin borçlarını aktar"
                      allowClear
                      style={{ width: '100%' }}
                      value={importBirlikId}
                      onChange={(val) => setImportBirlikId(val)}
                    >
                      {birlikler?.data?.map((b: any) => (
                        <Select.Option key={b.id} value={b.id}>
                          {b.birlikAdi} {b.birlikTipi === BirlikTipi.MERKEZ ? '(Merkez)' : ''}
                        </Select.Option>
                      ))}
                    </Select>
                  </div>
                </div>
                <Divider />
              </>
            )}

            <div className="mb-4">
              <Typography.Text strong>{canSelectBirlik ? '3' : '2'}. Excel Dosyasını Yükleyin:</Typography.Text>
              <div className="mt-2">
                <Upload.Dragger
                  accept=".xlsx,.xls"
                  maxCount={1}
                  showUploadList={false}
                  beforeUpload={(file) => handleImportUpload(file)}
                  disabled={importMutation.isLoading}
                >
                  <p className="ant-upload-drag-icon">
                    <UploadOutlined style={{ fontSize: 32, color: '#059669' }} />
                  </p>
                  <p className="ant-upload-text">
                    {importMutation.isLoading
                      ? 'İmport ediliyor...'
                      : 'Dosyayı sürükleyin veya tıklayın'}
                  </p>
                  <p className="ant-upload-hint">.xlsx veya .xls formatında</p>
                </Upload.Dragger>
              </div>
            </div>
          </>
        ) : (
          <>
            <Alert
              message="Import Tamamlandı"
              description={`${importResult.basarili} borç kaydı başarıyla eklendi, ${importResult.atlanan} atlandı, ${importResult.hatali} hatalı`}
              type={importResult.hatali > 0 ? 'warning' : 'success'}
              showIcon
              className="mb-4"
            />

            <Row gutter={16} className="mb-4">
              <Col span={6}>
                <Card size="small">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-green-600">{importResult.basarili}</div>
                    <div className="text-xs text-gray-500">Başarılı</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-orange-500">{importResult.atlanan}</div>
                    <div className="text-xs text-gray-500">Atlandı</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-red-600">{importResult.hatali}</div>
                    <div className="text-xs text-gray-500">Hatalı</div>
                  </div>
                </Card>
              </Col>
              <Col span={6}>
                <Card size="small">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-blue-600">{importResult.toplam}</div>
                    <div className="text-xs text-gray-500">Toplam</div>
                  </div>
                </Card>
              </Col>
            </Row>

            {importResult.hatalar.length > 0 && (
              <div className="mb-3">
                <Typography.Text type="danger" strong>Hatalar:</Typography.Text>
                <div className="mt-1 max-h-40 overflow-y-auto bg-red-50 p-2 rounded text-sm">
                  {importResult.hatalar.map((err, idx) => (
                    <div key={idx} className="text-red-700">• {err}</div>
                  ))}
                </div>
              </div>
            )}

            {importResult.uyarilar.length > 0 && (
              <div>
                <Typography.Text type="warning" strong>Uyarılar:</Typography.Text>
                <div className="mt-1 max-h-40 overflow-y-auto bg-orange-50 p-2 rounded text-sm">
                  {importResult.uyarilar.map((warn, idx) => (
                    <div key={idx} className="text-orange-700">• {warn}</div>
                  ))}
                </div>
              </div>
            )}
          </>
        )}
      </Modal>
    </div>
  )
}

export default AidatListPage
