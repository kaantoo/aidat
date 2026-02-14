import { useEffect, useState } from 'react'
import { Row, Col, Card, Statistic, Table, Progress, Spin } from 'antd'
import {
  TeamOutlined,
  BankOutlined,
  DollarOutlined,
  RiseOutlined,
  FallOutlined,
} from '@ant-design/icons'
import { useQuery } from 'react-query'
import { raporApi } from '@/api/rapor'
import { useAuthStore } from '@/store/authStore'
import { KullaniciRol } from '@/types'
import numeral from 'numeral'

const DashboardPage: React.FC = () => {
  const { user, hasRole } = useAuthStore()
  const [greeting, setGreeting] = useState('')

  useEffect(() => {
    const hour = new Date().getHours()
    if (hour < 12) setGreeting('Günaydın')
    else if (hour < 18) setGreeting('İyi günler')
    else setGreeting('İyi akşamlar')
  }, [])

  const { data: genelIstatistik, isLoading: isLoadingGenel } = useQuery(
    'genelIstatistik',
    () => raporApi.getGenelIstatistik(),
    { enabled: hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI]) }
  )

  const { data: birlikIstatistikleri, isLoading: isLoadingBirlik } = useQuery(
    'birlikIstatistikleri',
    () => raporApi.getBirlikIstatistikleri(),
    { enabled: hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI]) }
  )

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const istatistik = genelIstatistik?.data

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
      title: 'Bekleyen Aidat',
      dataIndex: 'bekleyenAidatTutari',
      key: 'bekleyenAidatTutari',
      align: 'right' as const,
      render: (value: number) => formatCurrency(value),
    },
    {
      title: 'Tahsil Oranı',
      dataIndex: 'tahsilOrani',
      key: 'tahsilOrani',
      align: 'center' as const,
      render: (value: number) => (
        <Progress
          percent={value}
          size="small"
          status={value >= 80 ? 'success' : value >= 50 ? 'normal' : 'exception'}
        />
      ),
    },
  ]

  if (isLoadingGenel || isLoadingBirlik) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" tip="Veriler yükleniyor..." />
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-semibold text-gray-800">
          {greeting}, {user?.ad}
        </h1>
        <p className="text-gray-500">
          Aidat ve Yönetim Sistemi kontrol panelinize hoş geldiniz
        </p>
      </div>

      {hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI]) && istatistik && (
        <>
          <Row gutter={[16, 16]} className="mb-6">
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless" className="shadow-sm">
                <Statistic
                  title="Toplam Birlik"
                  value={istatistik.toplamBirlikSayisi}
                  prefix={<BankOutlined style={{ color: '#b91c1c' }} />}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless" className="shadow-sm">
                <Statistic
                  title="Toplam Üye"
                  value={istatistik.toplamUyeSayisi}
                  prefix={<TeamOutlined style={{ color: '#0ea5e9' }} />}
                  suffix={
                    <span className="text-sm text-gray-500">
                      ({istatistik.aktifUyeSayisi} aktif)
                    </span>
                  }
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless" className="shadow-sm">
                <Statistic
                  title="Bekleyen Aidat"
                  value={istatistik.bekleyenAidatTutari}
                  precision={2}
                  prefix={<FallOutlined style={{ color: '#f59e0b' }} />}
                  suffix="₺"
                  valueStyle={{ color: '#f59e0b' }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={6}>
              <Card variant="borderless" className="shadow-sm">
                <Statistic
                  title="Tahsil Edilen"
                  value={istatistik.tahsilEdilmisAidatTutari}
                  precision={2}
                  prefix={<RiseOutlined style={{ color: '#10b981' }} />}
                  suffix="₺"
                  valueStyle={{ color: '#10b981' }}
                />
              </Card>
            </Col>
          </Row>

          <Row gutter={[16, 16]}>
            <Col xs={24} lg={16}>
              <Card
                title="Birlik Bazında İstatistikler"
                variant="borderless"
                className="shadow-sm"
              >
                <Table
                  dataSource={birlikIstatistikleri?.data || []}
                  columns={birlikColumns}
                  rowKey="birlikId"
                  pagination={{ pageSize: 10 }}
                  size="small"
                />
              </Card>
            </Col>
            <Col xs={24} lg={8}>
              <Card title="Hızlı İşlemler" variant="borderless" className="shadow-sm">
                <div className="space-y-3">
                  <Card.Grid
                    style={{ width: '50%', textAlign: 'center', cursor: 'pointer' }}
                    onClick={() => (window.location.href = '/uyeler/yeni')}
                  >
                    <TeamOutlined style={{ fontSize: 24, color: '#b91c1c' }} />
                    <div className="mt-2">Yeni Üye</div>
                  </Card.Grid>
                  <Card.Grid
                    style={{ width: '50%', textAlign: 'center', cursor: 'pointer' }}
                    onClick={() => (window.location.href = '/aidatlar/tahsilat')}
                  >
                    <DollarOutlined style={{ fontSize: 24, color: '#10b981' }} />
                    <div className="mt-2">Tahsilat</div>
                  </Card.Grid>
                  <Card.Grid
                    style={{ width: '50%', textAlign: 'center', cursor: 'pointer' }}
                    onClick={() => (window.location.href = '/birlikler/yeni')}
                  >
                    <BankOutlined style={{ fontSize: 24, color: '#0ea5e9' }} />
                    <div className="mt-2">Yeni Birlik</div>
                  </Card.Grid>
                  <Card.Grid
                    style={{ width: '50%', textAlign: 'center', cursor: 'pointer' }}
                    onClick={() => (window.location.href = '/raporlar')}
                  >
                    <RiseOutlined style={{ fontSize: 24, color: '#8b5cf6' }} />
                    <div className="mt-2">Raporlar</div>
                  </Card.Grid>
                </div>
              </Card>

              <Card
                title="Son Tahsilatlar"
                variant="borderless"
                className="shadow-sm mt-4"
              >
                <div className="text-gray-500 text-center py-4">
                  Son tahsilat kayıtları burada görüntülenecek
                </div>
              </Card>
            </Col>
          </Row>
        </>
      )}

      {hasRole([KullaniciRol.BIRLIK_YONETICI, KullaniciRol.BIRLIK_PERSONEL]) && (
        <Card title="Birlik Özeti" variant="borderless" className="shadow-sm">
          <p>
            {user?.birlikAdi} birliğine ait özet bilgiler burada görüntülenecek.
          </p>
        </Card>
      )}

      {hasRole([KullaniciRol.GOZLEMCI]) && (
        <Card variant="borderless" className="shadow-sm">
          <p>Gözlemci olarak sadece raporları görüntüleyebilirsiniz.</p>
        </Card>
      )}
    </div>
  )
}

export default DashboardPage
