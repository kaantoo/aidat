import React from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { useQuery } from 'react-query'
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Table,
  Tabs,
  Statistic,
  Row,
  Col,
  Timeline,
  Avatar,
  Divider,
  message,
  Popconfirm,
} from 'antd'
import {
  EditOutlined,
  ArrowLeftOutlined,
  UserOutlined,
  PhoneOutlined,
  MailOutlined,
  HomeOutlined,
  BankOutlined,
  DeleteOutlined,
  DollarOutlined,
  FileTextOutlined,
  HistoryOutlined,
} from '@ant-design/icons'
import dayjs from 'dayjs'
import numeral from 'numeral'
import { uyeApi } from '../../api/uye'
import { aidatApi } from '../../api/aidat'
import { belgeApi } from '../../api/belge'
import { UyeDurum, UyelikTipi, Cinsiyet, AidatDurum, type Aidat, type Belge } from '../../types'
import type { ColumnsType } from 'antd/es/table'

const UyeDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: uyeData, isLoading } = useQuery(
    ['uye', id],
    () => uyeApi.getById(Number(id)),
    { enabled: !!id }
  )

  const { data: aidatlar } = useQuery(
    ['uye-aidatlar', id],
    () => aidatApi.getAidatByUye(Number(id)),
    { enabled: !!id }
  )

  const { data: belgeler } = useQuery(
    ['uye-belgeler', id],
    () => belgeApi.getByUye(Number(id)),
    { enabled: !!id }
  )

  const uye = uyeData?.data

  const formatCurrency = (value: number) => numeral(value).format('0,0.00') + ' ₺'

  const getDurumTag = (durum: UyeDurum) => {
    const durumMap: Record<UyeDurum, { color: string; text: string }> = {
      [UyeDurum.AKTIF]: { color: 'green', text: 'Aktif' },
      [UyeDurum.PASIF]: { color: 'default', text: 'Pasif' },
      [UyeDurum.ASKIYA_ALINMIS]: { color: 'orange', text: 'Askıya Alınmış' },
      [UyeDurum.IHRAC_EDILMIS]: { color: 'red', text: 'İhraç Edilmiş' },
    }
    const info = durumMap[durum] || { color: 'default', text: durum }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const getAidatDurumTag = (durum: AidatDurum) => {
    const durumMap: Record<AidatDurum, { color: string; text: string }> = {
      [AidatDurum.BEKLIYOR]: { color: 'red', text: 'Ödeme Bekleniyor' },
      [AidatDurum.KISMI_ODENDI]: { color: 'orange', text: 'Kısmi Ödendi' },
      [AidatDurum.ODENDI]: { color: 'green', text: 'Ödendi' },
      [AidatDurum.GECIKTI]: { color: 'volcano', text: 'Gecikti' },
      [AidatDurum.IPTAL]: { color: 'default', text: 'İptal' },
    }
    const info = durumMap[durum] || { color: 'default', text: durum }
    return <Tag color={info.color}>{info.text}</Tag>
  }

  const aidatColumns: ColumnsType<Aidat> = [
    {
      title: 'Dönem',
      dataIndex: 'donemAdi',
      key: 'donemAdi',
    },
    {
      title: 'Tutar',
      dataIndex: 'tahakkukTutari',
      key: 'tahakkukTutari',
      align: 'right',
      render: (value) => formatCurrency(value),
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
      render: (value) => (
        <span style={{ color: value > 0 ? '#ef4444' : '#10b981', fontWeight: 'bold' }}>
          {formatCurrency(value)}
        </span>
      ),
    },
    {
      title: 'Durum',
      dataIndex: 'durumu',
      key: 'durumu',
      render: (durum: AidatDurum) => getAidatDurumTag(durum),
    },
    {
      title: 'Son Ödeme',
      dataIndex: 'sonOdemeTarihi',
      key: 'sonOdemeTarihi',
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
  ]

  const belgeColumns: ColumnsType<Belge> = [
    {
      title: 'Belge No',
      dataIndex: 'belgeNo',
      key: 'belgeNo',
      width: 120,
    },
    {
      title: 'Belge Tipi',
      dataIndex: 'belgeTipi',
      key: 'belgeTipi',
    },
    {
      title: 'Dosya Adı',
      dataIndex: 'dosyaAdi',
      key: 'dosyaAdi',
    },
    {
      title: 'Tarih',
      dataIndex: 'belgeTarihi',
      key: 'belgeTarihi',
      render: (date) => dayjs(date).format('DD.MM.YYYY'),
    },
    {
      title: 'İşlemler',
      key: 'actions',
      render: (_, record) => (
        <Button type="link" size="small" onClick={() => window.open(`/api/v1/belgeler/${record.id}/indir`)}>
          İndir
        </Button>
      ),
    },
  ]

  // Aidat istatistikleri hesapla
  const aidatIstatistik = React.useMemo(() => {
    const list = aidatlar?.data || []
    return {
      toplamBorc: list.reduce((acc, a) => acc + (a.tahakkukTutari || 0), 0),
      odenen: list.reduce((acc, a) => acc + (a.odenenTutar || 0), 0),
      kalan: list.reduce((acc, a) => acc + (a.kalanTutar || 0), 0),
      bekleyen: list.filter((a) => a.durumu !== AidatDurum.ODENDI).length,
    }
  }, [aidatlar])

  const handleDelete = async () => {
    try {
      await uyeApi.delete(Number(id))
      message.success('Üye silindi')
      navigate('/uyeler')
    } catch {
      message.error('Üye silinemedi')
    }
  }

  if (isLoading) {
    return <Card loading />
  }

  if (!uye) {
    return (
      <Card>
        <p>Üye bulunamadı</p>
        <Button onClick={() => navigate('/uyeler')}>Geri Dön</Button>
      </Card>
    )
  }

  return (
    <div>
      {/* Header */}
      <div className="flex justify-between items-center mb-4">
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/uyeler')}>
            Geri
          </Button>
          <h1 className="text-xl font-semibold m-0">Üye Detayı</h1>
        </Space>
        <Space>
          <Button
            type="primary"
            icon={<EditOutlined />}
            onClick={() => navigate(`/uyeler/${id}/duzenle`)}
          >
            Düzenle
          </Button>
          <Popconfirm
            title="Üyeyi silmek istediğinize emin misiniz?"
            onConfirm={handleDelete}
            okText="Evet"
            cancelText="Hayır"
          >
            <Button danger icon={<DeleteOutlined />}>
              Sil
            </Button>
          </Popconfirm>
        </Space>
      </div>

      {/* Üye Kartı */}
      <Card variant="borderless" className="shadow-sm mb-4">
        <Row gutter={24}>
          <Col span={4}>
            <div className="text-center">
              <Avatar size={100} icon={<UserOutlined />} className="bg-red-700" />
              <div className="mt-2">
                <Tag color="blue">{uye.uyeNo}</Tag>
              </div>
              <div className="mt-2">{getDurumTag(uye.uyeDurum)}</div>
            </div>
          </Col>
          <Col span={20}>
            <Row gutter={16}>
              <Col span={16}>
                <h2 className="text-2xl font-bold m-0 mb-2">
                  {uye.ad} {uye.soyad}
                </h2>
                <Space split={<Divider type="vertical" />} wrap>
                  {uye.cepTelefon && (
                    <span>
                      <PhoneOutlined className="mr-1" />
                      {uye.cepTelefon}
                    </span>
                  )}
                  {uye.email && (
                    <span>
                      <MailOutlined className="mr-1" />
                      {uye.email}
                    </span>
                  )}
                  {uye.birlikAdi && (
                    <span>
                      <BankOutlined className="mr-1" />
                      {uye.birlikAdi}
                    </span>
                  )}
                </Space>
                {uye.adres && (
                  <div className="mt-2 text-gray-500">
                    <HomeOutlined className="mr-1" />
                    {uye.adres}
                    {uye.ilAdi && `, ${uye.ilAdi}`}
                    {uye.ilceAdi && ` / ${uye.ilceAdi}`}
                  </div>
                )}
              </Col>
              <Col span={8}>
                <Row gutter={[16, 16]}>
                  <Col span={12}>
                    <Statistic
                      title="Toplam Borç"
                      value={aidatIstatistik.toplamBorc}
                      precision={2}
                      suffix="₺"
                      valueStyle={{ fontSize: 18 }}
                    />
                  </Col>
                  <Col span={12}>
                    <Statistic
                      title="Kalan Borç"
                      value={aidatIstatistik.kalan}
                      precision={2}
                      suffix="₺"
                      valueStyle={{ fontSize: 18, color: aidatIstatistik.kalan > 0 ? '#ef4444' : '#10b981' }}
                    />
                  </Col>
                </Row>
              </Col>
            </Row>
          </Col>
        </Row>
      </Card>

      {/* Tabs */}
      <Card variant="borderless" className="shadow-sm">
        <Tabs
          defaultActiveKey="genel"
          items={[
            {
              key: 'genel',
              label: (
                <span>
                  <UserOutlined />
                  Genel Bilgiler
                </span>
              ),
              children: (
                <Row gutter={24}>
                  <Col span={12}>
                    <Descriptions title="Kişisel Bilgiler" bordered column={1} size="small">
                      <Descriptions.Item label="TC Kimlik No">{uye.tcKimlikNo || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Ad Soyad">
                        {uye.ad} {uye.soyad}
                      </Descriptions.Item>
                      <Descriptions.Item label="Baba Adı">{uye.babaAdi || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Ana Adı">{uye.anaAdi || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Doğum Tarihi">
                        {uye.dogumTarihi ? dayjs(uye.dogumTarihi).format('DD.MM.YYYY') : '-'}
                      </Descriptions.Item>
                      <Descriptions.Item label="Doğum Yeri">{uye.dogumYeri || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Cinsiyet">
                        {uye.cinsiyet === Cinsiyet.ERKEK ? 'Erkek' : uye.cinsiyet === Cinsiyet.KADIN ? 'Kadın' : '-'}
                      </Descriptions.Item>
                    </Descriptions>
                  </Col>
                  <Col span={12}>
                    <Descriptions title="İletişim Bilgileri" bordered column={1} size="small">
                      <Descriptions.Item label="Cep Telefon">{uye.cepTelefon || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Sabit Telefon">{uye.sabitTelefon || '-'}</Descriptions.Item>
                      <Descriptions.Item label="E-posta">{uye.email || '-'}</Descriptions.Item>
                      <Descriptions.Item label="İl">{uye.ilAdi || '-'}</Descriptions.Item>
                      <Descriptions.Item label="İlçe">{uye.ilceAdi || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Adres">{uye.adres || '-'}</Descriptions.Item>
                    </Descriptions>
                  </Col>
                  <Col span={24} className="mt-4">
                    <Descriptions title="Üyelik Bilgileri" bordered column={2} size="small">
                      <Descriptions.Item label="Üyelik Tipi">
                        {uye.uyelikTipi === UyelikTipi.GERCEK_KISI ? 'Gerçek Kişi' : 'Tüzel Kişi'}
                      </Descriptions.Item>
                      <Descriptions.Item label="Birlik">{uye.birlikAdi || '-'}</Descriptions.Item>
                      <Descriptions.Item label="Katılım Tarihi">
                        {uye.katilimTarihi ? dayjs(uye.katilimTarihi).format('DD.MM.YYYY') : '-'}
                      </Descriptions.Item>
                      <Descriptions.Item label="Durum">{getDurumTag(uye.uyeDurum)}</Descriptions.Item>
                      {uye.isletmeAdi && (
                        <Descriptions.Item label="İşletme Adı">{uye.isletmeAdi}</Descriptions.Item>
                      )}
                      {uye.hayvanSayisi && (
                        <Descriptions.Item label="Hayvan Sayısı">{uye.hayvanSayisi}</Descriptions.Item>
                      )}
                    </Descriptions>
                  </Col>
                </Row>
              ),
            },
            {
              key: 'aidatlar',
              label: (
                <span>
                  <DollarOutlined />
                  Aidatlar ({aidatlar?.data?.length || 0})
                </span>
              ),
              children: (
                <div>
                  <Row gutter={16} className="mb-4">
                    <Col span={6}>
                      <Card size="small">
                        <Statistic
                          title="Toplam Tahakkuk"
                          value={aidatIstatistik.toplamBorc}
                          precision={2}
                          suffix="₺"
                        />
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small">
                        <Statistic
                          title="Ödenen"
                          value={aidatIstatistik.odenen}
                          precision={2}
                          suffix="₺"
                          valueStyle={{ color: '#10b981' }}
                        />
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small">
                        <Statistic
                          title="Kalan Borç"
                          value={aidatIstatistik.kalan}
                          precision={2}
                          suffix="₺"
                          valueStyle={{ color: '#ef4444' }}
                        />
                      </Card>
                    </Col>
                    <Col span={6}>
                      <Card size="small">
                        <Statistic title="Bekleyen Aidat" value={aidatIstatistik.bekleyen} suffix="adet" />
                      </Card>
                    </Col>
                  </Row>
                  <Table
                    dataSource={aidatlar?.data || []}
                    columns={aidatColumns}
                    rowKey="id"
                    pagination={{ pageSize: 10 }}
                    size="small"
                  />
                  <div className="mt-4 text-right">
                    <Link to="/aidatlar/tahsilat">
                      <Button type="primary" icon={<DollarOutlined />}>
                        Tahsilat Yap
                      </Button>
                    </Link>
                  </div>
                </div>
              ),
            },
            {
              key: 'belgeler',
              label: (
                <span>
                  <FileTextOutlined />
                  Belgeler ({belgeler?.data?.length || 0})
                </span>
              ),
              children: (
                <div>
                  <Table
                    dataSource={belgeler?.data || []}
                    columns={belgeColumns}
                    rowKey="id"
                    pagination={{ pageSize: 10 }}
                    size="small"
                  />
                  <div className="mt-4 text-right">
                    <Link to="/belgeler">
                      <Button type="primary" icon={<FileTextOutlined />}>
                        Belge Ekle
                      </Button>
                    </Link>
                  </div>
                </div>
              ),
            },
            {
              key: 'gecmis',
              label: (
                <span>
                  <HistoryOutlined />
                  İşlem Geçmişi
                </span>
              ),
              children: (
                <Timeline
                  items={[
                    {
                      color: 'green',
                      children: (
                        <>
                          <p className="font-medium">Üye Kaydı Oluşturuldu</p>
                          <p className="text-gray-500 text-sm">
                            {dayjs(uye.createdAt).format('DD.MM.YYYY HH:mm')}
                          </p>
                        </>
                      ),
                    },
                    ...(uye.katilimTarihi
                      ? [
                          {
                            color: 'blue',
                            children: (
                              <>
                                <p className="font-medium">Üyelik Başlangıcı</p>
                                <p className="text-gray-500 text-sm">
                                  {dayjs(uye.katilimTarihi).format('DD.MM.YYYY')}
                                </p>
                              </>
                            ),
                          },
                        ]
                      : []),
                    {
                      color: 'gray',
                      children: (
                        <>
                          <p className="font-medium">Son Güncelleme</p>
                          <p className="text-gray-500 text-sm">
                            {dayjs(uye.updatedAt).format('DD.MM.YYYY HH:mm')}
                          </p>
                        </>
                      ),
                    },
                  ]}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  )
}

export default UyeDetailPage
