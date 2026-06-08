<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Plus, Search, Refresh, ArrowDown, ArrowLeft, Warning,
  InfoFilled, View, Edit, CopyDocument, Delete, Check, Connection, TopRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { loginPageApi, type LoginPageRecord, type LoginPagePayload } from '@/api/loginPage'
import { typeCategoryApi } from '@/api/typeCategory'

const router = useRouter()

// Active view state: 'list' | 'create' | 'copy' | 'detail'
const currentView = ref<'list' | 'create' | 'copy' | 'detail'>('list')

// Selected / Active items
const selectedPage = ref<LoginPageRecord | null>(null)
const pageToCopy = ref<LoginPageRecord | null>(null)

// Edit drawer state
const isEditDrawerOpen = ref(false)
const drawerForm = reactive({
  id: '',
  name: '',
  code: '',
  applicableRealmTypeId: null as string | number | null,
  pageTitle: '',
  pageSubtitle: '',
  logoUrl: '',
  backgroundUrl: '',
  authMethods: [] as string[],
  defaultAuthMethod: 'password',
  showForgotPassword: true,
  showRegister: false,
  showThirdPartyLogin: false,
  successRedirectUrl: '',
  failurePromptMode: 'message',
  defaultPage: false,
  status: 1,
  referencesCount: 0,
  description: ''
})

// Delete / Disable referenced warning modal
const isDeleteModalOpen = ref(false)
const referencedPageForDelete = ref<LoginPageRecord | null>(null)

// Form submission loading
const saveLoading = ref(false)
const listLoading = ref(false)

// Query filters
const query = reactive({
  name: '',
  code: '',
  realm: '' as string | number | null,
  status: undefined as number | undefined
})

const total = ref(0)
const pagination = reactive({
  page: 1,
  size: 10
})

const loginPages = ref<LoginPageRecord[]>([])
const realmTypeOptions = ref<{ label: string, value: string | number }[]>([])
const realmTypeOptionsLoading = ref(false)

/**
 * 加载启用状态的身份域类型，登录页适用范围必须使用后端维护的身份域类型数据。
 */
const loadRealmTypeOptions = async () => {
  realmTypeOptionsLoading.value = true
  try {
    const list = await typeCategoryApi.list()
    realmTypeOptions.value = (list ?? []).map(item => ({
      label: item.name,
      value: item.id
    }))
  } catch (e: any) {
    realmTypeOptions.value = []
    ElMessage.error(e?.message || '加载身份域类型列表失败')
  } finally {
    realmTypeOptionsLoading.value = false
  }
}

const loadData = async () => {
  listLoading.value = true
  try {
    const res = await loginPageApi.page({
      page: pagination.page,
      size: pagination.size,
      name: query.name || undefined,
      code: query.code || undefined,
      applicableRealmTypeId: query.realm || undefined,
      status: query.status
    })
    loginPages.value = res.records
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载登录页列表失败')
  } finally {
    listLoading.value = false
  }
}

const getStatusBadge = (status: number) => {
  if (status === 1) return { text: '启用', class: 'green' }
  if (status === 2) return { text: '待配置', class: 'orange' }
  return { text: '禁用', class: 'red' }
}

const getAuthText = (methods?: any[]) => {
  if (!methods) return '-'
  return methods.map(m => m.name || m).join(' / ')
}

const getAuthMethodName = (method?: string) => {
  const names: Record<string, string> = {
    password: '密码登录',
    sms: '短信验证码',
    email: '邮箱验证码',
    qr: '扫码登录',
    third_party: '第三方登录',
    mfa: '多因素认证'
  }
  return method ? names[method] || method : '-'
}

const getFailurePromptModeName = (mode?: string) => {
  const names: Record<string, string> = {
    message: '消息提示',
    dialog: '弹窗提示',
    page: '结果页提示'
  }
  return mode ? names[mode] || mode : '-'
}

const getBooleanText = (value?: boolean) => {
  if (value === undefined || value === null) return '-'
  return value ? '是' : '否'
}

const getDisplayText = (value?: string | number | null) => {
  return value === undefined || value === null || value === '' ? '-' : value
}

const handleReset = () => {
  query.name = ''
  query.code = ''
  query.realm = null
  query.status = undefined
  pagination.page = 1
  loadData()
}

// Add page form model
const addForm = reactive({
  name: '',
  code: '',
  applicableRealmTypeId: null as string | number | null,
  status: 1,
  pageTitle: '',
  pageSubtitle: '',
  logoUrl: '',
  bgUrl: '',
  redirectUrl: '',
  authMethods: ['password'] as string[],
  defaultAuthMethod: 'password',
  showForgotPassword: true,
  showRegister: false,
  showThirdPartyLogin: false,
  description: ''
})

// Copy page form model
const copyForm = reactive({
  name: '',
  code: '',
  applicableRealmTypeId: null as string | number | null,
  status: 3
})

const openCreateView = () => {
  Object.assign(addForm, {
    name: '',
    code: '',
    applicableRealmTypeId: null,
    status: 1,
    pageTitle: '',
    pageSubtitle: '',
    logoUrl: '',
    bgUrl: '',
    redirectUrl: '',
    authMethods: ['password'],
    defaultAuthMethod: 'password',
    showForgotPassword: true,
    showRegister: false,
    showThirdPartyLogin: false,
    description: ''
  })
  currentView.value = 'create'
}

const openDetail = async (row: LoginPageRecord) => {
  try {
    const detail = await loginPageApi.detail(row.id)
    selectedPage.value = detail
    currentView.value = 'detail'
  } catch (e: any) {
    ElMessage.error(e?.message || '获取登录页详情失败')
  }
}

const openEditDrawer = async (row: LoginPageRecord) => {
  try {
    const detail = await loginPageApi.detail(row.id)
    selectedPage.value = detail
    currentView.value = 'detail'
    Object.assign(drawerForm, {
      id: String(detail.id),
      name: detail.name,
      code: detail.code,
      applicableRealmTypeId: detail.applicableRealmTypeId,
      pageTitle: detail.pageTitle || '',
      pageSubtitle: detail.pageSubtitle || '',
      logoUrl: detail.logoUrl || '',
      backgroundUrl: detail.backgroundUrl || '',
      authMethods: detail.authMethod?.map(m => m.description || m.id) || ['password'],
      defaultAuthMethod: detail.defaultAuthMethod || 'password',
      showForgotPassword: detail.showForgotPassword ?? true,
      showRegister: detail.showRegister ?? false,
      showThirdPartyLogin: detail.showThirdPartyLogin ?? false,
      successRedirectUrl: detail.successRedirectUrl || '',
      failurePromptMode: detail.failurePromptMode || 'message',
      defaultPage: detail.defaultPage ?? false,
      status: detail.status,
      referencesCount: detail.referenceCount || 0,
      description: detail.description || ''
    })
    isEditDrawerOpen.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '获取登录页详情失败')
  }
}

const openCopyView = async (row: LoginPageRecord) => {
  try {
    const detail = await loginPageApi.detail(row.id)
    pageToCopy.value = detail
    Object.assign(copyForm, {
      name: detail.name + '_副本',
      code: detail.code + '_copy',
      applicableRealmTypeId: detail.applicableRealmTypeId,
      status: 3
    })
    currentView.value = 'copy'
  } catch (e: any) {
    ElMessage.error(e?.message || '获取登录页详情失败')
  }
}

const handleDisableOrDelete = async (row: LoginPageRecord, action: 'disable' | 'delete') => {
  if (row.referenceCount && row.referenceCount > 0) {
    referencedPageForDelete.value = row
    isDeleteModalOpen.value = true
  } else {
    const actionName = action === 'disable' ? '禁用' : '删除'
    ElMessageBox.confirm(`确认${actionName}该登录页？`, '提示', { type: 'warning' }).then(async () => {
      try {
        if (action === 'disable') {
          await loginPageApi.disable(row.id)
          ElMessage.success('登录页已禁用')
        } else {
          await loginPageApi.delete(row.id)
          ElMessage.success('登录页已删除')
        }
        loadData()
      } catch (e: any) {
        ElMessage.error(e?.message || `${actionName}失败`)
      }
    }).catch(() => {})
  }
}

const submitCreateForm = async () => {
  if (!addForm.name || !addForm.code) {
    ElMessage.error('请填写必填项')
    return
  }
  saveLoading.value = true
  try {
    const payload: LoginPagePayload = {
      name: addForm.name,
      code: addForm.code,
      applicableRealmTypeId: addForm.applicableRealmTypeId,
      status: addForm.status,
      pageTitle: addForm.pageTitle || addForm.name,
      pageSubtitle: addForm.pageSubtitle || '统一登录入口，请完成身份认证',
      logoUrl: addForm.logoUrl,
      backgroundUrl: addForm.bgUrl,
      successRedirectUrl: addForm.redirectUrl,
      authMethods: addForm.authMethods,
      defaultAuthMethod: addForm.defaultAuthMethod,
      showForgotPassword: addForm.showForgotPassword,
      showRegister: addForm.showRegister,
      showThirdPartyLogin: addForm.showThirdPartyLogin,
      failurePromptMode: 'message',
      defaultPage: false,
      description: addForm.description
    }
    await loginPageApi.create(payload)
    ElMessage.success('新增登录页成功')
    currentView.value = 'list'
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '新增登录页失败')
  } finally {
    saveLoading.value = false
  }
}

const submitCopyForm = async () => {
  if (!copyForm.name || !copyForm.code) {
    ElMessage.error('请填写必填副本信息')
    return
  }
  if (!pageToCopy.value) return
  saveLoading.value = true
  try {
    const payload: LoginPagePayload = {
      name: copyForm.name,
      code: copyForm.code,
      applicableRealmTypeId: copyForm.applicableRealmTypeId,
      status: copyForm.status,
      pageTitle: pageToCopy.value.pageTitle || copyForm.name,
      pageSubtitle: pageToCopy.value.pageSubtitle || '统一登录入口，请完成身份认证',
      logoUrl: pageToCopy.value.logoUrl || '',
      backgroundUrl: pageToCopy.value.backgroundUrl || '',
      successRedirectUrl: pageToCopy.value.successRedirectUrl || '',
      authMethods: pageToCopy.value.authMethod?.map(m => m.description || m.id) || ['password'],
      defaultAuthMethod: pageToCopy.value.defaultAuthMethod || 'password',
      showForgotPassword: pageToCopy.value.showForgotPassword ?? true,
      showRegister: pageToCopy.value.showRegister ?? false,
      showThirdPartyLogin: pageToCopy.value.showThirdPartyLogin ?? false,
      failurePromptMode: pageToCopy.value.failurePromptMode || 'message',
      defaultPage: false,
      description: `复制自 ${pageToCopy.value.name} (${pageToCopy.value.code})`
    }
    await loginPageApi.create(payload)
    ElMessage.success('复制成功，默认状态为禁用')
    currentView.value = 'list'
    loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '复制登录页失败')
  } finally {
    saveLoading.value = false
  }
}

const saveDrawerEdit = async () => {
  if (!drawerForm.name.trim() || !drawerForm.pageTitle.trim()) {
    ElMessage.error('请填写登录页名称和页面标题')
    return
  }
  if (!drawerForm.authMethods.length) {
    ElMessage.error('请至少选择一种登录方式')
    return
  }
  if (!drawerForm.authMethods.includes(drawerForm.defaultAuthMethod)) {
    ElMessage.error('默认登录方式必须包含在支持的登录方式中')
    return
  }
  saveLoading.value = true
  try {
    const payload: LoginPagePayload = {
      name: drawerForm.name,
      code: drawerForm.code,
      applicableRealmTypeId: drawerForm.applicableRealmTypeId,
      pageTitle: drawerForm.pageTitle,
      pageSubtitle: drawerForm.pageSubtitle,
      logoUrl: drawerForm.logoUrl,
      backgroundUrl: drawerForm.backgroundUrl,
      authMethods: drawerForm.authMethods,
      defaultAuthMethod: drawerForm.defaultAuthMethod,
      showForgotPassword: drawerForm.showForgotPassword,
      showRegister: drawerForm.showRegister,
      showThirdPartyLogin: drawerForm.showThirdPartyLogin,
      successRedirectUrl: drawerForm.successRedirectUrl,
      failurePromptMode: drawerForm.failurePromptMode,
      defaultPage: drawerForm.defaultPage,
      status: drawerForm.status,
      description: drawerForm.description
    }
    await loginPageApi.update(drawerForm.id, payload)
    ElMessage.success('保存修改成功')
    isEditDrawerOpen.value = false
    const detail = await loginPageApi.detail(drawerForm.id)
    selectedPage.value = detail
    await loadData()
  } catch (e: any) {
    ElMessage.error(e?.message || '保存修改失败')
  } finally {
    saveLoading.value = false
  }
}

watch(() => [pagination.page, pagination.size], () => {
  loadData()
})

onMounted(() => {
  loadRealmTypeOptions()
  loadData()
})
</script>

<template>
  <div class="login-page-module">
    <!-- View 1: List View -->
    <div v-if="currentView === 'list'" class="list-layout">
      <div class="view-header">
        <div class="title-wrap">
          <h1>登录页管理</h1>
          <p>维护身份域或客户端未登录时展示的登录入口页面。</p>
        </div>
        <div class="action-buttons">
          <el-button :icon="Refresh" @click="loadData" :loading="listLoading">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openCreateView">新增登录页</el-button>
        </div>
      </div>

      <!-- Search Filter -->
      <el-card shadow="never" class="filter-card">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>登录页名称</label>
            <el-input v-model="query.name" placeholder="请输入名称" clearable />
          </div>
          <div class="filter-item">
            <label>登录页编码</label>
            <el-input v-model="query.code" placeholder="请输入编码" clearable />
          </div>
          <div class="filter-item">
            <label>适用域类型</label>
            <el-select
              v-model="query.realm"
              placeholder="全部域类型"
              clearable
              :loading="realmTypeOptionsLoading"
              no-data-text="暂无可用身份域类型"
              style="width: 100%"
            >
              <el-option
                v-for="item in realmTypeOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value"
              />
            </el-select>
          </div>
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="query.status" placeholder="全部状态" clearable>
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="3" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="loadData" :loading="listLoading">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- Main Table -->
      <el-card shadow="never" class="table-card">
        <div class="card-header-title">
          <h3>登录页列表</h3>
          <p>列表只展示识别、适用范围、登录方式、引用和状态。</p>
        </div>

        <el-table v-loading="listLoading" :data="loginPages" class="premium-table">
          <el-table-column prop="name" label="登录页名称" min-width="160">
            <template #default="{ row }">
              <span class="name-text-detail" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="code" label="登录页编码" min-width="140" />
          <el-table-column label="适用域类型" min-width="140">
            <template #default="{ row }">
              {{ row.applicableRealmTypeName || '通用' }}
            </template>
          </el-table-column>
          <el-table-column label="登录方式" min-width="160">
            <template #default="{ row }">
              {{ getAuthText(row.authMethod) }}
            </template>
          </el-table-column>
          <el-table-column label="默认" width="90" align="center">
            <template #default="{ row }">
              <span class="badge" :class="row.defaultPage ? 'blue' : 'gray'">
                {{ row.defaultPage ? '默认' : '普通' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="badge" :class="getStatusBadge(row.status).class">
                {{ getStatusBadge(row.status).text }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">预览</span>
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <el-dropdown trigger="click">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="openCopyView(row)">复制配置</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'disable')" :disabled="row.status === 3">禁用页面</el-dropdown-item>
                      <el-dropdown-item @click="handleDisableOrDelete(row, 'delete')" class="danger-item">删除页面</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- Pagination -->
        <div class="pagination-container">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="prev, pager, next, sizes, jumper"
            :total="total"
          />
        </div>
      </el-card>
    </div>

    <!-- View 2: Create Page View -->
    <div v-else-if="currentView === 'create'" class="form-layout">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>新增登录页</h2>
        </div>
        <div class="action-buttons">
          <el-button @click="currentView = 'list'">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitCreateForm">保存</el-button>
        </div>
      </div>

      <el-card shadow="never" class="form-card-box">
        <div class="card-header-title">
          <h3>登录页表单</h3>
          <p>配置基础参数与页面布局内容，不做复杂的可视化装修，保留必要核心入口及展示内容。</p>
        </div>

        <el-form label-position="top" class="form-body-container">
          <!-- Section 1 -->
          <div class="form-section">
            <div class="section-title">基础信息</div>
            <p class="section-subtext">登录页编码保存后不建议修改。</p>
            <div class="grid-row-3">
              <el-form-item label="登录页名称 *">
                <el-input v-model="addForm.name" placeholder="请输入名称" />
              </el-form-item>
              <el-form-item label="登录页编码 *">
                <el-input v-model="addForm.code" placeholder="请输入编码" />
              </el-form-item>
              <el-form-item>
                <template #label>
                  <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
                    <span>适用域类型</span>
                    <el-button link type="primary" size="small" style="font-weight: normal; padding: 0; height: auto;" @click="router.push('/realm/type-categories')">管理类型 ↗</el-button>
                  </div>
                </template>
                <el-select
                  v-model="addForm.applicableRealmTypeId"
                  placeholder="请选择身份域类型"
                  clearable
                  :loading="realmTypeOptionsLoading"
                  no-data-text="暂无可用身份域类型"
                  style="width: 100%"
                >
                  <el-option
                    v-for="item in realmTypeOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="addForm.status">
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="3" />
                </el-select>
              </el-form-item>
            </div>
          </div>

          <!-- Section 2 -->
          <div class="form-section" style="margin-top:24px;">
            <div class="section-title">页面展示</div>
            <p class="section-subtext">配置展示的文本信息、Logo 资源和跳转重定向路径。</p>
            <div class="grid-row-3">
              <el-form-item label="页面标题 *">
                <el-input v-model="addForm.pageTitle" placeholder="如: 租户管理后台" />
              </el-form-item>
              <el-form-item label="页面副标题">
                <el-input v-model="addForm.pageSubtitle" placeholder="统一登录入口，请完成身份认证" />
              </el-form-item>
              <el-form-item label="Logo 地址">
                <el-input v-model="addForm.logoUrl" placeholder="如: /static/logo.png" />
              </el-form-item>
              <el-form-item label="背景图地址" class="span-2">
                <el-input v-model="addForm.bgUrl" placeholder="可选，不配置则使用系统默认背景" />
              </el-form-item>
              <el-form-item label="登录成功跳转地址">
                <el-input v-model="addForm.redirectUrl" placeholder="默认返回来源客户端" />
              </el-form-item>
            </div>
          </div>

          <!-- Section 3 -->
          <div class="form-section" style="margin-top:24px;">
            <div class="section-title">登录方式与辅助项</div>
            <p class="section-subtext">控制默认及支持的鉴权方式与界面辅助选项。</p>
            <div class="grid-row-3">
              <el-form-item label="支持的登录方式 *">
                <el-checkbox-group v-model="addForm.authMethods">
                  <div class="checkbox-list">
                    <el-checkbox label="password">账号密码</el-checkbox>
                    <el-checkbox label="sms">短信验证码</el-checkbox>
                    <el-checkbox label="email">邮箱验证码</el-checkbox>
                    <el-checkbox label="qr">扫码登录</el-checkbox>
                    <el-checkbox label="third_party">第三方登录</el-checkbox>
                    <el-checkbox label="mfa">MFA 认证</el-checkbox>
                  </div>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item label="默认登录方式">
                <el-select v-model="addForm.defaultAuthMethod">
                  <el-option label="账号密码" value="password" />
                  <el-option label="短信验证码" value="sms" />
                  <el-option label="邮箱验证码" value="email" />
                  <el-option label="扫码登录" value="qr" />
                </el-select>
              </el-form-item>
              <el-form-item label="辅助入口选项">
                <div class="checkbox-list" style="margin-top: 6px;">
                  <el-checkbox v-model="addForm.showForgotPassword">显示忘记密码</el-checkbox>
                  <el-checkbox v-model="addForm.showRegister">显示注册入口</el-checkbox>
                  <el-checkbox v-model="addForm.showThirdPartyLogin">显示第三方登录</el-checkbox>
                </div>
              </el-form-item>
            </div>
          </div>
        </el-form>
      </el-card>
    </div>

    <!-- View 3: Copy / Clone View -->
    <div v-else-if="currentView === 'copy'" class="form-layout">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>复制登录页</h2>
        </div>
        <div class="action-buttons">
          <el-button @click="currentView = 'list'">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitCopyForm">保存副本</el-button>
        </div>
      </div>

      <el-card shadow="never" class="form-card-box">
        <div class="card-header-title">
          <h3>从 “{{ pageToCopy?.name }}” 复制</h3>
          <p>基于已有模板快速创建新页面。复制后的副本默认处于禁用状态，避免生产环境误引用。</p>
        </div>

        <div class="form-body-container">
          <div class="copy-alert-box">
            <el-icon class="alert-icon"><Warning /></el-icon>
            <span>复制后的默认状态为禁用。确认配置无误后，再手动启用并绑定到对应身份域。</span>
          </div>

          <div class="divider-line"></div>

          <div class="grid-row-3">
            <div class="form-item-mock">
              <label>新登录页名称 *</label>
              <el-input v-model="copyForm.name" />
            </div>
            <div class="form-item-mock">
              <label>新登录页编码 *</label>
              <el-input v-model="copyForm.code" />
            </div>
            <div class="form-item-mock">
              <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px;">
                <label style="margin-bottom: 0;">适用域类型</label>
                <el-button link type="primary" size="small" style="font-weight: normal; padding: 0; height: auto;" @click="router.push('/realm/type-categories')">管理类型 ↗</el-button>
              </div>
              <el-select
                v-model="copyForm.applicableRealmTypeId"
                placeholder="请选择身份域类型"
                clearable
                :loading="realmTypeOptionsLoading"
                no-data-text="暂无可用身份域类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in realmTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
            <div class="form-item-mock">
              <label>状态</label>
              <el-select v-model="copyForm.status" disabled>
                <el-option label="禁用" :value="3" />
              </el-select>
            </div>
          </div>

          <div class="divider-line"></div>

          <div class="section-title">继承配置项预览</div>
          <p class="section-subtext">默认自动复制父项的配置数据，保存后可以继续进入编辑修改。</p>
          
          <table class="mini-table">
            <thead>
              <tr>
                <th>配置项名称</th>
                <th>继承来源值</th>
                <th>复制副本结果</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>页面标题</td>
                <td>{{ pageToCopy?.pageTitle }}</td>
                <td>{{ pageToCopy?.pageTitle }} (继承)</td>
              </tr>
              <tr>
                <td>登录方式</td>
                <td>{{ getAuthText(pageToCopy?.authMethod) }}</td>
                <td>{{ getAuthText(pageToCopy?.authMethod) }} (继承)</td>
              </tr>
              <tr>
                <td>辅助入口选项</td>
                <td>忘记密码: {{ pageToCopy?.showForgotPassword ? '显示' : '不显示' }}</td>
                <td>忘记密码: {{ pageToCopy?.showForgotPassword ? '显示' : '不显示' }} (继承)</td>
              </tr>
            </tbody>
          </table>
        </div>
      </el-card>
    </div>

    <!-- View 4: Detail & Interactive Live Preview View -->
    <div v-else-if="currentView === 'detail'" class="detail-layout">
      <div class="view-header">
        <div class="header-back-wrap" @click="currentView = 'list'">
          <el-icon class="back-icon"><ArrowLeft /></el-icon>
          <h2>{{ selectedPage?.name }}</h2>
        </div>
        <div class="action-buttons">
          <el-button @click="currentView = 'list'">返回</el-button>
          <el-button type="primary" @click="openEditDrawer(selectedPage!)">编辑</el-button>
        </div>
      </div>

      <section class="detail-summary-card">
        <div class="detail-summary-main">
          <div class="detail-summary-icon"><el-icon><View /></el-icon></div>
          <div class="hero-content">
            <div class="detail-title-line">
              <h3>{{ selectedPage?.name }}</h3>
              <span class="hero-code">{{ selectedPage?.code }}</span>
            </div>
            <p class="hero-subtitle">{{ selectedPage?.description || '暂无登录页备注' }}</p>
            <div class="hero-badges">
              <span class="badge" :class="selectedPage?.status === 1 ? 'green' : 'red'">
                {{ selectedPage?.status === 1 ? '启用' : '禁用' }}
              </span>
              <span class="badge" :class="selectedPage?.defaultPage ? 'blue' : 'gray'">
                {{ selectedPage?.defaultPage ? '默认登录页' : '普通登录页' }}
              </span>
              <span class="badge gray">{{ selectedPage?.systemBuiltin ? '系统内置' : '自定义' }}</span>
            </div>
          </div>
        </div>
        <div class="detail-summary-stats">
          <div class="summary-stat"><strong>{{ selectedPage?.referenceCount ?? 0 }}</strong><span>引用总数</span></div>
          <div class="summary-stat"><strong>{{ selectedPage?.realmReferenceCount ?? 0 }}</strong><span>身份域引用</span></div>
          <div class="summary-stat"><strong>{{ selectedPage?.clientReferenceCount ?? 0 }}</strong><span>客户端引用</span></div>
        </div>
      </section>

      <div class="detail-content-grid">
        <section class="pane-card-box preview-card">
          <div class="pane-header">
            <h4>页面预览</h4>
            <p>根据详情接口返回的标题、背景、Logo 和入口开关渲染展示效果。</p>
          </div>
          <div class="live-simulator">
            <div
              class="login-preview-panel"
              :style="selectedPage?.backgroundUrl ? { backgroundImage: `url(${selectedPage.backgroundUrl})` } : undefined"
            >
              <div class="login-panel-left preview-overlay">
                <img v-if="selectedPage?.logoUrl" :src="selectedPage.logoUrl" alt="登录页 Logo" class="preview-logo" />
                <h3>{{ selectedPage?.pageTitle }}</h3>
                <p>{{ selectedPage?.pageSubtitle }}</p>
                <span class="center-brand-logo">{{ selectedPage?.name }}</span>
              </div>
              <div class="login-panel-right">
                <div class="portal-login-box">
                  <h4>欢迎登录</h4>
                  <p>请选择支持的认证方式完成身份认证</p>
                  
                  <div class="portal-input-field">用户名 / 手机号</div>
                  <div class="portal-input-field" v-if="selectedPage?.defaultAuthMethod === 'password'">请输入登录密码</div>
                  <div class="portal-input-field" v-else>请输入验证码</div>
                  
                  <button class="portal-submit-btn">登录</button>
                  
                  <div class="portal-footer-links">
                    <span v-if="selectedPage?.showForgotPassword" class="footer-link">忘记密码？</span>
                    <span v-if="selectedPage?.showRegister" class="footer-link">立即注册</span>
                    <span v-if="selectedPage?.showThirdPartyLogin" class="footer-link">第三方登录</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section class="pane-card-box">
          <div class="pane-header">
            <h4>基础信息</h4>
            <p>登录页标识、适用范围和系统维护属性。</p>
          </div>
          <div class="metadata-table">
            <div class="meta-row"><span class="lbl">登录页 ID</span><span class="val mono-value">{{ getDisplayText(selectedPage?.id) }}</span></div>
            <div class="meta-row"><span class="lbl">登录页编码</span><span class="val mono-value">{{ getDisplayText(selectedPage?.code) }}</span></div>
            <div class="meta-row"><span class="lbl">登录页名称</span><span class="val">{{ getDisplayText(selectedPage?.name) }}</span></div>
            <div class="meta-row"><span class="lbl">身份域类型</span><span class="val">{{ selectedPage?.applicableRealmTypeName || '通用' }}</span></div>
            <div class="meta-row"><span class="lbl">身份域类型 ID</span><span class="val mono-value">{{ getDisplayText(selectedPage?.applicableRealmTypeId) }}</span></div>
            <div class="meta-row"><span class="lbl">状态</span><span class="val">{{ selectedPage?.status === 1 ? '启用' : '禁用' }}</span></div>
            <div class="meta-row"><span class="lbl">默认登录页</span><span class="val">{{ getBooleanText(selectedPage?.defaultPage) }}</span></div>
            <div class="meta-row"><span class="lbl">系统内置</span><span class="val">{{ getBooleanText(selectedPage?.systemBuiltin) }}</span></div>
            <div class="meta-row"><span class="lbl">创建时间</span><span class="val">{{ getDisplayText(selectedPage?.createTime) }}</span></div>
            <div class="meta-row"><span class="lbl">更新时间</span><span class="val">{{ getDisplayText(selectedPage?.updateTime) }}</span></div>
          </div>
        </section>
      </div>

      <div class="detail-section-grid">
        <section class="pane-card-box">
          <div class="pane-header">
            <h4>页面展示配置</h4>
            <p>页面文案和静态资源地址。</p>
          </div>
          <div class="description-grid">
            <div class="description-item"><span>页面主标题</span><strong>{{ getDisplayText(selectedPage?.pageTitle) }}</strong></div>
            <div class="description-item"><span>页面副标题</span><strong>{{ getDisplayText(selectedPage?.pageSubtitle) }}</strong></div>
            <div class="description-item full-width"><span>Logo 资源地址</span><strong class="break-value">{{ getDisplayText(selectedPage?.logoUrl) }}</strong></div>
            <div class="description-item full-width"><span>背景图资源地址</span><strong class="break-value">{{ getDisplayText(selectedPage?.backgroundUrl) }}</strong></div>
            <div class="description-item full-width"><span>登录成功跳转地址</span><strong class="break-value">{{ getDisplayText(selectedPage?.successRedirectUrl) }}</strong></div>
            <div class="description-item full-width"><span>备注</span><strong>{{ getDisplayText(selectedPage?.description) }}</strong></div>
          </div>
        </section>

        <section class="pane-card-box">
          <div class="pane-header">
            <h4>认证与交互配置</h4>
            <p>支持的认证方式、默认方式和辅助入口开关。</p>
          </div>
          <div class="auth-method-list">
            <span v-for="method in selectedPage?.authMethod || []" :key="method.id" class="auth-method-chip">
              {{ method.name }}<small>{{ method.description }} · ID {{ method.id }}</small>
            </span>
            <span v-if="!selectedPage?.authMethod?.length" class="empty-text">暂无认证方式</span>
          </div>
          <div class="metadata-table compact-metadata">
            <div class="meta-row"><span class="lbl">默认认证方式</span><span class="val">{{ getAuthMethodName(selectedPage?.defaultAuthMethod) }}</span></div>
            <div class="meta-row"><span class="lbl">失败提示模式</span><span class="val">{{ getFailurePromptModeName(selectedPage?.failurePromptMode) }}</span></div>
            <div class="meta-row"><span class="lbl">忘记密码入口</span><span class="val">{{ getBooleanText(selectedPage?.showForgotPassword) }}</span></div>
            <div class="meta-row"><span class="lbl">注册入口</span><span class="val">{{ getBooleanText(selectedPage?.showRegister) }}</span></div>
            <div class="meta-row"><span class="lbl">第三方登录入口</span><span class="val">{{ getBooleanText(selectedPage?.showThirdPartyLogin) }}</span></div>
          </div>
        </section>
      </div>

      <section class="pane-card-box reference-card">
        <div class="pane-header">
          <h4>引用影响范围</h4>
          <p>详情接口返回的身份域、客户端及总引用数量。</p>
        </div>
        <div class="reference-metrics">
          <div class="reference-metric"><span>身份域引用</span><strong>{{ selectedPage?.realmReferenceCount ?? 0 }}</strong></div>
          <div class="reference-metric"><span>客户端引用</span><strong>{{ selectedPage?.clientReferenceCount ?? 0 }}</strong></div>
          <div class="reference-metric total"><span>引用总数</span><strong>{{ selectedPage?.referenceCount ?? 0 }}</strong></div>
          <div class="info-alert-panel">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
            <p>客户端可覆盖身份域默认登录页。修改或禁用前，应先确认引用范围及实际登录影响。</p>
          </div>
        </div>
      </section>
    </div>

    <!-- Drawer: Edit Overlay Drawer -->
    <el-drawer
      v-model="isEditDrawerOpen"
      title="编辑登录页"
      size="680px"
      class="custom-edit-drawer"
      destroy-on-close
    >
      <div class="drawer-body-wrap">
        <p class="drawer-subtitle">在详情页内修改登录页配置，保存后将原地刷新详情数据。</p>
        
        <!-- Warning alert if referenced -->
        <div v-if="drawerForm.referencesCount > 0" class="drawer-referenced-alert">
          <el-icon class="warning-icon"><Warning /></el-icon>
          <p>
            <strong>联动提示：</strong>该页面已经被 <strong>{{ drawerForm.referencesCount }}</strong> 个活跃的身份域引用。更改可能会影响用户的正常访问。
          </p>
        </div>

        <section class="drawer-form-section">
          <div class="drawer-section-title">基础信息</div>
          <div class="drawer-form-grid">
            <div class="form-item-mock">
              <label>登录页名称 *</label>
              <el-input v-model="drawerForm.name" />
            </div>
            <div class="form-item-mock">
              <label>登录页编码</label>
              <el-input v-model="drawerForm.code" disabled class="disabled-field" />
              <div class="hint-msg">登录页编码不可修改。</div>
            </div>
            <div class="form-item-mock drawer-grid-full">
              <div class="drawer-label-line">
                <label>适用身份域类型</label>
                <el-button link type="primary" size="small" @click="router.push('/realm/type-categories')">管理类型 ↗</el-button>
              </div>
              <el-select
                v-model="drawerForm.applicableRealmTypeId"
                placeholder="请选择身份域类型"
                clearable
                :loading="realmTypeOptionsLoading"
                no-data-text="暂无可用身份域类型"
                style="width: 100%"
              >
                <el-option
                  v-for="item in realmTypeOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </div>
            <div class="form-item-mock">
              <label>状态</label>
              <el-select v-model="drawerForm.status">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="2" />
              </el-select>
            </div>
            <div class="form-item-mock">
              <label>默认登录页</label>
              <el-switch v-model="drawerForm.defaultPage" />
            </div>
          </div>
        </section>

        <section class="drawer-form-section">
          <div class="drawer-section-title">页面展示</div>
          <div class="drawer-form-grid">
            <div class="form-item-mock">
              <label>页面标题 *</label>
              <el-input v-model="drawerForm.pageTitle" />
            </div>
            <div class="form-item-mock">
              <label>页面副标题</label>
              <el-input v-model="drawerForm.pageSubtitle" />
            </div>
            <div class="form-item-mock drawer-grid-full">
              <label>Logo 地址</label>
              <el-input v-model="drawerForm.logoUrl" />
            </div>
            <div class="form-item-mock drawer-grid-full">
              <label>背景图地址</label>
              <el-input v-model="drawerForm.backgroundUrl" />
            </div>
            <div class="form-item-mock drawer-grid-full">
              <label>登录成功跳转地址</label>
              <el-input v-model="drawerForm.successRedirectUrl" />
            </div>
          </div>
        </section>

        <section class="drawer-form-section">
          <div class="drawer-section-title">认证与交互</div>
          <div class="form-item-mock">
            <label>支持的登录方式 *</label>
            <el-checkbox-group v-model="drawerForm.authMethods" class="drawer-checkbox-grid">
              <el-checkbox label="password">账号密码</el-checkbox>
              <el-checkbox label="sms">短信验证码</el-checkbox>
              <el-checkbox label="email">邮箱验证码</el-checkbox>
              <el-checkbox label="qr">扫码登录</el-checkbox>
              <el-checkbox label="third_party">第三方登录</el-checkbox>
              <el-checkbox label="mfa">MFA 认证</el-checkbox>
            </el-checkbox-group>
          </div>
          <div class="drawer-form-grid">
            <div class="form-item-mock">
              <label>默认登录方式 *</label>
              <el-select v-model="drawerForm.defaultAuthMethod">
                <el-option
                  v-for="method in drawerForm.authMethods"
                  :key="method"
                  :label="getAuthMethodName(method)"
                  :value="method"
                />
              </el-select>
            </div>
            <div class="form-item-mock">
              <label>登录失败提示模式 *</label>
              <el-select v-model="drawerForm.failurePromptMode">
                <el-option label="消息提示" value="message" />
                <el-option label="弹窗提示" value="dialog" />
                <el-option label="结果页提示" value="page" />
              </el-select>
            </div>
          </div>
          <div class="form-item-mock">
            <label>辅助入口选项</label>
            <div class="checkbox-list">
              <el-checkbox v-model="drawerForm.showForgotPassword">显示忘记密码</el-checkbox>
              <el-checkbox v-model="drawerForm.showRegister">显示注册入口</el-checkbox>
              <el-checkbox v-model="drawerForm.showThirdPartyLogin">显示第三方登录</el-checkbox>
            </div>
          </div>
        </section>

        <section class="drawer-form-section">
          <div class="drawer-section-title">备注信息</div>
          <div class="form-item-mock">
            <label>备注</label>
            <el-input v-model="drawerForm.description" type="textarea" :rows="3" maxlength="512" show-word-limit />
          </div>
        </section>
      </div>
      <template #footer>
        <div class="drawer-footer-buttons">
          <el-button @click="isEditDrawerOpen = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="saveDrawerEdit">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- Modal: Referenced/Conflict modal that blocks Delete -->
    <el-dialog
      v-model="isDeleteModalOpen"
      title="无法删除登录页"
      width="450px"
      align-center
      class="custom-conflict-dialog"
    >
      <div class="dialog-body-container">
        <p class="dialog-desc">
          当前登录页已被以下身份域或应用客户端引用绑定，不能直接物理删除。请先进入绑定对象并完成解绑或登录配置替换。
        </p>
        
        <div class="conflict-scope-box">
          <div class="scope-title">影响范围分析：</div>
          <div class="scope-body">
            <strong>关联身份域：</strong>{{ referencedPageForDelete?.applicableRealmTypeName || '未分类域' }}<br/>
            <strong>关联引用数量：</strong>{{ referencedPageForDelete?.referenceCount }} 个关联项<br/>
            <strong>解除建议：</strong>请先前往对应的身份域管理或应用配置，更换其默认登录页模板。
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer-buttons">
          <el-button @click="isDeleteModalOpen = false">取消</el-button>
          <el-button type="primary" @click="isDeleteModalOpen = false">查看引用对象</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.login-page-module {
  font-family: "Inter", "PingFang SC", "Microsoft YaHei", system-ui, sans-serif;
  color: #334155;
  min-height: 100%;
}

/* View Header */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.title-wrap h1 {
  margin: 0 0 6px 0;
  font-size: 24px;
  font-weight: 700;
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
  font-weight: 600;
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
  font-weight: 700;
  color: #0F172A;
}
.back-icon {
  font-size: 18px;
  color: #64748B;
}

/* Filter Card */
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
  font-weight: 700;
}
.card-header-title p {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* Premium Table Row Link */
.page-name-link {
  color: #0F172A;
  font-weight: 700;
  cursor: pointer;
}
.page-name-link:hover {
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
  font-weight: 600;
  font-size: 13px;
  cursor: pointer;
}
.link-btn:hover {
  color: #1D4ED8;
}
.link-more-btn {
  color: #64748B;
  font-weight: 600;
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
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
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

/* Form layouts */
.form-card-box {
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #fff;
}
.form-card-box :deep(.el-card__body) {
  padding: 0;
}
.form-body-container {
  padding: 24px;
}
.form-section {
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 24px;
}
.form-section:last-of-type {
  border-bottom: 0;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 4px;
}
.section-subtext {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 16px 0;
}
.grid-row-3 {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px 24px;
}
.span-2 {
  grid-column: span 2;
}
.form-body-container :deep(.el-form-item) {
  margin-bottom: 0;
}
.form-body-container :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  padding: 0;
  margin-bottom: 6px;
  line-height: normal;
}
.form-body-container :deep(.el-input__wrapper),
.form-body-container :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: none !important;
}
.hint-msg {
  font-size: 12px;
  color: #64748B;
  margin-top: 6px;
}
.checkbox-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
}
.switch-row {
  display: flex;
  gap: 20px;
  margin-top: 6px;
}

/* Copy Page view banner */
.copy-alert-box {
  background-color: #EFF6FF;
  border: 1px solid #BFDBFE;
  border-radius: 8px;
  padding: 12px 14px;
  color: #1E40AF;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}
.alert-icon {
  font-size: 18px;
}
.divider-line {
  height: 1px;
  background-color: #E5E7EB;
  margin: 20px 0;
}
.form-item-mock {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-item-mock label {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}
.form-item-mock :deep(.el-input__wrapper),
.form-item-mock :deep(.el-select__wrapper) {
  height: 36px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  box-shadow: none !important;
}
.disabled-field :deep(.el-input__wrapper) {
  background-color: #F8FAFC;
}

/* Mini Table */
.mini-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
  font-size: 13px;
}
.mini-table th {
  height: 38px;
  background-color: #F8FAFC;
  text-align: left;
  padding: 0 12px;
  color: #64748B;
  font-weight: 700;
  border-bottom: 1px solid #E5E7EB;
}
.mini-table td {
  height: 48px;
  padding: 0 12px;
  border-bottom: 1px solid #E5E7EB;
  color: #334155;
}

/* Premium Table Header Background Style */
.premium-table :deep(th.el-table__cell) {
  background-color: #F8FAFC !important;
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

/* Detail view specific */
.detail-summary-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 28px;
  padding: 24px;
  background:
    radial-gradient(circle at 88% 12%, rgba(37, 99, 235, 0.12), transparent 30%),
    linear-gradient(135deg, #FFFFFF 0%, #F8FAFF 100%);
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  margin-bottom: 16px;
}
.detail-summary-main {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  min-width: 0;
}
.detail-summary-icon {
  width: 44px;
  height: 44px;
  flex: 0 0 auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #2563EB;
  font-size: 20px;
  background-color: #EFF6FF;
  border: 1px solid #BFDBFE;
  border-radius: 10px;
}
.detail-title-line {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.detail-title-line h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #0F172A;
}
.hero-code {
  font-size: 14px;
  color: #475569;
  font-weight: 600;
  padding: 3px 8px;
  background-color: #F1F5F9;
  border-radius: 4px;
}
.hero-subtitle {
  margin: 8px 0 14px 0;
  font-size: 13px;
  color: #64748B;
  line-height: 1.6;
}
.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.detail-summary-stats {
  display: flex;
  flex: 0 0 auto;
  align-items: stretch;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.82);
}
.summary-stat {
  min-width: 106px;
  padding: 13px 16px;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  border-right: 1px solid #E2E8F0;
}
.summary-stat:last-child {
  border-right: 0;
}
.summary-stat strong {
  color: #0F172A;
  font-size: 20px;
  line-height: 1;
}
.summary-stat span {
  color: #64748B;
  font-size: 12px;
}

.detail-content-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.detail-section-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}
.pane-card-box {
  background-color: #fff;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 20px;
}
.pane-header {
  border-bottom: 1px solid #F1F5F9;
  padding-bottom: 12px;
  margin-bottom: 16px;
}
.pane-header h4 {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #0F172A;
  font-weight: 700;
}
.pane-header p {
  margin: 0;
  font-size: 13px;
  color: #64748B;
}

/* Live Simulator Panel */
.live-simulator {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 12px 0;
}
.login-preview-panel {
  width: 100%;
  max-width: 600px;
  height: 320px;
  border: 1px solid #E5E7EB;
  border-radius: 10px;
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  overflow: hidden;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.05);
  background-position: center;
  background-size: cover;
}
.login-panel-left {
  background: linear-gradient(135deg, #0F1B2D, #1E3A8A);
  color: #fff;
  padding: 28px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.preview-overlay {
  background: linear-gradient(145deg, rgba(15, 27, 45, 0.96), rgba(30, 58, 138, 0.9));
}
.preview-logo {
  width: 42px;
  height: 42px;
  object-fit: contain;
  margin-bottom: 18px;
  border-radius: 8px;
}
.login-panel-left h3 {
  font-size: 20px;
  margin: 0 0 8px 0;
  font-weight: 700;
}
.login-panel-left p {
  font-size: 12px;
  color: #CBD5E1;
  line-height: 1.6;
  margin: 0;
}
.center-brand-logo {
  font-size: 11px;
  color: #AFC3E8;
  margin-top: 60px;
}
.login-panel-right {
  background-color: #fff;
  padding: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.portal-login-box {
  width: 100%;
}
.portal-login-box h4 {
  font-size: 18px;
  color: #0F172A;
  margin: 0 0 6px 0;
  font-weight: 700;
}
.portal-login-box p {
  margin: 0 0 14px 0;
  font-size: 12px;
  color: #64748B;
}
.portal-input-field {
  height: 34px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  color: #94A3B8;
  font-size: 12px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
.portal-submit-btn {
  height: 34px;
  width: 100%;
  border-radius: 6px;
  background-color: #2563EB;
  border: none;
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  margin-top: 8px;
}
.portal-footer-links {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
}
.footer-link {
  color: #2563EB;
  cursor: pointer;
}

/* Metadata property details */
.metadata-table {
  display: flex;
  flex-direction: column;
}
.meta-row {
  display: flex;
  height: 44px;
  align-items: center;
  border-bottom: 1px solid #E5E7EB;
}
.meta-row:last-child {
  border-bottom: 0;
}
.meta-row .lbl {
  width: 140px;
  font-size: 13px;
  color: #64748B;
}
.meta-row .val {
  min-width: 0;
  overflow-wrap: anywhere;
  font-size: 13px;
  color: #334155;
  font-weight: 600;
}
.mono-value {
  font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
}
.description-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.description-item {
  min-width: 0;
  min-height: 74px;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  background-color: #F8FAFC;
}
.description-item.full-width {
  grid-column: 1 / -1;
}
.description-item span {
  color: #64748B;
  font-size: 12px;
}
.description-item strong {
  color: #1E293B;
  font-size: 13px;
  font-weight: 600;
  line-height: 1.6;
}
.break-value {
  overflow-wrap: anywhere;
}
.auth-method-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.auth-method-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 9px;
  color: #1D4ED8;
  font-size: 13px;
  font-weight: 600;
  border: 1px solid #BFDBFE;
  border-radius: 6px;
  background-color: #EFF6FF;
}
.auth-method-chip small {
  color: #64748B;
  font-size: 11px;
  font-weight: 500;
}
.empty-text {
  color: #94A3B8;
  font-size: 13px;
}
.compact-metadata {
  border-top: 1px solid #E5E7EB;
}
.reference-card {
  margin-bottom: 16px;
}
.reference-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(130px, 180px)) minmax(280px, 1fr);
  gap: 12px;
  align-items: stretch;
}
.reference-metric {
  min-height: 76px;
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  background-color: #F8FAFC;
}
.reference-metric.total {
  border-color: #BFDBFE;
  background-color: #EFF6FF;
}
.reference-metric span {
  color: #64748B;
  font-size: 12px;
}
.reference-metric strong {
  color: #0F172A;
  font-size: 22px;
}
.info-alert-panel {
  display: flex;
  gap: 10px;
  background-color: #F8FAFC;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  padding: 10px 12px;
}
.info-icon {
  font-size: 18px;
  color: #2563EB;
  margin-top: 2px;
}
.info-alert-panel p {
  margin: 0;
  font-size: 12px;
  color: #64748B;
  line-height: 1.6;
}

/* Drawer formatting overrides */
.drawer-subtitle {
  font-size: 13px;
  color: #64748B;
  margin: 0 0 16px 0;
}
.drawer-referenced-alert {
  background-color: #FEF2F2;
  border: 1px solid #FECACA;
  border-radius: 8px;
  padding: 12px;
  color: #991B1B;
  font-size: 13px;
  line-height: 1.7;
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.drawer-referenced-alert .warning-icon {
  font-size: 18px;
  color: #DC2626;
  margin-top: 2px;
}
.drawer-referenced-alert p {
  margin: 0;
}
.drawer-body-wrap {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.drawer-form-section {
  padding: 16px;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  background-color: #FFFFFF;
}
.drawer-section-title {
  margin-bottom: 14px;
  color: #0F172A;
  font-size: 14px;
  font-weight: 700;
}
.drawer-form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.drawer-grid-full {
  grid-column: 1 / -1;
}
.drawer-label-line {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.drawer-label-line :deep(.el-button) {
  height: auto;
  padding: 0;
  font-weight: 500;
}
.drawer-checkbox-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px 12px;
}
.drawer-checkbox-grid :deep(.el-checkbox) {
  margin-right: 0;
}
.drawer-form-section .checkbox-list {
  flex-direction: row;
  flex-wrap: wrap;
}
.drawer-footer-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

/* Dialog style overrides */
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
  background-color: #F8FAFC;
  border: 1px solid #E5E7EB;
  border-radius: 8px;
  padding: 14px;
}
.scope-title {
  font-size: 13px;
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 6px;
}
.scope-body {
  font-size: 13px;
  color: #475569;
  line-height: 1.8;
}
.dialog-footer-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
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

.name-text-detail {
  font-weight: 700;
  color: #0F172A;
  cursor: pointer;
  transition: color 0.15s ease;
}
.name-text-detail:hover {
  color: #2563EB;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #E5E7EB;
  background-color: #fff;
}
.total-text {
  font-size: 13px;
  color: #64748B;
}

@media (max-width: 1024px) {
  .detail-summary-card {
    align-items: flex-start;
    flex-direction: column;
  }
  .detail-content-grid,
  .detail-section-grid {
    grid-template-columns: 1fr;
  }
  .reference-metrics {
    grid-template-columns: repeat(3, 1fr);
  }
  .reference-metrics .info-alert-panel {
    grid-column: 1 / -1;
  }
}

@media (max-width: 640px) {
  .detail-summary-stats,
  .description-grid,
  .reference-metrics {
    width: 100%;
    grid-template-columns: 1fr;
  }
  .detail-summary-stats {
    display: grid;
  }
  .summary-stat {
    border-right: 0;
    border-bottom: 1px solid #E2E8F0;
  }
  .summary-stat:last-child {
    border-bottom: 0;
  }
  .description-item.full-width,
  .reference-metrics .info-alert-panel {
    grid-column: auto;
  }
  .login-preview-panel {
    height: auto;
    grid-template-columns: 1fr;
  }
  .drawer-form-grid,
  .drawer-checkbox-grid {
    grid-template-columns: 1fr;
  }
  .drawer-grid-full {
    grid-column: auto;
  }
}
</style>
