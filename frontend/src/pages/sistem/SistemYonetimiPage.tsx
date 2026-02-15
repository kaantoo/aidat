import { Card, Switch, Input, Button, message, Spin, Alert, Descriptions, Divider, Tabs, Table, Tag, Popconfirm, DatePicker, Select, Statistic, Row, Col } from 'antd';
import { 
  LockOutlined, 
  UnlockOutlined, 
  SettingOutlined,
  WarningOutlined,
  SyncOutlined,
  DatabaseOutlined,
  FileProtectOutlined,
  CloudUploadOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { sistemApi, SistemDurumu, ReadOnlyModeRequest, AuditLogParams } from '@/api/sistem';
import { useState, useEffect } from 'react';
import { useAuthStore } from '@/store/authStore';
import { KullaniciRol, AuditLog, YedekBilgi } from '@/types';
import { Navigate } from 'react-router-dom';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;

const SistemYonetimiPage: React.FC = () => {
  const { hasRole } = useAuthStore();
  const queryClient = useQueryClient();
  
  const [readOnlyMesaj, setReadOnlyMesaj] = useState('');
  const [auditPage, setAuditPage] = useState(0);
  const [auditPageSize, setAuditPageSize] = useState(20);
  const [auditFilters, setAuditFilters] = useState<AuditLogParams>({});

  // Sadece SISTEM_ADMIN erişebilir
  if (!hasRole([KullaniciRol.SISTEM_ADMIN])) {
    return <Navigate to="/dashboard" replace />;
  }

  const { data: sistemDurumu, isLoading, error, refetch } = useQuery<SistemDurumu>(
    'sistemDurumu',
    sistemApi.getDurum,
    {
      refetchInterval: 30000,
    }
  );

  // Yedekleme durumu
  const { data: yedekDurum, isLoading: yedekLoading, refetch: refetchYedek } = useQuery(
    'yedekDurum',
    sistemApi.getYedekDurumu,
    { refetchInterval: 60000 }
  );

  // Audit loglar
  const { data: auditData, isLoading: auditLoading } = useQuery(
    ['auditLogs', auditPage, auditPageSize, auditFilters],
    () => sistemApi.getAuditLogs({ ...auditFilters, page: auditPage, size: auditPageSize }),
    { keepPreviousData: true }
  );

  useEffect(() => {
    if (sistemDurumu) {
      setReadOnlyMesaj(sistemDurumu.readOnlyMesaj || '');
    }
  }, [sistemDurumu]);

  const readOnlyMutation = useMutation(
    (request: ReadOnlyModeRequest) => sistemApi.setReadOnlyMode(request),
    {
      onSuccess: (data) => {
        queryClient.invalidateQueries('sistemDurumu');
        message.success(
          data.aktif 
            ? 'Sistem salt okunur moduna alındı' 
            : 'Salt okunur mod kapatıldı'
        );
      },
      onError: () => {
        message.error('İşlem başarısız oldu');
      },
    }
  );

  const bakimModuMutation = useMutation(
    (aktif: boolean) => sistemApi.setBakimModu(aktif),
    {
      onSuccess: (data) => {
        queryClient.invalidateQueries('sistemDurumu');
        message.success(
          data.aktif 
            ? 'Bakım modu aktif edildi' 
            : 'Bakım modu kapatıldı'
        );
      },
      onError: () => {
        message.error('İşlem başarısız oldu');
      },
    }
  );

  const handleReadOnlyToggle = (checked: boolean) => {
    readOnlyMutation.mutate({
      aktif: checked,
      mesaj: readOnlyMesaj || 'Sistem şu anda salt okunur modunda. Değişiklik yapamazsınız.',
    });
  };

  const handleBakimModuToggle = (checked: boolean) => {
    bakimModuMutation.mutate(checked);
  };

  // Yedekleme mutations
  const yedekleMutation = useMutation(
    () => sistemApi.manuelYedekle(),
    {
      onSuccess: () => {
        message.success('Yedekleme başarıyla tamamlandı');
        queryClient.invalidateQueries('yedekDurum');
      },
      onError: () => { message.error('Yedekleme başarısız') },
    }
  );

  const deleteYedekMutation = useMutation(
    (dosyaAdi: string) => sistemApi.deleteYedek(dosyaAdi),
    {
      onSuccess: () => {
        message.success('Yedek dosyası silindi');
        queryClient.invalidateQueries('yedekDurum');
      },
      onError: () => { message.error('Yedek dosyası silinemedi') },
    }
  );

  // Audit log columns
  const auditColumns: ColumnsType<AuditLog> = [
    {
      title: 'Tarih',
      dataIndex: 'islemZamani',
      key: 'islemZamani',
      width: 160,
      render: (val: string) => val ? dayjs(val).format('DD.MM.YYYY HH:mm:ss') : '-',
    },
    { title: 'Kullanıcı', dataIndex: 'kullaniciAdi', key: 'kullaniciAdi', width: 140 },
    {
      title: 'İşlem',
      dataIndex: 'islemTipi',
      key: 'islemTipi',
      width: 120,
      render: (val: string) => <Tag color="blue">{val}</Tag>,
    },
    { title: 'Entity', dataIndex: 'entityTipi', key: 'entityTipi', width: 120 },
    { title: 'Açıklama', dataIndex: 'aciklama', key: 'aciklama', ellipsis: true },
    { title: 'IP', dataIndex: 'ipAdresi', key: 'ipAdresi', width: 130 },
    {
      title: 'Durum',
      dataIndex: 'basarili',
      key: 'basarili',
      width: 80,
      align: 'center',
      render: (val: boolean) =>
        val ? <CheckCircleOutlined style={{ color: '#52c41a' }} /> : <CloseCircleOutlined style={{ color: '#ff4d4f' }} />,
    },
  ];

  // Yedek listesi columns
  const yedekColumns: ColumnsType<YedekBilgi> = [
    { title: 'Dosya Adı', dataIndex: 'dosyaAdi', key: 'dosyaAdi', ellipsis: true },
    {
      title: 'Boyut',
      dataIndex: 'dosyaBoyutuFormatli',
      key: 'boyut',
      width: 100,
      render: (val: string, record: YedekBilgi) => val || `${(record.dosyaBoyutu / 1024 / 1024).toFixed(2)} MB`,
    },
    {
      title: 'Tarih',
      dataIndex: 'olusturmaZamani',
      key: 'tarih',
      width: 160,
      render: (val: string) => val ? dayjs(val).format('DD.MM.YYYY HH:mm') : '-',
    },
    { title: 'Tür', dataIndex: 'yedekTipi', key: 'tur', width: 100, render: (v: string) => <Tag>{v}</Tag> },
    {
      title: 'Durum',
      dataIndex: 'basarili',
      key: 'basarili',
      width: 80,
      align: 'center',
      render: (val: boolean) =>
        val ? <Tag color="green">Başarılı</Tag> : <Tag color="red">Başarısız</Tag>,
    },
    {
      title: 'İşlem',
      key: 'action',
      width: 80,
      render: (_: unknown, record: YedekBilgi) => (
        <Popconfirm
          title="Bu yedek dosyasını silmek istediğinize emin misiniz?"
          onConfirm={() => deleteYedekMutation.mutate(record.dosyaAdi)}
          okText="Evet"
          cancelText="Hayır"
        >
          <Button type="link" danger size="small" icon={<DeleteOutlined />} />
        </Popconfirm>
      ),
    },
  ];

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" tip="Sistem durumu yükleniyor..." />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        type="error"
        message="Hata"
        description="Sistem durumu yüklenirken bir hata oluştu."
        showIcon
      />
    );
  }

  return (
    <div className="max-w-5xl mx-auto">
      <div className="mb-6 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-semibold text-gray-800 flex items-center gap-2">
            <SettingOutlined /> Sistem Yönetimi
          </h1>
          <p className="text-gray-500">
            Sistem genelinde ayarları ve modları yönetin
          </p>
        </div>
        <Button 
          icon={<SyncOutlined />} 
          onClick={() => { refetch(); refetchYedek(); }}
        >
          Yenile
        </Button>
      </div>

      <Tabs defaultActiveKey="genel" items={[
        {
          key: 'genel',
          label: <span><SettingOutlined /> Genel Ayarlar</span>,
          children: (
            <>
              {/* Mevcut Sistem Durumu */}
              <Card title="Sistem Durumu" className="mb-6 shadow-sm">
                <Descriptions column={1}>
                  <Descriptions.Item label="Salt Okunur Mod">
                    {sistemDurumu?.readOnlyMode ? (
                      <span className="text-red-600 font-semibold"><LockOutlined className="mr-1" /> Aktif</span>
                    ) : (
                      <span className="text-green-600 font-semibold"><UnlockOutlined className="mr-1" /> Kapalı</span>
                    )}
                  </Descriptions.Item>
                  <Descriptions.Item label="Bakım Modu">
                    {sistemDurumu?.bakimModu ? (
                      <span className="text-orange-600 font-semibold"><WarningOutlined className="mr-1" /> Aktif</span>
                    ) : (
                      <span className="text-green-600 font-semibold">Kapalı</span>
                    )}
                  </Descriptions.Item>
                </Descriptions>
              </Card>

              {/* Salt Okunur Mod Kontrolü */}
              <Card
                title={<span className="flex items-center gap-2"><LockOutlined /> Salt Okunur Mod</span>}
                className="mb-6 shadow-sm"
              >
                <Alert
                  type="warning"
                  message="Dikkat"
                  description="Salt okunur mod aktif edildiğinde, SISTEM_ADMIN dışındaki tüm kullanıcılar sistemde herhangi bir değişiklik yapamayacaktır."
                  showIcon
                  className="mb-4"
                />
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="font-medium">Salt Okunur Modu {sistemDurumu?.readOnlyMode ? 'Kapat' : 'Aç'}</div>
                      <div className="text-sm text-gray-500">Tüm kullanıcıların yazma işlemlerini engeller</div>
                    </div>
                    <Switch
                      checked={sistemDurumu?.readOnlyMode || false}
                      onChange={handleReadOnlyToggle}
                      loading={readOnlyMutation.isLoading}
                      checkedChildren={<LockOutlined />}
                      unCheckedChildren={<UnlockOutlined />}
                    />
                  </div>
                  <Divider />
                  <div>
                    <div className="font-medium mb-2">Salt Okunur Mod Mesajı</div>
                    <Input.TextArea value={readOnlyMesaj} onChange={(e) => setReadOnlyMesaj(e.target.value)} placeholder="Kullanıcılara gösterilecek mesaj..." rows={3} />
                    <Button type="primary" className="mt-2" onClick={() => readOnlyMutation.mutate({ aktif: sistemDurumu?.readOnlyMode || false, mesaj: readOnlyMesaj })} loading={readOnlyMutation.isLoading} disabled={!sistemDurumu?.readOnlyMode}>
                      Mesajı Güncelle
                    </Button>
                  </div>
                </div>
              </Card>

              {/* Bakım Modu Kontrolü */}
              <Card title={<span className="flex items-center gap-2"><WarningOutlined /> Bakım Modu</span>} className="shadow-sm">
                <Alert type="info" message="Bilgi" description="Bakım modu aktif edildiğinde, sistem bakım sürecinde olduğu mesajı gösterilir." showIcon className="mb-4" />
                <div className="flex items-center justify-between">
                  <div>
                    <div className="font-medium">Bakım Modunu {sistemDurumu?.bakimModu ? 'Kapat' : 'Aç'}</div>
                    <div className="text-sm text-gray-500">Tüm kullanıcıların sisteme erişimini engeller</div>
                  </div>
                  <Switch checked={sistemDurumu?.bakimModu || false} onChange={handleBakimModuToggle} loading={bakimModuMutation.isLoading} checkedChildren={<WarningOutlined />} />
                </div>
              </Card>
            </>
          ),
        },
        {
          key: 'yedekleme',
          label: <span><DatabaseOutlined /> Yedekleme</span>,
          children: (
            <>
              <Card className="mb-6 shadow-sm">
                <Row gutter={[24, 16]}>
                  <Col xs={12} md={6}>
                    <Statistic title="Toplam Yedek" value={yedekDurum?.toplamYedekSayisi || 0} />
                  </Col>
                  <Col xs={12} md={6}>
                    <Statistic title="Toplam Boyut" value={yedekDurum?.toplamBoyut || '0 MB'} />
                  </Col>
                  <Col xs={12} md={6}>
                    <Statistic
                      title="Otomatik Yedek"
                      value={yedekDurum?.otomatikYedekAktif ? 'Aktif' : 'Kapalı'}
                      valueStyle={{ color: yedekDurum?.otomatikYedekAktif ? '#52c41a' : '#999' }}
                    />
                  </Col>
                  <Col xs={12} md={6}>
                    <Statistic
                      title="Son Yedek"
                      value={yedekDurum?.sonYedekTarihi ? dayjs(yedekDurum.sonYedekTarihi).format('DD.MM.YYYY HH:mm') : 'Yok'}
                    />
                  </Col>
                </Row>
              </Card>

              <Card
                title={<span className="flex items-center gap-2"><CloudUploadOutlined /> Manuel Yedekleme</span>}
                className="mb-6 shadow-sm"
              >
                <Alert type="info" message="Veritabanının tam yedeğini alır. İşlem birkaç dakika sürebilir." showIcon className="mb-4" />
                <Button
                  type="primary"
                  icon={<CloudUploadOutlined />}
                  onClick={() => yedekleMutation.mutate()}
                  loading={yedekleMutation.isLoading}
                  size="large"
                >
                  Şimdi Yedekle
                </Button>
              </Card>

              <Card title="Yedek Dosyaları" className="shadow-sm">
                <Table
                  columns={yedekColumns}
                  dataSource={yedekDurum?.sonYedekler || []}
                  rowKey="dosyaAdi"
                  loading={yedekLoading}
                  pagination={{ pageSize: 10 }}
                  scroll={{ x: 700 }}
                />
              </Card>
            </>
          ),
        },
        {
          key: 'audit',
          label: <span><FileProtectOutlined /> İşlem Kayıtları</span>,
          children: (
            <>
              <Card className="mb-4 shadow-sm">
                <Row gutter={[16, 16]}>
                  <Col xs={24} sm={12} md={6}>
                    <Select
                      placeholder="İşlem Tipi"
                      style={{ width: '100%' }}
                      allowClear
                      onChange={(val) => setAuditFilters((p) => ({ ...p, islemTipi: val }))}
                      options={[
                        { value: 'CREATE', label: 'Oluşturma' },
                        { value: 'UPDATE', label: 'Güncelleme' },
                        { value: 'DELETE', label: 'Silme' },
                        { value: 'LOGIN', label: 'Giriş' },
                        { value: 'LOGOUT', label: 'Çıkış' },
                        { value: 'EXPORT', label: 'Dışa Aktarma' },
                      ]}
                    />
                  </Col>
                  <Col xs={24} sm={12} md={6}>
                    <Select
                      placeholder="Entity Tipi"
                      style={{ width: '100%' }}
                      allowClear
                      onChange={(val) => setAuditFilters((p) => ({ ...p, entityTipi: val }))}
                      options={[
                        { value: 'Uye', label: 'Üye' },
                        { value: 'Aidat', label: 'Aidat' },
                        { value: 'Birlik', label: 'Birlik' },
                        { value: 'Kullanici', label: 'Kullanıcı' },
                        { value: 'Toplanti', label: 'Toplantı' },
                        { value: 'GelirGider', label: 'Gelir/Gider' },
                      ]}
                    />
                  </Col>
                  <Col xs={24} sm={12} md={6}>
                    <Select
                      placeholder="Durum"
                      style={{ width: '100%' }}
                      allowClear
                      onChange={(val) => setAuditFilters((p) => ({ ...p, basarili: val }))}
                      options={[
                        { value: true, label: 'Başarılı' },
                        { value: false, label: 'Başarısız' },
                      ]}
                    />
                  </Col>
                  <Col xs={24} sm={12} md={6}>
                    <RangePicker
                      style={{ width: '100%' }}
                      placeholder={['Başlangıç', 'Bitiş']}
                      format="DD.MM.YYYY"
                      onChange={(dates) => {
                        setAuditFilters((p) => ({
                          ...p,
                          baslangicTarihi: dates?.[0]?.format('YYYY-MM-DD'),
                          bitisTarihi: dates?.[1]?.format('YYYY-MM-DD'),
                        }));
                      }}
                    />
                  </Col>
                </Row>
              </Card>

              <Card className="shadow-sm">
                <Table
                  columns={auditColumns}
                  dataSource={auditData?.content || []}
                  rowKey="id"
                  loading={auditLoading}
                  scroll={{ x: 900 }}
                  pagination={{
                    current: auditPage + 1,
                    pageSize: auditPageSize,
                    total: auditData?.totalElements || 0,
                    showSizeChanger: true,
                    showTotal: (total) => `Toplam ${total} kayıt`,
                    onChange: (p, s) => { setAuditPage(p - 1); setAuditPageSize(s); },
                  }}
                />
              </Card>
            </>
          ),
        },
      ]} />
    </div>
  );
};

export default SistemYonetimiPage;
