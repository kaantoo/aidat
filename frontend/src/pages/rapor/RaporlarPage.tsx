import { useState } from 'react'
import {
  Card,
  Row,
  Col,
  Statistic,
  Table,
  Tabs,
  Select,
  DatePicker,
  Button,
  Progress,
  Space,
  Spin,
} from 'antd'
import {
  DownloadOutlined,
  RiseOutlined,
  FallOutlined,
  TeamOutlined,
  BankOutlined,
  DollarOutlined,
} from '@ant-design/icons'
import { useQuery } from 'react-query'
import { raporApi } from '@/api/rapor'
import { birlikApi } from '@/api/birlik'
import numeral from 'numeral'
import dayjs from 'dayjs'

const RaporlarPage: React.FC = () => {
  const [selectedBirlik, setSelectedBirlik] = useState<number | undefined>()
  const [_dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null)

  const { data: genelIstatistik, isLoading } = useQuery('raporlar-genel', () =>
    raporApi.getGenelIstatistik()
  )

  const { data: birlikIstatistikleri } = useQuery('raporlar-birlik', () =>
    raporApi.getBirlikIstatistikleri()
  )

  const { data: aidatRaporu } = useQuery(['raporlar-aidat', selectedBirlik], () =>
    raporApi.getAidatRaporu(selectedBirlik)
  )

  const { data: gelirGiderRaporu } = useQuery(['raporlar-gelir-gider', selectedBirlik], () =>
    raporApi.getGelirGiderRaporu(selectedBirlik)
  )

  const { data: birlikler } = useQuery('birlikler', () => birlikApi.getAll())

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const istatistik = genelIstatistik?.data
  const gelirGider = gelirGiderRaporu?.data

  const handleExportExcel = async (raporTipi: string) => {
    try {
      const blob = await raporApi.exportExcel(raporTipi, selectedBirlik)
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${raporTipi}-raporu-${dayjs().format('YYYY-MM-DD')}.xlsx`
      a.click()
      window.URL.revokeObjectURL(url)
    } catch (error) {
      console.error('Export error:', error)
    }
  }

  const birlikColumns = [
    {
      title: 'Birlik Adı',
      dataIndex: 'birlikAdi',
      key: 'birlikAdi',
    },
    {
      title: 'Üye Sayısı',
      dataIndex: 'uyeSayisi',
      key: 'uyeSayisi',
      align: 'right' as const,
    },
    {
      title: 'Aktif Üye',
      dataIndex: 'aktifUyeSayisi',
      key: 'aktifUyeSayisi',
      align: 'right' as const,
    },
    {
      title: 'Bekleyen Aidat',
      dataIndex: 'bekleyenAidatTutari',
      key: 'bekleyenAidatTutari',
      align: 'right' as const,
      render: (value: number) => (
        <span style={{ color: value > 0 ? '#f59e0b' : '#10b981' }}>
          {formatCurrency(value)}
        </span>
      ),
    },
    {
      title: 'Tahsil Oranı',
      dataIndex: 'tahsilOrani',
      key: 'tahsilOrani',
      align: 'center' as const,
      render: (value: number) => (
        <Progress
          percent={Math.round(value)}
          size="small"
          status={value >= 80 ? 'success' : value >= 50 ? 'normal' : 'exception'}
        />
      ),
    },
    {
      title: 'Son Tahsilat',
      dataIndex: 'sonTahsilatTarihi',
      key: 'sonTahsilatTarihi',
      render: (date: string) => (date ? dayjs(date).format('DD.MM.YYYY') : '-'),
    },
  ]

  const aidatRaporuColumns = [
    {
      title: 'Birlik',
      dataIndex: 'birlikAdi',
      key: 'birlikAdi',
    },
    {
      title: 'Dönem',
      dataIndex: 'donem',
      key: 'donem',
    },
    {
      title: 'Toplam Üye',
      dataIndex: 'toplamUye',
      key: 'toplamUye',
      align: 'right' as const,
    },
    {
      title: 'Toplam Tahakkuk',
      dataIndex: 'tahakkukTutari',
      key: 'tahakkukTutari',
      align: 'right' as const,
      render: (value: number) => formatCurrency(value || 0),
    },
    {
      title: 'Toplam Tahsilat',
      dataIndex: 'tahsilatTutari',
      key: 'tahsilatTutari',
      align: 'right' as const,
      render: (value: number) => (
        <span style={{ color: '#10b981' }}>{formatCurrency(value || 0)}</span>
      ),
    },
    {
      title: 'Bekleyen Tutar',
      dataIndex: 'bekleyenTutar',
      key: 'bekleyenTutar',
      align: 'right' as const,
      render: (value: number) => (
        <span style={{ color: value > 0 ? '#ef4444' : 'inherit' }}>
          {formatCurrency(value || 0)}
        </span>
      ),
    },
    {
      title: 'Tahsil Oranı',
      dataIndex: 'tahsilatOrani',
      key: 'tahsilatOrani',
      align: 'center' as const,
      render: (value: number) => (
        <Progress
          percent={Math.round(value || 0)}
          size="small"
          status={(value || 0) >= 80 ? 'success' : (value || 0) >= 50 ? 'normal' : 'exception'}
        />
      ),
    },
  ]

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" tip="Veriler yükleniyor..." />
      </div>
    )
  }

  const tabItems = [
    {
      key: 'genel',
      label: 'Genel İstatistikler',
      children: (
        <div>
          <Row gutter={[16, 16]} className="mb-6">
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Birlik"
                  value={istatistik?.toplamBirlikSayisi || 0}
                  prefix={<BankOutlined style={{ color: '#b91c1c' }} />}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Üye"
                  value={istatistik?.toplamUyeSayisi || 0}
                  prefix={<TeamOutlined style={{ color: '#0ea5e9' }} />}
                  suffix={
                    <span className="text-sm text-gray-500">
                      ({istatistik?.aktifUyeSayisi || 0} aktif)
                    </span>
                  }
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Tahakkuk"
                  value={istatistik?.toplamTahakkuk || istatistik?.bekleyenAidatTutari || 0}
                  precision={2}
                  prefix={<FallOutlined style={{ color: '#f59e0b' }} />}
                  suffix="₺"
                  valueStyle={{ color: '#f59e0b' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Tahsilat"
                  value={istatistik?.toplamTahsilat || istatistik?.tahsilEdilmisAidatTutari || 0}
                  precision={2}
                  prefix={<RiseOutlined style={{ color: '#10b981' }} />}
                  suffix="₺"
                  valueStyle={{ color: '#10b981' }}
                />
              </Card>
            </Col>
          </Row>

          {/* Tahsilat Trendi */}
          {istatistik?.aylikTahsilatTrendi && istatistik.aylikTahsilatTrendi.length > 0 && (
            <Card title="Aylık Tahsilat Trendi" variant="borderless" className="mb-6">
              <div className="flex items-end gap-2 h-48">
                {istatistik.aylikTahsilatTrendi.map((item, index) => {
                  const trendData = istatistik.aylikTahsilatTrendi!
                  const maxValue = Math.max(...trendData.map((i) => i.tutar))
                  const height = maxValue > 0 ? (item.tutar / maxValue) * 100 : 0
                  return (
                    <div key={index} className="flex flex-col items-center flex-1">
                      <div
                        className="w-full bg-red-600 rounded-t"
                        style={{ height: `${height}%`, minHeight: 4 }}
                      />
                      <div className="text-xs mt-1 text-gray-500">{item.donem}</div>
                      <div className="text-xs font-medium">{numeral(item.tutar).format('0a')}</div>
                    </div>
                  )
                })}
              </div>
            </Card>
          )}

          <div className="text-right mb-4">
            <Button icon={<DownloadOutlined />} onClick={() => handleExportExcel('genel')}>
              Excel İndir
            </Button>
          </div>
        </div>
      ),
    },
    {
      key: 'aidat',
      label: 'Aidat Raporu',
      children: (
        <div>
          <div className="mb-4">
            <Space>
              <Select
                placeholder="Birlik Seçin"
                style={{ width: 250 }}
                allowClear
                onChange={setSelectedBirlik}
              >
                {birlikler?.data?.map((b) => (
                  <Select.Option key={b.id} value={b.id}>
                    {b.birlikAdi}
                  </Select.Option>
                ))}
              </Select>
              <Button icon={<DownloadOutlined />} onClick={() => handleExportExcel('aidat')}>
                Excel İndir
              </Button>
            </Space>
          </div>

          <Table
            dataSource={aidatRaporu?.data || []}
            columns={aidatRaporuColumns}
            rowKey="donemAdi"
            pagination={false}
            size="middle"
          />
        </div>
      ),
    },
    {
      key: 'birlik',
      label: 'Birlik Bazlı',
      children: (
        <div>
          <div className="text-right mb-4">
            <Button icon={<DownloadOutlined />} onClick={() => handleExportExcel('birlik')}>
              Excel İndir
            </Button>
          </div>
          <Table
            dataSource={birlikIstatistikleri?.data || []}
            columns={birlikColumns}
            rowKey="birlikId"
            pagination={{ pageSize: 10 }}
            size="middle"
          />
        </div>
      ),
    },
    {
      key: 'gelir-gider',
      label: 'Gelir/Gider Raporu',
      children: (
        <div>
          <div className="mb-4">
            <Space>
              <Select
                placeholder="Birlik Seçin"
                style={{ width: 250 }}
                allowClear
                onChange={setSelectedBirlik}
              >
                {birlikler?.data?.map((b) => (
                  <Select.Option key={b.id} value={b.id}>
                    {b.birlikAdi}
                  </Select.Option>
                ))}
              </Select>
              <DatePicker.RangePicker
                onChange={(dates) => setDateRange(dates as [dayjs.Dayjs | null, dayjs.Dayjs | null] | null)}
              />
              <Button icon={<DownloadOutlined />} onClick={() => handleExportExcel('gelir-gider')}>
                Excel İndir
              </Button>
            </Space>
          </div>

          <Row gutter={[16, 16]}>
            <Col xs={24} md={8}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Gelir"
                  value={gelirGider?.toplamGelir || 0}
                  precision={2}
                  suffix="₺"
                  prefix={<RiseOutlined />}
                  valueStyle={{ color: '#10b981' }}
                />
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card variant="borderless">
                <Statistic
                  title="Toplam Gider"
                  value={gelirGider?.toplamGider || 0}
                  precision={2}
                  suffix="₺"
                  prefix={<FallOutlined />}
                  valueStyle={{ color: '#ef4444' }}
                />
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card variant="borderless">
                <Statistic
                  title="Net Durum"
                  value={gelirGider?.netDurum || 0}
                  precision={2}
                  suffix="₺"
                  prefix={<DollarOutlined />}
                  valueStyle={{ color: (gelirGider?.netDurum || 0) >= 0 ? '#10b981' : '#ef4444' }}
                />
              </Card>
            </Col>
          </Row>
        </div>
      ),
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Raporlar</h1>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Tabs items={tabItems} />
      </Card>
    </div>
  )
}

export default RaporlarPage
