import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table,
  Card,
  Button,
  Input,
  Space,
  Tag,
  Select,
  Popconfirm,
  Tooltip,
  Row,
  Col,
  App,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
  ExportOutlined,
} from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { uyeApi } from '@/api/uye'
import { Uye, UyeDurum, UyelikTipi, UyeFilter } from '@/types'
import { useAuthStore } from '@/store/authStore'
import { KullaniciRol } from '@/types'

const UyeListPage: React.FC = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { hasRole } = useAuthStore()
  const { message } = App.useApp()
  
  const [pagination, setPagination] = useState({ current: 1, pageSize: 20 })
  const [filters, setFilters] = useState<UyeFilter>({})
  const [searchTerm, setSearchTerm] = useState('')

  const { data, isLoading, refetch } = useQuery(
    ['uyeler', pagination, filters, searchTerm],
    () => {
      if (searchTerm) {
        return uyeApi.search(searchTerm, pagination.current - 1, pagination.pageSize)
      }
      return uyeApi.getAll(pagination.current - 1, pagination.pageSize, filters)
    }
  )

  const deleteMutation = useMutation(uyeApi.delete, {
    onSuccess: () => {
      message.success('Üye başarıyla silindi')
      queryClient.invalidateQueries('uyeler')
    },
    onError: () => {
      message.error('Üye silinirken hata oluştu')
    },
  })

  const handleTableChange = (paginationConfig: TablePaginationConfig) => {
    setPagination({
      current: paginationConfig.current || 1,
      pageSize: paginationConfig.pageSize || 20,
    })
  }

  const handleSearch = (value: string) => {
    setSearchTerm(value)
    setPagination({ ...pagination, current: 1 })
  }

  const getDurumTag = (durum: UyeDurum) => {
    const config: Record<UyeDurum, { color: string; text: string }> = {
      [UyeDurum.AKTIF]: { color: 'green', text: 'Aktif' },
      [UyeDurum.PASIF]: { color: 'default', text: 'Pasif' },
      [UyeDurum.ASKIYA_ALINMIS]: { color: 'orange', text: 'Askıya Alınmış' },
      [UyeDurum.IHRAC_EDILMIS]: { color: 'red', text: 'İhraç Edilmiş' },
    }
    const durumConfig = config[durum]
    if (!durumConfig) {
      return <Tag color="default">{durum || 'Bilinmiyor'}</Tag>
    }
    const { color, text } = durumConfig
    return <Tag color={color}>{text}</Tag>
  }

  const getUyelikTipiTag = (tip: UyelikTipi) => {
    return (
      <Tag color={tip === UyelikTipi.GERCEK_KISI ? 'blue' : 'purple'}>
        {tip === UyelikTipi.GERCEK_KISI ? 'Gerçek Kişi' : 'Tüzel Kişi'}
      </Tag>
    )
  }

  const columns: ColumnsType<Uye> = [
    {
      title: 'Üye No',
      dataIndex: 'uyeNo',
      key: 'uyeNo',
      width: 140,
      fixed: 'left',
    },
    {
      title: 'Ad Soyad',
      key: 'adSoyad',
      render: (_, record) => `${record.ad} ${record.soyad}`,
    },
    {
      title: 'TC/Vergi No',
      key: 'kimlikNo',
      render: (_, record) => record.tcKimlikNo || record.vergiNo || '-',
      width: 140,
    },
    {
      title: 'Üyelik Tipi',
      dataIndex: 'uyelikTipi',
      key: 'uyelikTipi',
      width: 120,
      render: (tip: UyelikTipi) => getUyelikTipiTag(tip),
    },
    {
      title: 'Telefon',
      dataIndex: 'cepTelefon',
      key: 'cepTelefon',
      width: 130,
    },
    {
      title: 'Birlik',
      dataIndex: 'birlikAdi',
      key: 'birlik',
      ellipsis: true,
    },
    {
      title: 'Durum',
      dataIndex: 'uyeDurum',
      key: 'uyeDurum',
      width: 120,
      render: (durum: UyeDurum) => getDurumTag(durum),
    },
    {
      title: 'İşlemler',
      key: 'actions',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Tooltip title="Detay">
            <Button
              type="text"
              icon={<EyeOutlined />}
              onClick={() => navigate(`/uyeler/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="Düzenle">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => navigate(`/uyeler/${record.id}/duzenle`)}
            />
          </Tooltip>
          {hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI]) && (
            <Popconfirm
              title="Bu üyeyi silmek istediğinize emin misiniz?"
              onConfirm={() => deleteMutation.mutate(record.id)}
              okText="Evet"
              cancelText="Hayır"
            >
              <Tooltip title="Sil">
                <Button type="text" danger icon={<DeleteOutlined />} />
              </Tooltip>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Üye Yönetimi</h1>
        <Space>
          <Button icon={<ExportOutlined />}>Excel İndir</Button>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => navigate('/uyeler/yeni')}
            style={{ background: '#b91c1c' }}
          >
            Yeni Üye
          </Button>
        </Space>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <Row gutter={[16, 16]} className="mb-4">
          <Col xs={24} sm={12} md={8} lg={6}>
            <Input.Search
              placeholder="Üye ara (Ad, Soyad, TC, Üye No)"
              allowClear
              onSearch={handleSearch}
              prefix={<SearchOutlined className="text-gray-400" />}
            />
          </Col>
          <Col xs={24} sm={12} md={8} lg={4}>
            <Select
              placeholder="Durum"
              allowClear
              style={{ width: '100%' }}
              onChange={(value) => setFilters({ ...filters, durum: value })}
            >
              <Select.Option value={UyeDurum.AKTIF}>Aktif</Select.Option>
              <Select.Option value={UyeDurum.PASIF}>Pasif</Select.Option>
              <Select.Option value={UyeDurum.ASKIYA_ALINMIS}>Askıya Alınmış</Select.Option>
              <Select.Option value={UyeDurum.IHRAC_EDILMIS}>İhraç Edilmiş</Select.Option>
            </Select>
          </Col>
          <Col xs={24} sm={12} md={8} lg={4}>
            <Select
              placeholder="Üyelik Tipi"
              allowClear
              style={{ width: '100%' }}
              onChange={(value) => setFilters({ ...filters, uyelikTipi: value })}
            >
              <Select.Option value={UyelikTipi.GERCEK_KISI}>Gerçek Kişi</Select.Option>
              <Select.Option value={UyelikTipi.TUZEL_KISI}>Tüzel Kişi</Select.Option>
            </Select>
          </Col>
          <Col flex="auto" className="text-right">
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Yenile
            </Button>
          </Col>
        </Row>

        <Table
          columns={columns}
          dataSource={data?.data?.content}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: data?.data.totalElements,
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} üye`,
          }}
          onChange={handleTableChange}
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>
    </div>
  )
}

export default UyeListPage
