<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { 
  Back,
  Plus, 
  Document, 
  Lock, 
  Menu as IconMenu, 
  InfoFilled, 
  Edit, 
  Delete, 
  Compass,
  Connection,
  Check,
  Clock,
  Share,
  ArrowDown
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appClientApi, type AppClientRecord, type AppClientPayload, type AppClientExternalConfigRecord, type AppClientExternalConfigPayload } from '@/api/appClient'
import { authPolicyApi, type AuthPolicyOptionResponse } from '@/api/authPolicy'
import { loginPageApi, type LoginPageRecord } from '@/api/loginPage'
import { menuApi, type AppMenuRecord, type AppMenuPayload } from '@/api/menu'
import { appPermissionApi, type AppPermissionRecord, type AppPermissionPayload } from '@/api/appPermission'
import { realmApi, type RealmRecord } from '@/api/realm'

const route = useRoute()
const router = useRouter()

const appId = ref(route.params.id as string)
const clientId = ref(route.params.clientId as string)

const loading = ref(false)
const submitLoading = ref(false)
const clientInfo = ref<AppClientRecord | null>(null)
const drawerActiveTab = ref('basic')

const realmOptions = ref<RealmRecord[]>([])
const loginPageOptions = ref<LoginPageRecord[]>([])
const authPolicyOptions = ref<AuthPolicyOptionResponse[]>([])

const isServiceClient = computed(() => {
  const type = Number(clientInfo.value?.clientType)
  return type === 5 || type === 6
})

const loginModeOptions = [
  { label: '认证中心托管登录页', value: 'IAM_HOSTED' },
  { label: '跳转客户自有登录页', value: 'EXTERNAL_REDIRECT' },
  { label: '客户自有登录体系，仅接口接入', value: 'API_ONLY' }
]

const ossConfigOptions = computed(() => currentExternalConfigs.value.filter(item => item.providerType === 'OSS'))

// Mock Menu Data for Frontend Showcase
interface MenuNode {
  id: string
  parentId?: string
  label: string
  code: string
  path: string
  component?: string
  status: number
  visible?: number
  children?: MenuNode[]
}

const currentMenus = ref<MenuNode[]>([])

// Form to edit/create menu
const isEditingMenu = ref(false)
const selectedMenuNode = ref<MenuNode | null>(null)
const menuFormMode = ref<'create' | 'edit'>('create')
const menuFormRef = ref()
const menuFormData = reactive({
  id: '',
  parentId: '',
  label: '',
  code: '',
  path: '',
  component: '',
  status: 1,
  visible: 1
})

const menuFormRules = {
  label: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入唯一权限标识码', trigger: 'blur' }],
  path: [{ required: true, message: '请输入路由访问路径', trigger: 'blur' }]
}

const parentMenuOptions = computed(() => {
  const filterNode = (nodes: MenuNode[]): any[] => {
    return nodes
      .filter(node => node.id !== menuFormData.id)
      .map(node => ({
        value: node.id,
        label: node.label,
        children: node.children ? filterNode(node.children) : []
      }))
  }
  return [
    {
      value: '0',
      label: '无 (作为一级菜单)',
      children: filterNode(currentMenus.value)
    }
  ]
})

const currentPermissions = ref<AppPermissionRecord[]>([])

const permissionQuery = reactive({
  page: 1,
  size: 10,
  permissionType: undefined as number | undefined
})
const permissionTotal = ref(0)

const handlePermissionFilterChange = () => {
  permissionQuery.page = 1
  loadClientPermissions()
}

// Permission Form State
const isEditingPermission = ref(false)
const permissionFormMode = ref<'create' | 'edit'>('create')
const permissionFormRef = ref()
const permissionFormData = reactive({
  id: '',
  menuId: '',
  permissionCode: '',
  permissionName: '',
  permissionType: 2,
  apiPath: '',
  method: 'GET',
  description: '',
  status: 1
})

const permissionFormRules = {
  permissionName: [{ required: true, message: '请输入权限资源名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限唯一编码', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

const menuTreeOptions = computed(() => {
  const mapNode = (nodes: MenuNode[]): any[] => {
    return nodes.map(node => ({
      value: node.id,
      label: node.label,
      children: node.children ? mapNode(node.children) : []
    }))
  }
  return mapNode(currentMenus.value)
})

// Mock External configurations
interface ExternalConfig {
  id: string
  providerType: string
  providerCode: string
  providerName: string
  appId?: string
  appSecret?: string
  callbackUrl?: string
  configJson?: string
  status: number
}

const currentExternalConfigs = ref<ExternalConfig[]>([])

const platformTypeOptions = [
  { label: '微信小程序 (WECHAT_MINI)', value: 'WECHAT_MINI' },
  { label: '支付宝小程序 (ALIPAY_MINI)', value: 'ALIPAY_MINI' },
  { label: '抖音小程序 (DOUYIN_MINI)', value: 'DOUYIN_MINI' },
  { label: 'OIDC 身份源 (OIDC)', value: 'OIDC' },
  { label: '企业微信 (WECHAT_WORK)', value: 'WECHAT_WORK' },
  { label: '对象存储 (OSS)', value: 'OSS' }
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

const fetchClientConfigOptions = async () => {
  try {
    const [realmPage, loginPages, authPolicies] = await Promise.all([
      realmApi.page({ page: 1, size: 100, status: 1 }),
      loginPageApi.list(),
      authPolicyApi.list()
    ])
    realmOptions.value = realmPage.records ?? []
    loginPageOptions.value = loginPages ?? []
    authPolicyOptions.value = authPolicies ?? []
  } catch (error: any) {
    ElMessage.error(error.message || '加载客户端配置选项失败')
  }
}

const loadClientMenus = async () => {
  try {
    const list = await menuApi.listByClient(clientId.value)
    const mapNode = (item: AppMenuRecord): MenuNode => ({
      id: item.id,
      parentId: item.parentId,
      label: item.menuName,
      code: item.menuCode,
      path: item.routePath || '',
      component: item.componentPath,
      status: item.status,
      visible: item.visible,
      children: item.children ? item.children.map(mapNode) : []
    })
    currentMenus.value = list.map(mapNode)
  } catch (error: any) {
    ElMessage.error(error.message || '获取菜单资源失败')
  }
}

const loadClientPermissions = async () => {
  try {
    const res = await appPermissionApi.page(clientId.value, {
      page: permissionQuery.page,
      size: permissionQuery.size,
      permissionType: permissionQuery.permissionType
    })
    currentPermissions.value = res.records
    permissionTotal.value = res.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取权限列表失败')
  }
}

const fetchClientDetail = async () => {
  if (!clientId.value) return
  loading.value = true
  try {
    const data = await appClientApi.detail(clientId.value)
    data.defaultRealmId = data.defaultRealmId ?? data.realmId ?? null
    data.loginMode = [5, 6].includes(Number(data.clientType)) ? 'SERVICE' : data.loginMode || 'IAM_HOSTED'
    clientInfo.value = data
    await loadExternalConfigs()
  } catch (error: any) {
    ElMessage.error(error.message || '获取客户端详情失败')
  } finally {
    loading.value = false
  }
}

const mapExternalConfig = (item: AppClientExternalConfigRecord): ExternalConfig => ({
  id: item.id,
  providerType: item.providerType,
  providerCode: item.providerCode,
  providerName: item.providerName,
  appId: item.appId,
  appSecret: item.appSecret,
  callbackUrl: item.callbackUrl,
  configJson: item.configJson,
  status: item.status
})

const loadExternalConfigs = async () => {
  if (!clientId.value) return
  try {
    const list = await appClientApi.listExternalConfigs(clientId.value)
    currentExternalConfigs.value = list.map(mapExternalConfig)
  } catch (error: any) {
    ElMessage.error(error.message || '获取外部平台配置失败')
  }
}

const handleLoginModeChange = (mode: string) => {
  if (!clientInfo.value) return
  clientInfo.value.loginMode = mode
  if (mode !== 'IAM_HOSTED') {
    clientInfo.value.loginPageId = null
  }
  if (mode !== 'EXTERNAL_REDIRECT') {
    clientInfo.value.externalLoginUrl = ''
  }
}

watch(drawerActiveTab, async (newTab) => {
  if (!clientId.value) return
  if (newTab === 'menus') {
    await loadClientMenus()
  } else if (newTab === 'permissions') {
    await loadClientPermissions()
  }
})

const handleBack = () => {
  router.push(`/applications/detail/${appId.value}`)
}

const copyToClipboard = (text: string) => {
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('密钥已成功复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// Save action
const handleSaveConfig = async () => {
  if (!clientInfo.value) return
  submitLoading.value = true
  try {
    const payload: AppClientPayload = {
      clientCode: clientInfo.value.clientCode,
      clientName: clientInfo.value.clientName,
      clientType: clientInfo.value.clientType,
      status: clientInfo.value.status,
      description: clientInfo.value.description,
      realmId: clientInfo.value.defaultRealmId ?? clientInfo.value.realmId,
      defaultRealmId: clientInfo.value.defaultRealmId,
      defaultEntryUrl: clientInfo.value.defaultEntryUrl,
      loginMode: isServiceClient.value ? 'SERVICE' : clientInfo.value.loginMode || 'IAM_HOSTED',
      externalLoginUrl: clientInfo.value.loginMode === 'EXTERNAL_REDIRECT' ? clientInfo.value.externalLoginUrl : undefined,
      loginCallbackUrl: clientInfo.value.loginCallbackUrl,
      loginPageId: clientInfo.value.loginMode === 'IAM_HOSTED' ? clientInfo.value.loginPageId : undefined,
      authPolicyId: clientInfo.value.authPolicyId,
      ossConfigId: clientInfo.value.ossConfigId
    }
    await appClientApi.edit(clientId.value, payload)
    ElMessage.success('客户端配置已成功保存')
    handleBack()
  } catch (error: any) {
    ElMessage.error(error.message || '保存客户端配置失败')
  } finally {
    submitLoading.value = false
  }
}

// Menu structures handlers
const handleAddRootMenu = () => {
  selectedMenuNode.value = null
  menuFormMode.value = 'create'
  menuFormData.id = ''
  menuFormData.parentId = '0'
  menuFormData.label = ''
  menuFormData.code = ''
  menuFormData.path = ''
  menuFormData.component = ''
  menuFormData.status = 1
  menuFormData.visible = 1
  isEditingMenu.value = true
}

const handleAddSubMenu = (node: MenuNode) => {
  menuFormMode.value = 'create'
  menuFormData.id = ''
  menuFormData.parentId = node.id
  menuFormData.label = ''
  menuFormData.code = ''
  menuFormData.path = ''
  menuFormData.component = ''
  menuFormData.status = 1
  menuFormData.visible = 1
  isEditingMenu.value = true
}

const handleEditMenu = (node: MenuNode) => {
  selectedMenuNode.value = node
  menuFormMode.value = 'edit'
  menuFormData.id = node.id
  menuFormData.parentId = node.parentId || '0'
  menuFormData.label = node.label
  menuFormData.code = node.code
  menuFormData.path = node.path
  menuFormData.component = node.component || ''
  menuFormData.status = node.status
  menuFormData.visible = node.visible !== undefined ? node.visible : 1
  isEditingMenu.value = true
}

const handleHeaderAddCommand = (command: string) => {
  if (command === 'sub' && selectedMenuNode.value) {
    handleAddSubMenu(selectedMenuNode.value)
  } else {
    handleAddRootMenu()
  }
}

const handleNodeDrop = async (
  draggingNode: any,
  dropNode: any,
  dropType: string
) => {
  try {
    const draggingData = draggingNode.data as MenuNode
    let newParentId = '0'
    
    if (dropType === 'inner') {
      newParentId = dropNode.data.id
    } else {
      newParentId = dropNode.data.parentId || '0'
    }
    
    const payload: AppMenuPayload = {
      parentId: newParentId,
      menuCode: draggingData.code,
      menuName: draggingData.label,
      routePath: draggingData.path || undefined,
      componentPath: draggingData.component || undefined,
      sortNo: 0,
      visible: draggingData.visible !== undefined ? draggingData.visible : 1,
      status: draggingData.status
    }
    
    await menuApi.edit(draggingData.id, payload)
    ElMessage.success('已通过拖拽更新菜单结构')
    await loadClientMenus()
  } catch (error: any) {
    ElMessage.error(error.message || '拖拽更新失败')
    await loadClientMenus()
  }
}

const handleDeleteMenu = (node: MenuNode) => {
  ElMessageBox.confirm(`确定删除菜单「${node.label}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await menuApi.delete(node.id)
      ElMessage.success('菜单已删除')
      await loadClientMenus()
      if (selectedMenuNode.value?.id === node.id) {
        selectedMenuNode.value = null
      }
      if (isEditingMenu.value && menuFormData.id === node.id) {
        isEditingMenu.value = false
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除菜单失败')
    }
  }).catch(() => {})
}

const findNodeByCode = (nodes: MenuNode[], code: string): MenuNode | null => {
  for (const node of nodes) {
    if (node.code === code) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findNodeByCode(node.children, code)
      if (found) return found
    }
  }
  return null
}

const saveMenuForm = async () => {
  const valid = await menuFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  const targetCode = menuFormData.code
  const payload: AppMenuPayload = {
    parentId: menuFormData.parentId ? menuFormData.parentId : '0',
    menuCode: menuFormData.code,
    menuName: menuFormData.label,
    routePath: menuFormData.path || undefined,
    componentPath: menuFormData.component || undefined,
    sortNo: 0,
    visible: menuFormData.visible,
    status: menuFormData.status
  }
  
  try {
    if (menuFormMode.value === 'create') {
      await menuApi.create(clientId.value, payload)
      ElMessage.success('成功新增菜单项')
    } else {
      await menuApi.edit(menuFormData.id, payload)
      ElMessage.success('已保存菜单修改')
    }
    
    await loadClientMenus()
    
    // Auto-select and show details of the newly created or updated node
    const savedNode = findNodeByCode(currentMenus.value, targetCode)
    if (savedNode) {
      handleEditMenu(savedNode)
    } else {
      isEditingMenu.value = false
      selectedMenuNode.value = null
    }
  } catch (error: any) {
    ElMessage.error(error.message || '保存菜单失败')
  }
}

// Permission managers
const handleAddPermission = () => {
  permissionFormMode.value = 'create'
  permissionFormData.id = ''
  permissionFormData.menuId = ''
  permissionFormData.permissionCode = ''
  permissionFormData.permissionName = ''
  permissionFormData.permissionType = 2
  permissionFormData.apiPath = ''
  permissionFormData.method = 'GET'
  permissionFormData.description = ''
  permissionFormData.status = 1
  isEditingPermission.value = true
}

const handleEditPermission = (row: AppPermissionRecord) => {
  permissionFormMode.value = 'edit'
  permissionFormData.id = row.id
  permissionFormData.menuId = row.menuId || ''
  permissionFormData.permissionCode = row.permissionCode
  permissionFormData.permissionName = row.permissionName
  permissionFormData.permissionType = row.permissionType
  permissionFormData.apiPath = row.apiPath || ''
  permissionFormData.method = row.method || 'GET'
  permissionFormData.description = row.description || ''
  permissionFormData.status = row.status
  isEditingPermission.value = true
}

const handleDeletePermission = (row: AppPermissionRecord) => {
  ElMessageBox.confirm(`确定删除权限「${row.permissionName}」吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await appPermissionApi.delete(row.id)
      ElMessage.success('权限已删除')
      await loadClientPermissions()
    } catch (error: any) {
      ElMessage.error(error.message || '删除权限失败')
    }
  }).catch(() => {})
}

const savePermissionForm = async () => {
  const valid = await permissionFormRef.value?.validate().catch(() => false)
  if (!valid) return
  
  const payload: AppPermissionPayload = {
    menuId: permissionFormData.menuId ? permissionFormData.menuId : undefined,
    permissionCode: permissionFormData.permissionCode,
    permissionName: permissionFormData.permissionName,
    permissionType: permissionFormData.permissionType,
    apiPath: permissionFormData.permissionType === 2 && permissionFormData.apiPath ? permissionFormData.apiPath : undefined,
    method: permissionFormData.permissionType === 2 && permissionFormData.method ? permissionFormData.method : undefined,
    description: permissionFormData.description || undefined,
    status: permissionFormData.status
  }
  
  try {
    if (permissionFormMode.value === 'create') {
      await appPermissionApi.create(clientId.value, payload)
      ElMessage.success('成功新增权限项')
    } else {
      await appPermissionApi.edit(permissionFormData.id, payload)
      ElMessage.success('已保存权限修改')
    }
    isEditingPermission.value = false
    await loadClientPermissions()
  } catch (error: any) {
    ElMessage.error(error.message || '保存权限失败')
  }
}

// External platforms
const isEditingExternal = ref(false)
const externalFormMode = ref<'create' | 'edit'>('create')
const externalFormRef = ref()
const externalFormData = reactive({
  id: '',
  providerType: 'WECHAT_MINI',
  providerCode: '',
  providerName: '',
  appId: '',
  appSecret: '',
  callbackUrl: '',
  configJson: '',
  status: 1
})

const externalFormRules = {
  providerCode: [{ required: true, message: '请输入配置编码', trigger: 'blur' }],
  providerName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  appId: [{ required: true, message: '请输入 AppID / Client ID', trigger: 'blur' }],
  appSecret: [{ required: true, message: '请输入 Secret', trigger: 'blur' }]
}

const handleAddExternal = () => {
  externalFormMode.value = 'create'
  externalFormData.id = 'ext_' + Date.now()
  externalFormData.providerType = 'WECHAT_MINI'
  externalFormData.providerCode = ''
  externalFormData.providerName = ''
  externalFormData.appId = ''
  externalFormData.appSecret = ''
  externalFormData.callbackUrl = ''
  externalFormData.configJson = ''
  externalFormData.status = 1
  isEditingExternal.value = true
}

const handleEditExternal = (item: ExternalConfig) => {
  externalFormMode.value = 'edit'
  externalFormData.id = item.id
  externalFormData.providerType = item.providerType
  externalFormData.providerCode = item.providerCode
  externalFormData.providerName = item.providerName
  externalFormData.appId = item.appId || ''
  externalFormData.appSecret = item.appSecret || ''
  externalFormData.callbackUrl = item.callbackUrl || ''
  externalFormData.configJson = item.configJson || ''
  externalFormData.status = item.status
  isEditingExternal.value = true
}

const handleDeleteExternal = (index: number) => {
  ElMessageBox.confirm('确定禁用此外部平台配置吗？当前后端保留配置记录。', '提示', {
    type: 'warning'
  }).then(async () => {
    const item = currentExternalConfigs.value[index]
    await appClientApi.disableExternalConfig(item.id)
    item.status = 2
    ElMessage.success('配置已禁用')
  }).catch(() => {})
}

const saveExternalForm = async () => {
  const valid = await externalFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const payload: AppClientExternalConfigPayload = {
    providerType: externalFormData.providerType,
    providerCode: externalFormData.providerCode,
    providerName: externalFormData.providerName,
    appId: externalFormData.appId || undefined,
    appSecret: externalFormData.appSecret || undefined,
    callbackUrl: externalFormData.callbackUrl || undefined,
    configJson: externalFormData.configJson || undefined,
    status: externalFormData.status
  }

  if (externalFormMode.value === 'create') {
    await appClientApi.createExternalConfig(clientId.value, payload)
    ElMessage.success('成功新增外部平台配置')
  } else {
    await appClientApi.editExternalConfig(externalFormData.id, payload)
    ElMessage.success('修改已保存')
  }
  isEditingExternal.value = false
  await loadExternalConfigs()
}

onMounted(async () => {
  await fetchClientConfigOptions()
  await fetchClientDetail()
})
</script>

<template>
  <div class="client-config-page" v-loading="loading">
    <!-- Top Header -->
    <div class="page-header" v-if="clientInfo">
      <div class="back-btn" @click="handleBack">
        <el-icon><Back /></el-icon>
      </div>
      <div class="header-titles">
        <h2>客户端配置</h2>
        <div class="client-meta">
          <span class="client-name">{{ clientInfo.clientName }}</span>
          <span class="divider-dot"></span>
          <span class="client-id-code">ID: {{ clientInfo.clientCode }}</span>
          <span class="divider-dot"></span>
          <span class="client-type-badge">{{ clientTypeText(clientInfo.clientType) }}</span>
        </div>
      </div>
    </div>

    <!-- Main Workspace Area -->
    <div class="workspace-container" v-if="clientInfo">
      <!-- Left Vertical Sidebar Navigator -->
      <div class="workspace-sidebar">
        <div 
          class="sidebar-item" 
          :class="{ active: drawerActiveTab === 'basic' }"
          @click="drawerActiveTab = 'basic'"
        >
          <el-icon><InfoFilled /></el-icon>
          <span>登录入口</span>
        </div>
        <div 
          class="sidebar-item" 
          :class="{ active: drawerActiveTab === 'security' }"
          @click="drawerActiveTab = 'security'"
        >
          <el-icon><Lock /></el-icon>
          <span>重定向安全</span>
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
          <span>菜单资源</span>
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

      <!-- Right Main Panel Container -->
      <div class="workspace-main-panel">
        <!-- 1. Basic Settings -->
        <div v-if="drawerActiveTab === 'basic'" class="settings-panel animate-panel-fade-in">
          <h4 class="panel-section-title">基本属性</h4>
          <el-form label-position="top" class="form-container-styled max-w-720">
            <el-form-item label="客户端名称">
              <el-input v-model="clientInfo.clientName" />
            </el-form-item>
            <el-form-item label="客户端 ID (client_id)">
              <el-input v-model="clientInfo.clientCode" disabled />
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
              <el-select v-model="clientInfo.defaultRealmId" placeholder="请选择客户端登录默认身份域" clearable style="width: 100%">
                <el-option 
                  v-for="realm in realmOptions" 
                  :key="realm.id" 
                  :label="realm.name" 
                  :value="realm.id" 
                />
              </el-select>
              <span class="field-hint-text">用户通过此客户端进行登录时，默认将账号鉴权指向该身份空间。</span>
            </el-form-item>

            <el-form-item v-if="!isServiceClient" label="登录接入方式">
              <el-select v-model="clientInfo.loginMode" placeholder="请选择登录接入方式" style="width: 100%" @change="handleLoginModeChange">
                <el-option
                  v-for="item in loginModeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
              <span class="field-hint-text">区分认证中心托管登录、客户自有登录页跳转，以及客户系统自有登录体系的接口接入。</span>
            </el-form-item>

            <el-form-item v-if="!isServiceClient && clientInfo.loginMode === 'IAM_HOSTED'" label="登录入口模板">
              <el-select v-model="clientInfo.loginPageId" placeholder="请选择登录入口模板" clearable style="width: 100%">
                <el-option
                  v-for="page in loginPageOptions"
                  :key="page.id"
                  :label="page.name"
                  :value="page.id"
                />
              </el-select>
              <span class="field-hint-text">客户端未登录时展示认证中心托管登录入口模板。</span>
            </el-form-item>

            <el-form-item v-if="!isServiceClient && clientInfo.loginMode === 'EXTERNAL_REDIRECT'" label="客户登录页地址">
              <el-input v-model="clientInfo.externalLoginUrl" placeholder="如：https://customer.example.com/login" />
              <span class="field-hint-text">用户访问该客户端时跳转到客户自己的登录页。</span>
            </el-form-item>

            <el-form-item v-if="!isServiceClient && clientInfo.loginMode === 'API_ONLY'" label="接口接入说明">
              <el-input model-value="客户系统自有登录体系，仅调用注册账户与获取登录信息接口，不做登录页转发。" disabled />
              <span class="field-hint-text">该模式不需要登录入口模板，也不配置外部登录页跳转。</span>
            </el-form-item>

            <el-form-item label="覆盖认证策略">
              <el-select v-model="clientInfo.authPolicyId" placeholder="默认使用身份域认证策略" clearable style="width: 100%">
                <el-option
                  v-for="policy in authPolicyOptions"
                  :key="policy.id"
                  :label="policy.name"
                  :value="policy.id"
                />
              </el-select>
              <span class="field-hint-text">为空时继承身份域默认认证策略；仅当前客户端需要差异化时配置。</span>
            </el-form-item>

            <el-form-item label="客户端 OSS 配置">
              <el-select v-model="clientInfo.ossConfigId" placeholder="请选择当前客户端绑定的 OSS 配置" clearable style="width: 100%">
                <el-option
                  v-for="item in ossConfigOptions"
                  :key="item.id"
                  :label="item.providerName"
                  :value="item.id"
                />
              </el-select>
              <span class="field-hint-text">一个应用多个客户端时，每个客户端可绑定独立 OSS 处理。</span>
            </el-form-item>

            <el-form-item label="默认入口地址 (default_entry_url)">
              <el-input v-model="clientInfo.defaultEntryUrl" placeholder="如: https://merchant.example.com/login" />
              <span class="field-hint-text">客户端承接 SSO 登录、回跳和默认访问的入口地址。</span>
            </el-form-item>

            <el-form-item label="登录回调地址">
              <el-input v-model="clientInfo.loginCallbackUrl" placeholder="如: https://app.example.com/auth/callback" />
            </el-form-item>

            <el-form-item label="描述说明">
              <el-input v-model="clientInfo.description" type="textarea" :rows="4" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 2. Security & Redirection -->
        <div v-if="drawerActiveTab === 'security'" class="settings-panel animate-panel-fade-in">
          <h4 class="panel-section-title">安全与重定向</h4>
          <el-form label-position="top" class="form-container-styled max-w-720">
            <el-form-item label="允许的回调重定向 URI (Redirect URIs)">
              <el-input 
                v-model="clientInfo.defaultEntryUrl" 
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
          
          <div class="checkbox-cards-grid max-w-720">
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
              <h3>外部平台与 OSS 配置</h3>
              <span>配置当前客户端绑定的微信、支付宝、OIDC 或对象存储参数。</span>
            </div>
            <el-button type="primary" :icon="Plus" @click="handleAddExternal" class="btn-add-permission" v-if="!isEditingExternal">新增配置</el-button>
          </div>

          <!-- List of External Configs -->
          <div v-if="!isEditingExternal" class="external-configs-list mt-16">
            <div v-if="currentExternalConfigs.length === 0" class="panel-empty-placeholder">
              <el-icon class="placeholder-icon"><Share /></el-icon>
              <p>暂无绑定的外部平台或 OSS 配置，点击右上角“新增配置”开始维护。</p>
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
          <div v-else class="external-editor-card mt-16 animate-panel-fade-in max-w-960">
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
                  <el-form-item :label="externalFormData.providerType === 'OSS' ? 'AccessKey / 访问标识' : '第三方 AppID (app_id)'" prop="appId" required>
                    <el-input v-model="externalFormData.appId" :placeholder="externalFormData.providerType === 'OSS' ? '输入 OSS AccessKey 或访问标识' : '输入第三方的 AppID 或 Client ID'" />
                  </el-form-item>
                </div>
              </div>

              <div class="config-row">
                <div class="config-col">
                  <el-form-item :label="externalFormData.providerType === 'OSS' ? 'SecretKey / 访问密钥' : '通道密钥 (AppSecret)'" prop="appSecret" required>
                    <el-input v-model="externalFormData.appSecret" type="password" show-password :placeholder="externalFormData.providerType === 'OSS' ? '请输入 OSS SecretKey，安全存储' : '请输入对应的 AppSecret，安全存储'" />
                  </el-form-item>
                </div>
                <div class="config-col">
                  <el-form-item :label="externalFormData.providerType === 'OSS' ? '公开访问域名' : '自定义回调地址 (Callback URL)'">
                    <el-input v-model="externalFormData.callbackUrl" :placeholder="externalFormData.providerType === 'OSS' ? '如：https://cdn.example.com' : '例如 OIDC 的登录回调路径 (选填)'" />
                  </el-form-item>
                </div>
              </div>

              <el-form-item label="扩展配置 JSON">
                <el-input
                  v-model="externalFormData.configJson"
                  type="textarea"
                  :rows="4"
                  placeholder='OSS 示例：{"provider":"MINIO","endpoint":"https://oss.example.com","bucket":"authsphere","region":"cn-east-1","pathPrefix":"clients/mall"}'
                />
              </el-form-item>

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
                <el-dropdown v-if="selectedMenuNode" trigger="click" @command="handleHeaderAddCommand">
                  <el-button type="primary" size="small" :icon="Plus">
                    添加菜单 <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="root">添加根菜单</el-dropdown-item>
                      <el-dropdown-item command="sub">添加子菜单 (在「{{ selectedMenuNode.label }}」下)</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <el-button v-else type="primary" size="small" :icon="Plus" @click="handleAddRootMenu" class="btn-add-root-menu">
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
                  @node-click="handleEditMenu"
                  draggable
                  @node-drop="handleNodeDrop"
                  highlight-current
                >
                  <template #default="{ data }">
                    <div class="custom-tree-node">
                      <div class="node-info">
                        <span class="node-icon"><el-icon><Document /></el-icon></span>
                        <span class="node-label" :class="{ 'is-disabled': data.status !== 1 }">{{ data.label }}</span>
                        <span class="node-code">{{ data.code }}</span>
                        <span v-if="data.visible === 0" class="node-visibility-badge">隐藏</span>
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
                  <el-form-item label="上级菜单" prop="parentId">
                    <el-tree-select
                      v-model="menuFormData.parentId"
                      :data="parentMenuOptions"
                      check-strictly
                      :render-after-expand="false"
                      placeholder="请选择上级菜单"
                      style="width: 100%"
                    />
                  </el-form-item>
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
                  <el-form-item label="是否隐藏">
                    <el-switch
                      v-model="menuFormData.visible"
                      :active-value="0"
                      :inactive-value="1"
                      active-text="隐藏"
                      inactive-text="显示"
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
          <!-- List of Permissions -->
          <div v-if="!isEditingPermission" class="permissions-tab-pane">
            <div class="pane-action-header" style="display: flex; justify-content: space-between; align-items: center;">
              <div class="pane-title-left">
                <h3>接口调用权限范围 (Scopes)</h3>
                <span>控制此应用端能够调用的 API 网关接口集合。</span>
              </div>
              <div style="display: flex; gap: 12px; align-items: center;">
                <el-select
                  v-model="permissionQuery.permissionType"
                  placeholder="权限类型"
                  clearable
                  style="width: 130px"
                  @change="handlePermissionFilterChange"
                >
                  <el-option :value="1" label="BUTTON" />
                  <el-option :value="2" label="API" />
                  <el-option :value="3" label="DATA" />
                </el-select>
                <el-button type="primary" :icon="Plus" @click="handleAddPermission" class="btn-add-permission">新增权限</el-button>
              </div>
            </div>

            <el-table :data="currentPermissions" style="width: 100%" border class="permission-inner-table mt-16">
              <el-table-column prop="permissionName" label="权限名称" min-width="140" />
              <el-table-column prop="permissionCode" label="权限编码 (code)" min-width="140">
                <template #default="{ row }">
                  <code class="code-badge">{{ row.permissionCode }}</code>
                </template>
              </el-table-column>
              <el-table-column prop="permissionType" label="权限类型" width="110">
                <template #default="{ row }">
                  <span v-if="row.permissionType === 1" class="permission-type-badge type-button">BUTTON</span>
                  <span v-else-if="row.permissionType === 2" class="permission-type-badge type-api">API</span>
                  <span v-else-if="row.permissionType === 3" class="permission-type-badge type-data">DATA</span>
                  <span v-else class="text-secondary">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="apiPath" label="API 接口地址" min-width="180">
                <template #default="{ row }">
                  <template v-if="row.apiPath">
                    <span class="api-method-badge" :class="'method-' + (row.method || 'GET').toLowerCase()">{{ row.method || 'GET' }}</span>
                    <span class="api-path-text">{{ row.apiPath }}</span>
                  </template>
                  <span v-else class="text-secondary">-</span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明描述" min-width="180" show-overflow-tooltip />
              <el-table-column label="操作" width="120" align="center" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" :icon="Edit" @click="handleEditPermission(row)"></el-button>
                  <el-button link type="danger" :icon="Delete" @click="handleDeletePermission(row)"></el-button>
                </template>
              </el-table-column>
            </el-table>

            <div class="pagination-container mt-16" style="display: flex; justify-content: flex-end;">
              <el-pagination
                v-model:current-page="permissionQuery.page"
                v-model:page-size="permissionQuery.size"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="permissionTotal"
                @size-change="loadClientPermissions"
                @current-change="loadClientPermissions"
              />
            </div>
          </div>

          <!-- Permission Editor Card -->
          <div v-else class="external-editor-card mt-16 animate-panel-fade-in max-w-960">
            <div class="editor-header">
              <h4>{{ permissionFormMode === 'create' ? '新增权限资源' : '编辑权限资源' }}</h4>
              <el-button link @click="isEditingPermission = false">返回权限列表</el-button>
            </div>
            <el-form
              ref="permissionFormRef"
              :model="permissionFormData"
              :rules="permissionFormRules"
              label-position="top"
              class="form-container-styled pt-16"
            >
              <div class="config-row">
                <div class="config-col">
                  <el-form-item label="权限名称" prop="permissionName" required>
                    <el-input v-model="permissionFormData.permissionName" placeholder="如: 查询商品列表" />
                  </el-form-item>
                </div>
                <div class="config-col">
                  <el-form-item label="权限编码" prop="permissionCode" required>
                    <el-input v-model="permissionFormData.permissionCode" placeholder="如: goods:query" :disabled="permissionFormMode === 'edit'" />
                  </el-form-item>
                </div>
              </div>

              <div class="config-row">
                <div class="config-col">
                  <el-form-item label="权限类型" prop="permissionType" required>
                    <el-select v-model="permissionFormData.permissionType" style="width: 100%">
                      <el-option :value="1" label="BUTTON (按钮/操作权限)" />
                      <el-option :value="2" label="API (网关接口调用权限)" />
                      <el-option :value="3" label="DATA (数据权限)" />
                    </el-select>
                  </el-form-item>
                </div>
                <div class="config-col">
                  <el-form-item label="关联菜单" prop="menuId">
                    <el-tree-select
                      v-model="permissionFormData.menuId"
                      :data="menuTreeOptions"
                      check-strictly
                      :render-after-expand="false"
                      placeholder="选择此权限绑定的菜单项 (可选)"
                      style="width: 100%"
                      clearable
                    />
                  </el-form-item>
                </div>
              </div>

              <div class="config-row" v-if="permissionFormData.permissionType === 2">
                <div class="config-col" style="flex: 0 0 200px;">
                  <el-form-item label="HTTP 方法" prop="method">
                    <el-select v-model="permissionFormData.method" style="width: 100%">
                      <el-option value="GET" label="GET" />
                      <el-option value="POST" label="POST" />
                      <el-option value="PUT" label="PUT" />
                      <el-option value="DELETE" label="DELETE" />
                      <el-option value="PATCH" label="PATCH" />
                    </el-select>
                  </el-form-item>
                </div>
                <div class="config-col">
                  <el-form-item label="API 接口路径" prop="apiPath">
                    <el-input v-model="permissionFormData.apiPath" placeholder="如: /api/merchant/goods/page" />
                  </el-form-item>
                </div>
              </div>

              <el-form-item label="权限说明描述" prop="description">
                <el-input type="textarea" :rows="3" v-model="permissionFormData.description" placeholder="请输入该权限代表的具体业务操作或接口调用说明。" />
              </el-form-item>

              <el-form-item label="状态">
                <el-switch
                  v-model="permissionFormData.status"
                  :active-value="1"
                  :inactive-value="2"
                  active-text="启用"
                  inactive-text="禁用"
                  inline-prompt
                />
              </el-form-item>

              <div class="editor-actions pt-16">
                <el-button @click="isEditingPermission = false">取消</el-button>
                <el-button type="primary" @click="savePermissionForm">保存权限资源</el-button>
              </div>
            </el-form>
          </div>
        </div>

        <!-- 7. Token Lifetime settings -->
        <div v-if="drawerActiveTab === 'tokens'" class="settings-panel animate-panel-fade-in">
          <h4 class="panel-section-title">令牌与会话时效 (Token Lifetime)</h4>
          <p class="panel-section-subtitle">配置此客户端所颁发 Access Token 与 Refresh Token 的有效生存周期。</p>
          
          <el-form label-position="top" class="form-container-styled mt-24 max-w-720">
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

    <!-- Fixed Bottom Action Bar -->
    <div class="fixed-footer-bar" v-if="clientInfo">
      <div class="footer-actions-wrapper">
        <template v-if="drawerActiveTab === 'menus' || drawerActiveTab === 'permissions'">
          <el-button @click="handleBack">返回详情</el-button>
        </template>
        <template v-else>
          <el-button @click="handleBack">取消返回</el-button>
          <el-button type="primary" @click="handleSaveConfig" :loading="submitLoading" :icon="Check">
            保存全部配置
          </el-button>
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.client-config-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  position: relative;
  background-color: #f8fafc;
  margin: -20px;
  overflow: hidden;
}

/* Page Header */
.page-header {
  display: flex;
  align-items: center;
  padding: 20px 32px;
  background-color: #ffffff;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.02);
}

.back-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: #475569;
  cursor: pointer;
  margin-right: 16px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #ffffff;
  transition: all 0.2s;
}

.back-btn:hover {
  background-color: #f1f5f9;
  border-color: #cbd5e1;
  color: #0f172a;
}

.header-titles h2 {
  margin: 0 0 4px 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.client-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  color: #64748b;
}

.client-name {
  font-weight: 600;
  color: #334155;
}

.divider-dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background-color: #cbd5e1;
}

.client-type-badge {
  background-color: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
}

/* Workspace Layout */
.workspace-container {
  display: flex;
  flex: 1;
  overflow: hidden;
  height: 100%;
}

.workspace-sidebar {
  width: 220px;
  background-color: #fafbfe;
  border-right: 1px solid #e2e8f0;
  padding: 24px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex-shrink: 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 18px;
  color: #64748b;
  font-size: 14px;
  font-weight: 550;
  border-radius: 10px;
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

.workspace-main-panel {
  flex: 1;
  padding: 32px 40px;
  overflow-y: auto;
  background-color: #ffffff;
  padding-bottom: 100px; /* Space for fixed bottom footer */
}

/* Settings Panels styles */
.settings-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-section-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 20px 0;
  border-left: 4px solid #6366f1;
  padding-left: 12px;
}

.panel-section-subtitle {
  font-size: 13.5px;
  color: #64748b;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

/* Max widths constraints */
.max-w-720 {
  max-width: 720px;
}
.max-w-960 {
  max-width: 960px;
}

/* Forms */
.form-container-styled :deep(.el-form-item) {
  margin-bottom: 24px;
}

.form-container-styled :deep(.el-form-item__label) {
  font-weight: 600;
  color: #334155;
  font-size: 13.5px;
  padding-bottom: 8px;
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
  font-size: 12px;
  color: #94a3b8;
  margin-top: 6px;
  line-height: 1.4;
}

/* Grant types */
.checkbox-cards-grid {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.checkbox-card-item {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 20px;
  transition: all 0.2s;
  background-color: #f8fafc;
}

.checkbox-card-item:hover {
  border-color: #cbd5e1;
  background-color: #ffffff;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.02);
}

.checkbox-card-item :deep(.el-checkbox__label) {
  font-weight: 650;
  color: #1e293b;
  font-size: 14px;
}

.checkbox-card-item p {
  margin: 8px 0 0 24px;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

/* Menu Split Layout */
.settings-panel.split-mode {
  height: calc(100vh - 250px);
}

.tab-pane-split-container {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
  height: 100%;
}

.tree-panel, .editor-panel {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0,0,0,0.01);
}

.panel-header {
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 18px;
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
  font-weight: 650;
  color: #334155;
}

.tree-wrapper-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.custom-el-tree {
  --el-tree-node-hover-bg-color: #f5f3ff;
}

.custom-el-tree :deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ede9fe !important;
  border-left: 3px solid #6366f1;
}

.custom-el-tree :deep(.el-tree-node.is-current > .el-tree-node__content .node-label) {
  font-weight: 600;
  color: #4f46e5;
}

.custom-el-tree :deep(.el-tree-node.is-current > .el-tree-node__content .node-icon) {
  color: #4f46e5;
}

.custom-tree-node {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 8px;
  height: 38px;
  font-size: 13.5px;
  transition: all 0.2s ease;
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

.node-visibility-badge {
  font-size: 11px;
  color: #d97706;
  background-color: #fef3c7;
  padding: 1px 6px;
  border-radius: 4px;
  border: 1px solid #fde68a;
}

.node-actions {
  display: none;
  gap: 6px;
}

.custom-tree-node:hover .node-actions {
  display: flex;
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
  font-size: 44px;
  margin-bottom: 12px;
  color: #cbd5e1;
}

.placeholder-content p {
  font-size: 13px;
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

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

/* Scopes Table */
.permissions-tab-pane {
  display: flex;
  flex-direction: column;
}

.pane-action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.pane-title-left h3 {
  margin: 0 0 4px 0;
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}

.pane-title-left span {
  font-size: 13px;
  color: #64748b;
}

.permission-inner-table {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.code-badge {
  font-family: monospace;
  background-color: #f1f5f9;
  color: #0f172a;
  padding: 3px 8px;
  border-radius: 6px;
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

.method-put {
  background-color: #e0f2fe;
  color: #0369a1;
}

.method-delete {
  background-color: #fee2e2;
  color: #b91c1c;
}

.method-patch {
  background-color: #f3e8ff;
  color: #7e22ce;
}

.api-path-text {
  font-family: monospace;
  font-size: 12.5px;
  color: #334155;
}

/* Tokens and Time Lifetimes */
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

/* External integrations list cards */
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

/* Config row and column structure inside settings panel */
.config-row {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.config-col {
  flex: 1;
}

/* Fixed Bottom Action Bar */
.fixed-footer-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 72px;
  background-color: #ffffff;
  border-top: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 40px;
  box-shadow: 0 -4px 10px rgba(0, 0, 0, 0.03);
  z-index: 100;
}

.footer-actions-wrapper {
  display: flex;
  gap: 16px;
}

/* Animations */
.animate-panel-fade-in {
  animation: panelFadeIn 0.3s ease-out forwards;
}

@keyframes panelFadeIn {
  from {
    opacity: 0;
    transform: translateX(12px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}
.permission-type-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}
.type-button {
  background-color: #e0f2fe;
  color: #0369a1;
}
.type-api {
  background-color: #fef3c7;
  color: #d97706;
}
.type-data {
  background-color: #f3e8ff;
  color: #7e22ce;
}
</style>
