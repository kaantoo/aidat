import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Tabs, Spin, Statistic, Row, Col } from 'antd'
import { EditOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useBirlikStore } from '@/store/birlikStore'
import { BirlikTipi } from '@/types'

const BirlikDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { selectedBirlik, isLoading, fetchBirlikById } = useBirlikStore()

  useEffect(() => {
    if (id) {
      fetchBirlikById(parseInt(id))
    }
  }, [id, fetchBirlikById])

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Spin size="large" tip="Yükleniyor..." />
      </div>
    )
  }

  if (!selectedBirlik) {
    return <div>Birlik bulunamadı</div>
  }

  const birlik = selectedBirlik

  const getBirlikTipiLabel = (tip: BirlikTipi) => {
    const labels: Record<BirlikTipi, string> = {
      [BirlikTipi.MERKEZ]: 'Merkez Birliği',
      [BirlikTipi.ALT_BIRLIK]: 'Alt Birlik',
    }
    return labels[tip]
  }

  const tabItems = [
    {
      key: 'genel',
      label: 'Genel Bilgiler',
      children: (
        <Descriptions bordered column={{ xxl: 3, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }}>
          <Descriptions.Item label="Birlik Kodu">{birlik.birlikKodu}</Descriptions.Item>
          <Descriptions.Item label="Birlik Adı">{birlik.birlikAdi}</Descriptions.Item>
          <Descriptions.Item label="Birlik Tipi">
            {getBirlikTipiLabel(birlik.birlikTipi)}
          </Descriptions.Item>
          <Descriptions.Item label="İl Kodu">{birlik.ilKodu}</Descriptions.Item>
          <Descriptions.Item label="İlçe Kodu">{birlik.ilceKodu || '-'}</Descriptions.Item>
          <Descriptions.Item label="Durum">
            <Tag color={birlik.aktif ? 'green' : 'red'}>
              {birlik.aktif ? 'Aktif' : 'Pasif'}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Adres" span={3}>
            {birlik.adres || '-'}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'iletisim',
      label: 'İletişim Bilgileri',
      children: (
        <Descriptions bordered column={{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }}>
          <Descriptions.Item label="Telefon">{birlik.telefon || '-'}</Descriptions.Item>
          <Descriptions.Item label="E-posta">{birlik.email || '-'}</Descriptions.Item>
          <Descriptions.Item label="Yetkili Adı">
            {birlik.yetkiliAdi || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Yetkili Telefon">
            {birlik.yetkiliTelefon || '-'}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'mali',
      label: 'Mali Bilgiler',
      children: (
        <Descriptions bordered column={{ xxl: 2, xl: 2, lg: 2, md: 1, sm: 1, xs: 1 }}>
          <Descriptions.Item label="Vergi No">{birlik.vergiNo || '-'}</Descriptions.Item>
          <Descriptions.Item label="Vergi Dairesi">
            {birlik.vergiDairesi || '-'}
          </Descriptions.Item>
          <Descriptions.Item label="IBAN No" span={2}>
            {birlik.ibanNo || '-'}
          </Descriptions.Item>
        </Descriptions>
      ),
    },
    {
      key: 'uyeler',
      label: 'Üyeler',
      children: <div>Üye listesi burada gösterilecek</div>,
    },
    {
      key: 'aidatlar',
      label: 'Aidatlar',
      children: <div>Aidat listesi burada gösterilecek</div>,
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <div className="flex items-center gap-4">
          <Button
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/birlikler')}
          >
            Geri
          </Button>
          <h1 className="text-xl font-semibold m-0">{birlik.birlikAdi}</h1>
        </div>
        <Button
          type="primary"
          icon={<EditOutlined />}
          onClick={() => navigate(`/birlikler/${id}/duzenle`)}
          style={{ background: '#b91c1c' }}
        >
          Düzenle
        </Button>
      </div>

      <Row gutter={[16, 16]} className="mb-4">
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic title="Toplam Üye" value={0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic title="Aktif Üye" value={0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic title="Bekleyen Aidat" value={0} suffix="₺" precision={2} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Tahsil Oranı"
              value={0}
              suffix="%"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
      </Row>

      <Card variant="borderless" className="shadow-sm">
        <Tabs items={tabItems} />
      </Card>
    </div>
  )
}

export default BirlikDetailPage
