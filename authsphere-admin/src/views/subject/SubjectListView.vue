<script setup lang="ts">
import {
  ArrowDown,
  ArrowLeft,
  Calendar,
  CircleCheck,
  Connection,
  DocumentCopy,
  Download,
  Key,
  Link,
  Lock,
  Monitor,
  MoreFilled,
  OfficeBuilding,
  Plus,
  Refresh,
  Search,
  Setting,
  User,
  UserFilled,
  View,
  Warning,
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { type PageResult as RealmPageResult, type RealmRecord, realmApi } from '@/api/realm'
import { type PageResult, type SubjectPayload, type SubjectRecord, subjectApi } from '@/api/subject'
import { type SubjectTypeRecord, subjectTypeApi } from '@/api/subjectType'
import { showErrorMessage, showSuccessMessage } from '@/utils/feedback'

import SubjectMembersTab from './components/SubjectMembersTab.vue'

const STATUS_NORMAL = 1
const STATUS_DISABLED = 2

const loading = ref(false)
const optionLoading = ref(false)
const saveLoading = ref(false)

// Detail & Drawers state
const detailVisible = ref(false)
const currentDetail = ref<SubjectRecord>()
const activeDetailTab = ref('basic')

// Add / Edit Subject Drawer
const isSubjectDrawerOpen = ref(false)
const subjectDrawerMode = ref<'create' | 'edit'>('create')
const editingId = ref<string>()
const subjectFormRef = ref<FormInstance>()

const subjectForm = reactive<SubjectPayload>({
  subjectTypeId: '',
  realmId: '',
  code: '',
  name: '',
  rootSubjectId: null,
  parentSubjectId: null,
  isRoot: 0,
  builtIn: 0,
  description: '',
})

// Open App Drawer
const isOpenAppDrawerOpen = ref(false)
const openAppForm = reactive({
  subjectId: '',
  subjectName: '',
  appId: '1',
  instanceName: '租户A商城实例',
  instanceCode: 'tenant_a_mall',
  expireTime: '',
  status: 1,
  initClient: true,
  initResource: true,
  initRole: true
})

const query = reactive({
  page: 1,
  size: 10,
  name: '',
  code: '',
  realmId: undefined as string | undefined,
  subjectTypeId: undefined as string | undefined,
  status: undefined as number | undefined,
})

const tableData = ref<SubjectRecord[]>([])
const total = ref(0)
const realmOptions = ref<RealmRecord[]>([])
const subjectTypeOptions = ref<SubjectTypeRecord[]>([])
const subjectOptions = ref<SubjectRecord[]>([])

const availableSubjectOptions = computed(() =>
  subjectOptions.value.filter((item) => item.id !== editingId.value),
)

const rules: FormRules<SubjectPayload> = {
  subjectTypeId: [{ required: true, message: '请选择主体类型', trigger: 'change' }],
  realmId: [{ required: true, message: '请选择身份域', trigger: 'change' }],
  code: [
    { required: true, message: '请输入主体编码', trigger: 'blur' },
    {
      pattern: /^[a-z][a-z0-9-_]{1,63}$/,
      message: '编码需以小写字母开头，仅支持小写字母、数字、短横线和下划线',
      trigger: 'blur',
    },
  ],
  name: [{ required: true, message: '请输入主体名称', trigger: 'blur' }],
}

const getErrorMessage = (error: unknown, fallback = '操作失败') => {
  if (error instanceof Error && error.message) {
    return error.message
  }
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = (error as { message?: unknown }).message
    if (typeof message === 'string' && message) {
      return message
    }
  }
  return fallback
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await subjectApi.page({
      page: query.page,
      size: query.size,
      name: query.name || undefined,
      code: query.code || undefined,
      realmId: query.realmId,
      subjectTypeId: query.subjectTypeId,
      status: query.status,
    })
    tableData.value = result.records ?? []
    total.value = Number(result.total ?? 0)
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体列表失败'))
  } finally {
    loading.value = false
  }
}

const fetchOptions = async () => {
  optionLoading.value = true
  try {
    const [realmPage, subjectTypes, subjects] = await Promise.all([
      realmApi.page({ page: 1, size: 100 }) as Promise<RealmPageResult<RealmRecord>>,
      subjectTypeApi.list(),
      subjectApi.list(),
    ])
    realmOptions.value = realmPage.records ?? []
    subjectTypeOptions.value = subjectTypes ?? []
    subjectOptions.value = subjects ?? []
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体表单选项失败'))
  } finally {
    optionLoading.value = false
  }
}

const resetQuery = () => {
  query.page = 1
  query.name = ''
  query.code = ''
  query.realmId = undefined
  query.subjectTypeId = undefined
  query.status = undefined
  fetchData()
}

const getSubjectTypeTag = (row: SubjectRecord): { text: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' } => {
  const typeCode = row.subjectTypeCode?.toLowerCase() || ''
  if (typeCode.includes('platform') || typeCode.includes('admin')) {
    return { text: '平台', type: 'info' }
  } else if (typeCode.includes('tenant')) {
    return { text: '租户', type: 'primary' }
  } else if (typeCode.includes('merchant')) {
    return { text: '商户', type: 'warning' }
  } else if (typeCode.includes('provider') || typeCode.includes('service')) {
    return { text: '服务商', type: 'info' }
  } else {
    return { text: '消费者', type: 'info' }
  }
}

const defaultForm = (): SubjectPayload => ({
  subjectTypeId: '',
  realmId: '',
  code: '',
  name: '',
  rootSubjectId: null,
  parentSubjectId: null,
  isRoot: 0,
  builtIn: 0,
  description: '',
})

const openCreate = () => {
  router.push('/subjects/create')
}

const openEdit = async (row: SubjectRecord) => {
  subjectDrawerMode.value = 'edit'
  editingId.value = row.id
  loading.value = true
  try {
    const detail = await subjectApi.detail(row.id)
    Object.assign(subjectForm, {
      subjectTypeId: detail.subjectTypeId,
      realmId: detail.realmId,
      code: detail.code,
      name: detail.name,
      rootSubjectId: detail.rootSubjectId || null,
      parentSubjectId: detail.parentSubjectId || null,
      isRoot: Number(detail.isRoot ?? 0),
      builtIn: Number(detail.builtIn ?? 0),
      description: detail.description || '',
    })
    isSubjectDrawerOpen.value = true
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '获取主体详情失败'))
  } finally {
    loading.value = false
  }
}

const openOpenAppDrawer = (row: SubjectRecord) => {
  openAppForm.subjectId = row.id
  openAppForm.subjectName = row.name
  openAppForm.instanceName = `${row.name}商城实例`
  openAppForm.instanceCode = `${row.code}_mall`
  isOpenAppDrawerOpen.value = true
}

const submitOpenApp = () => {
  saveLoading.value = true
  setTimeout(() => {
    saveLoading.value = false
    isOpenAppDrawerOpen.value = false
    ElMessage.success('应用开通成功！已经为您自动初始化客户端、复制资源模板和配置角色。')
  }, 800)
}

const route = useRoute()
const router = useRouter()

const openDetail = (row: SubjectRecord) => {
  router.push({ path: `/subjects/detail/${row.id}` })
}

const goBack = () => {
  router.push('/subjects')
}

watch(
  () => route.query.subjectId,
  async (newId) => {
    if (newId && typeof newId === 'string') {
      loading.value = true
      try {
        currentDetail.value = await subjectApi.detail(newId)
        detailVisible.value = true
        activeDetailTab.value = 'basic'
      } catch (error) {
        showErrorMessage(getErrorMessage(error, '获取主体详情失败'))
        currentDetail.value = undefined
        detailVisible.value = false
      } finally {
        loading.value = false
      }
    } else {
      currentDetail.value = undefined
      detailVisible.value = false
    }
  },
  { immediate: true },
)

const submitSubjectForm = async () => {
  if (!subjectFormRef.value) return
  const valid = await subjectFormRef.value.validate().catch(() => false)
  if (!valid) {
    showErrorMessage('请先完善必填项')
    return
  }

  saveLoading.value = true
  try {
    const payload = {
      ...subjectForm,
      rootSubjectId: subjectForm.rootSubjectId || null,
      parentSubjectId: subjectForm.parentSubjectId || null,
      isRoot: Number(subjectForm.isRoot),
      builtIn: Number(subjectForm.builtIn),
    }
    if (subjectDrawerMode.value === 'create') {
      await subjectApi.create(payload)
    } else if (editingId.value) {
      await subjectApi.update(editingId.value, payload)
    }
    showSuccessMessage(subjectDrawerMode.value === 'create' ? '主体已创建' : '主体已更新')
    isSubjectDrawerOpen.value = false
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '保存主体失败'))
  } finally {
    saveLoading.value = false
  }
}

const toggleStatus = async (row: SubjectRecord) => {
  const action = row.status === STATUS_NORMAL ? '禁用' : '启用'
  await ElMessageBox.confirm(
    `${action}后会影响该主体进入系统和参与授权边界计算。确认${action}？`,
    `${action}主体`,
    {
      type: row.status === STATUS_NORMAL ? 'warning' : 'info',
      confirmButtonText: action,
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.toggleStatus(row.id)
    showSuccessMessage(`主体已${action}`)
    if (currentDetail.value?.id === row.id) {
      currentDetail.value.status = currentDetail.value.status === STATUS_NORMAL ? STATUS_DISABLED : STATUS_NORMAL
    }
    fetchData()
    fetchOptions()
  } catch (error) {
    showErrorMessage(getErrorMessage(error, `${action}主体失败`))
  }
}

const removeSubject = async (row: SubjectRecord) => {
  if (Number(row.builtIn) === 1) {
    showErrorMessage('系统内置主体不能删除')
    return
  }

  await ElMessageBox.confirm(
    `删除后主体「${row.name}」将不可恢复。确认删除？`,
    '删除主体',
    {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    },
  )

  try {
    await subjectApi.remove(row.id)
    showSuccessMessage('主体已删除')
    if (currentDetail.value?.id === row.id) {
      goBack()
    } else {
      fetchData()
      fetchOptions()
    }
  } catch (error) {
    showErrorMessage(getErrorMessage(error, '删除主体失败'))
  }
}

const copyText = (text?: string) => {
  if (!text) return
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('复制成功')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

const mockOperations = ref([
  {
    time: '2026-06-08 15:42:10',
    operator: 'admin',
    type: '编辑主体',
    content: '更新主体描述信息为: 租户A 作为业务主体，承载商城、支付、仓储等应用实例和数据边界。',
    source: '控制台',
  },
  {
    time: '2026-06-08 10:15:33',
    operator: 'admin',
    type: '开通应用实例',
    content: '新增应用实例: 租户A商城实例 (tenant_a_mall)',
    source: '控制台',
  },
  {
    time: '2026-06-08 09:30:22',
    operator: 'admin',
    type: '新增关系',
    content: '绑定父级主体关系: 平台主体 (platform) 为上级管理主体',
    source: '控制台',
  },
])

onMounted(() => {
  fetchData()
  fetchOptions()
})
</script>

<template>
  <section class="subject-page">
    <!-- View 1: Detail View -->
    <template v-if="detailVisible && currentDetail">
      <div class="subject-detail-view">
        <div class="top-breadcrumb">
          <span class="crumb-back" @click="goBack">← 返回主体列表</span>
          <span class="divider">/</span>
          <span class="crumb-curr">主体详情 - {{ currentDetail.name }}</span>
        </div>

        <!-- Hero block -->
        <div class="premium-hero-card">
          <div class="hero-left-section">
            <div class="subject-icon-box">
              <span>{{ currentDetail.name?.slice(0, 1) || '租' }}</span>
            </div>
            <div class="subject-meta-info">
              <div class="title-row">
                <h2>{{ currentDetail.name }}</h2>
                <span class="custom-badge" :class="currentDetail.status === STATUS_NORMAL ? 'green' : 'red'">
                  {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                </span>
              </div>
              <div class="meta-row">
                <span>主体编码：<code class="code-text">{{ currentDetail.code }}</code></span>
                <span class="dot-separator">|</span>
                <span>主体类型：{{ currentDetail.subjectTypeName || '租户' }}</span>
                <span class="dot-separator">|</span>
                <span>所属身份域：{{ currentDetail.realmCode || 'tenant_realm' }}</span>
                <span class="dot-separator">|</span>
                <span>父级主体：{{ currentDetail.parentSubjectName || '平台主体' }}</span>
              </div>
            </div>
          </div>
          <div class="hero-right-actions">
            <el-button @click="openEdit(currentDetail)">编辑</el-button>
            <el-button type="primary" @click="openOpenAppDrawer(currentDetail)">开通应用</el-button>
            <el-button :type="currentDetail.status === STATUS_NORMAL ? 'danger' : 'success'" plain @click="toggleStatus(currentDetail)">
              {{ currentDetail.status === STATUS_NORMAL ? '禁用' : '启用' }}
            </el-button>
          </div>
        </div>

        <!-- Tabs Navigation -->
        <div class="detail-tabs-wrapper">
          <el-tabs v-model="activeDetailTab" class="custom-detail-tabs">
            <!-- Tab 1: Basic Info -->
            <el-tab-pane label="基础信息" name="basic">
              <div class="basic-grid-layout">
                <!-- Left: Attributes -->
                <div class="pane-card">
                  <div class="pane-header">
                    <h3>基础信息</h3>
                    <el-button size="small" @click="openEdit(currentDetail!)">编辑基础信息</el-button>
                  </div>
                  <div class="info-grid-2col">
                    <div class="info-item">
                      <label>主体名称</label>
                      <span>{{ currentDetail.name }}</span>
                    </div>
                    <div class="info-item">
                      <label>主体编码</label>
                      <span class="code-text">{{ currentDetail.code }}</span>
                    </div>
                    <div class="info-item">
                      <label>主体类型</label>
                      <span>{{ currentDetail.subjectTypeName || '租户' }}</span>
                    </div>
                    <div class="info-item">
                      <label>所属身份域</label>
                      <span>{{ currentDetail.realmCode || 'tenant_realm' }}</span>
                    </div>
                    <div class="info-item">
                      <label>父级主体</label>
                      <span>{{ currentDetail.parentSubjectName || '平台主体' }}</span>
                    </div>
                    <div class="info-item">
                      <label>状态</label>
                      <span class="custom-badge" :class="currentDetail.status === STATUS_NORMAL ? 'green' : 'red'">
                        {{ currentDetail.status === STATUS_NORMAL ? '启用' : '禁用' }}
                      </span>
                    </div>
                    <div class="info-item full-width">
                      <label>备注</label>
                      <span class="desc-text">{{ currentDetail.description || '租户A 作为业务主体，承载商城、支付、仓储等应用实例和数据边界。' }}</span>
                    </div>
                  </div>
                </div>

                <!-- Right: Note & Flow Diagram -->
                <div class="pane-card">
                  <div class="pane-header">
                    <h3>关系说明</h3>
                  </div>
                  <div class="note-box">
                    父子主体只表达管理归属，不等于自动继承权限。账号加入主体后仍需在“应用实例 + 客户端”下分配角色。
                  </div>
                  <div class="visual-flow-container">
                    <div class="flow-step">
                      <strong class="step-num">1</strong>
                      <span>账号</span>
                      <small>lisi</small>
                    </div>
                    <div class="flow-arrow">→</div>
                    <div class="flow-step">
                      <strong class="step-num bg-purple">2</strong>
                      <span>加入主体</span>
                      <small>{{ currentDetail.name }}</small>
                    </div>
                    <div class="flow-arrow">→</div>
                    <div class="flow-step">
                      <strong class="step-num bg-green">3</strong>
                      <span>开通应用</span>
                      <small>商城实例</small>
                    </div>
                    <div class="flow-arrow">→</div>
                    <div class="flow-step">
                      <strong class="step-num">4</strong>
                      <span>访问入口</span>
                      <small>商城平台端</small>
                    </div>
                    <div class="flow-arrow">→</div>
                    <div class="flow-step text-orange">
                      <strong class="step-num bg-orange">5</strong>
                      <span>分配角色</span>
                      <small>商城管理员</small>
                    </div>
                    <div class="flow-arrow">→</div>
                    <div class="flow-step">
                      <strong class="step-num bg-blue">6</strong>
                      <span>返回权限</span>
                      <small>菜单/按钮/接口</small>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Bottom Split: Members & Apps Summary -->
              <div class="basic-grid-layout" style="margin-top: 16px;">
                <div class="pane-card">
                  <div class="pane-header">
                    <h3>成员账号</h3>
                    <el-button type="primary" size="small" @click="activeDetailTab = 'members'">添加成员</el-button>
                  </div>
                  <div class="summary-list-box">
                    <div class="summary-list-item">
                      <div class="item-left">
                        <strong>lisi</strong>
                        <small>成员类型：管理员 · 状态：启用</small>
                      </div>
                      <div class="item-ops">
                        <span class="link-btn-text" @click="activeDetailTab = 'members'">角色</span>
                        <span class="link-btn-text danger" @click="activeDetailTab = 'members'">移除</span>
                      </div>
                    </div>
                    <div class="summary-list-item">
                      <div class="item-left">
                        <strong>tenant_ops</strong>
                        <small>成员类型：成员 · 状态：启用</small>
                      </div>
                      <div class="item-ops">
                        <span class="link-btn-text" @click="activeDetailTab = 'members'">角色</span>
                        <span class="link-btn-text danger" @click="activeDetailTab = 'members'">移除</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="pane-card">
                  <div class="pane-header">
                    <h3>应用权限</h3>
                    <el-button type="primary" size="small" @click="openOpenAppDrawer(currentDetail!)">开通应用</el-button>
                  </div>
                  <div class="summary-list-box">
                    <div class="summary-app-card">
                      <div class="app-left">
                        <h4>{{ currentDetail.name }}商城实例</h4>
                        <p>应用：商城应用 · 编码：tenant_a_mall</p>
                        <div class="pills-row">
                          <span class="pill-badge">商城平台端</span>
                          <span class="pill-badge">商家后台</span>
                          <span class="pill-badge">消费者端</span>
                        </div>
                      </div>
                      <span class="custom-badge green">启用</span>
                    </div>

                    <div class="summary-app-card" style="margin-top: 10px;">
                      <div class="app-left">
                        <h4>{{ currentDetail.name }}支付实例</h4>
                        <p>应用：支付应用 · 编码：tenant_a_payment</p>
                        <div class="pills-row">
                          <span class="pill-badge">支付平台端</span>
                          <span class="pill-badge">支付开放 API</span>
                        </div>
                      </div>
                      <span class="custom-badge green">启用</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Tab 2: Members Tab Component -->
            <el-tab-pane label="成员账号" name="members">
              <SubjectMembersTab
                v-if="activeDetailTab === 'members'"
                :subject-id="currentDetail.id"
                :realm-code="currentDetail.realmCode"
              />
            </el-tab-pane>

            <!-- Tab 3: Open Apps -->
            <el-tab-pane label="应用权限" name="apps">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>应用权限</h3>
                  <el-button type="primary" @click="openOpenAppDrawer(currentDetail!)">开通应用</el-button>
                </div>
                <div class="apps-grid-box">
                  <div class="summary-app-card full-card">
                    <div class="app-left">
                      <h4>{{ currentDetail.name }}商城实例</h4>
                      <p>应用：商城应用 · 编码：tenant_a_mall</p>
                      <div class="pills-row">
                        <span class="pill-badge">商城平台端</span>
                        <span class="pill-badge">商家后台</span>
                        <span class="pill-badge">消费者端</span>
                      </div>
                    </div>
                    <div class="app-right-col">
                      <span class="custom-badge green">启用</span>
                      <span class="link-btn-text" @click="copyText('tenant_a_mall')" style="margin-top: 8px;">复制编码</span>
                    </div>
                  </div>

                  <div class="summary-app-card full-card">
                    <div class="app-left">
                      <h4>{{ currentDetail.name }}支付实例</h4>
                      <p>应用：支付应用 · 编码：tenant_a_payment</p>
                      <div class="pills-row">
                        <span class="pill-badge">支付平台端</span>
                        <span class="pill-badge">支付开放 API</span>
                      </div>
                    </div>
                    <div class="app-right-col">
                      <span class="custom-badge green">启用</span>
                      <span class="link-btn-text" @click="copyText('tenant_a_payment')" style="margin-top: 8px;">复制编码</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Tab 4: Sub subjects Tree-Table -->
            <el-tab-pane label="子主体" name="relation">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>子主体</h3>
                  <el-button type="primary" @click="openCreate">新增子主体</el-button>
                </div>

                <div class="tree-table-shell">
                  <div class="tree-row head">
                    <div></div>
                    <div>主体名称</div>
                    <div>主体类型</div>
                    <div>状态</div>
                    <div>操作</div>
                  </div>
                  
                  <!-- Row 1 -->
                  <div class="tree-row">
                    <div class="toggle-icon">▾</div>
                    <div><strong>{{ currentDetail.name }}</strong> <code class="code-text">{{ currentDetail.code }}</code></div>
                    <div><span class="custom-badge blue">租户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text" @click="openCreate">新增子级</span>
                    </div>
                  </div>

                  <!-- Row 2 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">├─ 商户W <code class="code-text">merchant_w</code></div>
                    <div><span class="custom-badge orange">商户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>

                  <!-- Row 3 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">├─ 私域商户M <code class="code-text">merchant_m</code></div>
                    <div><span class="custom-badge orange">商户</span></div>
                    <div><span class="custom-badge green">启用</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>

                  <!-- Row 4 -->
                  <div class="tree-row indent">
                    <div class="toggle-icon"></div>
                    <div class="nested-text">└─ 普通消费者主体 <code class="code-text">consumer_libai</code></div>
                    <div><span class="custom-badge gray">消费者</span></div>
                    <div><span class="custom-badge gray">普通</span></div>
                    <div class="item-ops">
                      <span class="link-btn-text">详情</span>
                      <span class="link-btn-text">编辑</span>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <!-- Tab 5: Audit Log -->
            <el-tab-pane label="审计日志" name="audit">
              <div class="pane-card" style="min-height: 400px;">
                <div class="pane-header">
                  <h3>审计合规性检查记录</h3>
                </div>
                <el-table :data="mockOperations" size="default" class="premium-table">
                  <el-table-column prop="time" label="操作时间" width="180" />
                  <el-table-column prop="operator" label="操作人" width="120" />
                  <el-table-column prop="type" label="操作类型" width="140">
                    <template #default="{ row }">
                      <el-tag size="small" :type="row.type.includes('编辑') ? 'primary' : 'success'">{{ row.type }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="操作内容" min-width="300" />
                  <el-table-column prop="source" label="来源" width="120" />
                </el-table>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </template>

    <!-- View 2: List View -->
    <template v-else>
      <div class="view-header">
        <div class="title-wrap">
          <h1>主体管理</h1>
          <p>主体用于定义业务归属、父子管理关系和应用开通边界，账号加入主体后才可代表该主体操作。</p>
        </div>
        <div class="action-buttons">
          <el-button :icon="Refresh" @click="fetchData" :loading="loading">刷新</el-button>
          <el-button type="primary" :icon="Plus" @click="openCreate">新增主体</el-button>
        </div>
      </div>

      <!-- Filters -->
      <el-card shadow="never" class="filter-card">
        <div class="filter-flex-row">
          <div class="filter-item">
            <label>主体名称 / 编码</label>
            <el-input v-model="query.name" placeholder="请输入名称或编码" clearable @keyup.enter="fetchData" />
          </div>
          <div class="filter-item">
            <label>主体类型</label>
            <el-select v-model="query.subjectTypeId" placeholder="全部类型" clearable>
              <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>所属身份域</label>
            <el-select v-model="query.realmId" placeholder="全部身份域" clearable>
              <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </div>
          <div class="filter-item">
            <label>状态</label>
            <el-select v-model="query.status" placeholder="全部状态" clearable>
              <el-option label="启用" :value="STATUS_NORMAL" />
              <el-option label="禁用" :value="STATUS_DISABLED" />
            </el-select>
          </div>
          <div class="filter-buttons">
            <el-button type="primary" @click="fetchData" :loading="loading">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>
        </div>
      </el-card>

      <!-- Main Table -->
      <el-card shadow="never" class="table-card">
        <div class="card-header-title">
          <h3>主体列表</h3>
          <p>只展示识别字段、归属字段、状态字段和高频操作。</p>
        </div>

        <el-table v-loading="loading" :data="tableData" class="premium-table">
          <el-table-column label="主体名称" min-width="150">
            <template #default="{ row }">
              <span class="subject-name-link" @click="openDetail(row)">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column label="主体编码" min-width="130">
            <template #default="{ row }">
              <code class="code-text">{{ row.code }}</code>
            </template>
          </el-table-column>
          <el-table-column label="主体类型" min-width="110">
            <template #default="{ row }">
              <el-tag :type="getSubjectTypeTag(row).type" effect="light" size="small">
                {{ getSubjectTypeTag(row).text }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="所属身份域" min-width="150">
            <template #default="{ row }">
              {{ row.realmName || row.realmCode || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="父级主体" min-width="140">
            <template #default="{ row }">
              {{ row.parentSubjectName || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <span class="custom-badge" :class="row.status === STATUS_NORMAL ? 'green' : 'red'">
                {{ row.status === STATUS_NORMAL ? '启用' : '禁用' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="220" fixed="right">
            <template #default="{ row }">
              <div class="row-actions">
                <span class="link-btn" @click="openDetail(row)">详情</span>
                <span class="link-btn" @click="openEdit(row)">编辑</span>
                <span class="link-btn" @click="openOpenAppDrawer(row)">开通应用</span>
                <el-dropdown trigger="click">
                  <span class="link-more-btn">更多 <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item @click="toggleStatus(row)">
                        {{ row.status === STATUS_NORMAL ? '禁用' : '启用' }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        :disabled="Number(row.builtIn) === 1"
                        class="danger-item"
                        divided
                        @click="removeSubject(row)"
                      >
                        删除主体
                      </el-dropdown-item>
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
            v-model:current-page="query.page"
            v-model:page-size="query.size"
            :page-sizes="[10, 20, 50, 100]"
            layout="prev, pager, next, sizes, jumper"
            :total="total"
            @change="fetchData"
          />
        </div>
      </el-card>
    </template>

    <!-- Drawer 1: Create / Edit Subject Drawer (Design Drawer Layout) -->
    <el-drawer
      v-model="isSubjectDrawerOpen"
      :title="subjectDrawerMode === 'create' ? '新增主体' : '编辑主体'"
      size="640px"
      destroy-on-close
    >
      <div class="drawer-main-content">
        <el-form
          id="subject-form"
          ref="subjectFormRef"
          v-loading="optionLoading"
          :model="subjectForm"
          :rules="rules"
          label-position="top"
          @submit.prevent="submitSubjectForm"
        >
          <div class="drawer-form-section">
            <h4>基础信息</h4>
            <div class="form-grid-2col">
              <el-form-item label="主体名称 *" prop="name">
                <el-input v-model="subjectForm.name" placeholder="例如：商户W" />
              </el-form-item>
              <el-form-item label="主体编码 *" prop="code">
                <el-input v-model="subjectForm.code" :disabled="subjectDrawerMode === 'edit'" placeholder="merchant_w" />
              </el-form-item>
              <el-form-item label="主体类型 *" prop="subjectTypeId">
                <el-select v-model="subjectForm.subjectTypeId" placeholder="请选择">
                  <el-option v-for="item in subjectTypeOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="所属身份域 *" prop="realmId">
                <el-select v-model="subjectForm.realmId" placeholder="请选择">
                  <el-option v-for="item in realmOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="父级主体">
                <el-select v-model="subjectForm.parentSubjectId" placeholder="请选择" clearable>
                  <el-option v-for="item in availableSubjectOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="数据隔离根主体">
                <el-select v-model="subjectForm.rootSubjectId" placeholder="请选择" clearable>
                  <el-option v-for="item in availableSubjectOptions" :key="item.id" :label="item.name" :value="item.id" />
                </el-select>
              </el-form-item>
              <div class="full-row-item">
                <el-form-item label="备注">
                  <el-input v-model="subjectForm.description" type="textarea" :rows="3" placeholder="说明该主体的业务用途" />
                </el-form-item>
              </div>
            </div>
          </div>

          <div class="drawer-form-section" style="margin-top: 20px;">
            <h4>额外属性</h4>
            <div class="switch-grid">
              <label>
                <span>
                  <strong>作为隔离根节点</strong>
                  <small>开启后该主体作为隔离边界根节点</small>
                </span>
                <el-switch v-model="subjectForm.isRoot" :active-value="1" :inactive-value="0" />
              </label>
              <label>
                <span>
                  <strong>系统内置主体</strong>
                  <small>内置主体不允许删除，用于兜底</small>
                </span>
                <el-switch v-model="subjectForm.builtIn" :active-value="1" :inactive-value="0" />
              </label>
            </div>
          </div>
        </el-form>

        <div class="dashed-note-box" style="margin-top: 20px;">
          保存后只写入主体记录，不自动创建账号、不自动分配角色、不自动继承父级权限。
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer-actions">
          <el-button @click="isSubjectDrawerOpen = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitSubjectForm">保存</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- Drawer 2: Open App Drawer (Design Drawer Layout) -->
    <el-drawer
      v-model="isOpenAppDrawerOpen"
      title="开通应用"
      size="640px"
      destroy-on-close
    >
      <div class="drawer-main-content">
        <div class="drawer-form-section">
          <h4>开通信息</h4>
          <div class="form-grid-2col">
            <el-form-item label="所属主体">
              <el-input v-model="openAppForm.subjectName" disabled />
            </el-form-item>
            <el-form-item label="应用 *">
              <el-select v-model="openAppForm.appId">
                <el-option label="商城应用" value="1" />
                <el-option label="支付应用" value="2" />
                <el-option label="仓储应用" value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="实例名称 *">
              <el-input v-model="openAppForm.instanceName" />
            </el-form-item>
            <el-form-item label="实例编码 *">
              <el-input v-model="openAppForm.instanceCode" />
            </el-form-item>
            <el-form-item label="到期时间">
              <el-date-picker v-model="openAppForm.expireTime" type="date" placeholder="永不过期" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="openAppForm.status">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="2" />
              </el-select>
            </el-form-item>
          </div>
        </div>

        <div class="drawer-form-section" style="margin-top: 20px;">
          <h4>自动初始化</h4>
          <div class="check-cards-list">
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>生成实例客户端</strong>
                <small>根据应用客户端模板生成商城平台端、商家后台等入口</small>
              </div>
              <el-checkbox v-model="openAppForm.initClient" />
            </div>
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>复制资源模板</strong>
                <small>生成客户端菜单、按钮、接口资源池</small>
              </div>
              <el-checkbox v-model="openAppForm.initResource" />
            </div>
            <div class="check-card-item">
              <div class="card-left-text">
                <strong>初始化默认角色</strong>
                <small>创建管理员、运营等角色，后续在客户端详情授权</small>
              </div>
              <el-checkbox v-model="openAppForm.initRole" />
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="drawer-footer-actions">
          <el-button @click="isOpenAppDrawerOpen = false">取消</el-button>
          <el-button type="primary" :loading="saveLoading" @click="submitOpenApp">确认开通</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Lexend:wght@300;400;500;600;700&family=Source+Sans+3:wght@300;400;500;600;700&display=swap');

/* Trust & Authority Theme Variables */
.subject-page {
  --primary-color: #0369A1;      /* Security Blue */
  --primary-hover: #0284c7;
  --secondary-color: #0EA5E9;    /* Sky Blue */
  --success-color: #16A34A;      /* Protected Green */
  --success-bg: #dcfce7;
  --success-border: #bbf7d0;
  --danger-color: #dc2626;
  --bg-color: #F0F9FF;           /* Theme Background */
  --text-main: #0C4A6E;          /* Deep Navy Text */
  --text-muted: #475569;
  --border-light: rgba(226, 232, 240, 0.8);
  --font-family-display: 'Lexend', system-ui, -apple-system, sans-serif;
  --font-family-body: 'Source Sans 3', system-ui, -apple-system, sans-serif;
  
  display: flex;
  flex-direction: column;
  gap: 16px;
  font-family: var(--font-family-body);
}

/* Breadcrumbs Styling */
.top-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  margin-bottom: 12px;
  color: var(--text-muted);
  font-family: var(--font-family-display);
}

.crumb-back {
  cursor: pointer;
  font-weight: 600;
  transition: color 0.2s ease;
}

.crumb-back:hover {
  color: var(--primary-color);
}

.crumb-curr {
  color: var(--text-main);
  font-weight: 500;
}

/* Premium Hero block */
.premium-hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 30px rgba(3, 105, 161, 0.03);
}

.hero-left-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.subject-icon-box {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--bg-color);
  border: 1px solid rgba(14, 165, 233, 0.2);
  color: var(--primary-color);
  font-weight: 800;
  font-size: 22px;
  font-family: var(--font-family-display);
}

.subject-meta-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-row h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.meta-row {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--text-muted);
  font-size: 13px;
}

.dot-separator {
  color: #cbd5e1;
}

.hero-right-actions {
  display: flex;
  gap: 10px;
}

/* Detail Tabs Custom Styling */
.detail-tabs-wrapper :deep(.el-tabs__header) {
  margin-bottom: 16px;
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 10px 10px 0 0;
  padding: 0 16px;
}

.detail-tabs-wrapper :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.detail-tabs-wrapper :deep(.el-tabs__item) {
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-muted);
  font-family: var(--font-family-display);
  transition: color 0.2s ease;
}

.detail-tabs-wrapper :deep(.el-tabs__item.is-active) {
  color: var(--primary-color);
}

.detail-tabs-wrapper :deep(.el-tabs__active-bar) {
  background-color: var(--primary-color);
  height: 2px;
}

/* Basic Split Grid */
.basic-grid-layout {
  display: grid;
  grid-template-columns: 1.2fr 1fr;
  gap: 16px;
  align-items: stretch;
}

.pane-card {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.01);
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  padding-bottom: 12px;
}

.pane-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

/* Info Grid */
.info-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.info-item {
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  padding: 10px 12px;
  background: #fafcff;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  display: block;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 4px;
}

.info-item span {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-main);
}

.desc-text {
  font-weight: normal !important;
  color: var(--text-muted) !important;
  line-height: 1.5;
}

/* Note Box */
.note-box {
  background: var(--bg-color);
  border: 1px dashed rgba(14, 165, 233, 0.3);
  border-radius: 10px;
  padding: 12px 14px;
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 20px;
}

/* Visual Flow Chart */
.visual-flow-container {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  align-items: center;
  margin-top: 10px;
}

.flow-step {
  background: #ffffff;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 10px 6px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(3, 105, 161, 0.02);
  transition: all 0.2s ease;
}

.flow-step:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.05);
  border-color: var(--secondary-color);
}

.flow-step span {
  display: block;
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
}

.flow-step small {
  display: block;
  font-size: 10px;
  font-weight: 600;
  color: var(--text-main);
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.step-num {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #475569;
  font-size: 10px;
  font-weight: 700;
}

.bg-purple { background: #f3e8ff; color: #7c3aed; }
.bg-green { background: var(--success-bg); color: var(--success-color); }
.bg-orange { background: #ffedd5; color: #ea580c; }
.bg-blue { background: var(--bg-color); color: var(--primary-color); }

.flow-arrow {
  text-align: center;
  color: var(--secondary-color);
  font-weight: bold;
}

/* Summary List Items */
.summary-list-box {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  padding: 10px 14px;
  transition: all 0.2s ease;
}

.summary-list-item:hover {
  border-color: var(--secondary-color);
  background: var(--bg-color);
}

.item-left {
  display: flex;
  flex-direction: column;
}

.item-left strong {
  font-size: 14px;
  color: var(--text-main);
}

.item-left small {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

.link-btn-text {
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  margin-left: 12px;
  transition: color 0.2s ease;
}

.link-btn-text:hover {
  color: var(--primary-hover);
}

.link-btn-text.danger {
  color: var(--danger-color);
}

/* App summary cards */
.summary-app-card {
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.2s ease;
}

.summary-app-card:hover {
  border-color: var(--primary-color);
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.04);
  background: var(--bg-color);
}

.summary-app-card.full-card {
  margin-bottom: 12px;
}

.app-left h4 {
  margin: 0;
  font-size: 14px;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.app-left p {
  margin: 4px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}

.pills-row {
  display: flex;
  gap: 6px;
  margin-top: 10px;
}

.pill-badge {
  background: #ffffff;
  border: 1px solid rgba(226, 232, 240, 0.8);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 11px;
  color: var(--text-muted);
  transition: all 0.2s ease;
}

.pill-badge:hover {
  border-color: var(--secondary-color);
  color: var(--primary-color);
}

.app-right-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

/* Apps Grid */
.apps-grid-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

/* Tree Table */
.tree-table-shell {
  display: flex;
  flex-direction: column;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  overflow: hidden;
}

.tree-row {
  display: grid;
  grid-template-columns: 32px 1.5fr 1fr 1fr 120px;
  align-items: center;
  height: 48px;
  border-bottom: 1px solid rgba(241, 245, 249, 0.8);
  font-size: 13px;
  padding: 0 16px;
  transition: background 0.2s ease;
}

.tree-row:hover {
  background: var(--bg-color);
}

.tree-row:last-child {
  border-bottom: none;
}

.tree-row.head {
  height: 38px;
  background: #fafcff;
  color: var(--text-main);
  font-weight: 700;
  font-family: var(--font-family-display);
}

.tree-row.indent {
  padding-left: 36px;
}

.toggle-icon {
  color: var(--text-muted);
  font-weight: bold;
}

.nested-text {
  color: var(--text-muted);
}

/* Badges styling */
.custom-badge {
  display: inline-flex;
  align-items: center;
  border-radius: 4px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  border: 1px solid transparent;
  font-family: var(--font-family-display);
}

.custom-badge.green {
  color: var(--success-color);
  background: var(--success-bg);
  border-color: var(--success-border);
}

.custom-badge.red {
  color: var(--danger-color);
  background: #fee2e2;
  border-color: #fecaca;
}

.custom-badge.blue {
  color: var(--primary-color);
  background: var(--bg-color);
  border-color: rgba(14, 165, 233, 0.2);
}

.custom-badge.orange {
  color: #c2410c;
  background: #ffedd5;
  border-color: #fed7aa;
}

.custom-badge.gray {
  color: var(--text-muted);
  background: #f1f5f9;
  border-color: #e2e8f0;
}

/* Header & Filter layouts */
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 18px;
}

.title-wrap h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.title-wrap p {
  margin: 6px 0 0;
  font-size: 14px;
  color: var(--text-muted);
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.filter-card {
  margin-bottom: 16px;
  border-radius: 10px;
  border: 1px solid var(--border-light);
  box-shadow: 0 4px 20px rgba(3, 105, 161, 0.01);
}

.filter-flex-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr) auto;
  gap: 16px;
  align-items: flex-end;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 13px;
  color: var(--text-main);
  font-weight: 600;
  font-family: var(--font-family-display);
}

.filter-buttons {
  display: flex;
  gap: 10px;
}

/* Main Table card */
.table-card {
  border-radius: 10px;
  border: 1px solid var(--border-light);
}

.card-header-title {
  margin-bottom: 16px;
}

.card-header-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
  font-family: var(--font-family-display);
}

.card-header-title p {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--text-muted);
}

.premium-table {
  border: 1px solid rgba(241, 245, 249, 0.8);
  border-radius: 8px;
  overflow: hidden;
}

.subject-name-link {
  font-weight: 700;
  color: var(--text-main);
  cursor: pointer;
  transition: color 0.15s;
}

.subject-name-link:hover {
  color: var(--primary-color);
}

.code-text {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  background: #f1f5f9;
  color: var(--text-muted);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.row-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.link-btn {
  cursor: pointer;
  color: var(--primary-color);
  font-weight: 600;
  font-size: 13px;
  transition: color 0.2s ease;
}

.link-btn:hover {
  color: var(--primary-hover);
}

.link-more-btn {
  cursor: pointer;
  color: var(--text-muted);
  font-weight: 600;
  font-size: 13px;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s ease;
}

.link-more-btn:hover {
  color: var(--primary-color);
}

.danger-item {
  color: var(--danger-color) !important;
}

/* Pagination */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.total-text {
  font-size: 13px;
  color: var(--text-muted);
}

/* Drawer styles */
.drawer-main-content {
  padding: 10px 0;
}

.drawer-form-section {
  border: 1px solid var(--border-light);
  border-radius: 10px;
  padding: 16px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(3, 105, 161, 0.01);
}

.drawer-form-section h4 {
  margin: 0 0 14px;
  font-size: 14px;
  color: var(--text-main);
  font-weight: 700;
  border-left: 3px solid var(--primary-color);
  padding-left: 8px;
  font-family: var(--font-family-display);
}

.form-grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.full-row-item {
  grid-column: 1 / -1;
}

.dashed-note-box {
  background: var(--bg-color);
  border: 1px dashed rgba(14, 165, 233, 0.3);
  border-radius: 10px;
  padding: 12px;
  color: var(--text-main);
  font-size: 13px;
  line-height: 1.6;
}

.drawer-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 10px;
}

/* Check cards */
.check-cards-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.check-card-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid var(--border-light);
  border-radius: 8px;
  padding: 12px;
  transition: all 0.2s ease;
}

.check-card-item:hover {
  border-color: var(--primary-color);
  background: var(--bg-color);
}

.card-left-text {
  display: flex;
  flex-direction: column;
}

.card-left-text strong {
  font-size: 13px;
  color: var(--text-main);
}

.card-left-text small {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 2px;
}

@media (max-width: 1400px) {
  .filter-flex-row {
    grid-template-columns: 1fr 1fr;
  }
  .filter-buttons {
    grid-column: span 2;
    justify-content: flex-end;
  }
}
</style>
