import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table,
  Card,
  Button,
  Input,
  Space,
  Tag,
  Popconfirm,
  App,
  Tooltip,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  ReloadOutlined,
} from '@ant-design/icons'
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table'
import { useBirlikStore } from '@/store/birlikStore'
import { Birlik, BirlikTipi } from '@/types'

const BirlikListPage: React.FC = () => {
  const navigate = useNavigate()
  const [searchTerm, setSearchTerm] = useState('')
  const { message } = App.useApp()
  const {
    birlikler,
    isLoading,
    totalElements,
    currentPage,
    pageSize,
    fetchBirlikler,
    deleteBirlik,
  } = useBirlikStore()

  useEffect(() => {
    fetchBirlikler()
  }, [fetchBirlikler])

  // Client-side filtreleme
  const filteredBirlikler = searchTerm.trim()
    ? birlikler.filter(
        (b) =>
          b.birlikAdi?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          b.birlikKodu?.toLowerCase().includes(searchTerm.toLowerCase()) ||
          b.ilKodu?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : birlikler

  const handleSearch = (value: string) => {
    setSearchTerm(value)
  }

  const handleTableChange = (pagination: TablePaginationConfig) => {
    fetchBirlikler((pagination.current || 1) - 1, pagination.pageSize)
  }

  const handleDelete = async (id: number) => {
    try {
      await deleteBirlik(id)
      message.success('Birlik başarıyla silindi')
    } catch {
      message.error('Birlik silinirken hata oluştu')
    }
  }

  const getBirlikTipiTag = (tip: BirlikTipi) => {
    const colors: Record<BirlikTipi, string> = {
      [BirlikTipi.MERKEZ]: 'blue',
      [BirlikTipi.ALT_BIRLIK]: 'green',
    }
    const labels: Record<BirlikTipi, string> = {
      [BirlikTipi.MERKEZ]: 'Merkez Birliği',
      [BirlikTipi.ALT_BIRLIK]: 'Alt Birlik',
    }
    return <Tag color={colors[tip]}>{labels[tip]}</Tag>
  }

  const columns: ColumnsType<Birlik> = [
    {
      title: 'Birlik Kodu',
      dataIndex: 'birlikKodu',
      key: 'birlikKodu',
      width: 120,
      sorter: true,
    },
    {
      title: 'Birlik Adı',
      dataIndex: 'birlikAdi',
      key: 'birlikAdi',
      sorter: true,
    },
    {
      title: 'Tipi',
      dataIndex: 'birlikTipi',
      key: 'birlikTipi',
      width: 140,
      render: (tip: BirlikTipi) => getBirlikTipiTag(tip),
    },
    {
      title: 'İl',
      dataIndex: 'ilKodu',
      key: 'ilKodu',
      width: 100,
    },
    {
      title: 'Telefon',
      dataIndex: 'telefon',
      key: 'telefon',
      width: 140,
    },
    {
      title: 'Durum',
      dataIndex: 'aktif',
      key: 'aktif',
      width: 100,
      render: (aktif: boolean) => (
        <Tag color={aktif ? 'green' : 'red'}>{aktif ? 'Aktif' : 'Pasif'}</Tag>
      ),
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
              onClick={() => navigate(`/birlikler/${record.id}`)}
            />
          </Tooltip>
          <Tooltip title="Düzenle">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => navigate(`/birlikler/${record.id}/duzenle`)}
            />
          </Tooltip>
          <Popconfirm
            title="Bu birliği silmek istediğinize emin misiniz?"
            onConfirm={() => handleDelete(record.id)}
            okText="Evet"
            cancelText="Hayır"
          >
            <Tooltip title="Sil">
              <Button type="text" danger icon={<DeleteOutlined />} />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Birlik Yönetimi</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/birlikler/yeni')}
          style={{ background: '#b91c1c' }}
        >
          Yeni Birlik
        </Button>
      </div>

      <Card variant="borderless" className="shadow-sm">
        <div className="flex justify-between items-center mb-4">
          <Input.Search
            placeholder="Birlik ara..."
            allowClear
            onSearch={handleSearch}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{ width: 300 }}
            prefix={<SearchOutlined className="text-gray-400" />}
          />
          <Button
            icon={<ReloadOutlined />}
            onClick={() => fetchBirlikler(currentPage, pageSize)}
          >
            Yenile
          </Button>
        </div>

        <Table
          columns={columns}
          dataSource={filteredBirlikler}
          rowKey="id"
          loading={isLoading}
          pagination={{
            current: (currentPage || 0) + 1,
            pageSize: pageSize || 20,
            total: searchTerm.trim() ? filteredBirlikler.length : (totalElements || 0),
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} birlik`,
          }}
          onChange={handleTableChange}
          scroll={{ x: 1000 }}
          size="middle"
        />
      </Card>
    </div>
  )
}

export default BirlikListPage
