<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, Search, Refresh, ArrowDown, ArrowLeft, Warning,
  InfoFilled, View, Edit, CopyDocument, Delete, Check, Connection, Document
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { authMethodApi } from '@/api/authMethod'

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
}

// Active top tab (policies / logs in list view)
const activeTopTab = ref<'methods' | 'policies' | 'logs'>('methods')

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
  params: {
    // SMS defaults
    smsProvider: '阿里云短信',
    codeTtl: 300,
    sendInterval: 60,
    dailyLimit: 10,
    retryLimit: 5,
    templateCode: 'SMS_LOGIN_DEFAULT',
    // Email defaults
    smtpHost: 'smtp.authsphere.com',
    smtpPort: 465,
    emailTtl: 300,
    // Password defaults
    passwordPolicy: 'default',
    maxRetries: 5,
    lockDuration: 30,
    // OIDC defaults
    oidcIssuer: 'https://oidc.authsphere.com',
    clientId: 'authsphere_admin_client',
    clientSecret: '••••••••••••••••••••',
    // TOTP defaults
    totpDigits: 6,
    totpPeriod: 30,
    totpWindow: 1
  }
})

// Query filters
const query = reactive({
  name: '',
  position: '',
  status: undefined as number | undefined
})

const methods = ref<AuthMethodRecord[]>([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(100)

const saveLoading = ref(false)

const loadMethods = async () => {
  loading.value = true
  try {
    const res = await authMethodApi.page({
      page: page.value,
      size: size.value,
      name: query.name || undefined,
      position: query.position || undefined,
      status: query.status !== undefined ? query.status : undefined
    })
    methods.value = res.records.map(record => ({
      id: record.id,
      name: record.name,
      code: record.code,
      positions: record.positions || [],
      applicableRange: record.applicableRange || '',
      status: record.status,
      referenceCount: record.referenceCount || 0,
      sortNo: record.sortNo || 0,
      description: record.description || '',
      params: {}
    }))
    total.value = res.total
  } catch (error: any) {
    ElMessage.error(error.message || '获取认证方式列表失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMethods()
})

const filteredMethods = computed(() => {
  return methods.value
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
  Object.assign(formModel, {
    id: detailData.id,
    name: detailData.name,
    code: detailData.code,
    applicableRange: detailData.applicableRange || '全部',
    status: detailData.status,
    sortNo: detailData.sortNo || 10,
    description: detailData.description || '',
    positions: detailData.positions || [],
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
    params: {
      smsProvider: '阿里云短信',
      codeTtl: 300,
      sendInterval: 60,
      dailyLimit: 10,
      retryLimit: 5,
      templateCode: 'SMS_LOGIN_DEFAULT',
      smtpHost: 'smtp.authsphere.com',
      smtpPort: 465,
      emailTtl: 300,
      passwordPolicy: 'default',
      maxRetries: 5,
      lockDuration: 30,
      oidcIssuer: 'https://oidc.authsphere.com',
      clientId: 'authsphere_admin_client',
      clientSecret: '',
      totpDigits: 6,
      totpPeriod: 30,
      totpWindow: 1
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

const getParamsByCode = (code: string, rawParams: Record<string, any>) => {
  const result: Record<string, any> = {}
  if (code === 'sms_code') {
    result.smsProvider = rawParams.smsProvider
    result.codeTtl = Number(rawParams.codeTtl)
    result.sendInterval = Number(rawParams.sendInterval)
    result.dailyLimit = Number(rawParams.dailyLimit)
    result.retryLimit = Number(rawParams.retryLimit)
    result.templateCode = rawParams.templateCode
  } else if (code === 'password_login') {
    result.passwordPolicy = rawParams.passwordPolicy
    result.maxRetries = Number(rawParams.maxRetries)
    result.lockDuration = Number(rawParams.lockDuration)
  } else if (code === 'email_code') {
    result.smtpHost = rawParams.smtpHost
    result.smtpPort = Number(rawParams.smtpPort)
    result.emailTtl = Number(rawParams.emailTtl)
  } else if (code === 'totp_mfa') {
    result.totpDigits = Number(rawParams.totpDigits)
    result.totpPeriod = Number(rawParams.totpPeriod)
    result.totpWindow = Number(rawParams.totpWindow)
  } else if (code === 'oidc_login') {
    result.oidcIssuer = rawParams.oidcIssuer
    result.clientId = rawParams.clientId
    result.clientSecret = rawParams.clientSecret
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
      params: getParamsByCode(formModel.code, formModel.params)
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


      <div v-if="activeTopTab === 'methods'">
        <!-- Filter Card -->
        <el-card shadow="never" class="filter-card">
          <div class="filter-flex-row">
            <div class="filter-item">
              <label>认证方式名称</label>
              <el-input v-model="query.name" placeholder="请输入名称" clearable @keyup.enter="handleSearch" />
            </div>
            <div class="filter-item">
              <label>可用位置</label>
              <el-select v-model="query.position" placeholder="全部" clearable @change="handleSearch">
                <el-option label="主登录" value="主登录" />
                <el-option label="MFA" value="MFA" />
                <el-option label="接口认证" value="接口认证" />
                <el-option label="允许自动建号" value="允许自动建号" />
              </el-select>
            </div>
            <div class="filter-item">
              <label>状态</label>
              <el-select v-model="query.status" placeholder="全部" clearable @change="handleSearch">
                <el-option label="启用" :value="1" />
                <el-option label="待配置" :value="2" />
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
            <p>字段表达具体配置状态、可用位置、引用状态及安全作用域。</p>
          </div>

          <el-table :data="filteredMethods" class="premium-table" v-loading="loading">
            <el-table-column prop="name" label="认证方式名称" min-width="180">
              <template #default="{ row }">
                <span class="method-name-link" @click="openDetail(row)">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="code" label="编码" min-width="150" />
            <el-table-column label="可用位置" min-width="150">
              <template #default="{ row }">
                {{ getPositionLabel(row.positions) }}
              </template>
            </el-table-column>
            <el-table-column prop="applicableRange" label="适用范围" min-width="130" />
            <el-table-column label="状态" width="110" align="center">
              <template #default="{ row }">
                <span class="badge" :class="row.status === 1 ? 'green' : (row.status === 2 ? 'orange' : 'red')">
                  {{ row.status === 1 ? '启用' : (row.status === 2 ? '待配置' : '禁用') }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="引用" min-width="110" align="center">
              <template #default="{ row }">
                <span class="badge blue">{{ row.referenceCount }} 个策略</span>
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

      <div v-else-if="activeTopTab === 'policies'">
        <el-card shadow="never" class="table-card">
          <div class="card-header-title">
            <h3>引用认证方式的策略明细</h3>
            <p>展示各认证策略中组合启用的主登录与 MFA 绑定范围。</p>
          </div>
          <el-table :data="[
            { policyName: '租户默认认证策略', methodCode: 'password_login', role: '主登录方式', isMandatory: '是', status: '已启用' },
            { policyName: '租户默认认证策略', methodCode: 'sms_code', role: '主登录 / MFA', isMandatory: '否', status: '已启用' },
            { policyName: '平台强认证策略', methodCode: 'password_login', role: '主登录方式', isMandatory: '是', status: '已启用' },
            { policyName: '平台强认证策略', methodCode: 'totp_mfa', role: 'MFA 方式', isMandatory: '是', status: '待配置' }
          ]" class="premium-table">
            <el-table-column prop="policyName" label="认证策略" />
            <el-table-column prop="methodCode" label="认证方式编码" />
            <el-table-column prop="role" label="策略角色" />
            <el-table-column prop="isMandatory" label="是否必须" width="100" />
            <el-table-column prop="status" label="绑定状态" width="120" />
          </el-table>
        </el-card>
      </div>

      <div v-else-if="activeTopTab === 'logs'">
        <el-card shadow="never" class="table-card">
          <div class="card-header-title">
            <h3>操作审计日志</h3>
            <p>追溯对具体认证配置、可用范围及参数调整的历史记录。</p>
          </div>
          <el-table :data="[
            { time: '2026-06-09 10:20:12', user: 'admin', action: '修改短信参数', target: 'sms_code', detail: '验证码有效期从 120 秒变更为 300 秒' },
            { time: '2026-06-08 15:44:03', user: 'admin', action: '新增配置项目', target: 'totp_mfa', detail: '新增 TOTP 认证方式口令配置' },
            { time: '2026-06-07 09:12:33', user: 'system', action: '组件初始化', target: 'password_login', detail: '平台初始化生成内置账号密码认证方式' }
          ]" class="premium-table">
            <el-table-column prop="time" label="操作时间" width="180" />
            <el-table-column prop="user" label="操作人" width="120" />
            <el-table-column prop="action" label="事件名称" width="160" />
            <el-table-column prop="target" label="操作对象" width="150" />
            <el-table-column prop="detail" label="变更描述" />
          </el-table>
        </el-card>
      </div>
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
        <div class="hint-alert-box">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
          <span>单页分区完成基础信息、可用位置与针对不同方式的具体参数配置。</span>
        </div>

        <div class="two-col-grid" style="margin-top: 18px;">
          <!-- Left Column -->
          <div class="form-left-col">
            <!-- Block 1: Basic Info -->
            <div class="form-card-block">
              <h3>基础信息</h3>
              <p>名称和编码是该方法的全局标识，描述该方法的具体使用范围。</p>
              <div class="field-grid">
                <div class="control-item">
                  <label>认证方式名称 <span class="req" v-if="!isReadOnly">*</span></label>
                  <el-input v-model="formModel.name" placeholder="请输入名称，例：短信验证码认证" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>认证方式编码 <span class="req" v-if="!isReadOnly">*</span></label>
                  <el-select v-model="formModel.code" placeholder="请选择或输入" :disabled="isReadOnly || formModel.id !== ''">
                    <el-option label="账号密码认证 (password_login)" value="password_login" />
                    <el-option label="短信验证码认证 (sms_code)" value="sms_code" />
                    <el-option label="邮箱验证码认证 (email_code)" value="email_code" />
                    <el-option label="TOTP 动态口令 (totp_mfa)" value="totp_mfa" />
                    <el-option label="OIDC 登录 (oidc_login)" value="oidc_login" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>适用范围</label>
                  <el-input v-model="formModel.applicableRange" placeholder="例：手机登录 / 后台 MFA" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>排序</label>
                  <el-input v-model.number="formModel.sortNo" type="number" placeholder="请输入序号" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>状态</label>
                  <el-select v-model="formModel.status" :disabled="isReadOnly">
                    <el-option label="启用" :value="1" />
                    <el-option label="待配置" :value="2" />
                    <el-option label="禁用" :value="3" />
                  </el-select>
                </div>
                <div class="control-item span-full">
                  <label>备注描述</label>
                  <el-input v-model="formModel.description" type="textarea" :rows="2" placeholder="请输入备注描述" :disabled="isReadOnly" />
                </div>
              </div>
            </div>

            <!-- Block 2: Positions -->
            <div class="form-card-block" style="margin-top: 18px;">
              <h3>可用位置</h3>
              <p>决定了该认证方式可以呈现在认证策略配置界面的哪个选项分区中。</p>
              <div class="choice-grid">
                <div 
                  class="choice" 
                  :class="[formModel.positions.includes('主登录') ? 'on' : '', isReadOnly ? 'disabled-choice' : '']"
                  @click="togglePosition('主登录')"
                >
                  <b>可作为主登录</b>
                  <span>登录页可供用户选择</span>
                </div>
                <div 
                  class="choice" 
                  :class="[formModel.positions.includes('MFA') ? 'on' : '', isReadOnly ? 'disabled-choice' : '']"
                  @click="togglePosition('MFA')"
                >
                  <b>可作为 MFA</b>
                  <span>二次强化认证可作为选项</span>
                </div>
                <div 
                  class="choice" 
                  :class="[formModel.positions.includes('接口认证') ? 'on' : '', isReadOnly ? 'disabled-choice' : '']"
                  @click="togglePosition('接口认证')"
                >
                  <b>可作为接口认证</b>
                  <span>支持通过接口直接凭证校验</span>
                </div>
                <div 
                  class="choice" 
                  :class="[formModel.positions.includes('允许自动建号') ? 'on' : '', isReadOnly ? 'disabled-choice' : '']"
                  @click="togglePosition('允许自动建号')"
                >
                  <b>允许自动建号</b>
                  <span>适合第三方外部身份源绑定</span>
                </div>
              </div>
            </div>

            <!-- Block 3: Referenced Policies (Only shown in Edit/Detail mode) -->
            <div v-if="formModel.id" class="form-card-block" style="margin-top: 18px;">
              <h3>当前关联策略 ({{ references.length }})</h3>
              <p>当前认证方式被应用在以下策略中。</p>
              <el-table :data="references" class="premium-table" style="margin-top: 10px;">
                <el-table-column prop="name" label="策略名称" />
                <el-table-column prop="code" label="策略编码" width="120" />
                <el-table-column prop="role" label="策略角色" width="120" />
              </el-table>
            </div>
          </div>

          <!-- Right Column -->
          <div class="form-right-col">
            <!-- Block 4: Dynamic Parameters Form Block -->
            <div class="form-card-block">
              <h3>针对方式的参数配置</h3>
              <p>不同类型的认证方式会渲染出不同的具体连接配置参数。</p>
              
              <!-- Case A: SMS Code Configuration -->
              <div v-if="formModel.code === 'sms_code'" class="field-grid">
                <div class="control-item span-full">
                  <label>短信服务商</label>
                  <el-select v-model="formModel.params.smsProvider" style="width:100%" :disabled="isReadOnly">
                    <el-option label="阿里云短信" value="阿里云短信" />
                    <el-option label="腾讯云短信" value="腾讯云短信" />
                    <el-option label="华为云短信" value="华为云短信" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>验证码有效期</label>
                  <el-input v-model.number="formModel.params.codeTtl" :disabled="isReadOnly"><template #append>秒</template></el-input>
                </div>
                <div class="control-item">
                  <label>发送间隔</label>
                  <el-input v-model.number="formModel.params.sendInterval" :disabled="isReadOnly"><template #append>秒</template></el-input>
                </div>
                <div class="control-item">
                  <label>每日上限</label>
                  <el-input v-model.number="formModel.params.dailyLimit" :disabled="isReadOnly"><template #append>次/账号</template></el-input>
                </div>
                <div class="control-item">
                  <label>失败重试上限</label>
                  <el-input v-model.number="formModel.params.retryLimit" :disabled="isReadOnly"><template #append>次</template></el-input>
                </div>
                <div class="control-item span-full">
                  <label>短信模板编码</label>
                  <el-input v-model="formModel.params.templateCode" placeholder="LOGIN_CODE" :disabled="isReadOnly" />
                </div>
              </div>

              <!-- Case B: Password Login Configuration -->
              <div v-else-if="formModel.code === 'password_login'" class="field-grid">
                <div class="control-item span-full">
                  <label>密码强度策略</label>
                  <el-select v-model="formModel.params.passwordPolicy" style="width:100%" :disabled="isReadOnly">
                    <el-option label="默认常规策略 (常规长度/大小写)" value="default" />
                    <el-option label="高强度密码策略 (要求特殊字符且定期过期)" value="strong" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>连续失败上限</label>
                  <el-input v-model.number="formModel.params.maxRetries" :disabled="isReadOnly"><template #append>次</template></el-input>
                </div>
                <div class="control-item">
                  <label>失败锁定时间</label>
                  <el-input v-model.number="formModel.params.lockDuration" :disabled="isReadOnly"><template #append>分钟</template></el-input>
                </div>
              </div>

              <!-- Case C: Email Login Configuration -->
              <div v-else-if="formModel.code === 'email_code'" class="field-grid">
                <div class="control-item span-full">
                  <label>SMTP 发送服务器地址</label>
                  <el-input v-model="formModel.params.smtpHost" placeholder="smtp.domain.com" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>SMTP 服务器端口</label>
                  <el-input v-model.number="formModel.params.smtpPort" placeholder="465" :disabled="isReadOnly" />
                </div>
                <div class="control-item">
                  <label>验证码有效期</label>
                  <el-input v-model.number="formModel.params.emailTtl" :disabled="isReadOnly"><template #append>秒</template></el-input>
                </div>
              </div>

              <!-- Case D: TOTP MFA Configuration -->
              <div v-else-if="formModel.code === 'totp_mfa'" class="field-grid">
                <div class="control-item">
                  <label>动态验证码长度</label>
                  <el-select v-model="formModel.params.totpDigits" style="width:100%" :disabled="isReadOnly">
                    <el-option label="6 位数字" :value="6" />
                    <el-option label="8 位数字" :value="8" />
                  </el-select>
                </div>
                <div class="control-item">
                  <label>步长时间窗口</label>
                  <el-input v-model.number="formModel.params.totpPeriod" :disabled="isReadOnly"><template #append>秒</template></el-input>
                </div>
                <div class="control-item">
                  <label>校验允许偏移量</label>
                  <el-input v-model.number="formModel.params.totpWindow" :disabled="isReadOnly"><template #append>窗口</template></el-input>
                </div>
              </div>

              <!-- Case E: OIDC Configuration -->
              <div v-else-if="formModel.code === 'oidc_login'" class="field-grid">
                <div class="control-item span-full">
                  <label>OIDC 认证端点发行地址 (Issuer URL)</label>
                  <el-input v-model="formModel.params.oidcIssuer" placeholder="https://oidc.host.com" :disabled="isReadOnly" />
                </div>
                <div class="control-item span-full">
                  <label>客户端 ID (Client ID)</label>
                  <el-input v-model="formModel.params.clientId" :disabled="isReadOnly" />
                </div>
                <div class="control-item span-full">
                  <label>客户端密钥 (Client Secret)</label>
                  <el-input v-model="formModel.params.clientSecret" type="password" show-password :disabled="isReadOnly" />
                </div>
              </div>

              <!-- Default Fallback -->
              <div v-else class="empty-params-card">
                <el-icon class="info-icon"><Document /></el-icon>
                <span>请选择适当的“认证方式编码”以加载该方式支持的定制参数字段。</span>
              </div>
            </div>
            
            <div class="warn-msg-block" style="margin-top: 18px;">
              提示：认证方式是基础组件，新增后需要在具体的“认证策略”中开启绑定，否则前台登录页面不会展现此入口。
            </div>
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
          <el-button type="primary" @click="() => { isDeleteBlockOpen = false; activeTopTab = 'policies'; currentView = 'list'; }">查看引用</el-button>
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
  grid-template-columns: 1fr 1fr;
  gap: 16px;
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

@media (max-width: 1024px) {
  .two-col-grid,
  .detail-split-grid {
    grid-template-columns: 1fr;
  }
}
</style>
