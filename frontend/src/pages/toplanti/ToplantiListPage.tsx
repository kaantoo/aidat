import { useState, useMemo } from 'react'
import { Card, Table, Tag, Button, Space, Select, Row, Col, Input, DatePicker, Popconfirm, message } from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  CalendarOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { toplantiApi } from '@/api/toplanti'
import { useAuthStore } from '@/store/authStore'
import { Toplanti, ToplantiTuru, ToplantiDurumu, KullaniciRol } from '@/types'
import dayjs from 'dayjs'

const { RangePicker } = DatePicker

const toplantiTuruLabels: Record<ToplantiTuru, string> = {
  [ToplantiTuru.GENEL_KURUL]: 'Genel Kurul',
  [ToplantiTuru.YONETIM_KURULU]: 'Yönetim Kurulu',
  [ToplantiTuru.DENETIM_KURULU]: 'Denetim Kurulu',
  [ToplantiTuru.OLAGAN_TOPLANTI]: 'Olağan Toplantı',
  [ToplantiTuru.OLAGANUSTU_TOPLANTI]: 'Olağanüstü Toplantı',
  [ToplantiTuru.DIGER]: 'Diğer',
}

const toplantiDurumuLabels: Record<ToplantiDurumu, string> = {
  [ToplantiDurumu.PLANLANMIS]: 'Planlanmış',
  [ToplantiDurumu.DEVAM_EDIYOR]: 'Devam Ediyor',
  [ToplantiDurumu.TAMAMLANDI]: 'Tamamlandı',
  [ToplantiDurumu.IPTAL]: 'İptal',
  [ToplantiDurumu.ERTELENDI]: 'Ertelendi',
}

const ToplantiListPage: React.FC = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, hasRole } = useAuthStore()
  const [searchText, setSearchText] = useState('')
  const [selectedTuru, setSelectedTuru] = useState<ToplantiTuru | undefined>()
  const [selectedDurum, setSelectedDurum] = useState<ToplantiDurumu | undefined>()
  const [dateRange, setDateRange] = useState<[dayjs.Dayjs | null, dayjs.Dayjs | null] | null>(null)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(20)

  const birlikId = user?.birlikId

  const { data, isLoading } = useQuery(
    ['toplantilar', birlikId, selectedTuru, selectedDurum, dateRange, page, pageSize],
    () =>
      toplantiApi.getAll({
        birlikId: birlikId || undefined,
        toplantiTuru: selectedTuru,
        durum: selectedDurum,
        baslangicTarihi: dateRange?.[0]?.format('YYYY-MM-DD'),
        bitisTarihi: dateRange?.[1]?.format('YYYY-MM-DD'),
        page,
        size: pageSize,
      }),
    { keepPreviousData: true }
  )

  const deleteMutation = useMutation(
    (id: number) => toplantiApi.delete(id),
    {
      onSuccess: () => {
        message.success('Toplantı silindi')
        queryClient.invalidateQueries('toplantilar')
      },
      onError: () => { message.error('Silme işlemi başarısız') },
    }
  )

  const toplantilar = useMemo(() => {
    let items = data?.data?.content || []
    if (!Array.isArray(items)) items = []
    if (searchText) {
      const s = searchText.toLowerCase()
      items = items.filter(
        (t: Toplanti) =>
          t.toplantiNo?.toLowerCase().includes(s) ||
          t.baslik?.toLowerCase().includes(s) ||
          t.yer?.toLowerCase().includes(s)
      )
    }
    return items
  }, [data, searchText])

  const totalElements = data?.data?.totalElements || toplantilar.length

  const canManage = hasRole([
    KullaniciRol.SISTEM_ADMIN,
    KullaniciRol.MERKEZ_YONETICI,
    KullaniciRol.BIRLIK_YONETICI,
  ])

  const getDurumTag = (durum: ToplantiDurumu) => {
    const config: Record<ToplantiDurumu, { color: string }> = {
      [ToplantiDurumu.PLANLANMIS]: { color: 'blue' },
      [ToplantiDurumu.DEVAM_EDIYOR]: { color: 'processing' },
      [ToplantiDurumu.TAMAMLANDI]: { color: 'green' },
      [ToplantiDurumu.IPTAL]: { color: 'red' },
      [ToplantiDurumu.ERTELENDI]: { color: 'orange' },
    }
    return <Tag color={config[durum]?.color || 'default'}>{toplantiDurumuLabels[durum]}</Tag>
  }

  const columns: ColumnsType<Toplanti> = [
    {
      title: 'Toplantı No',
      dataIndex: 'toplantiNo',
      key: 'toplantiNo',
      width: 150,
    },
    {
      title: 'Başlık',
      dataIndex: 'baslik',
      key: 'baslik',
      ellipsis: true,
    },
    {
      title: 'Tür',
      dataIndex: 'toplantiTuru',
      key: 'toplantiTuru',
      width: 160,
      render: (turu: ToplantiTuru) => (
        <Tag>{toplantiTuruLabels[turu] || turu}</Tag>
      ),
    },
    {
      title: 'Tarih',
      dataIndex: 'toplantiTarihi',
      key: 'tarih',
      width: 120,
      render: (val: string) => (val ? dayjs(val).format('DD.MM.YYYY') : '-'),
      sorter: (a, b) => dayjs(a.toplantiTarihi).unix() - dayjs(b.toplantiTarihi).unix(),
    },
    {
      title: 'Saat',
      key: 'saat',
      width: 110,
      render: (_: unknown, record: Toplanti) => {
        if (!record.baslangicSaati) return '-'
        return `${record.baslangicSaati}${record.bitisSaati ? ` - ${record.bitisSaati}` : ''}`
      },
    },
    {
      title: 'Yer',
      dataIndex: 'yer',
      key: 'yer',
      ellipsis: true,
      width: 150,
    },
    {
      title: 'Durum',
      dataIndex: 'durum',
      key: 'durum',
      width: 130,
      render: (durum: ToplantiDurumu) => getDurumTag(durum),
    },
    {
      title: 'Karar',
      dataIndex: 'kararSayisi',
      key: 'kararSayisi',
      width: 70,
      align: 'center',
    },
    {
      title: 'Katılımcı',
      dataIndex: 'katilimciSayisi',
      key: 'katilimciSayisi',
      width: 90,
      align: 'center',
    },
    {
      title: 'İşlemler',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_: unknown, record: Toplanti) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/toplantilar/${record.id}`)}
          />
          {canManage && (
            <>
              <Button
                type="link"
                size="small"
                icon={<EditOutlined />}
                onClick={() => navigate(`/toplantilar/${record.id}/duzenle`)}
              />
              <Popconfirm
                title="Bu toplantıyı silmek istediğinize emin misiniz?"
                onConfirm={() => deleteMutation.mutate(record.id)}
                okText="Evet"
                cancelText="Hayır"
              >
                <Button type="link" size="small" danger icon={<DeleteOutlined />} />
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 flex items-center gap-2">
            <CalendarOutlined /> Toplantılar
          </h1>
          <p className="text-gray-500">Toplantı ve karar yönetimi</p>
        </div>
        {canManage && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/toplantilar/yeni')}>
            Yeni Toplantı
          </Button>
        )}
      </div>

      <Card className="mb-4 shadow-sm">
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={6}>
            <Input
              placeholder="Ara (No, Başlık, Yer)..."
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
            />
          </Col>
          <Col xs={24} sm={12} md={5}>
            <Select
              placeholder="Toplantı Türü"
              style={{ width: '100%' }}
              allowClear
              value={selectedTuru}
              onChange={setSelectedTuru}
              options={Object.entries(toplantiTuruLabels).map(([k, v]) => ({ value: k, label: v }))}
            />
          </Col>
          <Col xs={24} sm={12} md={5}>
            <Select
              placeholder="Durum"
              style={{ width: '100%' }}
              allowClear
              value={selectedDurum}
              onChange={setSelectedDurum}
              options={Object.entries(toplantiDurumuLabels).map(([k, v]) => ({ value: k, label: v }))}
            />
          </Col>
          <Col xs={24} sm={12} md={8}>
            <RangePicker
              style={{ width: '100%' }}
              placeholder={['Başlangıç', 'Bitiş']}
              onChange={(dates) => setDateRange(dates as [dayjs.Dayjs | null, dayjs.Dayjs | null] | null)}
              format="DD.MM.YYYY"
            />
          </Col>
        </Row>
      </Card>

      <Card className="shadow-sm">
        <Table
          columns={columns}
          dataSource={toplantilar}
          rowKey="id"
          loading={isLoading}
          scroll={{ x: 1200 }}
          pagination={{
            current: page + 1,
            pageSize,
            total: totalElements,
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} toplantı`,
            onChange: (p, s) => {
              setPage(p - 1)
              setPageSize(s)
            },
          }}
        />
      </Card>
    </div>
  )
}

export default ToplantiListPage
