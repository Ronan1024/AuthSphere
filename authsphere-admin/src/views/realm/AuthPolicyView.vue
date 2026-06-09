<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, Search, Refresh, ArrowDown, ArrowLeft, Warning,
  InfoFilled, View, Edit, CopyDocument, Delete, Check, Connection, TopRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authPolicyApi } from '@/api/authPolicy'
import { typeCategoryApi, type TypeCategoryRecord } from '@/api/typeCategory'

const router = useRouter()

interface AuthPolicyRecord {
  id: string
  name: string
  code: string
  applicableRealmId: string | null
  realmTypeName: string // resolved via category lookup
  status: number // 1: 启用, 2: 待配置, 3: 禁用
  authMethods: string[]
  defaultAuthMethod: string
  successAction: string
  // Captcha & Lock
  captchaType: string // '失败后启用' | '每次登录' | '未启用'
  captchaFailCount: number
  captchaValidity: number
  captchaRefreshLimit: number
  maxFailures: number
  statPeriod: number
  lockoutDuration: number
  recordRiskLog: string
  // MFA
  mfaEnabled: string // '开启' | '关闭'
  mfaTrigger: string // '新设备登录' | '每次登录' | '管理员账号' | '未启用'
  mfaMethods: string[]
  mfaDefaultMethod: string
  mfaValidity: number
  mfaFailCount: number
  mfaRememberDevice: string // '允许' | '不允许'
  mfaRememberDuration: number
  // Risk
  riskIpLimit: string // '开启' | '关闭'
  riskNewDeviceAlert: string // '开启' | '关闭'
  riskAbnormalTime: string // '仅记录日志' | '阻止登录'
  riskHighRiskAction: string // '触发 MFA' | '二次密码验证'
  // Refs
  realmRefs: string[]
  clientRefs: string[]
  description: string
}

// View state: 'list' | 'create' | 'edit' | 'copy' | 'detail' | 'edit-summary'
const currentView = ref<'list' | 'create' | 'edit' | 'copy' | 'detail' | 'edit-summary'>('list')

// Selected / Active items
const selectedPolicy = ref<AuthPolicyRecord | null>(null)
const policyToCopy = ref<AuthPolicyRecord | null>(null)

// Dialog overlay states
const isConfirmDisableOpen = ref(false)
const isBlockDeleteOpen = ref(false)
const referencedPolicyForConfirm = ref<AuthPolicyRecord | null>(null)

// Edit drawer state
const isEditDrawerOpen = ref(false)
const drawerForm = reactive({
  id: '',
  name: '',
  code: '',
  applicableRealmTypeName: '租户域',
  status: 1,
  authMethodsText: '密码 / 短信 / MFA',
  captchaType: '失败 3 次后启用',
  maxFailures: 5,
  lockoutDuration: 30,
  mfaTrigger: '新设备登录',
  mfaMethodsText: '短信验证码 / 邮箱验证码',
  mfaRememberDevice: '允许, 30 天'
})

const activeTab = ref('auth-policy')
const activeDetailTab = ref('basic')
const saveLoading = ref(false)

// Form models
const formModel = reactive({
  id: '',
  name: '',
  code: '',
  applicableRealmId: null as string | null,
  status: 1,
  authMethods: ['password', 'sms', 'mfa'] as string[],
  defaultAuthMethod: 'password',
  successAction: '返回来源客户端',
  captchaType: '失败后启用',
  captchaFailCount: 3,
  captchaValidity: 120,
  captchaRefreshLimit: 5,
  maxFailures: 5,
  statPeriod: 10,
  lockoutDuration: 30,
  recordRiskLog: '开启',
  mfaEnabled: '开启',
  mfaTrigger: '新设备登录',
  mfaMethods: ['sms', 'email'] as string[],
  mfaDefaultMethod: 'sms',
  mfaValidity: 300,
  mfaFailCount: 5,
  mfaRememberDevice: '允许',
  mfaRememberDuration: 30,
  riskIpLimit: '关闭',
  riskNewDeviceAlert: '开启',
  riskAbnormalTime: '仅记录日志',
  riskHighRiskAction: '触发 MFA',
  description: ''
})

// Copy Form model
const copyForm = reactive({
  sourceName: '',
  name: '',
  code: '',
  realmTypeName: '租户域',
  status: 3,
  copyItems: ['auth', 'captcha', 'lock', 'mfa'] as string[]
})

// Query filters
const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  authMethod: '',
  mfa: '',
  status: undefined as number | undefined
})

const policies = ref<AuthPolicyRecord[]>([])
const total = ref(0)
const categories = ref<TypeCategoryRecord[]>([])

const getCategoryName = (id: string | number | null | undefined) => {
  if (!id) return '不限定'
  const cat = categories.value.find(c => String(c.id) === String(id))
  return cat ? cat.name : '不限定'
}

const loadCategories = async () => {
  try {
    const list = await typeCategoryApi.list()
    categories.value = list || []
  } catch (err) {
    console.error('加载身份域类型失败:', err)
  }
}

const loadPolicies = async () => {
  try {
    const params: any = {
      page: query.page,
      size: query.size,
      name: query.name || undefined,
      code: query.code || undefined,
      authMethod: query.authMethod || undefined,
      status: query.status !== undefined ? query.status : undefined
    }
    const res = await authPolicyApi.page(params)
    policies.value = (res.records || []).map(record => {
      return {
        id: String(record.id),
        name: record.name,
        code: record.code,
        applicableRealmId: (record as any).applicableRealmId || null,
        realmTypeName: getCategoryName((record as any).applicableRealmId),
        status: record.status,
        authMethods: record.authMethods || [],
        defaultAuthMethod: (record as any).defaultAuthMethod || 'password',
        successAction: '返回来源客户端',
        captchaType: record.captchaEnabled ? '失败后启用' : '未启用',
        captchaFailCount: (record as any).captchaFailureThreshold || 3,
        captchaValidity: (record as any).captchaTtlSeconds || 120,
        captchaRefreshLimit: (record as any).captchaErrorLimit || 5,
        maxFailures: record.maxFailureCount || 5,
        statPeriod: (record as any).failureWindowMinutes || 10,
        lockoutDuration: record.lockMinutes || 30,
        recordRiskLog: (record as any).riskLogEnabled ? '开启' : '关闭',
        mfaEnabled: record.mfaEnabled ? '开启' : '关闭',
        mfaTrigger: record.mfaEnabled ? '新设备登录' : '未启用',
        mfaMethods: (record as any).mfaMethods || [],
        mfaDefaultMethod: (record as any).defaultAuthMethod || '',
        mfaValidity: 300,
        mfaFailCount: 5,
        mfaRememberDevice: '允许',
        mfaRememberDuration: 30,
        riskIpLimit: '关闭',
        riskNewDeviceAlert: '开启',
        riskAbnormalTime: '仅记录日志',
        riskHighRiskAction: '触发 MFA',
        realmRefs: [],
        clientRefs: [],
        description: (record as any).description || ''
      }
    })
    total.value = res.total || 0
  } catch (err) {
    ElMessage.error('加载认证策略失败')
  }
}

onMounted(async () => {
  await loadCategories()
  await loadPolicies()
})

const filteredPolicies = computed(() => {
  return policies.value
})

const getAuthText = (methods: string[]) => {
  const map: Record<string, string> = {
    password: '密码',
    sms: '短信',
    email: '邮箱',
    scan: '扫码',
    third_party: '第三方',
    mfa: 'MFA'
  }
  return (methods || []).map(m => map[m] || m).join(' / ')
}

const toggleMethod = (method: string) => {
  const index = formModel.authMethods.indexOf(method)
  if (index >= 0) {
    if (formModel.authMethods.length > 1) {
      formModel.authMethods.splice(index, 1)
    } else {
      ElMessage.warning('至少启用一种登录方式')
    }
  } else {
    formModel.authMethods.push(method)
  }
}

const toggleCopyItem = (item: string) => {
  const index = copyForm.copyItems.indexOf(item)
  if (index >= 0) {
    copyForm.copyItems.splice(index, 1)
  } else {
    copyForm.copyItems.push(item)
  }
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.authMethod = ''
  query.mfa = ''
  query.status = undefined
  query.page = 1
  loadPolicies()
}

const openCreateView = () => {
  Object.assign(formModel, {
    id: '',
    name: '',
    code: '',
    applicableRealmId: null,
    status: 1,
    authMethods: ['password', 'sms', 'mfa'],
    defaultAuthMethod: 'password',
    successAction: '返回来源客户端',
    captchaType: '失败后启用',
    captchaFailCount: 3,
    captchaValidity: 120,
    captchaRefreshLimit: 5,
    maxFailures: 5,
    statPeriod: 10,
    lockoutDuration: 30,
    recordRiskLog: '开启',
    mfaEnabled: '开启',
    mfaTrigger: '新设备登录',
    mfaMethods: ['sms', 'email'],
    mfaDefaultMethod: 'sms',
    mfaValidity: 300,
    mfaFailCount: 5,
    mfaRememberDevice: '允许',
    mfaRememberDuration: 30,
    riskIpLimit: '关闭',
    riskNewDeviceAlert: '开启',
    riskAbnormalTime: '仅记录日志',
    riskHighRiskAction: '触发 MFA',
    description: ''
  })
  currentView.value = 'create'
}

const openEditDrawer = async (row: any) => {
  try {
    const detail = await authPolicyApi.detail(row.id)
    Object.assign(drawerForm, {
      id: String(detail.id),
      name: detail.name,
      code: detail.code,
      applicableRealmTypeName: getCategoryName(detail.applicableRealmId),
      status: detail.status,
      authMethodsText: getAuthText(detail.authMethods || []),
      captchaType: detail.captchaEnabled ? `失败 ${detail.captchaFailureThreshold} 次后启用` : '未启用',
      maxFailures: detail.maxFailureCount || 5,
      lockoutDuration: detail.lockMinutes || 30,
      mfaTrigger: detail.mfaTriggers && detail.mfaTriggers.length > 0 ? detail.mfaTriggers[0] : '未启用',
      mfaMethodsText: (detail.mfaMethods || []).map(m => m === 'sms' ? '短信验证码' : '邮箱验证码').join(' / ') || '未启用',
      mfaRememberDevice: detail.rememberDeviceEnabled ? `允许, ${detail.rememberDeviceDays} 天` : '不允许'
    })
    currentView.value = 'edit-summary'
    isEditDrawerOpen.value = true
  } catch (err) {
    ElMessage.error('加载策略配置摘要失败')
  }
}

watch(isEditDrawerOpen, (newVal) => {
  if (!newVal && currentView.value === 'edit-summary') {
    currentView.value = 'list'
  }
})

const openCopyView = (row: AuthPolicyRecord) => {
  policyToCopy.value = row
  Object.assign(copyForm, {
    sourceName: row.name,
    name: row.name + '副本',
    code: row.code + '_copy',
    realmTypeName: row.realmTypeName,
    status: 3,
    copyItems: ['auth', 'captcha', 'lock', 'mfa']
  })
  currentView.value = 'copy'
}

const openDetail = async (row: AuthPolicyRecord) => {
  try {
    const detail = await authPolicyApi.detail(row.id)
    selectedPolicy.value = {
      id: String(detail.id),
      name: detail.name,
      code: detail.code,
      applicableRealmId: detail.applicableRealmId || null,
      realmTypeName: getCategoryName(detail.applicableRealmId),
      status: detail.status,
      authMethods: detail.authMethods || [],
      defaultAuthMethod: detail.defaultAuthMethod || 'password',
      successAction: '返回来源客户端',
      captchaType: detail.captchaEnabled ? `失败 ${detail.captchaFailureThreshold} 次后启用` : '未启用',
      captchaFailCount: detail.captchaFailureThreshold || 3,
      captchaValidity: detail.captchaTtlSeconds || 120,
      captchaRefreshLimit: detail.captchaErrorLimit || 5,
      maxFailures: detail.maxFailureCount || 5,
      statPeriod: detail.failureWindowMinutes || 10,
      lockoutDuration: detail.lockMinutes || 30,
      recordRiskLog: detail.riskLogEnabled ? '开启' : '关闭',
      mfaEnabled: detail.mfaEnabled ? '开启' : '关闭',
      mfaTrigger: detail.mfaTriggers && detail.mfaTriggers.length > 0 ? detail.mfaTriggers[0] : '未启用',
      mfaMethods: detail.mfaMethods || [],
      mfaDefaultMethod: detail.defaultAuthMethod || '',
      mfaValidity: 300,
      mfaFailCount: 5,
      mfaRememberDevice: detail.rememberDeviceEnabled ? '允许' : '不允许',
      mfaRememberDuration: detail.rememberDeviceDays || 30,
      riskIpLimit: detail.ipRestrictionEnabled ? '开启' : '关闭',
      riskNewDeviceAlert: detail.deviceCheckEnabled ? '开启' : '关闭',
      riskAbnormalTime: detail.abnormalTimeCheckEnabled ? '阻止登录' : '仅记录日志',
      riskHighRiskAction: '触发 MFA',
      realmRefs: (detail.references || []).filter(r => r.referenceType === 'REALM').map(r => r.name),
      clientRefs: (detail.references || []).filter(r => r.referenceType === 'CLIENT').map(r => r.name),
      description: detail.description || ''
    }
    activeDetailTab.value = 'basic'
    currentView.value = 'detail'
  } catch (err) {
    ElMessage.error('获取策略详情失败')
  }
}

const handleDisableOrDelete = (row: AuthPolicyRecord, action: 'disable' | 'delete') => {
  referencedPolicyForConfirm.value = row
  if (row.realmRefs.length > 0 || row.clientRefs.length > 0) {
    if (action === 'disable') {
      isConfirmDisableOpen.value = true
    } else {
      isBlockDeleteOpen.value = true
    }
  } else {
    const actionName = action === 'disable' ? '禁用' : '删除'
    ElMessageBox.confirm(`确认${actionName}该认证策略？`, '提示', { type: 'warning' }).then(async () => {
      try {
        if (action === 'disable') {
          await authPolicyApi.disable(row.id)
          ElMessage.success('认证策略已成功禁用')
        } else {
          await authPolicyApi.delete(row.id)
          ElMessage.success('认证策略已成功删除')
        }
        await loadPolicies()
      } catch (err) {
        ElMessage.error(`${actionName}失败`)
      }
    }).catch(() => {})
  }
}

const submitForm = async () => {
  if (!formModel.name || !formModel.code) {
    ElMessage.error('请填写必填项')
    return
  }
  saveLoading.value = true
  try {
    const payload: any = {
      code: formModel.code,
      name: formModel.name,
      applicableRealmId: formModel.applicableRealmId ? String(formModel.applicableRealmId) : null,
      authMethods: formModel.authMethods,
      defaultAuthMethod: formModel.defaultAuthMethod,
      captchaEnabled: formModel.captchaType !== '未启用',
      captchaFailureThreshold: formModel.captchaFailCount || 0,
      captchaTtlSeconds: formModel.captchaValidity || 60,
      captchaErrorLimit: formModel.captchaRefreshLimit || 1,
      maxFailureCount: formModel.maxFailures || 5,
      failureWindowMinutes: formModel.statPeriod || 10,
      lockMinutes: formModel.lockoutDuration || 30,
      notifyUser: formModel.recordRiskLog === '开启',
      riskLogEnabled: formModel.recordRiskLog === '开启',
      mfaEnabled: formModel.mfaEnabled === '开启',
      mfaTriggers: formModel.mfaEnabled === '开启' ? [formModel.mfaTrigger] : [],
      mfaMethods: formModel.mfaMethods,
      rememberDeviceEnabled: formModel.mfaRememberDevice === '允许',
      rememberDeviceDays: formModel.mfaRememberDuration || 0,
      ipRestrictionEnabled: formModel.riskIpLimit === '开启',
      deviceCheckEnabled: formModel.riskNewDeviceAlert === '开启',
      remoteLoginCheckEnabled: formModel.riskNewDeviceAlert === '开启',
      abnormalTimeCheckEnabled: formModel.riskAbnormalTime === '阻止登录' || formModel.riskAbnormalTime === '仅记录日志',
      status: formModel.status,
      description: formModel.description
    }
    
    if (formModel.id) {
      await authPolicyApi.update(formModel.id, payload)
      ElMessage.success('更新认证策略成功')
    } else {
      await authPolicyApi.create(payload)
      ElMessage.success('新增认证策略成功')
    }
    currentView.value = 'list'
    await loadPolicies()
  } catch (err) {
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const saveDrawerEdit = async () => {
  if (!drawerForm.name) {
    ElMessage.error('请输入策略名称')
    return
  }
  try {
    const detail = await authPolicyApi.detail(drawerForm.id)
    const payload: any = {
      code: detail.code,
      name: drawerForm.name,
      applicableRealmId: detail.applicableRealmId ? String(detail.applicableRealmId) : null,
      authMethods: detail.authMethods || [],
      defaultAuthMethod: detail.defaultAuthMethod || 'password',
      captchaEnabled: detail.captchaEnabled,
      captchaFailureThreshold: detail.captchaFailureThreshold,
      captchaTtlSeconds: detail.captchaTtlSeconds,
      captchaErrorLimit: detail.captchaErrorLimit,
      maxFailureCount: drawerForm.maxFailures,
      failureWindowMinutes: detail.failureWindowMinutes,
      lockMinutes: drawerForm.lockoutDuration,
      notifyUser: detail.notifyUser,
      riskLogEnabled: detail.riskLogEnabled,
      mfaEnabled: detail.mfaEnabled,
      mfaTriggers: detail.mfaTriggers,
      mfaMethods: detail.mfaMethods,
      rememberDeviceEnabled: detail.rememberDeviceEnabled,
      rememberDeviceDays: detail.rememberDeviceDays,
      ipRestrictionEnabled: detail.ipRestrictionEnabled,
      deviceCheckEnabled: detail.deviceCheckEnabled,
      remoteLoginCheckEnabled: detail.remoteLoginCheckEnabled,
      abnormalTimeCheckEnabled: detail.abnormalTimeCheckEnabled,
      status: drawerForm.status,
      description: detail.description
    }
    await authPolicyApi.update(drawerForm.id, payload)
    ElMessage.success('保存修改成功')
    isEditDrawerOpen.value = false
    await loadPolicies()
  } catch (err) {
    ElMessage.error('保存修改失败')
  }
}

const submitCopyForm = async () => {
  if (!copyForm.name || !copyForm.code) {
    ElMessage.error('请填写副本信息')
    return
  }
  saveLoading.value = true
  try {
    if (policyToCopy.value) {
      await authPolicyApi.copy(policyToCopy.value.id, {
        code: copyForm.code,
        name: copyForm.name
      })
      ElMessage.success('副本复制成功，默认状态为禁用')
      currentView.value = 'list'
      await loadPolicies()
    }
  } catch (err) {
    ElMessage.error('复制失败')
  } finally {
    saveLoading.value = false
  }
}

const handleTabClick = (tabName: string) => {
  if (tabName === 'login-page') {
    router.push('/realm/login-pages')
  } else {
    ElMessage.info('该子项可在安全中心独立查看')
  }
}

const getCaptchaBadgeClass = (type: string) => {
  if (type === '每次登录') return 'green'
  if (type === '失败后启用') return 'blue'
  return 'gray'
}

const getMfaBadgeClass = (trigger: string) => {
  if (trigger === '每次登录') return 'orange'
  if (trigger === '未启用') return 'gray'
  return 'blue'
}
</script>

<template>
  <div class="auth-policy-module">
    <!-- View 1: List View -->
    <div v-if="currentView === 'list'" class="list-layout">
      <div class="view-header">
        <div class="title-wrap">
          <h1>认证策略</h1>
          <p>定义登录方式、验证码、失败锁定与 MFA 规则；MFA 作为认证策略内的配置分区维护。</p>
        </div>
        <div class="action-buttons">
          <el-button @click="handleReset">刷新</el-button>
          <el-button type="primary" :icon="Plus" class="btn-create-policy" @click="openCreateView">新增认证策略</el-button>
        </div>
      </div>


      <!-- Search Filter -->
      <el-card shadow="never" class="filter-card">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>策略名称</label>
            <el-input v-model="query.name" placeholder="请输入策略名称" clearable />
          </div>
          <div class="filter-item">
            <label>策略编码</label>
            <el-input v-model="query.code" placeholder="请输入策略编码" clearable />
          </div>
          <div class="filter-item">
            <label>登录方式</label>
            <el-select v-model="query.authMethod" placeholder="全部" clearable>
              <el-option label="密码" value="password" />
              <el-option label="短信" value="sms" />
              <el-option label="邮箱" value="email" />
              <el-option label="扫码" value="scan" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>MFA</label>
            <el-select v-model="query.mfa" placeholder="全部" clearable>
              <el-option label="已启用" value="enabled" />
              <el-option label="未启用" value="disabled" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="query.status" placeholder="全部状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="待配置" :value="2" />
              <el-option label="禁用" :value="3" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="loadPolicies">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- Main Table -->
      <el-card shadow="never" class="table-card">
        <div class="card-header-title">
          <h3>认证策略列表</h3>
          <p>列表只展示识别、登录方式、验证码、MFA、引用和状态。</p>
        </div>

        <el-table :data="filteredPolicies" class="premium-table">
          <el-table-column prop="name" label="策略名称" min-width="180">
            <template #default="{ row }">
              <span class="policy-name-link" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="策略编码" min-width="150" />
          <el-table-column label="登录方式" min-width="160">
            <template #default="{ row }">
              {{ getAuthText(row.authMethods) }}
            </template>
          </el-table-column>
          <el-table-column label="验证码" min-width="120">
            <template #default="{ row }">
              <span class="badge" :class="getCaptchaBadgeClass(row.captchaType)">
                {{ row.captchaType }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="失败锁定" min-width="130">
            <template #default="{ row }">
              {{ row.maxFailures }} 次 / {{ row.lockoutDuration }} 分钟
            </template>
          </el-table-column>
          <el-table-column label="MFA 配置" min-width="130">
            <template #default="{ row }">
              <span class="badge" :class="getMfaBadgeClass(row.mfaTrigger)">
                {{ row.mfaTrigger }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="引用" min-width="110">
            <template #default="{ row }">
              {{ row.realmRefs.length > 0 ? `${row.realmRefs.length} 个身份域` : '未引用' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <span class="badge" :class="row.status === 1 ? 'green' : (row.status === 2 ? 'orange' : 'red')">
                {{ row.status === 1 ? '启用' : (row.status === 2 ? '待配置' : '禁用') }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <span class="link-btn" @click="openEditDrawer(row)">编辑</span>
                <span class="link-btn" @click="openCopyView(row)">复制</span>
                <el-dropdown trigger="click">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'disable')" :disabled="row.status === 3">禁用策略</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'delete')" class="danger-item">删除策略</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- Table Footer Pagination -->
        <div class="table-pagination-footer">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="sizes, prev, pager, next, jumper"
            @change="loadPolicies"
          />
        </div>
      </el-card>
    </div>

    <!-- View 2: Create / Single-Page Form View -->
    <div v-else-if="currentView === 'create' || currentView === 'edit'" class="form-layout tall">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>{{ currentView === 'edit' ? '编辑认证策略' : '新增认证策略' }}</h2>
        </div>
        <div class="action-buttons">
          <el-button @click="currentView = 'list'">取消</el-button>
          <el-button>保存为草稿</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitForm">保存</el-button>
        </div>
      </div>

      <div class="form-body-tall-wrapper">
        <div class="hint-alert-box">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
          <span>当前页面合并展示：基础信息、登录方式、验证码与失败锁定、MFA 配置。MFA 不单独拆菜单。</span>
        </div>

        <div class="two-col-grid" style="margin-top: 18px;">
          <!-- Left Column -->
          <div class="form-left-col">
            <!-- Block 1: Basic -->
            <div class="form-card-block">
              <h3>基础信息</h3>
              <p>策略编码保存后不建议修改；适用身份域类型用于筛选和推荐，不表示真实引用。</p>
              <div class="field-grid">
                <div class="control-item">
                  <label>策略名称 <span class="req">*</span></label>
                  <el-input v-model="formModel.name" placeholder="请输入策略名称" />
                </div>
                <div class="control-item">
                  <label>策略编码 <span class="req">*</span></label>
                  <el-input v-model="formModel.code" :disabled="formModel.id !== ''" placeholder="请输入策略编码" />
                </div>
                <div class="control-item">
                  <label>适用身份域类型</label>
                  <el-select v-model="formModel.applicableRealmId" placeholder="选择类型" clearable>
                    <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>状态</label>
                  <el-select v-model="formModel.status" :disabled="true">
                    <el-option label="启用" :value="1" />
                    <el-option label="禁用" :value="3" />
                  </el-select>
                </div>
                <div class="control-item span-full">
                  <label>备注</label>
                  <el-input v-model="formModel.description" type="textarea" :rows="2" placeholder="配置备注信息" />
                </div>
              </div>
            </div>

            <!-- Block 2: Auth Methods -->
            <div class="form-card-block" style="margin-top: 18px;">
              <h3>登录方式</h3>
              <p>至少启用一种登录方式，默认登录方式必须在已启用方式中。</p>
              <div class="check-grid">
                <div class="check-box" :class="formModel.authMethods.includes('password') ? 'on' : ''" @click="toggleMethod('password')">
                  <span class="custom-dot"></span>密码登录
                </div>
                <div class="check-box" :class="formModel.authMethods.includes('sms') ? 'on' : ''" @click="toggleMethod('sms')">
                  <span class="custom-dot"></span>短信登录
                </div>
                <div class="check-box" :class="formModel.authMethods.includes('email') ? 'on' : ''" @click="toggleMethod('email')">
                  <span class="custom-dot"></span>邮箱登录
                </div>
                <div class="check-box" :class="formModel.authMethods.includes('scan') ? 'on' : ''" @click="toggleMethod('scan')">
                  <span class="custom-dot"></span>扫码登录
                </div>
                <div class="check-box" :class="formModel.authMethods.includes('third_party') ? 'on' : ''" @click="toggleMethod('third_party')">
                  <span class="custom-dot"></span>第三方登录
                </div>
                <div class="check-box" :class="formModel.authMethods.includes('mfa') ? 'on' : ''" @click="toggleMethod('mfa')">
                  <span class="custom-dot"></span>MFA 二次认证
                </div>
              </div>

              <div class="field-grid" style="margin-top: 18px;">
                <div class="control-item">
                  <label>默认登录方式</label>
                  <el-select v-model="formModel.defaultAuthMethod">
                    <el-option label="密码登录" value="password" />
                    <el-option label="短信登录" value="sms" />
                    <el-option label="邮箱登录" value="email" />
                    <el-option label="扫码登录" value="scan" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>登录成功动作</label>
                  <el-input v-model="formModel.successAction" placeholder="默认返回来源客户端" />
                </div>
              </div>
            </div>

            <!-- Block 3: Captcha & Lockout -->
            <div class="form-card-block" style="margin-top: 18px;">
              <h3>验证码与失败锁定</h3>
              <p>验证码、失败次数、锁定时长在同一分区处理，减少用户跳转。</p>
              <div class="field-grid">
                <div class="control-item">
                  <label>启用图形验证码</label>
                  <el-select v-model="formModel.captchaType">
                    <el-option label="失败后启用" value="失败后启用" />
                    <el-option label="每次登录" value="每次登录" />
                    <el-option label="未启用" value="未启用" />
                  </el-select>
                </div>
                <div class="control-item" v-if="formModel.captchaType === '失败后启用'">
                  <label>失败几次后启用</label>
                  <el-input v-model.number="formModel.captchaFailCount"><template #append>次</template></el-input>
                </div>
                <div class="control-item" v-if="formModel.captchaType !== '未启用'">
                  <label>验证码有效期</label>
                  <el-input v-model.number="formModel.captchaValidity"><template #append>秒</template></el-input>
                </div>
                <div class="control-item" v-if="formModel.captchaType !== '未启用'">
                  <label>验证码错误限制</label>
                  <el-input v-model.number="formModel.captchaRefreshLimit"><template #append>次后刷新</template></el-input>
                </div>
                <div class="control-item">
                  <label>最大失败次数</label>
                  <el-input v-model.number="formModel.maxFailures"><template #append>次</template></el-input>
                </div>
                <div class="control-item">
                  <label>失败统计周期</label>
                  <el-input v-model.number="formModel.statPeriod"><template #append>分钟</template></el-input>
                </div>
                <div class="control-item">
                  <label>锁定时长</label>
                  <el-input v-model.number="formModel.lockoutDuration"><template #append>分钟</template></el-input>
                </div>
                <div class="control-item">
                  <label>记录风险日志</label>
                  <el-select v-model="formModel.recordRiskLog">
                    <el-option label="开启" value="开启" />
                    <el-option label="关闭" value="关闭" />
                  </el-select>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column -->
          <div class="form-right-col">
            <!-- Block 4: MFA -->
            <div class="form-card-block">
              <h3>MFA 配置</h3>
              <p>MFA 作为认证策略内配置项，不单独做 MFA 管理页面。</p>
              <div class="field-grid">
                <div class="control-item">
                  <label>是否启用 MFA</label>
                  <el-select v-model="formModel.mfaEnabled">
                    <el-option label="开启" value="开启" />
                    <el-option label="关闭" value="关闭" />
                  </el-select>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启'">
                  <label>触发条件</label>
                  <el-select v-model="formModel.mfaTrigger">
                    <el-option label="新设备登录" value="新设备登录" />
                    <el-option label="每次登录" value="每次登录" />
                    <el-option label="管理员账号" value="管理员账号" />
                  </el-select>
                </div>
                <div class="control-item span-full" v-if="formModel.mfaEnabled === '开启'">
                  <label>验证方式</label>
                  <el-checkbox-group v-model="formModel.mfaMethods">
                    <el-checkbox label="sms">短信验证码</el-checkbox>
                    <el-checkbox label="email">邮箱验证码</el-checkbox>
                  </el-checkbox-group>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启'">
                  <label>默认验证方式</label>
                  <el-select v-model="formModel.mfaDefaultMethod">
                    <el-option label="短信验证码" value="sms" />
                    <el-option label="邮箱验证码" value="email" />
                  </el-select>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启'">
                  <label>验证码有效期</label>
                  <el-input v-model.number="formModel.mfaValidity"><template #append>秒</template></el-input>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启'">
                  <label>验证失败次数</label>
                  <el-input v-model.number="formModel.mfaFailCount"><template #append>次</template></el-input>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启'">
                  <label>允许记住设备</label>
                  <el-select v-model="formModel.mfaRememberDevice">
                    <el-option label="允许" value="允许" />
                    <el-option label="不允许" value="不允许" />
                  </el-select>
                </div>
                <div class="control-item" v-if="formModel.mfaEnabled === '开启' && formModel.mfaRememberDevice === '允许'">
                  <label>记住设备有效期</label>
                  <el-input v-model.number="formModel.mfaRememberDuration"><template #append>天</template></el-input>
                </div>
              </div>
              <div class="warn-msg-block" style="margin-top: 12px;" v-if="formModel.mfaEnabled === '开启'">
                如果启用 MFA，至少选择一种验证方式；默认验证方式必须包含在已选方式中。
              </div>
            </div>

            <!-- Block 5: Risk Rules -->
            <div class="form-card-block" style="margin-top: 18px;">
              <h3>风险校验预留</h3>
              <p>第一版只保留轻量配置，复杂设备指纹、异地识别可后续扩展。</p>
              <div class="field-grid">
                <div class="control-item">
                  <label>限制登录 IP</label>
                  <el-select v-model="formModel.riskIpLimit">
                    <el-option label="开启" value="开启" />
                    <el-option label="关闭" value="关闭" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>新设备提醒</label>
                  <el-select v-model="formModel.riskNewDeviceAlert">
                    <el-option label="开启" value="开启" />
                    <el-option label="关闭" value="关闭" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>异常时间登录</label>
                  <el-select v-model="formModel.riskAbnormalTime">
                    <el-option label="仅记录日志" value="仅记录日志" />
                    <el-option label="阻止登录" value="阻止登录" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>高风险动作</label>
                  <el-select v-model="formModel.riskHighRiskAction">
                    <el-option label="触发 MFA" value="触发 MFA" />
                    <el-option label="二次密码验证" value="二次密码验证" />
                  </el-select>
                </div>
              </div>
            </div>

            <!-- Block 6: Preview Chain -->
            <div class="form-card-block" style="margin-top: 18px;">
              <h3>认证链路预览</h3>
              <p>保存后该策略在登录流程中按以下顺序执行。</p>
              <div class="preview-flow-row">
                <div class="flow-step">账号校验</div>
                <div class="flow-arrow">→</div>
                <div class="flow-step">密码/短信</div>
                <div class="flow-arrow">→</div>
                <div class="flow-step">验证码</div>
              </div>
              <div class="preview-flow-row" style="margin-top: 8px;">
                <div class="flow-step">失败锁定</div>
                <div class="flow-arrow">→</div>
                <div class="flow-step">MFA</div>
                <div class="flow-arrow">→</div>
                <div class="flow-step">生成 Token</div>
              </div>
            </div>

            <!-- Error hints -->
            <div class="error-msg-block" style="margin-top: 18px;">
              保存校验：策略编码唯一；至少启用一种登录方式；MFA 默认方式有效；锁定时长不能小于 0。
            </div>
          </div>
        </div>
      </div>
      <div class="footer-bar">
        <el-button @click="currentView = 'list'">取消</el-button>
        <el-button>保存为草稿</el-button>
        <el-button type="primary" :loading="saveLoading" @click="submitForm">保存</el-button>
      </div>
    </div>

    <!-- View 3: Detail View -->
    <div v-else-if="currentView === 'detail' && selectedPolicy" class="detail-layout">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>{{ selectedPolicy.name }}</h2>
        </div>
        <div class="action-buttons">
          <el-button @click="currentView = 'list'">返回</el-button>
          <el-button type="primary" @click="openEditDrawer(selectedPolicy)">编辑</el-button>
          <el-button type="danger" @click="handleDisableOrDelete(selectedPolicy, 'disable')">禁用</el-button>
        </div>
      </div>

      <!-- Detail Navigation Tabs -->
      <div class="detail-tabs-bar">
        <span class="detail-tab-item" :class="activeDetailTab === 'basic' ? 'active' : ''" @click="activeDetailTab = 'basic'">基础信息</span>
        <span class="detail-tab-item" :class="activeDetailTab === 'methods' ? 'active' : ''" @click="activeDetailTab = 'methods'">登录方式</span>
        <span class="detail-tab-item" :class="activeDetailTab === 'captcha' ? 'active' : ''" @click="activeDetailTab = 'captcha'">验证码与锁定</span>
        <span class="detail-tab-item" :class="activeDetailTab === 'mfa' ? 'active' : ''" @click="activeDetailTab = 'mfa'">MFA 配置</span>
        <span class="detail-tab-item" :class="activeDetailTab === 'refs' ? 'active' : ''" @click="activeDetailTab = 'refs'">引用对象</span>
        <span class="detail-tab-item" :class="activeDetailTab === 'logs' ? 'active' : ''" @click="activeDetailTab = 'logs'">审计日志</span>
      </div>

      <div class="detail-split-grid">
        <!-- Left Column -->
        <div class="detail-main-col">
          <!-- Card 1: Basic Info -->
          <el-card shadow="never" class="detail-card-panel">
            <template #header>
              <div class="detail-card-header">
                <h4>基础信息</h4>
                <p>认证策略负责登录过程中的身份验证，不直接决定菜单或接口权限。</p>
              </div>
            </template>
            <div class="kv-grid">
              <div class="kv-pair"><span class="k">策略名称</span><span class="v">{{ selectedPolicy.name }}</span></div>
              <div class="kv-pair"><span class="k">策略编码</span><span class="v">{{ selectedPolicy.code }}</span></div>
              <div class="kv-pair"><span class="k">适用身份域类型</span><span class="v">{{ selectedPolicy.realmTypeName }}</span></div>
              <div class="kv-pair">
                <span class="k">状态</span>
                <span class="v">
                  <span class="badge green" v-if="selectedPolicy.status === 1">启用</span>
                  <span class="badge orange" v-else-if="selectedPolicy.status === 2">待配置</span>
                  <span class="badge red" v-else>禁用</span>
                </span>
              </div>
              <div class="kv-pair span-full"><span class="k">备注</span><span class="v">{{ selectedPolicy.description || '无' }}</span></div>
            </div>
          </el-card>

          <!-- Card 2: Flow -->
          <el-card shadow="never" class="detail-card-panel" style="margin-top: 20px;">
            <template #header>
              <div class="detail-card-header">
                <h4>认证链路</h4>
              </div>
            </template>
            <div class="detail-flow-row">
              <div class="flow-step">账号校验</div>
              <div class="flow-arrow">→</div>
              <div class="flow-step">密码校验</div>
              <div class="flow-arrow">→</div>
              <div class="flow-step">图形验证码</div>
            </div>
            <div class="detail-flow-row" style="margin-top: 8px;">
              <div class="flow-step">失败锁定</div>
              <div class="flow-arrow">→</div>
              <div class="flow-step">MFA 新设备</div>
              <div class="flow-arrow">→</div>
              <div class="flow-step">生成 Token</div>
            </div>
          </el-card>

          <!-- Card 3: Captcha and lockout -->
          <el-card shadow="never" class="detail-card-panel" style="margin-top: 20px;">
            <template #header>
              <div class="detail-card-header">
                <h4>验证码与锁定</h4>
              </div>
            </template>
            <div class="kv-grid">
              <div class="kv-pair"><span class="k">图形验证码</span><span class="v">{{ selectedPolicy.captchaType }}</span></div>
              <div class="kv-pair"><span class="k">验证码有效期</span><span class="v">{{ selectedPolicy.captchaValidity }} 秒</span></div>
              <div class="kv-pair"><span class="k">最大失败次数</span><span class="v">{{ selectedPolicy.maxFailures }} 次</span></div>
              <div class="kv-pair"><span class="k">锁定时长</span><span class="v">{{ selectedPolicy.lockoutDuration }} 分钟</span></div>
            </div>
          </el-card>
        </div>

        <!-- Right Column (Aside) -->
        <div class="detail-aside-col">
          <!-- Card 4: MFA -->
          <el-card shadow="never" class="detail-card-panel">
            <template #header>
              <div class="detail-card-header">
                <h4>MFA 配置</h4>
              </div>
            </template>
            <div class="kv-grid vertical">
              <div class="kv-pair-vertical">
                <span class="k">启用状态</span>
                <span class="v"><span class="badge blue">{{ selectedPolicy.mfaEnabled }}</span></span>
              </div>
              <div class="kv-pair-vertical"><span class="k">触发条件</span><span class="v">{{ selectedPolicy.mfaTrigger }}</span></div>
              <div class="kv-pair-vertical"><span class="k">验证方式</span><span class="v">{{ selectedPolicy.mfaMethods.join(' / ') || '无' }}</span></div>
              <div class="kv-pair-vertical"><span class="k">默认方式</span><span class="v">{{ selectedPolicy.mfaDefaultMethod || '无' }}</span></div>
              <div class="kv-pair-vertical"><span class="k">记住设备</span><span class="v">{{ selectedPolicy.mfaRememberDevice }}，{{ selectedPolicy.mfaRememberDuration }} 天</span></div>
            </div>
          </el-card>

          <!-- Card 5: Refs -->
          <el-card shadow="never" class="detail-card-panel" style="margin-top: 20px;">
            <template #header>
              <div class="detail-card-header">
                <h4>引用对象</h4>
                <p>已引用策略编辑时需要提示影响范围。</p>
              </div>
            </template>
            <div class="detail-ref-list">
              <div v-for="realm in selectedPolicy.realmRefs" :key="realm" class="detail-ref-item">
                <strong>{{ realm }}</strong><span>默认策略</span>
              </div>
              <div v-for="client in selectedPolicy.clientRefs" :key="client" class="detail-ref-item">
                <strong>{{ client }}</strong><span>客户端覆盖</span>
              </div>
              <div v-if="!selectedPolicy.realmRefs.length && !selectedPolicy.clientRefs.length" class="empty-refs">
                暂无引用对象
              </div>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- View 4: Edit Layout with right Drawer summary -->
    <div v-else-if="currentView === 'edit-summary'" class="edit-summary-layout">
      <!-- Edit Summary Table Left Side -->
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>编辑认证策略</h2>
        </div>
        <p class="desc">编辑仍在单页表单内完成；已引用时提示影响范围。</p>
      </div>

      <div class="warn-alert-box">
        <h4>租户默认认证策略</h4>
        <p>策略已被 3 个对象引用。修改登录方式、验证码、MFA 后会影响相关登录流程。</p>
        <div class="warn-msg-block">影响范围：租户身份域、商户身份域、租户A管理端客户端。</div>
      </div>

      <el-card shadow="never" class="table-card" style="margin-top: 20px;">
        <div class="section-head">
          <h2 class="card-title">当前配置摘要</h2>
          <p class="card-desc">主页面展示摘要，右侧抽屉承载编辑内容。</p>
        </div>
        <table class="summary-table-v3">
          <thead>
            <tr>
              <th>配置分区</th>
              <th>当前值</th>
              <th>说明</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><strong>基础信息</strong></td>
              <td>tenant_auth / 租户域 / 启用</td>
              <td>编码只读</td>
            </tr>
            <tr>
              <td><strong>登录方式</strong></td>
              <td>密码 / 短信 / MFA</td>
              <td>默认密码登录</td>
            </tr>
            <tr>
              <td><strong>验证码与锁定</strong></td>
              <td>失败 3 次启用验证码，5 次锁定 30 分钟</td>
              <td>登录安全规则</td>
            </tr>
            <tr>
              <td><strong>MFA 配置</strong></td>
              <td>新设备触发，短信或邮箱验证，记住设备 30 天</td>
              <td>认证策略内配置</td>
            </tr>
            <tr>
              <td><strong>风险校验</strong></td>
              <td>新设备提醒，高风险动作触发 MFA</td>
              <td>预留能力</td>
            </tr>
          </tbody>
        </table>
      </el-card>
    </div>

    <!-- View 5: Copy Page -->
    <div v-else-if="currentView === 'copy' && policyToCopy" class="copy-layout-v3">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>复制认证策略</h2>
        </div>
        <p class="desc">复制基础信息以外的登录方式、验证码与锁定、MFA 配置；不复制引用关系。</p>
      </div>

      <el-card shadow="never" class="form-card-box">
        <template #header>
          <div class="section-head">
            <h2 class="card-title">基于“{{ policyToCopy.name }}”创建副本</h2>
            <p class="card-desc">复制后默认禁用，避免误用于线上身份域。</p>
          </div>
        </template>
        <div class="form-body-container">
          <div class="two-col-grid">
            <div class="left-pane-v3">
              <div class="copy-block-card">
                <h3>新策略信息</h3>
                <div class="field-grid">
                  <div class="control-item">
                    <label>新策略名称</label>
                    <el-input v-model="copyForm.name" />
                  </div>
                  <div class="control-item">
                    <label>新策略编码</label>
                    <el-input v-model="copyForm.code" />
                  </div>
                  <div class="control-item">
                    <label>适用身份域类型</label>
                    <el-input v-model="copyForm.realmTypeName" :disabled="true" />
                  </div>
                  <div class="control-item">
                    <label>状态</label>
                    <el-select v-model="copyForm.status" :disabled="true">
                      <el-option label="禁用" :value="3" />
                    </el-select>
                  </div>
                </div>
              </div>

              <div class="copy-block-card" style="margin-top: 20px;">
                <h3>复制内容</h3>
                <div class="check-grid">
                  <div class="check-box" :class="copyForm.copyItems.includes('auth') ? 'on' : ''" @click="toggleCopyItem('auth')">
                    <span class="custom-dot"></span>登录方式
                  </div>
                  <div class="check-box" :class="copyForm.copyItems.includes('captcha') ? 'on' : ''" @click="toggleCopyItem('captcha')">
                    <span class="custom-dot"></span>验证码规则
                  </div>
                  <div class="check-box" :class="copyForm.copyItems.includes('lock') ? 'on' : ''" @click="toggleCopyItem('lock')">
                    <span class="custom-dot"></span>失败锁定
                  </div>
                  <div class="check-box" :class="copyForm.copyItems.includes('mfa') ? 'on' : ''" @click="toggleCopyItem('mfa')">
                    <span class="custom-dot"></span>MFA 配置
                  </div>
                  <div class="check-box" :class="copyForm.copyItems.includes('refs') ? 'on' : ''" @click="toggleCopyItem('refs')">
                    <span class="custom-dot"></span>引用关系
                  </div>
                  <div class="check-box" :class="copyForm.copyItems.includes('logs') ? 'on' : ''" @click="toggleCopyItem('logs')">
                    <span class="custom-dot"></span>审计记录
                  </div>
                </div>
              </div>
            </div>

            <div class="right-pane-v3">
              <div class="copy-block-card">
                <h3>来源配置摘要</h3>
                <div class="kv-grid vertical">
                  <div class="kv-pair-vertical"><span class="k">登录方式</span><span class="v">{{ getAuthText(policyToCopy.authMethods) }}</span></div>
                  <div class="kv-pair-vertical"><span class="k">验证码</span><span class="v">{{ policyToCopy.captchaType }}</span></div>
                  <div class="kv-pair-vertical"><span class="k">失败锁定</span><span class="v">{{ policyToCopy.maxFailures }} 次，锁定 {{ policyToCopy.lockoutDuration }} 分钟</span></div>
                  <div class="kv-pair-vertical"><span class="k">MFA</span><span class="v">{{ policyToCopy.mfaTrigger }}，{{ policyToCopy.mfaMethods.join(' / ') || '无' }}</span></div>
                  <div class="kv-pair-vertical"><span class="k">记住设备</span><span class="v">{{ policyToCopy.mfaRememberDevice }}，{{ policyToCopy.mfaRememberDuration }} 天</span></div>
                </div>
              </div>
              <div class="warn-msg-block" style="margin-top: 18px;">
                复制后的策略不会自动绑定身份域或客户端，需要人工启用并选择。
              </div>
            </div>
          </div>
        </div>
      </el-card>
      <div class="footer-bar" style="margin-top: 20px;">
        <el-button @click="currentView = 'list'">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="submitCopyForm">确认复制</el-button>
      </div>
    </div>

    <!-- Edit Drawer -->
    <el-drawer
      v-model="isEditDrawerOpen"
      title="编辑认证策略"
      size="560px"
      class="custom-edit-drawer-v3"
    >
      <div class="drawer-body-wrap">
        <p class="drawer-subtitle">单页抽屉内合并基础信息、验证码与 MFA。</p>

        <div class="form-item-mock">
          <label>策略名称</label>
          <el-input v-model="drawerForm.name" />
        </div>
        <div class="form-item-mock">
          <label>策略编码</label>
          <div class="input-readonly">tenant_auth（已引用，只读）</div>
        </div>
        <div class="form-item-mock">
          <label>登录方式</label>
          <el-input v-model="drawerForm.authMethodsText" :disabled="true" class="disabled-field" />
        </div>
        <div class="form-item-mock">
          <label>图形验证码</label>
          <el-input v-model="drawerForm.captchaType" :disabled="true" class="disabled-field" />
        </div>
        <div class="form-item-mock">
          <label>最大失败次数</label>
          <el-input v-model.number="drawerForm.maxFailures" />
        </div>
        <div class="form-item-mock">
          <label>锁定时长</label>
          <el-input v-model.number="drawerForm.lockoutDuration" />
        </div>
        <div class="form-item-mock">
          <label>MFA 触发条件</label>
          <el-input v-model="drawerForm.mfaTrigger" :disabled="true" class="disabled-field" />
        </div>
        <div class="form-item-mock">
          <label>MFA 验证方式</label>
          <el-input v-model="drawerForm.mfaMethodsText" :disabled="true" class="disabled-field" />
        </div>
        <div class="form-item-mock">
          <label>记住设备</label>
          <el-input v-model="drawerForm.mfaRememberDevice" :disabled="true" class="disabled-field" />
        </div>

        <div class="warn-msg-block">保存后立即影响引用该策略的登录流程。</div>
        <div style="display:flex;justify-content:flex-end;gap:12px;margin-top:24px">
          <el-button @click="isEditDrawerOpen = false">取消</el-button>
          <el-button type="primary" @click="saveDrawerEdit">保存</el-button>
        </div>
      </div>
    </el-drawer>

    <!-- Confirm Disable Dialog Overlay -->
    <el-dialog
      v-model="isConfirmDisableOpen"
      title="确认禁用认证策略？"
      width="450px"
      align-center
      class="custom-conflict-dialog"
    >
      <div class="dialog-body-container">
        <p class="dialog-desc">禁用后该策略不能再被新的身份域或客户端选择。</p>
        <div class="conflict-scope-box warn">
          <strong>当前绑定引用数：</strong>
          <p>
            身份域：{{ referencedPolicyForConfirm?.realmRefs.join('、') || '无' }}<br/>
            客户端：{{ referencedPolicyForConfirm?.clientRefs.join('、') || '无' }}<br/>
            <strong>警告：</strong>继续强制禁用可能会使绑定的身份空间登录流程阻断。
          </p>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button @click="isConfirmDisableOpen = false">取消</el-button>
          <el-button type="danger" @click="() => { if (referencedPolicyForConfirm) { referencedPolicyForConfirm.status = 3; isConfirmDisableOpen = false; ElMessage.success('已禁用'); } }">确认禁用</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Block Delete Dialog Overlay -->
    <el-dialog
      v-model="isBlockDeleteOpen"
      title="无法删除认证策略"
      width="450px"
      align-center
      class="custom-conflict-dialog"
    >
      <div class="dialog-body-container">
        <p class="dialog-desc">该认证策略正在被身份域或客户端引用，不能直接删除。</p>
        <div class="conflict-scope-box err">
          <strong>影响引用范围：</strong>
          <p>
            身份域：{{ referencedPolicyForConfirm?.realmRefs.join('、') || '无' }}<br/>
            客户端：{{ referencedPolicyForConfirm?.clientRefs.join('、') || '无' }}<br/>
            <strong>处理建议：</strong>请先前往对应的身份域或客户端，解绑此策略或替换为其他认证策略后，再执行删除。
          </p>
        </div>
        <div class="error-msg-block" style="margin-top: 12px;">MFA 是认证策略内配置，不需要额外检查 MFA 策略引用。</div>
      </div>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button @click="isBlockDeleteOpen = false">关闭</el-button>
          <el-button type="primary" @click="isBlockDeleteOpen = false">查看引用对象</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.auth-policy-module {
  font-family: "Inter", "Segoe UI", "Microsoft YaHei", sans-serif;
  color: #334155;
  min-height: 100%;
}

/* Header */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title-wrap h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 750;
  color: #0F172A;
}
.title-wrap p {
  margin: 0;
  font-size: 14px;
  color: #64748B;
}
.action-buttons {
  display: flex;
  gap: 12px;
}
.action-buttons :deep(.el-button) {
  height: 36px;
  font-weight: 650;
  border-radius: 6px;
}
.btn-create-policy {
  height: 36px;
  font-weight: 650;
  border-radius: 6px;
}

/* Back Action Wrap */
.header-back-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.header-back-wrap h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 750;
  color: #0F172A;
}
.back-icon {
  font-size: 18px;
  color: #64748B;
}

/* Premium Tabs */
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
  font-weight: 650;
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

/* Detail Navigation Tabs */
.detail-tabs-bar {
  display: flex;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 8px 16px;
  margin-bottom: 20px;
  gap: 8px;
}
.detail-tab-item {
  height: 32px;
  padding: 0 16px;
  display: inline-flex;
  align-items: center;
  border-radius: 6px;
  color: #64748B;
  font-size: 14px;
  font-weight: 650;
  cursor: pointer;
  transition: all 0.2s ease;
}
.detail-tab-item.active {
  background-color: #EFF6FF;
  color: #2563EB;
}

/* Filter */
.filter-card {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
  margin-bottom: 16px;
}
.filter-card :deep(.el-card__body) {
  padding: 20px 24px;
}
.filter-flex-row {
  display: flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 18px;
}
.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 180px;
}
.filter-item label {
  font-size: 13px;
  font-weight: 650;
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
  font-weight: 650;
  border-radius: 6px;
}
.filter-buttons :deep(.el-button--primary) {
  background-color: #2563EB;
  border-color: #2563EB;
}

/* Table Card */
.table-card {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
}
.table-card :deep(.el-card__body) {
  padding: 0;
}
.card-header-title {
  padding: 18px 24px;
  border-bottom: 1px solid #E5E7EB;
}
.card-header-title h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #0F172A;
  font-weight: 750;
}
.card-header-title p {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* Premium Table Row Link */
.policy-name-link {
  color: #0F172A;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.15s ease;
}
.policy-name-link:hover {
  color: #2563EB;
}

/* Row Action links */
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

/* Badges */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 750;
  white-space: nowrap;
}
.badge.green {
  background-color: #DCFCE7;
  color: #16A34A;
}
.badge.blue {
  background-color: #EFF6FF;
  color: #2563EB;
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

/* Form layouts */
.two-col-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.form-card-block {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
  padding: 20px 24px;
}
.form-card-block h3 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #0F172A;
  font-weight: 750;
}
.form-card-block p {
  margin: 0 0 18px 0;
  font-size: 13px;
  color: #64748B;
  line-height: 20px;
}
.field-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18px 20px;
}
.control-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.control-item label {
  font-size: 13px;
  font-weight: 650;
  color: #334155;
}
.control-item label .req {
  color: #DC2626;
}
.control-item :deep(.el-input__wrapper),
.control-item :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: none !important;
  width: 100%;
}
.control-item :deep(.el-textarea__inner) {
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: none !important;
}
.span-full {
  grid-column: span 2;
}

/* Checkboxes grid */
.check-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.check-box {
  height: 42px;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
  display: flex;
  align-items: center;
  padding: 0 14px;
  gap: 10px;
  font-size: 13px;
  color: #334155;
  cursor: pointer;
  transition: all 0.2s ease;
}
.check-box.on {
  border-color: #BFDBFE;
  background-color: #EFF6FF;
  color: #2563EB;
  font-weight: 650;
}
.custom-dot {
  width: 14px;
  height: 14px;
  border: 1px solid #CBD5E1;
  border-radius: 4px;
}
.check-box.on .custom-dot {
  background-color: #2563EB;
  border-color: #2563EB;
}

/* Alerts box */
.hint-alert-box {
  border: 1px solid #BFDBFE;
  background-color: #EFF6FF;
  border-radius: 8px;
  padding: 12px 16px;
  color: #1E40AF;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
}
.warn-msg-block {
  border: 1px solid #FED7AA;
  background-color: #FFF7ED;
  border-radius: 8px;
  padding: 12px 16px;
  color: #9A3412;
  font-size: 13px;
  line-height: 20px;
}
.error-msg-block {
  border: 1px solid #FECACA;
  background-color: #FFF1F2;
  border-radius: 8px;
  padding: 12px 16px;
  color: #991B1B;
  font-size: 13px;
  line-height: 20px;
}

/* Flow preview v3 */
.preview-flow-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.flow-step {
  height: 38px;
  border: 1px solid #E5E7EB;
  background-color: #fff;
  border-radius: 20px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #334155;
  font-weight: 650;
}
.flow-arrow {
  color: #94A3B8;
}

/* Footer v3 */
.footer-bar {
  margin-top: 24px;
  height: 64px;
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 24px;
}

/* Detail Redesign v3 */
.detail-split-grid {
  display: grid;
  grid-template-columns: 1fr 392px;
  gap: 24px;
}
.detail-card-panel {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
}
.detail-card-panel :deep(.el-card__body) {
  padding: 20px 24px;
}
.detail-card-header h4 {
  margin: 0;
  font-size: 16px;
  color: #0F172A;
  font-weight: 750;
}
.detail-card-header p {
  margin: 4px 0 0 0;
  font-size: 13px;
  color: #64748B;
}
.kv-grid {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 14px 24px;
  font-size: 13.5px;
}
.kv-grid.vertical {
  grid-template-columns: 1fr;
  gap: 16px;
}
.kv-pair-vertical {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.kv-pair-vertical .k {
  color: #64748B;
  font-size: 12.5px;
}
.kv-pair-vertical .v {
  color: #0F172A;
  font-weight: 650;
  font-size: 13.5px;
}
.kv-grid .k {
  color: #64748B;
}
.kv-grid .v {
  color: #334155;
  font-weight: 600;
}
.detail-flow-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Refs aside list */
.detail-ref-list {
  display: flex;
  flex-direction: column;
  margin-top: 10px;
}
.detail-ref-item {
  height: 44px;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}
.detail-ref-item strong {
  color: #0F172A;
}
.detail-ref-item span {
  color: #64748B;
}
.empty-refs {
  padding: 20px 0;
  text-align: center;
  color: #64748B;
  font-size: 13px;
}

/* Summary view v3 */
.warn-alert-box {
  border: 1px solid #FED7AA;
  background-color: #FFF7ED;
  border-radius: 8px;
  padding: 16px 20px;
}
.warn-alert-box h4 {
  margin: 0 0 6px 0;
  font-size: 16px;
  color: #9A3412;
}
.warn-alert-box p {
  margin: 0;
  font-size: 13px;
  color: #C2410C;
}
.summary-table-v3 {
  width: 100%;
  border-collapse: collapse;
}
.summary-table-v3 th {
  height: 44px;
  background-color: #F8FAFC;
  text-align: left;
  color: #64748B;
  font-weight: 750;
  border-bottom: 1px solid #E5E7EB;
  padding: 0 16px;
}
.summary-table-v3 td {
  height: 52px;
  border-bottom: 1px solid #E5E7EB;
  padding: 0 16px;
  color: #334155;
  font-size: 13.5px;
}

/* Drawer v3 */
.custom-edit-drawer-v3 :deep(.el-drawer__body) {
  padding: 24px;
}
.drawer-body-wrap {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.drawer-subtitle {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 8px 0;
}
.input-readonly {
  height: 36px;
  background-color: #F8FAFC;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  color: #64748B;
  display: flex;
  align-items: center;
  padding: 0 12px;
  font-size: 13px;
}

/* Copy layout v3 */
.copy-block-card {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
  padding: 20px 24px;
}
.copy-block-card h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #0F172A;
  font-weight: 750;
}

/* Dialog scope styles */
.dialog-body-container {
  display: flex;
  flex-direction: column;
}
.dialog-desc {
  font-size: 14px;
  color: #334155;
  line-height: 22px;
  margin: 0 0 16px 0;
}
.conflict-scope-box {
  border-radius: 8px;
  padding: 14px;
  font-size: 13px;
  line-height: 1.8;
}
.conflict-scope-box.warn {
  background-color: #FFFBEB;
  border: 1px solid #FED7AA;
  color: #92400E;
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
.custom-conflict-dialog :deep(.el-dialog__body) {
  padding: 20px 24px 16px;
}

@media (max-width: 1024px) {
  .two-col-grid,
  .detail-split-grid {
    grid-template-columns: 1fr;
  }
}
.table-pagination-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  color: #64748b;
  font-size: 13px;
}
</style>
