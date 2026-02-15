import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, Descriptions, Tag, Button, Tabs, Spin, Statistic, Row, Col, Table } from 'antd'
import { EditOutlined, ArrowLeftOutlined } from '@ant-design/icons'
import { useQuery } from 'react-query'
import { useBirlikStore } from '@/store/birlikStore'
import { uyeApi } from '@/api/uye'
import { aidatApi } from '@/api/aidat'
import { BirlikTipi, UyeDurum, AidatDurum } from '@/types'
import type { Uye, Aidat } from '@/types'

const BirlikDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { selectedBirlik, isLoading, fetchBirlikById } = useBirlikStore()

  const [uyePage, setUyePage] = useState(0)
  const [aidatPage, setAidatPage] = useState(0)

  useEffect(() => {
    if (id) {
      fetchBirlikById(parseInt(id))
    }
  }, [id, fetchBirlikById])

  const birlikId = id ? parseInt(id) : undefined

  // Üyeler sorgusu
  const { data: uyeData, isLoading: uyeLoading } = useQuery(
    ['birlik-uyeler', birlikId, uyePage],
    () => uyeApi.getByBirlik(birlikId!, uyePage, 10),
    { enabled: !!birlikId }
  )

  // Aidatlar sorgusu
  const { data: aidatData, isLoading: aidatLoading } = useQuery(
    ['birlik-aidatlar', birlikId, aidatPage],
    () => aidatApi.getAidatlar(aidatPage, 10, { birlikId }),
    { enabled: !!birlikId }
  )

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
      children: (
        <Table<Uye>
          dataSource={uyeData?.data?.content || []}
          loading={uyeLoading}
          rowKey="id"
          size="small"
          pagination={{
            current: uyePage + 1,
            total: uyeData?.data?.totalElements || 0,
            pageSize: 10,
            onChange: (page) => setUyePage(page - 1),
            showTotal: (total) => `Toplam ${total} üye`,
            showSizeChanger: false,
          }}
          columns={[
            { title: 'Üye No', dataIndex: 'uyeNo', key: 'uyeNo', width: 120 },
            {
              title: 'Ad Soyad',
              key: 'adSoyad',
              render: (_, record) => `${record.ad} ${record.soyad}`,
            },
            { title: 'TC Kimlik', dataIndex: 'tcKimlikNo', key: 'tc', width: 130 },
            { title: 'Telefon', dataIndex: 'cepTelefon', key: 'tel', width: 130 },
            {
              title: 'Durum',
              dataIndex: 'uyeDurum',
              key: 'durum',
              width: 120,
              render: (durum: UyeDurum) => {
                const colors: Record<UyeDurum, string> = {
                  [UyeDurum.AKTIF]: 'green',
                  [UyeDurum.PASIF]: 'default',
                  [UyeDurum.ASKIYA_ALINMIS]: 'orange',
                  [UyeDurum.IHRAC_EDILMIS]: 'red',
                }
                return <Tag color={colors[durum]}>{durum}</Tag>
              },
            },
            {
              title: '',
              key: 'actions',
              width: 80,
              render: (_, record) => (
                <Button type="link" size="small" onClick={() => navigate(`/uyeler/${record.id}`)}>
                  Detay
                </Button>
              ),
            },
          ]}
        />
      ),
    },
    {
      key: 'aidatlar',
      label: 'Aidatlar',
      children: (
        <Table<Aidat>
          dataSource={aidatData?.data?.content || []}
          loading={aidatLoading}
          rowKey="id"
          size="small"
          pagination={{
            current: aidatPage + 1,
            total: aidatData?.data?.totalElements || 0,
            pageSize: 10,
            onChange: (page) => setAidatPage(page - 1),
            showTotal: (total) => `Toplam ${total} aidat`,
            showSizeChanger: false,
          }}
          columns={[
            { title: 'Üye No', dataIndex: 'uyeNo', key: 'uyeNo', width: 120 },
            { title: 'Üye', dataIndex: 'uyeAdSoyad', key: 'uye' },
            { title: 'Dönem', dataIndex: 'donemAdi', key: 'donem', width: 140 },
            {
              title: 'Tutar',
              dataIndex: 'tahakkukTutari',
              key: 'tutar',
              width: 120,
              align: 'right' as const,
              render: (val: number) => val?.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' }),
            },
            {
              title: 'Ödenen',
              dataIndex: 'odenenTutar',
              key: 'odenen',
              width: 120,
              align: 'right' as const,
              render: (val: number) => val?.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' }),
            },
            {
              title: 'Kalan',
              dataIndex: 'kalanTutar',
              key: 'kalan',
              width: 120,
              align: 'right' as const,
              render: (val: number) => (
                <span style={{ color: val > 0 ? '#cf1322' : '#3f8600' }}>
                  {val?.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}
                </span>
              ),
            },
            {
              title: 'Durum',
              dataIndex: 'durumu',
              key: 'durum',
              width: 120,
              render: (durum: AidatDurum) => {
                const colors: Record<AidatDurum, string> = {
                  [AidatDurum.BEKLIYOR]: 'default',
                  [AidatDurum.KISMI_ODENDI]: 'orange',
                  [AidatDurum.ODENDI]: 'green',
                  [AidatDurum.GECIKTI]: 'red',
                  [AidatDurum.IPTAL]: 'default',
                }
                return <Tag color={colors[durum]}>{durum}</Tag>
              },
            },
          ]}
        />
      ),
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
            <Statistic title="Toplam Üye" value={uyeData?.data?.totalElements ?? 0} />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Aktif Üye"
              value={uyeData?.data?.content?.filter((u) => u.uyeDurum === UyeDurum.AKTIF).length ?? 0}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Bekleyen Aidat"
              value={aidatData?.data?.content?.reduce((sum, a) => sum + (a.kalanTutar || 0), 0) ?? 0}
              suffix="₺"
              precision={2}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <Card variant="borderless" className="shadow-sm">
            <Statistic
              title="Tahsil Oranı"
              value={
                aidatData?.data?.content && aidatData.data.content.length > 0
                  ? Math.round(
                      (aidatData.data.content.reduce((sum, a) => sum + (a.odenenTutar || 0), 0) /
                        aidatData.data.content.reduce((sum, a) => sum + (a.tahakkukTutari || 0), 0)) *
                        100
                    ) || 0
                  : 0
              }
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
