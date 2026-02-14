import { useState } from 'react'
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Modal,
  Input,
  Avatar,
  Dropdown,
  App,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined,
  UserOutlined,
  MoreOutlined,
  SearchOutlined,
  KeyOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import type { MenuProps } from 'antd'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useNavigate } from 'react-router-dom'
import { kullaniciApi } from '@/api/kullanici'
import { Kullanici, KullaniciRol, KullaniciDurum } from '@/types'
import dayjs from 'dayjs'

const KullaniciListPage: React.FC = () => {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { message } = App.useApp()
  const [searchTerm, setSearchTerm] = useState('')
  const [resetPasswordModal, setResetPasswordModal] = useState<{
    visible: boolean
    userId?: number
    userName?: string
  }>({ visible: false })
  const [newPassword, setNewPassword] = useState('')

  const { data, isLoading } = useQuery(['kullanicilar', searchTerm], () =>
    searchTerm ? kullaniciApi.search(searchTerm) : kullaniciApi.getAll()
  )

  const updateDurumMutation = useMutation(
    ({ id, durum }: { id: number; durum: KullaniciDurum }) =>
      kullaniciApi.updateDurum(id, durum),
    {
      onSuccess: () => {
        message.success('Kullanıcı durumu güncellendi')
        queryClient.invalidateQueries('kullanicilar')
      },
      onError: () => { message.error('İşlem başarısız') },
    }
  )

  const unlockMutation = useMutation((id: number) => kullaniciApi.unlockAccount(id), {
    onSuccess: () => {
      message.success('Kullanıcı hesabı açıldı')
      queryClient.invalidateQueries('kullanicilar')
    },
    onError: () => { message.error('İşlem başarısız') },
  })

  const resetPasswordMutation = useMutation(
    ({ id, password }: { id: number; password: string }) =>
      kullaniciApi.resetPassword(id, password),
    {
      onSuccess: () => {
        message.success('Şifre başarıyla sıfırlandı')
        setResetPasswordModal({ visible: false })
        setNewPassword('')
      },
      onError: () => { message.error('Şifre sıfırlama başarısız') },
    }
  )

  const deleteMutation = useMutation((id: number) => kullaniciApi.delete(id), {
    onSuccess: () => {
      message.success('Kullanıcı silindi')
      queryClient.invalidateQueries('kullanicilar')
    },
    onError: () => { message.error('Silme işlemi başarısız') },
  })

  const getRolTag = (rol: KullaniciRol) => {
    const config: Record<KullaniciRol, { color: string; text: string }> = {
      [KullaniciRol.SISTEM_ADMIN]: { color: 'red', text: 'Sistem Admin' },
      [KullaniciRol.MERKEZ_YONETICI]: { color: 'purple', text: 'Merkez Yönetici' },
      [KullaniciRol.BIRLIK_YONETICI]: { color: 'blue', text: 'Birlik Yönetici' },
      [KullaniciRol.BIRLIK_PERSONEL]: { color: 'cyan', text: 'Birlik Personel' },
      [KullaniciRol.MUHASEBE_SORUMLU]: { color: 'orange', text: 'Muhasebe Sorumlu' },
      [KullaniciRol.GOZLEMCI]: { color: 'default', text: 'Gözlemci' },
    }
    const { color, text } = config[rol]
    return <Tag color={color}>{text}</Tag>
  }

  const getDurumTag = (durum: KullaniciDurum) => {
    const config: Record<KullaniciDurum, { color: string; text: string }> = {
      [KullaniciDurum.AKTIF]: { color: 'green', text: 'Aktif' },
      [KullaniciDurum.PASIF]: { color: 'default', text: 'Pasif' },
      [KullaniciDurum.KILITLI]: { color: 'red', text: 'Kilitli' },
    }
    const { color, text } = config[durum]
    return <Tag color={color}>{text}</Tag>
  }

  const getActionMenuItems = (record: Kullanici): MenuProps['items'] => [
    {
      key: 'edit',
      label: 'Düzenle',
      icon: <EditOutlined />,
      onClick: () => navigate(`/kullanicilar/${record.id}/duzenle`),
    },
    {
      key: 'resetPassword',
      label: 'Şifre Sıfırla',
      icon: <KeyOutlined />,
      onClick: () =>
        setResetPasswordModal({
          visible: true,
          userId: record.id,
          userName: record.kullaniciAdi,
        }),
    },
    { type: 'divider' as const },
    record.durum === KullaniciDurum.AKTIF
      ? {
          key: 'deactivate',
          label: 'Pasif Yap',
          icon: <LockOutlined />,
          onClick: () =>
            updateDurumMutation.mutate({ id: record.id, durum: KullaniciDurum.PASIF }),
        }
      : {
          key: 'activate',
          label: 'Aktif Yap',
          icon: <UnlockOutlined />,
          onClick: () =>
            updateDurumMutation.mutate({ id: record.id, durum: KullaniciDurum.AKTIF }),
        },
    record.durum === KullaniciDurum.KILITLI
      ? {
          key: 'unlock',
          label: 'Kilidi Aç',
          icon: <UnlockOutlined />,
          onClick: () => unlockMutation.mutate(record.id),
        }
      : null,
    { type: 'divider' as const },
    {
      key: 'delete',
      label: 'Sil',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: () => {
        Modal.confirm({
          title: 'Kullanıcıyı silmek istediğinize emin misiniz?',
          content: `"${record.ad} ${record.soyad}" kullanıcısı kalıcı olarak silinecek.`,
          okText: 'Evet, Sil',
          okType: 'danger',
          cancelText: 'İptal',
          onOk: () => deleteMutation.mutate(record.id),
        })
      },
    },
  ].filter(Boolean)

  const columns: ColumnsType<Kullanici> = [
    {
      title: 'Kullanıcı',
      key: 'kullanici',
      width: 250,
      render: (_, record) => (
        <Space>
          <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#b91c1c' }}>
            {record.ad.charAt(0)}
          </Avatar>
          <div>
            <div className="font-medium">
              {record.ad} {record.soyad}
            </div>
            <div className="text-xs text-gray-500">@{record.kullaniciAdi}</div>
          </div>
        </Space>
      ),
    },
    {
      title: 'E-posta',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Telefon',
      dataIndex: 'telefon',
      key: 'telefon',
      render: (tel) => tel || '-',
    },
    {
      title: 'Birlik',
      key: 'birlik',
      render: (_, record) => record.birlikAdi || 'Merkez',
    },
    {
      title: 'Rol',
      dataIndex: 'rol',
      key: 'rol',
      render: (rol: KullaniciRol) => getRolTag(rol),
    },
    {
      title: 'Durum',
      dataIndex: 'durum',
      key: 'durum',
      render: (durum: KullaniciDurum) => getDurumTag(durum),
    },
    {
      title: 'Son Giriş',
      dataIndex: 'sonGirisTarihi',
      key: 'sonGirisTarihi',
      width: 140,
      render: (date) => (date ? dayjs(date).format('DD.MM.YYYY HH:mm') : 'Henüz giriş yok'),
    },
    {
      title: '2FA',
      dataIndex: 'twoFactorEnabled',
      key: 'twoFactorEnabled',
      width: 60,
      align: 'center',
      render: (enabled) =>
        enabled ? (
          <Tag color="green">Açık</Tag>
        ) : (
          <Tag color="default">Kapalı</Tag>
        ),
    },
    {
      title: '',
      key: 'actions',
      width: 50,
      render: (_, record) => (
        <Dropdown menu={{ items: getActionMenuItems(record) }} trigger={['click']}>
          <Button type="text" icon={<MoreOutlined />} />
        </Dropdown>
      ),
    },
  ]

  return (
    <div>
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-xl font-semibold">Kullanıcı Yönetimi</h1>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={() => navigate('/kullanicilar/yeni')}
          style={{ background: '#b91c1c' }}
        >
          Yeni Kullanıcı
        </Button>
      </div>

      <Card variant="borderless" className="shadow-sm mb-4">
        <Input.Search
          placeholder="Kullanıcı ara (ad, kullanıcı adı, e-posta)..."
          allowClear
          enterButton={<SearchOutlined />}
          size="large"
          onSearch={setSearchTerm}
          style={{ maxWidth: 400 }}
        />
      </Card>

      <Card variant="borderless" className="shadow-sm">
        <Table
          columns={columns}
          dataSource={data?.data?.content}
          rowKey="id"
          loading={isLoading}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Toplam ${total} kullanıcı`,
          }}
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>

      {/* Şifre Sıfırlama Modal */}
      <Modal
        title={`Şifre Sıfırla - ${resetPasswordModal.userName}`}
        open={resetPasswordModal.visible}
        onCancel={() => {
          setResetPasswordModal({ visible: false })
          setNewPassword('')
        }}
        onOk={() => {
          if (!newPassword || newPassword.length < 6) {
            message.error('Şifre en az 6 karakter olmalıdır')
            return
          }
          if (resetPasswordModal.userId) {
            resetPasswordMutation.mutate({
              id: resetPasswordModal.userId,
              password: newPassword,
            })
          }
        }}
        okText="Şifreyi Sıfırla"
        okButtonProps={{
          loading: resetPasswordMutation.isLoading,
          style: { background: '#b91c1c' },
        }}
        cancelText="İptal"
      >
        <div className="mb-4">
          <p className="text-gray-600 mb-2">Yeni şifreyi girin:</p>
          <Input.Password
            placeholder="Yeni şifre"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            minLength={6}
          />
          <p className="text-xs text-gray-400 mt-1">En az 6 karakter olmalıdır</p>
        </div>
      </Modal>
    </div>
  )
}

export default KullaniciListPage
