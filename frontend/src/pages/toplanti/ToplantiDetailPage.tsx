import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Tabs,
  Table,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Switch,
  InputNumber,
  Popconfirm,
  message,
  Spin,
  Empty,
} from 'antd'
import {
  ArrowLeftOutlined,
  EditOutlined,
  PlusOutlined,
  DeleteOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons'
import type { ColumnsType } from 'antd/es/table'
import { useQuery, useMutation, useQueryClient } from 'react-query'
import { toplantiApi, KararCreateRequest, KatilimciCreateRequest } from '@/api/toplanti'
import {
  Toplanti,
  Karar,
  ToplantiKatilimci,
  ToplantiTuru,
  ToplantiDurumu,
  KararDurumu,
  KullaniciRol,
} from '@/types'
import { useAuthStore } from '@/store/authStore'
import dayjs from 'dayjs'

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

const kararDurumuLabels: Record<KararDurumu, string> = {
  [KararDurumu.KABUL_EDILDI]: 'Kabul Edildi',
  [KararDurumu.REDDEDILDI]: 'Reddedildi',
  [KararDurumu.ERTELENDI]: 'Ertelendi',
  [KararDurumu.UYGULAMADA]: 'Uygulamada',
  [KararDurumu.TAMAMLANDI]: 'Tamamlandı',
}

const ToplantiDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { hasRole } = useAuthStore()
  const [kararModalOpen, setKararModalOpen] = useState(false)
  const [katilimciModalOpen, setKatilimciModalOpen] = useState(false)
  const [kararForm] = Form.useForm()
  const [katilimciForm] = Form.useForm()

  const canManage = hasRole([
    KullaniciRol.SISTEM_ADMIN,
    KullaniciRol.MERKEZ_YONETICI,
    KullaniciRol.BIRLIK_YONETICI,
  ])

  // Toplantı detay
  const { data: toplanti, isLoading } = useQuery<Toplanti>(
    ['toplanti', id],
    async () => {
      const res = await toplantiApi.getById(Number(id))
      return res.data
    },
    { enabled: !!id }
  )

  // Kararlar
  const { data: kararlar = [] } = useQuery<Karar[]>(
    ['kararlar', id],
    async () => {
      const res = await toplantiApi.getKararlar(Number(id))
      return res.data
    },
    { enabled: !!id }
  )

  // Katılımcılar
  const { data: katilimcilar = [] } = useQuery<ToplantiKatilimci[]>(
    ['katilimcilar', id],
    async () => {
      const res = await toplantiApi.getKatilimcilar(Number(id))
      return res.data
    },
    { enabled: !!id }
  )

  // Karar ekle
  const addKararMutation = useMutation(
    (data: KararCreateRequest) => toplantiApi.addKarar(Number(id), data),
    {
      onSuccess: () => {
        message.success('Karar eklendi')
        queryClient.invalidateQueries(['kararlar', id])
        queryClient.invalidateQueries(['toplanti', id])
        setKararModalOpen(false)
        kararForm.resetFields()
      },
      onError: () => { message.error('Karar eklenemedi') },
    }
  )

  // Karar sil
  const deleteKararMutation = useMutation(
    (kararId: number) => toplantiApi.deleteKarar(Number(id), kararId),
    {
      onSuccess: () => {
        message.success('Karar silindi')
        queryClient.invalidateQueries(['kararlar', id])
      },
      onError: () => { message.error('Karar silinemedi') },
    }
  )

  // Katılımcı ekle
  const addKatilimciMutation = useMutation(
    (data: KatilimciCreateRequest) => toplantiApi.addKatilimci(Number(id), data),
    {
      onSuccess: () => {
        message.success('Katılımcı eklendi')
        queryClient.invalidateQueries(['katilimcilar', id])
        queryClient.invalidateQueries(['toplanti', id])
        setKatilimciModalOpen(false)
        katilimciForm.resetFields()
      },
      onError: () => { message.error('Katılımcı eklenemedi') },
    }
  )

  // Katılımcı sil
  const removeKatilimciMutation = useMutation(
    (katilimciId: number) => toplantiApi.removeKatilimci(Number(id), katilimciId),
    {
      onSuccess: () => {
        message.success('Katılımcı çıkarıldı')
        queryClient.invalidateQueries(['katilimcilar', id])
      },
      onError: () => { message.error('Katılımcı çıkarılamadı') },
    }
  )

  const getDurumTag = (durum: ToplantiDurumu) => {
    const colors: Record<ToplantiDurumu, string> = {
      [ToplantiDurumu.PLANLANMIS]: 'blue',
      [ToplantiDurumu.DEVAM_EDIYOR]: 'processing',
      [ToplantiDurumu.TAMAMLANDI]: 'green',
      [ToplantiDurumu.IPTAL]: 'red',
      [ToplantiDurumu.ERTELENDI]: 'orange',
    }
    return <Tag color={colors[durum]}>{toplantiDurumuLabels[durum]}</Tag>
  }

  const getKararDurumTag = (durum: KararDurumu) => {
    const colors: Record<KararDurumu, string> = {
      [KararDurumu.KABUL_EDILDI]: 'green',
      [KararDurumu.REDDEDILDI]: 'red',
      [KararDurumu.ERTELENDI]: 'orange',
      [KararDurumu.UYGULAMADA]: 'processing',
      [KararDurumu.TAMAMLANDI]: 'cyan',
    }
    return <Tag color={colors[durum]}>{kararDurumuLabels[durum]}</Tag>
  }

  // Karar tablosu kolonları
  const kararColumns: ColumnsType<Karar> = [
    { title: 'Karar No', dataIndex: 'kararNo', key: 'kararNo', width: 160 },
    { title: 'Sıra', dataIndex: 'kararSirasi', key: 'kararSirasi', width: 60, align: 'center' },
    { title: 'Başlık', dataIndex: 'baslik', key: 'baslik', ellipsis: true },
    {
      title: 'Durum',
      dataIndex: 'durum',
      key: 'durum',
      width: 130,
      render: (d: KararDurumu) => getKararDurumTag(d),
    },
    {
      title: 'Oy',
      key: 'oy',
      width: 100,
      render: (_: unknown, r: Karar) =>
        r.oyBirligi ? (
          <Tag color="green">Oy Birliği</Tag>
        ) : (
          <span>{r.kabulOyu ?? 0}/{r.redOyu ?? 0}/{r.cekimserOyu ?? 0}</span>
        ),
    },
    { title: 'Sorumlu', dataIndex: 'sorumlu', key: 'sorumlu', width: 140, ellipsis: true },
    ...(canManage
      ? [
          {
            title: 'İşlem',
            key: 'action',
            width: 60,
            render: (_: unknown, r: Karar) => (
              <Popconfirm
                title="Bu kararı silmek istediğinize emin misiniz?"
                onConfirm={() => deleteKararMutation.mutate(r.id)}
                okText="Evet"
                cancelText="Hayır"
              >
                <Button type="link" danger size="small" icon={<DeleteOutlined />} />
              </Popconfirm>
            ),
          },
        ]
      : []),
  ]

  // Katılımcı tablosu kolonları
  const katilimciColumns: ColumnsType<ToplantiKatilimci> = [
    { title: 'Ad Soyad', dataIndex: 'adSoyad', key: 'adSoyad' },
    { title: 'Üye No', dataIndex: 'uyeNo', key: 'uyeNo', width: 120 },
    { title: 'Görev', dataIndex: 'gorev', key: 'gorev', width: 150 },
    {
      title: 'Katıldı',
      dataIndex: 'katildi',
      key: 'katildi',
      width: 80,
      align: 'center',
      render: (v: boolean) =>
        v ? <CheckCircleOutlined style={{ color: '#52c41a' }} /> : <CloseCircleOutlined style={{ color: '#ff4d4f' }} />,
    },
    {
      title: 'İmzaladı',
      dataIndex: 'imzaladi',
      key: 'imzaladi',
      width: 80,
      align: 'center',
      render: (v: boolean) =>
        v ? <CheckCircleOutlined style={{ color: '#52c41a' }} /> : <CloseCircleOutlined style={{ color: '#ff4d4f' }} />,
    },
    { title: 'Mazeret', dataIndex: 'mazeret', key: 'mazeret', ellipsis: true },
    ...(canManage
      ? [
          {
            title: 'İşlem',
            key: 'action',
            width: 60,
            render: (_: unknown, r: ToplantiKatilimci) => (
              <Popconfirm
                title="Bu katılımcıyı çıkarmak istediğinize emin misiniz?"
                onConfirm={() => removeKatilimciMutation.mutate(r.id)}
                okText="Evet"
                cancelText="Hayır"
              >
                <Button type="link" danger size="small" icon={<DeleteOutlined />} />
              </Popconfirm>
            ),
          },
        ]
      : []),
  ]

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Spin size="large" />
      </div>
    )
  }

  if (!toplanti) {
    return <Empty description="Toplantı bulunamadı" />
  }

  return (
    <div>
      <div className="mb-6 flex justify-between items-center">
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/toplantilar')}>
            Geri
          </Button>
          <h1 className="text-2xl font-semibold text-gray-800 flex items-center gap-2 m-0">
            <CalendarOutlined /> {toplanti.baslik}
          </h1>
        </Space>
        {canManage && (
          <Button
            type="primary"
            icon={<EditOutlined />}
            onClick={() => navigate(`/toplantilar/${id}/duzenle`)}
          >
            Düzenle
          </Button>
        )}
      </div>

      {/* Toplantı Bilgileri */}
      <Card className="mb-6 shadow-sm">
        <Descriptions column={{ xs: 1, sm: 2, md: 3 }} bordered size="small">
          <Descriptions.Item label="Toplantı No">{toplanti.toplantiNo}</Descriptions.Item>
          <Descriptions.Item label="Birlik">{toplanti.birlikAdi || '-'}</Descriptions.Item>
          <Descriptions.Item label="Tür">
            <Tag>{toplantiTuruLabels[toplanti.toplantiTuru]}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Durum">{getDurumTag(toplanti.durum)}</Descriptions.Item>
          <Descriptions.Item label="Tarih">
            {toplanti.toplantiTarihi ? dayjs(toplanti.toplantiTarihi).format('DD.MM.YYYY') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="Saat">
            {toplanti.baslangicSaati || '-'}
            {toplanti.bitisSaati ? ` - ${toplanti.bitisSaati}` : ''}
          </Descriptions.Item>
          <Descriptions.Item label="Yer" span={3}>
            {toplanti.yer || '-'}
          </Descriptions.Item>
          {toplanti.gundem && (
            <Descriptions.Item label="Gündem" span={3}>
              <pre className="whitespace-pre-wrap m-0 font-sans">{toplanti.gundem}</pre>
            </Descriptions.Item>
          )}
          {toplanti.aciklama && (
            <Descriptions.Item label="Açıklama" span={3}>
              {toplanti.aciklama}
            </Descriptions.Item>
          )}
        </Descriptions>
      </Card>

      {/* Kararlar & Katılımcılar */}
      <Card className="shadow-sm">
        <Tabs
          defaultActiveKey="kararlar"
          items={[
            {
              key: 'kararlar',
              label: `Kararlar (${kararlar.length})`,
              children: (
                <>
                  {canManage && (
                    <div className="mb-4">
                      <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        onClick={() => setKararModalOpen(true)}
                      >
                        Karar Ekle
                      </Button>
                    </div>
                  )}
                  <Table
                    columns={kararColumns}
                    dataSource={kararlar}
                    rowKey="id"
                    pagination={false}
                    scroll={{ x: 800 }}
                    expandable={{
                      expandedRowRender: (record: Karar) => (
                        <div className="p-2">
                          <p className="mb-1">
                            <strong>Karar Metni:</strong>
                          </p>
                          <p className="whitespace-pre-wrap">{record.kararMetni}</p>
                          {record.notlar && (
                            <>
                              <p className="mb-1 mt-2">
                                <strong>Notlar:</strong>
                              </p>
                              <p>{record.notlar}</p>
                            </>
                          )}
                        </div>
                      ),
                    }}
                  />
                </>
              ),
            },
            {
              key: 'katilimcilar',
              label: `Katılımcılar (${katilimcilar.length})`,
              children: (
                <>
                  {canManage && (
                    <div className="mb-4">
                      <Button
                        type="primary"
                        icon={<PlusOutlined />}
                        onClick={() => setKatilimciModalOpen(true)}
                      >
                        Katılımcı Ekle
                      </Button>
                    </div>
                  )}
                  <Table
                    columns={katilimciColumns}
                    dataSource={katilimcilar}
                    rowKey="id"
                    pagination={false}
                    scroll={{ x: 700 }}
                  />
                </>
              ),
            },
          ]}
        />
      </Card>

      {/* Karar Ekle Modal */}
      <Modal
        title="Yeni Karar Ekle"
        open={kararModalOpen}
        onCancel={() => {
          setKararModalOpen(false)
          kararForm.resetFields()
        }}
        onOk={() => kararForm.submit()}
        confirmLoading={addKararMutation.isLoading}
        okText="Ekle"
        cancelText="İptal"
        width={640}
      >
        <Form
          form={kararForm}
          layout="vertical"
          onFinish={(values: KararCreateRequest) => addKararMutation.mutate(values)}
          initialValues={{ oyBirligi: true, durum: KararDurumu.KABUL_EDILDI }}
        >
          <Form.Item name="baslik" label="Başlık" rules={[{ required: true, message: 'Başlık gerekli' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="kararMetni" label="Karar Metni" rules={[{ required: true, message: 'Karar metni gerekli' }]}>
            <Input.TextArea rows={4} />
          </Form.Item>
          <Form.Item name="durum" label="Durum">
            <Select
              options={Object.entries(kararDurumuLabels).map(([k, v]) => ({ value: k, label: v }))}
            />
          </Form.Item>
          <Form.Item name="oyBirligi" label="Oy Birliği" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.oyBirligi !== cur.oyBirligi}>
            {({ getFieldValue }) =>
              !getFieldValue('oyBirligi') && (
                <Space>
                  <Form.Item name="kabulOyu" label="Kabul">
                    <InputNumber min={0} />
                  </Form.Item>
                  <Form.Item name="redOyu" label="Red">
                    <InputNumber min={0} />
                  </Form.Item>
                  <Form.Item name="cekimserOyu" label="Çekimser">
                    <InputNumber min={0} />
                  </Form.Item>
                </Space>
              )
            }
          </Form.Item>
          <Form.Item name="sorumlu" label="Sorumlu">
            <Input />
          </Form.Item>
          <Form.Item name="notlar" label="Notlar">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      {/* Katılımcı Ekle Modal */}
      <Modal
        title="Yeni Katılımcı Ekle"
        open={katilimciModalOpen}
        onCancel={() => {
          setKatilimciModalOpen(false)
          katilimciForm.resetFields()
        }}
        onOk={() => katilimciForm.submit()}
        confirmLoading={addKatilimciMutation.isLoading}
        okText="Ekle"
        cancelText="İptal"
      >
        <Form
          form={katilimciForm}
          layout="vertical"
          onFinish={(values: KatilimciCreateRequest) => addKatilimciMutation.mutate(values)}
          initialValues={{ katildi: true, imzaladi: false }}
        >
          <Form.Item name="adSoyad" label="Ad Soyad" rules={[{ required: true, message: 'Ad Soyad gerekli' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="gorev" label="Görev">
            <Input placeholder="Örn: Başkan, Üye, Denetçi" />
          </Form.Item>
          <Form.Item name="katildi" label="Katıldı" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item noStyle shouldUpdate={(prev, cur) => prev.katildi !== cur.katildi}>
            {({ getFieldValue }) =>
              !getFieldValue('katildi') && (
                <Form.Item name="mazeret" label="Mazeret">
                  <Input.TextArea rows={2} />
                </Form.Item>
              )
            }
          </Form.Item>
          <Form.Item name="imzaladi" label="İmzaladı" valuePropName="checked">
            <Switch />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default ToplantiDetailPage
