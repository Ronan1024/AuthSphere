<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, Search, Refresh, ArrowDown, ArrowLeft, Warning,
  InfoFilled, View, Edit, CopyDocument, Delete, Check, Connection, Document
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authMethodApi } from '@/api/authMethod'
import type { AuthMethodTemplateResponse } from '@/api/authMethod'

const router = useRouter()

interface AuthMethodReference {
  id: string
  code: string
  name: string
  role: string
  usage?: string
  status: number
}

interface AuthMethodRecord {
  id: string
  name: string
  code: string
  positions: string[]
  applicableRange: string
  status: number
  referenceCount: number
  sortNo: number
  description: string
  params: Record<string, any>
  systemBuiltin?: boolean
  references?: AuthMethodReference[]
  template?: string
  fieldsCount?: number
}


// Current view: 'list' | 'create' | 'edit' | 'detail'
const currentView = ref<'list' | 'create' | 'edit' | 'detail'>('list')

const references = ref<AuthMethodReference[]>([])
const isDeleteBlockOpen = ref(false)
const referencedMethodForConfirm = ref<any>(null)


// Computed read-only state for detail view
const isReadOnly = computed(() => currentView.value === 'detail')

// Page Title
const pageTitle = computed(() => {
  if (currentView.value === 'create') return '新增认证方式'
  if (currentView.value === 'edit') return '编辑认证方式'
  return '认证方式详情'
})

interface FormField {
  label: string
  code: string
  type: string
  required: boolean
  secret: boolean
  placeholder: string
}

// Form models for unified Create/Edit/Detail view
const formModel = reactive({
  id: '',
  name: '',
  code: '',
  applicableRange: '全部',
  status: 1,
  sortNo: 10,
  description: '',
  positions: [] as string[],
  template: 'extend',
  formSchema: [] as FormField[],
  params: {
    // SMS defaults
    smsProvider: '使用平台默认短信通道',
    codeTtl: 300,
    sendInterval: 60,
    dailyLimit: 10,
    retryLimit: 5,
    templateCode: 'LOGIN_CODE',
    // Email defaults
    emailProvider: '使用平台默认邮件通道',
    emailTtl: 300,
    emailTemplate: 'login-code-template',
    // Password/TOTP defaults (for none template)
    passwordPolicy: 'default',
    maxRetries: 5,
    lockDuration: 30,
    totpDigits: 6,
    totpPeriod: 30,
    totpWindow: 1,
    // OIDC defaults
    oidcIssuer: 'https://id.example.com',
    clientId: '',
    clientSecret: '',
    userField: 'sub',
    callbackUrl: '/oauth/callback/{methodCode}',
    // LDAP defaults
    ldapUrl: 'ldap://127.0.0.1:389',
    baseDn: 'dc=example,dc=com',
    ldapUserField: 'uid',
    ldapMailField: 'mail',
    // Client defaults
    tokenTtl: 365,
    signAlg: 'HS256',
    allowScope: 'iam:token, api:read'
  }
})

// Query filters
const query = reactive({
  name: '',
  templateKind: '',
  position: '',
  status: undefined as number | undefined
})

const methods = ref<AuthMethodRecord[]>([])
const builtinTemplateMap = ref<Record<string, AuthMethodTemplateResponse>>({})
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(100)

const saveLoading = ref(false)


const positionOptions = [
  { label: '主登录', desc: '作为主登录方式' },
  { label: 'MFA 二次认证', desc: '作为二次强化认证选项' },
  { label: '接口认证', desc: '服务端接口凭证校验' },
  { label: '账号绑定校验', desc: '绑定手机号或第三方账号' },
  { label: '敏感操作', desc: '高风险操作再次校验' }
]

const templateOptions = [
  { key: 'password', title: '账号密码模板', desc: '系统内置。基于 password_hash 进行凭证校验，字段不可自定义。', kind: '系统内置' },
  { key: 'totp', title: 'TOTP 模板', desc: '系统内置。本地算法校验动态口令，配置参数不可自定义。', kind: '系统内置' },
  { key: 'extend', title: '扩展登录模板', desc: '用户自定义字段。适合微信/支付宝小程序及各种自定义系统。', kind: '可自定义字段' },
  { key: 'client', title: 'Client Credentials', desc: '系统内置。用于服务端接口认证，不开放字段结构修改。', kind: '系统内置' }
]

const lockedTemplateItems: Record<string, { title: string; desc: string }[]> = {
  password: [
    { title: '允许用户名登录', desc: '包含英文/数字，不允许包含中文字符' },
    { title: '允许手机号登录', desc: '必须是合规的 11 位大陆手机号' },
    { title: '允许邮箱登录', desc: '合规电子邮箱账号，不允许包含中文字符' },
    { title: '密码策略来源', desc: '使用身份域默认密码策略或主体自定义策略' }
  ],
  totp: [
    { title: '校验安全度', desc: '默认 6 位' },
    { title: '时间步长', desc: '默认 30 秒' },
    { title: '允许时间窗口', desc: '默认前后 1 个窗口' },
    { title: '恢复码', desc: '可选平台能力作为容灾校验' }
  ],
  client: [
    { title: '客户端标识', desc: '系统生成或录入' },
    { title: '密钥保存', desc: '只保存 Hash 或加密值' },
    { title: '授权范围', desc: '基于 Client scope 控制' },
    { title: '有效期', desc: '依 Token 策略周期控制' }
  ]
}

const getTemplateLabel = (template?: string) => {
  return templateOptions.find(item => item.key === template)?.title || '未选择模板'
}

const getTemplateKind = (template?: string) => {
  return template === 'extend' ? '扩展模板' : '系统内置'
}

const getParamsSummary = (row: any) => {
  const tpl = row.template || ''
  const code = (row.code || '').toLowerCase()
  if (tpl === 'password') return '密码策略来源、账号锁定策略'
  if (tpl === 'sms' || code.includes('sms')) return '手机号、验证码规则、通道来源'
  if (tpl === 'totp' || code.includes('totp') || code.includes('mfa')) return '时间步长、恢复码'
  if (code.includes('wechat') || code.includes('miniapp')) return 'AppId、AppSecret、openid映射'
  if (tpl === 'extend' || code.includes('custom') || code.includes('extend')) return '登录接口、请求密钥、用户ID字段'
  if (tpl === 'client') return '客户端标识、密钥校验'
  return '-'
}

const getStatusText = (status: number) => {
  return status === 1 ? '启用' : (status === 2 ? '待配置' : '禁用')
}

const loadBuiltinTemplates = async () => {
  try {
    const templates = await authMethodApi.templates()
    builtinTemplateMap.value = templates.reduce<Record<string, AuthMethodTemplateResponse>>((result, item) => {
      result[item.template] = item
      return result
    }, {})
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证方式模板失败')
  }
}

const applyBuiltinTemplateDefaults = (template: string) => {
  if (currentView.value !== 'create') return
  const builtinTemplate = builtinTemplateMap.value[template]
  if (!builtinTemplate) return
  formModel.name = builtinTemplate.defaultName
  formModel.code = builtinTemplate.defaultCode
}

const selectTemplate = async (template: string) => {
  if (isReadOnly.value) return
  formModel.template = template
  if (!builtinTemplateMap.value[template]) {
    await loadBuiltinTemplates()
  }
  applyBuiltinTemplateDefaults(template)
}

const addField = () => {
  formModel.formSchema.push({
    label: '新字段',
    code: 'newField' + (formModel.formSchema.length + 1),
    type: 'text',
    required: false,
    secret: false,
    placeholder: '请输入'
  })
  ElMessage.success('已新增字段')
}

const removeField = (index: number) => {
  formModel.formSchema.splice(index, 1)
  ElMessage.success('字段已删除')
}

const handleTypeChange = (type: any, index: number) => {
  if (formModel.formSchema[index]) {
    formModel.formSchema[index].secret = (type === 'secret')
  }
}

const handleSecretChange = (secret: any, index: number) => {
  if (formModel.formSchema[index]) {
    if (secret) {
      formModel.formSchema[index].type = 'secret'
    } else if (formModel.formSchema[index].type === 'secret') {
      formModel.formSchema[index].type = 'text'
    }
  }
}


const loadMethods = async () => {
  loading.value = true
  try {
    const res = await authMethodApi.page({
      page: page.value,
      size: size.value,
      position: query.position || undefined,
      status: query.status !== undefined ? query.status : undefined
    })
    methods.value = res.records.map((r: any) => {
      let inferredTemplate = 'none'
      const code = (r.code || '').toLowerCase()
      if (code.includes('sms')) {
        inferredTemplate = 'sms'
      } else if (code.includes('email')) {
        inferredTemplate = 'email'
      } else if (code.includes('oidc')) {
        inferredTemplate = 'oidc'
      } else if (code.includes('ldap')) {
        inferredTemplate = 'ldap'
      } else if (code.includes('client') || code.includes('api')) {
        inferredTemplate = 'client'
      } else if (r.params?.formSchema || code.includes('extend') || code.includes('wechat') || code.includes('custom')) {
        inferredTemplate = 'extend'
      } else if (code.includes('password')) {
        inferredTemplate = 'password'
      } else if (code.includes('totp')) {
        inferredTemplate = 'totp'
      }

      const fieldsCount = r.params?.formSchema ? r.params.formSchema.length : 0

      return {
        id: r.id,
        name: r.name,
        code: r.code,
        positions: r.positions || [],
        applicableRange: r.applicableRange || '',
        status: r.status,
        referenceCount: r.referenceCount || 0,
        sortNo: r.sortNo || 0,
        description: r.description || '',
        template: inferredTemplate,
        fieldsCount: fieldsCount,
        params: r.params || {}
      }
    })
    total.value = res.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证方式列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBuiltinTemplates()
  loadMethods()
})

const filteredMethods = computed(() => {
  return methods.value.filter((item) => {
    const keyword = query.name.trim().toLowerCase()
    const keywordMatched = !keyword ||
      item.name.toLowerCase().includes(keyword) ||
      item.code.toLowerCase().includes(keyword) ||
      getTemplateLabel(item.template).toLowerCase().includes(keyword)
    const templateKindMatched = !query.templateKind || getTemplateKind(item.template) === query.templateKind
    const positionMatched = !query.position || (item.positions && item.positions.includes(query.position))
    const statusMatched = query.status === undefined || (query.status as any) === '' || item.status === query.status
    return keywordMatched && templateKindMatched && positionMatched && statusMatched
  })
})

const getPositionLabel = (positions: string[]) => {
  return (positions || []).join(' / ')
}

const togglePosition = (pos: string) => {
  if (isReadOnly.value) return
  const idx = formModel.positions.indexOf(pos)
  if (idx >= 0) {
    formModel.positions.splice(idx, 1)
  } else {
    formModel.positions.push(pos)
  }
}

const handleReset = () => {
  query.name = ''
  query.templateKind = ''
  query.position = ''
  query.status = undefined
  page.value = 1
  loadMethods()
}

const handleSearch = () => {
  page.value = 1
  loadMethods()
}

const loadDetailIntoForm = (detailData: any) => {
  let inferredTemplate = 'none'
  const code = (detailData.code || '').toLowerCase()
  if (code.includes('sms')) {
    inferredTemplate = 'sms'
  } else if (code.includes('email')) {
    inferredTemplate = 'email'
  } else if (code.includes('oidc')) {
    inferredTemplate = 'oidc'
  } else if (code.includes('ldap')) {
    inferredTemplate = 'ldap'
  } else if (code.includes('client') || code.includes('api')) {
    inferredTemplate = 'client'
  } else if (detailData.params?.formSchema || code.includes('extend') || code.includes('wechat') || code.includes('custom')) {
    inferredTemplate = 'extend'
  } else if (code.includes('password')) {
    inferredTemplate = 'password'
  } else if (code.includes('totp')) {
    inferredTemplate = 'totp'
  }

  Object.assign(formModel, {
    id: detailData.id,
    name: detailData.name,
    code: detailData.code,
    applicableRange: detailData.applicableRange || '全部',
    status: detailData.status,
    sortNo: detailData.sortNo || 10,
    description: detailData.description || '',
    positions: detailData.positions || [],
    template: inferredTemplate,
    formSchema: detailData.params?.formSchema || [],
    params: {
      ...formModel.params,
      ...detailData.params
    }
  })
}

const openCreateView = () => {
  Object.assign(formModel, {
    id: '',
    name: '',
    code: '',
    applicableRange: '全部',
    status: 1,
    sortNo: 10,
    description: '',
    positions: ['主登录'],
    template: 'extend',
    formSchema: [] as FormField[],
    params: {
      // SMS defaults
      smsProvider: '使用平台默认短信通道',
      codeTtl: 300,
      sendInterval: 60,
      dailyLimit: 10,
      retryLimit: 5,
      templateCode: 'LOGIN_CODE',
      // Email defaults
      emailProvider: '使用平台默认邮件通道',
      emailTtl: 300,
      emailTemplate: 'login-code-template',
      // Password/TOTP defaults (for none template)
      passwordPolicy: 'default',
      maxRetries: 5,
      lockDuration: 30,
      totpDigits: 6,
      totpPeriod: 30,
      totpWindow: 1,
      // OIDC defaults
      oidcIssuer: 'https://id.example.com',
      clientId: '',
      clientSecret: '',
      userField: 'sub',
      callbackUrl: '/oauth/callback/{methodCode}',
      // LDAP defaults
      ldapUrl: 'ldap://127.0.0.1:389',
      baseDn: 'dc=example,dc=com',
      ldapUserField: 'uid',
      ldapMailField: 'mail',
      // Client defaults
      tokenTtl: 365,
      signAlg: 'HS256',
      allowScope: 'iam:token, api:read'
    }
  })
  references.value = []
  currentView.value = 'create'
}

const openDetail = async (row: AuthMethodRecord) => {
  try {
    const detailData = await authMethodApi.detail(row.id)
    loadDetailIntoForm(detailData)
    references.value = detailData.references || []
    currentView.value = 'detail'
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证方式详情失败')
  }
}

const openEditView = async (row: AuthMethodRecord) => {
  try {
    const detailData = await authMethodApi.detail(row.id)
    loadDetailIntoForm(detailData)
    references.value = detailData.references || []
    currentView.value = 'edit'
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证方式详情失败')
  }
}

const enterEditMode = () => {
  currentView.value = 'edit'
}

const handleCancelForm = () => {
  if (formModel.id) {
    currentView.value = 'detail'
  } else {
    currentView.value = 'list'
  }
}

const goBackToList = () => {
  currentView.value = 'list'
}

const handleEnableForm = async () => {
  try {
    await authMethodApi.enable(formModel.id)
    ElMessage.success('认证方式已启用')
    formModel.status = 1
    loadMethods()
  } catch (error: any) {
    ElMessage.error(error.message || '启用失败')
  }
}

const handleEnable = async (row: AuthMethodRecord) => {
  try {
    await authMethodApi.enable(row.id)
    ElMessage.success('认证方式已启用')
    loadMethods()
  } catch (error: any) {
    ElMessage.error(error.message || '启用失败')
  }
}

const handleDisableForm = async () => {
  try {
    const detailData = await authMethodApi.detail(formModel.id)
    if (detailData.referenceCount > 0) {
      ElMessageBox.confirm(
        `该认证方式已应用在 ${detailData.referenceCount} 个策略中。禁用后已配置的策略认证流程将受影响，是否确认禁用？`,
        '风险提示',
        { type: 'warning', confirmButtonClass: 'el-button--danger' }
      ).then(async () => {
        await authMethodApi.disable(formModel.id)
        ElMessage.success('认证方式已禁用')
        formModel.status = 3
        loadMethods()
      }).catch(() => {})
    } else {
      ElMessageBox.confirm('确认禁用该认证方式？', '提示', { type: 'warning' }).then(async () => {
        await authMethodApi.disable(formModel.id)
        ElMessage.success('认证方式已禁用')
        formModel.status = 3
        loadMethods()
      }).catch(() => {})
    }
  } catch (error: any) {
    ElMessage.error(error.message || '禁用失败')
  }
}

const handleDeleteForm = async () => {
  try {
    const detailData = await authMethodApi.detail(formModel.id)
    if (detailData.referenceCount > 0) {
      referencedMethodForConfirm.value = detailData
      isDeleteBlockOpen.value = true
    } else {
      ElMessageBox.confirm('确认删除该认证方式？', '提示', { type: 'warning' }).then(async () => {
        await authMethodApi.delete(formModel.id)
        ElMessage.success('认证方式已成功删除')
        currentView.value = 'list'
        loadMethods()
      }).catch(() => {})
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const handleDisableOrDelete = async (row: AuthMethodRecord, action: 'disable' | 'delete') => {
  try {
    const detailData = await authMethodApi.detail(row.id)
    referencedMethodForConfirm.value = detailData

    if (detailData.referenceCount > 0) {
      if (action === 'disable') {
        ElMessageBox.confirm(
          `该认证方式已应用在 ${detailData.referenceCount} 个策略中。禁用后已配置的策略认证流程将受影响，是否确认禁用？`,
          '风险提示',
          { type: 'warning', confirmButtonClass: 'el-button--danger' }
        ).then(async () => {
          await authMethodApi.disable(row.id)
          ElMessage.success('认证方式已禁用')
          loadMethods()
        }).catch(() => {})
      } else {
        isDeleteBlockOpen.value = true
      }
    } else {
      const actionName = action === 'disable' ? '禁用' : '删除'
      ElMessageBox.confirm(`确认${actionName}该认证方式？`, '提示', { type: 'warning' }).then(async () => {
        if (action === 'disable') {
          await authMethodApi.disable(row.id)
          ElMessage.success('认证方式已成功禁用')
        } else {
          await authMethodApi.delete(row.id)
          ElMessage.success('认证方式已成功删除')
        }
        loadMethods()
      }).catch(() => {})
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const getParamsByTemplate = (template: string, rawParams: Record<string, any>) => {
  const result: Record<string, any> = {}
  if (template === 'sms') {
    result.smsProvider = rawParams.smsProvider
    result.codeTtl = Number(rawParams.codeTtl)
    result.sendInterval = Number(rawParams.sendInterval)
    result.dailyLimit = Number(rawParams.dailyLimit)
    result.retryLimit = Number(rawParams.retryLimit)
    result.templateCode = rawParams.templateCode
  } else if (template === 'email') {
    result.emailProvider = rawParams.emailProvider
    result.emailTtl = Number(rawParams.emailTtl)
    result.emailTemplate = rawParams.emailTemplate
    result.sendInterval = Number(rawParams.sendInterval)
  } else if (template === 'oidc') {
    result.oidcIssuer = rawParams.oidcIssuer
    result.clientId = rawParams.clientId
    result.clientSecret = rawParams.clientSecret
    result.userField = rawParams.userField
    result.callbackUrl = rawParams.callbackUrl
  } else if (template === 'ldap') {
    result.ldapUrl = rawParams.ldapUrl
    result.baseDn = rawParams.baseDn
    result.ldapUserField = rawParams.ldapUserField
    result.ldapMailField = rawParams.ldapMailField
  } else if (template === 'client') {
    result.tokenTtl = Number(rawParams.tokenTtl)
    result.signAlg = rawParams.signAlg
    result.allowScope = rawParams.allowScope
  } else if (template === 'extend') {
    result.formSchema = formModel.formSchema || []
  } else if (template === 'password') {
    result.passwordPolicy = rawParams.passwordPolicy
    result.maxRetries = Number(rawParams.maxRetries)
    result.lockDuration = Number(rawParams.lockDuration)
  } else if (template === 'totp') {
    result.totpDigits = Number(rawParams.totpDigits)
    result.totpPeriod = Number(rawParams.totpPeriod)
    result.totpWindow = Number(rawParams.totpWindow)
  }
  return result
}

const submitForm = async () => {
  if (!formModel.name || !formModel.code) {
    ElMessage.error('请填写名称与编码')
    return
  }
  if (!formModel.positions || formModel.positions.length === 0) {
    ElMessage.error('至少选择一个可用位置')
    return
  }
  if (formModel.template === 'extend') {
    if (!formModel.formSchema.length) {
      ElMessage.error('扩展模板至少需要定义一个字段')
      return
    }
    const fieldCodes = formModel.formSchema.map(field => field.code.trim()).filter(Boolean)
    if (fieldCodes.length !== formModel.formSchema.length) {
      ElMessage.error('请补全所有字段编码')
      return
    }
    if (new Set(fieldCodes).size !== fieldCodes.length) {
      ElMessage.error('字段编码在当前认证方式内必须唯一')
      return
    }
  }
  saveLoading.value = true
  try {
    const payload = {
      code: formModel.code,
      name: formModel.name,
      positions: formModel.positions,
      applicableRange: formModel.applicableRange,
      status: formModel.status,
      sortNo: formModel.sortNo,
      description: formModel.description,
      params: getParamsByTemplate(formModel.template, formModel.params)
    }

    if (formModel.id) {
      await authMethodApi.update(formModel.id, payload)
      ElMessage.success('更新认证方式成功')
      currentView.value = 'detail'
    } else {
      await authMethodApi.create(payload)
      ElMessage.success('新增认证方式成功')
      currentView.value = 'list'
    }
    loadMethods()
  } catch (error: any) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    saveLoading.value = false
  }
}



</script>

<template>
  <div class="auth-method-module">
    <!-- View 1: List Layout -->
    <div v-if="currentView === 'list'" class="list-layout">
      <div class="view-header">
        <div class="title-wrap">
          <h1>认证方式管理</h1>
          <p>只维护具体认证方式，不再展示认证类型页面；认证策略直接从已启用的方式中进行组合选择。</p>
        </div>
        <div class="action-buttons">
          <el-button @click="loadMethods">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openCreateView">新增认证方式</el-button>
        </div>
      </div>

      <!-- Filter Card -->
      <el-card shadow="never" class="filter-card" style="margin-top: 16px;">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>关键词</label>
            <el-input v-model="query.name" placeholder="名称 / 编码 / 模板" clearable @keyup.enter="handleSearch" />
          </div>
          <div class="filter-item">
            <label>模板类型</label>
            <el-select v-model="query.templateKind" placeholder="全部" clearable>
              <el-option label="系统内置" value="系统内置" />
              <el-option label="扩展模板" value="扩展模板" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>作用位置</label>
            <el-select v-model="query.position" placeholder="全部" clearable @change="handleSearch">
              <el-option v-for="item in positionOptions" :key="item.label" :label="item.label" :value="item.label" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="query.status" placeholder="全部" clearable @change="handleSearch">
              <el-option label="启用" :value="1" />
              <el-option label="待实现" :value="2" />
              <el-option label="禁用" :value="3" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- Table Card -->
      <el-card shadow="never" class="table-card">
        <div class="card-header-title">
          <h3>认证方式列表</h3>
          <p>基于系统内置模板或扩展模板创建认证方式。内置模板字段锁定，扩展模板允许自定义动态表单字段。</p>
        </div>

        <el-table :data="filteredMethods" class="premium-table" v-loading="loading">
          <el-table-column prop="name" label="认证方式" min-width="160">
            <template #default="{ row }">
              <div style="cursor: pointer" @click="openDetail(row)">
                <b style="color: #0f172a; font-weight: 700;">{{ row.name }}</b>
                <div style="font-size: 12px; color: #94a3b8; margin-top: 2px;">{{ row.code }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="作用位置" min-width="140">
            <template #default="{ row }">
              <span 
                v-for="pos in row.positions" 
                :key="pos" 
                class="badge"
                :class="{
                  'blue': pos.includes('登录') || pos.includes('主登录'),
                  'orange': pos.includes('MFA'),
                  'gray': pos.includes('绑定'),
                  'plain': pos.includes('接口')
                }"
                style="margin-right: 4px;"
              >
                {{ pos.replace('主登录', '登录').replace('MFA 二次认证', 'MFA').replace('账号绑定校验', '绑定校验') }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="模板类型" min-width="150">
            <template #default="{ row }">
              <span>{{ row.template === 'extend' ? '扩展表单 / 用户定义' : '系统内置 / 字段锁定' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="配置摘要" min-width="200">
            <template #default="{ row }">
              <span style="font-size: 13px; color: #475569;">{{ getParamsSummary(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sortNo" label="排序" width="80" align="center" />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="badge" :class="row.status === 1 ? 'green' : (row.status === 2 ? 'orange' : 'red')">
                {{ getStatusText(row.status) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="引用" min-width="100" align="center">
            <template #default="{ row }">
              <span style="color: #64748b; font-size: 13px;">{{ row.referenceCount }} 个策略</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <span class="link-btn" @click="openEditView(row)">编辑</span>
                <el-dropdown trigger="click">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleEnable(row)" v-if="row.status !== 1">启用方式</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'disable')" v-else>禁用方式</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'delete')" class="danger-item">删除方式</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="table-pagination-footer">
          <span class="total-text">共 {{ filteredMethods.length }} 条记录</span>
        </div>
      </el-card>
    </div>

    <!-- View 2: Create / Edit / Detail View -->
    <div v-else-if="currentView === 'create' || currentView === 'edit' || currentView === 'detail'" class="form-layout tall">
      <div class="view-header">
        <div class="header-back-wrap" @click="goBackToList">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>{{ pageTitle }}</h2>
        </div>
        <div class="action-buttons">
          <!-- Detail View Buttons -->
          <template v-if="currentView === 'detail'">
            <el-button @click="goBackToList">返回列表</el-button>
            <el-button type="primary" @click="enterEditMode">进入编辑</el-button>
            <el-button type="success" @click="handleEnableForm" v-if="formModel.status !== 1">启用方式</el-button>
            <el-button type="danger" @click="handleDisableForm" v-else>禁用方式</el-button>
            <el-button type="danger" plain @click="handleDeleteForm">删除方式</el-button>
          </template>
          <!-- Create / Edit View Buttons -->
          <template v-else>
            <el-button @click="handleCancelForm">取消</el-button>
            <el-button type="primary" :loading="saveLoading" @click="submitForm">保存</el-button>
          </template>
        </div>
      </div>

      <div class="form-body-tall-wrapper">
        <div style="margin-top: 0px;">
            <!-- Panel 1: Base Info -->
            <section class="panel-section" id="base">
              <h3>基础信息</h3>
              <div class="field-grid">
                <div class="control-item">
                  <label>认证方式名称 <span class="req" v-if="!isReadOnly">*</span></label>
                  <el-input v-model="formModel.name" placeholder="例如：租户短信验证码认证" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>认证方式编码 <span class="req" v-if="!isReadOnly">*</span></label>
                  <el-input v-model="formModel.code" placeholder="例如：tenant_sms_code" :disabled="isReadOnly || formModel.id !== ''" />
                  <span class="input-help-text" v-if="!isReadOnly">英文、数字、下划线，保存后不建议修改。</span>
                </div>
                <div class="control-item">
                  <label>状态</label>
                  <el-select v-model="formModel.status" :disabled="isReadOnly" style="width: 100%">
                    <el-option label="启用" :value="1" />
                    <el-option label="待实现" :value="2" />
                    <el-option label="禁用" :value="3" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>排序</label>
                  <el-input v-model.number="formModel.sortNo" type="number" placeholder="请输入序号" :disabled="isReadOnly" />
                </div>
                <div class="control-item span-full">
                  <label>说明</label>
                  <el-input v-model="formModel.description" type="textarea" :rows="2" placeholder="说明该认证方式的用途，例如用于租户后台手机号验证码登录。" :disabled="isReadOnly" />
                </div>
              </div>
            </section>

            <!-- Panel 2: Positions -->
            <section class="panel-section" id="scope" style="margin-top: 18px;">
              <h3>可作用位置</h3>
              <p class="section-desc">决定该认证方式可用于哪些认证环节。</p>
              <div class="choice-grid">
                <div 
                  v-for="item in positionOptions"
                  :key="item.label"
                  class="choice" 
                  :class="[formModel.positions.includes(item.label) ? 'on' : '', isReadOnly ? 'disabled-choice' : '']"
                  role="button"
                  tabindex="0"
                  @click="togglePosition(item.label)"
                  @keydown.enter.prevent="togglePosition(item.label)"
                  @keydown.space.prevent="togglePosition(item.label)"
                >
                  <b>{{ item.label }}</b>
                  <span>{{ item.desc }}</span>
                </div>
              </div>
              <div class="input-help-text" style="margin-top: 8px;">例如短信验证码可以用于主登录和 MFA；Client Credentials 通常只用于接口认证；敏感操作用于高风险动作再次校验。</div>
            </section>

            <!-- Panel 3: Templates selection -->
            <section class="panel-section" id="tpl" style="margin-top: 18px;">
              <h3>选择配置模板</h3>
              <p class="section-desc">内置模板字段锁定不可修改；扩展模板可以自行定义参数配置表单字段。</p>
              <div class="templates-grid">
                <div 
                  v-for="tpl in templateOptions"
                  :key="tpl.key"
                  class="tpl-card" 
                  :class="[formModel.template === tpl.key ? 'active' : '', isReadOnly ? 'disabled-tpl' : '']"
                  role="button"
                  tabindex="0"
                  @click="selectTemplate(tpl.key)"
                  @keydown.enter.prevent="selectTemplate(tpl.key)"
                  @keydown.space.prevent="selectTemplate(tpl.key)"
                >
                  <span class="tag lock-tag" :class="tpl.key === 'extend' ? 'orange' : 'gray'">
                    {{ tpl.key === 'extend' ? '可自定义字段' : '锁定' }}
                  </span>
                  <b>{{ tpl.title }}</b>
                  <span>{{ tpl.desc }}</span>
                  <small>{{ tpl.kind }}</small>
                </div>
              </div>
            </section>

            <!-- Panel 4: Template Parameters Configuration -->
            <section class="panel-section" id="config" style="margin-top: 18px;">
              <h3>模板参数配置</h3>
              <p class="section-desc">
                {{ formModel.template === 'extend'
                  ? '为扩展认证方式定义需要填写的参数。字段定义保存后将用于新增/编辑该认证方式的配置表单。'
                  : '当前为系统内置模板，字段结构由系统提供，不允许用户自定义。页面只展示模板提供的固定配置项。' }}
              </p>
              
              <!-- Case: Extend Custom Builder -->
              <div v-if="formModel.template === 'extend'" class="builder-wrap">
                <!-- Left: Field definition cards (compressed to save space) -->
                <div class="builder-panel">
                  <div class="builder-title" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                    <b style="font-size: 14px; color: #0f172a;">字段配置</b>
                    <div v-if="!isReadOnly">
                      <el-button size="small" type="primary" @click="addField">新增字段</el-button>
                    </div>
                  </div>
                  
                  <div class="fields-list" style="display: flex; flex-direction: column; gap: 10px;">
                    <div v-for="(field, index) in formModel.formSchema" :key="index" class="field-card" style="border: 1px solid #e2e8f0; border-radius: 8px; background: #ffffff; padding: 12px;">
                      <div class="field-card-head" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; border-bottom: 1px solid #f1f5f9; padding-bottom: 6px;">
                        <b style="font-size: 13px; color: #334155;">#{{ index + 1 }} {{ field.label || '新自定义字段' }}</b>
                        <el-button v-if="!isReadOnly" size="small" type="danger" link @click="removeField(index)">删除</el-button>
                      </div>
                      
                      <!-- Compressing the field configuration space as requested -->
                      <div class="field-card-body-grid" style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                        <div class="control-item">
                          <label style="font-size: 11px; color: #64748b;">字段名称</label>
                          <el-input v-model="field.label" placeholder="例如：AppId" :disabled="isReadOnly" size="small" />
                        </div>
                        <div class="control-item">
                          <label style="font-size: 11px; color: #64748b;">字段编码</label>
                          <el-input v-model="field.code" placeholder="例如：appId" :disabled="isReadOnly" size="small" />
                        </div>
                        <div class="control-item">
                          <label style="font-size: 11px; color: #64748b;">字段类型</label>
                          <el-select v-model="field.type" :disabled="isReadOnly" size="small" style="width: 100%" @change="(val: any) => handleTypeChange(val, index)">
                            <el-option label="文本 (text)" value="text" />
                            <el-option label="密文 (secret)" value="secret" />
                            <el-option label="链接 (url)" value="url" />
                            <el-option label="数字 (number)" value="number" />
                            <el-option label="下拉 (select)" value="select" />
                            <el-option label="开关 (boolean)" value="boolean" />
                          </el-select>
                        </div>
                        <div class="control-item">
                          <label style="font-size: 11px; color: #64748b;">校验规则</label>
                          <div style="height: 24px; display: flex; align-items: center; gap: 10px;">
                            <el-checkbox v-model="field.required" :disabled="isReadOnly" size="small">必填</el-checkbox>
                            <el-checkbox v-model="field.secret" :disabled="isReadOnly" size="small" @change="(val: any) => handleSecretChange(val, index)">密文</el-checkbox>
                          </div>
                        </div>
                        <div class="control-item span-full" style="grid-column: span 2;">
                          <label style="font-size: 11px; color: #64748b;">占位提示</label>
                          <el-input v-model="field.placeholder" placeholder="请输入占位提示" :disabled="isReadOnly" size="small" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                
                <!-- Right: Form Preview (Larger & prettier) -->
                <div class="preview" style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px; padding: 18px; align-self: start; position: sticky; top: 24px;">
                  <div class="preview-title" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; border-bottom: 1px solid #edf2f7; padding-bottom: 8px;">
                    <b style="font-size: 14px; color: #0f172a;">表单预览效果</b>
                    <span class="badge green" style="font-size: 10px; height: 18px; padding: 0 6px;">LIVE PREVIEW</span>
                  </div>
                  
                  <div class="fake-form" style="display: flex; flex-direction: column; gap: 12px;">
                    <div v-for="(field, idx) in formModel.formSchema" :key="idx" class="fake-row" style="background: #ffffff; border: 1px solid #f1f5f9; border-radius: 8px; padding: 10px 14px;">
                      <div class="fake-label" style="display: flex; justify-content: space-between; font-size: 12px; color: #475569; margin-bottom: 6px;">
                        <span>{{ field.label || '未命名字段' }} <span v-if="field.required" class="req" style="color: #dc2626; margin-left: 2px;">*</span></span>
                        <span style="font-size: 10px; color: #94a3b8; font-family: monospace;">{{ field.type }}{{ field.secret ? ' / 密文' : '' }}</span>
                      </div>
                      
                      <!-- Render control type simulation -->
                      <div v-if="field.type === 'boolean'" style="height: 32px; display: flex; align-items: center;">
                        <el-switch disabled active-text="开启" inactive-text="关闭" />
                      </div>
                      <div v-else class="fake-control" style="height: 32px; background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 6px; display: flex; align-items: center; padding: 0 10px; color: #94a3b8; font-size: 12px;">
                        {{ field.placeholder || '请输入' + (field.label || '') }}
                      </div>
                    </div>
                    <div v-if="formModel.formSchema.length === 0" style="padding: 30px 10px; text-align: center; color: #94a3b8; font-size: 12px;">
                      暂无自定义配置字段，点击左侧“新增字段”添加配置项。
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="locked-template-wrap">
                <div class="locked-info">
                  <el-icon class="info-icon"><InfoFilled /></el-icon>
                  <div>
                    <strong>字段定义已锁定</strong>
                    <span>该模板由系统内置实现，只允许调整当前认证方式实例的展示信息、可用位置和模板参数。</span>
                  </div>
                </div>
                <div class="locked-box">
                  <div v-for="item in lockedTemplateItems[formModel.template] || []" :key="item.title" class="locked-item">
                    <strong>{{ item.title }}</strong>
                    <p>{{ item.desc }}</p>
                  </div>
                </div>
              </div>
            </section>

            <!-- Block 5: Referenced Policies (Only shown in Edit/Detail mode) -->
            <section v-if="formModel.id" class="panel-section" style="margin-top: 18px;">
              <h3>当前关联策略 ({{ references.length }})</h3>
              <p class="section-desc">当前认证方式被应用在以下策略中。</p>
              <el-table :data="references" class="premium-table" style="margin-top: 10px;">
                <el-table-column prop="name" label="策略名称" />
                <el-table-column prop="code" label="策略编码" width="120" />
                <el-table-column prop="role" label="策略角色" width="120" />
              </el-table>
            </section>

            <div class="form-footer-bar">
              <el-button @click="currentView === 'detail' ? goBackToList() : handleCancelForm()">{{ currentView === 'detail' ? '返回列表' : '取消' }}</el-button>
              <el-button v-if="currentView !== 'detail'" type="primary" :loading="saveLoading" @click="submitForm">保存认证方式</el-button>
              <el-button v-else type="primary" @click="enterEditMode">进入编辑</el-button>
            </div>
          </div>
        </div>
    </div>




    <!-- Delete Validation Dialog Modal -->
    <el-dialog
      v-model="isDeleteBlockOpen"
      title="无法删除认证方式"
      width="480px"
      align-center
      class="custom-conflict-dialog"
    >
      <div class="dialog-body-container">
        <p class="dialog-desc">
          <strong>“{{ referencedMethodForConfirm?.name }}”</strong> 已被认证策略引用，当前无法直接删除。请先在以下策略中解除对该认证方式的绑定，或替换为其它方式后再行尝试。
        </p>
        <div class="conflict-scope-box err">
          <strong>引用范围：</strong>
          <p>
            • 租户默认认证策略 (主登录 / MFA)<br/>
            • 平台强认证策略 (主登录方式)<br/>
            • 移动端快捷认证策略 (主登录 / MFA)
          </p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button @click="isDeleteBlockOpen = false">知道了</el-button>
          <el-button type="primary" @click="() => { isDeleteBlockOpen = false; router.push('/realm/auth-policies'); }">查看引用</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<style scoped>
.auth-method-module {
  padding: 0px;
}

/* Header standard styles */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title-wrap h1 {
  font-size: 24px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
.title-wrap p,
.form-layout .desc,
.drawer .desc {
  font-size: 14px;
  color: #64748B;
  margin: 6px 0 0 0;
}
.action-buttons {
  display: flex;
  gap: 12px;
}

.metrics-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.metric-cell {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 64px;
  padding: 12px 16px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.metric-cell span {
  color: #64748b;
  font-size: 12px;
  font-weight: 700;
}
.metric-cell strong {
  color: #0f172a;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 22px;
  line-height: 1;
}

/* Premium Navigation Tabs Bar */
.premium-tabs-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 4px;
  margin-bottom: 20px;
  width: fit-content;
}
.tab-item {
  border: 0;
  background: transparent;
  padding: 8px 16px;
  font-size: 13px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s ease;
}
.tab-item.active {
  background-color: #EFF6FF;
  color: #2563EB;
}
.tab-item:hover:not(.active) {
  color: #0F172A;
}
.tab-item:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
}

/* Filter Card */
.filter-card {
  border-radius: 10px;
  margin-bottom: 20px;
  border: 1px solid #E2E8F0;
}
.filter-flex-row {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  align-items: flex-end;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 200px;
  flex: 1;
}
.filter-item label,
.control-item label {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}
.filter-buttons {
  display: flex;
  gap: 10px;
}

/* Main Table Card */
.table-card {
  border-radius: 10px;
  border: 1px solid #E2E8F0;
}
.card-header-title {
  margin-bottom: 20px;
}
.card-header-title h3 {
  font-size: 16px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
.card-header-title p {
  font-size: 13px;
  color: #64748B;
  margin: 6px 0 0 0;
}

/* Link Actions */
.method-name-link {
  color: #0F172A;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.15s ease;
}
.method-name-link:hover {
  color: #2563EB;
}
.row-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}
.link-btn {
  color: #2563EB;
  font-weight: 650;
  font-size: 13px;
  cursor: pointer;
}
.link-btn:hover {
  color: #1D4ED8;
}
.link-more-btn {
  color: #64748B;
  font-weight: 650;
  font-size: 13px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.danger-item {
  color: #DC2626 !important;
}

/* Premium Table Header Background Style */
.premium-table :deep(th.el-table__cell) {
  background-color: #F8FAFC !important;
  color: #64748B;
  font-weight: 750;
  height: 44px;
  border-bottom: 1px solid #E5E7EB;
}
.premium-table :deep(td.el-table__cell) {
  height: 58px;
  border-bottom: 1px solid #E5E7EB;
  color: #334155;
}

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
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
.badge.blue {
  background-color: #EFF6FF;
  color: #2563EB;
}
.badge.gray {
  background-color: #F1F5F9;
  color: #64748B;
}

/* Footer elements */
.table-pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  font-size: 13px;
  color: #64748B;
}
.note-tip {
  font-weight: 700;
  color: #2563EB;
  background-color: #EFF6FF;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #BFDBFE;
}

/* Form layouts */
.form-layout.tall {
  background-color: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  padding: 24px;
}
.header-back-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}
.header-back-wrap h2 {
  font-size: 20px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
.back-icon {
  font-size: 18px;
  color: #64748B;
}
.hint-alert-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 12px 16px;
  color: #334155;
  font-size: 13px;
}
.info-icon {
  color: #2563EB;
  font-size: 16px;
}

/* Two-column layout grid */
.two-col-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.form-card-block {
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 24px;
}
.form-card-block h3 {
  font-size: 16px;
  font-weight: 800;
  color: #0F172A;
  margin: 0 0 6px 0;
}
.form-card-block p {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 20px 0;
}
.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.control-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.control-item.span-full {
  grid-column: span 2;
}
.req {
  color: #DC2626;
  margin-left: 2px;
}

/* Choice Grid Positions */
.choice-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}
.choice {
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  background-color: #FFFFFF;
  transition: all 0.2s ease;
  min-height: 86px;
}
.choice:hover:not(.disabled-choice) {
  border-color: #93c5fd;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
}
.choice:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
}
.choice.on {
  background-color: #EFF6FF;
  border-color: #BFDBFE;
}
.choice.off {
  background-color: #F8FAFC;
  opacity: 0.6;
}
.choice.disabled-choice {
  cursor: not-allowed;
  opacity: 0.8;
}
.choice b {
  display: block;
  font-size: 14px;
  color: #0F172A;
  margin-bottom: 6px;
}
.choice span {
  display: block;
  font-size: 12px;
  color: #64748B;
}

/* Dynamic parameter blocks */
.empty-params-card {
  grid-column: span 2;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background-color: #F8FAFC;
  border: 1px dashed #CBD5E1;
  border-radius: 8px;
  color: #64748B;
  font-size: 13px;
  gap: 12px;
}
.empty-params-card .info-icon {
  font-size: 24px;
  color: #94A3B8;
}
.warn-msg-block {
  background-color: #FFEDD5;
  border: 1px solid #FED7AA;
  color: #C2410C;
  font-size: 13px;
  padding: 10px 14px;
  border-radius: 6px;
  font-weight: 600;
  line-height: 1.5;
}

/* Detail View Layout */
.detail-layout {
  background-color: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  padding: 24px;
}
.detail-tabs-bar {
  display: flex;
  gap: 24px;
  border-bottom: 1px solid #E2E8F0;
  margin-bottom: 24px;
}
.detail-tab-item {
  padding: 12px 4px;
  font-size: 14px;
  font-weight: 700;
  color: #64748B;
  cursor: pointer;
  position: relative;
  transition: color 0.15s ease;
}
.detail-tab-item.active {
  color: #2563EB;
}
.detail-tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #2563EB;
}
.detail-split-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  align-items: start;
}
.detail-card-panel {
  border-radius: 8px;
  border: 1px solid #E2E8F0;
}
.detail-card-header h4 {
  font-size: 15px;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
}
.kv-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px 24px;
}
.kv-pair {
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 10px;
}
.kv-pair.span-full {
  grid-column: span 2;
}
.kv-pair .k {
  font-size: 12px;
  color: #64748B;
  font-weight: 700;
}
.kv-pair .v {
  font-size: 13px;
  color: #0F172A;
  font-weight: 600;
}
.rules-list-desc p {
  font-size: 13px;
  color: #475569;
  line-height: 1.8;
  margin: 0 0 12px 0;
}
.rules-list-desc p:last-child {
  margin: 0;
}

/* Edit summary configuration layout */
.edit-summary-layout {
  background-color: #FFFFFF;
  border-radius: 12px;
  border: 1px solid #E2E8F0;
  padding: 24px;
}
.warn-alert-box {
  background-color: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}
.warn-alert-box h4 {
  font-size: 15px;
  font-weight: 800;
  color: #991B1B;
  margin: 0 0 6px 0;
}
.warn-alert-box p {
  font-size: 13px;
  color: #B91C1C;
  margin: 0;
}
.summary-table-v3 {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}
.summary-table-v3 th {
  text-align: left;
  font-size: 12px;
  color: #64748B;
  font-weight: 700;
  padding: 12px;
  background-color: #F8FAFC;
}
.summary-table-v3 td {
  padding: 14px 12px;
  font-size: 13px;
  color: #334155;
  border-bottom: 1px solid #F1F5F9;
}

/* Drawer forms */
.form-item-mock {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}
.form-item-mock label {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}
.input-readonly {
  background-color: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  padding: 10px 12px;
  color: #64748B;
  font-size: 13px;
  font-weight: 600;
}
.note {
  background-color: #EFF6FF;
  border: 1px solid #BFDBFE;
  color: #2563EB;
  font-weight: 700;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
}

/* Conflict styling */
.dialog-desc {
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  margin: 0 0 16px 0;
}
.conflict-scope-box {
  border-radius: 8px;
  padding: 14px;
  font-size: 13px;
  line-height: 1.8;
}
.conflict-scope-box.err {
  background-color: #FEF2F2;
  border: 1px solid #FECACA;
  color: #991B1B;
}
.conflict-scope-box strong {
  display: block;
  margin-bottom: 6px;
}
.dialog-footer-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* Steps container layout (step-based scroll navigation) */
.layout {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 24px;
  align-items: start;
}
.steps {
  position: sticky;
  top: 24px;
  background-color: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 12px;
}
.step {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s ease;
}
.step:hover {
  background-color: #F8FAFC;
}
.step.active {
  background-color: #EFF6FF;
  color: #2563EB;
}
.step .num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #E2E8F0;
  font-size: 12px;
  font-weight: 750;
  flex-shrink: 0;
}
.step.active .num {
  background-color: #2563EB;
  color: #FFFFFF;
}
.step b {
  display: block;
  font-size: 13px;
}
.step small {
  display: block;
  font-size: 11px;
  color: #94A3B8;
}

.panel-section {
  background-color: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 24px;
}
.panel-section h3 {
  font-size: 16px;
  font-weight: 800;
  color: #0F172A;
  margin: 0 0 16px 0;
}
.panel-section .section-desc {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 20px 0;
}

/* Templates Grid selection */
.templates-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.tpl-card {
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  padding: 14px;
  cursor: pointer;
  background-color: #FFFFFF;
  transition: all 0.2s ease;
  min-height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  position: relative;
}
.tpl-card:hover:not(.disabled-tpl) {
  border-color: #93C5FD;
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.07);
  transform: translateY(-1px);
}
.tpl-card.active {
  border-color: #2563EB;
  background-color: #EFF6FF;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.08);
}
.tpl-card:focus-visible {
  outline: 2px solid #93c5fd;
  outline-offset: 2px;
}
.tpl-card.disabled-tpl {
  cursor: not-allowed;
  opacity: 0.8;
}
.tpl-card b {
  display: block;
  font-size: 14px;
  color: #0F172A;
  margin-bottom: 6px;
}
.tpl-card span {
  display: block;
  font-size: 12px;
  color: #64748B;
  line-height: 1.4;
}
.tpl-card small {
  color: #94a3b8;
  font-size: 12px;
  font-weight: 700;
  margin-top: 10px;
}
.tpl-card .lock-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  height: 20px;
  margin: 0;
  padding: 0 7px;
  font-size: 11px;
}
.input-help-text {
  font-size: 12px;
  color: #64748B;
  margin-top: 4px;
}

.locked-template-wrap {
  margin-bottom: 18px;
}
.locked-info {
  display: flex;
  gap: 10px;
  padding: 12px;
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 10px;
  color: #1e40af;
  margin-bottom: 12px;
}
.locked-info strong,
.locked-info span {
  display: block;
}
.locked-info span {
  margin-top: 2px;
  font-size: 13px;
  color: #1d4ed8;
}
.locked-box {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.locked-item {
  min-height: 86px;
  padding: 12px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
}
.locked-item strong {
  display: block;
  color: #0f172a;
  font-size: 13px;
  margin-bottom: 6px;
}
.locked-item p {
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
  margin: 0;
}

.template-param-grid {
  margin-top: 0;
}

.form-footer-bar {
  position: sticky;
  bottom: 0;
  z-index: 4;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin: 18px -24px -24px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0.94);
  border-top: 1px solid #e5e7eb;
  backdrop-filter: blur(8px);
}

.json-preview {
  max-height: 420px;
  overflow: auto;
  margin: 0;
  padding: 14px;
  color: #dbeafe;
  background: #0f172a;
  border-radius: 10px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
}

/* Dynamic Form Builder CSS */
.builder-wrap {
  display: grid;
  grid-template-columns: 1fr 1fr; /* Compressing configuration column and expanding preview column */
  gap: 20px;
  align-items: start;
}
.builder-panel {
  background: #fbfdff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px;
}
.field-card-body-grid {
  margin-top: 10px;
  border-top: 1px dashed #f1f5f9;
  padding-top: 10px;
}
.preview {
  position: sticky;
  top: 24px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.04);
}
.preview-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #edf2f7;
  padding-bottom: 8px;
  margin-bottom: 12px;
}
.fake-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.fake-row {
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 8px;
  padding: 12px 16px;
}
.fake-label {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #475569;
  margin-bottom: 6px;
  font-weight: 600;
}
.preview-type-badge {
  font-size: 11px;
  color: #94a3b8;
  font-family: monospace;
}
.fake-control {
  height: 36px;
  background: #f8fafc;
  border: 1px solid #dbe1ea;
  border-radius: 6px;
  display: flex;
  align-items: center;
  padding: 0 12px;
  color: #94a3b8;
  font-size: 13px;
}
.empty-preview {
  padding: 40px 10px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
  background: #f8fafc;
}
.preview-badge {
  background-color: #dcfce7;
  color: #15803d;
  font-size: 10px;
  height: 18px;
  padding: 0 6px;
  border-radius: 4px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

@media (max-width: 1180px) {
  .metrics-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .layout {
    grid-template-columns: 1fr;
  }
  .steps {
    position: static;
  }
  .templates-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .choice-grid,
  .locked-box {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
  .builder-wrap {
    grid-template-columns: 1fr;
  }
  .two-col-grid,
  .detail-split-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .view-header,
  .filter-flex-row {
    align-items: stretch;
    flex-direction: column;
  }
  .metrics-strip,
  .templates-grid,
  .choice-grid,
  .locked-box,
  .field-grid {
    grid-template-columns: 1fr;
  }
  .control-item.span-full {
    grid-column: span 1;
  }
  .form-footer-bar {
    flex-wrap: wrap;
  }
}
</style>
