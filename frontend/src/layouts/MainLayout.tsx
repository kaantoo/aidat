import { useState, useMemo } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Dropdown, Avatar, Space, Badge, theme, Popover, List, Empty, Typography, Button, Spin } from 'antd'
import {
  HomeOutlined,
  BankOutlined,
  TeamOutlined,
  DollarOutlined,
  WalletOutlined,
  FileOutlined,
  UserOutlined,
  BarChartOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  BellOutlined,
  SettingOutlined,
  LogoutOutlined,
  CheckOutlined,
  CalendarOutlined,
} from '@ant-design/icons'
import type { MenuProps } from 'antd'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { useAuthStore } from '@/store/authStore'
import { KullaniciRol } from '@/types'
import { bildirimApi, BildirimDTO, BildirimOzetDTO } from '@/api/bildirim'

const { Header, Sider, Content } = Layout
const { Text } = Typography

const MainLayout: React.FC = () => {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout, hasRole } = useAuthStore()
  const queryClient = useQueryClient()
  const {
    token: { colorBgContainer, borderRadiusLG },
  } = theme.useToken()

  // Bildirim özeti API çağrısı
  const { data: bildirimOzeti, isLoading: bildirimLoading } = useQuery<BildirimOzetDTO>({
    queryKey: ['bildirimOzeti'],
    queryFn: async () => {
      const response = await bildirimApi.getOzet()
      return response.data
    },
    refetchInterval: 30000, // Her 30 saniyede bir yenile
    staleTime: 10000,
  })

  const unreadCount = bildirimOzeti?.okunmamisSayisi || 0
  const notifications = bildirimOzeti?.sonBildirimler || []

  // Okundu işaretle mutation
  const markAsReadMutation = useMutation({
    mutationFn: (id: number) => bildirimApi.markAsRead(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bildirimOzeti'] })
    },
  })

  // Tümünü okundu işaretle mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: () => bildirimApi.markAllAsRead(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['bildirimOzeti'] })
    },
  })

  const markAllAsRead = () => {
    markAllAsReadMutation.mutate()
  }

  const markAsRead = (id: number) => {
    markAsReadMutation.mutate(id)
  }

  const notificationContent = (
    <div style={{ width: 320 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', borderBottom: '1px solid #f0f0f0' }}>
        <Text strong>Bildirimler</Text>
        {unreadCount > 0 && (
          <Button type="link" size="small" onClick={markAllAsRead} icon={<CheckOutlined />} loading={markAllAsReadMutation.isLoading}>
            Tümünü okundu işaretle
          </Button>
        )}
      </div>
      {bildirimLoading ? (
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Spin />
        </div>
      ) : notifications.length > 0 ? (
        <List
          dataSource={notifications}
          renderItem={(item: BildirimDTO) => (
            <List.Item 
              style={{ 
                padding: '12px', 
                cursor: 'pointer',
                backgroundColor: item.okundu ? 'transparent' : '#f6ffed'
              }}
              onClick={() => !item.okundu && markAsRead(item.id)}
            >
              <List.Item.Meta
                title={<Text strong={!item.okundu}>{item.baslik}</Text>}
                description={
                  <>
                    <Text type="secondary" style={{ fontSize: 12 }}>{item.mesaj}</Text>
                    <br />
                    <Text type="secondary" style={{ fontSize: 11 }}>{item.zamanOnce}</Text>
                  </>
                }
              />
            </List.Item>
          )}
          style={{ maxHeight: 300, overflow: 'auto' }}
        />
      ) : (
        <Empty description="Bildirim yok" style={{ padding: 24 }} />
      )}
      <div style={{ textAlign: 'center', padding: '8px', borderTop: '1px solid #f0f0f0' }}>
        <Button type="link" onClick={() => navigate('/bildirimler')}>Tüm bildirimleri gör</Button>
      </div>
    </div>
  )

  const menuItems: MenuProps['items'] = useMemo(() => {
    const items: MenuProps['items'] = [
      {
        key: '/',
        icon: <HomeOutlined />,
        label: 'Ana Sayfa',
      },
    ]

    // Birlik yönetimi - sadece sistem admin ve merkez yönetici
    if (hasRole([KullaniciRol.SISTEM_ADMIN, KullaniciRol.MERKEZ_YONETICI])) {
      items.push({
        key: '/birlikler',
        icon: <BankOutlined />,
        label: 'Birlik Yönetimi',
      })
    }

    // Üye yönetimi
    items.push({
      key: '/uyeler',
      icon: <TeamOutlined />,
      label: 'Üye Yönetimi',
    })

    // Aidat yönetimi
    items.push({
      key: 'aidat-menu',
      icon: <DollarOutlined />,
      label: 'Aidat Yönetimi',
      children: [
        {
          key: '/aidatlar',
          label: 'Aidat Listesi',
        },
        {
          key: '/aidatlar/donemler',
          label: 'Dönem Tanımları',
        },
        {
          key: '/aidatlar/tahsilat',
          label: 'Tahsilat İşlemleri',
        },
      ],
    })

    // Gelir Gider
    items.push({
      key: 'gelir-gider-menu',
      icon: <WalletOutlined />,
      label: 'Gelir / Gider',
      children: [
        {
          key: '/gelir-gider',
          label: 'Gelir Gider Listesi',
        },
        {
          key: '/beklenen-gelir',
          label: 'Beklenen Gelirler',
        },
      ],
    })

    // Belge Yönetimi
    items.push({
      key: '/belgeler',
      icon: <FileOutlined />,
      label: 'Belgeler',
    })

    // Toplantı Yönetimi
    items.push({
      key: '/toplantilar',
      icon: <CalendarOutlined />,
      label: 'Toplantılar',
    })

    // Kullanıcı yönetimi - sadece yöneticiler
    if (
      hasRole([
        KullaniciRol.SISTEM_ADMIN,
        KullaniciRol.MERKEZ_YONETICI,
        KullaniciRol.BIRLIK_YONETICI,
      ])
    ) {
      items.push({
        key: '/kullanicilar',
        icon: <UserOutlined />,
        label: 'Kullanıcı Yönetimi',
      })
    }

    // Raporlar
    items.push({
      key: '/raporlar',
      icon: <BarChartOutlined />,
      label: 'Raporlar',
    })

    // Sistem Yönetimi - sadece SISTEM_ADMIN
    if (user?.rol === KullaniciRol.SISTEM_ADMIN) {
      items.push({
        key: '/sistem-yonetimi',
        icon: <SettingOutlined />,
        label: 'Sistem Yönetimi',
      })
    }

    return items
  }, [hasRole, user])

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: 'Profilim',
      onClick: () => navigate('/profil'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Ayarlar',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Çıkış Yap',
      onClick: async () => {
        await logout()
        navigate('/login')
      },
    },
  ]

  const getRolLabel = (rol: KullaniciRol): string => {
    const labels: Record<KullaniciRol, string> = {
      [KullaniciRol.SISTEM_ADMIN]: 'Sistem Admin',
      [KullaniciRol.MERKEZ_YONETICI]: 'Merkez Yönetici',
      [KullaniciRol.BIRLIK_YONETICI]: 'Birlik Yönetici',
      [KullaniciRol.BIRLIK_PERSONEL]: 'Birlik Personel',
      [KullaniciRol.MUHASEBE_SORUMLU]: 'Muhasebe Sorumlu',
      [KullaniciRol.GOZLEMCI]: 'Gözlemci',
    }
    return labels[rol] || rol
  }

  const getSelectedKeys = (): string[] => {
    const path = location.pathname
    if (path.startsWith('/aidatlar')) {
      return [path]
    }
    return [path]
  }

  const getOpenKeys = (): string[] => {
    if (location.pathname.startsWith('/aidatlar')) {
      return ['aidat-menu']
    }
    return []
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        theme="dark"
        style={{
          overflow: 'auto',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          bottom: 0,
          background: 'linear-gradient(180deg, #b91c1c 0%, #991b1b 100%)',
        }}
      >
        <div
          style={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            borderBottom: '1px solid rgba(255,255,255,0.1)',
          }}
        >
          {collapsed ? (
            <span style={{ color: 'white', fontWeight: 'bold', fontSize: 20 }}>TB</span>
          ) : (
            <span style={{ color: 'white', fontWeight: 'bold', fontSize: 16 }}>
              TÜKETBİR
            </span>
          )}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={({ key }) => {
            if (!key.includes('-menu')) {
              navigate(key)
            }
          }}
          style={{
            background: 'transparent',
            borderRight: 0,
          }}
        />
      </Sider>

      <Layout style={{ marginLeft: collapsed ? 80 : 200, transition: 'all 0.2s' }}>
        <Header
          style={{
            padding: '0 24px',
            background: colorBgContainer,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
            position: 'sticky',
            top: 0,
            zIndex: 100,
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center' }}>
            {collapsed ? (
              <MenuUnfoldOutlined
                onClick={() => setCollapsed(false)}
                style={{ fontSize: 18, cursor: 'pointer' }}
              />
            ) : (
              <MenuFoldOutlined
                onClick={() => setCollapsed(true)}
                style={{ fontSize: 18, cursor: 'pointer' }}
              />
            )}
          </div>

          <Space size={24}>
            <Popover 
              content={notificationContent} 
              trigger="click" 
              placement="bottomRight"
              arrow={{ pointAtCenter: true }}
            >
              <Badge count={unreadCount} size="small">
                <BellOutlined style={{ fontSize: 18, cursor: 'pointer' }} />
              </Badge>
            </Popover>

            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar
                  style={{ backgroundColor: '#b91c1c' }}
                  icon={<UserOutlined />}
                />
                <div style={{ lineHeight: '1.2' }}>
                  <div style={{ fontWeight: 500 }}>{user?.adSoyad}</div>
                  <div style={{ fontSize: 12, color: '#888' }}>
                    {user && getRolLabel(user.rol)}
                  </div>
                </div>
              </Space>
            </Dropdown>
          </Space>
        </Header>

        <Content
          style={{
            margin: 24,
            padding: 24,
            background: colorBgContainer,
            borderRadius: borderRadiusLG,
            minHeight: 'calc(100vh - 64px - 48px)',
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default MainLayout
