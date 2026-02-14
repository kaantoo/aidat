import { Card, Switch, Input, Button, message, Spin, Alert, Descriptions, Divider } from 'antd';
import { 
  LockOutlined, 
  UnlockOutlined, 
  SettingOutlined,
  WarningOutlined,
  SyncOutlined
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { sistemApi, SistemDurumu, ReadOnlyModeRequest } from '@/api/sistem';
import { useState, useEffect } from 'react';
import { useAuthStore } from '@/store/authStore';
import { KullaniciRol } from '@/types';
import { Navigate } from 'react-router-dom';

const SistemYonetimiPage: React.FC = () => {
  const { hasRole } = useAuthStore();
  const queryClient = useQueryClient();
  
  const [readOnlyMesaj, setReadOnlyMesaj] = useState('');

  // Sadece SISTEM_ADMIN erişebilir
  if (!hasRole([KullaniciRol.SISTEM_ADMIN])) {
    return <Navigate to="/dashboard" replace />;
  }

  const { data: sistemDurumu, isLoading, error, refetch } = useQuery<SistemDurumu>(
    'sistemDurumu',
    sistemApi.getDurum,
    {
      refetchInterval: 30000, // 30 saniyede bir güncelle
    }
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
    <div className="max-w-4xl mx-auto">
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
          onClick={() => refetch()}
        >
          Yenile
        </Button>
      </div>

      {/* Mevcut Sistem Durumu */}
      <Card 
        title="Sistem Durumu" 
        className="mb-6 shadow-sm"
      >
        <Descriptions column={1}>
          <Descriptions.Item label="Salt Okunur Mod">
            {sistemDurumu?.readOnlyMode ? (
              <span className="text-red-600 font-semibold">
                <LockOutlined className="mr-1" /> Aktif
              </span>
            ) : (
              <span className="text-green-600 font-semibold">
                <UnlockOutlined className="mr-1" /> Kapalı
              </span>
            )}
          </Descriptions.Item>
          <Descriptions.Item label="Bakım Modu">
            {sistemDurumu?.bakimModu ? (
              <span className="text-orange-600 font-semibold">
                <WarningOutlined className="mr-1" /> Aktif
              </span>
            ) : (
              <span className="text-green-600 font-semibold">Kapalı</span>
            )}
          </Descriptions.Item>
        </Descriptions>
      </Card>

      {/* Salt Okunur Mod Kontrolü */}
      <Card 
        title={
          <span className="flex items-center gap-2">
            <LockOutlined /> Salt Okunur Mod
          </span>
        }
        className="mb-6 shadow-sm"
      >
        <Alert
          type="warning"
          message="Dikkat"
          description="Salt okunur mod aktif edildiğinde, SISTEM_ADMIN dışındaki tüm kullanıcılar sistemde herhangi bir değişiklik yapamayacaktır. Yalnızca görüntüleme işlemleri mümkün olacaktır."
          showIcon
          className="mb-4"
        />
        
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="font-medium">Salt Okunur Modu {sistemDurumu?.readOnlyMode ? 'Kapat' : 'Aç'}</div>
              <div className="text-sm text-gray-500">
                Tüm kullanıcıların yazma işlemlerini engeller
              </div>
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
            <Input.TextArea
              value={readOnlyMesaj}
              onChange={(e) => setReadOnlyMesaj(e.target.value)}
              placeholder="Kullanıcılara gösterilecek mesaj..."
              rows={3}
            />
            <Button
              type="primary"
              className="mt-2"
              onClick={() => {
                readOnlyMutation.mutate({
                  aktif: sistemDurumu?.readOnlyMode || false,
                  mesaj: readOnlyMesaj,
                });
              }}
              loading={readOnlyMutation.isLoading}
              disabled={!sistemDurumu?.readOnlyMode}
            >
              Mesajı Güncelle
            </Button>
          </div>
        </div>
      </Card>

      {/* Bakım Modu Kontrolü */}
      <Card 
        title={
          <span className="flex items-center gap-2">
            <WarningOutlined /> Bakım Modu
          </span>
        }
        className="shadow-sm"
      >
        <Alert
          type="info"
          message="Bilgi"
          description="Bakım modu aktif edildiğinde, sistem bakım sürecinde olduğu mesajı gösterilir. Kullanıcılar sisteme erişemez."
          showIcon
          className="mb-4"
        />
        
        <div className="flex items-center justify-between">
          <div>
            <div className="font-medium">Bakım Modunu {sistemDurumu?.bakimModu ? 'Kapat' : 'Aç'}</div>
            <div className="text-sm text-gray-500">
              Tüm kullanıcıların sisteme erişimini engeller
            </div>
          </div>
          <Switch
            checked={sistemDurumu?.bakimModu || false}
            onChange={handleBakimModuToggle}
            loading={bakimModuMutation.isLoading}
            checkedChildren={<WarningOutlined />}
          />
        </div>
      </Card>
    </div>
  );
};

export default SistemYonetimiPage;
