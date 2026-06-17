<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Plus, 
  Document, 
  Setting, 
  Lock, 
  Menu as IconMenu, 
  InfoFilled, 
  Edit, 
  Delete, 
  Search,
  Compass,
  Connection,
  Check,
  Clock,
  Share
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { AppClientRecord } from '@/api/appClient'

const route = useRoute()
const router = useRouter()
const query = reactive({
  clientName: '',
  clientCode: '',
  clientType: undefined as number | undefined,
  status: undefined as number | undefined
})

const loading = ref(false)
const tableData = ref<AppClientRecord[]>([])

const mockClientsByApp: Record<string, AppClientRecord[]> = {
  '10001': [
    {
      id: 'client_10001_admin',
      appId: '10001',
      appCode: 'admin-console',
      clientCode: 'admin_web',
      clientName: '管理后台 Web',
      clientType: 1,
      realmName: '平台身份域',
      loginMode: 'IAM_HOSTED',
      loginPageId: 'login_admin',
      loginPageName: '平台统一登录模板',
      authPolicyName: '平台强认证策略',
      defaultEntryUrl: 'http://localhost:5173/login',
      status: 1,
      description: '认证中心管理端客户端',
      updateTime: '2026-06-16 17:28:00'
    },
    {
      id: 'client_10001_service',
      appId: '10001',
      appCode: 'admin-console',
      clientCode: 'iam_service',
      clientName: 'IAM 内部服务',
      clientType: 6,
      realmName: '平台身份域',
      loginMode: 'SERVICE',
      authPolicyName: 'Client Credentials',
      defaultEntryUrl: 'http://localhost:8080/api',
      status: 1,
      description: '内部服务间调用客户端',
      updateTime: '2026-06-16 17:28:00'
    },
    {
      id: 'client_10001_api_only',
      appId: '10001',
      appCode: 'admin-console',
      clientCode: 'account_sync_api',
      clientName: '账号同步 API',
      clientType: 5,
      realmName: '平台身份域',
      loginMode: 'API_ONLY',
      authPolicyName: '接口签名认证',
      defaultEntryUrl: 'http://localhost:8080/account-sync',
      status: 2,
      description: '仅调用注册账号和获取登录信息接口',
      updateTime: '2026-06-15 15:40:00'
    }
  ],
  '10002': [
    {
      id: 'client_10002_admin',
      appId: '10002',
      appCode: 'ecommerce',
      clientCode: 'mall_admin_client',
      clientName: '商城平台端',
      clientType: 1,
      realmName: '租户身份域 (租户A)',
      loginMode: 'IAM_HOSTED',
      loginPageName: '商城平台登录模板',
      authPolicyName: '账号密码 + TOTP',
      defaultEntryUrl: 'https://mall.example.com/admin/login',
      status: 1,
      description: '平台运营后台',
      updateTime: '2026-06-16 16:10:00'
    },
    {
      id: 'client_10002_merchant',
      appId: '10002',
      appCode: 'ecommerce',
      clientCode: 'merchant_web',
      clientName: '商家后台',
      clientType: 2,
      realmName: '租户身份域 (租户A)',
      loginMode: 'EXTERNAL_REDIRECT',
      externalLoginUrl: 'https://merchant.example.com/login',
      authPolicyName: '商家登录回调校验',
      defaultEntryUrl: 'https://merchant.example.com',
      status: 1,
      description: '客户自有登录页跳转',
      updateTime: '2026-06-16 16:10:00'
    },
    {
      id: 'client_10002_mini',
      appId: '10002',
      appCode: 'ecommerce',
      clientCode: 'buyer_mini_client',
      clientName: '消费者小程序',
      clientType: 3,
      realmName: '租户身份域 (租户A)',
      loginMode: 'IAM_HOSTED',
      loginPageName: '移动端登录模板',
      authPolicyName: '微信小程序登录',
      defaultEntryUrl: 'app://mall/pages/index',
      status: 1,
      description: '消费者小程序登录入口',
      updateTime: '2026-06-15 10:20:00'
    },
    {
      id: 'client_10002_open',
      appId: '10002',
      appCode: 'ecommerce',
      clientCode: 'mall_open_api',
      clientName: '开放 API',
      clientType: 5,
      realmName: '租户身份域 (租户A)',
      loginMode: 'SERVICE',
      authPolicyName: 'Client Credentials',
      defaultEntryUrl: 'https://mall.example.com/openapi',
      status: 1,
      description: '服务端接口认证',
      updateTime: '2026-06-16 16:10:00'
    }
  ],
  '10006': [
    {
      id: 'client_10006_customer',
      appId: '10006',
      appCode: 'customer-login',
      clientCode: 'customer_owned_login',
      clientName: '客户自有登录入口',
      clientType: 2,
      realmName: '租户身份域 (租户A)',
      loginMode: 'EXTERNAL_REDIRECT',
      externalLoginUrl: 'https://customer.example.com/login',
      authPolicyName: '外部登录回调校验',
      defaultEntryUrl: 'https://customer.example.com',
      status: 1,
      description: '使用客户系统自己的登录页',
      updateTime: '2026-06-16 09:05:00'
    }
  ]
}

const fallbackClients = (appId: string): AppClientRecord[] => [
  {
    id: `client_${appId}_default`,
    appId,
    appCode: 'mock_app',
    clientCode: 'default_web',
    clientName: '默认 Web 客户端',
    clientType: 1,
    realmName: '平台身份域',
    loginMode: 'IAM_HOSTED',
    loginPageName: '默认登录入口模板',
    authPolicyName: '默认认证策略',
    defaultEntryUrl: 'https://app.example.com',
    status: 1,
    description: '详情页本地初始化客户端数据',
    updateTime: '2026-06-17 10:00:00'
  }
]

const clientTypeOptions = [
  { label: 'ADMIN_WEB (管理后台)', value: 1 },
  { label: 'MERCHANT_WEB (商家后台)', value: 2 },
  { label: 'MINI_PROGRAM (微信小程序)', value: 3 },
  { label: 'H5 (轻量移动网页)', value: 4 },
  { label: 'OPEN_API (开放API接口)', value: 5 },
  { label: 'SERVICE (服务间通信)', value: 6 }
]

const clientTypeText = (type?: string | number) => {
  const value = Number(type)
  return clientTypeOptions.find(item => item.value === value)?.label || '-'
}

const loginEntryText = (row: AppClientRecord) => {
  if (Number(row.clientType) === 5 || Number(row.clientType) === 6) {
    return '接口认证'
  }
  if (row.loginMode === 'API_ONLY') {
    return '接口接入'
  }
  if (row.loginMode === 'EXTERNAL_REDIRECT') {
    return row.externalLoginUrl || '外部登录页'
  }
  return row.loginPageName || (row.loginPageId ? `模板 ID ${row.loginPageId}` : '未绑定模板')
}

const authPolicyText = (row: AppClientRecord) => {
  return row.authPolicyName || (row.authPolicyId ? `策略 ID ${row.authPolicyId}` : '继承身份域默认')
}

const filteredTableData = computed(() => {
  return tableData.value.filter(item => {
    const matchName = !query.clientName || item.clientName.includes(query.clientName)
    const matchCode = !query.clientCode || item.clientCode.includes(query.clientCode)
    const matchType = query.clientType === undefined || Number(item.clientType) === query.clientType
    const matchStatus = query.status === undefined || item.status === query.status
    return matchName && matchCode && matchType && matchStatus
  })
})

const fetchData = async () => {
  const appId = route.params.id as string
  if (!appId) return
  loading.value = true
  window.setTimeout(() => {
    const clients = mockClientsByApp[appId] || fallbackClients(appId)
    tableData.value = clients.map(item => ({ ...item }))
    loading.value = false
  }, 120)
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  query.clientName = ''
  query.clientCode = ''
  query.clientType = undefined
  query.status = undefined
  fetchData()
}

const handleCreate = () => {
  router.push(`/applications/detail/${route.params.id}/clients/create`)
}

const toggleStatus = async (row: AppClientRecord) => {
  const enabled = row.status === 1
  const action = enabled ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${action}客户端「${row.clientName}」？`, '提示', { type: 'warning' })
    row.status = enabled ? 2 : 1
    ElMessage.success(`客户端已${action}`)
  } catch (error: any) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error(error.message || `${action}客户端失败`)
  }
}

// Table search/action handlers
const handleConfigure = (row: AppClientRecord) => {
  router.push(`/applications/detail/${route.params.id}/clients/config/${row.id}`)
}
// Obsolete drawer variables to ensure compilation passes
const configDrawerVisible = ref(false)
const selectedClient = ref<any>(null)
const drawerActiveTab = ref('basic')
const isEditingMenu = ref(false)
const menuFormMode = ref('create')
const menuFormData = ref<any>({})
const menuFormRules = ref<any>({})
const menuFormRef = ref()
const saveMenuForm = () => {}
const handleAddPermission = () => {}
const currentPermissions = ref<any[]>([])
const handleDeletePermission = (_idx?: any) => {}
const handleAddSubMenu = (_node?: any) => {}
const handleEditMenu = (_node?: any) => {}
const handleDeleteMenu = (_node?: any) => {}
const currentMenus = ref<any[]>([])
const currentExternalConfigs = ref<any[]>([])
const copyToClipboard = (_text?: any) => {}
const handleAddExternal = () => {}
const isEditingExternal = ref(false)
const handleEditExternal = (_row?: any) => {}
const handleDeleteExternal = (_idx?: any) => {}
const externalFormMode = ref('create')
const externalFormData = ref<any>({})
const externalFormRules = ref<any>({})
const platformTypeOptions = ref<any[]>([])
const saveExternalForm = () => {}
const handleAddRootMenu = () => {}
const realmOptions = ref<any[]>([])

onMounted(fetchData)
</script>

<template>
  <div class="client-tab-container">
    <!-- Filter and Actions -->
    <div class="filter-action-bar">
      <div class="filter-form">
        <div class="filter-item">
          <label>客户端名称</label>
          <el-input v-model="query.clientName" placeholder="请输入名称" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </div>
        <div class="filter-item">
          <label>client_id</label>
          <el-input v-model="query.clientCode" placeholder="请输入 ID" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </div>
        <div class="filter-item">
          <label>客户端类型</label>
          <el-select v-model="query.clientType" placeholder="请选择类型" clearable style="width: 160px">
            <el-option v-for="item in clientTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </div>
        <div class="filter-item">
          <label>状态</label>
          <el-select v-model="query.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </div>
        <div class="filter-btn-group">
          <el-button type="primary" @click="handleSearch" :icon="Search" class="btn-search-primary">查询</el-button>
          <el-button @click="handleReset" class="btn-reset">重置</el-button>
        </div>
      </div>
      <div class="action-btn-group">
        <el-button type="primary" :icon="Plus" @click="handleCreate" class="btn-add-client-gradient">新增客户端</el-button>
      </div>
    </div>

    <!-- Data Table -->
    <el-table :data="filteredTableData" v-loading="loading" style="width: 100%" class="custom-table" border>
      <el-table-column prop="clientName" label="客户端名称" min-width="150" />
      <el-table-column prop="clientCode" label="client_id" min-width="150" />
      <el-table-column label="客户端类型" min-width="150">
        <template #default="{ row }">
          <span class="type-tag">{{ clientTypeText(row.clientType) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="realmName" label="默认身份域" min-width="130" />
      <el-table-column label="登录入口模板" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ loginEntryText(row) }}
        </template>
      </el-table-column>
      <el-table-column label="认证策略" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          {{ authPolicyText(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="defaultEntryUrl" label="默认入口地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" min-width="90">
        <template #default="{ row }">
          <div class="status-cell" :class="row.status === 1 ? 'text-green' : 'text-red'">
            <span class="dot"></span> {{ row.status === 1 ? '启用' : '禁用' }}
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
      <el-table-column prop="updateTime" label="更新时间" min-width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <div class="row-actions">
            <el-button link type="primary" @click="handleConfigure(row)" :icon="Compass" class="btn-table-action">客户端配置</el-button>
            <span class="divider"></span>
            <el-button link :type="row.status === 1 ? 'danger' : 'primary'" @click="toggleStatus(row)" class="btn-table-action">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <div class="pagination-container">
      <span class="total-text">共 {{ filteredTableData.length }} 条</span>
      <el-pagination
        background
        layout="sizes, prev, pager, next, jumper"
        :total="filteredTableData.length"
        :page-sizes="[10, 20, 50]"
        :default-page-size="10"
      />
    </div>

    <!-- Configuration Drawer -->
    <el-drawer
      v-model="configDrawerVisible"
      size="920px"
      destroy-on-close
      class="premium-config-drawer"
    >
      <template #header>
        <div class="drawer-header-banner" v-if="selectedClient">
          <div class="header-left">
            <div class="avatar-icon-wrapper">
              <el-icon><Setting /></el-icon>
            </div>
            <div class="header-text">
              <h3>客户端配置</h3>
              <div class="client-meta">
                <span class="client-name">{{ selectedClient.clientName }}</span>
                <span class="divider-dot"></span>
                <span class="client-id-code">ID: {{ selectedClient.clientCode }}</span>
              </div>
            </div>
          </div>
          <span class="client-type-pill">{{ clientTypeText(selectedClient.clientType) }}</span>
        </div>
      </template>

      <!-- Drawer Content: Vertical Sidebar Layout -->
      <div class="drawer-layout-container" v-if="selectedClient">
        <!-- Vertical Tabs Sidebar -->
        <div class="drawer-sidebar">
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'basic' }"
            @click="drawerActiveTab = 'basic'"
          >
            <el-icon><InfoFilled /></el-icon>
            <span>基本配置</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'security' }"
            @click="drawerActiveTab = 'security'"
          >
            <el-icon><Lock /></el-icon>
            <span>安全与重定向</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'grants' }"
            @click="drawerActiveTab = 'grants'"
          >
            <el-icon><Connection /></el-icon>
            <span>授权模式</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'external' }"
            @click="drawerActiveTab = 'external'"
          >
            <el-icon><Share /></el-icon>
            <span>外部平台</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'menus' }"
            @click="drawerActiveTab = 'menus'"
          >
            <el-icon><IconMenu /></el-icon>
            <span>端菜单树</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'permissions' }"
            @click="drawerActiveTab = 'permissions'"
          >
            <el-icon><Compass /></el-icon>
            <span>接口权限</span>
          </div>
          <div 
            class="sidebar-item" 
            :class="{ active: drawerActiveTab === 'tokens' }"
            @click="drawerActiveTab = 'tokens'"
          >
            <el-icon><Clock /></el-icon>
            <span>令牌时效</span>
          </div>
        </div>

        <!-- Main Content Area -->
        <div class="drawer-main-content">
          <!-- 1. Basic Settings -->
          <div v-if="drawerActiveTab === 'basic'" class="settings-panel animate-panel-fade-in">
            <h4 class="panel-section-title">基本属性</h4>
            <el-form label-position="top" class="form-container-styled">
              <el-form-item label="客户端名称">
                <el-input v-model="selectedClient.clientName" />
              </el-form-item>
              <el-form-item label="客户端 ID (client_id)">
                <el-input v-model="selectedClient.clientCode" disabled />
              </el-form-item>
              <el-form-item label="客户端密钥 (client_secret)">
                <div class="secret-input-wrapper">
                  <el-input 
                    value="••••••••••••••••••••••••••••••••••••••••" 
                    type="password"
                    show-password 
                    disabled 
                    class="secret-field"
                  />
                  <el-button @click="copyToClipboard('sec_authsphere_client_dev_secret_684f8263')">复制密钥</el-button>
                </div>
                <span class="field-hint-text">密钥用于 M2M 或 Web 认证中的安全通信，请妥善保管。</span>
              </el-form-item>
              
              <el-form-item label="默认身份域 (Realm)">
                <el-select v-model="selectedClient.defaultRealmId" placeholder="请选择客户端登录默认身份域" style="width: 100%">
                  <el-option 
                    v-for="realm in realmOptions" 
                    :key="realm.value" 
                    :label="realm.label" 
                    :value="realm.value" 
                  />
                </el-select>
                <span class="field-hint-text">用户通过此客户端进行单点登录时，默认将账号鉴权指向该安全域空间。</span>
              </el-form-item>

              <el-form-item label="默认入口地址 (default_entry_url)">
                <el-input v-model="selectedClient.defaultEntryUrl" placeholder="如: https://merchant.authsphere.com/login" />
                <span class="field-hint-text">客户端的前端单点登录承载与默认重定向入口地址。</span>
              </el-form-item>

              <el-form-item label="描述说明">
                <el-input v-model="selectedClient.description" type="textarea" :rows="3" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 2. Security & Redirection -->
          <div v-if="drawerActiveTab === 'security'" class="settings-panel animate-panel-fade-in">
            <h4 class="panel-section-title">安全与重定向</h4>
            <el-form label-position="top" class="form-container-styled">
              <el-form-item label="允许的回调重定向 URI (Redirect URIs)">
                <el-input 
                  v-model="selectedClient.defaultEntryUrl" 
                  placeholder="https://app.example.com/callback" 
                />
                <span class="field-hint-text">用户登录成功后允许跳转回的主机与路径白名单（支持逗号分隔多个）。</span>
              </el-form-item>
              <el-form-item label="允许的注销后重定向 URI (Post Logout URIs)">
                <el-input placeholder="https://app.example.com/login" />
                <span class="field-hint-text">用户注销登录后允许重定向的返回地址。</span>
              </el-form-item>
              <el-form-item label="允许的 Web 发源地 (Web Origins / CORS)">
                <el-input placeholder="*" />
                <span class="field-hint-text">配置允许跨域请求资源的 Origins 域名，输入 * 允许所有。</span>
              </el-form-item>
            </el-form>
          </div>

          <!-- 3. Grant Types -->
          <div v-if="drawerActiveTab === 'grants'" class="settings-panel animate-panel-fade-in">
            <h4 class="panel-section-title">授权模式 (Grant Types)</h4>
            <p class="panel-section-subtitle">配置该应用端被允许通过何种 OAuth2 流程进行授权颁发 Token。</p>
            
            <div class="checkbox-cards-grid">
              <div class="checkbox-card-item">
                <el-checkbox label="Authorization Code (授权码模式)" checked />
                <p>最安全的登录模式，适用于配有后端的 Web 应用及原生 App。</p>
              </div>
              <div class="checkbox-card-item">
                <el-checkbox label="Client Credentials (客户端凭证模式)" checked />
                <p>适用于没有用户参与的 M2M（机器对机器）服务间互调。</p>
              </div>
              <div class="checkbox-card-item">
                <el-checkbox label="Refresh Token (刷新令牌流程)" checked />
                <p>允许客户端在 Access Token 过期时无感获取新令牌。</p>
              </div>
              <div class="checkbox-card-item">
                <el-checkbox label="Implicit (隐式授权模式)" />
                <p>不推荐。适用于纯前端无安全后端的单页应用（SPA）。</p>
              </div>
              <div class="checkbox-card-item">
                <el-checkbox label="Password (密码模式)" />
                <p>不推荐。直接将用户账号密码暴露给客户端以换取 Token。</p>
              </div>
            </div>
          </div>

          <!-- 4. External Platform Configurations -->
          <div v-if="drawerActiveTab === 'external'" class="settings-panel animate-panel-fade-in">
            <div class="pane-action-header">
              <div class="pane-title-left">
                <h3>外部第三方平台集成</h3>
                <span>配置当前客户端绑定的微信、支付宝、OIDC等平台的授权对接参数。</span>
              </div>
              <el-button type="primary" :icon="Plus" @click="handleAddExternal" class="btn-add-permission" v-if="!isEditingExternal">新增平台配置</el-button>
            </div>

            <!-- List of External Configs -->
            <div v-if="!isEditingExternal" class="external-configs-list mt-16">
              <div v-if="currentExternalConfigs.length === 0" class="panel-empty-placeholder">
                <el-icon class="placeholder-icon"><Share /></el-icon>
                <p>暂无绑定的外部平台配置，点击右上角“新增平台配置”开始集成。</p>
              </div>
              <div class="external-config-cards-grid" v-else>
                <div v-for="(item, index) in currentExternalConfigs" :key="item.id" class="external-config-card">
                  <div class="card-left">
                    <div class="platform-logo-wrapper" :class="item.providerType.toLowerCase()">
                      <el-icon><Connection /></el-icon>
                    </div>
                    <div class="card-details">
                      <div class="card-title-row">
                        <h4>{{ item.providerName }}</h4>
                        <span class="platform-type-pill-small">{{ item.providerType }}</span>
                      </div>
                      <div class="card-meta-row">
                        <span>标识: <code>{{ item.providerCode }}</code></span>
                        <span class="divider-dot"></span>
                        <span>AppID: <code>{{ item.appId }}</code></span>
                      </div>
                    </div>
                  </div>
                  <div class="card-actions">
                    <el-switch 
                      v-model="item.status" 
                      :active-value="1" 
                      :inactive-value="2" 
                      active-text="启用" 
                      inactive-text="禁用" 
                      inline-prompt
                      class="mr-12"
                    />
                    <el-button link type="primary" :icon="Edit" @click="handleEditExternal(item)"></el-button>
                    <el-button link type="danger" :icon="Delete" @click="handleDeleteExternal(index)"></el-button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Editor Pane for External Config -->
            <div v-else class="external-editor-card mt-16 animate-panel-fade-in">
              <div class="editor-header">
                <h4>{{ externalFormMode === 'create' ? '新增集成通道' : '编辑集成通道' }}</h4>
                <el-button link @click="isEditingExternal = false">返回通道列表</el-button>
              </div>
              <el-form
                ref="externalFormRef"
                :model="externalFormData"
                :rules="externalFormRules"
                label-position="top"
                class="form-container-styled pt-16"
              >
                <div class="config-row">
                  <div class="config-col">
                    <el-form-item label="通道平台类型" required>
                      <el-select v-model="externalFormData.providerType" style="width: 100%">
                        <el-option 
                          v-for="opt in platformTypeOptions" 
                          :key="opt.value" 
                          :label="opt.label" 
                          :value="opt.value" 
                        />
                      </el-select>
                    </el-form-item>
                  </div>
                  <div class="config-col">
                    <el-form-item label="通道唯一标识 (provider_code)" prop="providerCode" required>
                      <el-input v-model="externalFormData.providerCode" placeholder="如: wechat_mini_primary" />
                    </el-form-item>
                  </div>
                </div>

                <div class="config-row">
                  <div class="config-col">
                    <el-form-item label="通道显示名称 (provider_name)" prop="providerName" required>
                      <el-input v-model="externalFormData.providerName" placeholder="如: 商城端小程序集成" />
                    </el-form-item>
                  </div>
                  <div class="config-col">
                    <el-form-item label="第三方 AppID (app_id)" prop="appId" required>
                      <el-input v-model="externalFormData.appId" placeholder="输入第三方的 AppID 或 Client ID" />
                    </el-form-item>
                  </div>
                </div>

                <div class="config-row">
                  <div class="config-col">
                    <el-form-item label="通道密钥 (AppSecret)" prop="appSecret" required>
                      <el-input v-model="externalFormData.appSecret" type="password" show-password placeholder="请输入对应的 AppSecret，安全存储" />
                    </el-form-item>
                  </div>
                  <div class="config-col">
                    <el-form-item label="自定义回调地址 (Callback URL)">
                      <el-input v-model="externalFormData.callbackUrl" placeholder="例如 OIDC 的登录回调路径 (选填)" />
                    </el-form-item>
                  </div>
                </div>

                <el-form-item label="启用状态">
                  <el-switch
                    v-model="externalFormData.status"
                    :active-value="1"
                    :inactive-value="2"
                    active-text="启用"
                    inactive-text="禁用"
                    inline-prompt
                  />
                </el-form-item>

                <div class="editor-actions pt-16">
                  <el-button @click="isEditingExternal = false">取消</el-button>
                  <el-button type="primary" @click="saveExternalForm">保存集成配置</el-button>
                </div>
              </el-form>
            </div>
          </div>

          <!-- 5. Menus Configuration -->
          <div v-if="drawerActiveTab === 'menus'" class="settings-panel split-mode animate-panel-fade-in">
            <div class="tab-pane-split-container">
              <!-- Left Tree View -->
              <div class="tree-panel">
                <div class="panel-header">
                  <span>菜单结构</span>
                  <el-button type="primary" size="small" :icon="Plus" @click="handleAddRootMenu" class="btn-add-root-menu">
                    添加菜单
                  </el-button>
                </div>
                
                <div class="tree-wrapper-scroll">
                  <el-tree
                    :data="currentMenus"
                    node-key="id"
                    default-expand-all
                    :expand-on-click-node="false"
                    class="custom-el-tree"
                  >
                    <template #default="{ data }">
                      <div class="custom-tree-node">
                        <div class="node-info">
                          <span class="node-icon"><el-icon><Document /></el-icon></span>
                          <span class="node-label" :class="{ 'is-disabled': data.status !== 1 }">{{ data.label }}</span>
                          <span class="node-code">{{ data.code }}</span>
                        </div>
                        <div class="node-actions">
                          <el-button link type="primary" :icon="Plus" @click.stop="handleAddSubMenu(data)"></el-button>
                          <el-button link type="primary" :icon="Edit" @click.stop="handleEditMenu(data)"></el-button>
                          <el-button link type="danger" :icon="Delete" @click.stop="handleDeleteMenu(data)"></el-button>
                        </div>
                      </div>
                    </template>
                  </el-tree>
                </div>
              </div>

              <!-- Right Editor Pane -->
              <div class="editor-panel" :class="{ 'is-active': isEditingMenu }">
                <div class="panel-header" v-if="isEditingMenu">
                  <span>{{ menuFormMode === 'create' ? '新增菜单' : '编辑属性' }}</span>
                </div>
                <div class="panel-empty-placeholder" v-else>
                  <el-icon class="placeholder-icon"><InfoFilled /></el-icon>
                  <p>在左侧选择菜单节点，或者点击“添加菜单”进行配置。</p>
                </div>

                <div class="form-content-inner" v-if="isEditingMenu">
                  <el-form
                    ref="menuFormRef"
                    :model="menuFormData"
                    :rules="menuFormRules"
                    label-position="top"
                    class="menu-node-form"
                  >
                    <el-form-item label="菜单名称" prop="label" required>
                      <el-input v-model="menuFormData.label" placeholder="如: 商品列表" />
                    </el-form-item>
                    <el-form-item label="权限标识码 (menu_code)" prop="code" required>
                      <el-input v-model="menuFormData.code" placeholder="如: goods_list" />
                    </el-form-item>
                    <el-form-item label="路由路径 (route_path)" prop="path" required>
                      <el-input v-model="menuFormData.path" placeholder="如: /goods/list" />
                    </el-form-item>
                    <el-form-item label="组件地址 (component_path)">
                      <el-input v-model="menuFormData.component" placeholder="如: @/views/goods/List.vue" />
                    </el-form-item>
                    <el-form-item label="菜单状态">
                      <el-switch
                        v-model="menuFormData.status"
                        :active-value="1"
                        :inactive-value="2"
                        active-text="启用"
                        inactive-text="禁用"
                        inline-prompt
                      />
                    </el-form-item>

                    <div class="editor-actions">
                      <el-button @click="isEditingMenu = false" size="small">取消</el-button>
                      <el-button type="primary" @click="saveMenuForm" size="small">确定</el-button>
                    </div>
                  </el-form>
                </div>
              </div>
            </div>
          </div>

          <!-- 6. Permissions Scopes -->
          <div v-if="drawerActiveTab === 'permissions'" class="settings-panel animate-panel-fade-in">
            <div class="permissions-tab-pane">
              <div class="pane-action-header">
                <div class="pane-title-left">
                  <h3>接口调用权限范围 (Scopes)</h3>
                  <span>控制此应用端能够调用的 API 网关接口集合。</span>
                </div>
                <el-button type="primary" :icon="Plus" @click="handleAddPermission" class="btn-add-permission">新增权限</el-button>
              </div>

              <el-table :data="currentPermissions" style="width: 100%" border class="permission-inner-table">
                <el-table-column prop="name" label="权限名称" min-width="140" />
                <el-table-column prop="code" label="权限编码 (code)" min-width="140">
                  <template #default="{ row }">
                    <code class="code-badge">{{ row.code }}</code>
                  </template>
                </el-table-column>
                <el-table-column prop="apiPath" label="API 接口地址" min-width="180">
                  <template #default="{ row }">
                    <span class="api-method-badge" :class="'method-' + row.method.toLowerCase()">{{ row.method }}</span>
                    <span class="api-path-text">{{ row.apiPath }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="description" label="说明描述" min-width="180" show-overflow-tooltip />
                <el-table-column label="操作" width="60" align="center" fixed="right">
                  <template #default="{ $index }">
                    <el-button link type="danger" :icon="Delete" @click="handleDeletePermission($index)"></el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>

          <!-- 7. Token Lifetime settings -->
          <div v-if="drawerActiveTab === 'tokens'" class="settings-panel animate-panel-fade-in">
            <h4 class="panel-section-title">令牌与会话时效 (Token Lifetime)</h4>
            <p class="panel-section-subtitle">配置此客户端所颁发 Access Token 与 Refresh Token 的有效生存周期。</p>
            
            <el-form label-position="top" class="form-container-styled mt-24">
              <el-form-item label="访问令牌有效期 (Access Token Lifespan)">
                <div class="time-input-group">
                  <el-input-number :min="60" :step="60" value="3600" />
                  <span class="unit-text">秒 (1小时)</span>
                </div>
                <span class="field-hint-text">客户端持有令牌访问受保护 API 的时限，建议短生命周期以保证安全。</span>
              </el-form-item>
              
              <el-form-item label="刷新令牌有效期 (Refresh Token Lifespan)">
                <div class="time-input-group">
                  <el-input-number :min="3600" :step="3600" value="2592000" />
                  <span class="unit-text">秒 (30天)</span>
                </div>
                <span class="field-hint-text">允许客户端在后台自动延长登录状态的时效，建议设置为较长的时间。</span>
              </el-form-item>

              <el-form-item label="空闲注销时间 (SSO Session Idle)">
                <div class="time-input-group">
                  <el-input-number :min="600" value="7200" />
                  <span class="unit-text">秒 (2小时)</span>
                </div>
                <span class="field-hint-text">会话在无任何操作情况下的最大空闲留存时间。</span>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>


      <template #footer>
        <div class="drawer-footer-inner">
          <el-button @click="configDrawerVisible = false" class="btn-drawer-close">取消</el-button>
          <el-button type="primary" @click="configDrawerVisible = false" :icon="Check" class="btn-drawer-confirm">
            确认并保存配置
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- Info Box -->
    <div class="info-box">
      <div class="info-icon">
        <el-icon><Document /></el-icon>
      </div>
      <div class="info-content-wrapper">
        <div class="info-left">
          <h4>应用客户端与资源说明</h4>
          <p>客户端是系统对外的访问入口，用于管理第三方应用或自建应用接入的身份认证配置。</p>
        </div>
        <div class="info-right">
          <ul>
            <li>
              支持多种客户端类型：
              <span class="type-badge">Web 应用</span>
              <span class="type-badge">原生移动应用</span>
              <span class="type-badge">M2M 服务间调用 API</span>
            </li>
            <li>每个客户端对应独立的 client_id，菜单资源与接口权限均挂载于客户端层面。</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.client-tab-container {
  padding: 0;
}

/* Filter and Actions */
.filter-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.filter-form {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  font-size: 13.5px;
  font-weight: 550;
  color: #475569;
  white-space: nowrap;
}

.filter-btn-group {
  display: flex;
  gap: 12px;
  margin-left: 8px;
}

.btn-search-primary {
  font-weight: 600;
  border-radius: 6px;
}

.btn-reset {
  border-radius: 6px;
  font-weight: 600;
  border-color: #cbd5e1;
  color: #475569;
}

.btn-add-client-gradient {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  border: none;
  font-weight: 600;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(37, 99, 235, 0.2);
  transition: all 0.2s;
}

.btn-add-client-gradient:hover {
  background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  box-shadow: 0 6px 14px rgba(37, 99, 235, 0.3);
}

/* Table specific styles */
.custom-table {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(226, 232, 240, 0.8);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01);
}

.custom-table :deep(th.el-table__cell) {
  background-color: #f8fafc;
  color: #334155;
  font-weight: 600;
  height: 48px;
  border-bottom: 1px solid #f1f5f9;
}

.custom-table :deep(td.el-table__cell) {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
}

.type-tag {
  color: #4f46e5;
  font-size: 11.5px;
  font-weight: 600;
  background-color: #eff6ff;
  padding: 4px 10px;
  border-radius: 9999px;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 550;
}

.status-cell .dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: currentColor;
}
.text-green { color: #10b981; }
.text-red { color: #ef4444; }

.row-actions {
  display: flex;
  align-items: center;
}

.btn-table-action {
  font-weight: 600;
}

.divider {
  width: 1px;
  height: 12px;
  background-color: #e2e8f0;
  margin: 0 10px;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
}

.total-text {
  font-size: 13px;
  color: #64748b;
}

/* Premium Drawer with Vertical Layout */
:deep(.premium-config-drawer) {
  background-color: #ffffff;
  box-shadow: -10px 0 40px rgba(0, 0, 0, 0.08);
}

:deep(.premium-config-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 20px 28px;
  border-bottom: 1px solid #f1f5f9;
}

.drawer-header-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.drawer-header-banner .header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.avatar-icon-wrapper {
  background: linear-gradient(135deg, #eeebff 0%, #e0e7ff 100%);
  color: #6366f1;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: inset 0 2px 4px rgba(255,255,255,0.4);
}

.header-text h3 {
  margin: 0 0 2px 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.client-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.divider-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background-color: #cbd5e1;
}

.client-type-pill {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 6px;
}

/* Vertical layout */
.drawer-layout-container {
  display: flex;
  height: calc(100vh - 150px);
}

.drawer-sidebar {
  width: 200px;
  background-color: #fafbfe;
  border-right: 1px solid #f1f5f9;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: #64748b;
  font-size: 13.5px;
  font-weight: 550;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-left: 3px solid transparent;
}

.sidebar-item:hover {
  background-color: #f1f5f9;
  color: #0f172a;
}

.sidebar-item.active {
  background-color: rgba(99, 102, 241, 0.08);
  color: #4f46e5;
  font-weight: 600;
  border-left: 3px solid #6366f1;
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

/* External Config styles */
.external-configs-list {
  display: flex;
  flex-direction: column;
}

.external-config-cards-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.external-config-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 24px;
  background-color: #ffffff;
  transition: all 0.25s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.01);
}

.external-config-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  transform: translateY(-1px);
}

.card-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.platform-logo-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
}

.platform-logo-wrapper.wechat_mini {
  background-color: #e8fbf3;
  color: #10b981;
}

.platform-logo-wrapper.alipay_mini {
  background-color: #e0f2fe;
  color: #0284c7;
}

.platform-logo-wrapper.oidc {
  background-color: #f5f3ff;
  color: #7c3aed;
}

.platform-logo-wrapper.douyin_mini {
  background-color: #f1f5f9;
  color: #0f172a;
}

.platform-logo-wrapper.wechat_work {
  background-color: #eff6ff;
  color: #3b82f6;
}

.card-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title-row h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 650;
  color: #0f172a;
}

.platform-type-pill-small {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
  background-color: #f1f5f9;
  color: #64748b;
}

.card-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.card-meta-row code {
  font-family: monospace;
  background-color: #f8fafc;
  padding: 1px 4px;
  border-radius: 3px;
  color: #0f172a;
}

.card-actions {
  display: flex;
  align-items: center;
}

.mr-12 {
  margin-right: 12px;
}

.external-editor-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  background-color: #ffffff;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 12px;
}

.editor-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.pt-16 {
  padding-top: 16px;
}
.mt-16 {
  margin-top: 16px;
}
.mt-24 {
  margin-top: 24px;
}

/* Main display content container */
.drawer-main-content {
  flex: 1;
  padding: 28px 32px;
  overflow-y: auto;
  background-color: #ffffff;
}

.settings-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 16px 0;
  border-left: 3px solid #6366f1;
  padding-left: 10px;
}

.panel-section-subtitle {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

/* Form Styles */
.form-container-styled :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-container-styled :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  font-size: 13px;
  padding-bottom: 6px;
}

.secret-input-wrapper {
  display: flex;
  gap: 12px;
  width: 100%;
}

.secret-field {
  flex: 1;
}

.field-hint-text {
  font-size: 11.5px;
  color: #94a3b8;
  margin-top: 4px;
  line-height: 1.4;
}

/* Grant types checkbox cards */
.checkbox-cards-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkbox-card-item {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px;
  transition: all 0.2s;
  background-color: #f8fafc;
}

.checkbox-card-item:hover {
  border-color: #cbd5e1;
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.checkbox-card-item :deep(.el-checkbox__label) {
  font-weight: 600;
  color: #1e293b;
  font-size: 13.5px;
}

.checkbox-card-item p {
  margin: 6px 0 0 24px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.4;
}

/* Split menu style inside sidebar drawer */
.settings-panel.split-mode {
  height: calc(100vh - 210px);
}

.tab-pane-split-container {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 16px;
  height: 100%;
}

.tree-panel, .editor-panel {
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  height: 42px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 13px;
  font-weight: 600;
  color: #475569;
}

.tree-wrapper-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 32px;
  font-size: 13px;
}

.node-code {
  font-size: 10px;
  margin-left: 4px;
}

.node-actions {
  gap: 2px;
}

.panel-empty-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #94a3b8;
  text-align: center;
  padding: 20px;
}

.placeholder-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.placeholder-content p {
  font-size: 12px;
}

.form-content-inner {
  padding: 16px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 16px;
}

/* Permissions Pane */
.permissions-tab-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.pane-action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.pane-title-left h3 {
  margin: 0 0 2px 0;
  font-size: 14.5px;
  font-weight: 700;
}

.pane-title-left span {
  font-size: 12px;
  color: #94a3b8;
}

.permission-inner-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.code-badge {
  font-family: monospace;
  background-color: #f1f5f9;
  color: #334155;
  padding: 2px 5px;
  border-radius: 4px;
  font-size: 11.5px;
}

.api-method-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 4px;
  margin-right: 6px;
}

.method-post {
  background-color: #fef3c7;
  color: #d97706;
}

.method-get {
  background-color: #dcfce7;
  color: #15803d;
}

.api-path-text {
  font-family: monospace;
  font-size: 12px;
}

/* Token settings */
.time-input-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.unit-text {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

/* Footer elements */
.drawer-footer-inner {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
  padding: 4px 12px;
}

.btn-drawer-close {
  font-weight: 600;
}

.btn-drawer-confirm {
  font-weight: 600;
}

/* Info Box */
.info-box {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  gap: 16px;
  margin-top: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01);
}

.info-icon {
  width: 40px;
  height: 40px;
  background-color: #eeebff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 20px;
  flex-shrink: 0;
}

.info-content-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  flex-grow: 1;
}

.info-left {
  flex: 1;
  min-width: 300px;
}

.info-left h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #0f172a;
  font-weight: 650;
}

.info-left p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.info-right {
  flex: 2;
  min-width: 400px;
}

.info-right ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}

.info-right li {
  margin-bottom: 4px;
}

.info-right li:last-child {
  margin-bottom: 0;
}

.type-badge {
  display: inline-block;
  margin-left: 8px;
  color: #334155;
  font-weight: 600;
}

/* Animations */
.animate-panel-fade-in {
  animation: panelFadeIn 0.3s ease-out forwards;
}

@keyframes panelFadeIn {
  from {
    opacity: 0;
    transform: translateX(10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}




.drawer-tab-view :deep(.el-tabs__item) {
  font-size: 14.5px;
  font-weight: 600;
  color: #64748b;
  height: 52px;
  line-height: 52px;
}

.drawer-tab-view :deep(.el-tabs__item.is-active) {
  color: #6366f1;
}

.drawer-tab-view :deep(.el-tabs__active-bar) {
  background-color: #6366f1;
  height: 3px;
  border-radius: 9px;
}

.tab-label-custom {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Tab Split layout for tree & details */
.tab-pane-split-container {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 24px;
  height: calc(100vh - 250px);
  margin-top: 16px;
}

.tree-panel, .editor-panel {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01);
}

.panel-header {
  height: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 13.5px;
  font-weight: 650;
  color: #334155;
}

.btn-add-root-menu {
  font-weight: 600;
}

.tree-wrapper-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* Custom tree node appearance */
.custom-el-tree {
  --el-tree-node-hover-bg-color: #f5f3ff;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
  height: 36px;
  font-size: 13.5px;
}

.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.node-icon {
  color: #94a3b8;
  display: flex;
  align-items: center;
}

.node-label.is-disabled {
  color: #94a3b8;
  text-decoration: line-through;
}

.node-code {
  font-size: 11px;
  color: #94a3b8;
  background-color: #f8fafc;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid #f1f5f9;
}

.node-actions {
  display: none;
  gap: 4px;
}

.custom-tree-node:hover .node-actions {
  display: flex;
}

/* Editor panel on the right side */
.editor-panel {
  padding: 0;
  background-color: #ffffff;
}

.panel-empty-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: 40px;
  color: #94a3b8;
  text-align: center;
}

.placeholder-icon {
  font-size: 40px;
  margin-bottom: 12px;
  color: #cbd5e1;
}

.panel-empty-placeholder p {
  font-size: 13px;
  max-width: 240px;
  line-height: 1.5;
}

.form-content-inner {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.menu-node-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
  font-size: 13px;
}

.btn-editor-cancel {
  font-weight: 600;
}
.btn-editor-save {
  font-weight: 600;
}

/* Permissions Pane */
.permissions-tab-pane {
  padding-top: 16px;
  height: calc(100vh - 250px);
  overflow-y: auto;
}

.pane-action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.pane-title-left h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 750;
  color: #0f172a;
}

.pane-title-left span {
  font-size: 12.5px;
  color: #64748b;
}

.btn-add-permission {
  font-weight: 600;
}

.permission-inner-table {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.code-badge {
  font-family: monospace;
  background-color: #f1f5f9;
  color: #0f172a;
  padding: 3px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.api-method-badge {
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
}

.method-post {
  background-color: #fef3c7;
  color: #d97706;
}

.method-get {
  background-color: #dcfce7;
  color: #15803d;
}

.api-path-text {
  font-family: monospace;
  font-size: 12.5px;
  color: #334155;
}

/* Footer elements */
.drawer-footer-inner {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  width: 100%;
}

.btn-drawer-close {
  font-weight: 600;
  border-color: #cbd5e1;
}

.btn-drawer-confirm {
  font-weight: 600;
}

/* Info Box */
.info-box {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  gap: 16px;
  margin-top: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.01);
}

.info-icon {
  width: 40px;
  height: 40px;
  background-color: #eeebff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6366f1;
  font-size: 20px;
  flex-shrink: 0;
}

.info-content-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 40px;
  flex-grow: 1;
}

.info-left {
  flex: 1;
  min-width: 300px;
}

.info-left h4 {
  margin: 0 0 8px 0;
  font-size: 15px;
  color: #0f172a;
  font-weight: 650;
}

.info-left p {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

.info-right {
  flex: 2;
  min-width: 400px;
}

.info-right ul {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #64748b;
  line-height: 1.8;
}

.info-right li {
  margin-bottom: 4px;
}

.info-right li:last-child {
  margin-bottom: 0;
}

.type-badge {
  display: inline-block;
  margin-left: 8px;
  color: #334155;
  font-weight: 600;
}
</style>
