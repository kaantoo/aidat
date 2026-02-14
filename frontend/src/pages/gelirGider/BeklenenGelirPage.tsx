import { useState, useMemo } from 'react'
import { Card, Table, Tag, Statistic, Row, Col, Select, Progress, Input, Empty } from 'antd'
import { 
  DollarOutlined, 
  BankOutlined, 
  UserOutlined, 
  SearchOutlined,
  RiseOutlined,
  ClockCircleOutlined
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery } from 'react-query'
import { aidatApi } from '@/api/aidat'
import { birlikApi } from '@/api/birlik'
import { useAuthStore } from '@/store/authStore'
import { Aidat, AidatDurum, AidatDonemi, KullaniciRol } from '@/types'
import numeral from 'numeral'

interface BirlikGelirOzet {
  birlikId: number
  birlikAdi: string
  birlikKodu: string
  toplamTahakkuk: number
  toplamTahsilat: number
  kalanBorc: number
  tahsilOrani: number
  bekleyenAidatSayisi: number
}

interface UyeGelirOzet {
  uyeId: number
  uyeNo: string
  uyeAdSoyad: string
  toplamTahakkuk: number
  toplamTahsilat: number
  kalanBorc: number
  bekleyenAidatSayisi: number
  sonOdemeTarihi?: string
}

const BeklenenGelirPage: React.FC = () => {
  const { user, hasRole } = useAuthStore()
  const [searchText, setSearchText] = useState('')
  const [selectedDonemId, setSelectedDonemId] = useState<number | undefined>()
  
  // Merkez birlik yöneticileri alt birlikleri görür, diğerleri üyeleri
  const isMerkezBirlik = hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI])

  // Dönemler
  const { data: donemlerData } = useQuery(
    ['donemler', user?.birlikId],
    () => aidatApi.getDonemleri(user?.birlikId)
  )

  // Tüm aidatlar
  const { data: aidatlarData, isLoading: aidatlarLoading } = useQuery(
    ['aidatlar-beklenen'],
    () => aidatApi.getAidatlar(0, 5000)
  )

  // Alt birlikler (sadece merkez birlik için)
  useQuery(
    ['birlikler'],
    () => birlikApi.getAll(0, 100),
    { enabled: isMerkezBirlik }
  )

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  // Merkez birlik için alt birlik bazlı özet hesapla
  const birlikOzetleri = useMemo((): BirlikGelirOzet[] => {
    if (!isMerkezBirlik || !aidatlarData?.data) return []
    
    const aidatlar = aidatlarData.data.content || aidatlarData.data || []
    if (!Array.isArray(aidatlar)) return []

    // Dönem filtresi
    let filteredAidatlar = aidatlar
    if (selectedDonemId) {
      filteredAidatlar = aidatlar.filter((a: Aidat) => a.aidatDonemiId === selectedDonemId)
    }

    // Birlik bazlı grupla
    const birlikMap = new Map<number, BirlikGelirOzet>()
    
    filteredAidatlar.forEach((aidat: Aidat) => {
      const birlikId = aidat.birlikId || 0
      if (!birlikMap.has(birlikId)) {
        birlikMap.set(birlikId, {
          birlikId,
          birlikAdi: aidat.birlikAdi || 'Bilinmiyor',
          birlikKodu: '',
          toplamTahakkuk: 0,
          toplamTahsilat: 0,
          kalanBorc: 0,
          tahsilOrani: 0,
          bekleyenAidatSayisi: 0,
        })
      }
      
      const ozet = birlikMap.get(birlikId)!
      ozet.toplamTahakkuk += aidat.tahakkukTutari || 0
      ozet.toplamTahsilat += aidat.odenenTutar || 0
      ozet.kalanBorc += aidat.kalanTutar || 0
      
      if (aidat.durumu !== AidatDurum.ODENDI && aidat.durumu !== AidatDurum.IPTAL) {
        ozet.bekleyenAidatSayisi++
      }
    })

    // Tahsil oranını hesapla
    birlikMap.forEach(ozet => {
      ozet.tahsilOrani = ozet.toplamTahakkuk > 0 
        ? (ozet.toplamTahsilat / ozet.toplamTahakkuk) * 100 
        : 0
    })

    // Arama filtresi
    let result = Array.from(birlikMap.values())
    if (searchText) {
      const searchLower = searchText.toLowerCase()
      result = result.filter(b => 
        b.birlikAdi.toLowerCase().includes(searchLower) ||
        b.birlikKodu.toLowerCase().includes(searchLower)
      )
    }

    return result.sort((a, b) => b.kalanBorc - a.kalanBorc)
  }, [aidatlarData, selectedDonemId, searchText, isMerkezBirlik])

  // Alt birlik için üye bazlı özet hesapla
  const uyeOzetleri = useMemo((): UyeGelirOzet[] => {
    if (isMerkezBirlik || !aidatlarData?.data) return []
    
    const aidatlar = aidatlarData.data.content || aidatlarData.data || []
    if (!Array.isArray(aidatlar)) return []

    // Dönem filtresi
    let filteredAidatlar = aidatlar
    if (selectedDonemId) {
      filteredAidatlar = aidatlar.filter((a: Aidat) => a.aidatDonemiId === selectedDonemId)
    }

    // Üye bazlı grupla
    const uyeMap = new Map<number, UyeGelirOzet>()
    
    filteredAidatlar.forEach((aidat: Aidat) => {
      const uyeId = aidat.uyeId || 0
      if (!uyeMap.has(uyeId)) {
        uyeMap.set(uyeId, {
          uyeId,
          uyeNo: aidat.uyeNo || '',
          uyeAdSoyad: aidat.uyeAdSoyad || 'Bilinmiyor',
          toplamTahakkuk: 0,
          toplamTahsilat: 0,
          kalanBorc: 0,
          bekleyenAidatSayisi: 0,
        })
      }
      
      const ozet = uyeMap.get(uyeId)!
      ozet.toplamTahakkuk += aidat.tahakkukTutari || 0
      ozet.toplamTahsilat += aidat.odenenTutar || 0
      ozet.kalanBorc += aidat.kalanTutar || 0
      
      if (aidat.durumu !== AidatDurum.ODENDI && aidat.durumu !== AidatDurum.IPTAL) {
        ozet.bekleyenAidatSayisi++
        if (aidat.sonOdemeTarihi && (!ozet.sonOdemeTarihi || aidat.sonOdemeTarihi < ozet.sonOdemeTarihi)) {
          ozet.sonOdemeTarihi = aidat.sonOdemeTarihi
        }
      }
    })

    // Arama filtresi
    let result = Array.from(uyeMap.values())
    if (searchText) {
      const searchLower = searchText.toLowerCase()
      result = result.filter(u => 
        u.uyeNo.toLowerCase().includes(searchLower) ||
        u.uyeAdSoyad.toLowerCase().includes(searchLower)
      )
    }

    return result.sort((a, b) => b.kalanBorc - a.kalanBorc)
  }, [aidatlarData, selectedDonemId, searchText, isMerkezBirlik])

  // Toplam istatistikler
  const toplamlar = useMemo(() => {
    const data = isMerkezBirlik ? birlikOzetleri : uyeOzetleri
    return {
      toplamTahakkuk: data.reduce((sum, item) => sum + item.toplamTahakkuk, 0),
      toplamTahsilat: data.reduce((sum, item) => sum + item.toplamTahsilat, 0),
      kalanBorc: data.reduce((sum, item) => sum + item.kalanBorc, 0),
      kayitSayisi: data.length,
    }
  }, [birlikOzetleri, uyeOzetleri, isMerkezBirlik])

  const tahsilOrani = toplamlar.toplamTahakkuk > 0 
    ? (toplamlar.toplamTahsilat / toplamlar.toplamTahakkuk) * 100 
    : 0

  // Birlik kolonları
  const birlikColumns: ColumnsType<BirlikGelirOzet> = [
    {
      title: 'Birlik Adı',
      dataIndex: 'birlikAdi',
      key: 'birlikAdi',
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: 'Toplam Tahakkuk',
      dataIndex: 'toplamTahakkuk',
      key: 'toplamTahakkuk',
      align: 'right',
      render: (value) => formatCurrency(value),
      sorter: (a, b) => a.toplamTahakkuk - b.toplamTahakkuk,
    },
    {
      title: 'Tahsil Edilen',
      dataIndex: 'toplamTahsilat',
      key: 'toplamTahsilat',
      align: 'right',
      render: (value) => <span className="text-green-600">{formatCurrency(value)}</span>,
      sorter: (a, b) => a.toplamTahsilat - b.toplamTahsilat,
    },
    {
      title: 'Beklenen Gelir',
      dataIndex: 'kalanBorc',
      key: 'kalanBorc',
      align: 'right',
      render: (value) => <strong className="text-red-600">{formatCurrency(value)}</strong>,
      sorter: (a, b) => a.kalanBorc - b.kalanBorc,
      defaultSortOrder: 'descend',
    },
    {
      title: 'Tahsil Oranı',
      dataIndex: 'tahsilOrani',
      key: 'tahsilOrani',
      align: 'center',
      width: 150,
      render: (value) => (
        <Progress 
          percent={Math.round(value)} 
          size="small" 
          status={value >= 80 ? 'success' : value >= 50 ? 'normal' : 'exception'}
        />
      ),
      sorter: (a, b) => a.tahsilOrani - b.tahsilOrani,
    },
    {
      title: 'Bekleyen Aidat',
      dataIndex: 'bekleyenAidatSayisi',
      key: 'bekleyenAidatSayisi',
      align: 'center',
      render: (value) => (
        <Tag color={value > 0 ? 'orange' : 'green'}>
          {value} adet
        </Tag>
      ),
      sorter: (a, b) => a.bekleyenAidatSayisi - b.bekleyenAidatSayisi,
    },
  ]

  // Üye kolonları
  const uyeColumns: ColumnsType<UyeGelirOzet> = [
    {
      title: 'Üye No',
      dataIndex: 'uyeNo',
      key: 'uyeNo',
      width: 100,
    },
    {
      title: 'Üye Adı Soyadı',
      dataIndex: 'uyeAdSoyad',
      key: 'uyeAdSoyad',
      render: (text) => <strong>{text}</strong>,
    },
    {
      title: 'Toplam Tahakkuk',
      dataIndex: 'toplamTahakkuk',
      key: 'toplamTahakkuk',
      align: 'right',
      render: (value) => formatCurrency(value),
      sorter: (a, b) => a.toplamTahakkuk - b.toplamTahakkuk,
    },
    {
      title: 'Ödenen',
      dataIndex: 'toplamTahsilat',
      key: 'toplamTahsilat',
      align: 'right',
      render: (value) => <span className="text-green-600">{formatCurrency(value)}</span>,
      sorter: (a, b) => a.toplamTahsilat - b.toplamTahsilat,
    },
    {
      title: 'Beklenen Gelir',
      dataIndex: 'kalanBorc',
      key: 'kalanBorc',
      align: 'right',
      render: (value) => <strong className="text-red-600">{formatCurrency(value)}</strong>,
      sorter: (a, b) => a.kalanBorc - b.kalanBorc,
      defaultSortOrder: 'descend',
    },
    {
      title: 'Bekleyen Aidat',
      dataIndex: 'bekleyenAidatSayisi',
      key: 'bekleyenAidatSayisi',
      align: 'center',
      render: (value) => (
        <Tag color={value > 0 ? 'orange' : 'green'}>
          {value} adet
        </Tag>
      ),
      sorter: (a, b) => a.bekleyenAidatSayisi - b.bekleyenAidatSayisi,
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">
          <ClockCircleOutlined className="mr-2" />
          Beklenen Gelirler
        </h1>
      </div>

      {/* İstatistik Kartları */}
      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} sm={12} md={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Toplam Tahakkuk"
              value={toplamlar.toplamTahakkuk}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="₺"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Tahsil Edilen"
              value={toplamlar.toplamTahsilat}
              precision={2}
              prefix={<RiseOutlined />}
              suffix="₺"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Beklenen Gelir"
              value={toplamlar.kalanBorc}
              precision={2}
              prefix={<ClockCircleOutlined />}
              suffix="₺"
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Tahsil Oranı"
              value={tahsilOrani}
              precision={1}
              suffix="%"
              valueStyle={{ color: tahsilOrani >= 70 ? '#52c41a' : tahsilOrani >= 40 ? '#faad14' : '#ff4d4f' }}
            />
            <Progress 
              percent={Math.round(tahsilOrani)} 
              showInfo={false}
              status={tahsilOrani >= 70 ? 'success' : tahsilOrani >= 40 ? 'normal' : 'exception'}
            />
          </Card>
        </Col>
      </Row>

      {/* Filtreler ve Tablo */}
      <Card variant="borderless" className="shadow-sm">
        <Row gutter={[16, 16]} className="mb-4">
          <Col xs={24} sm={12} md={8}>
            <Input
              placeholder={isMerkezBirlik ? "Birlik Ara..." : "Üye No veya Ad Ara..."}
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col xs={24} sm={12} md={8}>
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
        </Row>

        {isMerkezBirlik ? (
          <>
            <div className="mb-2 text-gray-500">
              <BankOutlined className="mr-1" />
              Alt birliklerden beklenen gelirler ({birlikOzetleri.length} birlik)
            </div>
            <Table
              columns={birlikColumns}
              dataSource={birlikOzetleri}
              rowKey="birlikId"
              loading={aidatlarLoading}
              pagination={{
                showSizeChanger: true,
                showTotal: (total) => `Toplam ${total} birlik`,
              }}
              scroll={{ x: 900 }}
              size="middle"
              locale={{
                emptyText: <Empty description="Beklenen gelir bulunamadı" />
              }}
            />
          </>
        ) : (
          <>
            <div className="mb-2 text-gray-500">
              <UserOutlined className="mr-1" />
              Üyelerden beklenen gelirler ({uyeOzetleri.length} üye)
            </div>
            <Table
              columns={uyeColumns}
              dataSource={uyeOzetleri}
              rowKey="uyeId"
              loading={aidatlarLoading}
              pagination={{
                showSizeChanger: true,
                showTotal: (total) => `Toplam ${total} üye`,
              }}
              scroll={{ x: 900 }}
              size="middle"
              locale={{
                emptyText: <Empty description="Beklenen gelir bulunamadı" />
              }}
            />
          </>
        )}
      </Card>
    </div>
  )
}

export default BeklenenGelirPage
