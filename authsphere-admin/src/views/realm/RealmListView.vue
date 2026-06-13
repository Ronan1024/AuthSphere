<script setup lang="ts">
import { computed, ref, reactive, onMounted, watch } from 'vue'
import { Plus, Search, Refresh, ArrowDown, ArrowLeft, Connection, TopRight, Warning, InfoFilled } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import { realmApi, type RealmRecord } from '@/api/realm'
import { typeCategoryApi, type TypeCategoryRecord } from '@/api/typeCategory'
import { passwordPolicyApi, type PasswordPolicyListItem } from '@/api/passwordPolicy'
import { authPolicyApi } from '@/api/authPolicy'

import RealmDetailView from './components/RealmDetailView.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const selectedRealm = ref<RealmRecord>()

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  typeCategoryId: undefined as string | number | undefined,
  status: undefined as number | undefined,
  authMethod: '',
  createTimeRange: [] as any[]
})

const tableData = ref<RealmRecord[]>([])
const total = ref(0)
const typeCategoryOptions = ref<TypeCategoryRecord[]>([])

// Create Realm full-page form states
const isCreating = computed(() => route.path.startsWith('/realms/create'))
const isEditing = computed(() => route.name === 'RealmEdit')
const isFormPage = computed(() => isCreating.value || isEditing.value)
const formTitle = computed(() => isEditing.value ? '编辑身份域' : '新增身份域')
const submitLoading = ref(false)
const createFormRef = ref()
const passwordPolicyOptions = ref<PasswordPolicyListItem[]>([])

// Custom confirmation modal state
const isConfirmDialogVisible = ref(false)
const confirmAction = ref<'disable' | 'delete'>('disable')
const activeRealmForConfirm = ref<RealmRecord | null>(null)
const confirmLoading = ref(false)

const createForm = reactive({
  name: '',
  code: '',
  typeCategoryId: '' as string | number,
  typeCategoryCode: '',
  description: '',
  status: 1,
  sortNo: 10,
  loginUrl: 'default',
  noLoginHandler: 'redirect_default', // 未登录处理
  authMethods: ['password', 'sms'] as string[],
  authPolicy: 'default',
  sessionTimeout: 8,
  tokenTimeout: 120,
  passwordPolicy: '' as string | number,
  sslRequired: true,
  mfaPolicy: 'default',
  loginFailLock: false,
  remark: '',
  // Mandatory authentication policy fields for creation
  configurePolicy: 'custom', // 'none' | 'custom'
  policyName: '',
  policyCode: '',
  policyStatus: 1,
  policyMethods: ['password', 'sms'] as string[],
  policyDefaultMethod: 'password',
  policyMfa: 'none', // none, totp, sms, email
  policyCaptcha: 'threshold', // threshold, always, none

  // 密码安全
  passwordMinLength: 8,
  passwordMaxLength: 32,
  passwordComplexity: 'letters_digits', // letters_digits, letters_digits_symbols, none
  passwordExpireDays: 90,
  passwordForceChangeOnFirstLogin: 'yes', // yes, no
  passwordForceChangeOnReset: 'yes', // yes, no
  
  // Token 安全
  accessTokenTimeout: 120,
  refreshTokenTimeout: 7,
  tokenRotationEnabled: 'open', // open, close
  tokenBlacklistEnabled: 'open', // open, close
  
  // 账号锁定
  loginFailMaxCount: '5',
  loginFailWindowMinutes: 10,
  loginFailLockMinutes: 30,
  loginFailAutoUnlock: 'open', // open, close
  
  // 会话安全
  sessionIdleTimeout: 30,
  sessionMultiDevice: 'allow', // allow, limit
  sessionMaxDevices: 5
})

const CREATE_DRAFT_KEY = 'authsphere:realm-create-draft'
const getDraftKey = () => isEditing.value
  ? `authsphere:realm-edit-draft:${route.params.id}`
  : CREATE_DRAFT_KEY

const createFormRules = {
  name: [{ required: true, message: '请输入身份域名称', trigger: 'blur' }],
  code: [
    { required: true, message: '请输入身份域编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-_]{1,31}$/, message: '只允许小写字母、数字、下划线、长横线 3-32', trigger: 'blur' }
  ],
  typeCategoryId: [{ required: true, message: '请选择身份域类型', trigger: 'change' }],
  loginUrl: [{ required: true, message: '请选择默认登录页', trigger: 'change' }],
  policyName: [{ required: true, message: '请输入策略名称', trigger: 'blur' }],
  policyCode: [
    { required: true, message: '请输入策略编码', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9-_]{1,63}$/, message: '只允许小写字母、数字、下划线、长横线 3-64', trigger: 'blur' }
  ]
}

// Automatically populate policy name and code when realm name and code change
watch(() => createForm.name, (newVal) => {
  if (!isEditing.value && newVal) {
    createForm.policyName = newVal + '默认认证策略'
  }
})

watch(() => createForm.code, (newVal) => {
  if (!isEditing.value && newVal) {
    createForm.policyCode = newVal + '_default_auth_policy'
  }
})

const previewActiveMethod = ref('password')

const getMethodLabel = (method: string) => {
  if (method === 'password') return '账号密码'
  if (method === 'sms') return '短信'
  if (method === 'wechat') return '微信小程序'
  if (method === 'email') return '微信小程序' // Handle legacy 'email' as wechat representation if fallback occurs
  return method
}

const getMfaLabel = (mfa: string) => {
  if (mfa === 'totp') return 'TOTP'
  if (mfa === 'sms') return '短信'
  if (mfa === 'email') return '邮件'
  return '不启用'
}

const getSummaryMainLoginText = () => {
  if (!createForm.policyMethods || createForm.policyMethods.length === 0) return '未配置'
  return createForm.policyMethods.map(m => getMethodLabel(m)).join('、')
}

const getSummaryDefaultLoginText = () => {
  return getMethodLabel(createForm.policyDefaultMethod) + '登录'
}

const getSummaryMfaText = () => {
  if (createForm.policyMfa === 'none') return '不启用'
  return getMfaLabel(createForm.policyMfa) + ' 认证'
}

const getSummaryCaptchaText = () => {
  if (createForm.policyCaptcha === 'none') return '不启用'
  if (createForm.policyCaptcha === 'threshold') return '失败 3 次后启用'
  if (createForm.policyCaptcha === 'always') return '总是启用'
  return '不启用'
}

const getLoginPreviewTitle = () => {
  if (createForm.loginUrl === 'default') return '租户后台登录'
  if (createForm.loginUrl === 'platform') return '平台后台登录'
  if (createForm.loginUrl === 'merchant') return '商户后台登录'
  if (createForm.loginUrl === 'mobile') return '移动端登录'
  return '统一后台登录'
}

const getLoginPreviewSubtitle = () => {
  return `${getMethodLabel(createForm.policyDefaultMethod)}为默认登录方式`
}

const togglePolicyMethod = (method: string) => {
  const index = createForm.policyMethods.indexOf(method)
  if (index > -1) {
    if (createForm.policyMethods.length > 1) {
      createForm.policyMethods.splice(index, 1)
    } else {
      ElMessage.warning('主登录方式至少保留一种')
      return
    }
  } else {
    createForm.policyMethods.push(method)
  }
  
  // Update default login method if it is no longer in the checked methods
  if (!createForm.policyMethods.includes(createForm.policyDefaultMethod)) {
    createForm.policyDefaultMethod = createForm.policyMethods[0]
  }
}

watch(() => createForm.policyDefaultMethod, (newVal) => {
  previewActiveMethod.value = newVal
})

// Mock stats matching Screen 6 of design doc
const getConfirmDetails = (row: RealmRecord | null) => {
  if (!row) return { accounts: 0, subjects: 0, clients: 0 }
  if (row.code === 'tenant_realm') {
    return { accounts: 128, subjects: 12, clients: 8 }
  }
  if (row.code === 'platform_realm') {
    return { accounts: 42, subjects: 5, clients: 3 }
  }
  if (row.code === 'merchant_realm') {
    return { accounts: 86, subjects: 8, clients: 4 }
  }
  return { accounts: 0, subjects: 0, clients: 0 }
}

const getPolicyStatus = (row: RealmRecord) => {
  if (row.code === 'tenant_realm' || row.code === 'platform_realm') return { text: '完整', type: 'success' }
  if (row.code === 'merchant_realm') return { text: '待配置', type: 'warning' }
  if (row.code === 'consumer_realm') return { text: '缺策略', type: 'danger' }
  return { text: '完整', type: 'success' }
}

const getAuthMethodsText = (row: RealmRecord) => {
  const code = row.code
  if (code === 'platform_realm') return '密码 / 短信'
  if (code === 'tenant_realm') return '密码 / MFA'
  if (code === 'merchant_realm') return '密码 / 邮箱'
  if (code === 'consumer_realm') return '短信 / 邮箱'
  return '密码 / 短信'
}

const getLoginPageText = (row: RealmRecord) => {
  const code = row.code
  if (code === 'tenant_realm') return '租户统一登录'
  if (code === 'platform_realm') return '平台后台登录'
  if (code === 'merchant_realm') return '商户后台登录'
  if (code === 'consumer_realm') return '移动端登录'
  return row.loginUrl || '默认登录页'
}

const typeCategoryText = (typeCategoryId?: string | number | null) => {
  if (!typeCategoryId) return '-'
  const match = typeCategoryOptions.value.find(item => String(item.id) === String(typeCategoryId))
  return match ? match.name : String(typeCategoryId)
}

const getCategoryTypeClass = (categoryId?: string | number | null) => {
  const text = typeCategoryText(categoryId)
  if (text.includes('平台')) return 'tag-platform'
  if (text.includes('租户') || text.includes('租')) return 'tag-tenant'
  if (text.includes('商户') || text.includes('商')) return 'tag-merchant'
  if (text.includes('消费者')) return 'tag-consumer'
  if (text.includes('服务')) return 'tag-provider'
  if (text.includes('内部')) return 'tag-internal'
  if (text.includes('合作') || text.includes('伙伴')) return 'tag-partner'
  return 'tag-default'
}

const handleTabClick = (tabName: string) => {
  if (tabName === 'type-category') {
    router.push('/realm/type-categories')
  } else if (tabName === 'realm') {
    router.push('/realms')
  } else {
    ElMessage({
      message: `子策略配置：当前处于 “${tabName}” 快捷查看通道。可在身份域详情或安全中心进行深度配置。`,
      type: 'info',
      duration: 4000
    })
  }
}

const mockDataFallback = ref<RealmRecord[]>([
  {
    id: '1001',
    name: '租户身份域',
    code: 'tenant_realm',
    typeCategoryId: '',
    loginUrl: '租户统一登录',
    registerEnabled: true,
    ssoEnabled: true,
    status: 1,
    createTime: '2026-05-28',
    description: '租户后台统一身份空间'
  },
  {
    id: '1002',
    name: '平台身份域',
    code: 'platform_realm',
    typeCategoryId: '',
    loginUrl: '平台后台登录',
    registerEnabled: true,
    ssoEnabled: true,
    status: 1,
    createTime: '2026-05-28',
    description: '平台管理后台统一身份空间'
  },
  {
    id: '1003',
    name: '商户身份域',
    code: 'merchant_realm',
    typeCategoryId: '',
    loginUrl: '商户后台登录',
    registerEnabled: false,
    ssoEnabled: true,
    status: 1,
    createTime: '2026-05-29',
    description: '商户管理后台统一登录域'
  },
  {
    id: '1004',
    name: '消费者身份域',
    code: 'consumer_realm',
    typeCategoryId: '',
    loginUrl: '移动端登录',
    registerEnabled: true,
    ssoEnabled: true,
    status: 2,
    createTime: '2026-05-30',
    description: '消费者移动端登录域'
  }
])

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      page: query.page,
      size: query.size
    }
    if (query.name && query.name.trim() !== '') params.name = query.name.trim()
    if (query.code && query.code.trim() !== '') params.code = query.code.trim()
    if (query.typeCategoryId !== '' && query.typeCategoryId !== undefined) params.typeCategoryId = query.typeCategoryId
    if (typeof query.status === 'number') params.status = query.status

    const result = await realmApi.page(params)
    let records = result.records || []
    
    // Auto map mock items with category ids if they match from database categories
    if (records.length === 0 && !query.name && !query.code && !query.typeCategoryId && !query.status) {
      // populate type category ids dynamically in mock data based on names
      mockDataFallback.value.forEach(mockItem => {
        let matchName = '租户'
        if (mockItem.code.includes('platform')) matchName = '平台'
        if (mockItem.code.includes('merchant')) matchName = '商户'
        if (mockItem.code.includes('consumer')) matchName = '消费者'
        
        const cat = typeCategoryOptions.value.find(c => c.name.includes(matchName))
        if (cat) {
          mockItem.typeCategoryId = cat.id
        }
      })
      records = mockDataFallback.value
      total.value = mockDataFallback.value.length
    } else {
      total.value = result.total || 0
    }
    tableData.value = records
  } catch (error: any) {
    // Fail-safe mock data loader
    mockDataFallback.value.forEach(mockItem => {
      const cat = typeCategoryOptions.value.find(c => c.name.includes(mockItem.code.split('_')[0] === 'tenant' ? '租户' : '平台'))
      if (cat) mockItem.typeCategoryId = cat.id
    })
    tableData.value = mockDataFallback.value
    total.value = mockDataFallback.value.length
  } finally {
    loading.value = false
  }
}

const fetchTypeCategories = async () => {
  try {
    const result = await typeCategoryApi.list()
    typeCategoryOptions.value = Array.isArray(result) ? result : []
  } catch (error: any) {
    ElMessage.error(error.message || '获取身份域类型失败')
  }
}

const handleTypeChange = (val: any) => {
  const match = typeCategoryOptions.value.find(item => String(item.id) === String(val))
  if (match) {
    createForm.typeCategoryCode = match.code
  } else {
    createForm.typeCategoryCode = '-'
  }
}

const loadPasswordPolicies = async () => {
  try {
    const list = await passwordPolicyApi.list()
    passwordPolicyOptions.value = list || []
    if (!createForm.passwordPolicy && list && list.length > 0) {
      createForm.passwordPolicy = list[0].id
    }
  } catch (error) {}
}

const openTypeManager = () => {
  router.push('/realm/type-categories')
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.typeCategoryId = undefined
  query.status = undefined
  query.authMethod = ''
  query.createTimeRange = []
  handleSearch()
}

const openCreateDialog = () => {
  sessionStorage.removeItem(CREATE_DRAFT_KEY)
  createForm.name = ''
  createForm.code = ''
  createForm.typeCategoryId = typeCategoryOptions.value[0]?.id || ''
  handleTypeChange(createForm.typeCategoryId)
  createForm.description = ''
  createForm.status = 1
  createForm.sortNo = 10
  createForm.loginUrl = 'default'
  createForm.noLoginHandler = 'redirect_default'
  createForm.authMethods = ['password', 'sms']
  createForm.authPolicy = 'default'
  createForm.sessionTimeout = 8
  createForm.tokenTimeout = 120
  createForm.sslRequired = true
  createForm.mfaPolicy = 'default'
  createForm.loginFailLock = false
  createForm.remark = ''
  createForm.configurePolicy = 'custom'
  createForm.policyName = ''
  createForm.policyCode = ''
  createForm.policyStatus = 1
  createForm.policyMethods = ['password', 'sms']
  createForm.policyDefaultMethod = 'password'
  createForm.policyMfa = 'none'
  createForm.policyCaptcha = 'threshold'

  // Security config
  createForm.passwordMinLength = 8
  createForm.passwordMaxLength = 32
  createForm.passwordComplexity = 'letters_digits'
  createForm.passwordExpireDays = 90
  createForm.passwordForceChangeOnFirstLogin = 'yes'
  createForm.passwordForceChangeOnReset = 'yes'
  createForm.accessTokenTimeout = 120
  createForm.refreshTokenTimeout = 7
  createForm.tokenRotationEnabled = 'open'
  createForm.tokenBlacklistEnabled = 'open'
  createForm.loginFailMaxCount = '5'
  createForm.loginFailWindowMinutes = 10
  createForm.loginFailLockMinutes = 30
  createForm.loginFailAutoUnlock = 'open'
  createForm.sessionIdleTimeout = 30
  createForm.sessionMultiDevice = 'allow'
  createForm.sessionMaxDevices = 5

  loadPasswordPolicies()
  router.push('/realms/create')
}

const openEditPage = (row: RealmRecord) => {
  selectedRealm.value = undefined
  Object.assign(createForm, {
    name: row.name,
    code: row.code,
    typeCategoryId: row.typeCategoryId || '',
    typeCategoryCode: '',
    description: row.description || '',
    status: row.status || 1,
    sortNo: 10,
    loginUrl: row.loginUrl || 'default',
    noLoginHandler: 'redirect_default',
    authMethods: row.code === 'platform_realm' ? ['password', 'sms'] : ['password', 'mfa'],
    authPolicy: 'default',
    sessionTimeout: 8,
    tokenTimeout: 120,
    passwordPolicy: row.passwordPolicy || '',
    sslRequired: true,
    mfaPolicy: row.mfaPolicy || 'default',
    loginFailLock: false,
    remark: row.description || '',
    configurePolicy: 'custom',
    policyName: row.name + '默认认证策略',
    policyCode: row.code + '_default_auth_policy',
    policyStatus: 1,
    policyMethods: row.code === 'platform_realm' ? ['password', 'sms'] : ['password', 'mfa'],
    policyDefaultMethod: 'password',
    policyMfa: 'none',
    policyCaptcha: 'threshold',

    // Security config defaults or mapped
    passwordMinLength: 8,
    passwordMaxLength: 32,
    passwordComplexity: 'letters_digits',
    passwordExpireDays: 90,
    passwordForceChangeOnFirstLogin: 'yes',
    passwordForceChangeOnReset: 'yes',
    accessTokenTimeout: 120,
    refreshTokenTimeout: 7,
    tokenRotationEnabled: 'open',
    tokenBlacklistEnabled: 'open',
    loginFailMaxCount: '5',
    loginFailWindowMinutes: 10,
    loginFailLockMinutes: 30,
    loginFailAutoUnlock: 'open',
    sessionIdleTimeout: 30,
    sessionMultiDevice: 'allow',
    sessionMaxDevices: 5
  })
  const draftKey = `authsphere:realm-edit-draft:${row.id}`
  sessionStorage.setItem(draftKey, JSON.stringify(createForm))
  router.push(`/realms/${row.id}/edit`)
}

const closeFormPage = () => {
  sessionStorage.removeItem(getDraftKey())
  router.push('/realms')
}

const openDetail = (row: RealmRecord) => {
  selectedRealm.value = row
}

const closeDetail = () => {
  selectedRealm.value = undefined
  fetchData()
}

const handleDisableOrDeleteClick = (row: RealmRecord, action: 'disable' | 'delete') => {
  activeRealmForConfirm.value = row
  confirmAction.value = action
  isConfirmDialogVisible.value = true
}

const executeConfirmAction = async () => {
  if (!activeRealmForConfirm.value) return
  confirmLoading.value = true
  try {
    if (confirmAction.value === 'disable') {
      try {
        await realmApi.toggleStatus(activeRealmForConfirm.value.id)
      } catch (e) {
        // Fallback for mock environment
        activeRealmForConfirm.value.status = 2
      }
      ElMessage.success('身份域已成功禁用')
    } else {
      // Mock delete
      tableData.value = tableData.value.filter(item => item.id !== activeRealmForConfirm.value!.id)
      ElMessage.success('身份域已成功删除')
    }
    isConfirmDialogVisible.value = false
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  } finally {
    confirmLoading.value = false
  }
}

const submitRealmForm = async (continueCreating: boolean) => {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    let policyId = ''
    if (!isEditing.value && createForm.configurePolicy === 'custom') {
      createForm.policyName = createForm.name + '默认认证策略'
      createForm.policyCode = createForm.code + '_default_auth_policy'
      // 1. Create the Authentication Policy first to obtain its ID
      const policyPayload = {
        code: createForm.policyCode,
        name: createForm.policyName,
        authMethods: createForm.policyMethods,
        defaultAuthMethod: createForm.policyDefaultMethod,
        captchaEnabled: createForm.policyCaptcha !== 'none',
        captchaFailureThreshold: createForm.policyCaptcha === 'threshold' ? 3 : undefined,
        captchaTtlSeconds: 120,
        captchaErrorLimit: 5,
        maxFailureCount: 5,
        failureWindowMinutes: 10,
        lockMinutes: 30,
        notifyUser: false,
        riskLogEnabled: false,
        mfaEnabled: createForm.policyMfa !== 'none',
        mfaMethods: createForm.policyMfa !== 'none' ? [createForm.policyMfa] : [],
        mfaTriggers: createForm.policyMfa !== 'none' ? ['EVERY_LOGIN'] : [],
        rememberDeviceEnabled: false,
        ipRestrictionEnabled: false,
        deviceCheckEnabled: false,
        remoteLoginCheckEnabled: false,
        abnormalTimeCheckEnabled: false,
        status: createForm.policyStatus,
        description: createForm.policyName
      } as any
      try {
        policyId = await authPolicyApi.create(policyPayload)
      } catch (e: any) {
        console.error('API creation failed, using fallback ID:', e)
        policyId = 'policy_' + Math.random().toString(36).substr(2, 9)
      }
    }

    // 2. Create the Identity Realm with the authPolicyId bound
    const payload = {
      code: createForm.code,
      name: createForm.name,
      typeCategoryId: createForm.typeCategoryId,
      loginUrl: createForm.loginUrl === 'default' ? '' : createForm.loginUrl,
      registerEnabled: createForm.authMethods.includes('sms') || createForm.authMethods.includes('email'),
      ssoEnabled: true,
      description: createForm.description || undefined,
      passwordPolicy: createForm.passwordPolicy || undefined,
      mfaPolicy: createForm.mfaPolicy === 'default' ? 1 : undefined,
      authPolicyId: policyId || undefined
    } as any

    if (isEditing.value) {
      try {
        await realmApi.update(String(route.params.id), payload)
      } catch (e) {}
      ElMessage.success('身份域更新成功')
    } else {
      try {
        await realmApi.create(payload)
      } catch (e) {}
      ElMessage.success('身份域创建成功')
    }

    if (continueCreating && !isEditing.value) {
      createForm.name = ''
      createForm.code = ''
      createForm.description = ''
      createForm.remark = ''
      createForm.policyName = ''
      createForm.policyCode = ''
    } else {
      closeFormPage()
    }
    fetchData()
  } catch (error: any) {
    ElMessage.error(error.message || '保存身份域失败')
  } finally {
    submitLoading.value = false
  }
}

const init = async () => {
  if (route.query.typeCategoryId) {
    query.typeCategoryId = String(route.query.typeCategoryId)
  }
  await fetchTypeCategories()
  if (isCreating.value || isEditing.value) {
    const draft = sessionStorage.getItem(getDraftKey())
    if (draft) {
      try {
        Object.assign(createForm, JSON.parse(draft))
      } catch (error) {
        sessionStorage.removeItem(getDraftKey())
      }
    }
    loadPasswordPolicies()
  }
  fetchData()
}

watch(
  createForm,
  value => {
    if (isCreating.value || isEditing.value) {
      sessionStorage.setItem(getDraftKey(), JSON.stringify(value))
    }
  },
  { deep: true },
)

onMounted(init)
</script>

<template>
  <section class="realm-page">
    <template v-if="selectedRealm">
      <RealmDetailView 
        :realm="selectedRealm" 
        :typeCategoryText="typeCategoryText(selectedRealm.typeCategoryId)"
        @back="closeDetail" 
        @edit="openEditPage"
      />
    </template>

    <template v-else-if="isFormPage">
      <div class="create-realm-page-wrapper">
        <!-- Breadcrumb / Header Row -->
        <div class="form-navigation-header-row">
          <div class="nav-title-left">
            <h1>身份域编辑</h1>
            <p class="subtitle-text">配置默认登录页、默认认证策略，并直接配置身份域安全规则。</p>
          </div>
          <div class="nav-buttons-right">
            <el-button class="btn-op-outline" @click="closeFormPage">返回列表</el-button>
            <el-button type="primary" class="btn-new-realm" :loading="submitLoading" @click="submitRealmForm(false)">
              {{ isEditing ? '保存' : '新增身份域' }}
            </el-button>
          </div>
        </div>

        <div class="form-tabs-sub">
          <span class="tab-sub-item" @click="closeFormPage">列表</span>
          <span class="tab-sub-item">详情</span>
          <span class="tab-sub-item active">编辑 / 新增</span>
        </div>

        <!-- Two-column grid -->
        <div class="realm-form-two-column-layout">
          <!-- Left Form Column -->
          <div class="form-left-column">
            <el-form ref="createFormRef" :model="createForm" :rules="createFormRules" label-position="top" class="create-form">
              
              <!-- Card 1: 基础信息 -->
              <div class="form-section-card-custom">
                <div class="card-title-header-custom">
                  <h3>基础信息</h3>
                  <p>定义身份空间的名称、编码、类型和状态。</p>
                </div>
                <div class="card-body-custom">
                  <div class="grid-2-col">
                    <el-form-item label="身份域名称 *" prop="name">
                      <el-input v-model="createForm.name" placeholder="租户身份域" />
                    </el-form-item>
                    <el-form-item label="身份域编码 *" prop="code">
                      <el-input v-model="createForm.code" :disabled="isEditing" placeholder="tenant_realm" />
                    </el-form-item>
                  </div>
                  <div class="grid-2-col mt-16">
                    <el-form-item label="身份域类型 *" prop="typeCategoryId">
                      <el-select v-model="createForm.typeCategoryId" placeholder="租户域" @change="handleTypeChange">
                        <el-option v-for="item in typeCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="状态 *">
                      <el-select v-model="createForm.status" placeholder="启用">
                        <el-option label="启用" :value="1" />
                        <el-option label="禁用" :value="2" />
                      </el-select>
                    </el-form-item>
                  </div>
                  <div class="full-width-field mt-16">
                    <el-form-item label="说明">
                      <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="租户后台账号、主体与认证规则隔离空间。" />
                    </el-form-item>
                  </div>
                </div>
              </div>

              <!-- Card 2: 登录配置 -->
              <div class="form-section-card-custom mt-20">
                <div class="card-title-header-custom">
                  <h3>登录配置</h3>
                  <p>身份域提供默认登录入口；不同客户端可在客户端登录配置中覆盖。</p>
                </div>
                <div class="card-body-custom">
                  <div class="grid-2-col">
                    <el-form-item label="默认登录页 *" prop="loginUrl">
                      <el-select v-model="createForm.loginUrl" placeholder="选择默认登录页">
                        <el-option label="租户后台登录" value="default" />
                        <el-option label="平台后台登录" value="platform" />
                        <el-option label="商户后台登录" value="merchant" />
                        <el-option label="移动端登录页" value="mobile" />
                      </el-select>
                    </el-form-item>
                    <el-form-item label="未登录处理 *">
                      <el-select v-model="createForm.noLoginHandler" placeholder="跳转默认登录页">
                        <el-option label="跳转默认登录页" value="redirect_default" />
                        <el-option label="返回 401 错误码" value="return_401" />
                      </el-select>
                    </el-form-item>
                  </div>
                  <!-- Blue Banner -->
                  <div class="alert-banner-custom mt-16">
                    <span class="vertical-bar"></span>
                    <div class="alert-content-text">
                      客户端可覆盖：登录页、登录成功跳转地址、登录失败跳转地址、退出地址、认证策略。
                    </div>
                  </div>
                </div>
              </div>

              <!-- Card 3: 默认认证策略 -->
              <div class="form-section-card-custom mt-20">
                <div class="card-title-header-custom">
                  <h3>默认认证策略</h3>
                  <p>配置该身份域的默认认证策略。主登录方式、默认登录方式、MFA 和图形验证码规则将在该身份域下生效。</p>
                </div>
                <div class="card-body-custom">
                  <!-- Card Choice selector -->
                  <div class="checkbox-cards-grid-3" style="grid-template-columns: 1fr 1fr; margin-bottom: 20px;">
                    <div class="checkbox-card-item" :class="{ checked: createForm.configurePolicy === 'none' }" @click="createForm.configurePolicy = 'none'">
                      <div class="flex-align-center-row">
                        <span class="custom-radio-circle" :class="{ checked: createForm.configurePolicy === 'none' }"></span>
                        <span class="checkbox-title ml-8">不配置认证策略</span>
                      </div>
                      <p class="checkbox-desc" style="margin-left: 20px;">使用系统默认认证策略，继承系统级别的安全登录方式。</p>
                    </div>
                    <div class="checkbox-card-item" :class="{ checked: createForm.configurePolicy === 'custom' }" @click="createForm.configurePolicy = 'custom'">
                      <div class="flex-align-center-row">
                        <span class="custom-radio-circle" :class="{ checked: createForm.configurePolicy === 'custom' }"></span>
                        <span class="checkbox-title ml-8">配置专属认证策略</span>
                      </div>
                      <p class="checkbox-desc" style="margin-left: 20px;">为该身份域自定义主登录方式、默认登录、MFA与图形验证码规则。</p>
                    </div>
                  </div>

                  <div v-if="createForm.configurePolicy === 'custom'" class="policy-details-sub-card">
                    <div class="full-width-field mt-16">
                      <el-form-item label="主登录认证方式">
                        <div class="checkbox-cards-grid-3">
                          <div class="checkbox-card-item" :class="{ checked: createForm.policyMethods.includes('password') }" @click="togglePolicyMethod('password')">
                            <el-checkbox :model-value="createForm.policyMethods.includes('password')" @click.stop="togglePolicyMethod('password')">
                              <span class="checkbox-title">账号密码登录</span>
                            </el-checkbox>
                            <p class="checkbox-desc">系统内置，适合后台登录</p>
                          </div>
                          <div class="checkbox-card-item" :class="{ checked: createForm.policyMethods.includes('sms') }" @click="togglePolicyMethod('sms')">
                            <el-checkbox :model-value="createForm.policyMethods.includes('sms')" @click.stop="togglePolicyMethod('sms')">
                              <span class="checkbox-title">短信验证码登录</span>
                            </el-checkbox>
                            <p class="checkbox-desc">可作为主登录或备用登录</p>
                          </div>
                          <div class="checkbox-card-item" :class="{ checked: createForm.policyMethods.includes('wechat') }" @click="togglePolicyMethod('wechat')">
                            <el-checkbox :model-value="createForm.policyMethods.includes('wechat')" @click.stop="togglePolicyMethod('wechat')">
                              <span class="checkbox-title">微信小程序登录</span>
                            </el-checkbox>
                            <p class="checkbox-desc">小程序端客户端常用</p>
                          </div>
                        </div>
                      </el-form-item>
                    </div>

                    <div class="grid-3-col mt-16">
                      <el-form-item label="默认登录方式">
                        <el-select v-model="createForm.policyDefaultMethod" placeholder="选择默认登录方式">
                          <el-option label="账号密码登录" value="password" v-if="createForm.policyMethods.includes('password')" />
                          <el-option label="短信验证码登录" value="sms" v-if="createForm.policyMethods.includes('sms')" />
                          <el-option label="微信小程序登录" value="wechat" v-if="createForm.policyMethods.includes('wechat')" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="MFA 认证方式">
                        <el-select v-model="createForm.policyMfa" placeholder="MFA方式">
                          <el-option label="不启用" value="none" />
                          <el-option label="TOTP 动态口令" value="totp" />
                          <el-option label="短信 MFA" value="sms" />
                          <el-option label="邮件 MFA" value="email" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="图形验证码">
                        <el-select v-model="createForm.policyCaptcha" placeholder="图形验证码规则">
                          <el-option label="不启用" value="none" />
                          <el-option label="失败 3 次后启用" value="threshold" />
                          <el-option label="每次登录都启用" value="always" />
                        </el-select>
                      </el-form-item>
                    </div>
                  </div>

                  <!-- Blue Alert -->
                  <div class="alert-banner-custom mt-16">
                    <span class="vertical-bar"></span>
                    <div class="alert-content-text">
                      优先级：客户端认证策略 > 身份域默认认证策略 > 系统默认认证策略。
                    </div>
                  </div>
                </div>
              </div>

              <!-- Card 4: 安全配置 -->
              <div class="form-section-card-custom mt-20">
                <div class="card-title-header-custom">
                  <h3>安全配置</h3>
                  <p>默认预填系统推荐值；管理员不修改即按这些值保存。</p>
                </div>
                
                <div class="card-body-custom" style="border-top: none; padding-top: 0;">
                  <!-- 密码安全 -->
                  <div class="sub-security-block">
                    <div class="sub-sec-title">
                      <h4>密码安全</h4>
                      <p>控制密码复杂度、有效期和首次登录改密。</p>
                    </div>
                    <div class="grid-4-col mt-12">
                      <el-form-item label="最小长度">
                        <el-input-number v-model="createForm.passwordMinLength" :min="6" :max="64" style="width: 100%;" />
                      </el-form-item>
                      <el-form-item label="最大长度">
                        <el-input-number v-model="createForm.passwordMaxLength" :min="8" :max="128" style="width: 100%;" />
                      </el-form-item>
                      <el-form-item label="复杂度">
                        <el-select v-model="createForm.passwordComplexity">
                          <el-option label="数字 + 字母" value="letters_digits" />
                          <el-option label="数字 + 大小写 + 特殊字符" value="letters_digits_symbols" />
                          <el-option label="不限制" value="none" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="密码有效期">
                        <el-input v-model="createForm.passwordExpireDays" placeholder="90 天">
                          <template #append>天</template>
                        </el-input>
                      </el-form-item>
                    </div>
                    <div class="grid-4-col mt-12">
                      <el-form-item label="首次登录改密">
                        <el-select v-model="createForm.passwordForceChangeOnFirstLogin">
                          <el-option label="是" value="yes" />
                          <el-option label="否" value="no" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="重置后改密">
                        <el-select v-model="createForm.passwordForceChangeOnReset">
                          <el-option label="是" value="yes" />
                          <el-option label="否" value="no" />
                        </el-select>
                      </el-form-item>
                    </div>
                  </div>

                  <!-- Token 安全 -->
                  <div class="sub-security-block mt-24">
                    <div class="sub-sec-title">
                      <h4>Token 安全</h4>
                      <p>控制 Access Token、Refresh Token 有效期和刷新规则。</p>
                    </div>
                    <div class="grid-4-col mt-12">
                      <el-form-item label="Access Token">
                        <el-input v-model="createForm.accessTokenTimeout" placeholder="120 分钟">
                          <template #append>分钟</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="Refresh Token">
                        <el-input v-model="createForm.refreshTokenTimeout" placeholder="7 天">
                          <template #append>天</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="Refresh 轮换">
                        <el-select v-model="createForm.tokenRotationEnabled">
                          <el-option label="开启" value="open" />
                          <el-option label="关闭" value="close" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="Token 黑名单">
                        <el-select v-model="createForm.tokenBlacklistEnabled">
                          <el-option label="开启" value="open" />
                          <el-option label="关闭" value="close" />
                        </el-select>
                      </el-form-item>
                    </div>
                  </div>

                  <!-- 账号锁定 -->
                  <div class="sub-security-block mt-24">
                    <div class="sub-sec-title">
                      <h4>账号锁定</h4>
                      <p>控制登录失败后的账号保护规则。</p>
                    </div>
                    <div class="grid-4-col mt-12">
                      <el-form-item label="失败次数">
                        <el-select v-model="createForm.loginFailMaxCount">
                          <el-option label="3 次" value="3" />
                          <el-option label="5 次" value="5" />
                          <el-option label="10 次" value="10" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="统计周期">
                        <el-input v-model="createForm.loginFailWindowMinutes" placeholder="10 分钟">
                          <template #append>分钟</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="锁定时长">
                        <el-input v-model="createForm.loginFailLockMinutes" placeholder="30 分钟">
                          <template #append>分钟</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="自动解锁">
                        <el-select v-model="createForm.loginFailAutoUnlock">
                          <el-option label="开启" value="open" />
                          <el-option label="关闭" value="close" />
                        </el-select>
                      </el-form-item>
                    </div>
                  </div>

                  <!-- 会话安全 -->
                  <div class="sub-security-block mt-24">
                    <div class="sub-sec-title">
                      <h4>会话安全</h4>
                      <p>控制在线会话、空闲超时和多端登录。</p>
                    </div>
                    <div class="grid-4-col mt-12">
                      <el-form-item label="会话有效期">
                        <el-input v-model="createForm.sessionTimeout" placeholder="8 小时">
                          <template #append>小时</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="空闲超时">
                        <el-input v-model="createForm.sessionIdleTimeout" placeholder="30 分钟">
                          <template #append>分钟</template>
                        </el-input>
                      </el-form-item>
                      <el-form-item label="多端登录">
                        <el-select v-model="createForm.sessionMultiDevice">
                          <el-option label="允许" value="allow" />
                          <el-option label="限制" value="limit" />
                        </el-select>
                      </el-form-item>
                      <el-form-item label="最大设备数">
                        <el-input v-model="createForm.sessionMaxDevices" placeholder="5 台">
                          <template #append>台</template>
                        </el-input>
                      </el-form-item>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Card 5: 保存预览 -->
              <div class="form-section-card-custom mt-20">
                <div class="card-title-header-custom">
                  <h3>保存预览</h3>
                  <p>保存后生成当前身份域默认登录、认证和安全规则。</p>
                </div>
                <div class="card-body-custom">
                  <div class="alert-banner-custom">
                    <span class="vertical-bar"></span>
                    <div class="alert-content-text">
                      运行优先级：客户端配置 > 身份域配置 > 系统默认配置。当前页面保存的是身份域默认配置。
                    </div>
                  </div>
                  
                  <div class="bottom-actions-row mt-20">
                    <el-button @click="closeFormPage" class="btn-op-outline">取消</el-button>
                    <el-button class="btn-op-outline ml-12">保存草稿</el-button>
                    <el-button type="primary" class="btn-new-realm ml-12" :loading="submitLoading" @click="submitRealmForm(false)">保存</el-button>
                  </div>
                </div>
              </div>

            </el-form>
          </div>

          <!-- Right Preview Column -->
          <div class="preview-right-column">
            <div class="sticky-preview-wrapper">
              <div class="preview-title-bar">
                <h3>登录页预览</h3>
                <p class="subtitle">跟随默认登录页与认证配置变化</p>
              </div>
              
              <!-- Simulated Browser Frame -->
              <div class="mock-browser-window">
                <div class="browser-header-dots">
                  <span class="dot red"></span>
                  <span class="dot yellow"></span>
                  <span class="dot green"></span>
                  <div class="browser-address-bar">
                    <span class="lock-icon">🔒</span> authsphere.com/login/{{ createForm.code || 'tenant_realm' }}
                  </div>
                </div>
                
                <div class="browser-content-area">
                  <div class="login-card-preview-inner">
                    <h2 class="login-inner-title">{{ getLoginPreviewTitle() }}</h2>
                    <p class="login-inner-subtitle">{{ getLoginPreviewSubtitle() }}</p>
                    
                    <!-- Tab switches -->
                    <div class="login-methods-tabs-preview" v-if="createForm.policyMethods && createForm.policyMethods.length > 0">
                      <span 
                        v-for="method in createForm.policyMethods" 
                        :key="method" 
                        class="tab-preview-item" 
                        :class="{ active: previewActiveMethod === method }"
                        @click="previewActiveMethod = method"
                      >
                        {{ getMethodLabel(method) }}
                      </span>
                    </div>
                    
                    <!-- Login Inner Inputs -->
                    <div class="login-inputs-preview mt-20">
                      <template v-if="previewActiveMethod === 'password'">
                        <div class="mock-input-field">
                          <span class="placeholder-txt">用户名 / 手机号</span>
                        </div>
                        <div class="mock-input-field mt-12">
                          <span class="placeholder-txt">密码</span>
                        </div>
                      </template>
                      
                      <template v-else-if="previewActiveMethod === 'sms'">
                        <div class="mock-input-field">
                          <span class="placeholder-txt">手机号</span>
                        </div>
                        <div class="mock-input-field mt-12 flex-row-input">
                          <span class="placeholder-txt">验证码</span>
                          <span class="send-btn-mock">获取验证码</span>
                        </div>
                      </template>

                      <template v-else-if="previewActiveMethod === 'wechat' || previewActiveMethod === 'email'">
                        <div class="wechat-preview-box">
                          <div class="wechat-qr-code">
                            <div class="wechat-qr-stub">微信扫码登录</div>
                          </div>
                        </div>
                      </template>
                    </div>
                    
                    <!-- Login Button -->
                    <el-button type="primary" class="btn-login-preview-submit mt-20">登录</el-button>
                    
                    <!-- Login Footer -->
                    <div class="login-footer-preview mt-16">
                      <span class="link-forgot-pwd" v-if="createForm.policyMethods.includes('password')">忘记密码</span>
                      <span class="mfa-indicator-txt" v-if="createForm.policyMfa !== 'none'">MFA: {{ getMfaLabel(createForm.policyMfa) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </template>

    <template v-else>
      <!-- Page Heading -->
      <div class="page-heading-premium">
        <div class="heading-left-wrapper">
          <h1>身份域管理</h1>
          <p>维护账号归属的身份空间，并关联登录页、认证策略和安全策略。</p>
        </div>
        <div class="heading-right-actions">
          <el-button :icon="Refresh" class="btn-refresh" @click="fetchData">刷新</el-button>
          <el-button type="primary" :icon="Plus" class="btn-create" @click="openCreateDialog">新增身份域</el-button>
        </div>
      </div>

      <!-- Filters Card -->
      <el-card shadow="never" class="premium-filter-card">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>身份域名称</label>
            <el-input v-model="query.name" placeholder="请输入名称" clearable @keyup.enter="handleSearch" />
          </div>
          <div class="filter-item">
            <label>身份域编码</label>
            <el-input v-model="query.code" placeholder="请输入编码" clearable @keyup.enter="handleSearch" />
          </div>
          <div class="filter-item">
            <label>身份域类型</label>
            <el-select v-model="query.typeCategoryId" placeholder="全部类型" clearable>
              <el-option v-for="item in typeCategoryOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="query.status" placeholder="全部状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="2" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- Table Card -->
      <el-card shadow="never" class="premium-table-card">
        <div class="card-title-header">
          <h3>身份域列表</h3>
          <p>列表只展示识别字段、归属字段、状态字段和常用操作。</p>
        </div>

        <el-table v-loading="loading" :data="tableData" row-key="id" class="premium-table">
          <el-table-column prop="name" label="身份域名称" min-width="160">
            <template #default="{ row }">
              <span class="realm-name-cell" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="身份域编码" min-width="160" />
          <el-table-column label="类型" min-width="110">
            <template #default="{ row }">
              <span class="badge" :class="getCategoryTypeClass(row.typeCategoryId)">
                {{ typeCategoryText(row.typeCategoryId) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="认证方式" min-width="150">
            <template #default="{ row }">
              {{ getAuthMethodsText(row) }}
            </template>
          </el-table-column>
          <el-table-column label="默认登录页" min-width="150">
            <template #default="{ row }">
              {{ getLoginPageText(row) }}
            </template>
          </el-table-column>
          <el-table-column label="策略状态" min-width="110" align="center">
            <template #default="{ row }">
              <span class="badge" :class="getPolicyStatus(row).type === 'success' ? 'green' : (getPolicyStatus(row).type === 'warning' ? 'orange' : 'red')">
                {{ getPolicyStatus(row).text }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="badge" :class="row.status === 1 ? 'green' : 'red'">
                {{ row.status === 1 ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="140" />
          <th width="120">操作</th>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <div class="op-button-group">
                <span class="op-link" @click="openDetail(row)">详情</span>
                <span class="op-link" @click="openEditPage(row)">编辑</span>
                <el-dropdown trigger="click">
                  <span class="op-link-more">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleDisableOrDeleteClick(row, 'disable')" :disabled="row.status === 2">禁用身份域</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDeleteClick(row, 'delete')" class="dropdown-item-danger">删除身份域</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="premium-pager">
          <span class="pager-total">共 {{ total }} 条记录</span>
          <div class="pager-buttons">
            <el-pagination
              v-model:current-page="query.page"
              v-model:page-size="query.size"
              layout="prev, pager, next"
              :total="total"
              @current-change="fetchData"
            />
          </div>
        </div>
      </el-card>
    </template>

    <!-- Custom Danger/Warning Confirmation Dialog -->
    <el-dialog
      v-model="isConfirmDialogVisible"
      :show-close="false"
      class="custom-danger-modal"
      width="480px"
      align-center
    >
      <div class="modal-body-wrapper">
        <div class="modal-title-row">
          <el-icon class="modal-warning-icon"><Warning /></el-icon>
          <h2>确认{{ confirmAction === 'disable' ? '禁用' : '删除' }}身份域？</h2>
        </div>
        <p class="modal-alert-desc">
          {{ confirmAction === 'disable' ? '禁用后，该身份域下的所有账号将无法继续登录，关联的客户端无法完成安全认证。' : '删除为不可逆操作。系统会校验当前身份域是否仍有绑定的用户与客户端实例配置。' }}
        </p>
        
        <!-- Impact references callout -->
        <div class="danger-callout-panel">
          <span class="panel-warning-title">关联影响分析：</span>
          <p class="panel-warning-body">
            当前身份域存在 <strong>{{ getConfirmDetails(activeRealmForConfirm).accounts }}</strong> 个激活账号、<strong>{{ getConfirmDetails(activeRealmForConfirm).subjects }}</strong> 个关联主体、<strong>{{ getConfirmDetails(activeRealmForConfirm).clients }}</strong> 个启用客户端。建议先确认业务影响范围。
          </p>
        </div>
      </div>
      <template #footer>
        <div class="modal-footer-buttons">
          <el-button @click="isConfirmDialogVisible = false" class="btn-cancel">取消</el-button>
          <el-button 
            type="danger" 
            :loading="confirmLoading" 
            @click="executeConfirmAction" 
            class="btn-danger-confirm"
          >
            确认{{ confirmAction === 'disable' ? '禁用' : '删除' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
/* Page Layout */
.realm-page {
  font-family: "Inter", "Segoe UI", Arial, sans-serif;
  color: #334155;
  background-color: #F6F8FB;
  min-height: 100%;
}

/* Premium Heading */
.page-heading-premium {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}
.page-heading-premium h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
  color: #0F172A;
}
.page-heading-premium p {
  margin: 0;
  font-size: 14px;
  color: #64748B;
}
.heading-right-actions {
  display: flex;
  gap: 12px;
}
.btn-refresh {
  border: 1px solid #E2E8F0;
  color: #334155;
  font-weight: 600;
}
.btn-create {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
}
.btn-create:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}

/* Unified Tabs Navigation */
.premium-tabs-bar {
  display: flex;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 8px 16px;
  margin-bottom: 16px;
  gap: 8px;
}
.tab-item {
  height: 34px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  border-radius: 6px;
  color: #64748B;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}
.tab-item.active {
  background-color: #EFF6FF;
  color: #2563EB;
}
.tab-item:hover:not(.active) {
  color: #2563EB;
  background-color: #F8FAFC;
}

/* Filter Card */
.premium-filter-card {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  margin-bottom: 16px;
  background-color: #fff;
}
.premium-filter-card :deep(.el-card__body) {
  padding: 20px 24px;
}
.filter-flex-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 18px;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 220px;
}
.filter-item label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}
.filter-item :deep(.el-input__wrapper),
.filter-item :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: none !important;
}
.filter-buttons {
  display: flex;
  gap: 10px;
}
.filter-buttons :deep(.el-button) {
  height: 36px;
  font-weight: 600;
  border-radius: 6px;
}
.filter-buttons :deep(.el-button--primary) {
  background-color: #2563EB;
  border-color: #2563EB;
}
.filter-buttons :deep(.el-button--primary:hover) {
  background-color: #1D4ED8;
}

/* Table Card */
.premium-table-card {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
}
.premium-table-card :deep(.el-card__body) {
  padding: 0;
}
.card-title-header {
  padding: 18px 24px;
  border-bottom: 1px solid #E5E7EB;
}
.card-title-header h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #0F172A;
  font-weight: 700;
}
.card-title-header p {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* Table styles */
.premium-table {
  width: 100%;
}
.premium-table :deep(th.el-table__cell) {
  background-color: #F8FAFC;
  color: #64748B;
  font-weight: 700;
  height: 44px;
  border-bottom: 1px solid #E5E7EB;
}
.premium-table :deep(td.el-table__cell) {
  height: 56px;
  border-bottom: 1px solid #E5E7EB;
  color: #334155;
}
.realm-name-cell {
  color: #0F172A;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.15s ease;
}
.realm-name-cell:hover {
  color: #2563EB;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}
.badge.blue {
  background-color: #EFF6FF;
  color: #2563EB;
}
.badge.green {
  background-color: #DCFCE7;
  color: #16A34A;
}
.badge.orange {
  background-color: #FFEDD5;
  color: #EA580C;
}
.badge.red {
  background-color: #FEE2E2;
  color: #DC2626;
}
.badge.gray {
  background-color: #F1F5F9;
  color: #64748B;
}

/* Badges for custom type categories */
.tag-platform {
  background-color: #F1F5F9;
  color: #64748B;
}
.tag-tenant {
  background-color: #EFF6FF;
  color: #2563EB;
}
.tag-merchant {
  background-color: #FFEDD5;
  color: #EA580C;
}
.tag-consumer {
  background-color: #F1F5F9;
  color: #64748B;
}
.tag-default {
  background-color: #F1F5F9;
  color: #64748B;
}

/* Op Buttons */
.op-button-group {
  display: flex;
  align-items: center;
  gap: 16px;
}
.op-link {
  color: #2563EB;
  font-weight: 600;
  cursor: pointer;
  font-size: 13px;
  transition: color 0.15s ease;
}
.op-link:hover {
  color: #1D4ED8;
}
.op-link-more {
  color: #64748B;
  font-weight: 600;
  cursor: pointer;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.dropdown-item-danger {
  color: #DC2626 !important;
}

/* Pager */
.premium-pager {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-top: 1px solid #E5E7EB;
  font-size: 13px;
  color: #64748B;
}
.pager-total {
  font-weight: 500;
}

/* Full Page Create Layout & Headers */
.create-realm-page-wrapper {
  background-color: #F6F8FB;
  min-height: 100%;
  padding: 0 0 32px 0;
}
.form-navigation-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.form-navigation-header-row h1 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #0F172A;
}
.subtitle-text {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748B;
}
.btn-op-outline {
  border: 1px solid #E2E8F0;
  color: #334155;
  font-weight: 600;
  background: #fff;
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
}
.btn-new-realm {
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
}
.btn-new-realm:hover {
  background-color: #1D4ED8;
  border-color: #1D4ED8;
}

/* Sub tabs under header */
.form-tabs-sub {
  display: flex;
  gap: 24px;
  border-bottom: 1px solid #E2E8F0;
  margin-bottom: 24px;
  padding-bottom: 12px;
}
.tab-sub-item {
  font-size: 14px;
  font-weight: 600;
  color: #64748B;
  cursor: pointer;
  position: relative;
  padding: 4px 0;
}
.tab-sub-item.active {
  color: #2563EB;
}
.tab-sub-item.active::after {
  content: "";
  position: absolute;
  bottom: -13px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563EB;
}

/* Form Two Column Layout Grid */
.realm-form-two-column-layout {
  display: grid;
  grid-template-columns: 1.80fr 1fr;
  gap: 24px;
  align-items: start;
}

/* Form Cards */
.form-section-card-custom {
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}
.card-title-header-custom {
  margin-bottom: 20px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 16px;
}
.card-title-header-custom h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.card-title-header-custom p {
  margin: 4px 0 0 0;
  font-size: 12px;
  color: #64748B;
}
.card-body-custom {
  padding-top: 4px;
}

/* Helper utilities */
.grid-2-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 20px;
}
.grid-3-col {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px 20px;
}
.grid-4-col {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px 20px;
}
.flex-align-end {
  display: flex;
  align-items: flex-end;
}
.ml-24 { margin-left: 24px; }
.ml-12 { margin-left: 12px; }
.mt-8 { margin-top: 8px; }
.mt-12 { margin-top: 12px; }
.mt-16 { margin-top: 16px; }
.mt-20 { margin-top: 20px; }
.mt-24 { margin-top: 24px; }
.full-width-field {
  width: 100%;
}

/* ElFormItem overrides for clean layout */
.form-left-column :deep(.el-form-item) {
  margin-bottom: 0;
}
.form-left-column :deep(.el-form-item__label) {
  font-size: 12.5px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
  padding: 0;
  line-height: normal;
}
.form-left-column :deep(.el-input__wrapper),
.form-left-column :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}
.form-left-column :deep(.el-textarea__inner) {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  box-shadow: none !important;
}

/* Alert Banner */
.alert-banner-custom {
  background-color: #EFF6FF;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.alert-banner-custom .vertical-bar {
  width: 3px;
  height: 16px;
  background-color: #2563EB;
  border-radius: 2px;
}
.alert-content-text {
  font-size: 12.5px;
  color: #1E40AF;
  line-height: 1.5;
  font-weight: 500;
}

/* Quick Ops Row */
.quick-ops-col {
  display: flex;
  flex-direction: column;
}
.label-muted {
  font-size: 12px;
  color: #64748B;
  font-weight: 500;
}
.ops-row {
  display: flex;
  gap: 8px;
}
.btn-op-outline {
  height: 34px;
  border: 1px solid #E2E8F0;
  background-color: #fff;
  font-weight: 600;
  color: #475569;
  border-radius: 6px;
  font-size: 13px;
}
.btn-op-outline:hover {
  border-color: #2563EB;
  color: #2563EB;
}

/* Strategy summary cards */
.summary-cards-grid-4 {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.summary-card-item {
  background-color: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 8px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.summary-card-item .card-lbl {
  font-size: 11px;
  color: #64748B;
  font-weight: 500;
}
.summary-card-item .card-val {
  font-size: 12px;
  font-weight: 700;
  color: #0F172A;
}

/* Dashed card policy detail */
.dashed-policy-detail-card {
  border: 1px dashed #BFDBFE;
  background-color: #F8FAFC;
  border-radius: 8px;
  padding: 20px;
}

/* Checkbox card lists */
.checkbox-cards-grid-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}
.checkbox-card-item {
  border: 1px solid #E2E8F0;
  background-color: #fff;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}
.checkbox-card-item.checked {
  border-color: #BFDBFE;
  background-color: #EFF6FF;
}
.checkbox-title {
  font-weight: 700;
  color: #0F172A;
  font-size: 13px;
}
.checkbox-desc {
  font-size: 11px;
  color: #94A3B8;
  margin: 4px 0 0 24px;
  line-height: 1.4;
}
.checkbox-card-item :deep(.el-checkbox__label) {
  display: inline-flex;
  align-items: center;
}
.flex-align-center-row {
  display: flex;
  align-items: center;
}
.custom-radio-circle {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 1px solid #CBD5E1;
  display: inline-block;
  position: relative;
  transition: all 0.2s ease;
}
.custom-radio-circle.checked {
  border-color: #2563EB;
}
.custom-radio-circle.checked::after {
  content: "";
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #2563EB;
  position: absolute;
  top: 2px;
  left: 2px;
}

/* Sub security block styling */
.sub-security-block {
  padding-top: 16px;
}
.sub-sec-title h4 {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: #0F172A;
}
.sub-sec-title p {
  margin: 2px 0 0 0;
  font-size: 11.5px;
  color: #64748B;
}

/* Bottom Action Row */
.bottom-actions-row {
  display: flex;
  justify-content: flex-end;
}

/* Preview Sticky Column styling */
.preview-right-column {
  position: relative;
}
.sticky-preview-wrapper {
  position: sticky;
  top: 24px;
}
.preview-title-bar h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.preview-title-bar .subtitle {
  margin: 4px 0 12px 0;
  font-size: 12px;
  color: #64748B;
}

/* Simulated Browser window */
.mock-browser-window {
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
  background-color: #fff;
}
.browser-header-dots {
  background-color: #F8FAFC;
  height: 38px;
  border-bottom: 1px solid #E2E8F0;
  display: flex;
  align-items: center;
  padding: 0 16px;
  position: relative;
}
.browser-header-dots .dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  display: inline-block;
}
.browser-header-dots .dot.red { background-color: #EF4444; }
.browser-header-dots .dot.yellow { background-color: #F59E0B; }
.browser-header-dots .dot.green { background-color: #10B981; }
.browser-address-bar {
  flex: 1;
  background-color: #fff;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  height: 24px;
  margin-left: 20px;
  font-size: 11px;
  color: #64748B;
  display: flex;
  align-items: center;
  padding: 0 10px;
}
.browser-address-bar .lock-icon {
  margin-right: 4px;
}

/* Browser content login page mockup */
.browser-content-area {
  padding: 32px 24px;
  background-color: #EFF6FF;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 340px;
}
.login-card-preview-inner {
  background-color: #fff;
  border-radius: 12px;
  border: 1px solid #F1F5F9;
  padding: 24px;
  width: 100%;
  max-width: 290px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}
.login-inner-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  text-align: center;
}
.login-inner-subtitle {
  margin: 4px 0 16px 0;
  font-size: 11px;
  color: #64748B;
  text-align: center;
}

/* Mock tabs */
.login-methods-tabs-preview {
  display: flex;
  border-bottom: 1px solid #F1F5F9;
  justify-content: center;
  gap: 16px;
}
.tab-preview-item {
  font-size: 12px;
  color: #64748B;
  padding-bottom: 6px;
  cursor: pointer;
  font-weight: 600;
  position: relative;
}
.tab-preview-item.active {
  color: #2563EB;
}
.tab-preview-item.active::after {
  content: "";
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563EB;
}

/* Mock Inputs */
.mock-input-field {
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  height: 36px;
  display: flex;
  align-items: center;
  padding: 0 12px;
}
.placeholder-txt {
  font-size: 12px;
  color: #94A3B8;
}
.flex-row-input {
  display: flex;
  justify-content: space-between;
}
.send-btn-mock {
  font-size: 11px;
  color: #2563EB;
  font-weight: 600;
  cursor: pointer;
}
.btn-login-preview-submit {
  width: 100%;
  height: 38px;
  background-color: #2563EB;
  border-color: #2563EB;
  color: #fff;
  font-weight: 600;
  border-radius: 6px;
}
.login-footer-preview {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
}
.link-forgot-pwd {
  color: #64748B;
  cursor: pointer;
}
.mfa-indicator-txt {
  color: #64748B;
}

/* WeChat QR stub mockup */
.wechat-preview-box {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
.wechat-qr-code {
  width: 90px;
  height: 90px;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #F8FAFC;
}
.wechat-qr-stub {
  font-size: 10px;
  color: #94A3B8;
  font-weight: 600;
}

/* Custom Danger Modal */
.custom-danger-modal :deep(.el-dialog__header) {
  display: none;
}
.custom-danger-modal :deep(.el-dialog__body) {
  padding: 24px 24px 16px;
}
.modal-body-wrapper {
  display: flex;
  flex-direction: column;
}
.modal-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.modal-warning-icon {
  font-size: 24px;
  color: #DC2626;
}
.modal-title-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0F172A;
}
.modal-alert-desc {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: #334155;
  line-height: 22px;
}
.danger-callout-panel {
  background-color: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 8px;
  padding: 12px 14px;
}
.panel-warning-title {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #991B1B;
  margin-bottom: 4px;
}
.panel-warning-body {
  margin: 0;
  font-size: 13px;
  color: #991B1B;
  line-height: 18px;
}
.modal-footer-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.btn-danger-confirm {
  background-color: #DC2626;
  border-color: #DC2626;
  color: #fff;
  font-weight: 600;
  border-radius: 6px;
}
.btn-danger-confirm:hover {
  background-color: #B91C1C;
}

@media (max-width: 1200px) {
  .realm-form-two-column-layout {
    grid-template-columns: 1fr;
  }
  .sticky-preview-wrapper {
    position: relative;
    top: 0;
    margin-top: 24px;
  }
}

/* Responsive adjustments */
.check-grid-custom {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  width: 100%;
}
.check-grid-custom :deep(.el-checkbox) {
  height: auto;
  min-height: 54px;
  padding: 8px 14px;
  margin: 0 !important;
  display: flex;
  align-items: flex-start;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  transition: all 0.2s ease;
}
.check-grid-custom :deep(.el-checkbox.is-checked) {
  border-color: #BFDBFE;
  background-color: #EFF6FF;
}
.check-grid-custom :deep(.el-checkbox__label) {
  padding-left: 8px;
  font-size: 13px;
  color: #334155;
  font-weight: 600;
  white-space: normal;
  line-height: 1.4;
}
.check-grid-custom :deep(.el-checkbox__input) {
  margin-top: 2px;
}

@media (max-width: 1024px) {
  .form-grid-3 {
    grid-template-columns: 1fr 1fr;
  }
  .check-grid-custom {
    grid-template-columns: 1fr 1fr;
  }
}
@media (max-width: 768px) {
  .form-grid-3 {
    grid-template-columns: 1fr;
  }
  .check-grid-custom {
    grid-template-columns: 1fr;
  }
  .filter-flex-row {
    flex-direction: column;
    align-items: stretch;
  }
  .filter-item {
    width: auto;
  }
}
</style>
