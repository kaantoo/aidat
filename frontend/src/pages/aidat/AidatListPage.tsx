import { useState, useMemo } from 'react'
import { Card, Table, Tag, Button, Space, Select, Row, Col, Input } from 'antd'
import { ExportOutlined, DollarOutlined, SearchOutlined } from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery } from 'react-query'
import { aidatApi } from '@/api/aidat'
import { Aidat, AidatDurum, AidatDonemi } from '@/types'
import { useAuthStore } from '@/store/authStore'
import numeral from 'numeral'

const AidatListPage: React.FC = () => {
  const { user } = useAuthStore()
  const [searchText, setSearchText] = useState('')
  const [selectedDonemId, setSelectedDonemId] = useState<number | undefined>()
  const [selectedDurum, setSelectedDurum] = useState<AidatDurum | undefined>()

  // Tüm aidatları çek, frontend'de filtrele
  const { data, isLoading } = useQuery(
    ['aidatlar'],
    () => aidatApi.getAidatlar(0, 1000)
  )

  const { data: donemlerData } = useQuery(
    ['donemler', user?.birlikId],
    () => aidatApi.getDonemleri(user?.birlikId)
  )

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
    </div>
  )
}

export default AidatListPage
